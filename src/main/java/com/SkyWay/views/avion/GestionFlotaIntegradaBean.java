package com.SkyWay.views.avion;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.asiento.domain.repository.AsientoRepository;
import com.SkyWay.modules.avion.domain.exception.AvionValidationException;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;
import com.SkyWay.modules.fabricante.domain.model.Fabricante;
import com.SkyWay.modules.fabricante.domain.service.FabricanteService;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.modeloavion.domain.service.ModeloAvionService;
import com.SkyWay.util.Logger;
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

@Named("gestionFlotaIntegradaBean")
@ViewScoped
public class GestionFlotaIntegradaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private AvionService avionService;

    @Autowired
    private ModeloAvionService modeloAvionService;

    @Autowired
    private FabricanteService fabricanteService;

    @Autowired
    private ClaseAsientoRepository claseAsientoRepository;

    @Autowired
    private AsientoRepository asientoRepository;

    // Listas Administrativas (Traen TODO: Activos e Inactivos para las tablas de gestión)
    private List<Avion> listaAvionesAdmin;
    private List<ModeloAvion> listaModelosAdmin;
    private List<Fabricante> listaFabricantesAdmin;
    private List<ClaseAsiento> listaClasesAdmin;

    // Listas Operativas / Activas (Traen SOLO Activos para selects, dropdowns y modales)
    private List<Avion> listaAvionesActivos;
    private List<ModeloAvion> listaModelosActivos;
    private List<Fabricante> listaFabricantesActivos;
    private List<ClaseAsiento> listaClasesActivas;

    // Entidades para formularios
    private Avion avionSeleccionado;
    private ModeloAvion modeloSeleccionado;
    private Fabricante fabricanteSeleccionado;

    // Variables Auxiliares
    private Integer idModeloParaAvion;
    private Integer idFabricanteParaModelo;
    private Date fechaMantenimientoAux;
    private boolean modoEdicionAvion;
    private int totalAsientosCalculados;

    private Map<Integer, Integer> capacidadesClaseMap = new HashMap<>();
    private Map<Integer, List<Asiento>> asientosPorFilaMap = new TreeMap<>();
    private int tabActiva = 0;

    @PostConstruct
    public void init() {
        cargarTodo();
    }

    public void cargarTodo() {
        // 1. Listas administrativas completas (para las dataTables del admin)
        this.listaAvionesAdmin = avionService.findAllAdmin();
        this.listaModelosAdmin = modeloAvionService.findAllAdmin();
        this.listaFabricantesAdmin = fabricanteService.findAllAdmin();
        this.listaClasesAdmin = claseAsientoRepository.findAllAdmin();

        // 2. Listas de entidades activas (para dropdowns de asignación y modales)
        this.listaAvionesActivos = avionService.findAll();
        this.listaFabricantesActivos = fabricanteService.findAll();
        this.listaModelosActivos = modeloAvionService.findAll().stream()
                .filter(m -> m.getFabricante() == null || Boolean.TRUE.equals(m.getFabricante().getActivo()))
                .collect(Collectors.toList());
        this.listaClasesActivas = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();
    }

    // ==========================================
    // 1. GESTIÓN DE FABRICANTES
    // ==========================================
    public void prepararNuevoFabricante() {
        this.fabricanteSeleccionado = new Fabricante();
        PrimeFaces.current().executeScript("PF('dlgFabricante').show();");
    }

    public void guardarFabricante() {
        try {
            fabricanteService.guardar(fabricanteSeleccionado);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Fabricante registrado correctamente.");
            PrimeFaces.current().executeScript("PF('dlgFabricante').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void eliminarFabricante(Integer id) {
        try {
            fabricanteService.eliminar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Fabricante dado de baja exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar un fabricante con modelos asociados.");
        }
    }

    public void reactivarFabricante(Integer id) {

        try {
            fabricanteService.reactivar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Fabricante reactivado exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar el fabricante: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. GESTIÓN DE MODELOS DE AVIÓN
    // ==========================================
    public void prepararNuevoModelo() {
        this.modeloSeleccionado = new ModeloAvion();
        this.idFabricanteParaModelo = null;
        this.listaFabricantesActivos = fabricanteService.findAll();
        PrimeFaces.current().executeScript("PF('dlgModelo').show();");
    }

    public void guardarModelo() {
        try {
            if (idFabricanteParaModelo != null) {
                Fabricante fab = listaFabricantesActivos != null ? listaFabricantesActivos.stream()
                        .filter(f -> f.getIdFabricante().equals(idFabricanteParaModelo))
                        .findFirst().orElse(null) : null;
                if (fab == null && listaFabricantesAdmin != null) {
                    fab = listaFabricantesAdmin.stream()
                            .filter(f -> f.getIdFabricante().equals(idFabricanteParaModelo))
                            .findFirst().orElse(null);
                }
                modeloSeleccionado.setFabricante(fab);
            }
            modeloAvionService.guardar(modeloSeleccionado);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Modelo de avión guardado.");
            PrimeFaces.current().executeScript("PF('dlgModelo').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void eliminarModelo(Integer id) {
        try {
            modeloAvionService.eliminar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Modelo dado de baja exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar un modelo con aeronaves asignadas.");
        }
    }

    public void reactivarModelo(Integer id) {
        try {
            modeloAvionService.reactivar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Modelo reactivado exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar el modelo: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. GESTIÓN DE AERONAVES (AVIÓN)
    // ==========================================
    public void prepararNuevoAvion() {
        this.avionSeleccionado = new Avion();
        this.avionSeleccionado.setEstadoDeMantenimiento("Operativo");
        this.avionSeleccionado.setCapacidadDeCarga(15000);
        this.avionSeleccionado.setAnoDeFabricacion(LocalDate.now().getYear());
        this.idModeloParaAvion = null;
        this.modoEdicionAvion = false;
        this.totalAsientosCalculados = 0;

        this.listaModelosActivos = modeloAvionService.findAll().stream()
                .filter(m -> m.getFabricante() == null || Boolean.TRUE.equals(m.getFabricante().getActivo()))
                .collect(Collectors.toList());
        this.listaClasesActivas = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();

        LocalDate seisMeses = LocalDate.now().plusMonths(6);
        this.fechaMantenimientoAux = Date.from(seisMeses.atStartOfDay(ZoneId.systemDefault()).toInstant());

        this.capacidadesClaseMap.clear();
        if (listaClasesActivas != null) {
            for (ClaseAsiento c : listaClasesActivas) {
                capacidadesClaseMap.put(c.getIdClase(), 0);
            }
        }

        PrimeFaces.current().executeScript("PF('dlgAvion').show();");
    }

    public void prepararEdicionAvion(Avion a) {
        if (a == null) {
            return;
        }

        Logger.logInfo(a.toString());
        this.avionSeleccionado = a;
        this.modoEdicionAvion = true;
        this.idModeloParaAvion = (a.getModeloAvion() != null) ? a.getModeloAvion().getIdModelo() : null;

        this.listaModelosActivos = modeloAvionService.findAll().stream()
                .filter(m -> m.getFabricante() == null || Boolean.TRUE.equals(m.getFabricante().getActivo()))
                .collect(Collectors.toList());
        this.listaClasesActivas = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();

        // Carga de la fecha de próximo mantenimiento
        if (a.getFechaProximoMantenimiento() != null) {
            this.fechaMantenimientoAux = new Date(a.getFechaProximoMantenimiento().getTime());
        } else {
            this.fechaMantenimientoAux = null;
        }

        // Inicializar mapa de capacidades
        this.capacidadesClaseMap.clear();
        if (listaClasesActivas != null) {
            for (ClaseAsiento c : listaClasesActivas) {
                capacidadesClaseMap.put(c.getIdClase(), 0);
            }
        }

        // Mapear capacidades registradas actualmente en el avión
        if (a.getCapacidadClases1() != null && !a.getCapacidadClases1().isEmpty()) {
            for (CapacidadClase cc : a.getCapacidadClases1()) {
                if (cc.getClaseAsiento1() != null && cc.getCantidad() != null) {
                    capacidadesClaseMap.put(cc.getClaseAsiento1().getIdClase(), cc.getCantidad());
                }
            }
        }

        recalcularTotalAsientos();
    }

    public void recalcularTotalAsientos() {
        this.totalAsientosCalculados = capacidadesClaseMap.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void guardarAvion() {
        try {
            if (idModeloParaAvion != null) {
                ModeloAvion mod = listaModelosActivos != null ? listaModelosActivos.stream()
                        .filter(m -> m.getIdModelo().equals(idModeloParaAvion))
                        .findFirst().orElse(null) : null;
                if (mod == null && listaModelosAdmin != null) {
                    mod = listaModelosAdmin.stream()
                            .filter(m -> m.getIdModelo().equals(idModeloParaAvion))
                            .findFirst().orElse(null);
                }
                avionSeleccionado.setModeloAvion(mod);
            }

            if (fechaMantenimientoAux != null) {
                avionSeleccionado.setFechaProximoMantenimiento(new Timestamp(fechaMantenimientoAux.getTime()));
            }

            // Ejecuta guardar y dispara el TRIGGER PostgreSQL
            avionService.saveConCapacidades(avionSeleccionado, capacidadesClaseMap);

            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave certificada y asientos generados por Trigger.");
            PrimeFaces.current().executeScript("PF('dlgAvion').hide();");
        } catch (AvionValidationException ave) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación Operativa", ave.getMessage());
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void verMapaAsientos(Avion a) {
        this.avionSeleccionado = a;
        List<Asiento> asientos = asientoRepository.findByAvion_IdAvionOrderByFilaAscLetraAsc(a.getIdAvion());

        if (asientos != null) {
            this.asientosPorFilaMap = asientos.stream()
                    .collect(Collectors.groupingBy(Asiento::getFila, TreeMap::new, Collectors.toList()));
        } else {
            this.asientosPorFilaMap.clear();
        }

        PrimeFaces.current().executeScript("PF('dlgMapaAsientos').show();");
    }

    public void eliminarAvion(Integer id) {
        try {
            avionService.deleteById(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave dada de baja.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar la aeronave. Posee itinerarios asociados.");
        }
    }

    public void reactivarAvion(Integer id) {
        try {
            avionService.reactivar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave reactivada exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar la aeronave: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public int getTabActiva() { return tabActiva; }
    public void setTabActiva(int tabActiva) { this.tabActiva = tabActiva; }
    // Getters Listas Administrativas
    public List<Avion> getListaAvionesAdmin() { return listaAvionesAdmin; }
    public List<ModeloAvion> getListaModelosAdmin() { return listaModelosAdmin; }
    public List<Fabricante> getListaFabricantesAdmin() { return listaFabricantesAdmin; }
    public List<ClaseAsiento> getListaClasesAdmin() { return listaClasesAdmin; }

    // Getters Listas Activas
    public List<Avion> getListaAvionesActivos() { return listaAvionesActivos; }
    public List<ModeloAvion> getListaModelosActivos() { return listaModelosActivos; }
    public List<Fabricante> getListaFabricantesActivos() { return listaFabricantesActivos; }
    public List<ClaseAsiento> getListaClasesActivas() { return listaClasesActivas; }

    // Compatibilidad retroactiva
    public List<Avion> getListaAviones() { return listaAvionesAdmin; }
    public List<ModeloAvion> getListaModelos() { return listaModelosAdmin; }
    public List<Fabricante> getListaFabricantes() { return listaFabricantesAdmin; }
    public List<ClaseAsiento> getListaClases() { return listaClasesAdmin; }
    public List<Fabricante> getFabricantesActivos() { return listaFabricantesActivos; }
    public List<ModeloAvion> getModelosActivos() { return listaModelosActivos; }
    public List<ClaseAsiento> getClasesActivas() { return listaClasesActivas; }
    public Avion getAvionSeleccionado() { return avionSeleccionado; }
    public void setAvionSeleccionado(Avion avionSeleccionado) { this.avionSeleccionado = avionSeleccionado; }
    public ModeloAvion getModeloSeleccionado() { return modeloSeleccionado; }
    public void setModeloSeleccionado(ModeloAvion modeloSeleccionado) { this.modeloSeleccionado = modeloSeleccionado; }
    public Fabricante getFabricanteSeleccionado() { return fabricanteSeleccionado; }
    public void setFabricanteSeleccionado(Fabricante fabricanteSeleccionado) { this.fabricanteSeleccionado = fabricanteSeleccionado; }
    public Integer getIdModeloParaAvion() { return idModeloParaAvion; }
    public void setIdModeloParaAvion(Integer idModeloParaAvion) { this.idModeloParaAvion = idModeloParaAvion; }
    public Integer getIdFabricanteParaModelo() { return idFabricanteParaModelo; }
    public void setIdFabricanteParaModelo(Integer idFabricanteParaModelo) { this.idFabricanteParaModelo = idFabricanteParaModelo; }
    public Date getFechaMantenimientoAux() { return fechaMantenimientoAux; }
    public void setFechaMantenimientoAux(Date fechaMantenimientoAux) { this.fechaMantenimientoAux = fechaMantenimientoAux; }
    public boolean isModoEdicionAvion() { return modoEdicionAvion; }
    public Map<Integer, Integer> getCapacidadesClaseMap() { return capacidadesClaseMap; }
    public Map<Integer, List<Asiento>> getAsientosPorFilaMap() { return asientosPorFilaMap; }
    public int getTotalAsientosCalculados() { return totalAsientosCalculados; }
}