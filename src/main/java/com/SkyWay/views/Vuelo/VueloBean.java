package com.SkyWay.views.Vuelo;

import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import com.SkyWay.modules.aerolinea.domain.service.AerolineaService;
import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
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
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

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

    private SegmentoVuelo segmentoActual = new SegmentoVuelo();
    private List<SegmentoVuelo> segmentos = new ArrayList<>();
    private Map<SegmentoVuelo, PuertaEmbarque> asignacionesPuertasTemp = new HashMap<>();

    private List<Avion> avions;
    private List<Aerolinea> aerolineas;
    private List<Vuelo> vuelos;

    @PersistenceContext
    private EntityManager entityManager;
    @Autowired private VueloService vueloService;
    @Autowired private SegmentoVueloService segmentoVueloService;
    @Autowired private AeropuertoService aeropuertoService;
    @Autowired private PiloService pilotoService;
    @Autowired private AvionService avionService;
    @Autowired private EstadoVueloService estadoVueloService;
    @Autowired private ClaseAsientoService claseAsientoService;
    @Autowired private PrecioAsientoService precioAsientoService;
    @Autowired private AerolineaService aerolineaService;
    @Autowired private CapacidadClaseService capacidadClaseService;
    @Autowired private AsignacionPuertaService asignacionPuertaService;
    @Autowired private PuertaEmbarqueService puertaService;
    @Autowired private TurnoService turnoService;
    @Autowired private TipoTurnoService tipoTurnoService;

    private List<Aeropuerto> listaAeropuertos;
    private List<Avion> listaAviones;
    private List<Piloto> listaPilotos;
    private List<EstadoVuelo> listaEstadosVuelo;

    private String aeropuertoOrigen;
    private String aeropuertoDestino;

    private HashMap<String, Aeropuerto> aeropuertos;
    private HashMap<String, Piloto> pilotos;
    private HashMap<String, Avion> aviones;
    private Avion avionSeleccionado;

    private String avionId;
    private String pilotoId;
    private String aerolineaId;
    private List<PuertaEmbarque> puertaEmbarques;
    private String puertaEmbarqueSeleccion;
    private boolean vueloCreado = false;
    private List<ClaseAsientoPrecioDto> preciosAsientos;
    private String aeropuertosMapaJson;
    private List<AeropuertoMapaProjection> listaAeropuertosJSON;

    private List<Vuelo> vuelosFiltrados;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        var idVuelo = externalContext.getRequestParameterMap().get("vueloId");

        listaAeropuertos = aeropuertoService.findAll();
        this.listaAeropuertosJSON = aeropuertoService.findAllConCoordenadas();
        listaAviones = avionService.findAll();
        avions = listaAviones;
        listaPilotos = pilotoService.findAll();
        listaEstadosVuelo = estadoVueloService.findAll();

        aeropuertos = new HashMap<>();
        aviones = new HashMap<>();
        pilotos = new HashMap<>();
        if (listaAeropuertos != null) listaAeropuertos.forEach(a -> aeropuertos.put(a.getCodigoIata(), a));
        if (listaAviones != null) listaAviones.forEach(a -> aviones.put(a.getIdAvion().toString(), a));
        if (listaPilotos != null) listaPilotos.forEach(p -> pilotos.put(p.getRut(), p));

        aerolineas = aerolineaService.findAll();
        preciosAsientos = new ArrayList<>();
        segmentoActual = new SegmentoVuelo();

        if (idVuelo != null) {
            Integer vId = Integer.valueOf(idVuelo);
            segmentos = segmentoVueloService.findByIdVuelo(vId);
            vuelo = vueloService.findById(vId).orElse(null);

            if (vuelo != null) {
                this.numeroVuelo = vuelo.getNumeroVuelo();
                this.pilotoId = vuelo.getPiloto().getRut();
                this.aerolineaId = String.valueOf(vuelo.getAerolinea().getIdAerolinea());
                this.avionId = String.valueOf(vuelo.getAvion().getIdAvion());
                this.avionSeleccionado = vuelo.getAvion();

                cargarTarifasYCapacidades(vuelo.getAvion().getIdAvion(), vId);
                vueloCreado = true;
            }
        } else {
            vuelo = new Vuelo();
            segmentos = new ArrayList<>();
            vuelos=vueloService.findAll();
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.aeropuertosMapaJson = mapper.writeValueAsString(aeropuertoService.findAllParaMapa());
        } catch (Exception e) {
            this.aeropuertosMapaJson = "[]";
        }
    }

    private void cargarTarifasYCapacidades(Integer idAvion, Integer idVuelo) {
        preciosAsientos = new ArrayList<>();
        List<CapacidadClase> capacidades = capacidadClaseService.findByAvionId(idAvion);
        for (CapacidadClase cap : capacidades) {
            ClaseAsientoPrecioDto dto = new ClaseAsientoPrecioDto();
            dto.setClaseAsiento(cap.getClaseAsiento1().getIdClase());
            dto.setClase(cap.getClaseAsiento1().getDescripcion());
            dto.setCantidad(cap.getCantidad());

            if (idVuelo != null) {
                precioAsientoService.findByVueloAndClase(idVuelo, cap.getClaseAsiento1().getIdClase())
                        .ifPresentOrElse(pa -> dto.setPrecio(pa.getPrecio()), () -> dto.setPrecio(0));
            } else {
                dto.setPrecio(0);
            }
            preciosAsientos.add(dto);
        }
    }

    public void agregarSegmentoEnMemoria() {
        try {
            if (aeropuertoOrigen == null || aeropuertoOrigen.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar un aeropuerto de origen.");
                return;
            }
            if (aeropuertoDestino == null || aeropuertoDestino.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar un aeropuerto de destino.");
                return;
            }
            if (fechaSalida == null || fechaLlegada == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe ingresar las fechas y horas de salida y llegada.");
                return;
            }
            if (!fechaLlegada.isAfter(fechaSalida)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Fechas Inválidas", "La fecha/hora de llegada debe ser posterior a la fecha/hora de salida.");
                return;
            }

            if (puertaEmbarqueSeleccion == null || puertaEmbarqueSeleccion.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar una Puerta de Embarque.");
                return;
            }

            // Determinar el número de orden del segmento actual
            int ordenActual = (segmentoActual.getOrdenSegmento() != null && segmentoActual.getOrdenSegmento() > 0)
                    ? segmentoActual.getOrdenSegmento()
                    : segmentos.size() + 1;

            // ✅ CORRECCIÓN: Si NO es el primer tramo (ordenActual > 1), validar contra el tramo ANTERIOR
            if (ordenActual > 1 && !segmentos.isEmpty()) {
                int indiceAnterior = ordenActual - 2; // El tramo previo en la lista (index 0-based)
                if (indiceAnterior >= 0 && indiceAnterior < segmentos.size()) {
                    SegmentoVuelo tramoAnterior = segmentos.get(indiceAnterior);
                    // Si el tramo anterior no es el mismo que estamos editando
                    if (tramoAnterior != segmentoActual && tramoAnterior.getHoraLlegada() != null) {
                        LocalDateTime llegadaAnterior = tramoAnterior.getHoraLlegada().toLocalDateTime();
                        if (fechaSalida.isBefore(llegadaAnterior)) {
                            addMessage(FacesMessage.SEVERITY_ERROR, "Inconsistencia de Escala",
                                    "La salida de este tramo debe ser posterior a la llegada del tramo anterior (" + llegadaAnterior.toString().replace("T", " ") + ").");
                            return;
                        }
                    }
                }
            }

            // Asignar propiedades al segmento
            segmentoActual.setAeropuertoOrigen(aeropuertos.get(this.aeropuertoOrigen));
            segmentoActual.setAeropuertoDestino(aeropuertos.get(this.aeropuertoDestino));
            segmentoActual.setHoraSalida(getFechaSalidaAsTimestamp());
            segmentoActual.setHoraLlegada(getFechaLlegadaAsTimestamp());
            segmentoActual.setOrdenSegmento(ordenActual);

            if (puertaEmbarqueSeleccion != null && !puertaEmbarqueSeleccion.isEmpty()) {
                PuertaEmbarque pe = puertaService.findById(Integer.valueOf(puertaEmbarqueSeleccion)).orElse(null);
                if (pe != null) {
                    asignacionesPuertasTemp.put(segmentoActual, pe);
                }
            }

            if (!segmentos.contains(segmentoActual)) {
                segmentos.add(segmentoActual);
            }

            openNewSegmento();
            reiniciarSeleccionMapa();
            addMessage(FacesMessage.SEVERITY_INFO, "Escala Guardada", "El tramo se ha procesado correctamente.");
        } catch (Exception e) {
            Logger.logError("Error en agregarSegmentoEnMemoria: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo agregar el tramo: " + e.getMessage());
        }
    }

    @Transactional
    public void confirmarYGuardarVueloCompleto() {
        try {
            if (pilotoId == null || pilotoId.isEmpty() || aerolineaId == null || aerolineaId.isEmpty() || avionId == null || avionId.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Complete todos los campos de la cabecera en el Paso 1.");
                return;
            }

            if (segmentos.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Ruta Incompleta", "Debe agregar al menos un tramo/escala en el Paso 3 antes de confirmar.");
                return;
            }

            if (preciosAsientos == null || preciosAsientos.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "No hay clases de asiento para configurar. Seleccione una aeronave primero.");
                return;
            }

            boolean error = false;
            for (ClaseAsientoPrecioDto dto : preciosAsientos) {
                if (dto.getPrecio() <= 0) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Tarifa Requerida", "Ingrese un precio mayor a $0 para la clase: " + dto.getClase());
                    error = true;
                    return;
                }
            }

            Piloto p = pilotoService.findByRut(pilotoId).orElseThrow(() -> new RuntimeException("Piloto no encontrado"));
            Aerolinea a = aerolineaService.findById(Integer.parseInt(aerolineaId));
            Avion av = aviones.get(avionId);

            if (av.getEstadoDeMantenimiento() != null && av.getEstadoDeMantenimiento().equalsIgnoreCase("EN MANTENIMIENTO")) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Aeronave No Disponible", "El avión seleccionado se encuentra en mantenimiento.");
                return;
            }



            EstadoVuelo estado = estadoVueloService.findById(1).orElse(null);

            vuelo.setAvion(av);
            vuelo.setPiloto(p);
            vuelo.setAerolinea(a);
            vuelo.setEstadoVuelo(estado);

            // LOG: Inicio del proceso


            // 1. Persistir Vuelo
            vuelo = vueloService.save(vuelo);


            // 2. Persistir Tarifas
            for (ClaseAsientoPrecioDto dto : preciosAsientos) {
                PrecioAsiento pa = precioAsientoService.findByVueloAndClase(vuelo.getIdVuelo(), dto.getClaseAsiento())
                        .orElse(new PrecioAsiento());
                pa.setVuelo(vuelo);
                pa.setPrecio(dto.getPrecio());
                pa.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(dto.getClaseAsiento()).orElse(null));
                precioAsientoService.guardarPrecioAsiento(pa);
            }

            // 3. Persistir Segmentos y Puertas con LOGS DE FECHAS

            for (int i = 0; i < segmentos.size(); i++) {
                SegmentoVuelo seg = segmentos.get(i);

                // LOG PRE-PERSISTENCIA


                seg.setVuelo(vuelo);
                SegmentoVuelo segGuardado = segmentoVueloService.save(seg);

                // LOG POST-PERSISTENCIA

                if (asignacionesPuertasTemp.containsKey(seg)) {
                    PuertaEmbarque pe = asignacionesPuertasTemp.get(seg);
                    AsignacionPuerta ap = asignacionPuertaService.findBySegmentoVuelo(segGuardado)
                            .stream().findFirst().orElse(new AsignacionPuerta());
                    ap.setSegmentoVuelo(segGuardado);
                    ap.setPuertaEmbarque(pe);
                    asignacionPuertaService.save(ap);


                }
            }

            // 4. Sincronización con la Base de Datos
            vuelo = vueloService.findById(vuelo.getIdVuelo()).orElse(vuelo);
            entityManager.flush();   // Envía los INSERTs/UPDATEs pendientes a PostgreSQL
            entityManager.refresh(vuelo); // Obliga a releyendo el estado real de la BD

            this.numeroVuelo = vuelo.getNumeroVuelo();
            vueloCreado = true;


            Logger.logInfo(vuelo.toString());

            addMessage(FacesMessage.SEVERITY_INFO, "¡Vuelo Creado Exitosamente!",
                    "El vuelo con código " + (numeroVuelo != null ? numeroVuelo : vuelo.getIdVuelo()) + " y " + segmentos.size() + " escala(s) ha sido registrado.");
        } catch (Exception e) {
            Logger.logError("Error en confirmarYGuardarVueloCompleto: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Persistencia", "No se pudo guardar el vuelo: " + e.getMessage());
        }
    }

    public void eliminarSegmento(SegmentoVuelo segmento) {
        if (segmento != null) {
            if (segmento.getIdSegmento() != null) {
                segmentoVueloService.deleteById(segmento.getIdSegmento());
            }
            segmentos.remove(segmento);
            asignacionesPuertasTemp.remove(segmento);
            addMessage(FacesMessage.SEVERITY_INFO, "Escala Removida", "El tramo se ha eliminado de la lista.");
        }
    }

    public void ajaxListener(AjaxBehaviorEvent event) {
        if (avionId != null && !avionId.isEmpty()) {
            cargarTarifasYCapacidades(Integer.parseInt(avionId), vuelo != null ? vuelo.getIdVuelo() : null);
            avionSeleccionado = aviones.get(this.avionId);
        }
    }

    public void guardarTarifasAsientos() {
        if (preciosAsientos == null || preciosAsientos.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "No hay clases de asiento para configurar. Seleccione una aeronave primero.");
            return;
        }

        boolean error = false;
        for (ClaseAsientoPrecioDto dto : preciosAsientos) {
            if (dto.getPrecio() <= 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Tarifa Requerida", "Ingrese un precio mayor a $0 para la clase: " + dto.getClase());
                error = true;
            }
        }

        if (!error) {
            addMessage(FacesMessage.SEVERITY_INFO, "Tarifas Registradas", "Se han validado y guardado las tarifas base por cabina.");
        }
    }

    public void openNewSegmento() {
        this.segmentoActual = new SegmentoVuelo();
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        this.fechaSalida = null;
        this.fechaLlegada = null;
        this.puertaEmbarqueSeleccion = null;
        this.puertaEmbarques = new ArrayList<>();
    }

    public void prepararEdicion(SegmentoVuelo seg) {
        this.segmentoActual = seg;

        if (seg.getAeropuertoOrigen() != null) {
            this.aeropuertoOrigen = seg.getAeropuertoOrigen().getCodigoIata();
            this.puertaEmbarques = puertaService.findByAeropuerto(seg.getAeropuertoOrigen().getIdAeropuerto());

            if (asignacionesPuertasTemp.containsKey(seg)) {
                PuertaEmbarque pe = asignacionesPuertasTemp.get(seg);
                if (pe != null) {
                    this.puertaEmbarqueSeleccion = String.valueOf(pe.getIdPuerta());
                }
            } else if (seg.getIdSegmento() != null) {
                asignacionPuertaService.findBySegmentoVuelo(seg).stream()
                        .findFirst()
                        .ifPresent(ap -> {
                            if (ap.getPuertaEmbarque() != null) {
                                this.puertaEmbarqueSeleccion = String.valueOf(ap.getPuertaEmbarque().getIdPuerta());
                                this.asignacionesPuertasTemp.put(seg, ap.getPuertaEmbarque());
                            }
                        });
            }
        }

        if (seg.getAeropuertoDestino() != null) {
            this.aeropuertoDestino = seg.getAeropuertoDestino().getCodigoIata();
        }

        if (seg.getHoraSalida() != null) {
            this.fechaSalida = seg.getHoraSalida().toLocalDateTime();
        }
        if (seg.getHoraLlegada() != null) {
            this.fechaLlegada = seg.getHoraLlegada().toLocalDateTime();
        }
    }

    // Devuelve el JSON con la información de todos los segmentos para el mapa general
    public String getSegmentosJson() {
        if (segmentos == null || segmentos.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < segmentos.size(); i++) {
            SegmentoVuelo s = segmentos.get(i);
            AeropuertoMapaProjection origen = buscarAeropuertoPrj(s.getAeropuertoOrigen().getCodigoIata());
            AeropuertoMapaProjection destino = buscarAeropuertoPrj(s.getAeropuertoDestino().getCodigoIata());

            if (i > 0) sb.append(",");
            sb.append("{")
                    .append("\"ordenSegmento\":").append(s.getOrdenSegmento()).append(",")
                    .append("\"aeropuertoOrigen\":{")
                    .append("\"codigoIata\":\"").append(s.getAeropuertoOrigen().getCodigoIata()).append("\",")
                    .append("\"nombreAeropuerto\":\"").append(escapeJson(s.getAeropuertoOrigen().getNombreAeropuerto())).append("\",")
                    .append("\"latitud\":").append(origen.getLatitud()).append(",")
                    .append("\"longitud\":").append(origen.getLongitud())
                    .append("},")
                    .append("\"aeropuertoDestino\":{")
                    .append("\"codigoIata\":\"").append(s.getAeropuertoDestino().getCodigoIata()).append("\",")
                    .append("\"nombreAeropuerto\":\"").append(escapeJson(s.getAeropuertoDestino().getNombreAeropuerto())).append("\",")
                    .append("\"latitud\":").append(destino.getLatitud()).append(",")
                    .append("\"longitud\":").append(destino.getLongitud())
                    .append("}")
                    .append("}");
        }
        sb.append("]");
        return sb.toString();
    }

    public AeropuertoMapaProjection buscarAeropuertoPrj(String codigoIATA){
        for (AeropuertoMapaProjection projection : listaAeropuertosJSON) {
            if (projection.getCodigoIata().equalsIgnoreCase(codigoIATA)) return projection;
        }
        return null;
    }
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\"", "\\\"").replace("\n", "").replace("\r", "");
    }

    public void seleccionarOrigenDesdeMapa() {
        String iata = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("iata");
        this.aeropuertoOrigen = iata;
        Aeropuerto aero = aeropuertos.get(iata);
        if (aero != null) {
            this.puertaEmbarques = puertaService.findByAeropuerto(aero.getIdAeropuerto());
        } else {
            this.puertaEmbarques = new ArrayList<>();
        }
        this.puertaEmbarqueSeleccion = null;
    }

    public void seleccionarDestinoDesdeMapa() {
        String iata = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("iata");
        if (iata != null && iata.equals(this.aeropuertoOrigen)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Selección inválida", "El destino no puede ser igual al origen.");
            return;
        }
        this.aeropuertoDestino = iata;
    }

    public void reiniciarSeleccionMapa() {
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        this.puertaEmbarques = new ArrayList<>();
        this.puertaEmbarqueSeleccion = null;
    }

    public void editarVuelo(Vuelo vuelo) throws IOException {

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


    public Timestamp getFechaSalidaAsTimestamp() {
        return fechaSalida != null ? Timestamp.from(fechaSalida.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    public Timestamp getFechaLlegadaAsTimestamp() {
        return fechaLlegada != null ? Timestamp.from(fechaLlegada.atZone(ZoneId.systemDefault()).toInstant()) : null;
    }


    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }
    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }
    public LocalDateTime getFechaLlegada() { return fechaLlegada; }
    public void setFechaLlegada(LocalDateTime fechaLlegada) { this.fechaLlegada = fechaLlegada; }
    public Double getPrecioBase() { return precioBase; }
    public void setPrecioBase(Double precioBase) { this.precioBase = precioBase; }
    public Integer getIdAerolinea() { return idAerolinea; }
    public void setIdAerolinea(Integer idAerolinea) { this.idAerolinea = idAerolinea; }
    public String getRutPiloto() { return rutPiloto; }
    public void setRutPiloto(String rutPiloto) { this.rutPiloto = rutPiloto; }
    public Integer getIdAvion() { return idAvion; }
    public void setIdAvion(Integer idAvion) { this.idAvion = idAvion; }
    public Integer getIdEstadoVuelo() { return idEstadoVuelo; }
    public void setIdEstadoVuelo(Integer idEstadoVuelo) { this.idEstadoVuelo = idEstadoVuelo; }
    public Vuelo getVuelo() { return vuelo; }
    public void setVuelo(Vuelo vuelo) { this.vuelo = vuelo; }
    public List<SegmentoVuelo> getSegmentos() { return segmentos; }
    public void setSegmentos(List<SegmentoVuelo> segmentos) { this.segmentos = segmentos; }
    public List<Avion> getAvions() { return avions; }
    public List<Aerolinea> getAerolineas() { return aerolineas; }
    public List<Piloto> getListaPilotos() { return listaPilotos; }
    public List<Aeropuerto> getListaAeropuertos() { return listaAeropuertos; }
    public boolean isVueloCreado() { return vueloCreado; }
    public String getAerolineaId() { return aerolineaId; }
    public void setAerolineaId(String aerolineaId) { this.aerolineaId = aerolineaId; }
    public String getAvionId() { return avionId; }
    public void setAvionId(String avionId) { this.avionId = avionId; }
    public String getPilotoId() { return pilotoId; }
    public void setPilotoId(String pilotoId) { this.pilotoId = pilotoId; }
    public SegmentoVuelo getSegmentoActual() { return segmentoActual; }
    public void setSegmentoActual(SegmentoVuelo segmentoActual) { this.segmentoActual = segmentoActual; }
    public String getAeropuertoOrigen() { return aeropuertoOrigen; }
    public void setAeropuertoOrigen(String aeropuertoOrigen) { this.aeropuertoOrigen = aeropuertoOrigen; }
    public String getAeropuertoDestino() { return aeropuertoDestino; }
    public void setAeropuertoDestino(String aeropuertoDestino) { this.aeropuertoDestino = aeropuertoDestino; }
    public List<ClaseAsientoPrecioDto> getPreciosAsientos() { return preciosAsientos; }
    public void setPreciosAsientos(List<ClaseAsientoPrecioDto> preciosAsientos) { this.preciosAsientos = preciosAsientos; }
    public List<PuertaEmbarque> getPuertaEmbarques() { return puertaEmbarques; }
    public void setPuertaEmbarques(List<PuertaEmbarque> puertaEmbarques) { this.puertaEmbarques = puertaEmbarques; }
    public String getPuertaEmbarqueSeleccion() { return puertaEmbarqueSeleccion; }
    public void setPuertaEmbarqueSeleccion(String puertaEmbarqueSeleccion) { this.puertaEmbarqueSeleccion = puertaEmbarqueSeleccion; }
    public String getAeropuertosMapaJson() { return aeropuertosMapaJson; }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public List<Vuelo> getVuelosFiltrados() {
        return vuelosFiltrados;
    }

    public void setVuelosFiltrados(List<Vuelo> vuelosFiltrados) {
        this.vuelosFiltrados = vuelosFiltrados;
    }
}