package com.SkyWay.views.admin;

import com.SkyWay.modules.CaracteristicaTarifa.domain.model.CaracteristicaTarifa;
import com.SkyWay.modules.CaracteristicaTarifa.domain.service.CaracteristicaTarifaService;
import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import org.omnifaces.cdi.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component("tarifaBean")
@ViewScoped
public class TarifaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private CaracteristicaTarifaService caracteristicaService;

    // Listas Administrativas (Traen TODO: Activos e Inactivos para las tablas de gestión)
    private List<Tarifa> listaTarifasAdmin;
    private List<CaracteristicaTarifa> listaCaracteristicasAdmin;

    // Listas Activas (Solo Activos para la asignación comercial a tarifas)
    private List<Tarifa> listaTarifasActivas;
    private List<CaracteristicaTarifa> listaCaracteristicasActivas;

    private Tarifa tarifaSeleccionada;
    private CaracteristicaTarifa nuevaCaracteristica;
    private boolean esNuevo;
    private int tabActiva = 0;

    @PostConstruct
    public void init() {
        cargarTarifas();
        cargarCaracteristicas();
        this.nuevaCaracteristica = new CaracteristicaTarifa();
    }

    public void cargarTarifas() {
        this.listaTarifasAdmin = tarifaService.findAllAdmin();
        this.listaTarifasActivas = tarifaService.findAll();
    }

    public void cargarCaracteristicas() {
        this.listaCaracteristicasAdmin = caracteristicaService.findAllAdmin();
        this.listaCaracteristicasActivas = caracteristicaService.findAll();
    }

    // --- ACCIÓN: NUEVA TARIFA (GARANTIZA CARACTERÍSTICAS NUEVAS) ---
    public void prepararNuevaTarifa() {
        this.esNuevo = true;
        this.tarifaSeleccionada = new Tarifa();
        this.tarifaSeleccionada.setTarifaCaracteristicas(new ArrayList<>());

        // Cargar catálogo global de características fresco
        cargarCaracteristicas();

        for (CaracteristicaTarifa cGlobal : (listaCaracteristicasActivas != null ? listaCaracteristicasActivas : listaCaracteristicasAdmin)) {
            TarifaCaracteristica tc = new TarifaCaracteristica();
            tc.setTarifa(this.tarifaSeleccionada);
            tc.setCaracteristica(cGlobal);

            if ("boolean".equalsIgnoreCase(cGlobal.getTipoDato())) {
                tc.setValorBool(false);
            } else if ("int".equalsIgnoreCase(cGlobal.getTipoDato())) {
                tc.setValorInt(0);
            } else {
                tc.setValor("");
            }
            this.tarifaSeleccionada.getTarifaCaracteristicas().add(tc);
        }
    }

    // --- ACCIÓN: EDITAR TARIFA (PREVIENE CONTAMINACIÓN ENTRE OBJETOS) ---
    public void prepararEdicion(Tarifa tarifaParam) {
        this.esNuevo = false;

        // Refrescar objeto completo de la base de datos
        this.tarifaSeleccionada = tarifaService.findById(tarifaParam.getIdTarifa())
                .orElse(tarifaParam);

        if (this.tarifaSeleccionada.getTarifaCaracteristicas() == null) {
            this.tarifaSeleccionada.setTarifaCaracteristicas(new ArrayList<>());
        }

        cargarCaracteristicas();

        // Mapear características existentes
        Map<Integer, TarifaCaracteristica> mapaExistentes = this.tarifaSeleccionada.getTarifaCaracteristicas()
                .stream()
                .collect(Collectors.toMap(
                        tc -> tc.getCaracteristica().getIdCaracteristica(),
                        tc -> tc,
                        (existing, replacement) -> existing
                ));

        List<TarifaCaracteristica> listaSincronizada = new ArrayList<>();

        for (CaracteristicaTarifa cGlobal : (listaCaracteristicasActivas != null ? listaCaracteristicasActivas : listaCaracteristicasAdmin)) {
            if (mapaExistentes.containsKey(cGlobal.getIdCaracteristica())) {
                listaSincronizada.add(mapaExistentes.get(cGlobal.getIdCaracteristica()));
            } else {
                // Nueva característica del catálogo aún no asociada a esta tarifa
                TarifaCaracteristica tc = new TarifaCaracteristica();
                tc.setTarifa(this.tarifaSeleccionada);
                tc.setCaracteristica(cGlobal);
                if ("boolean".equalsIgnoreCase(cGlobal.getTipoDato())) tc.setValorBool(false);
                else if ("int".equalsIgnoreCase(cGlobal.getTipoDato())) tc.setValorInt(0);
                else tc.setValor("");

                listaSincronizada.add(tc);
            }
        }

        this.tarifaSeleccionada.setTarifaCaracteristicas(listaSincronizada);
    }

    public void guardarTarifa() {
        if (tarifaSeleccionada.getNombre() == null || tarifaSeleccionada.getNombre().isBlank()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre de la tarifa es obligatorio.");
            return;
        }

        try {
            // Asegurar la relación bidireccional antes de guardar
            if (tarifaSeleccionada.getTarifaCaracteristicas() != null) {
                for (TarifaCaracteristica tc : tarifaSeleccionada.getTarifaCaracteristicas()) {
                    tc.setTarifa(tarifaSeleccionada);
                }
            }

            tarifaService.save(tarifaSeleccionada);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", esNuevo ? "Tarifa creada." : "Tarifa actualizada.");

            cargarTarifas();
            org.primefaces.PrimeFaces.current().executeScript("PF('dlgTarifa').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar la tarifa: " + e.getMessage());
        }
    }

    public void guardarNuevaCaracteristicaCatálogo() {
        try {
            if (nuevaCaracteristica.getNombre() == null || nuevaCaracteristica.getNombre().isBlank()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "El nombre del beneficio es requerido.");
                return;
            }
            caracteristicaService.save(nuevaCaracteristica);
            addMessage(FacesMessage.SEVERITY_INFO, "Catálogo Actualizado", "Beneficio agregado exitosamente.");

            this.nuevaCaracteristica = new CaracteristicaTarifa();
            cargarCaracteristicas();
            org.primefaces.PrimeFaces.current().executeScript("PF('dlgNuevaCaracteristica').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar la característica.");
        }
    }

    public void eliminarTarifa(Tarifa tarifa) {
        try {
            tarifaService.deleteById(tarifa.getIdTarifa());
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Tarifa dada de baja exitosamente.");
            cargarTarifas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la tarifa.");
        }
    }

    public void reactivarTarifa(Tarifa tarifa) {
        try {
            tarifaService.reactivar(tarifa.getIdTarifa());
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Tarifa reactivada exitosamente.");
            cargarTarifas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar la tarifa: " + e.getMessage());
        }
    }

    public void eliminarCaracteristica(Integer id) {
        try {
            caracteristicaService.eliminar(id);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Característica dada de baja exitosamente.");
            cargarCaracteristicas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo desactivar la característica.");
        }
    }

    public void reactivarCaracteristica(Integer id) {
        try {
            caracteristicaService.reactivar(id);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Característica reactivada exitosamente.");
            cargarCaracteristicas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar la característica: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    // Getters Listas Administrativas
    public List<Tarifa> getListaTarifasAdmin() { return listaTarifasAdmin; }
    public List<CaracteristicaTarifa> getListaCaracteristicasAdmin() { return listaCaracteristicasAdmin; }

    // Getters Listas Activas
    public List<Tarifa> getListaTarifasActivas() { return listaTarifasActivas; }
    public List<CaracteristicaTarifa> getListaCaracteristicasActivas() { return listaCaracteristicasActivas; }

    // Compatibilidad
    public List<Tarifa> getListaTarifas() { return listaTarifasAdmin; }
    public List<CaracteristicaTarifa> getListaCaracteristicasGlobales() { return listaCaracteristicasAdmin; }
    public Tarifa getTarifaSeleccionada() { return tarifaSeleccionada; }
    public CaracteristicaTarifa getNuevaCaracteristica() { return nuevaCaracteristica; }
    public boolean isEsNuevo() { return esNuevo; }
    public int getTabActiva() { return tabActiva; }
    public void setTabActiva(int tabActiva) { this.tabActiva = tabActiva; }
}