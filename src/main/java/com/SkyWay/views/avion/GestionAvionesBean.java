package com.SkyWay.views.avion;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.asiento.domain.repository.AsientoRepository;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.repository.AvionRepository;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;

import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Named("gestionAvionesBean")
@ViewScoped
public class GestionAvionesBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private AvionService avionService;

    @Autowired
    private AvionRepository avionRepository;

    @Autowired
    private ModeloAvionRepository modeloAvionRepository;

    @Autowired
    private ClaseAsientoRepository claseAsientoRepository;

    @Autowired
    private AsientoRepository asientoRepository;

    private List<Avion> listaAviones;
    private List<ModeloAvion> listaModelos;
    private List<ClaseAsiento> listaClases;

    private List<Asiento> asientosMapaVisual;
    private Map<Integer, List<Asiento>> asientosPorFilaMap = new TreeMap<>();

    private Avion avionSeleccionado;
    private Integer idModeloSeleccionado;
    private Date fechaMantenimientoAux;
    private boolean modoEdicion = false;
    private int totalAsientosCalculados = 0;

    private Map<Integer, Integer> capacidadesClaseMap = new HashMap<>();

    @PostConstruct
    public void init() {
        cargarListas();
    }

    public void cargarListas() {
        this.listaAviones = avionService.findAll();
        this.listaModelos = modeloAvionRepository.findAll();
        this.listaClases = claseAsientoRepository.findAll();
    }

    public void recalcularTotalAsientos() {
        this.totalAsientosCalculados = capacidadesClaseMap.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void prepararNuevoAvion() {
        this.avionSeleccionado = new Avion();
        this.avionSeleccionado.setEstadoDeMantenimiento("Operativo");
        this.avionSeleccionado.setCapacidadDeCarga(15000);
        this.avionSeleccionado.setAnoDeFabricacion(LocalDate.now().getYear());
        this.idModeloSeleccionado = null;

        // Asignar por defecto 6 meses a futuro la fecha de próximo mantenimiento
        LocalDate seisMeses = LocalDate.now().plusMonths(6);
        this.fechaMantenimientoAux = Date.from(seisMeses.atStartOfDay(ZoneId.systemDefault()).toInstant());

        this.modoEdicion = false;
        this.totalAsientosCalculados = 0;

        this.capacidadesClaseMap.clear();
        for (ClaseAsiento c : listaClases) {
            capacidadesClaseMap.put(c.getIdClase(), 0);
        }

        PrimeFaces.current().executeScript("PF('dlgAvion').show();");
    }

    public void prepararEdicion(Avion a) {
        this.avionSeleccionado = a;
        this.modoEdicion = true;
        this.idModeloSeleccionado = (a.getModeloAvion() != null) ? a.getModeloAvion().getIdModelo() : null;
        this.fechaMantenimientoAux = (a.getFechaProximoMantenimiento() != null) ? new Date(a.getFechaProximoMantenimiento().getTime()) : null;

        this.capacidadesClaseMap.clear();
        for (ClaseAsiento c : listaClases) {
            capacidadesClaseMap.put(c.getIdClase(), 0);
        }

        if (a.getCapacidadClases1() != null) {
            for (CapacidadClase cc : a.getCapacidadClases1()) {
                if (cc.getClaseAsiento1() != null) {
                    capacidadesClaseMap.put(cc.getClaseAsiento1().getIdClase(), cc.getCantidad());
                }
            }
        }
        recalcularTotalAsientos();

        PrimeFaces.current().executeScript("PF('dlgAvion').show();");
    }

    public void guardarAvion() {
        try {
            // 1. Validar Matrícula (Tail Number)
            if (avionSeleccionado.getNumeroDeRegistro() == null || avionSeleccionado.getNumeroDeRegistro().trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Campo Requerido", "Debe ingresar el Número de Registro (Matrícula).");
                return;
            }

            String matClean = avionSeleccionado.getNumeroDeRegistro().trim().toUpperCase();
            if (!matClean.matches("^[A-Z0-9-]{3,12}$")) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Matrícula Inválida", "La matrícula debe contener de 3 a 12 caracteres (Letras, Números y Guion). Ej: CC-BBA, N707SA.");
                return;
            }

            // Validar Duplicidad de Matrícula al crear nuevo
            if (!modoEdicion && avionRepository.findAll().stream().anyMatch(a -> matClean.equalsIgnoreCase(a.getNumeroDeRegistro()))) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Matrícula Existente", "Ya existe una aeronave registrada con la matrícula " + matClean);
                return;
            }
            avionSeleccionado.setNumeroDeRegistro(matClean);

            // 2. Validar Selección de Modelo
            if (idModeloSeleccionado == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Modelo Requerido", "Debe seleccionar el modelo de avión.");
                return;
            }

            // 3. Validar Año de Fabricación
            int anioActual = LocalDate.now().getYear();
            if (avionSeleccionado.getAnoDeFabricacion() == null || avionSeleccionado.getAnoDeFabricacion() < 1960 || avionSeleccionado.getAnoDeFabricacion() > anioActual + 1) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Año Inválido", "El año de fabricación debe estar entre 1960 y " + (anioActual + 1));
                return;
            }

            // 4. Validar Mantenimiento vs Estado
            if (fechaMantenimientoAux == null) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Fecha Requerida", "Debe seleccionar la fecha de próximo mantenimiento.");
                return;
            }

            LocalDate fechaMaintLocalDate = fechaMantenimientoAux.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if ("Operativo".equalsIgnoreCase(avionSeleccionado.getEstadoDeMantenimiento()) && fechaMaintLocalDate.isBefore(LocalDate.now())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Incoherencia Mantenimiento", "No se puede registrar una aeronave como 'Operativo' si la fecha de mantenimiento ya venció. Cambie el estado a 'En Mantenimiento'.");
                return;
            }

            // 5. Validar Capacidades de Asientos
            recalcularTotalAsientos();
            if (totalAsientosCalculados <= 0) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Capacidad Nula", "Debe asignar al menos 1 asiento en cualquiera de las clases de cabina.");
                return;
            }

            if (totalAsientosCalculados > 850) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Exceso de Capacidad", "La suma total de pasajeros (" + totalAsientosCalculados + ") excede el límite máximo aeronáutico mundial (850 pax).");
                return;
            }

            ModeloAvion m = modeloAvionRepository.findById(idModeloSeleccionado).orElse(null);
            avionSeleccionado.setModeloAvion(m);
            avionSeleccionado.setFechaProximoMantenimiento(new Timestamp(fechaMantenimientoAux.getTime()));

            // Guardar e invocar el Trigger PostgreSQL que genera la tabla Asiento
            avionService.saveConCapacidades(avionSeleccionado, capacidadesClaseMap);

            cargarListas();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave certificada correctamente. Distribución de asientos regenerada.");
            PrimeFaces.current().executeScript("PF('dlgAvion').hide();");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al Guardar", e.getMessage());
        }
    }

    public void verMapaAsientos(Avion a) {
        this.avionSeleccionado = a;
        this.asientosMapaVisual = asientoRepository.findByAvion_IdAvionOrderByFilaAscLetraAsc(a.getIdAvion());

        if (asientosMapaVisual != null) {
            this.asientosPorFilaMap = asientosMapaVisual.stream()
                    .collect(Collectors.groupingBy(
                            Asiento::getFila,
                            TreeMap::new,
                            Collectors.toList()
                    ));
        } else {
            this.asientosPorFilaMap.clear();
        }

        PrimeFaces.current().executeScript("PF('dlgMapaAsientos').show();");
    }

    public void eliminarAvion(Integer id) {
        try {
            avionService.deleteById(id);
            cargarListas();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave eliminada de la flota.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Restricción de Integridad", "No se puede eliminar la aeronave. Posee itinerarios o boletos vinculados.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<Avion> getListaAviones() { return listaAviones; }
    public List<ModeloAvion> getListaModelos() { return listaModelos; }
    public List<ClaseAsiento> getListaClases() { return listaClases; }
    public List<Asiento> getAsientosMapaVisual() { return asientosMapaVisual; }
    public Map<Integer, List<Asiento>> getAsientosPorFilaMap() { return asientosPorFilaMap; }
    public Avion getAvionSeleccionado() { return avionSeleccionado; }
    public void setAvionSeleccionado(Avion avionSeleccionado) { this.avionSeleccionado = avionSeleccionado; }
    public Integer getIdModeloSeleccionado() { return idModeloSeleccionado; }
    public void setIdModeloSeleccionado(Integer idModeloSeleccionado) { this.idModeloSeleccionado = idModeloSeleccionado; }
    public Date getFechaMantenimientoAux() { return fechaMantenimientoAux; }
    public void setFechaMantenimientoAux(Date fechaMantenimientoAux) { this.fechaMantenimientoAux = fechaMantenimientoAux; }
    public boolean isModoEdicion() { return modoEdicion; }
    public Map<Integer, Integer> getCapacidadesClaseMap() { return capacidadesClaseMap; }
    public void setCapacidadesClaseMap(Map<Integer, Integer> capacidadesClaseMap) { this.capacidadesClaseMap = capacidadesClaseMap; }
    public int getTotalAsientosCalculados() { return totalAsientosCalculados; }
}