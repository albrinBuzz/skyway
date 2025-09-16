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
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Future;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

@Named("itinerarioBean")
@ViewScoped
public class ItinerarioBean implements Serializable {


    @Autowired
    private ItinerarioService itinerarioService;
    @Autowired
    private CiudadService ciudadService;

    private List<ItinerarioDTO> itinerariosBusqueda;
    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private VueloService vueloService;

    @Autowired
    private ItinerarioVueloService itinVueloService;

    @Autowired
    private SegmentoVueloService segmentoVueloService;

    private Itinerario itinerario = new Itinerario();
    private List<Aeropuerto> aeropuertos;
    private HashMap<String,Aeropuerto> aeropuertosMap;
    private HashMap<String,Vuelo> vuelosMap;
    private List<Vuelo> vuelos;  // vuelos disponibles para asignar
    private List<ItinerarioVuelo> itinerariosAsignados ;
    private List<Vuelo> vuelosAsignados;
    private ItinerarioVuelo itinerarioVuelo;
    private List<SegmentoVuelo>segmentoVuelos;
    private String codigoIataOrigen;
    private String codigoIataDestino;
    private LocalDate fechaRetorno;
    @Future
    private LocalDate fechaIda;
    private String aeropuertoOrigen; // Cambiado de String a Aeropuerto
    private String aeropuertoDestino; // Cambiado de String a Aeropuerto
    private LocalDate fechaBusqueda;
    private String vueloId;
    private Vuelo vueloSeleccionado;
    private Integer adultos=1;
    private List<Ciudad>ciudads;


