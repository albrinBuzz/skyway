package com.SkyWay.views.Vuelo;

import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import com.SkyWay.modules.aerolinea.domain.service.AerolineaService;
import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.asignacionpuerta.domain.service.AsignacionPuertaService;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.repository.AvionRepository;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.capacidadclase.domain.service.CapacidadClaseService;
import com.SkyWay.modules.capacidadclase.presentation.dto.ClaseAsientoPrecioDto;
import com.SkyWay.modules.claseasiento.domain.service.ClaseAsientoService;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.piloto.domain.repository.PilotoRepositoy;
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
import java.util.stream.Collectors;

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
    private Map<Integer, Integer> tarifasHistoricas = new HashMap<>();

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
    @Autowired
    private PilotoRepositoy pilotoRepository;

    private String detalleFisicoExplicacion; // Almacena el desglose técnico opcional
    private boolean mostrarDetalleFisico = false; // Controla la visibilidad del box secundario

    @Autowired
    private AvionRepository avionRepository;

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
    private Integer idVueloParam;
    private List<Avion> avionesFiltradosModal;
    private String textoBusquedaAvion;
    private String filtroClasesAvion = "TODAS";
    private Map<Integer, List<CapacidadClase>> capacidadesPorAvionMap = new HashMap<>();
    private Piloto pilotoSeleccionado;
    private List<Piloto> pilotosFiltradosModal;
    private String textoBusquedaPiloto;
    private boolean pilotoEnConflicto = false;
    private boolean avionEnConflicto = false;



    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String idVueloStr = externalContext.getRequestParameterMap().get("vueloId");

        listaAeropuertos = aeropuertoService.findAll();
        this.listaAeropuertosJSON = aeropuertoService.findAllConCoordenadas();
        listaAviones = avionService.findAll();
        recargarMapaCapacidades();
        avions = new ArrayList<>(listaAviones);
        this.avionesFiltradosModal = new ArrayList<>(listaAviones);
        listaPilotos = pilotoService.findAll();
        this.pilotosFiltradosModal = listaPilotos != null ? new ArrayList<>(listaPilotos) : new ArrayList<>();
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

        if (idVueloStr != null && !idVueloStr.isEmpty()) {
            this.idVueloParam = Integer.valueOf(idVueloStr);
            segmentos = segmentoVueloService.findByIdVuelo(idVueloParam);
            vuelo = vueloService.findById(idVueloParam).orElse(null);

            if (vuelo != null) {
                this.numeroVuelo = vuelo.getNumeroVuelo();
                this.pilotoId = vuelo.getPiloto() != null ? vuelo.getPiloto().getRut() : null;
                this.pilotoSeleccionado = vuelo.getPiloto();
                this.aerolineaId = vuelo.getAerolinea() != null ? String.valueOf(vuelo.getAerolinea().getIdAerolinea()) : null;
                this.avionId = vuelo.getAvion() != null ? String.valueOf(vuelo.getAvion().getIdAvion()) : null;
                this.avionSeleccionado = vuelo.getAvion();

                cargarTarifasYCapacidades(vuelo.getAvion().getIdAvion(), idVueloParam);
                vueloCreado = true;
            }
        } else {
            vuelo = new Vuelo();
            segmentos = new ArrayList<>();
            vuelos = vueloService.findAll();
            if (aerolineas != null && !aerolineas.isEmpty()) {
                Aerolinea aerolineaPorDefecto = aerolineas.stream()
                        .filter(a -> a.getNombre() != null && a.getNombre().toLowerCase().contains("skyway"))
                        .findFirst()
                        .orElse(aerolineas.get(0));
                this.aerolineaId = String.valueOf(aerolineaPorDefecto.getIdAerolinea());
            }
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            this.aeropuertosMapaJson = mapper.writeValueAsString(aeropuertoService.findAllParaMapa());
        } catch (Exception e) {
            this.aeropuertosMapaJson = "[]";
        }
    }




    private void cargarTarifasYCapacidades(Integer idAvion, Integer idVuelo) {
        // 2. Acumular/Actualizar los precios ingresados actualmente en el historial global
        if (preciosAsientos != null) {
            for (ClaseAsientoPrecioDto dto : preciosAsientos) {
                if (dto.getPrecio() > 0) {
                    this.tarifasHistoricas.put(dto.getClaseAsiento(), dto.getPrecio());
                }
            }
        }

        // 3. Limpiar la lista actual de la vista y cargar las capacidades del nuevo avión
        preciosAsientos = new ArrayList<>();
        List<CapacidadClase> capacidades = capacidadClaseService.findByAvionId(idAvion);

        for (CapacidadClase cap : capacidades) {
            ClaseAsientoPrecioDto dto = new ClaseAsientoPrecioDto();
            int idClase = cap.getClaseAsiento1().getIdClase();

            dto.setClaseAsiento(idClase);
            dto.setClase(cap.getClaseAsiento1().getDescripcion());
            dto.setCantidad(cap.getCantidad());

            // 4. Buscar la tarifa según prioridad:
            // A) Historial acumulado en memoria del Bean (Cubre si cambió de avión y volvió)
            // B) Si no está en historial pero el vuelo ya existe en BD
            // C) 0 por defecto si es una clase nunca configurada
            if (this.tarifasHistoricas.containsKey(idClase)) {
                dto.setPrecio(this.tarifasHistoricas.get(idClase));
            } else if (idVuelo != null) {
                precioAsientoService.findByVueloAndClase(idVuelo, idClase)
                        .ifPresentOrElse(
                                pa -> {
                                    dto.setPrecio(pa.getPrecio());
                                    this.tarifasHistoricas.put(idClase, pa.getPrecio()); // Guardar también en el historial
                                },
                                () -> dto.setPrecio(0)
                        );
            } else {
                dto.setPrecio(0);
            }

            preciosAsientos.add(dto);
        }
    }

    public void agregarSegmentoEnMemoria() {
        try {
            boolean hayError = false;

            if (this.aeropuertoOrigen == null || this.aeropuertoOrigen.trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar un aeropuerto de origen.");
                hayError = true;
            }
            if (this.aeropuertoDestino == null || this.aeropuertoDestino.trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar un aeropuerto de destino.");
                hayError = true;
            }
            if (this.fechaSalida == null || this.fechaLlegada == null) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe ingresar las fechas y horas de salida y llegada.");
                hayError = true;
            }
            if (this.puertaEmbarqueSeleccion == null || this.puertaEmbarqueSeleccion.trim().isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe seleccionar una Puerta de Embarque.");
                hayError = true;
            }

            if (hayError) {
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            if (!fechaLlegada.isAfter(fechaSalida)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Fechas Inválidas", "La fecha/hora de llegada debe ser posterior a la fecha/hora de salida.");
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            Aeropuerto origenObj = aeropuertos.get(this.aeropuertoOrigen);
            Aeropuerto destinoObj = aeropuertos.get(this.aeropuertoDestino);

            if (origenObj.equals(destinoObj)) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Ruta Inválida", "El aeropuerto de origen y destino no pueden ser el mismo en un tramo.");
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // El vuelo posee exclusivamente 1 segmento directo (orden = 1); las escalas pertenecen al Itinerario
            int ordenActual = 1;

            // ✅ 2. VALIDACIÓN GEOGRÁFICA CON CONSEJO CONCRETO EN FECHAS Y HORAS
            // ✅ VALIDACIÓN GEOGRÁFICA CON EXPLICACIÓN DETALLADA OPCIONAL
            if (origenObj.getPosicion() != null && destinoObj.getPosicion() != null) {
                double distanciaGrados = origenObj.getPosicion().distance(destinoObj.getPosicion());
                double distanciaKm = distanciaGrados * 111.0;

                double horasEstimadasMinimas = (distanciaKm / 800.0) + 0.75;
                long minutosRealesVuelo = java.time.Duration.between(fechaSalida, fechaLlegada).toMinutes();
                long minutosMinimosEsperados = (long) (horasEstimadasMinimas * 60);

                if (minutosRealesVuelo < minutosMinimosEsperados) {
                    long horasMin = minutosMinimosEsperados / 60;
                    long minsMin = minutosMinimosEsperados % 60;

                    LocalDateTime sugerenciaLlegada = fechaSalida.plusMinutes(minutosMinimosEsperados);
                    String formatoSugerencia = sugerenciaLlegada.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

                    // Texto legible formateado para el mensaje principal
                    String tiempoTexto = (horasMin > 0 ? horasMin + (horasMin == 1 ? " hora" : " horas") : "") +
                            (horasMin > 0 && minsMin > 0 ? " y " : "") +
                            (minsMin > 0 ? minsMin + " minutos" : "");
                    if (horasMin == 0 && minsMin == 0) tiempoTexto = "0 minutos";

                    // Texto legible formateado para el desglose técnico
                    double horasVueloPuras = distanciaKm / 800.0;
                    long horasVueloEnteras = (long) horasVueloPuras;
                    long minsVueloRestantes = Math.round((horasVueloPuras - horasVueloEnteras) * 60);
                    String tiempoVueloTexto = (horasVueloEnteras > 0 ? horasVueloEnteras + "h " : "") + minsVueloRestantes + "m";

                    // 1. Mensaje principal claro y conciso
                    addMessage(FacesMessage.SEVERITY_ERROR, "Duración Física Insuficiente",
                            String.format("Distancia entre %s y %s: ~%.0f km. Requiere mínimo %s. Sugerencia de llegada: %s.",
                                    origenObj.getCodigoIata(), destinoObj.getCodigoIata(), distanciaKm, tiempoTexto, formatoSugerencia));

                    // 2. Explicación sencilla con horas y minutos claros para lectura rápida
                    this.detalleFisicoExplicacion = String.format(
                            "✈️ ¿Por qué el sistema rechaza este horario?\n" +
                                    "• Distancia del trayecto: Aprox. %.0f kilómetros en línea recta entre ambos aeropuertos.\n" +
                                    "• Velocidad de vuelo: Un avión comercial vuela a una velocidad estándar de 800 km/h, tardando aproximadamente %s solo en recorrer el aire.\n" +
                                    "• Operaciones en tierra: Se requieren 45 minutos obligatorios para rodaje en pista, despegue, ascenso, descenso y aterrizaje.\n" +
                                    "• Conclusión: Por física y seguridad aérea, este tramo no puede completarse en menos de %s.",
                            distanciaKm, tiempoVueloTexto, tiempoTexto
                    );
                    this.mostrarDetalleFisico = false; // Inicia colapsado

                    FacesContext.getCurrentInstance().validationFailed();
                    return;
                }
            }

            // ✅ 3. EVITAR VUELOS EXAGERADOS
            long duracionHoras = java.time.Duration.between(fechaSalida, fechaLlegada).toHours();
            if (duracionHoras > 18) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Duración Exagerada",
                        "Un solo segmento de vuelo no puede durar más de 18 horas continuas. Verifique las fechas ingresadas.");
                FacesContext.getCurrentInstance().validationFailed();
                return;
            }

            // Asignar propiedades al segmento solo si pasó TODAS las validaciones
            segmentoActual.setAeropuertoOrigen(origenObj);
            segmentoActual.setAeropuertoDestino(destinoObj);
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
                segmentos.clear();
                segmentos.add(segmentoActual);
            }

            actualizarDisponibilidadRecursos();

            this.detalleFisicoExplicacion = null;
            this.mostrarDetalleFisico = false;
            addMessage(FacesMessage.SEVERITY_INFO, "Ruta Definida", "El tramo único del vuelo se ha validado correctamente.");

        } catch (Exception e) {
            Logger.logError("Error en agregarSegmentoEnMemoria: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo agregar el tramo: " + e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    @Transactional
    public void confirmarYGuardarVueloCompleto() {
        try {
            if (segmentos == null || segmentos.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Ruta Incompleta", "Debe agregar al menos un tramo/escala y sus fechas en el Paso 1 antes de confirmar.");
                return;
            }

            if (pilotoId == null || pilotoId.isEmpty() || aerolineaId == null || aerolineaId.isEmpty() || avionId == null || avionId.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Complete la aerolínea, comandante y aeronave en el Paso 2.");
                return;
            }

            if (pilotoEnConflicto || avionEnConflicto) {
                addMessage(FacesMessage.SEVERITY_WARN, "Conflicto de Horarios", "Reasigne un comandante y una aeronave disponibles para las fechas del Paso 1.");
                return;
            }

            if (preciosAsientos == null || preciosAsientos.isEmpty()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Validación", "No hay clases de asiento para configurar. Seleccione una aeronave en el Paso 2.");
                return;
            }

            for (ClaseAsientoPrecioDto dto : preciosAsientos) {
                if (dto.getPrecio() <= 20000) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Tarifa Requerida", "Ingrese un precio mayor a $20.000 para la clase: " + dto.getClase());
                    return;
                }
            }

            // =========================================================================
            // 1. OBTENER ENTIDADES
            // =========================================================================
            Piloto p = pilotoService.findByRut(pilotoId)
                    .orElseThrow(() -> new RuntimeException("Piloto no encontrado"));
            Aerolinea a = aerolineaService.findById(Integer.parseInt(aerolineaId));
            Avion av = aviones.get(avionId);

            if (av == null) {
                av = avionService.findById(Integer.parseInt(avionId)).orElse(null);
            }

            // =========================================================================
            // 2. VALIDACIÓN DE ESTADO DE MANTENIMIENTO DEL AVIÓN
            // =========================================================================
            if (av != null && av.getEstadoDeMantenimiento() != null && av.getEstadoDeMantenimiento().equalsIgnoreCase("EN MANTENIMIENTO")) {
                addMessage(FacesMessage.SEVERITY_ERROR, "Aeronave No Disponible", "El avión seleccionado (" + av.getNumeroDeRegistro() + ") se encuentra en mantenimiento.");
                return;
            }

            // =========================================================================
            // 3. CÁLCULO DE RANGO GLOBAL DE FECHAS Y ASIGNACIÓN AL VUELO
            // =========================================================================
            SegmentoVuelo primerSegmento = segmentos.get(0);
            SegmentoVuelo ultimoSegmento = segmentos.get(segmentos.size() - 1);

            Timestamp fechaHoraSalidaGlobal = new Timestamp(primerSegmento.getHoraSalida().getTime());
            Timestamp fechaHoraLlegadaGlobal = new Timestamp(ultimoSegmento.getHoraLlegada().getTime());

            // FIX: Asignar las fechas globales al objeto vuelo en memoria antes de evaluar o guardar
            vuelo.setFechaHoraSalida(fechaHoraSalidaGlobal);
            vuelo.setFechaHoraLlegada(fechaHoraLlegadaGlobal);

            // FIX: Asegurar la extracción correcta del ID del vuelo (si ya existe en BD)
            Integer idVueloActual = (vuelo != null && vuelo.getIdVuelo() != null && vuelo.getIdVuelo() > 0)
                    ? vuelo.getIdVuelo()
                    : null;

            // =========================================================================
            // 4. VALIDACIÓN DE CONFLICTO DE HORARIO PARA EL PILOTO
            // =========================================================================
            boolean conflictoPiloto = vueloService.existeConflictoPiloto(
                    p.getRut(),
                    fechaHoraSalidaGlobal,
                    fechaHoraLlegadaGlobal,
                    idVueloActual
            );

            if (conflictoPiloto) {
                addMessage(FacesMessage.SEVERITY_ERROR,
                        "Conflicto de Agenda - Piloto",
                        "El comandante " + p.getUsuario().getNombre() + " " + p.getUsuario().getApellido() +
                                " ya tiene asignado otro vuelo en el rango horario de este itinerario.");
                return;
            }

            // =========================================================================
            // 5. VALIDACIÓN DE CONFLICTO DE HORARIO PARA LA AERONAVE
            // =========================================================================
            boolean conflictoAvion = vueloService.existeConflictoAvion(
                    av.getIdAvion(),
                    fechaHoraSalidaGlobal,
                    fechaHoraLlegadaGlobal,
                    idVueloActual
            );

            if (conflictoAvion) {
                addMessage(FacesMessage.SEVERITY_ERROR,
                        "Conflicto de Agenda - Aeronave",
                        "La aeronave " + av.getModeloAvion().getNombre() + " [" + av.getNumeroDeRegistro() + "]" +
                                " ya se encuentra programada para otro vuelo en este mismo rango de fechas.");
                return;
            }

            EstadoVuelo estado = estadoVueloService.findById(1).orElse(null);

            vuelo.setAvion(av);
            vuelo.setPiloto(p);
            vuelo.setAerolinea(a);
            if (vuelo.getEstadoVuelo() == null) {
                vuelo.setEstadoVuelo(estado);
            }
            // 1. Guardar primero el Vuelo (se crea la fila con numero_vuelo null temporalmente)
            vuelo = vueloService.save(vuelo);

            // 2. Guardar Tarifas
            for (ClaseAsientoPrecioDto dto : preciosAsientos) {
                PrecioAsiento pa = precioAsientoService.findByVueloAndClase(vuelo.getIdVuelo(), dto.getClaseAsiento())
                        .orElse(new PrecioAsiento());
                pa.setVuelo(vuelo);
                pa.setPrecio(dto.getPrecio());
                pa.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(dto.getClaseAsiento()).orElse(null));
                precioAsientoService.guardarPrecioAsiento(pa);
            }

            // 3. Guardar Segmentos (AQUÍ ES DONDE EL TRIGGER DE POSTGRESQL SE EJECUTA E INSCRIME EL NUMERO_VUELO)
            for (int i = 0; i < segmentos.size(); i++) {
                SegmentoVuelo seg = segmentos.get(i);
                PuertaEmbarque puertaTemp = asignacionesPuertasTemp.remove(seg);
                seg.setVuelo(vuelo);
                SegmentoVuelo segGuardado = segmentoVueloService.save(seg);
                segmentos.set(i, segGuardado);

                if (puertaTemp != null) {
                    asignacionesPuertasTemp.put(segGuardado, puertaTemp);
                    AsignacionPuerta ap = asignacionPuertaService.findBySegmentoVuelo(segGuardado)
                            .stream().findFirst().orElse(new AsignacionPuerta());
                    ap.setSegmentoVuelo(segGuardado);
                    ap.setPuertaEmbarque(puertaTemp);
                    asignacionPuertaService.save(ap);
                }
            }

            // 4. Sincronización con la Base de Datos y Refresco de JPA
            // ⚠️ Forzamos a enviar todas las sentencias SQL pendiendes a la BD
            entityManager.flush();

            // ⚠️ OBLIGAMOS A HIBERNATE A RECARGAR LA ENTIDAD DIRECTAMENTE DE LA BD (ignorando el caché en memoria)
            entityManager.refresh(vuelo);

            // Ahora vuelo.getNumeroVuelo() contendrá el valor actualizado por el Trigger
            if (vuelo != null) {
                String numGenerado = vuelo.getNumeroVuelo();
                if ((numGenerado == null || numGenerado.isBlank()) && vuelo.getIdVuelo() != null) {
                    Object dbNum = entityManager
                            .createNativeQuery("SELECT numero_vuelo FROM vuelo WHERE id_vuelo = :id")
                            .setParameter("id", vuelo.getIdVuelo())
                            .getSingleResult();
                    if (dbNum != null) {
                        numGenerado = dbNum.toString();
                        vuelo.setNumeroVuelo(numGenerado);
                    }
                }
                this.numeroVuelo = numGenerado;
                this.vueloCreado = true;
            }

            Logger.logInfo(vuelo.toString());

            addMessage(FacesMessage.SEVERITY_INFO, "¡Vuelo Guardado Exitosamente!",
                    "El vuelo con código " + (this.numeroVuelo != null ? this.numeroVuelo : vuelo.getIdVuelo()) + " y " + segmentos.size() + " escala(s) ha sido registrado/actualizado.");


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
            actualizarDisponibilidadRecursos();
            addMessage(FacesMessage.SEVERITY_INFO, "Escala Removida", "El tramo se ha eliminado de la lista.");
        }
    }

    public void ajaxListener(AjaxBehaviorEvent event) {
        if (avionId != null && !avionId.isEmpty()) {
            cargarTarifasYCapacidades(Integer.parseInt(avionId), vuelo != null ? vuelo.getIdVuelo() : null);
            avionSeleccionado = aviones.get(this.avionId);
        }
    }

    public void seleccionarAvionDirecto(Avion av) {
        if (av != null) {
            this.avionId = String.valueOf(av.getIdAvion());
            this.avionSeleccionado = av;
            this.avionEnConflicto = false;
            cargarTarifasYCapacidades(av.getIdAvion(), vuelo != null ? vuelo.getIdVuelo() : null);
            addMessage(FacesMessage.SEVERITY_INFO, "Aeronave Asignada", 
                av.getModeloAvion().getNombre() + " [" + av.getNumeroDeRegistro() + "] asignada exitosamente.");
        }
    }

    public void desasignarAvion() {
        this.avionId = null;
        this.avionSeleccionado = null;
        this.avionEnConflicto = false;
        this.preciosAsientos = new ArrayList<>();
        addMessage(FacesMessage.SEVERITY_WARN, "Aeronave Removida", "Se ha removido la asignación de la aeronave.");
    }

    private void recargarMapaCapacidades() {
        Map<Integer, List<CapacidadClase>> mapa = new HashMap<>();
        List<CapacidadClase> todas = capacidadClaseService.findAll();
        if (todas != null) {
            for (CapacidadClase cc : todas) {
                if (cc.getAvion1() == null || cc.getAvion1().getIdAvion() == null
                        || cc.getClaseAsiento1() == null || cc.getCantidad() == null || cc.getCantidad() <= 0) {
                    continue;
                }
                mapa.computeIfAbsent(cc.getAvion1().getIdAvion(), k -> new ArrayList<>()).add(cc);
            }
            for (List<CapacidadClase> lista : mapa.values()) {
                lista.sort(Comparator.comparing(c -> c.getClaseAsiento1().getIdClase()));
            }
        }
        this.capacidadesPorAvionMap = mapa;
    }

    public List<CapacidadClase> getCapacidadesAvion(Avion av) {
        if (av == null || av.getIdAvion() == null) {
            return Collections.emptyList();
        }
        List<CapacidadClase> cached = capacidadesPorAvionMap.get(av.getIdAvion());
        if (cached != null) {
            return cached;
        }
        if (av.getCapacidadClases1() == null) {
            return Collections.emptyList();
        }
        return av.getCapacidadClases1().stream()
                .filter(c -> c.getCantidad() != null && c.getCantidad() > 0 && c.getClaseAsiento1() != null)
                .sorted(Comparator.comparing(c -> c.getClaseAsiento1().getIdClase()))
                .collect(Collectors.toList());
    }

    public int getCantidadClasesAvion(Avion av) {
        List<CapacidadClase> caps = getCapacidadesAvion(av);
        if (caps.isEmpty()) {
            return 0;
        }
        return (int) caps.stream()
                .map(c -> c.getClaseAsiento1().getIdClase())
                .distinct()
                .count();
    }

    public String getResumenClasesAvion(Avion av) {
        List<CapacidadClase> caps = getCapacidadesAvion(av);
        if (caps.isEmpty()) {
            return "Sin cabinas configuradas";
        }
        return caps.stream()
                .map(c -> c.getClaseAsiento1().getDescripcion() + ": " + c.getCantidad())
                .collect(Collectors.joining(" · "));
    }

    public void filtrarAvionesModal() {
        if (this.avions == null || (this.avions.isEmpty() && (this.segmentos == null || this.segmentos.isEmpty()))) {
            this.avions = avionService.findAll();
        }
        if (this.avions == null) {
            this.avionesFiltradosModal = new ArrayList<>();
            return;
        }
        String term = (this.textoBusquedaAvion != null) ? this.textoBusquedaAvion.trim().toLowerCase() : "";
        String filtroClases = (this.filtroClasesAvion != null) ? this.filtroClasesAvion.trim() : "TODAS";

        this.avionesFiltradosModal = this.avions.stream().filter(a -> {
            int cantClases = getCantidadClasesAvion(a);
            if ("3".equals(filtroClases) && cantClases != 3) {
                return false;
            }
            if ("2".equals(filtroClases) && cantClases != 2) {
                return false;
            }
            if ("1".equals(filtroClases) && cantClases != 1) {
                return false;
            }
            if (term.isEmpty()) {
                return true;
            }
            String mod = (a.getModeloAvion() != null && a.getModeloAvion().getNombre() != null)
                    ? a.getModeloAvion().getNombre().toLowerCase() : "";
            String fab = (a.getModeloAvion() != null && a.getModeloAvion().getFabricante() != null && a.getModeloAvion().getFabricante().getNombre() != null)
                    ? a.getModeloAvion().getFabricante().getNombre().toLowerCase() : "";
            String mat = (a.getNumeroDeRegistro() != null)
                    ? a.getNumeroDeRegistro().toLowerCase() : "";
            String cap = a.getCapacidadDePasajeros() != null ? String.valueOf(a.getCapacidadDePasajeros()) : "";
            String est = (a.getEstadoDeMantenimiento() != null)
                    ? a.getEstadoDeMantenimiento().toLowerCase() : "operacional";
            String cab = getResumenClasesAvion(a).toLowerCase();
            return mod.contains(term) || fab.contains(term) || mat.contains(term)
                    || cap.contains(term) || est.contains(term) || cab.contains(term);
        }).collect(Collectors.toList());
    }

    public void abrirSelectorAvion() {
        this.textoBusquedaAvion = "";
        this.filtroClasesAvion = "TODAS";
        recargarMapaCapacidades();
        actualizarDisponibilidadRecursos(false);
    }

    public void limpiarFiltroAviones() {
        this.textoBusquedaAvion = "";
        this.filtroClasesAvion = "TODAS";
        actualizarDisponibilidadRecursos(false);
    }

    public void seleccionarPilotoDirecto(Piloto pl) {
        if (pl != null) {
            this.pilotoId = pl.getRut();
            this.pilotoSeleccionado = pl;
            this.pilotoEnConflicto = false;
            String nombreCompleto = pl.getUsuario() != null
                    ? pl.getUsuario().getNombre() + " " + pl.getUsuario().getApellido()
                    : pl.getRut();
            addMessage(FacesMessage.SEVERITY_INFO, "Comandante Asignado",
                    nombreCompleto + " asignado al vuelo.");
        }
    }

    public void desasignarPiloto() {
        this.pilotoId = null;
        this.pilotoSeleccionado = null;
        this.pilotoEnConflicto = false;
        addMessage(FacesMessage.SEVERITY_WARN, "Comandante Removido", "Se ha removido la asignación del piloto comandante.");
    }

    public void abrirSelectorPiloto() {
        this.textoBusquedaPiloto = "";
        actualizarDisponibilidadRecursos(false);
    }

    public void filtrarPilotosModal() {
        if (this.listaPilotos == null || (this.listaPilotos.isEmpty() && (this.segmentos == null || this.segmentos.isEmpty()))) {
            this.listaPilotos = pilotoService.findAll();
        }
        if (this.listaPilotos == null) {
            this.pilotosFiltradosModal = new ArrayList<>();
            return;
        }
        if (this.textoBusquedaPiloto == null || this.textoBusquedaPiloto.trim().isEmpty()) {
            this.pilotosFiltradosModal = new ArrayList<>(this.listaPilotos);
            return;
        }
        String term = this.textoBusquedaPiloto.trim().toLowerCase();
        this.pilotosFiltradosModal = this.listaPilotos.stream().filter(p -> {
            String nombre = (p.getUsuario() != null && p.getUsuario().getNombre() != null)
                    ? p.getUsuario().getNombre().toLowerCase() : "";
            String apellido = (p.getUsuario() != null && p.getUsuario().getApellido() != null)
                    ? p.getUsuario().getApellido().toLowerCase() : "";
            String nombreCompleto = (nombre + " " + apellido).trim();
            String correo = (p.getUsuario() != null && p.getUsuario().getCorreoElectronico() != null)
                    ? p.getUsuario().getCorreoElectronico().toLowerCase() : "";
            String rut = p.getRut() != null ? p.getRut().toLowerCase() : "";
            String esp = p.getEspecializaciones() != null ? p.getEspecializaciones().toLowerCase() : "";
            String exp = String.valueOf(p.getExperienciaAnos());
            return nombreCompleto.contains(term) || rut.contains(term)
                    || esp.contains(term) || correo.contains(term) || exp.contains(term);
        }).collect(Collectors.toList());
    }

    public void guardarTarifasAsientos() {
        if (preciosAsientos == null || preciosAsientos.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "No hay clases de asiento para configurar. Seleccione una aeronave primero.");
            return;
        }

        boolean error = false;
        for (ClaseAsientoPrecioDto dto : preciosAsientos) {
            if (dto.getPrecio() <= 20000) {
                addMessage(FacesMessage.SEVERITY_WARN, "Tarifa Requerida", "Ingrese un precio mayor a $20.000 para la clase: " + dto.getClase());
                error = true;
            }
        }

        if (!error) {
            addMessage(FacesMessage.SEVERITY_INFO, "Tarifas Registradas", "Se han validado y guardado las tarifas base por cabina.");
        }
    }


    public void openNewSegmento() {
        if (segmentos != null && !segmentos.isEmpty()) {
            prepararEdicion(segmentos.get(0));
            return;
        }
        this.segmentoActual = new SegmentoVuelo();
        this.segmentoActual.setOrdenSegmento(1);
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        this.puertaEmbarqueSeleccion = null;
        this.puertaEmbarques = new ArrayList<>();
        this.detalleFisicoExplicacion = null;
        this.mostrarDetalleFisico = false;
        this.fechaSalida = LocalDateTime.now().plusHours(2).withSecond(0).withNano(0);
        this.fechaLlegada = null;
    }

    public void prepararEdicion(SegmentoVuelo seg) {
        this.segmentoActual = seg;
        this.detalleFisicoExplicacion = null;
        this.mostrarDetalleFisico = false;

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

    public boolean isOrigenEncadenado() {
        return false;
    }

    public void setOrigenEncadenado(boolean ignored) {
        // Calculado dinámicamente según el orden del segmento en memoria
    }

    public void seleccionarOrigenDesdeMapa() {
        if (isOrigenEncadenado()) {
            addMessage(FacesMessage.SEVERITY_INFO, "Origen Enlazado",
                    "El origen del tramo (" + this.aeropuertoOrigen + ") coincide automáticamente con la llegada del tramo anterior. Seleccione el aeropuerto de destino.");
            return;
        }
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
        if (isOrigenEncadenado()) {
            this.aeropuertoDestino = null;
            return;
        }
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

    // Devuelve el nombre formateado para el origen (ej. "SCL - Arturo Merino Benítez (Santiago)")
    public String getNombreAeropuertoOrigenCompleto() {
        if (this.aeropuertoOrigen == null || this.aeropuertoOrigen.isEmpty()) return "Sin Seleccionar";
        Aeropuerto a = aeropuertos.get(this.aeropuertoOrigen);
        if (a != null) {
            String ciudadNombre = (a.getCiudad() != null) ? a.getCiudad().getNombre() : "";
            return a.getCodigoIata() + " - " + a.getNombreAeropuerto() + (!ciudadNombre.isEmpty() ? " (" + ciudadNombre + ")" : "");
        }
        return this.aeropuertoOrigen;
    }

    // Devuelve el nombre formateado para el destino (ej. "GRU - Guarulhos (São Paulo)")
    public String getNombreAeropuertoDestinoCompleto() {
        if (this.aeropuertoDestino == null || this.aeropuertoDestino.isEmpty()) return "Sin Seleccionar";
        Aeropuerto a = aeropuertos.get(this.aeropuertoDestino);
        if (a != null) {
            String ciudadNombre = (a.getCiudad() != null) ? a.getCiudad().getNombre() : "";
            return a.getCodigoIata() + " - " + a.getNombreAeropuerto() + (!ciudadNombre.isEmpty() ? " (" + ciudadNombre + ")" : "");
        }
        return this.aeropuertoDestino;
    }

    // Valida que el Paso 1 (Hoja de ruta, segmentos y horarios) esté completo
    public boolean isPaso1Completo() {
        return segmentos != null && !segmentos.isEmpty();
    }

    // Valida que el Paso 2 (Línea aérea, Comandante y Aeronave disponibles) esté completo
    public boolean isPaso2Completo() {
        if (!isPaso1Completo()) {
            return false;
        }
        return aerolineaId != null && !aerolineaId.trim().isEmpty() &&
                avionId != null && !avionId.trim().isEmpty() &&
                pilotoId != null && !pilotoId.trim().isEmpty() &&
                !pilotoEnConflicto && !avionEnConflicto;
    }

    // Valida que el Paso 3 (Tarifas configuradas) esté completo
    public boolean isPaso3Completo() {
        if (!isPaso2Completo()) {
            return false;
        }
        if (preciosAsientos == null || preciosAsientos.isEmpty()) {
            return false;
        }
        return preciosAsientos.stream().allMatch(p -> p.getPrecio() > 0);
    }


    private Integer obtenerIdVueloActual() {
        if (this.vuelo != null && this.vuelo.getIdVuelo() != null && this.vuelo.getIdVuelo() > 0) {
            return this.vuelo.getIdVuelo();
        }
        return this.idVueloParam;
    }

    public void actualizarDisponibilidadRecursos() {
        actualizarDisponibilidadRecursos(true);
    }

    public void actualizarDisponibilidadRecursos(boolean emitirMensajes) {
        this.pilotoEnConflicto = false;
        this.avionEnConflicto = false;

        if (segmentos == null || segmentos.isEmpty()) {
            this.listaPilotos = pilotoService.findAll();
            this.avions = avionService.findAll();
            filtrarPilotosModal();
            filtrarAvionesModal();
            return;
        }

        SegmentoVuelo primerSegmento = segmentos.get(0);
        SegmentoVuelo ultimoSegmento = segmentos.get(segmentos.size() - 1);

        if (primerSegmento.getHoraSalida() == null || ultimoSegmento.getHoraLlegada() == null) {
            return;
        }

        Timestamp salidaGlobal = new Timestamp(primerSegmento.getHoraSalida().getTime());
        Timestamp llegadaGlobal = new Timestamp(ultimoSegmento.getHoraLlegada().getTime());

        // FIX CENTRAL: Obtener ID asegurado para excluir el propio vuelo en las consultas de solapamiento
        Integer idVueloActual = obtenerIdVueloActual();

        // 1. Consultar la disponibilidad en BD ignorando las reservas del vuelo actual
        List<Piloto> pilotosLibres = pilotoRepository.findPilotosDisponiblesEnRango(salidaGlobal, llegadaGlobal, idVueloActual);
        List<Avion> avionesLibres = avionRepository.findAvionesDisponiblesEnRango(salidaGlobal, llegadaGlobal, idVueloActual);

        // 2. Verificar si el piloto actualmente seleccionado presenta conflicto de horario en el rango
        if (this.pilotoId != null && !this.pilotoId.trim().isEmpty()) {
            boolean pilotoDisponible = pilotosLibres.stream().anyMatch(p -> p.getRut().equalsIgnoreCase(this.pilotoId));

            if (!pilotoDisponible) {
                this.pilotoEnConflicto = true;
                if (emitirMensajes) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Conflicto de Horario - Piloto",
                            "El comandante seleccionado ya tiene otro vuelo en este rango horario. Seleccione un comandante disponible.");
                }
            }
        }

        // 3. Verificar si la aeronave actualmente seleccionada presenta conflicto de horario en el rango
        if (this.avionId != null && !this.avionId.trim().isEmpty()) {
            int idAvionSel = Integer.parseInt(this.avionId);
            boolean avionDisponible = avionesLibres.stream().anyMatch(a -> a.getIdAvion().equals(idAvionSel));

            if (!avionDisponible) {
                this.avionEnConflicto = true;
                if (emitirMensajes) {
                    addMessage(FacesMessage.SEVERITY_WARN, "Conflicto de Horario - Aeronave",
                            "La aeronave seleccionada ya está programada para otro vuelo en este horario. Seleccione una aeronave disponible.");
                }
            }
        }

        this.listaPilotos = pilotosLibres;
        this.avions = avionesLibres;
        filtrarPilotosModal();
        filtrarAvionesModal();
    }

    public String getRangoHorarioRutaTexto() {
        if (segmentos == null || segmentos.isEmpty()) {
            return "";
        }
        SegmentoVuelo primero = segmentos.get(0);
        SegmentoVuelo ultimo = segmentos.get(segmentos.size() - 1);
        if (primero.getHoraSalida() == null || ultimo.getHoraLlegada() == null) {
            return "";
        }
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return primero.getHoraSalida().toLocalDateTime().format(fmt) + " — " + ultimo.getHoraLlegada().toLocalDateTime().format(fmt);
    }

    // Método auxiliar para autocalcular la fecha de llegada lógica
    public void calcularLlegadaEstimada() {
        if (this.aeropuertoOrigen == null || this.aeropuertoDestino == null) {
            return;
        }

        // Si es un tramo subsiguiente y la fecha de salida está vacía, tomar la llegada anterior + 90 min
        if (this.fechaSalida == null && !segmentos.isEmpty()) {
            SegmentoVuelo ultimoTramo = segmentos.get(segmentos.size() - 1);
            if (ultimoTramo.getHoraLlegada() != null) {
                this.fechaSalida = ultimoTramo.getHoraLlegada().toLocalDateTime().plusMinutes(90);
            }
        }

        if (this.fechaSalida == null) {
            this.fechaSalida = LocalDateTime.now().plusHours(2).withSecond(0).withNano(0);
        }

        Aeropuerto origenObj = aeropuertos.get(this.aeropuertoOrigen);
        Aeropuerto destinoObj = aeropuertos.get(this.aeropuertoDestino);

        if (origenObj != null && destinoObj != null && origenObj.getPosicion() != null && destinoObj.getPosicion() != null) {
            double distanciaGrados = origenObj.getPosicion().distance(destinoObj.getPosicion());
            double distanciaKm = distanciaGrados * 111.0;

            // Tiempo neto a 800 km/h + 45 min de margen operacional (despegue/aterrizaje)
            double horasEstimadasMinimas = (distanciaKm / 800.0) + 0.75;
            long minutosMinimosEsperados = (long) (horasEstimadasMinimas * 60);

            // Se calcula automáticamente la llegada manteniendo el estándar real de vuelo
            this.fechaLlegada = this.fechaSalida.plusMinutes(minutosMinimosEsperados);
        }
    }


    // Método helper para obtener el texto de la puerta asignada tanto en memoria como en BD
    public String obtenerPuertaEmbarqueTexto(SegmentoVuelo seg) {
        if (seg == null) return "Por Asignar en Despacho";

        // 1. Buscar en las asignaciones temporales en memoria
        if (this.asignacionesPuertasTemp.containsKey(seg)) {
            PuertaEmbarque pe = this.asignacionesPuertasTemp.get(seg);
            if (pe != null) {
                String terminal = (pe.getTerminal() != null && !pe.getTerminal().isBlank())
                        ? " (Terminal " + pe.getTerminal() + ")"
                        : "";
                return "GATE " + pe.getCodigoPuerta() + terminal;
            }
        }

        // 2. Si ya viene persistido desde BD
        if (seg.getAsignacionPuertas() != null && !seg.getAsignacionPuertas().isEmpty()) {
            AsignacionPuerta ap = seg.getAsignacionPuertas().get(0);
            if (ap != null && ap.getPuertaEmbarque() != null) {
                PuertaEmbarque pe = ap.getPuertaEmbarque();
                String terminal = (pe.getTerminal() != null && !pe.getTerminal().isBlank())
                        ? " (Terminal " + pe.getTerminal() + ")"
                        : "";
                return "GATE " + pe.getCodigoPuerta() + terminal;
            }
        }

        return "Por Asignar en Despacho";
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

    public Integer getIdVueloParam() {
        return idVueloParam;
    }

    public void setIdVueloParam(Integer idVueloParam) {
        this.idVueloParam = idVueloParam;
    }

    public String getDetalleFisicoExplicacion() { return detalleFisicoExplicacion; }
    public void setDetalleFisicoExplicacion(String detalleFisicoExplicacion) { this.detalleFisicoExplicacion = detalleFisicoExplicacion; }
    public boolean isMostrarDetalleFisico() { return mostrarDetalleFisico; }
    public void setMostrarDetalleFisico(boolean mostrarDetalleFisico) { this.mostrarDetalleFisico = mostrarDetalleFisico; }

    public Avion getAvionSeleccionado() {
        if (this.avionSeleccionado == null && this.avionId != null && !this.avionId.isBlank()) {
            if (this.aviones != null && this.aviones.containsKey(this.avionId)) {
                this.avionSeleccionado = this.aviones.get(this.avionId);
            }
        }
        return this.avionSeleccionado;
    }
    public void setAvionSeleccionado(Avion avionSeleccionado) {
        this.avionSeleccionado = avionSeleccionado;
    }

    public List<Avion> getAvionesFiltradosModal() {
        if (this.avionesFiltradosModal == null && this.avions != null) {
            this.avionesFiltradosModal = new ArrayList<>(this.avions);
        }
        return this.avionesFiltradosModal;
    }
    public void setAvionesFiltradosModal(List<Avion> avionesFiltradosModal) {
        this.avionesFiltradosModal = avionesFiltradosModal;
    }

    public String getTextoBusquedaAvion() {
        return textoBusquedaAvion;
    }
    public void setTextoBusquedaAvion(String textoBusquedaAvion) {
        this.textoBusquedaAvion = textoBusquedaAvion;
    }

    public String getFiltroClasesAvion() {
        return filtroClasesAvion;
    }
    public void setFiltroClasesAvion(String filtroClasesAvion) {
        this.filtroClasesAvion = filtroClasesAvion;
    }

    public Piloto getPilotoSeleccionado() {
        if (this.pilotoSeleccionado == null && this.pilotoId != null && !this.pilotoId.isBlank()) {
            if (this.pilotos != null && this.pilotos.containsKey(this.pilotoId)) {
                this.pilotoSeleccionado = this.pilotos.get(this.pilotoId);
            }
        }
        return this.pilotoSeleccionado;
    }
    public void setPilotoSeleccionado(Piloto pilotoSeleccionado) {
        this.pilotoSeleccionado = pilotoSeleccionado;
    }

    public List<Piloto> getPilotosFiltradosModal() {
        if (this.pilotosFiltradosModal == null && this.listaPilotos != null) {
            this.pilotosFiltradosModal = new ArrayList<>(this.listaPilotos);
        }
        return this.pilotosFiltradosModal;
    }
    public void setPilotosFiltradosModal(List<Piloto> pilotosFiltradosModal) {
        this.pilotosFiltradosModal = pilotosFiltradosModal;
    }

    public String getTextoBusquedaPiloto() {
        return textoBusquedaPiloto;
    }
    public void setTextoBusquedaPiloto(String textoBusquedaPiloto) {
        this.textoBusquedaPiloto = textoBusquedaPiloto;
    }

    public boolean isPilotoEnConflicto() {
        return pilotoEnConflicto;
    }

    public boolean isAvionEnConflicto() {
        return avionEnConflicto;
    }

    public void toggleDetalleFisico() {
        this.mostrarDetalleFisico = !this.mostrarDetalleFisico;
    }
}