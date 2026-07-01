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
import com.SkyWay.modules.tipoturno.domain.service.TipoTurnoService;
import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turno.domain.service.TurnoService;
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

    private SegmentoVuelo segmentoActual = new SegmentoVuelo();
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
    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TipoTurnoService tipoTurnoService;

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
    private Turno turno;
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
        segmentoActual=new SegmentoVuelo();
        turno=new Turno();
        if (idVuelo != null) {

            segmentos=segmentoVueloService.findByIdVuelo(Integer.valueOf(idVuelo));

            for (SegmentoVuelo segmento : segmentos) {
                Logger.logInfo("segmento"+segmento.getIdSegmento());
                List<AsignacionPuerta> asignaciones = asignacionPuertaService.findBySegmentoVuelo(segmento);
                for (AsignacionPuerta asignacione : asignaciones) {
                    Logger.logInfo("puerta-> "+asignacione.getPuertaEmbarque().getTerminal());

                }
            }


            vuelo=vueloService.findById(Integer.valueOf(idVuelo)).get();
            turno=turnoService.findByVueloId(Integer.valueOf(idVuelo)).get(0);


            aerolineaId=vuelo.getAerolinea().getNombre();
            avionId=vuelo.getAvion().getModeloAvion().getNombre();



            // Sincronizar IDs para los SelectOneMenu
            this.numeroVuelo = vuelo.getNumeroVuelo();
            this.pilotoId = vuelo.getPiloto().getRut();
            this.aerolineaId = String.valueOf(vuelo.getAerolinea().getIdAerolinea());
            this.avionId = String.valueOf(vuelo.getAvion().getIdAvion());
            this.avionSeleccionado = vuelo.getAvion(); // Importante para el listener

            // CARGAR PRECIOS EXISTENTES PARA EL PASO 2
            this.preciosAsientos = new ArrayList<>();
            // Primero obtenemos la capacidad del avión
            List<CapacidadClase> capacidades = capacidadClaseService.findByAvionId(vuelo.getAvion().getIdAvion());

            for (CapacidadClase cap : capacidades) {
                ClaseAsientoPrecioDto dto = new ClaseAsientoPrecioDto();
                dto.setClaseAsiento(cap.getClaseAsiento1().getIdClase());
                dto.setClase(cap.getClaseAsiento1().getDescripcion());
                dto.setCantidad(cap.getCantidad());

                // Buscar si ya existe un precio guardado para este vuelo y clase
                precioAsientoService.findByVueloAndClase(vuelo.getIdVuelo(), cap.getClaseAsiento1().getIdClase())
                        .ifPresentOrElse(
                                pa -> dto.setPrecio(pa.getPrecio()), // Si existe, ponemos el precio real
                                () -> dto.setPrecio(0)             // Si no, empezamos en 0
                        );

                preciosAsientos.add(dto);
            }

            vueloCreado = true;
        }

        else {
            vuelo=new Vuelo();


            turno=new Turno();
            segmentos=new ArrayList<>();
            vuelos=vueloService.findAll();
            //segmentos=simularSegmentos();

        }


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

    public void verTurnos(Vuelo vuelo) throws IOException {
        FacesContext.getCurrentInstance().getExternalContext()
                .redirect("/admin/turno/turno.xhtml?vueloId=" + vuelo.getIdVuelo());
    }

    public void eliminarSegmento(SegmentoVuelo segmento) {
        if (segmento != null) {
            segmentoVueloService.deleteById(segmento.getIdSegmento());  // O el método correspondiente en tu servicio
            segmentos.remove(segmento);
            Logger.logInfo("Segmento eliminado: " + segmento.getIdSegmento());
        }
    }


    public void prepararEdicion(SegmentoVuelo seg) {
        // 1. Cargamos el objeto que el diálogo usará directamente
        this.segmentoActual = seg;

        // 2. Sincronizamos los campos de texto que usan tus combos en el diálogo
        if (seg.getAeropuertoOrigen() != null) {
            this.aeropuertoOrigen = seg.getAeropuertoOrigen().getCodigoIata();

            // 3. ¡Crucial! Cargar las puertas del aeropuerto de origen para el combo
            this.puertaEmbarques = puertaService.findByAeropuerto(seg.getAeropuertoOrigen().getIdAeropuerto());

            // Buscamos si ya tiene una puerta asignada para pre-seleccionarla
            List<AsignacionPuerta> asignaciones = asignacionPuertaService.findBySegmentoVuelo(seg);
            if (!asignaciones.isEmpty()) {
                this.puertaEmbarqueSeleccion = String.valueOf(asignaciones.get(0).getPuertaEmbarque().getIdPuerta());
            }
        }

        if (seg.getAeropuertoDestino() != null) {
            this.aeropuertoDestino = seg.getAeropuertoDestino().getCodigoIata();
        }

        // 4. Mapeamos las fechas del Timestamp (SQL) al LocalDateTime (Java 8) que usa p:datePicker
        if (seg.getHoraSalida() != null) {
            this.fechaSalida = seg.getHoraSalida().toLocalDateTime();
        }
        if (seg.getHoraLlegada() != null) {
            this.fechaLlegada = seg.getHoraLlegada().toLocalDateTime();
        }
    }



    // Método que se ejecuta al guardar los cambios en el segmento
    public void guardarSegmentoEditado() {
        // Aquí actualizamos el vuelo con la nueva fecha de salida
        if (segmentoActual != null) {
            for (SegmentoVuelo seg : vuelo.getSegmentoVuelos()) {
                if (seg.equals(segmentoActual)) {
                    // Asumimos que 'horaSalida' es la fecha seleccionada por el usuario en el formulario
                    seg.setHoraSalida(segmentoActual.getHoraSalida());
                    seg.setHoraLlegada(segmentoActual.getHoraLlegada());
                    break;
                }
            }
            // Ahora, podrías guardar los cambios en la base de datos o realizar cualquier otra operación
        }
    }


    public void instanciarNuevoSegmento() {
        segmentoActual = new SegmentoVuelo(); // Aquí se instancia el nuevo segmento
        Logger.logInfo("Nuevo segmento instanciado correctamente.");
    }



    public void guardarSegmento() {
        try {
            // 1. Validaciones Geográficas y Temporales
            if (aeropuertoOrigen == null || aeropuertoDestino == null) return;
            confirmarGuardadoVuelo();
            segmentoActual.setAeropuertoOrigen(aeropuertos.get(this.aeropuertoOrigen));
            segmentoActual.setAeropuertoDestino(aeropuertos.get(this.aeropuertoDestino));
            segmentoActual.setHoraSalida(getFechaSalidaAsTimestamp());
            segmentoActual.setHoraLlegada(getFechaLlegadaAsTimestamp());
            segmentoActual.setVuelo(this.vuelo);

            // Calcular Orden si es nuevo
            if (segmentoActual.getIdSegmento() == null) {
                segmentoActual.setOrdenSegmento(segmentos.size() + 1);
            }

            // 2. Guardar Segmento
            SegmentoVuelo guardado = segmentoVueloService.save(segmentoActual);

            // 3. Gestionar Puerta de Embarque (AsignacionPuerta)
            if (puertaEmbarqueSeleccion != null && !puertaEmbarqueSeleccion.isEmpty()) {
                PuertaEmbarque pe = puertaService.findById(Integer.valueOf(puertaEmbarqueSeleccion)).get();

                // Buscar si ya existe asignación para este tramo (Edición)
                AsignacionPuerta ap = asignacionPuertaService.findBySegmentoVuelo(guardado)
                        .stream().findFirst().orElse(new AsignacionPuerta());

                ap.setSegmentoVuelo(guardado);
                ap.setPuertaEmbarque(pe);
                asignacionPuertaService.save(ap);
            }

            // 4. Sincronizar Turno (Si es el primer tramo)
            /*if (guardado.getOrdenSegmento() == 1) {
                actualizarTurnoOperativo(guardado);
            }*/

            // 5. Refrescar lista de la vista
            segmentos = segmentoVueloService.findByIdVuelo(vuelo.getIdVuelo());
            openNewSegmento(); // Reset para el siguiente

            addMessage(FacesMessage.SEVERITY_INFO, "Tramo Confirmado", "Ruta actualizada correctamente.");
        } catch (Exception e) {
            Logger.logError("Error en guardarSegmento: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error al procesar el tramo.");
        }
    }

    private void actualizarTurnoOperativo(SegmentoVuelo primerTramo) {
        if (turno.getVuelo() == null) {
            turno.setVuelo(vuelo);
            turno.setTipoTurno(tipoTurnoService.findById(1).get());
        }
        // Sincronizar horas del turno con el primer despegue
        turno.setFecha(primerTramo.getHoraSalida());
        turnoService.save(turno);
    }

    public void confirmarGuardadoVuelo(){
        vuelo = vueloService.save(vuelo);
        if (turno.getVuelo() == null) {
            turno.setVuelo(vuelo);
            turno.setTipoTurno(tipoTurnoService.findById(1).get());
            turnoService.save(turno);
        }

        // 4. Guardar/Actualizar Precios por Clase
        for (ClaseAsientoPrecioDto dto : preciosAsientos) {
            PrecioAsiento pa = precioAsientoService.findByVueloAndClase(vuelo.getIdVuelo(), dto.getClaseAsiento())
                    .orElse(new PrecioAsiento());

            pa.setVuelo(vuelo);
            pa.setPrecio(dto.getPrecio());
            pa.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(dto.getClaseAsiento()).get());
            precioAsientoService.guardarPrecioAsiento(pa);
        }

    }
    public void guardarVuelo() {
        try {
            Logger.logInfo("Sincronizando cabecera de vuelo: " + numeroVuelo);

            // 1. Recuperar y validar entidades maestras
            Piloto p = pilotoService.findByRut(pilotoId).orElseThrow(() -> new RuntimeException("Piloto no válido"));
            Aerolinea a = aerolineaService.findById(Integer.parseInt(aerolineaId));
            EstadoVuelo estado = estadoVueloService.findById(1).get();

            // 2. Configurar objeto Vuelo
            vuelo.setNumeroVuelo(this.numeroVuelo);
            vuelo.setAvion(avionSeleccionado);
            vuelo.setPiloto(p);
            vuelo.setAerolinea(a);
            vuelo.setEstadoVuelo(estado);

            vueloCreado = true;
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Cabecera de vuelo habilitada operativa.");
        } catch (Exception e) {
            Logger.logError("Error en guardarVuelo: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar la configuración inicial.");
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
        // 1. Recuperamos el objeto Aeropuerto usando el IATA que llegó del combo
        Logger.logInfo("Puertas Accionadas");
        Aeropuerto aero = aeropuertos.get(this.aeropuertoOrigen);

        if (aero != null) {
            Logger.logInfo("Cargando puertas para el ID: " + aero.getIdAeropuerto());
            // 2. Buscamos las puertas por el ID numérico
            this.puertaEmbarques = puertaService.findByAeropuerto(aero.getIdAeropuerto());
        } else {
            this.puertaEmbarques = new ArrayList<>();
        }
    }


    public void openNewSegmento(){
        this.segmentoActual = new SegmentoVuelo();
    }


    private Piloto obtenerPiloto() throws RuntimeException {
        return pilotoService.findByRut(pilotoId)
                .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));
    }

    private Avion obtenerAvion() throws RuntimeException {
        return avionService.findById(Integer.parseInt(avionId))
                .orElseThrow(() -> new RuntimeException("Avión no encontrado"));
    }


    /**
     * Centraliza el envío de mensajes a la interfaz de usuario.
     * @param severity Nivel del mensaje (FacesMessage.SEVERITY_INFO, ERROR, etc.)
     * @param summary Título del mensaje
     * @param detail Cuerpo del mensaje
     */
    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().
                addMessage(null, new FacesMessage(severity, summary, detail));
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


    public SegmentoVuelo getSegmentoActual() {
        return segmentoActual;
    }

    public void setSegmentoActual(SegmentoVuelo segmentoActual) {
        this.segmentoActual = segmentoActual;
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
