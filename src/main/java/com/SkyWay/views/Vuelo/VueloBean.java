package com.SkyWay.views.Vuelo;



import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import com.SkyWay.modules.aerolinea.domain.service.AerolineaService;
import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.asignacionpuerta.domain.service.AsignacionPuertaService;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.capacidadclase.domain.service.CapacidadClaseService;
import com.SkyWay.modules.capacidadclase.presentation.dto.ClaseAsientoPrecioDto;
import com.SkyWay.modules.claseasiento.domain.service.ClaseAsientoService;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.SkyWay.modules.precioasiento.domain.service.PrecioAsientoService;
import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import com.SkyWay.modules.puertaembarque.domain.service.PuertaEmbarqueService;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.service.SegmentoVueloService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class VueloBean implements Serializable {

    private String numeroVuelo;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaLlegada;
    private Double precioBase;
    private Integer idAerolinea;
    private String rutPiloto;
    private Integer idAvion;
    private Integer idEstadoVuelo;
    private Vuelo vuelo;
    private Vuelo vueloSeleccionado;
    private SegmentoVuelo segmentoEditado; // El segmento que estamos editando

    private SegmentoVuelo segmentoNuevo = new SegmentoVuelo();
    private List<SegmentoVuelo> segmentos = new ArrayList<>();
    private List<Avion>avions;
    private List<Aerolinea>aerolineas;
    private List<Vuelo> vuelos;
    @Autowired
    private VueloService vueloService; // Servicio para acceder a la lógica de negocio y persistencia
    @Autowired
    private SegmentoVueloService segmentoVueloService;
    @Autowired
    private AeropuertoService aeropuertoService;
    @Autowired
    private PiloService pilotoService;
    @Autowired
    private AvionService avionService;
    @Autowired
    private EstadoVueloService estadoVueloService;
    @Autowired
    private ClaseAsientoService claseAsientoService;

    @Autowired
    private PrecioAsientoService precioAsientoService;
    @Autowired
    private AerolineaService aerolineaService;
    @Autowired
    private CapacidadClaseService capacidadClaseService;

    @Autowired
    private AsignacionPuertaService asignacionPuertaService;

    @Autowired
    private PuertaEmbarqueService puertaService;


    private List<Aeropuerto> listaAeropuertos;
    private List<Avion> listaAviones;
    private List<Piloto> listaPilotos;
    private List<EstadoVuelo> listaEstadosVuelo;
    private List<CapacidadClase>capacidadClases;

    private String aeropuertoOrigen; // Cambiado de String a Aeropuerto
    private String aeropuertoDestino; // Cambiado de String a Aeropuerto

    private HashMap<String,Aeropuerto> aeropuertos;
    private HashMap<String,Piloto >pilotos;
    private HashMap<String, Avion>aviones;
    private HashMap<Integer,PuertaEmbarque>puertasMap;
    private Map<Integer, Integer> preciosPorClase = new HashMap<>();
    private Avion avionSeleccionado;

    private String avionId;
    private String pilotoId;
    private String aerolineaId;
    private List<PuertaEmbarque> puertaEmbarques;
    private String puertaEmbarqueSeleccion;
    private boolean vueloCreado = false;
    private SegmentoVuelo nuevoSegmento;
    private List<ClaseAsientoPrecioDto>preciosAsientos;

    @PostConstruct
    public void init() {
        // Inicializaciones si se requieren
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        var idVuelo= externalContext.getRequestParameterMap().get("vueloId");


        listaAeropuertos = aeropuertoService.findAll();
        listaAviones = avionService.findAll();
        avions= avionService.findAll();
        listaPilotos = pilotoService.findAll();
        listaEstadosVuelo =estadoVueloService.findAll();
        aeropuertos=new HashMap<>();
        aviones=new HashMap<String, Avion>();
        pilotos=new HashMap<String, Piloto>();
        listaAeropuertos.forEach(aeropuerto -> aeropuertos.put(aeropuerto.getCodigoIata(),aeropuerto));
        listaAviones.forEach(t -> aviones.put(t.getIdAvion().toString(),t));
        listaPilotos.forEach(t -> pilotos.put(t.getRut(), t));
        aerolineas=aerolineaService.findAll();
        preciosPorClase=new HashMap<>();
        preciosAsientos=new ArrayList<>();
        aerolineas=aerolineaService.findAll();
        preciosPorClase=new HashMap<>();
        preciosAsientos=new ArrayList<>();
        nuevoSegmento=new SegmentoVuelo();

        if (idVuelo!=null){
            segmentos=segmentoVueloService.findByIdVuelo(Integer.valueOf(idVuelo));

            for (SegmentoVuelo segmento : segmentos) {
                Logger.logInfo("segmento"+segmento.getIdSegmento());
                List<AsignacionPuerta> asignaciones = asignacionPuertaService.findBySegmentoVuelo(segmento);
                for (AsignacionPuerta asignacione : asignaciones) {
                    Logger.logInfo("puerta-> "+asignacione.getPuertaEmbarque().getTerminal());

                }
            }


            vuelo=vueloService.findById(Integer.valueOf(idVuelo)).get();

            pilotoId=vuelo.getPiloto().getRut();
            aerolineaId=vuelo.getAerolinea().getNombre();
            avionId=vuelo.getAvion().getModeloAvion().getNombre();
            vueloCreado = true;

        }else {
            vuelo=new Vuelo();



            segmentos=new ArrayList<>();
            vuelos=vueloService.findAll();
            //segmentos=simularSegmentos();

        }
        segmentoEditado=new SegmentoVuelo();

    }



    public void editarVuelo(Vuelo vuelo) throws IOException {
        this.vueloSeleccionado = vuelo;
        //Logger.logInfo(vuelo.toString());
        // Aquí podrías redirigir a un formulario de edición si lo deseas
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/admin/vuelo/vuelo.xhtml?vueloId=" + vuelo.getIdVuelo());

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Vuelo seleccionado", vuelo.getNumeroVuelo()));
    }

    public void eliminarSegmento(SegmentoVuelo segmento) {
        if (segmento != null) {
            segmentoVueloService.deleteById(segmento.getIdSegmento());  // O el método correspondiente en tu servicio
            segmentos.remove(segmento);
            Logger.logInfo("Segmento eliminado: " + segmento.getIdSegmento());
        }
    }

    public void editarSegmento(SegmentoVuelo segmento) {
        Logger.logInfo("editanto segmento-> "+segmento.toString());
        this.segmentoEditado = segmento;
        this.segmentoVueloService.save(segmento);
    }

    public SegmentoVuelo getSegmentoEditado() {
        return segmentoEditado;
    }

    public void setSegmentoEditado(SegmentoVuelo segmentoEditado) {
        this.segmentoEditado = segmentoEditado;
    }

    // Método que se ejecuta al guardar los cambios en el segmento
    public void guardarSegmentoEditado() {
        // Aquí actualizamos el vuelo con la nueva fecha de salida
        if (segmentoEditado != null) {
            for (SegmentoVuelo seg : vuelo.getSegmentoVuelos()) {
                if (seg.equals(segmentoEditado)) {
                    // Asumimos que 'horaSalida' es la fecha seleccionada por el usuario en el formulario
                    seg.setHoraSalida(segmentoEditado.getHoraSalida());
                    seg.setHoraLlegada(segmentoEditado.getHoraLlegada());
                    break;
                }
            }
            // Ahora, podrías guardar los cambios en la base de datos o realizar cualquier otra operación
        }
    }


    public void instanciarNuevoSegmento() {
        nuevoSegmento = new SegmentoVuelo(); // Aquí se instancia el nuevo segmento
        Logger.logInfo("Nuevo segmento instanciado correctamente.");
    }


    public void guardarSegmentoNuevo() {


        if (aeropuertoOrigen == null || aeropuertoDestino == null) {
            Logger.logError("Error: El Aeropuerto de Origen o Destino no está seleccionado correctamente.");
            return;
        }

        var aeropuertoOrigen=aeropuertos.get(this.aeropuertoOrigen);

        var aeropuertoDestino=aeropuertos.get(this.aeropuertoDestino);
        // Asignar los aeropuertos al nuevo segmento
        nuevoSegmento.setAeropuertoOrigen(aeropuertoOrigen);
        nuevoSegmento.setAeropuertoDestino(aeropuertoDestino);
        Logger.logInfo(this.fechaSalida.toString());
        Timestamp timestampSalida = getFechaSalidaAsTimestamp();
        Timestamp timestampLlegada = getFechaLlegadaAsTimestamp();

        nuevoSegmento.setHoraSalida(timestampSalida);
        nuevoSegmento.setHoraLlegada(timestampLlegada);


        if (nuevoSegmento.getAeropuertoOrigen() == null || nuevoSegmento.getAeropuertoDestino() == null) {
            Logger.logError("Error: Los aeropuertos no se han asignado correctamente al segmento.");
            return;
        }

        // Continuar con el guardado del segmento
        Logger.logInfo("Guardando un nuevo segmento...");
        if (!puertaEmbarqueSeleccion.isEmpty()){
            var puertaEmbarque=puertaService.findById(Integer.valueOf(puertaEmbarqueSeleccion)).get();
            nuevoSegmento.setVuelo(vuelo);
            var segmentoGuardado= segmentoVueloService.save(nuevoSegmento);
            AsignacionPuerta asignacionPuerta=new AsignacionPuerta();

            asignacionPuerta.setPuertaEmbarque(puertaEmbarque);
            asignacionPuerta.setSegmentoVuelo(segmentoGuardado);
            asignacionPuertaService.save(asignacionPuerta);
            //var segmentofind=segmentoVueloService.findById(segmentoGuardado.getIdSegmento()).get();
            segmentos.add(segmentoVueloService.findById(segmentoGuardado.getIdSegmento()).get());
            Logger.logInfo(segmentoVueloService.findById(segmentoGuardado.getIdSegmento()).get().toString());



        }

        nuevoSegmento = new SegmentoVuelo(); // Aquí se instancia el nuevo segmento

    }


    public void guardarVuelo() {
        try {
            Logger.logInfo("Iniciando creación de vuelo...");
            Logger.logInfo("Número de vuelo: " + numeroVuelo);
            Logger.logInfo("Cantidad de segmentos: " + segmentos.size());

            preciosAsientos.forEach(p -> Logger.logInfo(p.toString()));

            // Buscar entidades relacionadas
            Piloto pilotoEncontrado = pilotoService.findByRut(pilotoId)
                    .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));

            Avion avionEncontrado = avionService.findById(Integer.parseInt(avionId))
                    .orElseThrow(() -> new RuntimeException("Avión no encontrado"));

            Aerolinea aerolineaEncontrada = aerolineaService.findById(Integer.parseInt(aerolineaId));

            EstadoVuelo estadoInicial = estadoVueloService.findById(1)
                    .orElseThrow(() -> new RuntimeException("Estado de vuelo no encontrado"));

            // Crear y persistir vuelo
            //vuelo = new Vuelo();
            vuelo.setAvion(avionSeleccionado);
            vuelo.setPiloto(pilotoEncontrado);
            vuelo.setAerolinea(aerolineaEncontrada);
            vuelo.setEstadoVuelo(estadoInicial);

            Vuelo vueloGuardado = vueloService.save(vuelo);
            vuelo=vueloGuardado;
            vueloCreado = true;
            //vuelo = new Vuelo();

            Logger.logInfo("Vuelo guardado: " + vueloGuardado);

            // Guardar precios por clase
            for (ClaseAsientoPrecioDto dto : preciosAsientos) {
                PrecioAsiento precioAsiento = new PrecioAsiento();
                precioAsiento.setVuelo(vueloGuardado);
                precioAsiento.setPrecio(dto.getPrecio());
                precioAsiento.setClaseAsiento(
                        claseAsientoService.obtenerClaseAsientoPorId(dto.getClaseAsiento())
                                .orElseThrow(() -> new RuntimeException("Clase de asiento no encontrada"))
                );
                precioAsientoService.guardarPrecioAsiento(precioAsiento);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Vuelo creado correctamente."));

        } catch (Exception e) {
            Logger.logError("Error al guardar vuelo "+e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al crear vuelo: " + e.getMessage()));
        }
    }

    public void ajaxListener(AjaxBehaviorEvent event) {
        Logger.logInfo("Cambio de avión seleccionado: " + avionId);

        capacidadClases = capacidadClaseService.findByAvionId(Integer.parseInt(avionId));
        preciosAsientos = new ArrayList<>();

        for (CapacidadClase cap : capacidadClases) {
            ClaseAsientoPrecioDto dto = new ClaseAsientoPrecioDto();
            dto.setClaseAsiento(cap.getClaseAsiento1().getIdClase());
            dto.setClase(cap.getClaseAsiento1().getDescripcion());
            dto.setCantidad(cap.getCantidad());
            preciosAsientos.add(dto);
        }

        avionSeleccionado = aviones.get(this.avionId);
    }

    public void ajaxPuerta(AjaxBehaviorEvent event) {
        Aeropuerto aeropuertoOrigen = aeropuertos.get(this.aeropuertoOrigen);
        if (aeropuertoOrigen != null) {

            puertaEmbarques = puertaService.findByAeropuerto(aeropuertoOrigen.getIdAeropuerto());

        } else {
            puertaEmbarques = new ArrayList<>();
        }
    }


    public void openNewSegmento(){
        this.segmentoNuevo = new SegmentoVuelo();
    }


    private Piloto obtenerPiloto() throws RuntimeException {
        return pilotoService.findByRut(pilotoId)
                .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));
    }

    private Avion obtenerAvion() throws RuntimeException {
        return avionService.findById(Integer.parseInt(avionId))
                .orElseThrow(() -> new RuntimeException("Avión no encontrado"));
    }





    // Getters y Setters



    public Timestamp getFechaSalidaAsTimestamp() {
        if (fechaSalida != null) {
            // Convertimos el LocalDateTime a Instant (con zona predeterminada)
            Instant instant = fechaSalida.atZone(ZoneId.systemDefault()).toInstant();
            return Timestamp.from(instant);
        }
        return null;
    }

    public Timestamp getFechaLlegadaAsTimestamp() {
        if (fechaLlegada != null) {
            // Convertimos el LocalDateTime a Instant (con zona predeterminada)
            Instant instant = fechaLlegada.atZone(ZoneId.systemDefault()).toInstant();
            return Timestamp.from(instant);
        }
        return null;
    }

    public Vuelo getVueloSeleccionado() {
        return vueloSeleccionado;
    }

    public void setVueloSeleccionado(Vuelo vueloSeleccionado) {
        this.vueloSeleccionado = vueloSeleccionado;
    }

    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(LocalDateTime fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }

    public Double getPrecioBase() {
        return precioBase;
    }

    public void setPrecioBase(Double precioBase) {
        this.precioBase = precioBase;
    }

    public Integer getIdAerolinea() {
        return idAerolinea;
    }

    public void setIdAerolinea(Integer idAerolinea) {
        this.idAerolinea = idAerolinea;
    }

    public String getRutPiloto() {
        return rutPiloto;
    }

    public void setRutPiloto(String rutPiloto) {
        this.rutPiloto = rutPiloto;
    }

    public Integer getIdAvion() {
        return idAvion;
    }

    public void setIdAvion(Integer idAvion) {
        this.idAvion = idAvion;
    }

    public Integer getIdEstadoVuelo() {
        return idEstadoVuelo;
    }

    public void setIdEstadoVuelo(Integer idEstadoVuelo) {
        this.idEstadoVuelo = idEstadoVuelo;
    }

    public SegmentoVuelo getSegmentoNuevo() {
        return segmentoNuevo;
    }

    public void setSegmentoNuevo(SegmentoVuelo segmentoNuevo) {
        this.segmentoNuevo = segmentoNuevo;
    }

    public List<SegmentoVuelo> getSegmentos() {
        return segmentos;
    }

    public void setSegmentos(List<SegmentoVuelo> segmentos) {
        this.segmentos = segmentos;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public Vuelo getVuelo() {
        return vuelo;
    }


    public List<Avion> getAvions() {
        return avions;
    }

    public List<Aeropuerto> getListaAeropuertos() {
        return listaAeropuertos;
    }

    public List<Piloto> getListaPilotos() {
        return listaPilotos;
    }


    public void setPilotoId(String pilotoId) {
        this.pilotoId = pilotoId;
    }

    public String getPilotoId() {
        return pilotoId;
    }



    public List<Aerolinea> getAerolineas() {
        return aerolineas;
    }

    public boolean isVueloCreado() {
        return vueloCreado;
    }

    public void setAerolineaId(String aerolineaId) {
        this.aerolineaId = aerolineaId;
    }



    public String getAerolineaId() {
        return aerolineaId;
    }


    public void setAvionId(String avionId) {
        this.avionId = avionId;
    }

    public String getAvionId() {
        return avionId;
    }

    public SegmentoVuelo getNuevoSegmento() {
        return nuevoSegmento;
    }

    public void setNuevoSegmento(SegmentoVuelo nuevoSegmento) {
        this.nuevoSegmento = nuevoSegmento;
    }


    public void setAeropuertoOrigen(String aeropuertoOrigen) {
        this.aeropuertoOrigen = aeropuertoOrigen;
    }

    public String getAeropuertoDestino() {
        return aeropuertoDestino;
    }

    public String getAeropuertoOrigen() {
        return aeropuertoOrigen;
    }



    public void setAeropuertoDestino(String aeropuertoDestino) {
        this.aeropuertoDestino = aeropuertoDestino;
    }

    public List<CapacidadClase> getCapacidadClases() {
        return capacidadClases;
    }

    public Map<Integer, Integer> getPreciosPorClase() {
        return preciosPorClase;
    }

    public void setPreciosPorClase(Map<Integer, Integer> preciosPorClase) {
        this.preciosPorClase = preciosPorClase;
    }

    public List<ClaseAsientoPrecioDto> getPreciosAsientos() {
        return preciosAsientos;
    }

    public void setPreciosAsientos(List<ClaseAsientoPrecioDto> preciosAsientos) {
        this.preciosAsientos = preciosAsientos;
    }
    public List<Vuelo> getVuelos() {
        return vuelos;
    }
    public List<PuertaEmbarque> getPuertaEmbarques() {
        return puertaEmbarques;
    }

    public void setPuertaEmbarques(List<PuertaEmbarque> puertaEmbarques) {
        this.puertaEmbarques = puertaEmbarques;
    }

    public String getPuertaEmbarqueSeleccion() {
        return puertaEmbarqueSeleccion;
    }

    public void setPuertaEmbarqueSeleccion(String puertaEmbarqueSeleccion) {
        this.puertaEmbarqueSeleccion = puertaEmbarqueSeleccion;
    }

    public AeropuertoService getAeropuertoService() {
        return aeropuertoService;
    }
}
