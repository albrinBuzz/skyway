package com.SkyWay.views.perfil.pasajero;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator.TarifaCaracteristicaValidator;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.BloqueAsientosDTO;
import com.SkyWay.modules.asiento.presentation.dto.FilaCabinaDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.auditoria.application.LogAuditoriaService;
import com.SkyWay.modules.avion.domain.model.ConfiguracionCabina;
import com.SkyWay.modules.avion.domain.repository.ConfiguracionCabinaRepository;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.reservaasiento.domain.repository.ReservaAsientoRepository;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.reservaasiento.domain.service.CambioAsientoProcesadorService;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.modules.reservaasiento.presentation.dto.ResultadoCambioAsientoDTO;
import com.SkyWay.modules.reservaasiento.presentation.dto.SolicitudCambioAsientoDTO;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Named("cambioBean")
@ViewScoped
public class CambioAsientoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired private VueloService vueloService;
    @Autowired private AsientoService asientoService;
    @Autowired private ReservaService reservaService;
    @Autowired private ReservaAsientoService reservaAsientoService;
    @Autowired private ReservaAsientoRepository reservaAsientoRepository;
    @Autowired private ConfiguracionCabinaRepository configuracionCabinaRepository;
    @Autowired private TarifaCaracteristicaValidator tarifaValidator;
    @Autowired private AsientoCacheService asientoCacheService;
    @Autowired private LogAuditoriaService logAuditoriaService;
    @Autowired private PagoFactoryService pagoFactoryService;
    @Autowired private CambioAsientoProcesadorService cambioAsientoProcesadorService;

    private String idVuelo;
    private String idReserva;
    private Integer idItinerario;
    private String origenRetorno = "CHECKIN";
    private Integer idMetodoPagoSeleccionado = 1;

    private boolean permiteCambio = true;
    private String mensajeReglaTarifa;
    private String nombreTarifa;
    private int porcentajeCargoTarifa = 15;

    private Vuelo vuelo;
    private InfoAsientoDTO asientoActual;

    private List<Vuelo> vuelosReserva = new ArrayList<>();
    private List<InfoAsientoDTO> asientos = new ArrayList<>();
    private List<InfoAsientoDTO> asientoSeleccionados = new ArrayList<>();
    private Map<Integer, InfoAsientoDTO> asientosCambio = new HashMap<>();

    // Multi-Pasajeros y Asignaciones persistentes por tramo de vuelo
    private List<AsignacionPasajeroDTO> asignaciones = new ArrayList<>();
    private AsignacionPasajeroDTO asignacionActiva;
    private String rutUsuarioAutenticado;
    private boolean esUsuarioTitular = false;

    // PERSISTENCIA ENTRE CAMBIOS DE VUELO (Map<idVuelo, ...>)
    private Map<Integer, List<AsignacionPasajeroDTO>> asignacionesPorVuelo = new HashMap<>();
    private Map<Integer, Map<Integer, InfoAsientoDTO>> cambiosPorVuelo = new HashMap<>();

    // CONFIGURACIÓN DINÁMICA DE CABINA (DESDE TABLA Configuracion_Cabina)
    private List<ConfiguracionCabina> configuracionesModelo = new ArrayList<>();
    private boolean avionDoblePasillo = false;
    private String nombreModeloAvion = "Aeronave Comercial";

    // Estructura de Cabina para renderizado realista
    private List<FilaCabinaDTO> filasCabina = new ArrayList<>();

    // Retroalimentación operacional y comprobante transaccional para el pasajero
    private ResultadoCambioAsientoDTO resultadoCambio;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

            // 0. Identificar usuario en sesión
            Usuario usuarioLogueado = (Usuario) sessionMap.get("usuario");
            if (usuarioLogueado != null) {
                this.rutUsuarioAutenticado = usuarioLogueado.getRut();
            } else {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                    this.rutUsuarioAutenticado = auth.getName();
                }
            }

            // 1. Prioridad: Inyección segura por sesión
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ID_RESERVA")) {
                Object r = sessionMap.get("CAMBIO_ASIENTO_ID_RESERVA");
                if (r != null) this.idReserva = r.toString();
            }
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ID_VUELO")) {
                Object v = sessionMap.get("CAMBIO_ASIENTO_ID_VUELO");
                if (v != null) this.idVuelo = v.toString();
            }
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ID_ITINERARIO")) {
                Object it = sessionMap.get("CAMBIO_ASIENTO_ID_ITINERARIO");
                if (it instanceof Integer) {
                    this.idItinerario = (Integer) it;
                } else if (it != null) {
                    try { this.idItinerario = Integer.parseInt(it.toString()); } catch (NumberFormatException ignored) {}
                }
            }
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ORIGEN")) {
                Object orig = sessionMap.get("CAMBIO_ASIENTO_ORIGEN");
                if (orig != null) this.origenRetorno = orig.toString();
            }

            // 2. Manejo seguro de estado y resultados por sesión HTTP (sin exponer parámetros sensibles en URL)
            if (sessionMap.containsKey("CAMBIO_ASIENTO_STATUS")) {
                String st = (String) sessionMap.remove("CAMBIO_ASIENTO_STATUS");
                if ("SUCCESS".equalsIgnoreCase(st)) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "¡Operación Exitosa!",
                            "Tus nuevos asientos han sido reasignados y emitidos correctamente."));
                } else if ("CANCELLED".equalsIgnoreCase(st)) {
                    facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Pago Cancelado",
                            "La transacción en la pasarela fue cancelada. No se efectuaron cobros ni cambios en tus butacas."));
                }
            }

            // Comprobante detallado tras retorno exitoso de pasarela
            if (sessionMap.containsKey("RESULTADO_CAMBIO_ASIENTO")) {
                Object r = sessionMap.remove("RESULTADO_CAMBIO_ASIENTO");
                if (r instanceof ResultadoCambioAsientoDTO) {
                    this.resultadoCambio = (ResultadoCambioAsientoDTO) r;
                    if (this.resultadoCambio.getIdReserva() != null) {
                        this.idReserva = String.valueOf(this.resultadoCambio.getIdReserva());
                        sessionMap.put("CAMBIO_ASIENTO_ID_RESERVA", this.idReserva);
                    }
                    if (this.resultadoCambio.getOrigen() != null && !this.resultadoCambio.getOrigen().isBlank()) {
                        this.origenRetorno = this.resultadoCambio.getOrigen();
                    }
                }
            }

            // 3. Si no hay idVuelo pero sí idReserva, resolver vuelo automáticamente
            if ((this.idVuelo == null || this.idVuelo.isBlank()) && this.idReserva != null && !this.idReserva.isBlank()) {
                resolverVueloDeReserva();
            }

            // 4. Validar reglas de la tarifa contratada
            validarTarifaYReglas();

            if (this.permiteCambio) {
                cargarDatosVueloYAsientos();
            }
        }
    }

    private void resolverVueloDeReserva() {
        try {
            int parsedIdReserva = Integer.parseInt(idReserva);
            Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
            if (optRes.isPresent()) {
                Reserva res = optRes.get();

            // Validación de seguridad y autorización: el usuario autenticado debe pertenecer a la reserva
            if (this.rutUsuarioAutenticado != null && !this.rutUsuarioAutenticado.isBlank()
                    && !"anonymousUser".equalsIgnoreCase(this.rutUsuarioAutenticado)) {
                String rutTitular = (res.getPasajero() != null) ? res.getPasajero().getRut() : "";
                boolean tieneAcceso = this.rutUsuarioAutenticado.equalsIgnoreCase(rutTitular);
                if (!tieneAcceso && res.getReservaAsientos() != null) {
                    for (ReservaAsiento ra : res.getReservaAsientos()) {
                        if (ra.getRut() != null && this.rutUsuarioAutenticado.equalsIgnoreCase(ra.getRut())) {
                            tieneAcceso = true;
                            break;
                        }
                    }
                }
                if (!tieneAcceso) {
                    this.permiteCambio = false;
                    this.mensajeReglaTarifa = "No tienes autorización para acceder a los asientos de esta reserva.";
                    return;
                }
            }
                if (res.getReservaItinerarios() != null) {
                    for (ReservaItinerario ri : res.getReservaItinerarios()) {
                        if (ri.getItinerario() != null && ri.getItinerario().getItinerarioVuelos() != null) {
                            for (ItinerarioVuelo iv : ri.getItinerario().getItinerarioVuelos()) {
                                if (iv.getVuelo() != null) {
                                    this.idVuelo = String.valueOf(iv.getVuelo().getIdVuelo());
                                    if (this.idItinerario == null && ri.getItinerario() != null) {
                                        this.idItinerario = ri.getItinerario().getIdItinerario();
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logError("Error al resolver vuelo de reserva: " + e.getMessage());
        }
    }

    public void validarTarifaYReglas() {
        if (idReserva == null || idReserva.isBlank()) {
            this.permiteCambio = false;
            this.mensajeReglaTarifa = "No se ha proporcionado una reserva para cambio de asiento.";
            return;
        }

        try {
            int parsedIdReserva = Integer.parseInt(idReserva);
            Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
            if (optRes.isEmpty()) {
                this.permiteCambio = false;
                this.mensajeReglaTarifa = "No se encontró información de la reserva #" + idReserva;
                return;
            }

            Reserva res = optRes.get();

            // Validación de seguridad y autorización: el usuario autenticado debe pertenecer a la reserva
            if (this.rutUsuarioAutenticado != null && !this.rutUsuarioAutenticado.isBlank()
                    && !"anonymousUser".equalsIgnoreCase(this.rutUsuarioAutenticado)) {
                String rutTitular = (res.getPasajero() != null) ? res.getPasajero().getRut() : "";
                boolean tieneAcceso = this.rutUsuarioAutenticado.equalsIgnoreCase(rutTitular);
                if (!tieneAcceso && res.getReservaAsientos() != null) {
                    for (ReservaAsiento ra : res.getReservaAsientos()) {
                        if (ra.getRut() != null && this.rutUsuarioAutenticado.equalsIgnoreCase(ra.getRut())) {
                            tieneAcceso = true;
                            break;
                        }
                    }
                }
                if (!tieneAcceso) {
                    this.permiteCambio = false;
                    this.mensajeReglaTarifa = "No tienes autorización para acceder a los asientos de esta reserva.";
                    return;
                }
            }

            if (res.getEstadoReservaBean() != null && res.getEstadoReservaBean().getDescripcion() != null
                    && res.getEstadoReservaBean().getDescripcion().toLowerCase().contains("cancelad")) {
                this.permiteCambio = false;
                this.mensajeReglaTarifa = "La reserva se encuentra cancelada. No es posible cambiar asientos.";
                return;
            }

            if (res.getReservaItinerarios() != null && !res.getReservaItinerarios().isEmpty()) {
                for (ReservaItinerario ri : res.getReservaItinerarios()) {
                    if (ri.getItinerarioTarifa() != null && ri.getItinerarioTarifa().getTarifa() != null) {
                        Tarifa t = ri.getItinerarioTarifa().getTarifa();
                        this.nombreTarifa = t.getNombre();
                        List<TarifaCaracteristica> caracs = t.getTarifaCaracteristicas();

                        this.porcentajeCargoTarifa = tarifaValidator.obtenerPorcentajeCargoCambio(caracs);

                        if (!tarifaValidator.permiteCambio(caracs)) {
                            this.permiteCambio = false;
                            this.mensajeReglaTarifa = "La tarifa básica no permite cambios de asiento.";
                            return;
                        }

                        Date fSalida = null;
                        if (ri.getItinerario() != null && ri.getItinerario().getHoraSalida() != null) {
                            fSalida = new Date(ri.getItinerario().getHoraSalida().getTime());
                        }

                        if (fSalida != null) {
                            long diffMillis = fSalida.getTime() - System.currentTimeMillis();
                            int horasRestantes = (int) (diffMillis / (3600 * 1000));
                            // El plazo límite de la tarifa aplica siempre, también desde el flujo de check-in
                            if (!tarifaValidator.cumpleHorasMinimasCambio(caracs, horasRestantes)) {
                                this.permiteCambio = false;
                                Integer hMin = tarifaValidator.obtenerHorasMinimasCambio(caracs);
                                int h = (hMin != null && hMin > 0) ? hMin : (this.nombreTarifa != null && this.nombreTarifa.toLowerCase().contains("flex") ? 48 : 24);
                                if (this.nombreTarifa != null && this.nombreTarifa.toLowerCase().contains("flex")) {
                                    this.mensajeReglaTarifa = "Solo puedes cambiar el asiento hasta " + h + " horas antes del vuelo con tarifa flexible.";
                                } else if (this.nombreTarifa != null && this.nombreTarifa.toLowerCase().contains("prem")) {
                                    this.mensajeReglaTarifa = "Solo puedes cambiar el asiento hasta " + h + " horas antes del vuelo con tarifa premium.";
                                } else {
                                    this.mensajeReglaTarifa = "Solo puedes cambiar el asiento hasta " + h + " horas antes del vuelo con tu tarifa.";
                                }
                                return;
                            }
                        }

                        this.permiteCambio = true;
                        return;
                    }
                }
            }

            this.permiteCambio = true;
        } catch (Exception e) {
            Logger.logError("Error al verificar tarifa en CambioAsientoBean: " + e.getMessage());
            this.permiteCambio = false;
            this.mensajeReglaTarifa = "Ocurrió un error al verificar las restricciones de tu tarifa: " + e.getMessage();
        }
    }

    public void cargarDatosVueloYAsientos() {
        if (idReserva != null && !idReserva.isEmpty()) {
            try {
                int parsedIdReserva = Integer.parseInt(idReserva);

                // 1. Cargar lista de vuelos de la reserva si aún no se cargaron
                if (vuelosReserva.isEmpty()) {
                    Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
                    if (optRes.isPresent() && optRes.get().getReservaItinerarios() != null) {
                        Set<Integer> vuelosIds = new LinkedHashSet<>();
                        for (ReservaItinerario ri : optRes.get().getReservaItinerarios()) {
                            if (ri.getItinerario() != null && ri.getItinerario().getItinerarioVuelos() != null) {
                                for (ItinerarioVuelo iv : ri.getItinerario().getItinerarioVuelos()) {
                                    if (iv.getVuelo() != null && vuelosIds.add(iv.getVuelo().getIdVuelo())) {
                                        vuelosReserva.add(iv.getVuelo());
                                    }
                                }
                            }
                        }
                    }
                }

                if ((idVuelo == null || idVuelo.isEmpty()) && !vuelosReserva.isEmpty()) {
                    idVuelo = String.valueOf(vuelosReserva.get(0).getIdVuelo());
                }

                if (idVuelo != null && !idVuelo.isEmpty()) {
                    int parsedIdVuelo = Integer.parseInt(idVuelo);
                    vuelo = vueloService.findById(parsedIdVuelo).orElse(null);

                    if (vuelo != null) {
                        // 2. Cargar configuración de cabina del modelo de avión desde la base de datos
                        cargarConfiguracionCabinaModelo(vuelo);

                        asientos = asientoService.getAsientosVuelo(parsedIdReserva, parsedIdVuelo);

                        if (asientos != null) {
                            asientoSeleccionados = asientos.stream()
                                    .filter(asiento -> "seleccionado".equalsIgnoreCase(asiento.getEstado()))
                                    .toList();

                            construirFilasCabina();

                            // 3. Obtener o construir asignaciones de pasajeros persistentes
                            this.asignaciones = obtenerOConstruirAsignacionesVuelo(parsedIdReserva, parsedIdVuelo, vuelo);
                            this.asientosCambio = cambiosPorVuelo.computeIfAbsent(parsedIdVuelo, k -> new HashMap<>());

                            // Seleccionar por defecto la primera asignación editable
                            if (!this.asignaciones.isEmpty()) {
                                this.asignacionActiva = this.asignaciones.stream()
                                        .filter(AsignacionPasajeroDTO::isEditable)
                                        .findFirst()
                                        .orElse(this.asignaciones.get(0));

                                if (this.asignacionActiva.getIdAsientoOriginal() != null) {
                                    this.asientoActual = asientos.stream()
                                            .filter(a -> a.getIdAsiento() == this.asignacionActiva.getIdAsientoOriginal())
                                            .findFirst()
                                            .orElse(null);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.logError("Error cargando vuelo/asientos en CambioAsientoBean: " + e.getMessage());
            }
        }
    }

    private void cargarConfiguracionCabinaModelo(Vuelo v) {
        if (v != null && v.getAvion() != null && v.getAvion().getModeloAvion() != null) {
            Integer idModelo = v.getAvion().getModeloAvion().getIdModelo();
            this.nombreModeloAvion = v.getAvion().getModeloAvion().getNombre();
            this.configuracionesModelo = configuracionCabinaRepository.findByModeloAvionIdModelo(idModelo);

            // Determinar si es un avión de fuselaje ancho (Doble pasillo) a partir de la configuración
            this.avionDoblePasillo = this.configuracionesModelo.stream()
                    .anyMatch(cc -> cc.getDistribucionColumnas() != null &&
                            cc.getDistribucionColumnas().chars().filter(ch -> ch == '-').count() >= 2);
        } else {
            this.configuracionesModelo = Collections.emptyList();
            this.avionDoblePasillo = false;
            this.nombreModeloAvion = "Aeronave Comercial";
        }
    }

    private List<AsignacionPasajeroDTO> obtenerOConstruirAsignacionesVuelo(int parsedIdReserva, int parsedIdVuelo, Vuelo vueloObj) {
        if (asignacionesPorVuelo.containsKey(parsedIdVuelo)) {
            return asignacionesPorVuelo.get(parsedIdVuelo);
        }

        List<AsignacionPasajeroDTO> listaAsig = new ArrayList<>();
        try {
            Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
            String rutTitular = optRes.map(Reserva::getPasajero)
                    .map(com.SkyWay.modules.pasajero.domain.model.Pasajero::getRut).orElse("");
            this.esUsuarioTitular = (this.rutUsuarioAutenticado != null && this.rutUsuarioAutenticado.equalsIgnoreCase(rutTitular));

            List<ReservaAsiento> listaRA = reservaAsientoRepository.findAsignacionesByReservaYVuelo(parsedIdReserva, parsedIdVuelo);

            Map<Integer, InfoAsientoDTO> mapaInfo = new HashMap<>();
            if (this.asientos != null) {
                for (InfoAsientoDTO a : this.asientos) {
                    mapaInfo.put(a.getIdAsiento(), a);
                }
            }

            String numVuelo = (vueloObj != null) ? vueloObj.getNumeroVuelo() : "SW-" + parsedIdVuelo;
            String tramo = (vueloObj != null && vueloObj.getSegmentoVuelos() != null && !vueloObj.getSegmentoVuelos().isEmpty())
                    ? vueloObj.getSegmentoVuelos().get(0).getAeropuertoOrigen().getCodigoIata() + " ➔ " +
                      vueloObj.getSegmentoVuelos().get(vueloObj.getSegmentoVuelos().size() - 1).getAeropuertoDestino().getCodigoIata()
                    : "Tramo";

            for (ReservaAsiento ra : listaRA) {
                Integer idAsiento = (ra.getAsiento() != null) ? ra.getAsiento().getIdAsiento() : null;
                String numAsiento = (ra.getAsiento() != null) ? ra.getAsiento().getNumeroAsiento() : "N/A";
                String clase = "Económica";
                int precio = 0;

                InfoAsientoDTO info = (idAsiento != null) ? mapaInfo.get(idAsiento) : null;
                if (info != null) {
                    clase = info.getClase();
                    precio = info.getPrecio();
                } else if (ra.getAsiento() != null && ra.getAsiento().getClaseAsiento() != null) {
                    clase = ra.getAsiento().getClaseAsiento().getDescripcion();
                }

                String rutPasajero = ra.getRut();
                String nombrePasajero = (ra.getPasajero() != null && ra.getPasajero().getUsuario() != null)
                        ? ra.getPasajero().getUsuario().getNombre() + " " + ra.getPasajero().getUsuario().getApellido()
                        : "Pasajero (" + rutPasajero + ")";

                boolean esTitular = (rutPasajero != null && rutPasajero.equalsIgnoreCase(rutTitular));
                boolean editable = this.esUsuarioTitular || (this.rutUsuarioAutenticado != null && this.rutUsuarioAutenticado.equalsIgnoreCase(rutPasajero));

                AsignacionPasajeroDTO asig = new AsignacionPasajeroDTO(
                        parsedIdVuelo, numVuelo, tramo,
                        idAsiento, numAsiento, clase, precio, rutPasajero, nombrePasajero, esTitular, editable
                );
                asig.setPorcentajeCargo(this.porcentajeCargoTarifa);

                listaAsig.add(asig);
            }

            asignacionesPorVuelo.put(parsedIdVuelo, listaAsig);

        } catch (Exception e) {
            Logger.logError("Error al cargar asignaciones de vuelo " + parsedIdVuelo + ": " + e.getMessage());
        }

        return listaAsig;
    }

    public void seleccionarPasajero(AsignacionPasajeroDTO asig) {
        if (asig == null) return;
        if (!asig.isEditable()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Acción No Autorizada",
                            "Solo el titular de la reserva puede gestionar el asiento de este acompañante."));
            return;
        }

        this.asignacionActiva = asig;
        if (asig.getIdAsientoOriginal() != null && this.asientos != null) {
            this.asientoActual = this.asientos.stream()
                    .filter(a -> a.getIdAsiento() == asig.getIdAsientoOriginal())
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * CAMBIO DE VUELO CON PERSISTENCIA COMPLETA DE SELECCIONES PREVIAS.
     */
    public void seleccionarVuelo(Vuelo v) {
        if (v != null) {
            // 1. Guardar cambios del vuelo anterior en memoria
            if (this.idVuelo != null && !this.idVuelo.isBlank()) {
                try {
                    int oldVueloId = Integer.parseInt(this.idVuelo);
                    cambiosPorVuelo.put(oldVueloId, new HashMap<>(this.asientosCambio));
                    asignacionesPorVuelo.put(oldVueloId, new ArrayList<>(this.asignaciones));
                } catch (NumberFormatException ignored) {}
            }

            // 2. Establecer nuevo vuelo
            this.idVuelo = String.valueOf(v.getIdVuelo());
            this.vuelo = v;
            int parsedIdVuelo = v.getIdVuelo();
            int parsedIdReserva = Integer.parseInt(this.idReserva);

            // 3. Cargar configuración de cabina y asientos del vuelo seleccionado
            cargarConfiguracionCabinaModelo(v);
            this.asientos = asientoService.getAsientosVuelo(parsedIdReserva, parsedIdVuelo);
            construirFilasCabina();

            // 4. Restaurar asignaciones y selección previa (NO SE REINICIA)
            this.asignaciones = obtenerOConstruirAsignacionesVuelo(parsedIdReserva, parsedIdVuelo, v);
            this.asientosCambio = cambiosPorVuelo.computeIfAbsent(parsedIdVuelo, k -> new HashMap<>());

            // 5. Configurar asignación activa
            this.asignacionActiva = this.asignaciones.stream()
                    .filter(AsignacionPasajeroDTO::isEditable)
                    .findFirst()
                    .orElse(!this.asignaciones.isEmpty() ? this.asignaciones.get(0) : null);

            if (this.asignacionActiva != null && this.asignacionActiva.getIdAsientoOriginal() != null && this.asientos != null) {
                this.asientoActual = this.asientos.stream()
                        .filter(a -> a.getIdAsiento() == this.asignacionActiva.getIdAsientoOriginal())
                        .findFirst()
                        .orElse(null);
            } else {
                this.asientoActual = null;
            }
        }
    }

    public void volver() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getExternalContext() != null) {
                if ("PERFIL".equalsIgnoreCase(this.origenRetorno)) {
                    context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/perfil/pasajero/index.xhtml");
                } else {
                    Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
                    if (this.idReserva != null) {
                        try {
                            sessionMap.put("CHECKIN_ID_RESERVA", Integer.parseInt(this.idReserva));
                        } catch (NumberFormatException ignored) {}
                    }
                    if (this.idItinerario != null) {
                        sessionMap.put("CHECKIN_ID_ITINERARIO", this.idItinerario);
                    }
                    context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/home/checkin.xhtml");
                }
            }
        } catch (Exception e) {
            Logger.logError("Error al volver desde cambio de asiento: " + e.getMessage());
        }
    }

    /**
     * CONSTRUCCIÓN DINÁMICA DE CABINA A PARTIR DE LA TABLA Configuracion_Cabina.
     */
    private void construirFilasCabina() {
        this.filasCabina = new ArrayList<>();
        if (this.asientos == null || this.asientos.isEmpty()) return;

        Map<Integer, List<InfoAsientoDTO>> mapaFilas = new TreeMap<>();
        for (InfoAsientoDTO a : this.asientos) {
            int numFila = (a.getFila() != null && a.getFila() > 0) ? a.getFila() : extraerFila(a.getNumeroAsiento());
            if (a.getLetra() == null || a.getLetra().trim().isEmpty()) {
                a.setLetra(extraerLetra(a.getNumeroAsiento()));
            }
            mapaFilas.computeIfAbsent(numFila, k -> new ArrayList<>()).add(a);
        }

        String claseAnterior = null;

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : mapaFilas.entrySet()) {
            int numFila = entry.getKey();
            List<InfoAsientoDTO> asientosFila = entry.getValue();
            asientosFila.sort(Comparator.comparing(InfoAsientoDTO::getLetra));

            // 1. Obtener la configuración específica para esta fila desde Configuracion_Cabina
            ConfiguracionCabina configFila = null;
            for (ConfiguracionCabina cc : this.configuracionesModelo) {
                if (cc.getFilaInicio() != null && cc.getFilaFin() != null &&
                    numFila >= cc.getFilaInicio() && numFila <= cc.getFilaFin()) {
                    configFila = cc;
                    break;
                }
            }

            // 2. Determinar clase y salida de emergencia
            String claseFila = (configFila != null && configFila.getClaseAsiento() != null)
                    ? configFila.getClaseAsiento().getDescripcion()
                    : asientosFila.get(0).getClase();

            boolean esEmergencia = (configFila != null && Boolean.TRUE.equals(configFila.getEsSalidaEmergencia()))
                    || asientosFila.stream().anyMatch(a -> Boolean.TRUE.equals(a.getEsEmergencia()));

            FilaCabinaDTO filaDTO = new FilaCabinaDTO(numFila, claseFila, esEmergencia);
            if (!asientosFila.isEmpty()) {
                filaDTO.setPrecio(asientosFila.get(0).getPrecio());
            }

            // 3. Cabecera dinámica de inicio de clase
            if (claseAnterior == null || !claseAnterior.equalsIgnoreCase(claseFila)) {
                filaDTO.setEsPrimerFilaDeClase(true);
                claseAnterior = claseFila;
            }

            // 4. Agrupar bloques de asientos según Distribucion_Columnas de la BD
            List<Integer> tamaniosBloques = parsearDistribucionColumnas(
                    (configFila != null) ? configFila.getDistribucionColumnas() : null,
                    asientosFila.size()
            );

            int indiceAsiento = 0;
            int totalAsientos = asientosFila.size();

            for (int tamBloque : tamaniosBloques) {
                if (indiceAsiento >= totalAsientos) break;
                BloqueAsientosDTO bloque = new BloqueAsientosDTO();
                for (int b = 0; b < tamBloque && indiceAsiento < totalAsientos; b++) {
                    bloque.getAsientos().add(asientosFila.get(indiceAsiento++));
                }
                if (!bloque.getAsientos().isEmpty()) {
                    filaDTO.getBloques().add(bloque);
                }
            }

            // Respaldo de seguridad si sobran asientos
            if (indiceAsiento < totalAsientos) {
                if (filaDTO.getBloques().isEmpty()) {
                    filaDTO.getBloques().add(new BloqueAsientosDTO());
                }
                BloqueAsientosDTO ultimoBloque = filaDTO.getBloques().get(filaDTO.getBloques().size() - 1);
                while (indiceAsiento < totalAsientos) {
                    ultimoBloque.getAsientos().add(asientosFila.get(indiceAsiento++));
                }
            }

            this.filasCabina.add(filaDTO);
        }
    }

    private List<Integer> parsearDistribucionColumnas(String distribucion, int totalFila) {
        List<Integer> resultado = new ArrayList<>();
        if (distribucion != null && distribucion.contains("-")) {
            try {
                String[] partes = distribucion.split("-");
                for (String p : partes) {
                    int val = Integer.parseInt(p.trim());
                    if (val > 0) resultado.add(val);
                }
                if (!resultado.isEmpty()) {
                    return resultado;
                }
            } catch (NumberFormatException ignored) {}
        }
        // Fallbacks inteligentes según total de asientos en la fila
        if (totalFila == 4) {
            resultado.add(2); resultado.add(2);
        } else if (totalFila == 6) {
            resultado.add(3); resultado.add(3);
        } else if (totalFila == 8) {
            resultado.add(2); resultado.add(4); resultado.add(2);
        } else if (totalFila == 9) {
            resultado.add(3); resultado.add(3); resultado.add(3);
        } else if (totalFila == 10) {
            resultado.add(3); resultado.add(4); resultado.add(3);
        } else if (totalFila == 3) {
            resultado.add(1); resultado.add(2);
        } else {
            resultado.add(totalFila);
        }
        return resultado;
    }

    private String extraerLetra(String num) {
        if (num == null) return "";
        String l = num.replaceAll("[0-9]", "").trim();
        return l.isEmpty() ? num : l;
    }

    private int extraerFila(String num) {
        if (num == null) return 1;
        String d = num.replaceAll("[^0-9]", "");
        return d.isEmpty() ? 1 : Integer.parseInt(d);
    }

    public void seleccionarAsientoAEditar(InfoAsientoDTO asiento) {
        this.asientoActual = asiento;
        if (asiento != null) {
            for (AsignacionPasajeroDTO asig : asignaciones) {
                if (asig.getIdAsientoOriginal() != null && asig.getIdAsientoOriginal() == asiento.getIdAsiento()) {
                    this.asignacionActiva = asig;
                    break;
                }
            }
        }
    }

    public void setearAsiento(InfoAsientoDTO asientoNuevo) {
        if (asignacionActiva == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Selección de Pasajero Requerida",
                            "Por favor selecciona primero a qué pasajero deseas cambiarle el asiento."));
            return;
        }

        if (!asignacionActiva.isEditable()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Acción No Autorizada",
                            "Solo el titular puede gestionar el asiento de este pasajero."));
            return;
        }

        if (asientoNuevo == null || "ocupado".equalsIgnoreCase(asientoNuevo.getEstado())) {
            return;
        }

        // Si selecciona su propio asiento original -> deseleccionar y restaurar
        if (asignacionActiva.getIdAsientoOriginal() != null && asientoNuevo.getIdAsiento() == asignacionActiva.getIdAsientoOriginal()) {
            deseleccionarAsiento(asignacionActiva);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Asiento Restaurado",
                            "Se mantuvo el asiento original " + asignacionActiva.getNumeroAsientoOriginal() + " para " + asignacionActiva.getNombrePasajero() + "."));
            return;
        }

        // Verificar si el asiento ya fue seleccionado para otro pasajero en este vuelo
        boolean ocupadoPorOtro = asignaciones.stream()
                .anyMatch(a -> a != asignacionActiva && a.getNuevoAsiento() != null && a.getNuevoAsiento().getIdAsiento() == asientoNuevo.getIdAsiento());
        if (ocupadoPorOtro) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Asiento Ya Asignado",
                            "Este asiento ya fue seleccionado para otro pasajero en este vuelo."));
            return;
        }

        // Regla de Descenso de Categoría (Downgrade no permitido en autoservicio): se evalúa por jerarquía de cabina
        if (com.SkyWay.mobile.service.MobileOperationsService.rangoClase(asientoNuevo.getClase())
                < com.SkyWay.mobile.service.MobileOperationsService.rangoClase(asignacionActiva.getClaseOriginal())) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Descenso de Cabina No Permitido",
                            "No es posible seleccionar una butaca de categoría inferior a la originalmente contratada (" + asignacionActiva.getClaseOriginal() + ")."));
            return;
        }

        // Asignar el nuevo asiento con cálculo de tarifa y recargo administrativo desde BD
        asignacionActiva.asignarNuevoAsiento(asientoNuevo, this.porcentajeCargoTarifa);
        asientosCambio.put(asignacionActiva.getIdAsientoOriginal(), asientoNuevo);

        // Sincronizar en el mapa global por vuelo
        if (idVuelo != null) {
            try {
                int vId = Integer.parseInt(idVuelo);
                cambiosPorVuelo.computeIfAbsent(vId, k -> new HashMap<>()).put(asignacionActiva.getIdAsientoOriginal(), asientoNuevo);
            } catch (NumberFormatException ignored) {}
        }

        if (asignacionActiva.esUpgrade()) {
            String msgCargo = (asignacionActiva.getCargoGestion() > 0)
                    ? String.format(" + Cargo administrativo (%d%%): $%,d CLP", asignacionActiva.getPorcentajeCargo(), asignacionActiva.getCargoGestion())
                    : " (Sin cargo administrativo adicional)";
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Ascenso de Cabina Seleccionado",
                            String.format("Asiento %s (%s) para %s. Diferencia: $%,d CLP%s.",
                                    asientoNuevo.getNumeroAsiento(), asientoNuevo.getClase(), asignacionActiva.getNombrePasajero(),
                                    asignacionActiva.getDiferenciaTarifa(), msgCargo)));
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Nuevo Asiento Seleccionado",
                            String.format("Asiento %s (%s) seleccionado para %s (Sin costo adicional).",
                                    asientoNuevo.getNumeroAsiento(), asientoNuevo.getClase(), asignacionActiva.getNombrePasajero())));
        }
    }

    public void deseleccionarAsiento(AsignacionPasajeroDTO asig) {
        if (asig != null) {
            if (asig.getIdAsientoOriginal() != null) {
                asientosCambio.remove(asig.getIdAsientoOriginal());
                if (idVuelo != null) {
                    try {
                        int vId = Integer.parseInt(idVuelo);
                        if (cambiosPorVuelo.containsKey(vId)) {
                            cambiosPorVuelo.get(vId).remove(asig.getIdAsientoOriginal());
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
            asig.deseleccionar();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Selección Cancelada", "Se descartó el cambio de asiento para " + asig.getNombrePasajero() + "."));
        }
    }

    // MÉTODOS DE CÁLCULO MULTI-VUELO
    public int getCantidadCambiosVuelo(Integer idVueloCheck) {
        if (idVueloCheck == null) return 0;
        List<AsignacionPasajeroDTO> list = asignacionesPorVuelo.get(idVueloCheck);
        if (list == null) return 0;
        return (int) list.stream().filter(AsignacionPasajeroDTO::tieneCambio).count();
    }

    public int getTotalCambiosGlobal() {
        int total = 0;
        for (List<AsignacionPasajeroDTO> list : asignacionesPorVuelo.values()) {
            total += (int) list.stream().filter(AsignacionPasajeroDTO::tieneCambio).count();
        }
        return total;
    }

    public boolean isHayCambiosPendientes() {
        return getTotalCambiosGlobal() > 0;
    }

    public int getTotalDiferenciaTarifa() {
        int total = 0;
        for (List<AsignacionPasajeroDTO> list : asignacionesPorVuelo.values()) {
            total += list.stream().mapToInt(AsignacionPasajeroDTO::getDiferenciaTarifa).sum();
        }
        return total;
    }

    public int getTotalCargoGestion() {
        int total = 0;
        for (List<AsignacionPasajeroDTO> list : asignacionesPorVuelo.values()) {
            total += list.stream().mapToInt(AsignacionPasajeroDTO::getCargoGestion).sum();
        }
        return total;
    }

    public int getGranTotalAdicional() {
        int total = 0;
        for (List<AsignacionPasajeroDTO> list : asignacionesPorVuelo.values()) {
            total += list.stream().mapToInt(AsignacionPasajeroDTO::getTotalAdicional).sum();
        }
        return total;
    }

    public List<AsignacionPasajeroDTO> getTodosLosCambiosPendientes() {
        List<AsignacionPasajeroDTO> todos = new ArrayList<>();
        for (List<AsignacionPasajeroDTO> list : asignacionesPorVuelo.values()) {
            for (AsignacionPasajeroDTO asig : list) {
                if (asig.tieneCambio()) {
                    todos.add(asig);
                }
            }
        }
        return todos;
    }

    public void abrirDialogoConfirmacion() {
        if (!isHayCambiosPendientes()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin Cambios", "No has seleccionado ningún nuevo asiento en el mapa de cabina."));
            return;
        }
        PrimeFaces.current().executeScript("PF('dlgConfirmarCambio').show();");
    }

    public void confirmarCambioFinal() {
        if (!isHayCambiosPendientes()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin Cambios", "No hay modificaciones pendientes por confirmar."));
            return;
        }

        try {
            int parsedIdReserva = Integer.parseInt(idReserva);

            // Armar DTO consolidado con todos los cambios multi-tramo
            SolicitudCambioAsientoDTO dto = new SolicitudCambioAsientoDTO();
            dto.setIdReserva(parsedIdReserva);
            dto.setRutUsuario(this.rutUsuarioAutenticado != null ? this.rutUsuarioAutenticado : "SISTEMA");
            dto.setGranTotalAdicional(getGranTotalAdicional());
            dto.setIdMetodoPago(this.idMetodoPagoSeleccionado != null ? this.idMetodoPagoSeleccionado : 1);
            dto.setOrigen(this.origenRetorno != null ? this.origenRetorno : "RESERVA");

            for (Map.Entry<Integer, List<AsignacionPasajeroDTO>> entry : asignacionesPorVuelo.entrySet()) {
                Integer flightId = entry.getKey();
                for (AsignacionPasajeroDTO asig : entry.getValue()) {
                    if (asig.tieneCambio()) {
                        InfoAsientoDTO nuevo = asig.getNuevoAsiento();
                        SolicitudCambioAsientoDTO.ItemCambioAsientoDTO it = new SolicitudCambioAsientoDTO.ItemCambioAsientoDTO();
                        it.setIdVuelo(flightId);
                        it.setNumeroVuelo(asig.getNumeroVuelo());
                        it.setTramoVuelo(asig.getTramoVuelo());
                        it.setRutPasajero(asig.getRutPasajero());
                        it.setNombrePasajero(asig.getNombrePasajero());
                        it.setIdAsientoOriginal(asig.getIdAsientoOriginal());
                        it.setNumeroAsientoOriginal(asig.getNumeroAsientoOriginal());
                        it.setClaseOriginal(asig.getClaseOriginal());
                        it.setIdAsientoNuevo(nuevo.getIdAsiento());
                        it.setNumeroAsientoNuevo(nuevo.getNumeroAsiento());
                        it.setClaseNueva(nuevo.getClase());
                        it.setMontoAdicional(asig.getTotalAdicional());
                        dto.addItem(it);
                    }
                }
            }

            FacesContext fc = FacesContext.getCurrentInstance();
            HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
            HttpSession session = req.getSession(true);

            // CASO A: Sin costo adicional ($0 CLP, misma cabina) -> Proceso directo atómico
            if (dto.getGranTotalAdicional() <= 0) {
                ResultadoCambioAsientoDTO res = cambioAsientoProcesadorService.procesarCambioAsientoDirecto(dto, req);
                if (res != null) {
                    this.resultadoCambio = res;
                    fc.addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO,
                                    "¡Modificación Exitosa!",
                                    "Tus nuevos asientos y tarjetas de embarque han sido emitidos correctamente sin costo adicional."));

                    // Limpiar cachés temporales y recargar
                    asientosCambio.clear();
                    cambiosPorVuelo.clear();
                    asignacionesPorVuelo.clear();
                    cargarDatosVueloYAsientos();

                    PrimeFaces.current().executeScript("PF('dlgConfirmarCambio').hide(); PF('dlgResultadoCambio').show();");
                    return;
                }
            }

            // CASO B: Requiere Pago en Pasarela (Ascenso de clase / diferencia tarifaria)
            session.setAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE", dto);

            // Determinar pasarela seleccionada
            MetodoPagoEnum metodo;
            if (dto.getIdMetodoPago() == 2) {
                metodo = MetodoPagoEnum.PAYPAL;
            } else if (dto.getIdMetodoPago() == 3) {
                metodo = MetodoPagoEnum.MERCADOPAGO;
            } else {
                metodo = MetodoPagoEnum.WEBPAY;
            }

            String scheme = req.getScheme();
            String serverName = req.getServerName();
            int serverPort = req.getServerPort();
            String contextPath = req.getContextPath();
            String portSegment = ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
                    ? "" : ":" + serverPort;
            String baseUrl = String.format("%s://%s%s%s", scheme, serverName, portSegment, contextPath);

            SolicitudPagoDTO solicitud = new SolicitudPagoDTO();
            solicitud.setOrdenCompra("SEAT-" + System.currentTimeMillis() % 1000000000L);
            solicitud.setSessionId(session.getId());
            solicitud.setDescripcion("Cambio Asientos SkyWay - Reserva #" + dto.getIdReserva());

            if (metodo == MetodoPagoEnum.PAYPAL) {
                solicitud.setMoneda("USD");
                double montoUsd = (double) dto.getGranTotalAdicional() / 950.0;
                solicitud.setMonto(BigDecimal.valueOf(montoUsd).setScale(2, RoundingMode.HALF_UP));
                solicitud.setReturnUrl(baseUrl + "/paypal/commit");
                solicitud.setCancelUrl(baseUrl + "/paypal/cancel");
            } else if (metodo == MetodoPagoEnum.MERCADOPAGO) {
                solicitud.setMoneda("CLP");
                solicitud.setMonto(BigDecimal.valueOf(dto.getGranTotalAdicional()));
                solicitud.setReturnUrl(baseUrl + "/mercadopago/commit");
                solicitud.setCancelUrl(baseUrl + "/mercadopago/cancel");
            } else {
                solicitud.setMoneda("CLP");
                solicitud.setMonto(BigDecimal.valueOf(dto.getGranTotalAdicional()));
                solicitud.setReturnUrl(baseUrl + "/webpay/commit");
                solicitud.setCancelUrl(baseUrl + "/webpay/commit");
            }

            PasarelaPagoStrategy pasarela = pagoFactoryService.obtenerEstrategia(metodo);
            String redirectUrl = pasarela.iniciarTransaccion(solicitud);
            Logger.logInfo("🚀 [CAMBIO-ASIENTO] Redirigiendo a pasarela " + metodo + ": " + redirectUrl);

            fc.getExternalContext().redirect(redirectUrl);
            fc.responseComplete();

        } catch (Exception e) {
            Logger.logError("Error al iniciar confirmación/pago de cambio de asiento: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Pago", "No fue posible procesar la solicitud con la pasarela seleccionada: " + e.getMessage()));
        }
    }

    public Integer getIdMetodoPagoSeleccionado() {
        return idMetodoPagoSeleccionado;
    }

    public void setIdMetodoPagoSeleccionado(Integer idMetodoPagoSeleccionado) {
        this.idMetodoPagoSeleccionado = idMetodoPagoSeleccionado;
    }

    public ResultadoCambioAsientoDTO getResultadoCambio() {
        return resultadoCambio;
    }

    public void setResultadoCambio(ResultadoCambioAsientoDTO resultadoCambio) {
        this.resultadoCambio = resultadoCambio;
    }

    public void cerrarDialogoResultado() {
        this.resultadoCambio = null;
    }

    public String getCambio(Integer idAsiento) {
        Optional<InfoAsientoDTO> opt = Optional.ofNullable(asientosCambio.get(idAsiento));
        return opt.map(InfoAsientoDTO::getNumeroAsiento).orElse("");
    }

    public String getEstadoVisualAsiento(InfoAsientoDTO a) {
        if (a == null) return "disponible";

        // 1. Ocupado en base de datos
        if ("ocupado".equalsIgnoreCase(a.getEstado())) {
            return "ocupado";
        }

        // 2. Nuevo asiento seleccionado para algún pasajero del vuelo actual
        for (AsignacionPasajeroDTO asig : asignaciones) {
            if (asig.getNuevoAsiento() != null && asig.getNuevoAsiento().getIdAsiento() == a.getIdAsiento()) {
                return "seleccionado";
            }
        }

        // 3. Asiento original del pasajero en el vuelo actual
        for (AsignacionPasajeroDTO asig : asignaciones) {
            if (asig.getIdAsientoOriginal() != null && asig.getIdAsientoOriginal() == a.getIdAsiento()) {
                return "en_proceso";
            }
        }

        return "disponible";
    }

    // Getters y Setters
    public Vuelo getVuelo() { return vuelo; }
    public void setVuelo(Vuelo vuelo) { this.vuelo = vuelo; }
    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }
    public String getIdVuelo() { return idVuelo; }
    public void setIdVuelo(String idVuelo) { this.idVuelo = idVuelo; }
    public Integer getIdItinerario() { return idItinerario; }
    public void setIdItinerario(Integer idItinerario) { this.idItinerario = idItinerario; }
    public String getOrigenRetorno() { return origenRetorno; }
    public void setOrigenRetorno(String origenRetorno) { this.origenRetorno = origenRetorno; }
    public boolean isPermiteCambio() { return permiteCambio; }
    public void setPermiteCambio(boolean permiteCambio) { this.permiteCambio = permiteCambio; }
    public String getMensajeReglaTarifa() { return mensajeReglaTarifa; }
    public void setMensajeReglaTarifa(String mensajeReglaTarifa) { this.mensajeReglaTarifa = mensajeReglaTarifa; }
    public String getNombreTarifa() { return nombreTarifa; }
    public void setNombreTarifa(String nombreTarifa) { this.nombreTarifa = nombreTarifa; }
    public List<Vuelo> getVuelosReserva() { return vuelosReserva; }
    public List<InfoAsientoDTO> getAsientos() { return asientos; }
    public List<InfoAsientoDTO> getAsientoSeleccionados() { return asientoSeleccionados; }
    public InfoAsientoDTO getAsientoActual() { return asientoActual; }
    public void setAsientoActual(InfoAsientoDTO asientoActual) { this.asientoActual = asientoActual; }
    public List<FilaCabinaDTO> getFilasCabina() { return filasCabina; }

    public List<AsignacionPasajeroDTO> getAsignaciones() { return asignaciones; }
    public AsignacionPasajeroDTO getAsignacionActiva() { return asignacionActiva; }
    public void setAsignacionActiva(AsignacionPasajeroDTO asignacionActiva) { this.asignacionActiva = asignacionActiva; }
    public boolean isEsUsuarioTitular() { return esUsuarioTitular; }

    public boolean isAvionDoblePasillo() { return avionDoblePasillo; }
    public String getNombreModeloAvion() { return nombreModeloAvion; }
    public List<ConfiguracionCabina> getConfiguracionesModelo() { return configuracionesModelo; }

    public int getPorcentajeCargoTarifa() { return porcentajeCargoTarifa; }
    public void setPorcentajeCargoTarifa(int porcentajeCargoTarifa) { this.porcentajeCargoTarifa = porcentajeCargoTarifa; }
}
