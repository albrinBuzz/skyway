package com.SkyWay.views.Itinerario;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.service.ItinerarioVueloService;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.service.SegmentoVueloService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
//import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Future;
import org.omnifaces.cdi.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Named("itinerarioBusquedaBean")
@RequestScoped


//@Component("itinerarioBusquedaBean")
//@Scope(value = "view", proxyMode = ScopedProxyMode.TARGET_CLASS)

public class ItinerarioBusquedaBean implements Serializable {


    @Autowired
    private CiudadService ciudadService;

    @Autowired
    private AeropuertoService aeropuertoService;


    private List<Aeropuerto> aeropuertos;
    private HashMap<String, Aeropuerto> aeropuertosMap;
    private HashMap<String, Vuelo> vuelosMap;
    private String codigoIataOrigen;
    private String codigoIataDestino;
    private LocalDate fechaRetorno;
    @Future
    private LocalDate fechaIda;

    private String vueloId;
    private Vuelo vueloSeleccionado;
    private Integer adultos = 1;
    private List<Ciudad> ciudads;

    private static final Logger LOGGER = Logger.getLogger(ItinerarioBusquedaBean.class.getName());

    @PostConstruct
    public void init() {
        try {
            aeropuertosMap = new HashMap<>();

            ciudads = ciudadService.getAllCiudades();

            aeropuertos = aeropuertoService.findAll();

            if (aeropuertos != null) {
                aeropuertos.forEach(aeropuerto ->
                        aeropuertosMap.put(aeropuerto.getCodigoIata(), aeropuerto)
                );
            }
        } catch (Exception e) {
            // 1. Registrar la excepción en el log del servidor
            LOGGER.log(Level.SEVERE, "Error al inicializar los datos de ciudades y aeropuertos", e);
            com.SkyWay.util.Logger.logError(e.getMessage());
            // 2. Opcional: Notificar a PrimeFaces / JSF si la vista está lista
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                context.addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        "Error de Carga",
                        "No se pudieron cargar los datos iniciales de aeropuertos."
                ));
            }
        }
    }

    public void buscarVuelosSoloIda() throws ParseException {

        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            String redirectUrl = "/home/vuelos.xhtml"
                    + "?salida=" + URLEncoder.encode(codigoIataOrigen, StandardCharsets.UTF_8)
                    + "&llegada=" + URLEncoder.encode(codigoIataDestino, StandardCharsets.UTF_8)
                    + "&fechaIda=" + fechaIda
                    + "&adultos=" + adultos
                    + "&trip=OW";

            externalContext.redirect(redirectUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void buscarVuelosIdaYVuelta() throws ParseException {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            String redirectUrl = "/home/vuelos.xhtml"
                    + "?salida=" + URLEncoder.encode(codigoIataOrigen, StandardCharsets.UTF_8)
                    + "&llegada=" + URLEncoder.encode(codigoIataDestino, StandardCharsets.UTF_8)
                    + "&fechaIda=" + fechaIda
                    + "&fechaRegreso=" + fechaRetorno  // Asegúrate de tener returnDate en tu bean
                    + "&adultos=" + adultos
                    + "&trip=RT";
            externalContext.redirect(redirectUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public List<Aeropuerto> buscarAeropuertos(String query) {
        String filtro = query.toLowerCase();
        //Logger.logInfo(filtro);

        return aeropuertos.stream()
                .filter(a ->
                        a.getCodigoIata().toLowerCase().contains(filtro) ||
                                a.getNombreAeropuerto().toLowerCase().contains(filtro) ||
                                a.getCiudad().getNombre().toLowerCase().contains(filtro)
                )
                .toList();
    }

    public String getItemLabel(Object item) {
        if (item == null) {
            return "";
        }

        if (item instanceof Aeropuerto a) {
            String ciudadNombre = (a.getCiudad() != null && a.getCiudad().getNombre() != null)
                    ? a.getCiudad().getNombre()
                    : "Desconocida";

            String aeropuertoNombre = (a.getNombreAeropuerto() != null)
                    ? a.getNombreAeropuerto()
                    : "Aeropuerto desconocido";

            String codigoIata = (a.getCodigoIata() != null)
                    ? a.getCodigoIata()
                    : "N/A";

            return ciudadNombre + " - " + aeropuertoNombre + " (" + codigoIata + ")";
        } else if (item instanceof String s) {
            if (s.isBlank()) return "";
            if (aeropuertosMap != null && aeropuertosMap.containsKey(s.toUpperCase())) {
                return getItemLabel(aeropuertosMap.get(s.toUpperCase()));
            }
            return s;
        }

        return item.toString();
    }


    // Método que se llama cuando se hace clic en "Seleccionar"


    public void setAdultos(Integer adultos) {
        this.adultos = adultos;
    }

    public Integer getAdultos() {
        return adultos;
    }

    public String getCodigoIataDestino() {
        return codigoIataDestino;
    }

    public String getCodigoIataOrigen() {
        return codigoIataOrigen;
    }

    public void setCodigoIataDestino(String codigoIataDestino) {
        this.codigoIataDestino = codigoIataDestino;
    }

    public void setCodigoIataOrigen(String codigoIataOrigen) {
        this.codigoIataOrigen = codigoIataOrigen;
    }

    public void setFechaIda(@Future LocalDate fechaIda) {
        this.fechaIda = fechaIda;
    }

    public void setFechaRetorno(LocalDate fechaRetorno) {
        this.fechaRetorno = fechaRetorno;
    }

    public @Future LocalDate getFechaIda() {
        return fechaIda;
    }

    public LocalDate getFechaRetorno() {
        return fechaRetorno;
    }

    public List<Ciudad> getCiudads() {
        return ciudads;
    }

    public List<Aeropuerto> getAeropuertos() {
        return aeropuertos;
    }


    public String getVueloId() {
        return vueloId;
    }

    public void setVueloId(String vueloId) {
        this.vueloId = vueloId;
    }


    public Vuelo getVueloSeleccionado() {
        return vueloSeleccionado;
    }

    public void setVueloSeleccionado(Vuelo vueloSeleccionado) {
        this.vueloSeleccionado = vueloSeleccionado;
    }


}