package com.SkyWay.views.Itinerario;

import com.SkyWay.dto.VueloDTO;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import com.SkyWay.modules.itinerario.presentation.dto.PuntoMapaDTO;
import com.SkyWay.modules.tarifa.presentation.dto.TarifaDTO;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.util.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Scope;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
//import org.omnifaces.cdi.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope; 
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Component
@Named("itinerarioDetalleBean")
@Scope(value = "view", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ItinerarioDetalleBean implements Serializable {

    private ItinerarioDTO selectedVuelo;

    private String selectedVueloParadas;
    private Integer itinerario;
    private List<ItinerarioDTO> vuelosIda;
    private List<ItinerarioDTO> vuelosRegreso;
    private List<ItinerarioDTO> vuelosSeleccionados;
    private HashMap<Integer, Integer> itinerariosTarifas;
    private String salida;
    private String llegada;
    private LocalDate fechaIda;
    private LocalDate fechaRegreso;
    private String tipoViaje;
    private String tipoVuelo;
    private List<ItinerarioDetalleDTO> paradasVuelo;
    private Integer total;

    private String mapaVueloJson = "[]";
    private String rutaMapaJson;

    @Autowired
    private ItinerarioService itinerarioService;
    private Integer cantAdultos;

    @Autowired
    private ItinerarioTarifaService itinerarioTarifaService;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();

        // 🔒 Evita el NullPointerException cuando Spring evalúa el Bean al arrancar la App
        if (fc == null || fc.getExternalContext() == null) {
            return;
        }

        try {
            ExternalContext externalContext = fc.getExternalContext();
            Map<String, String> params = externalContext.getRequestParameterMap();

            this.salida = params.get("salida");
            this.llegada = params.get("llegada");
            String fechaIdaStr = params.get("fechaIda");
            String fechaRegresoStr = params.get("fechaRegreso");
            this.tipoViaje = params.getOrDefault("trip", "OW").toUpperCase();
            String adultosStr = params.getOrDefault("adultos", "1");

            cantAdultos = Integer.parseInt(adultosStr);

            if (salida == null || llegada == null || fechaIdaStr == null || salida.isBlank() || llegada.isBlank()) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Parámetros incompletos", "Debes completar origen, destino y fecha.");
                return;
            }

            if (salida.equalsIgnoreCase(llegada)) {
                addMessage(FacesMessage.SEVERITY_WARN, "Destino inválido", "El destino no puede ser igual al origen.");
                return;
            }

            this.fechaIda = LocalDate.parse(fechaIdaStr);
            if (fechaIda.isBefore(LocalDate.now())) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida", "La fecha de ida no puede estar en el pasado.");
                return;
            }

            if ("RT".equals(tipoViaje) && fechaRegresoStr != null) {
                this.fechaRegreso = LocalDate.parse(fechaRegresoStr);
                if (fechaRegreso.isBefore(fechaIda)) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "Fechas inválidas", "La fecha de regreso no puede ser antes que la de ida.");
                    return;
                }
            }

            this.tipoVuelo = "RT".equals(tipoViaje) ? " Vuelos Ida" : "Solo Ida";

            vuelosIda = itinerarioService.buscarItinerarios(salida, llegada, fechaIda.toString());
            if (vuelosIda != null) {
                vuelosIda.sort(Comparator.comparing(ItinerarioDTO::getHoraLlegada24h));
            }

            if ("RT".equals(tipoViaje) && fechaRegreso != null) {
                vuelosRegreso = itinerarioService.buscarItinerarios(llegada, salida, fechaRegreso.toString());
                if (vuelosRegreso != null) {
                    vuelosRegreso.sort(Comparator.comparing(ItinerarioDTO::getHoraLlegada24h));
                }
            }

            if ((vuelosIda == null || vuelosIda.isEmpty()) &&
                    ("RT".equals(tipoViaje) && (vuelosRegreso == null || vuelosRegreso.isEmpty()))) {
                addMessage(FacesMessage.SEVERITY_WARN, "Sin resultados", "No se encontraron vuelos en las fechas seleccionadas.");
            }

            vuelosSeleccionados = new ArrayList<>();
            itinerariosTarifas = new HashMap<>();
            total = 0;

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al cargar los vuelos.");
            e.printStackTrace();
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

    public void showParadas(Integer idItinerario) {
        paradasVuelo = itinerarioService.obtenerDetalleItinerario(idItinerario);
    }

    public void showMapa(Integer idItinerario) {
        cargarRutaMapa(idItinerario);
    }

    public void cargarRutaMapa(Integer idItinerario) {
        try {
            List<PuntoMapaDTO> puntos = itinerarioService.obtenerRutaMapa(idItinerario);
            ObjectMapper mapper = new ObjectMapper();
            this.rutaMapaJson = mapper.writeValueAsString(puntos);

            PrimeFaces.current().executeScript("renderRutaVuelo(" + rutaMapaJson + ")");
        } catch (Exception e) {
            Logger.logInfo("Error generando ruta de mapa: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo cargar el mapa de la ruta.");
        }
    }

    public String selectVuelo(ItinerarioDTO vuelo, TarifaDTO tarifa) {
        Logger.logInfo("seleccionar vuelo " + vuelo.toString());
        this.selectedVuelo = vuelo;
        this.total += vuelo.getPrecio();
        this.vuelosSeleccionados.add(vuelo);
        this.itinerariosTarifas.put(vuelo.getItinerario(), tarifa.getIdTarifa());

        if (tipoViaje.equals("RT")) {
            this.vuelosIda = vuelosRegreso;
            this.tipoVuelo = "Vuelos Regreso";
            if (vuelosSeleccionados.size() >= 2) {
                return redireccionar();
            }
        } else {
            return redireccionar();
        }

        return "";
    }

    public void procederCompra() {
    }

    public String redireccionar() {
        addMessage(FacesMessage.SEVERITY_INFO, "Compra confirmada", "Gracias por tu compra.");

        StringBuilder url = new StringBuilder();
        url.append("seleccionAsientos.xhtml?faces-redirect=true&itinerarios=");

        this.itinerariosTarifas.forEach((integer, integer2) -> {
            url.append(integer).append(",");
        });

        url.deleteCharAt(url.length() - 1);
        url.append("&adultos=").append(cantAdultos);

        url.append("&tarifas=");
        this.itinerariosTarifas.forEach((integer, integer2) -> {
            url.append(integer2).append(",");
        });

        url.deleteCharAt(url.length() - 1);
        Logger.logInfo(url.toString());

        return url.toString();
    }

    public List<ItinerarioTarifa> getTarifasPorItinerario(Integer itinerario) {
        return itinerarioTarifaService.findByItinerario(itinerario);
    }

    public List<TarifaDTO> getTarifasItinerario(Integer idItinerario) {
        return itinerarioTarifaService.getTarifasItinerario(idItinerario);
    }

    public void seleccionarTarifa(VueloDTO vuelo, ItinerarioTarifa tarifaSeleccionada) {
    }

    public void onVueloSelect(SelectEvent<ItinerarioDTO> event) {
        this.selectedVuelo = event.getObject();
    }

    // --- Getters y Setters ---
    public String getRutaMapaJson() { return rutaMapaJson; }
    public String getMapaVueloJson() { return mapaVueloJson; }
    public void setSelectedVuelo(ItinerarioDTO selectedVuelo) { this.selectedVuelo = selectedVuelo; }
    public ItinerarioDTO getSelectedVuelo() { return selectedVuelo; }
    public String getSelectedVueloParadas() { return selectedVueloParadas; }
    public Integer getItinerario() { return itinerario; }
    public void setItinerario(Integer itinerario) { this.itinerario = itinerario; }
    public List<ItinerarioDetalleDTO> getParadasVuelo() { return paradasVuelo; }
    public String getDetallesDeParadas() { return "Detalles de paradas: " + selectedVuelo.getCantParadas(); }
    public String getDuracion() { return selectedVuelo.getDuracion(); }
    public Integer getPrecio() { return selectedVuelo.getPrecio(); }
    public String getHoraSalida() { return selectedVuelo.getHoraSalida24h(); }
    public String getHoraLlegada() { return selectedVuelo.getHoraLlegada24h(); }
    public String getSalida() { return salida; }
    public void setSalida(String salida) { this.salida = salida; }
    public String getLlegada() { return llegada; }
    public void setLlegada(String llegada) { this.llegada = llegada; }
    public LocalDate getFechaIda() { return fechaIda; }
    public LocalDate getFechaRegreso() { return fechaRegreso; }
    public void setFechaRegreso(LocalDate fechaRegreso) { this.fechaRegreso = fechaRegreso; }
    public void setFechaIda(LocalDate fechaIda) { this.fechaIda = fechaIda; }
    public List<ItinerarioDTO> getVuelosIda() { return vuelosIda; }
    public List<ItinerarioDTO> getVuelosRegreso() { return vuelosRegreso; }
    public List<ItinerarioDTO> getVuelosSeleccionados() { return vuelosSeleccionados; }
    public void setVuelosSeleccionados(List<ItinerarioDTO> vuelosSeleccionados) { this.vuelosSeleccionados = vuelosSeleccionados; }
    public String getTipoViaje() { return tipoViaje; }
    public void setTipoViaje(String tipoViaje) { this.tipoViaje = tipoViaje; }
    public String getTipoVuelo() { return tipoVuelo; }
    public void setTipoVuelo(String tipoVuelo) { this.tipoVuelo = tipoVuelo; }
    public Integer getTotal() { return total; }
}
