package com.SkyWay.views.Itinerario;


import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
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
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.Future;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
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

    @Autowired
    private TarifaService tarifaService;

    @Autowired
    private ItinerarioTarifaService itinerarioTarifaService;




    private Itinerario itinerario = new Itinerario();
    private List<Tarifa>tarifas;
    private HashMap<Integer,Integer>precioTarifas;
    private List<Aeropuerto> aeropuertos;
    private HashMap<String,Aeropuerto> aeropuertosMap;
    private HashMap<String,Vuelo> vuelosMap;
    private List<Vuelo> vuelos;  // vuelos disponibles para asignar
    private List<ItinerarioVuelo> itinerariosAsignados ;
    private List<Vuelo> vuelosAsignados;
    private ItinerarioVuelo itinerarioVuelo;
    private List<SegmentoVuelo>segmentoVuelos;
    private Integer precioTarifa;

    private String aeropuertoOrigen; // Cambiado de String a Aeropuerto
    private String aeropuertoDestino; // Cambiado de String a Aeropuerto
    private String aeropuertoOrigenBusqueda; // Cambiado de String a Aeropuerto
    private String aeropuertoDestinoBusqueda; // Cambiado de String a Aeropuerto

    private LocalDate fechaBusqueda;
    private String vueloId;
    private Vuelo vueloSeleccionado;
    private List<Ciudad>ciudads;


    @PostConstruct
    public void init(){
        aeropuertosMap=new HashMap<>();
        vuelosMap=new HashMap<>();
        itinerariosAsignados = new ArrayList<>();
        vuelosAsignados=new ArrayList<>();

        vuelos = vueloService.findAll();

        vuelos.forEach(vuelo -> vuelosMap.put(vuelo.getIdVuelo().toString(), vuelo));

        ciudads=ciudadService.getAllCiudades();
        itinerarioVuelo=new ItinerarioVuelo();
        precioTarifas=new HashMap<>();

         tarifas=tarifaService.listarTarifas();
        for (Tarifa tarifa : tarifas) {
            //Logger.logInfo(tarifa.toString());
            for (TarifaCaracteristica tarifaCaracteristica : tarifa.getTarifaCaracteristicas()) {
                var carateristica= tarifaCaracteristica.getCaracteristica();
                //.logInfo(carateristica.toString());

            }
            //Logger.logInfo("--");
        }

        aeropuertos=aeropuertoService.findAll();
        aeropuertos.forEach(aeropuerto -> aeropuertosMap.put(aeropuerto.getCodigoIata(),aeropuerto));
    }





    public void saveItinerario() {

        var aeropuertoOrigen=aeropuertosMap.get(this.aeropuertoOrigen);
        var aeropuertoDestino=aeropuertosMap.get(this.aeropuertoDestino);
        itinerario.setFechaCreacion(new Timestamp(new Date().getTime()));
        this.itinerario.setAeropuertoOrigen(aeropuertoOrigen);
        this.itinerario.setAeropuertoDestino(aeropuertoDestino);
        //Logger.logInfo(aeropuertoOrigen.getCodigoIata()+"-"+aeropuertoDestino.getCodigoIata()+"-"+fechaBusqueda.toString());


        Logger.logInfo("itinerarioGuardado"+itinerario.toString());
    }

    public void buscarVuelos(){

        Logger.logInfo(aeropuertoOrigenBusqueda+"-"+aeropuertoDestinoBusqueda);
        if (fechaBusqueda!=null){
            vuelos=vueloService.buscarVuelo(aeropuertoOrigenBusqueda,aeropuertoDestinoBusqueda,fechaBusqueda.toString());
        }else {

            vuelos=vueloService.buscarVuelo(aeropuertoOrigenBusqueda,aeropuertoDestinoBusqueda,null);
        }

        vuelos.forEach(vuelo -> {

            Logger.logInfo(vuelo.toString());

        });


        //Logger.logInfo("itinerarioGuardado"+itinerario.toString());

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
    public void guardarItinerario() {

        if (precioTarifas == null || precioTarifas.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(
                    null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Error", "No hay tarifas definidas")
            );
            return; // salir del método
        }




        boolean ok;
        ok = true; // inicializamos la variable
        Logger.logInfo("dkdk");
        for (Map.Entry<Integer, Integer> entry : precioTarifas.entrySet()) {
            Logger.logInfo("!kkkk");
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            Logger.logInfo(key+"->"+key);

            if (value == null) {
                FacesContext.getCurrentInstance().addMessage(
                        null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Error", "DEbe seter el valor de las tarifas")
                );
                ok = false;
            }
        }

        if (ok) {

        Logger.logInfo("todo ok");

            var itinerarioGuardado = itinerarioService.save(itinerario); // Reset form

            for (ItinerarioVuelo iv : itinerariosAsignados) {
                iv.setItinerario(itinerarioGuardado);
                var itineraioVueloGuardado = itinVueloService.save(iv);
                Logger.logInfo(itineraioVueloGuardado.toString());

            }

            //var tarifas= tarifaService.listarTarifas();

            precioTarifas.forEach((integer, integer2) -> {
                if (integer2 != null) {
                    var tarifa = tarifaService.buscarPorId(integer);
                    ItinerarioTarifa itinerarioTarifa = new ItinerarioTarifa();
                    itinerarioTarifa.setTarifa(tarifa.get());
                    itinerarioTarifa.setItinerario(itinerarioGuardado);
                    itinerarioTarifa.setPrecio(new BigDecimal(integer2));
                    var itinerarioTarifaGuardado = itinerarioTarifaService.save(itinerarioTarifa);
                    Logger.logInfo(itinerarioTarifaGuardado.toString());
                } else {

                }

            });

        Logger.logInfo(itinerarioGuardado.toString());
            //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Itinerario Creado con exito", "Creacion Exitosa del itinerario");
            //PrimeFaces.current().dialog().showMessageDynamic(message);

            FacesContext.getCurrentInstance().
                    addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Itinerario Creado con exito", "Creacion Exitosa del itinerario"));
            }
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


    public void actualizarPrecioTarifa(Tarifa tarifa) {
        Logger.logInfo("seteando precio tarifa->"+precioTarifa);
        try {
            //tarifaService.actualizarPrecio(tarifa); // o como manejes la persistencia
            precioTarifas.put(tarifa.getIdTarifa(),precioTarifa);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Precio actualizado", "Nuevo precio: $" + precioTarifa));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al actualizar el precio", e.getMessage()));
        }
    }


    // Método que se llama cuando se hace clic en "Seleccionar"



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

    public void setAeropuertoDestinoBusqueda(String aeropuertoDestinoBusqueda) {
        this.aeropuertoDestinoBusqueda = aeropuertoDestinoBusqueda;
    }


    public void setAeropuertoOrigenBusqueda(String aeropuertoOrigenBusqueda) {
        this.aeropuertoOrigenBusqueda = aeropuertoOrigenBusqueda;
    }

    public String getAeropuertoDestinoBusqueda() {
        return aeropuertoDestinoBusqueda;
    }

    public String getAeropuertoOrigenBusqueda() {
        return aeropuertoOrigenBusqueda;
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

    public List<Tarifa> getTarifas() {
        return tarifas;
    }

    public void setTarifas(List<Tarifa> tarifas) {
        this.tarifas = tarifas;
    }

    public void setPrecioTarifa(Integer precioTarifa) {
        this.precioTarifa = precioTarifa;
    }

    public Integer getPrecioTarifa() {
        return precioTarifa;
    }
}
