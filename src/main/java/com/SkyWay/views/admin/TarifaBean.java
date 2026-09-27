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

    private List<Tarifa> listaTarifas;
    private List<CaracteristicaTarifa> listaCaracteristicasGlobales;

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
        this.listaTarifas = tarifaService.findAll();
    }

    public void cargarCaracteristicas() {
        this.listaCaracteristicasGlobales = caracteristicaService.findAll();
    }

    // --- ACCIÓN: NUEVA TARIFA (GARANTIZA CARACTERÍSTICAS NUEVAS) ---
    public void prepararNuevaTarifa() {
        this.esNuevo = true;
        this.tarifaSeleccionada = new Tarifa();
        this.tarifaSeleccionada.setTarifaCaracteristicas(new ArrayList<>());

        // Cargar catálogo global de características fresco
        cargarCaracteristicas();

        for (CaracteristicaTarifa cGlobal : listaCaracteristicasGlobales) {
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

        for (CaracteristicaTarifa cGlobal : listaCaracteristicasGlobales) {
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
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Tarifa eliminada.");
            cargarTarifas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar la tarifa.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<Tarifa> getListaTarifas() { return listaTarifas; }
    public List<CaracteristicaTarifa> getListaCaracteristicasGlobales() { return listaCaracteristicasGlobales; }
    public Tarifa getTarifaSeleccionada() { return tarifaSeleccionada; }
    public CaracteristicaTarifa getNuevaCaracteristica() { return nuevaCaracteristica; }
    public boolean isEsNuevo() { return esNuevo; }
    public int getTabActiva() { return tabActiva; }
    public void setTabActiva(int tabActiva) { this.tabActiva = tabActiva; }
}