    @PostConstruct
    public void init(){
        aeropuertosMap=new HashMap<>();
        vuelosMap=new HashMap<>();
        itinerariosAsignados = new ArrayList<>();
        vuelosAsignados=new ArrayList<>();

        //vuelos = vueloService.findAll();

        //vuelos.forEach(vuelo -> vuelosMap.put(vuelo.getIdVuelo().toString(), vuelo));

        ciudads=ciudadService.getAllCiudades();
        itinerarioVuelo=new ItinerarioVuelo();

        aeropuertos=aeropuertoService.findAll();
        aeropuertos.forEach(aeropuerto -> aeropuertosMap.put(aeropuerto.getCodigoIata(),aeropuerto));
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



    public void buscarVuelosSegmento() {

        Logger.logInfo("buscando vuelos");

        // Aquí se puede realizar la búsqueda de vuelos según los filtros establecidos
        // Este ejemplo es solo una simulación
        vuelos = new ArrayList<>(); // Resetear la lista de vuelos
        // Filtrar vuelos según los criterios de búsqueda
        // Simulamos algunos vuelos para demostrar el funcionamiento
        Logger.logInfo(aeropuertoOrigen+"->"+aeropuertoDestino+" : "+fechaBusqueda);
        if (aeropuertoOrigen != null && aeropuertoDestino != null && fechaBusqueda != null) {
            Logger.logInfo(aeropuertoOrigen+"->"+aeropuertoDestino+" : "+fechaBusqueda);
            vuelos.add(new Vuelo("1234", new Date(), "AER1", "AER2"));
            vuelos.add(new Vuelo("5678", new Date(), "AER3", "AER2"));
        }else {
            Logger.logInfo("estan los campos nulos");
        }
    }


    public void saveItinerario() {

        var aeropuertoOrigen=aeropuertosMap.get(this.aeropuertoOrigen);
        var aeropuertoDestino=aeropuertosMap.get(this.aeropuertoDestino);
        itinerario.setFechaCreacion(new Timestamp(new Date().getTime()));
        this.itinerario.setAeropuertoOrigen(aeropuertoOrigen);
        this.itinerario.setAeropuertoDestino(aeropuertoDestino);


        Logger.logInfo("itinerarioGuardado"+itinerario.toString());
    }

    public void addVuelo() {
        var vuelo=vuelosMap.get(this.vueloId);
        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(itinerario);
        iv.setVuelo(vuelo);
        itinerariosAsignados.add(iv);
    }

    public void saveItinerarioVuelo() {
        System.out.println("creando el itinerarioVuelo");
        if (vueloId == null ) {
            System.out.println("VueloId nulo");
            return;
        }
        Logger.logInfo(vueloId);
        //var itinerarioGuardado = itinerarioService.obtenerItinerarioPorId(); // Reset form
        var vuelo=vuelosMap.get(this.vueloId);
        Logger.logInfo(vuelo.toString());
        Logger.logInfo(vuelo.toString());
        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(itinerario);
        iv.setVuelo(vuelo);
        var itineraioVueloGuardado= itinVueloService.save(iv);
        var initinerarioVuelo=itinVueloService.findById(itineraioVueloGuardado.getIdItinerarioVuelo()).get();
        Logger.logInfo(itineraioVueloGuardado.toString());
        Logger.logInfo(initinerarioVuelo.toString());
        itinerariosAsignados.add(initinerarioVuelo);


    }
    public void guardarItinerario(){


        var aeropuertoOrigen=aeropuertosMap.get(this.aeropuertoOrigen);
        var aeropuertoDestino=aeropuertosMap.get(this.aeropuertoDestino);
        itinerario.setFechaCreacion(new Timestamp(new Date().getTime()));
        this.itinerario.setAeropuertoOrigen(aeropuertoOrigen);
        this.itinerario.setAeropuertoDestino(aeropuertoDestino);

        var itinerarioGuardado = itinerarioService.save(itinerario); // Reset form

        for (ItinerarioVuelo iv : itinerariosAsignados) {
            iv.setItinerario(itinerarioGuardado);
            var itineraioVueloGuardado= itinVueloService.save(iv);
            Logger.logInfo(itineraioVueloGuardado.toString());

        }
        Logger.logInfo("itinerarioGuardado"+itinerario.toString());

    }

    public void agregarVuelo(Vuelo vuelo){
        Logger.logInfo("agregando vuelo");
        vuelosAsignados.add(vuelo);
        for (Vuelo vuelosAsignado : vuelosAsignados) {
            Logger.logInfo(vuelosAsignado.toString());
        }
        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(itinerario);
        iv.setVuelo(vuelo);
        //var itineraioVueloGuardado= itinVueloService.save(iv);
        //var initinerarioVuelo=itinVueloService.findById(itineraioVueloGuardado.getIdItinerarioVuelo()).get();
        //Logger.logInfo(itineraioVueloGuardado.toString());
        //Logger.logInfo(initinerarioVuelo.toString());
        itinerariosAsignados.add(iv);
    }

    public List<Aeropuerto> buscarAeropuertos(String query) {
        String filtro = query.toLowerCase();

        return aeropuertos.stream()
                .filter(a ->
                        a.getCodigoIata().toLowerCase().contains(filtro) ||
                                a.getNombreAeropuerto().toLowerCase().contains(filtro) ||
                                a.getCiudad().getNombre().toLowerCase().contains(filtro)
                )
                .toList();
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

    public Itinerario getItinerario() {
        return itinerario;
    }

    public void setItinerario(Itinerario itinerario) {
        this.itinerario = itinerario;
    }

    public void setAeropuertoOrigen(String aeropuertoOrigen) {
        this.aeropuertoOrigen = aeropuertoOrigen;
    }

    public void setAeropuertoDestino(String aeropuertoDestino) {
        this.aeropuertoDestino = aeropuertoDestino;
    }

    public String getAeropuertoOrigen() {
        return aeropuertoOrigen;
    }

    public String getAeropuertoDestino() {
        return aeropuertoDestino;
    }

    public String getVueloId() {
        return vueloId;
    }

    public void setVueloId(String vueloId) {
        this.vueloId = vueloId;
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public ItinerarioVuelo getItinerarioVuelo() {
        return itinerarioVuelo;
    }

    public List<ItinerarioVuelo> getItinerariosAsignados() {
        return itinerariosAsignados;
    }

    public List<SegmentoVuelo> getSegmentoVuelos() {
        return segmentoVuelos;
    }

    public void setFechaBusqueda(LocalDate fechaBusqueda) {
        this.fechaBusqueda = fechaBusqueda;
    }

    public LocalDate getFechaBusqueda() {
        return fechaBusqueda;
    }
    public Vuelo getVueloSeleccionado() {
        return vueloSeleccionado;
    }

    public void setVueloSeleccionado(Vuelo vueloSeleccionado) {
        this.vueloSeleccionado = vueloSeleccionado;
    }

    public void setVuelosAsignados(List<Vuelo> vuelosAsignados) {
        this.vuelosAsignados = vuelosAsignados;
    }


    public List<Vuelo> getVuelosAsignados() {
      return this.vuelosAsignados;
    }


}
