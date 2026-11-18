package com.SkyWay.views.Itinerario;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.CombinacionRutaSugeridaDTO;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.service.ItinerarioVueloService;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO;

@Component
@Named("itinerarioBean")
@ViewScoped
public class ItinerarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- SERVICIOS CORE ---
    private final ItinerarioService itinerarioService;
    private final CiudadService ciudadService;
    private final AeropuertoService aeropuertoService;
    private final VueloService vueloService;
    private final ItinerarioVueloService itinVueloService;
    private final TarifaService tarifaService;
    private final ItinerarioTarifaService itinerarioTarifaService;

    // --- ESTADO COMERCIAL Y LOGÍSTICO ---
    private String itinerarioArmadoMapaJson = "[]";
    private Itinerario itinerario = new Itinerario();
    private List<Itinerario> listaItinerarios;
    private List<Tarifa> tarifas;
    private List<Aeropuerto> aeropuertos;
    private List<Ciudad> ciudades;
    private List<Vuelo> vuelos;
    private List<ItinerarioVuelo> itinerariosAsignados;

    // --- MAPAS DE INDEXACIÓN RÁPIDA ---
    private Map<String, Aeropuerto> aeropuertosMap;
    private Map<Integer, BigDecimal> precioTarifas;

    // --- ENLACES DE ENTRADA DESDE LA VISTA ---
    private String aeropuertoOrigen;
    private String aeropuertoDestino;
    private String aeropuertoOrigenBusqueda;
    private String aeropuertoDestinoBusqueda;
    private LocalDate fechaBusqueda;
    private String aeropuertosMapaJson;
    private String vuelosMapaJson = "[]";

    private boolean esModificacion = false;
    private long reservasTotalesEdicion = 0L;
    private long reservasActivasEdicion = 0L;
    private boolean tramosBloqueadosPorReservas = false;

    // --- ESTRATEGIA DE PERMUTACIONES, PAGINACIÓN Y OCUPACIÓN ---
    private List<CombinacionRutaSugeridaDTO> combinacionesSugeridas = new ArrayList<>();
    private String criterioOrdenSugerencias = "OPORTUNIDAD";
    private int maxHorasEspera = 10;
    private Itinerario ultimoItinerarioPublicado;
    private Map<Integer, Long> asientosVendidosPorVuelo = new HashMap<>();
    private LazyDataModel<Itinerario> itinerariosLazy;
    private LazyDataModel<Vuelo> vuelosDisponiblesLazy;
    private long totalVuelosDisponibles = 0L;
    private boolean busquedaVuelosActiva = false;

    // --- CLAVE DE RESPALDO EN SESIÓN ---
    private static final String SESSION_DRAFT_KEY = "SKW_ITINERARIO_DRAFT_";

    @Autowired
    public ItinerarioBean(ItinerarioService itinerarioService, CiudadService ciudadService,
                          AeropuertoService aeropuertoService, VueloService vueloService,
                          ItinerarioVueloService itinVueloService, TarifaService tarifaService,
                          ItinerarioTarifaService itinerarioTarifaService) {
        this.itinerarioService = itinerarioService;
        this.ciudadService = ciudadService;
        this.aeropuertoService = aeropuertoService;
        this.vueloService = vueloService;
        this.itinVueloService = itinVueloService;
        this.tarifaService = tarifaService;
        this.itinerarioTarifaService = itinerarioTarifaService;
    }

    @PostConstruct
    public void init() {
        long inicioTotal = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Inicio de ItinerarioBean.init()");

        this.aeropuertosMap = new HashMap<>();
        this.precioTarifas = new HashMap<>();
        this.itinerariosAsignados = new ArrayList<>();
        this.vuelos = new ArrayList<>();

        // 1. Inicializar modelos paginados y catálogos livianos
        inicializarItinerariosLazy();
        inicializarVuelosDisponiblesLazy();
        this.tarifas = tarifaService.findAll();
        this.aeropuertos = aeropuertoService.findAll();
        if (this.aeropuertos != null) {
            this.aeropuertos.forEach(a -> aeropuertosMap.put(a.getCodigoIata(), a));
        }

        // 2. Mapeo JSON de aeropuertos para Leaflet
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.aeropuertosMapaJson = mapper.writeValueAsString(aeropuertoService.findAllParaMapa());
        } catch (Exception e) {
            Logger.logInfo("Error serializando aeropuertos para mapa: " + e.getMessage());
            this.aeropuertosMapaJson = "[]";
        }

        // 3. Evaluar contexto: ¿Edición por URL o Restauración por F5?
        boolean editando = verificarParametroEdicion();
        String contextoActual = obtenerContextoDraftActual();
        restaurarBorradorSesion(contextoActual, editando);

        Logger.logInfo(">>> [PERF-ITINERARIO] Tiempo TOTAL init(): " + (System.currentTimeMillis() - inicioTotal) + " ms");
    }

    // ==========================================
    // MANEJO DE BORRADOR DE SESIÓN (PERSISTENCIA F5)
    // ==========================================

    private String obtenerContextoDraftActual() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            String idParam = fc.getExternalContext().getRequestParameterMap().get("id");
            if (idParam != null && !idParam.isBlank()) {
                return "EDIT_" + idParam.trim();
            }
        }
        if (this.esModificacion && this.itinerario != null && this.itinerario.getIdItinerario() != null) {
            return "EDIT_" + this.itinerario.getIdItinerario();
        }
        return "NEW";
    }

    public void guardarBorradorSesion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
            sessionMap.put(SESSION_DRAFT_KEY + "context", obtenerContextoDraftActual());
            sessionMap.put(SESSION_DRAFT_KEY + "itinerario", this.itinerario);
            sessionMap.put(SESSION_DRAFT_KEY + "origen", this.aeropuertoOrigen);
            sessionMap.put(SESSION_DRAFT_KEY + "destino", this.aeropuertoDestino);
            sessionMap.put(SESSION_DRAFT_KEY + "tarifas",
                    this.precioTarifas != null ? new HashMap<>(this.precioTarifas) : new HashMap<>());
            sessionMap.put(SESSION_DRAFT_KEY + "asignados",
                    this.itinerariosAsignados != null ? new ArrayList<>(this.itinerariosAsignados) : new ArrayList<>());
            sessionMap.put(SESSION_DRAFT_KEY + "esModif", this.esModificacion);
            sessionMap.put(SESSION_DRAFT_KEY + "reservasTotales", this.reservasTotalesEdicion);
            sessionMap.put(SESSION_DRAFT_KEY + "reservasActivas", this.reservasActivasEdicion);
            sessionMap.put(SESSION_DRAFT_KEY + "tramosBloqueados", this.tramosBloqueadosPorReservas);
            sessionMap.put(SESSION_DRAFT_KEY + "maxHorasEspera", this.maxHorasEspera);
            sessionMap.put(SESSION_DRAFT_KEY + "criterioOrden", this.criterioOrdenSugerencias);
            sessionMap.put(SESSION_DRAFT_KEY + "origenBusq", this.aeropuertoOrigenBusqueda);
            sessionMap.put(SESSION_DRAFT_KEY + "destinoBusq", this.aeropuertoDestinoBusqueda);
            sessionMap.put(SESSION_DRAFT_KEY + "fechaBusq", this.fechaBusqueda);
            sessionMap.put(SESSION_DRAFT_KEY + "busquedaActiva", this.busquedaVuelosActiva);
            sessionMap.put(SESSION_DRAFT_KEY + "ultimoPublicado", this.ultimoItinerarioPublicado);
            Logger.logInfo("Borrador de itinerario respaldado en sesión HTTP.");
        }
    }

    public void restaurarBorradorSesion() {
        restaurarBorradorSesion(obtenerContextoDraftActual(), false);
    }

    @SuppressWarnings("unchecked")
    private void restaurarBorradorSesion(String contextoEsperado, boolean desdeEdicionUrl) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;

        Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
        Object ctxObj = sessionMap.get(SESSION_DRAFT_KEY + "context");
        if (ctxObj == null) {
            return;
        }
        String contextoGuardado = String.valueOf(ctxObj);
        if (!contextoEsperado.equals(contextoGuardado)) {
            return;
        }

        try {
            Object itinObj = sessionMap.get(SESSION_DRAFT_KEY + "itinerario");
            if (itinObj instanceof Itinerario) {
                this.itinerario = (Itinerario) itinObj;
            }
            if (!desdeEdicionUrl || !this.tramosBloqueadosPorReservas) {
                this.aeropuertoOrigen = (String) sessionMap.get(SESSION_DRAFT_KEY + "origen");
                this.aeropuertoDestino = (String) sessionMap.get(SESSION_DRAFT_KEY + "destino");
                List<ItinerarioVuelo> asignadosDraft = (List<ItinerarioVuelo>) sessionMap.get(SESSION_DRAFT_KEY + "asignados");
                if (asignadosDraft != null) {
                    this.itinerariosAsignados = new ArrayList<>(asignadosDraft);
                }
            }

            Map<Integer, BigDecimal> tarifasDraft = (Map<Integer, BigDecimal>) sessionMap.get(SESSION_DRAFT_KEY + "tarifas");
            if (tarifasDraft != null) {
                this.precioTarifas = new HashMap<>(tarifasDraft);
            }

            if (sessionMap.containsKey(SESSION_DRAFT_KEY + "esModif")) {
                this.esModificacion = Boolean.TRUE.equals(sessionMap.get(SESSION_DRAFT_KEY + "esModif"));
            }
            Object maxEsperaObj = sessionMap.get(SESSION_DRAFT_KEY + "maxHorasEspera");
            if (maxEsperaObj instanceof Integer) {
                this.maxHorasEspera = (Integer) maxEsperaObj;
            }
            Object ordenObj = sessionMap.get(SESSION_DRAFT_KEY + "criterioOrden");
            if (ordenObj instanceof String && !((String) ordenObj).isBlank()) {
                this.criterioOrdenSugerencias = (String) ordenObj;
            }
            this.aeropuertoOrigenBusqueda = (String) sessionMap.get(SESSION_DRAFT_KEY + "origenBusq");
            this.aeropuertoDestinoBusqueda = (String) sessionMap.get(SESSION_DRAFT_KEY + "destinoBusq");
            Object fechaObj = sessionMap.get(SESSION_DRAFT_KEY + "fechaBusq");
            this.fechaBusqueda = (fechaObj instanceof LocalDate) ? (LocalDate) fechaObj : null;
            this.busquedaVuelosActiva = Boolean.TRUE.equals(sessionMap.get(SESSION_DRAFT_KEY + "busquedaActiva"));

            Object pubObj = sessionMap.get(SESSION_DRAFT_KEY + "ultimoPublicado");
            this.ultimoItinerarioPublicado = (pubObj instanceof Itinerario) ? (Itinerario) pubObj : null;

            if (this.itinerario != null && this.itinerario.getAeropuertoOrigen() != null
                    && this.itinerario.getAeropuertoDestino() != null) {
                this.combinacionesSugeridas = itinerarioService.sugerirCombinacionesRuta(
                        this.aeropuertoOrigen,
                        this.aeropuertoDestino,
                        this.criterioOrdenSugerencias,
                        this.maxHorasEspera
                );
            }
            if (this.busquedaVuelosActiva) {
                this.totalVuelosDisponibles = itinerarioService.contarVuelosOperables(
                        this.aeropuertoOrigenBusqueda,
                        this.aeropuertoDestinoBusqueda,
                        this.fechaBusqueda
                );
            }
            actualizarItinerarioArmadoMapa();
            Logger.logInfo("Borrador de itinerario restaurado con éxito desde la sesión HTTP (" + contextoEsperado + ").");
        } catch (Exception e) {
            Logger.logInfo("No se pudo restaurar el borrador completo: " + e.getMessage());
        }
    }

    public void limpiarBorradorSesion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
            sessionMap.remove(SESSION_DRAFT_KEY + "context");
            sessionMap.remove(SESSION_DRAFT_KEY + "itinerario");
            sessionMap.remove(SESSION_DRAFT_KEY + "origen");
            sessionMap.remove(SESSION_DRAFT_KEY + "destino");
            sessionMap.remove(SESSION_DRAFT_KEY + "tarifas");
            sessionMap.remove(SESSION_DRAFT_KEY + "asignados");
            sessionMap.remove(SESSION_DRAFT_KEY + "esModif");
            sessionMap.remove(SESSION_DRAFT_KEY + "reservasTotales");
            sessionMap.remove(SESSION_DRAFT_KEY + "reservasActivas");
            sessionMap.remove(SESSION_DRAFT_KEY + "tramosBloqueados");
            sessionMap.remove(SESSION_DRAFT_KEY + "maxHorasEspera");
            sessionMap.remove(SESSION_DRAFT_KEY + "criterioOrden");
            sessionMap.remove(SESSION_DRAFT_KEY + "origenBusq");
            sessionMap.remove(SESSION_DRAFT_KEY + "destinoBusq");
            sessionMap.remove(SESSION_DRAFT_KEY + "fechaBusq");
            sessionMap.remove(SESSION_DRAFT_KEY + "busquedaActiva");
            sessionMap.remove(SESSION_DRAFT_KEY + "ultimoPublicado");
        }
    }

    // ==========================================
    // LÓGICA DE NEGOCIO Y PERSISTENCIA
    // ==========================================

    public void cargarListaItinerarios() {
        try {
            this.listaItinerarios = itinerarioService.findAll();
        } catch (Exception e) {
            this.listaItinerarios = new ArrayList<>();
            Logger.logInfo("Error al cargar la matriz de itinerarios: " + e.getMessage());
        }
    }

    private boolean verificarParametroEdicion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return false;

        String idParam = fc.getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idParam);
                Optional<Itinerario> itinOpt = Optional.ofNullable(itinerarioService.findById(id));
                if (itinOpt.isPresent()) {
                    this.itinerario = itinOpt.get();
                    this.aeropuertoOrigen = this.itinerario.getAeropuertoOrigen().getCodigoIata();
                    this.aeropuertoDestino = this.itinerario.getAeropuertoDestino().getCodigoIata();
                    this.esModificacion = true;

                    if (this.itinerario.getItinerarioVuelos() != null) {
                        this.itinerariosAsignados = new ArrayList<>(this.itinerario.getItinerarioVuelos());
                        this.itinerariosAsignados.sort(Comparator.comparingInt(ItinerarioVuelo::getOrden));
                    }

                    List<ItinerarioTarifa> tarifasAsociadas = itinerarioTarifaService.findByItinerario(id);
                    if (tarifasAsociadas != null) {
                        for (ItinerarioTarifa it : tarifasAsociadas) {
                            this.precioTarifas.put(it.getTarifa().getIdTarifa(), it.getPrecio());
                        }
                    }

                    this.reservasTotalesEdicion = itinerarioService.contarReservasTotalesPorItinerario(id);
                    this.reservasActivasEdicion = itinerarioService.contarReservasActivasPorItinerario(id);
                    this.tramosBloqueadosPorReservas = this.reservasTotalesEdicion > 0;
                    cargarCombinacionesSugeridas();
                    prepararBusquedaSiguienteTramo(false);

                    Logger.logInfo("Itinerario ID: " + id + " cargado para edición.");
                    actualizarItinerarioArmadoMapa();
                    return true;
                }
            } catch (NumberFormatException e) {
                Logger.logInfo("Formato de ID inválido en la URL de gestión.");
            }
        }
        return false;
    }

    public void saveItinerario() {
        if (this.tramosBloqueadosPorReservas) {
            addMessage(FacesMessage.SEVERITY_WARN, "Ruta Protegida",
                    "Este itinerario ya registra " + this.reservasTotalesEdicion + " reserva(s). No se puede alterar su origen ni destino.");
            return;
        }
        if (this.aeropuertoOrigen == null || this.aeropuertoDestino == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe especificar un origen y un destino comercial.");
            return;
        }
        if (this.aeropuertoOrigen.equalsIgnoreCase(this.aeropuertoDestino)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "El aeropuerto de origen y destino no pueden ser el mismo.");
            return;
        }

        Aeropuerto origen = aeropuertosMap.get(this.aeropuertoOrigen);
        Aeropuerto destino = aeropuertosMap.get(this.aeropuertoDestino);

        if (!esModificacion) {
            this.itinerario.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        }

        this.itinerario.setAeropuertoOrigen(origen);
        this.itinerario.setAeropuertoDestino(destino);
        this.ultimoItinerarioPublicado = null;

        cargarCombinacionesSugeridas();
        prepararBusquedaSiguienteTramo(true);
        actualizarItinerarioArmadoMapa();
        guardarBorradorSesion();
        Logger.logInfo("Ruta maestra fijada: " + origen.getCodigoIata() + " -> " + destino.getCodigoIata());
        addMessage(FacesMessage.SEVERITY_INFO, "Ruta Fijada",
                "Ruta " + origen.getCodigoIata() + " → " + destino.getCodigoIata() + " fijada ("
                        + this.combinacionesSugeridas.size() + " combinación(es) sugerida(s)).");
    }

    public void invertirRutaMaestra() {
        if (this.tramosBloqueadosPorReservas) {
            addMessage(FacesMessage.SEVERITY_WARN, "Ruta Protegida", "No se puede invertir una ruta que ya tiene reservas asociadas.");
            return;
        }
        String temp = this.aeropuertoOrigen;
        this.aeropuertoOrigen = this.aeropuertoDestino;
        this.aeropuertoDestino = temp;
        guardarBorradorSesion();
    }

    public void guardarTodasLasTarifas() {
        if (this.tarifas == null || this.tarifas.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "No hay catálogo de tarifas cargado.");
            return;
        }

        BigDecimal montoMinimoComercial = new BigDecimal("10000");
        int asignadas = 0;

        for (Tarifa tarifa : this.tarifas) {
            Object rawValue = this.precioTarifas.get(tarifa.getIdTarifa());
            BigDecimal monto = parseToBigDecimal(rawValue);

            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Tarifa Incompleta",
                        String.format("Debe ingresar un monto válido para la categoría '%s'.", tarifa.getNombre()));
                return;
            }

            if (monto.compareTo(montoMinimoComercial) < 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Valor Mínimo No Alcanzado",
                        String.format("La tarifa '%s' ($%s) no puede ser inferior al mínimo comercial de $10.000 CLP.",
                                tarifa.getNombre(), monto.toPlainString()));
                return;
            }

            this.precioTarifas.put(tarifa.getIdTarifa(), monto);
            asignadas++;
        }

        if (asignadas < this.tarifas.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Estrategia Comercial Incompleta",
                    String.format("Ha configurado %d de %d tarifas. Debe ingresar un valor para todas las categorías antes de continuar.",
                            asignadas, this.tarifas.size()));
            return;
        }

        String errorJerarquia = validarCoherenciaJerarquica();
        if (errorJerarquia != null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Incoherencia Comercial de Tarifas", errorJerarquia);
            return;
        }

        actualizarItinerarioArmadoMapa();
        guardarBorradorSesion();
        Logger.logInfo("Precios de tarifas consolidados y validados correctamente.");
        addMessage(FacesMessage.SEVERITY_INFO, "Configuración Guardada", "Tarifas confirmadas correctamente.");
    }

    private BigDecimal parseToBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            String strVal = value.toString().replaceAll("[^0-9.]", "").trim();
            return strVal.isEmpty() ? null : new BigDecimal(strVal);
        } catch (Exception e) {
            return null;
        }
    }

    private String validarCoherenciaJerarquica() {
        BigDecimal precioBasica = null;
        BigDecimal precioIntermedia = null;
        BigDecimal precioPremium = null;

        String nombreBasica = "", nombreIntermedia = "", nombrePremium = "";

        for (Tarifa t : this.tarifas) {
            String nombreLower = t.getNombre().toLowerCase().trim();
            BigDecimal precio = parseToBigDecimal(this.precioTarifas.get(t.getIdTarifa()));

            if (nombreLower.contains("basic") || nombreLower.contains("básic") || nombreLower.contains("promo") || nombreLower.contains("light")) {
                precioBasica = precio;
                nombreBasica = t.getNombre();
            } else if (nombreLower.contains("flex") || nombreLower.contains("med") || nombreLower.contains("standar") || nombreLower.contains("estándar")) {
                precioIntermedia = precio;
                nombreIntermedia = t.getNombre();
            } else if (nombreLower.contains("premium") || nombreLower.contains("execut") || nombreLower.contains("ejecut") || nombreLower.contains("top") || nombreLower.contains("first")) {
                precioPremium = precio;
                nombrePremium = t.getNombre();
            }
        }

        if (precioBasica != null && precioPremium != null && precioBasica.compareTo(precioPremium) >= 0) {
            return String.format("La tarifa '%s' ($%s) no puede ser mayor o igual a la tarifa '%s' ($%s).",
                    nombreBasica, precioBasica.toPlainString(), nombrePremium, precioPremium.toPlainString());
        }

        if (precioBasica != null && precioIntermedia != null && precioBasica.compareTo(precioIntermedia) >= 0) {
            return String.format("La tarifa '%s' ($%s) no puede ser mayor o igual a la tarifa '%s' ($%s).",
                    nombreBasica, precioBasica.toPlainString(), nombreIntermedia, precioIntermedia.toPlainString());
        }

        if (precioIntermedia != null && precioPremium != null && precioIntermedia.compareTo(precioPremium) >= 0) {
            return String.format("La tarifa '%s' ($%s) no puede ser mayor o igual a la tarifa '%s' ($%s).",
                    nombreIntermedia, precioIntermedia.toPlainString(), nombrePremium, precioPremium.toPlainString());
        }

        return null;
    }

    public void agregarVuelo(Vuelo vuelo) {
        if (vuelo == null) return;
        if (this.tramosBloqueadosPorReservas) {
            addMessage(FacesMessage.SEVERITY_WARN, "Tramos Protegidos",
                    "Este itinerario ya registra reservas vinculadas. No se pueden agregar tramos.");
            return;
        }

        String motivo = getMotivoIncompatibilidad(vuelo);
        if (motivo != null) {
            addMessage(FacesMessage.SEVERITY_WARN, "No se puede vincular", motivo);
            return;
        }

        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(this.itinerario);
        iv.setVuelo(vuelo);
        iv.setOrden(this.itinerariosAsignados.size() + 1);

        this.itinerariosAsignados.add(iv);
        this.ultimoItinerarioPublicado = null;
        actualizarItinerarioArmadoMapa();
        if (!"OK".equals(getResumenSecuencia())) {
            prepararBusquedaSiguienteTramo(true);
        }
        guardarBorradorSesion();

        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Vinculado",
                String.format("Vuelo %s agregado en posición #%d.", vuelo.getNumeroVuelo(), iv.getOrden()));
    }

    public void removerVuelo(ItinerarioVuelo iv) {
        if (this.tramosBloqueadosPorReservas) {
            addMessage(FacesMessage.SEVERITY_WARN, "Tramos Protegidos",
                    "Este itinerario ya registra reservas vinculadas. No se pueden quitar tramos.");
            return;
        }
        this.itinerariosAsignados.remove(iv);
        this.ultimoItinerarioPublicado = null;
        for (int i = 0; i < this.itinerariosAsignados.size(); i++) {
            this.itinerariosAsignados.get(i).setOrden(i + 1);
        }
        actualizarItinerarioArmadoMapa();
        prepararBusquedaSiguienteTramo(true);
        guardarBorradorSesion();
        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Removido", "Se actualizó la secuencia operativa.");
    }

    public void guardarItinerario() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (this.itinerario.getAeropuertoOrigen() == null || this.itinerario.getAeropuertoDestino() == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Falta Ruta Principal", "Define primero el aeropuerto de origen y destino (Paso 1).");
            if (fc != null) fc.validationFailed();
            return;
        }
        if (this.itinerario.getPrecioBase() == null || this.itinerario.getPrecioBase() <= 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Falta Precio Base", "Fija un Precio Base Referencial válido en el Paso 1.");
            if (fc != null) fc.validationFailed();
            return;
        }
        if (this.precioTarifas.isEmpty() || this.precioTarifas.size() < this.tarifas.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Faltan Tarifas", "Confirma los precios de todas las tarifas (Paso 2).");
            if (fc != null) fc.validationFailed();
            return;
        }
        if (this.itinerariosAsignados.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Sin Vuelos", "Selecciona al menos un vuelo para el itinerario (Paso 3).");
            if (fc != null) fc.validationFailed();
            return;
        }

        String resumenSecuencia = getResumenSecuencia();
        if (!"OK".equals(resumenSecuencia)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Ruta Incompleta", resumenSecuencia);
            if (fc != null) fc.validationFailed();
            return;
        }

        try {
            Itinerario guardado = itinerarioService.guardarItinerarioTransaccional(
                    this.itinerario,
                    this.itinerariosAsignados,
                    this.precioTarifas,
                    this.esModificacion,
                    this.maxHorasEspera
            );

            this.itinerario = guardado;
            this.ultimoItinerarioPublicado = guardado;

            String msgExito = esModificacion
                    ? "El itinerario SKW-ITI-" + guardado.getIdItinerario() + " fue actualizado correctamente."
                    : "Itinerario SKW-ITI-" + guardado.getIdItinerario() + " publicado. Los datos se mantienen en pantalla por si deseas seguir trabajando.";
            addMessage(FacesMessage.SEVERITY_INFO, "Itinerario Guardado", msgExito);

            guardarBorradorSesion();
            actualizarItinerarioArmadoMapa();
        } catch (Exception e) {
            Logger.logInfo("Error de persistencia en guardarItinerario: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "No se pudo guardar", e.getMessage());
            if (fc != null) fc.validationFailed();
        }
    }

    public void crearOtroMismaRuta() {
        Aeropuerto orig = aeropuertosMap != null ? aeropuertosMap.get(this.aeropuertoOrigen) : null;
        Aeropuerto dest = aeropuertosMap != null ? aeropuertosMap.get(this.aeropuertoDestino) : null;
        Integer precioBaseActual = this.itinerario != null ? this.itinerario.getPrecioBase() : null;
        this.ultimoItinerarioPublicado = null;
        this.esModificacion = false;
        this.reservasTotalesEdicion = 0L;
        this.reservasActivasEdicion = 0L;
        this.tramosBloqueadosPorReservas = false;
        this.itinerariosAsignados.clear();
        this.itinerario = new Itinerario();
        this.itinerario.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        this.itinerario.setAeropuertoOrigen(orig);
        this.itinerario.setAeropuertoDestino(dest);
        this.itinerario.setPrecioBase(precioBaseActual);

        cargarCombinacionesSugeridas();
        prepararBusquedaSiguienteTramo(true);
        actualizarItinerarioArmadoMapa();
        guardarBorradorSesion();
        addMessage(FacesMessage.SEVERITY_INFO, "Listo para nuevo itinerario",
                "Se conservaron la ruta " + this.aeropuertoOrigen + " → " + this.aeropuertoDestino + " y las tarifas. Elige nuevos vuelos.");
    }

    public void continuarEditandoPublicado() {
        if (this.ultimoItinerarioPublicado == null) {
            return;
        }
        this.itinerario = this.ultimoItinerarioPublicado;
        this.esModificacion = true;
        this.ultimoItinerarioPublicado = null;
        if (this.itinerario.getIdItinerario() != null) {
            this.reservasTotalesEdicion = itinerarioService.contarReservasTotalesPorItinerario(this.itinerario.getIdItinerario());
            this.reservasActivasEdicion = itinerarioService.contarReservasActivasPorItinerario(this.itinerario.getIdItinerario());
            this.tramosBloqueadosPorReservas = this.reservasTotalesEdicion > 0;
        }
        guardarBorradorSesion();
        actualizarItinerarioArmadoMapa();
    }

    public void iniciarNuevaRutaDesdeCero() {
        resetForm();
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().executeScript("resetearMapaRutaVisual(); renderItinerarioArmado('[]');");
        }
        addMessage(FacesMessage.SEVERITY_INFO, "Formulario limpio",
                "Se limpiaron todos los campos y el borrador en sesión. Puedes comenzar desde cero.");
    }

    public void eliminarItinerario(Itinerario itin) {
        if (itin == null || itin.getIdItinerario() == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Itinerario inválido.");
            return;
        }
        try {
            itinerarioService.eliminarItinerarioControlado(itin.getIdItinerario());
            addMessage(FacesMessage.SEVERITY_INFO, "Itinerario Eliminado",
                    "Se eliminó el itinerario SKW-ITI-" + itin.getIdItinerario() + " (sin reservas vinculadas).");
        } catch (Exception e) {
            Logger.logInfo("Bloqueo al eliminar itinerario #" + itin.getIdItinerario() + ": " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_WARN, "Operación Bloqueada", e.getMessage());
        }
    }

    public void resetForm() {
        this.itinerario = new Itinerario();
        this.itinerariosAsignados.clear();
        this.precioTarifas.clear();
        this.vuelos.clear();
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        this.aeropuertoOrigenBusqueda = null;
        this.aeropuertoDestinoBusqueda = null;
        this.fechaBusqueda = null;
        this.maxHorasEspera = 10;
        this.criterioOrdenSugerencias = "OPORTUNIDAD";
        this.esModificacion = false;
        this.ultimoItinerarioPublicado = null;
        this.reservasTotalesEdicion = 0L;
        this.reservasActivasEdicion = 0L;
        this.tramosBloqueadosPorReservas = false;
        this.combinacionesSugeridas = new ArrayList<>();
        this.asientosVendidosPorVuelo = new HashMap<>();
        this.totalVuelosDisponibles = 0L;
        this.busquedaVuelosActiva = false;
        this.itinerarioArmadoMapaJson = "[]";
        limpiarBorradorSesion();
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y ACCESORES
    // ==========================================

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

    public void seleccionarOrigenMapa() {
        if (this.tramosBloqueadosPorReservas) return;
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;
        String iata = fc.getExternalContext().getRequestParameterMap().get("iata");

        if (iata != null && aeropuertosMap.containsKey(iata) && !iata.equals(this.aeropuertoDestino)) {
            this.aeropuertoOrigen = iata;
            guardarBorradorSesion();
        }
    }

    public void seleccionarDestinoMapa() {
        if (this.tramosBloqueadosPorReservas) return;
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;
        String iata = fc.getExternalContext().getRequestParameterMap().get("iata");

        if (iata != null && aeropuertosMap.containsKey(iata) && !iata.equals(this.aeropuertoOrigen)) {
            this.aeropuertoDestino = iata;
            guardarBorradorSesion();
        }
    }

    public void reiniciarSeleccionRutaMaestra() {
        if (this.tramosBloqueadosPorReservas) return;
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        guardarBorradorSesion();
    }

    public void buscarVuelos() {
        this.busquedaVuelosActiva = true;
        this.totalVuelosDisponibles = itinerarioService.contarVuelosOperables(
                this.aeropuertoOrigenBusqueda,
                this.aeropuertoDestinoBusqueda,
                this.fechaBusqueda
        );
        guardarBorradorSesion();
        if (PrimeFaces.current().isAjaxRequest()) {
            PrimeFaces.current().executeScript("if (PF('wvTablaVuelosPaso3')) { PF('wvTablaVuelosPaso3').getPaginator().setPage(0); }");
        }
    }

    public void verTodasFechasSiguienteTramo() {
        this.fechaBusqueda = null;
        if (this.aeropuertoOrigenBusqueda == null) {
            this.aeropuertoOrigenBusqueda = getSiguienteOrigenEsperado();
        }
        buscarVuelos();
    }

    public void cargarCombinacionesSugeridas() {
        if (this.aeropuertoOrigen == null || this.aeropuertoDestino == null) {
            this.combinacionesSugeridas = new ArrayList<>();
            return;
        }
        this.combinacionesSugeridas = itinerarioService.sugerirCombinacionesRuta(
                this.aeropuertoOrigen,
                this.aeropuertoDestino,
                this.criterioOrdenSugerencias,
                this.maxHorasEspera
        );
        guardarBorradorSesion();
    }

    private void inicializarItinerariosLazy() {
        this.itinerariosLazy = new LazyDataModel<>() {
            private static final long serialVersionUID = 1L;
            private List<Itinerario> paginaActual = new ArrayList<>();

            @Override
            public String getRowKey(Itinerario it) {
                return it != null && it.getIdItinerario() != null ? String.valueOf(it.getIdItinerario()) : null;
            }

            @Override
            public Itinerario getRowData(String rowKey) {
                if (rowKey == null || paginaActual == null) return null;
                return paginaActual.stream()
                        .filter(i -> String.valueOf(i.getIdItinerario()).equals(rowKey))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                return (int) itinerarioService.contarItinerariosPaginados(extraerFiltroGlobal(filterBy));
            }

            @Override
            public List<Itinerario> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                String global = extraerFiltroGlobal(filterBy);
                String sortField = null;
                boolean asc = false;
                if (sortBy != null && !sortBy.isEmpty()) {
                    SortMeta meta = sortBy.values().iterator().next();
                    sortField = meta.getField();
                    asc = meta.getOrder() == SortOrder.ASCENDING;
                }
                this.paginaActual = itinerarioService.buscarItinerariosPaginados(first, pageSize, global, sortField, asc);
                return this.paginaActual;
            }

            private String extraerFiltroGlobal(Map<String, FilterMeta> filterBy) {
                if (filterBy == null) return null;
                FilterMeta gf = filterBy.get("globalFilter");
                return gf != null && gf.getFilterValue() != null ? gf.getFilterValue().toString() : null;
            }
        };
    }

    private void inicializarVuelosDisponiblesLazy() {
        this.vuelosDisponiblesLazy = new LazyDataModel<>() {
            private static final long serialVersionUID = 1L;
            private List<Vuelo> paginaActual = new ArrayList<>();

            @Override
            public String getRowKey(Vuelo v) {
                return v != null && v.getIdVuelo() != null ? String.valueOf(v.getIdVuelo()) : null;
            }

            @Override
            public Vuelo getRowData(String rowKey) {
                if (rowKey == null || paginaActual == null) return null;
                return paginaActual.stream()
                        .filter(v -> String.valueOf(v.getIdVuelo()).equals(rowKey))
                        .findFirst()
                        .orElse(null);
            }

            @Override
            public int count(Map<String, FilterMeta> filterBy) {
                if (!busquedaVuelosActiva) {
                    totalVuelosDisponibles = 0L;
                    return 0;
                }
                long count = itinerarioService.contarVuelosOperables(
                        aeropuertoOrigenBusqueda,
                        aeropuertoDestinoBusqueda,
                        fechaBusqueda
                );
                totalVuelosDisponibles = count;
                return (int) count;
            }

            @Override
            public List<Vuelo> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
                if (!busquedaVuelosActiva) {
                    this.paginaActual = new ArrayList<>();
                    return this.paginaActual;
                }
                List<Vuelo> encontrados = itinerarioService.buscarVuelosOperablesPaginados(
                        aeropuertoOrigenBusqueda,
                        aeropuertoDestinoBusqueda,
                        fechaBusqueda,
                        first,
                        pageSize
                );
                List<Integer> idsPagina = encontrados.stream()
                        .map(Vuelo::getIdVuelo)
                        .filter(Objects::nonNull)
                        .toList();
                asientosVendidosPorVuelo = itinerarioService.obtenerMapaAsientosVendidosPorVuelos(idsPagina);
                encontrados.sort(Comparator.comparing((Vuelo v) -> !isVueloCompatibleParaVincular(v)));
                this.paginaActual = encontrados;
                return this.paginaActual;
            }
        };
    }

    public void aplicarCombinacionSugerida(CombinacionRutaSugeridaDTO combinacion) {
        if (combinacion == null || combinacion.getVuelos() == null || combinacion.getVuelos().isEmpty()) {
            return;
        }
        if (this.tramosBloqueadosPorReservas) {
            addMessage(FacesMessage.SEVERITY_WARN, "Tramos Protegidos",
                    "Este itinerario ya registra reservas vinculadas. No se pueden modificar sus tramos.");
            return;
        }

        this.itinerariosAsignados.clear();
        this.ultimoItinerarioPublicado = null;
        int orden = 1;
        for (Vuelo v : combinacion.getVuelos()) {
            ItinerarioVuelo iv = new ItinerarioVuelo();
            iv.setItinerario(this.itinerario);
            iv.setVuelo(v);
            iv.setOrden(orden++);
            this.itinerariosAsignados.add(iv);
        }

        actualizarItinerarioArmadoMapa();
        guardarBorradorSesion();
        addMessage(FacesMessage.SEVERITY_INFO, "Combinación Cargada",
                "Se cargó la estrategia " + combinacion.getSecuenciaCodigos()
                        + " (" + combinacion.getOcupacionPromedioPct() + "% ocupación media).");
    }

    public int getAsientosVendidosVuelo(Vuelo v) {
        if (v == null || v.getIdVuelo() == null || this.asientosVendidosPorVuelo == null) {
            return 0;
        }
        return this.asientosVendidosPorVuelo.getOrDefault(v.getIdVuelo(), 0L).intValue();
    }

    public int getCapacidadVuelo(Vuelo v) {
        if (v == null || v.getAvion() == null || v.getAvion().getCapacidadDePasajeros() == null) {
            return 0;
        }
        return v.getAvion().getCapacidadDePasajeros();
    }

    public int getAsientosLibresVuelo(Vuelo v) {
        int cap = getCapacidadVuelo(v);
        return Math.max(0, cap - getAsientosVendidosVuelo(v));
    }

    public int getOcupacionPctVuelo(Vuelo v) {
        int cap = getCapacidadVuelo(v);
        if (cap <= 0) return 0;
        return Math.min(100, (int) Math.round((getAsientosVendidosVuelo(v) * 100.0) / cap));
    }

    public void limpiarFiltros() {
        this.aeropuertoOrigenBusqueda = null;
        this.aeropuertoDestinoBusqueda = null;
        this.fechaBusqueda = null;
        this.vuelos = new ArrayList<>();
        this.totalVuelosDisponibles = 0L;
        this.busquedaVuelosActiva = false;
        guardarBorradorSesion();
    }

    public void sugerirSiguienteTramo() {
        prepararBusquedaSiguienteTramo(true);
    }

    private void prepararBusquedaSiguienteTramo(boolean ejecutarBusqueda) {
        if (this.itinerariosAsignados == null || this.itinerariosAsignados.isEmpty()) {
            this.aeropuertoOrigenBusqueda = this.aeropuertoOrigen;
            this.aeropuertoDestinoBusqueda = null;
            this.fechaBusqueda = null;
        } else {
            Vuelo ultimo = this.itinerariosAsignados.get(this.itinerariosAsignados.size() - 1).getVuelo();
            String destinoUltimo = obtenerIataDestinoVuelo(ultimo);
            this.aeropuertoOrigenBusqueda = destinoUltimo;
            this.aeropuertoDestinoBusqueda = null;
            this.fechaBusqueda = (ultimo != null && ultimo.getFechaHoraLlegada() != null)
                    ? ultimo.getFechaHoraLlegada().toLocalDateTime().toLocalDate()
                    : null;
        }
        if (ejecutarBusqueda && this.aeropuertoOrigenBusqueda != null) {
            buscarVuelos();
        }
    }

    public String getSiguienteOrigenEsperado() {
        if ("OK".equals(getResumenSecuencia())) {
            return null;
        }
        if (this.itinerariosAsignados == null || this.itinerariosAsignados.isEmpty()) {
            return this.aeropuertoOrigen;
        }
        Vuelo ultimo = this.itinerariosAsignados.get(this.itinerariosAsignados.size() - 1).getVuelo();
        return obtenerIataDestinoVuelo(ultimo);
    }

    public boolean isVueloCompatibleParaVincular(Vuelo vuelo) {
        return getMotivoIncompatibilidad(vuelo) == null;
    }

    public String getMotivoIncompatibilidad(Vuelo vuelo) {
        if (vuelo == null) {
            return "Vuelo inválido";
        }
        if (estaVinculado(vuelo)) {
            return "Ya seleccionado";
        }
        if ("OK".equals(getResumenSecuencia())) {
            return "Ruta ya completa";
        }
        if (!esVueloOperable(vuelo)) {
            String est = vuelo.getEstadoVuelo() != null ? vuelo.getEstadoVuelo().getDescripcion() : "No disponible";
            return "Estado: " + est;
        }
        String origenVuelo = obtenerIataOrigenVuelo(vuelo);
        if (this.itinerariosAsignados == null || this.itinerariosAsignados.isEmpty()) {
            if (this.aeropuertoOrigen != null && origenVuelo != null
                    && !this.aeropuertoOrigen.equalsIgnoreCase(origenVuelo)) {
                return "Debe salir de " + this.aeropuertoOrigen;
            }
            return null;
        }
        Vuelo ultimo = this.itinerariosAsignados.get(this.itinerariosAsignados.size() - 1).getVuelo();
        String destinoUltimo = obtenerIataDestinoVuelo(ultimo);
        if (destinoUltimo != null && origenVuelo != null && !destinoUltimo.equalsIgnoreCase(origenVuelo)) {
            return "Requiere origen " + destinoUltimo;
        }
        if (!esFechaCorrecta(vuelo)) {
            return "Sale antes de llegada previa";
        }
        long minutosEspera = calcularMinutosEntre(ultimo.getFechaHoraLlegada(), vuelo.getFechaHoraSalida());
        if (minutosEspera < 30) {
            return "Escala mínima < 30 min";
        }
        long maxMin = Math.max(1, this.maxHorasEspera) * 60L;
        if (minutosEspera > maxMin) {
            return "Espera > " + this.maxHorasEspera + "h (" + formatearMinutos(minutosEspera) + ")";
        }
        return null;
    }

    public String getTextoConexionSugerida(Vuelo vuelo) {
        if (vuelo == null || !isVueloCompatibleParaVincular(vuelo)) {
            return "";
        }
        String destinoVuelo = obtenerIataDestinoVuelo(vuelo);
        boolean cierraRuta = this.aeropuertoDestino != null && this.aeropuertoDestino.equalsIgnoreCase(destinoVuelo);
        if (this.itinerariosAsignados == null || this.itinerariosAsignados.isEmpty()) {
            return cierraRuta ? "Directo a destino final" : "Primer tramo hacia " + (destinoVuelo != null ? destinoVuelo : "-");
        }
        Vuelo ultimo = this.itinerariosAsignados.get(this.itinerariosAsignados.size() - 1).getVuelo();
        long mins = calcularMinutosEntre(ultimo.getFechaHoraLlegada(), vuelo.getFechaHoraSalida());
        String espera = formatearMinutos(mins);
        return cierraRuta ? "Completa ruta · Espera " + espera : "Conecta · Espera " + espera;
    }

    public String getTiempoEsperaEntreTramos(int index) {
        if (this.itinerariosAsignados == null || index < 0 || index + 1 >= this.itinerariosAsignados.size()) {
            return "";
        }
        Vuelo actual = this.itinerariosAsignados.get(index).getVuelo();
        Vuelo siguiente = this.itinerariosAsignados.get(index + 1).getVuelo();
        if (actual == null || siguiente == null) {
            return "";
        }
        long mins = calcularMinutosEntre(actual.getFechaHoraLlegada(), siguiente.getFechaHoraSalida());
        return formatearMinutos(mins);
    }

    public String getNivelAlertaEspera(int index) {
        if (this.itinerariosAsignados == null || index < 0 || index + 1 >= this.itinerariosAsignados.size()) {
            return "normal";
        }
        Vuelo actual = this.itinerariosAsignados.get(index).getVuelo();
        Vuelo siguiente = this.itinerariosAsignados.get(index + 1).getVuelo();
        if (actual == null || siguiente == null) {
            return "normal";
        }
        long mins = calcularMinutosEntre(actual.getFechaHoraLlegada(), siguiente.getFechaHoraSalida());
        if (mins < 45) return "corta";
        if (mins > Math.max(1, this.maxHorasEspera) * 60L || mins > 480) return "larga";
        return "normal";
    }

    public String getDuracionTotalArmada() {
        if (this.itinerariosAsignados == null || this.itinerariosAsignados.isEmpty()) {
            return "-";
        }
        Vuelo primero = this.itinerariosAsignados.get(0).getVuelo();
        Vuelo ultimo = this.itinerariosAsignados.get(this.itinerariosAsignados.size() - 1).getVuelo();
        if (primero == null || ultimo == null || primero.getFechaHoraSalida() == null || ultimo.getFechaHoraLlegada() == null) {
            return "-";
        }
        return formatearMinutos(calcularMinutosEntre(primero.getFechaHoraSalida(), ultimo.getFechaHoraLlegada()));
    }

    private boolean esVueloOperable(Vuelo vuelo) {
        if (vuelo == null || vuelo.getEstadoVuelo() == null) {
            return true;
        }
        String desc = vuelo.getEstadoVuelo().getDescripcion() != null ? vuelo.getEstadoVuelo().getDescripcion().toUpperCase() : "";
        String flag = vuelo.getEstadoVuelo().getEstado() != null ? vuelo.getEstadoVuelo().getEstado().toUpperCase() : "";
        return !desc.contains("CANCEL") && !flag.contains("CANCEL") && !"INACTIVO".equals(flag)
                && !desc.contains("EN VUELO") && !desc.contains("ATERRIZ") && !desc.contains("FINALIZ")
                && !"FINALIZADO".equals(flag);
    }

    private String obtenerIataOrigenVuelo(Vuelo vuelo) {
        if (vuelo == null || vuelo.getSegmentoVuelos() == null || vuelo.getSegmentoVuelos().isEmpty()) {
            return null;
        }
        var orig = vuelo.getSegmentoVuelos().get(0).getAeropuertoOrigen();
        return orig != null ? orig.getCodigoIata() : null;
    }

    private String obtenerIataDestinoVuelo(Vuelo vuelo) {
        if (vuelo == null || vuelo.getSegmentoVuelos() == null || vuelo.getSegmentoVuelos().isEmpty()) {
            return null;
        }
        var dest = vuelo.getSegmentoVuelos().get(vuelo.getSegmentoVuelos().size() - 1).getAeropuertoDestino();
        return dest != null ? dest.getCodigoIata() : null;
    }

    private long calcularMinutosEntre(Timestamp inicio, Timestamp fin) {
        if (inicio == null || fin == null) return 0L;
        return Duration.between(inicio.toInstant(), fin.toInstant()).toMinutes();
    }

    private String formatearMinutos(long mins) {
        if (mins <= 0) return "0m";
        long horas = mins / 60;
        long resto = mins % 60;
        if (horas == 0) return resto + "m";
        if (resto == 0) return horas + "h";
        return horas + "h " + resto + "m";
    }

    public boolean estaVinculado(Vuelo vuelo) {
        if (vuelo == null) return false;
        return itinerariosAsignados.stream().anyMatch(iv -> iv.getVuelo().getIdVuelo().equals(vuelo.getIdVuelo()));
    }

    public boolean esFechaCorrecta(Vuelo vuelo) {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) return true;
        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        return !vuelo.getFechaHoraSalida().before(ultimoVuelo.getFechaHoraLlegada());
    }

    public boolean ultimoAeropuertoCorrecto(Vuelo vuelo) {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) return true;
        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        var aeropuertoDestinoUltimo = ultimoVuelo.getSegmentoVuelos()
                .get(ultimoVuelo.getSegmentoVuelos().size() - 1)
                .getAeropuertoDestino().getIdAeropuerto();

        var aeropuertoOrigenNuevo = vuelo.getSegmentoVuelos().get(0).getAeropuertoOrigen().getIdAeropuerto();
        return aeropuertoDestinoUltimo.equals(aeropuertoOrigenNuevo);
    }

    public String getResumenSecuencia() {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) {
            return "El itinerario no tiene vuelos asignados.";
        }

        String origenPrimero = itinerariosAsignados.get(0).getVuelo()
                .getSegmentoVuelos().get(0).getAeropuertoOrigen().getCodigoIata();

        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        String destinoUltimo = ultimoVuelo.getSegmentoVuelos()
                .get(ultimoVuelo.getSegmentoVuelos().size() - 1)
                .getAeropuertoDestino().getCodigoIata();

        boolean coincideOrigen = origenPrimero.equalsIgnoreCase(this.aeropuertoOrigen);
        boolean coincideDestino = destinoUltimo.equalsIgnoreCase(this.aeropuertoDestino);

        if (coincideOrigen && coincideDestino) return "OK";

        if (!coincideOrigen && !coincideDestino) {
            return String.format("Incoherencia total: Inicia en %s y termina en %s (Se requiere %s → %s).",
                    origenPrimero, destinoUltimo, this.aeropuertoOrigen, this.aeropuertoDestino);
        } else if (!coincideOrigen) {
            return String.format("Origen incoherente: Inicia en %s, pero el origen planificado es %s.",
                    origenPrimero, this.aeropuertoOrigen);
        } else {
            return String.format("Destino incompleto: Finaliza en %s, pero el destino planificado es %s.",
                    destinoUltimo, this.aeropuertoDestino);
        }
    }

    private void actualizarItinerarioArmadoMapa() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Vuelo> vuelosEnOrden = itinerariosAsignados.stream()
                    .sorted(Comparator.comparingInt(ItinerarioVuelo::getOrden))
                    .map(ItinerarioVuelo::getVuelo)
                    .toList();

            List<com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO> geometrias = new ArrayList<>();
            if (!vuelosEnOrden.isEmpty()) {
                geometrias = vueloService.construirGeometriaVuelos(vuelosEnOrden);
                if (geometrias == null || geometrias.isEmpty()) {
                    geometrias = construirGeometriasRespaldo(vuelosEnOrden);
                }
            }
            this.itinerarioArmadoMapaJson = mapper.writeValueAsString(geometrias);

            if (PrimeFaces.current().isAjaxRequest()) {
                String script = String.format("renderItinerarioArmado(%s);", mapper.writeValueAsString(this.itinerarioArmadoMapaJson));
                PrimeFaces.current().executeScript(script);
            }
        } catch (Exception e) {
            Logger.logError("Error en actualizarItinerarioArmadoMapa: " + e.getMessage());
            this.itinerarioArmadoMapaJson = "[]";
        }
    }

    private List<com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO> construirGeometriasRespaldo(List<Vuelo> vuelosEnOrden) {
        List<com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO> lista = new ArrayList<>();
        for (Vuelo v : vuelosEnOrden) {
            if (v == null || v.getSegmentoVuelos() == null || v.getSegmentoVuelos().isEmpty()) {
                continue;
            }
            List<com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO.PuntoSegmentoDTO> pts = new ArrayList<>();
            var segs = v.getSegmentoVuelos();
            for (int i = 0; i < segs.size(); i++) {
                var s = segs.get(i);
                if (i == 0 && s.getAeropuertoOrigen() != null && s.getAeropuertoOrigen().getPosicion() != null) {
                    var ao = s.getAeropuertoOrigen();
                    pts.add(new com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO.PuntoSegmentoDTO(
                            ao.getCodigoIata(), ao.getNombreAeropuerto(),
                            ao.getPosicion().getY(), ao.getPosicion().getX(), "ORIGEN"
                    ));
                }
                if (s.getAeropuertoDestino() != null && s.getAeropuertoDestino().getPosicion() != null) {
                    var ad = s.getAeropuertoDestino();
                    pts.add(new com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO.PuntoSegmentoDTO(
                            ad.getCodigoIata(), ad.getNombreAeropuerto(),
                            ad.getPosicion().getY(), ad.getPosicion().getX(),
                            (i == segs.size() - 1) ? "DESTINO" : "ESCALA"
                    ));
                }
            }
            if (!pts.isEmpty()) {
                lista.add(new com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO(v.getIdVuelo(), v.getNumeroVuelo(), pts));
            }
        }
        return lista;
    }

    /**
     * Prepara y serializa en JSON la geometría visual de un vuelo individual (origen, escalas y destino)
     * para ser renderizado en el mapa Leaflet dentro del diálogo modal de vista previa.
     *
     * @param vuelo El objeto Vuelo seleccionado en la tabla de búsquedas.
     */
    public void prepararMapaVuelo(Vuelo vuelo) {
        if (vuelo == null) {
            return;
        }
        try {
            // 1. Generar los DTOs geométricos usando el servicio de vuelos
            List<VueloMapaDTO> geometrias = vueloService.construirGeometriaVuelos(Collections.singletonList(vuelo));

            String vueloJson = "{}";
            if (geometrias != null && !geometrias.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                vueloJson = mapper.writeValueAsString(geometrias.get(0));
            }

            // 2. Inyectar el JSON en los argumentos de PrimeFaces (args.vueloJson)
            PrimeFaces.current().ajax().addCallbackParam("vueloJson", vueloJson);

        } catch (Exception e) {
            Logger.logInfo("Error al preparar la geometría del mapa para el vuelo "
                    + vuelo.getNumeroVuelo() + ": " + e.getMessage());
            PrimeFaces.current().ajax().addCallbackParam("vueloJson", "{}");
        }
    }

    public int getNumeroEscalasCalculado() {
        return Math.max(0, itinerariosAsignados.size() - 1);
    }

    // --- GETTERS Y SETTERS ---
    public String getItinerarioArmadoMapaJson() { return itinerarioArmadoMapaJson; }
    public void setItinerarioArmadoMapaJson(String itinerarioArmadoMapaJson) {
        // Mantenemos el JSON calculado en el servidor ante decodificaciones de formulario JSF
        if (itinerarioArmadoMapaJson != null && !itinerarioArmadoMapaJson.isBlank()) {
            this.itinerarioArmadoMapaJson = itinerarioArmadoMapaJson;
        }
    }
    public String getAeropuertosMapaJson() { return aeropuertosMapaJson; }
    public String getVuelosMapaJson() { return vuelosMapaJson; }
    public Itinerario getItinerario() { return itinerario; }
    public void setItinerario(Itinerario itinerario) { this.itinerario = itinerario; }
    public List<Itinerario> getListaItinerarios() { return listaItinerarios; }
    public List<Tarifa> getTarifas() { return tarifas; }
    public void setTarifas(List<Tarifa> tarifas) { this.tarifas = tarifas; }
    public List<Aeropuerto> getAeropuertos() { return aeropuertos; }
    public List<Ciudad> getCiudads() { return ciudades; }
    public List<Vuelo> getVuelos() { return vuelos; }
    public List<ItinerarioVuelo> getItinerariosAsignados() { return itinerariosAsignados; }
    public Map<Integer, BigDecimal> getPrecioTarifas() { return precioTarifas; }
    public Map<String, Aeropuerto> getAeropuertosMap() { return aeropuertosMap; }
    public String getAeropuertoOrigen() { return aeropuertoOrigen; }
    public void setAeropuertoOrigen(String aeropuertoOrigen) { this.aeropuertoOrigen = aeropuertoOrigen; }
    public String getAeropuertoDestino() { return aeropuertoDestino; }
    public void setAeropuertoDestino(String aeropuertoDestino) { this.aeropuertoDestino = aeropuertoDestino; }
    public String getAeropuertoOrigenBusqueda() { return aeropuertoOrigenBusqueda; }
    public void setAeropuertoOrigenBusqueda(String origenBusqueda) { this.aeropuertoOrigenBusqueda = origenBusqueda; }
    public String getAeropuertoDestinoBusqueda() { return aeropuertoDestinoBusqueda; }
    public void setAeropuertoDestinoBusqueda(String destinoBusqueda) { this.aeropuertoDestinoBusqueda = destinoBusqueda; }
    public LocalDate getFechaBusqueda() { return fechaBusqueda; }
    public void setFechaBusqueda(LocalDate fechaBusqueda) { this.fechaBusqueda = fechaBusqueda; }
    public boolean isEsModificacion() { return esModificacion; }
    public long getReservasTotalesEdicion() { return reservasTotalesEdicion; }
    public long getReservasActivasEdicion() { return reservasActivasEdicion; }
    public boolean isTramosBloqueadosPorReservas() { return tramosBloqueadosPorReservas; }
    public List<CombinacionRutaSugeridaDTO> getCombinacionesSugeridas() { return combinacionesSugeridas; }
    public String getCriterioOrdenSugerencias() { return criterioOrdenSugerencias; }
    public void setCriterioOrdenSugerencias(String criterioOrdenSugerencias) { this.criterioOrdenSugerencias = criterioOrdenSugerencias; }
    public int getMaxHorasEspera() { return maxHorasEspera; }
    public void setMaxHorasEspera(int maxHorasEspera) { this.maxHorasEspera = maxHorasEspera; }
    public Itinerario getUltimoItinerarioPublicado() { return ultimoItinerarioPublicado; }
    public LazyDataModel<Itinerario> getItinerariosLazy() {
        if (itinerariosLazy == null) inicializarItinerariosLazy();
        return itinerariosLazy;
    }
    public LazyDataModel<Vuelo> getVuelosDisponiblesLazy() {
        if (vuelosDisponiblesLazy == null) inicializarVuelosDisponiblesLazy();
        return vuelosDisponiblesLazy;
    }
    public long getTotalVuelosDisponibles() { return totalVuelosDisponibles; }
    public boolean isBusquedaVuelosActiva() { return busquedaVuelosActiva; }
}