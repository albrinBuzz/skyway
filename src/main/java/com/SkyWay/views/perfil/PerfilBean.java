package com.SkyWay.views.perfil;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator.TarifaCaracteristicaValidator;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioResumenDTO;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ETicketPdfGeneratorService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.SkyWay.modules.tarifa.presentation.dto.SolicitudUpgradeDTO;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import jakarta.faces.context.ExternalContext;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.SkyWay.modules.tarifa.domain.service.UpgradeTarifaService;
import com.SkyWay.modules.tarifa.presentation.dto.OpcionUpgradeDTO;
import com.SkyWay.modules.tarifa.presentation.dto.ResultadoUpgradeDTO;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.PasajeroReserva.domain.model.PasajeroReserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.BarcodeGeneratorUtil;
import com.SkyWay.util.BoardingPassSecurityService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Named("perfilPasajeroView")
@ViewScoped
public class PerfilBean implements Serializable {
	private static final long serialVersionUID = 1L;

	@Autowired private ItinerarioService itinerarioService;
	@Autowired private ReservaService reservaService;
	@Autowired private AsientoService asientoService;
	@Autowired private ReservaItinerarioService reservaItinerarioService;
	@Autowired private TarifaCaracteristicaValidator tarifaValidator;
	@Autowired private VueloService vueloService;
	@Autowired private EquipajeService equipajeService;
	@Autowired private ETicketPdfGeneratorService eTicketPdfGeneratorService;
	@Autowired private UpgradeTarifaService upgradeTarifaService;
	@Autowired private PagoFactoryService pagoFactoryService;
	@Autowired private PasajeroService pasajeroService;
	@Autowired private AeropuertoService aeropuertoService;

	private List<OpcionUpgradeDTO> opcionesUpgrade = new ArrayList<>();
	private OpcionUpgradeDTO opcionUpgradeSeleccionada;
	private Integer idMetodoPagoUpgrade = 1;


	private List<ItinerarioResumenDTO> listaItinerarios;
	private List<ItinerarioResumenDTO> listaItinerariosFiltrados;
	private ItinerarioResumenDTO itinerarioSeleccionado;
	private Itinerario itinerarioDetalle;
	private Reserva reservaDetalle;
	private List<InfoAsientoReservaDTO> listaAsientosReservados;
	private List<Equipaje> listaEquipajeReserva;

	// Multi-pasajeros de la reserva
	private final Map<String, PasajeroReservaInfo> pasajerosReservaMap = new LinkedHashMap<>();
	private final List<PasajeroReservaInfo> listaPasajerosReserva = new ArrayList<>();

	// Métricas y colecciones estilo Delta OmniPro
	private Pasajero pasajeroLogueado;
	private List<ItinerarioResumenDTO> listaItinerariosHoy = new ArrayList<>();
	private List<ItinerarioResumenDTO> listaItinerariosFuturos = new ArrayList<>();
	private List<ItinerarioResumenDTO> listaItinerariosPasados = new ArrayList<>();
	private List<AcompananteDTO> acompanantesHabituales = new ArrayList<>();

	private final Map<Integer, String> pnrCache = new HashMap<>();
	private final Map<Integer, String> asientosReservaCache = new HashMap<>();
	private final Map<Integer, Integer> cantidadPasajerosCache = new HashMap<>();
	private final Map<Integer, String> nombresPasajerosCache = new HashMap<>();
	private final Map<String, String> ciudadIataCache = new HashMap<>();
	private final Map<Integer, Reserva> reservaEntityCache = new HashMap<>();
	private final Map<Integer, String> equipajeResumenCache = new HashMap<>();
	private final Map<Integer, ItinerarioDetalleDTO> itinerarioDetalleCache = new HashMap<>();
	private final Map<String, String> puertaEmbarqueCache = new HashMap<>();

	// Schedule nativo PrimeFaces para itinerarios
	private ScheduleModel scheduleModel = new DefaultScheduleModel();
	private String vistaActiva = "calendario"; // "calendario" o "lista"
	private LocalDate fechaInicialSchedule;

	private LocalDate fechaInicioFiltro;
	private LocalDate fechaFinFiltro;
	private List<TicketInfo> ticketInfos;
	private Tarifa tarifaActual;
	private List<TarifaCaracteristica> tarifaCaracteristicas;
	private Usuario usuario;

	private String motivoCancelacion;
	private boolean reservaCancelada;

	// Métricas para la vista del modal
	private String estadoVueloVisual;
	private String estadoVueloBadgeClass;
	private String estadoVueloIcono;
	private String tiempoRestanteFormateado;
	private long segundosTotalesSalida;
	private boolean checkInAbierto;
	private boolean checkInCompletado;
	private double porcentajeCheckIn;
	private String estadoCheckInTexto;
	private String cintaEquipaje;
	private int etapaActualCronograma;

	private final Map<Integer, String> numeroVueloCache = new HashMap<>();

	private ResultadoUpgradeDTO resultadoUpgrade;
	private boolean mostrarModalResultadoUpgrade = false;

	@PostConstruct
	public void init() {
		long startTime = System.currentTimeMillis();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null && facesContext.getExternalContext() != null) {
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
			if (session != null) {
				usuario = (Usuario) session.getAttribute("usuario");
			}
		}

		if (usuario != null) {
			if (pasajeroService != null) {
				this.pasajeroLogueado = pasajeroService.findById(usuario.getRut()).orElse(null);
			}

			boolean esPasajero = false;
			if (usuario.getRoles() != null) {
				for (Role role : usuario.getRoles()) {
					if ("Pasajero".equalsIgnoreCase(role.getNombre())) {
						esPasajero = true;
						break;
					}
				}
			} else {
				esPasajero = true;
			}

			if (esPasajero) {
				listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);
				filtrarVuelosFuturos();
				actualizarSchedule();
				inicializarMetricasDelta();
			}
		}

		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en init(): " + (endTime - startTime) + " ms");

		if (facesContext != null && facesContext.getExternalContext() != null) {
			Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
			Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

			if (params.containsKey("upgradeSuccess")) {
				if (sessionMap.containsKey("RESULTADO_UPGRADE_CONFIRMADO")) {
					this.resultadoUpgrade = (ResultadoUpgradeDTO) sessionMap.get("RESULTADO_UPGRADE_CONFIRMADO");
					sessionMap.remove("RESULTADO_UPGRADE_CONFIRMADO");
				}
				this.mostrarModalResultadoUpgrade = true;

				facesContext.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_INFO, "¡Ascenso de Tarifa Exitoso!",
								"Tu categoría de vuelo ha sido actualizada exitosamente a " +
										(this.resultadoUpgrade != null ? this.resultadoUpgrade.getTarifaNueva() : "una categoría superior") +
										". Disfruta de tus nuevos beneficios."));
			} else if (params.containsKey("upgradeCancelled")) {
				facesContext.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Ascenso Cancelado",
								"El proceso de pago del ascenso de tarifa fue cancelado en la pasarela."));
			}
		}
	}

	public void filtrarVuelosHoy() {
		if (usuario == null) return;
		LocalDate hoy = LocalDate.now();
		listaItinerariosFiltrados = itinerarioService.buscarConFiltroFechas(
				usuario.getRut(),
				hoy,
				hoy,
				100,
				0
		);
		actualizarSchedule();
	}

	public void filtrarVuelosFuturos() {
		if (listaItinerarios == null) return;
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida() != null && it.getHoraSalida().after(ahora))
				.collect(Collectors.toList());
		actualizarSchedule();
	}

	public void filtrarVuelosPasados() {
		if (listaItinerarios == null) return;
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida() != null && it.getHoraSalida().before(ahora))
				.collect(Collectors.toList());
		actualizarSchedule();
	}

	public void buscarPorRangoFechas() {
		if (usuario == null) return;

		LocalDate inicio = (fechaInicioFiltro != null) ? fechaInicioFiltro : null;
		LocalDate fin = (fechaFinFiltro != null) ? fechaFinFiltro : null;

		long startTime = System.currentTimeMillis();

		listaItinerariosFiltrados = itinerarioService.buscarConFiltroFechas(
				usuario.getRut(),
				inicio,
				fin,
				100,
				0
		);

		actualizarSchedule();

		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en buscarPorRangoFechas(): " + (endTime - startTime) + " ms");
	}

	public void limpiarFiltroFechas() {
		this.fechaInicioFiltro = null;
		this.fechaFinFiltro = null;
		if (usuario != null) {
			this.listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);
			filtrarVuelosFuturos();
			actualizarSchedule();
			inicializarMetricasDelta();
		}
	}

	public void verDetalleItinerario(ItinerarioResumenDTO itinerario) {
		if (itinerario == null) return;
		this.itinerarioDetalle = itinerarioService.findById(itinerario.getIdItinerario());
	}

	public void verAsientoItinerario(ItinerarioResumenDTO itinerario) {
		if (itinerario == null) return;

		this.itinerarioSeleccionado = itinerario;
		listaAsientosReservados = asientoService.getAsientosReservados(itinerario.getIdItinerario(), itinerario.getIdReserva());

		ReservaItinerario reservaItinerario = reservaItinerarioService.obtenerReservaItinerario(itinerario.getIdReserva(), itinerario.getIdItinerario());

		if (reservaItinerario != null && reservaItinerario.getItinerarioTarifa() != null) {
			tarifaActual = reservaItinerario.getItinerarioTarifa().getTarifa();
			if (tarifaActual != null) {
				tarifaCaracteristicas = tarifaActual.getTarifaCaracteristicas();
			}
		}
	}

	public void verDetalleCompletoReserva(ItinerarioResumenDTO item) {
		if (item == null) return;
		this.itinerarioSeleccionado = item;
		this.itinerarioDetalle = itinerarioService.findById(item.getIdItinerario());

		ReservaItinerario ri = reservaItinerarioService.obtenerReservaItinerario(item.getIdReserva(), item.getIdItinerario());
		if (item.getIdReserva() != null) {
			this.reservaDetalle = reservaService.findById(item.getIdReserva()).orElse(ri != null ? ri.getReserva() : null);
		} else if (ri != null && ri.getReserva() != null) {
			this.reservaDetalle = ri.getReserva();
		} else {
			this.reservaDetalle = null;
		}

		if (this.reservaDetalle != null && this.reservaDetalle.getEstadoReservaBean() != null) {
			String estadoNombre = this.reservaDetalle.getEstadoReservaBean().getDescripcion();
			this.reservaCancelada = estadoNombre != null && estadoNombre.toLowerCase().contains("cancelad");
		} else {
			this.reservaCancelada = false;
		}

		listaAsientosReservados = asientoService.getAsientosReservados(item.getIdItinerario(), item.getIdReserva());

		if (ri != null && ri.getItinerarioTarifa() != null) {
			tarifaActual = ri.getItinerarioTarifa().getTarifa();
			if (tarifaActual != null) {
				tarifaCaracteristicas = tarifaActual.getTarifaCaracteristicas();
			}
		}

		try {
			listaEquipajeReserva = equipajeService.buscarPorReserva(item.getIdReserva());
		} catch (Exception e) {
			listaEquipajeReserva = new ArrayList<>();
		}

		cargarPasajerosReserva();

		calcularMétricasVuelo(item);
	}

	public void cambiarAsiento(InfoAsientoReservaDTO infoAsientoDTO) throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();

		// Si no están cargadas las características, cargarlas desde la reserva del DTO
		if (tarifaCaracteristicas == null && infoAsientoDTO != null && infoAsientoDTO.getIdReserva() > 0) {
			try {
				int resId = infoAsientoDTO.getIdReserva();
				Optional<Reserva> rOpt = reservaService.findById(resId);
				if (rOpt.isPresent() && rOpt.get().getReservaItinerarios() != null) {
					for (ReservaItinerario ri : rOpt.get().getReservaItinerarios()) {
						if (ri.getItinerarioTarifa() != null && ri.getItinerarioTarifa().getTarifa() != null) {
							tarifaActual = ri.getItinerarioTarifa().getTarifa();
							tarifaCaracteristicas = tarifaActual.getTarifaCaracteristicas();
							break;
						}
					}
				}
			} catch (Exception e) {
				Logger.logError("Error al cargar tarifa en PerfilBean.cambiarAsiento: " + e.getMessage());
			}
		}

		if (tarifaCaracteristicas == null || !tarifaValidator.permiteCambio(tarifaCaracteristicas)) {
			Logger.logInfo("La tarifa no permite cambios para la reserva/itinerario actual.");
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Cambio No Permitido",
							"La tarifa básica no permite cambios de asiento."));
			return;
		}

		if (itinerarioDetalle != null && itinerarioDetalle.getHoraSalida() != null) {
			LocalDateTime fechaSalida = LocalDateTime.ofInstant(itinerarioDetalle.getHoraSalida().toInstant(), ZoneId.systemDefault());
			long horasRestantes = Duration.between(LocalDateTime.now(), fechaSalida).toHours();

			if (!tarifaValidator.cumpleHorasMinimasCambio(tarifaCaracteristicas, (int) horasRestantes)) {
				Integer hMin = tarifaValidator.obtenerHorasMinimasCambio(tarifaCaracteristicas);
				String tName = (tarifaActual != null && tarifaActual.getNombre() != null) ? tarifaActual.getNombre().toLowerCase(java.util.Locale.ROOT) : "";
				int h = (hMin != null && hMin > 0) ? hMin : (tName.contains("flex") ? 48 : 24);
				String msg;
				if (tName.contains("flex")) {
					msg = "Solo puedes cambiar el asiento hasta 48 horas antes del vuelo con tarifa flexible.";
				} else if (tName.contains("prem")) {
					msg = "Solo puedes cambiar el asiento hasta 24 horas antes del vuelo con tarifa premium.";
				} else {
					msg = "Solo puedes cambiar el asiento hasta " + h + " horas antes del vuelo con tu tarifa.";
				}
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"Plazo Expirado",
								msg));
				return;
			}
		}

		Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
		sessionMap.put("CAMBIO_ASIENTO_ID_RESERVA", infoAsientoDTO.getIdReserva());
		sessionMap.put("CAMBIO_ASIENTO_ID_VUELO", infoAsientoDTO.getIdVuelo());
		if (itinerarioSeleccionado != null) {
			sessionMap.put("CAMBIO_ASIENTO_ID_ITINERARIO", itinerarioSeleccionado.getIdItinerario());
		}
		PasajeroReservaInfo pInfo = getPasajeroParaAsiento(infoAsientoDTO);
		if (pInfo != null && pInfo.getRut() != null) {
			sessionMap.put("CAMBIO_ASIENTO_RUT_PASAJERO", pInfo.getRut());
		}
		sessionMap.put("CAMBIO_ASIENTO_ORIGEN", "PERFIL");

		context.getExternalContext()
				.redirect(context.getExternalContext().getRequestContextPath() + "/home/cambio-asiento.xhtml");
	}


	public String getNumeroVuelo(int idVuelo) {
		return numeroVueloCache.computeIfAbsent(idVuelo, id ->
				vueloService.findById(id)
						.map(Vuelo::getNumeroVuelo)
						.orElse("N/A")
		);
	}

	public void procesarCancelacion() {
		if (itinerarioSeleccionado == null || usuario == null) return;

		if (reservaCancelada) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Reserva Ya Cancelada", "Esta reserva ya fue procesada y cancelada previamente."));
			return;
		}

		try {
			reservaService.cancelarReserva(itinerarioSeleccionado.getIdReserva(), usuario.getRut(), motivoCancelacion);

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Reserva Cancelada", "La reserva N° #" + itinerarioSeleccionado.getIdReserva() + " ha sido cancelada exitosamente."));

			listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);
			filtrarVuelosFuturos();
			actualizarSchedule();

		} catch (Exception e) {
			Logger.logError("Error al cancelar la reserva: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
		}
	}

	public void calcularMétricasVuelo(ItinerarioResumenDTO item) {
		if (item == null || item.getHoraSalida() == null) return;

		LocalDateTime ahora = LocalDateTime.now();
		LocalDateTime salida = LocalDateTime.ofInstant(item.getHoraSalida().toInstant(), ZoneId.systemDefault());

		LocalDateTime llegada = (item.getHoraLlegada() != null)
				? LocalDateTime.ofInstant(item.getHoraLlegada().toInstant(), ZoneId.systemDefault())
				: salida.plusHours(2);

		Duration duracionHastaSalida = Duration.between(ahora, salida);
		long segundosHastaSalida = duracionHastaSalida.getSeconds();
		this.segundosTotalesSalida = segundosHastaSalida;

		if (ahora.isAfter(salida)) {
			this.etapaActualCronograma = 4; // Vuelo en progreso o Aterrizado
		} else if (segundosHastaSalida <= 1200) { // Menos de 20 minutos
			this.etapaActualCronograma = 3; // Puerta de Embarque
		} else if (segundosHastaSalida <= 3600) { // Entre 60 y 20 minutos
			this.etapaActualCronograma = 2; // Entrega de Equipaje
		} else {
			this.etapaActualCronograma = 1; // Check-In Web
		}

		int hashId = Math.abs(Integer.valueOf(item.getIdItinerario()).hashCode());

		// A) ESTADO DEL VUELO
		if (ahora.isAfter(llegada)) {
			this.estadoVueloVisual = "Aterrizado";
			this.estadoVueloBadgeClass = "bg-secondary";
			this.estadoVueloIcono = "pi-check-circle";
			this.tiempoRestanteFormateado = "Vuelo Finalizado";
			this.cintaEquipaje = "Cinta N° " + ((hashId % 6) + 1);

		} else if (ahora.isAfter(salida) && ahora.isBefore(llegada)) {
			this.estadoVueloVisual = "En Vuelo";
			this.estadoVueloBadgeClass = "bg-info text-dark";
			this.estadoVueloIcono = "pi-send";

			Duration duracionLlegada = Duration.between(ahora, llegada);
			this.tiempoRestanteFormateado = String.format("Aterriza en %dh %02dm %02ds",
					duracionLlegada.toHours(), duracionLlegada.toMinutesPart(), duracionLlegada.toSecondsPart());
			this.cintaEquipaje = "Por Asignar en Destino";

		} else if (segundosHastaSalida <= 900 && segundosHastaSalida > 0) {
			this.estadoVueloVisual = "Última Llamada";
			this.estadoVueloBadgeClass = "bg-danger text-white";
			this.estadoVueloIcono = "pi-exclamation-triangle";
			this.tiempoRestanteFormateado = String.format("%02dm %02ds", duracionHastaSalida.toMinutesPart(), duracionHastaSalida.toSecondsPart());
			this.cintaEquipaje = "Despacho Cerrado";

		} else if (segundosHastaSalida <= 2700 && segundosHastaSalida > 900) {
			this.estadoVueloVisual = "Embarcando";
			this.estadoVueloBadgeClass = "bg-warning text-dark font-weight-bold";
			this.estadoVueloIcono = "pi-users";
			this.tiempoRestanteFormateado = String.format("%02dm %02ds", duracionHastaSalida.toMinutesPart(), duracionHastaSalida.toSecondsPart());
			this.cintaEquipaje = "Despacho Cerrado";

		} else {
			this.estadoVueloVisual = "A Tiempo";
			this.estadoVueloBadgeClass = "bg-success";
			this.estadoVueloIcono = "pi-check-circle";

			long dias = duracionHastaSalida.toDays();
			long horas = duracionHastaSalida.toHoursPart();
			long minutos = duracionHastaSalida.toMinutesPart();
			long segundos = duracionHastaSalida.toSecondsPart();

			if (dias > 0) {
				this.tiempoRestanteFormateado = String.format("%dd %02dh %02dm %02ds", dias, horas, minutos, segundos);
			} else {
				this.tiempoRestanteFormateado = String.format("%02dh %02dm %02ds", horas, minutos, segundos);
			}

			this.cintaEquipaje = (duracionHastaSalida.toHours() <= 24)
					? "Cinta N° " + ((hashId % 6) + 1)
					: "Se asigna 24h antes";
		}

		// B) CRONOGRAMA DE CHECK-IN
		boolean checkInYaCompletado = (itinerarioSeleccionado.getIdReserva() != null)
				&& reservaService.tieneCheckInCompletado(itinerarioSeleccionado.getIdReserva());
		this.checkInCompletado = checkInYaCompletado;

		long horasHastaSalida = duracionHastaSalida.toHours();

		if (checkInYaCompletado) {
			this.checkInAbierto = true;
			this.estadoCheckInTexto = "CHECK-IN COMPLETADO";
			this.porcentajeCheckIn = 100.0;
			this.etapaActualCronograma = 2;
		} else if (horasHastaSalida > 48) {
			this.checkInAbierto = false;
			Date fechaApertura = getFechaAperturaCheckIn(itinerarioSeleccionado.getHoraSalida());
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			this.estadoCheckInTexto = "Abre el " + (fechaApertura != null ? sdf.format(fechaApertura) + " hrs" : String.format("Abre en %d días", (horasHastaSalida - 48) / 24 + 1));
			this.porcentajeCheckIn = 15.0;
			this.etapaActualCronograma = 1;
		} else if (horasHastaSalida >= 1 && segundosHastaSalida > 0) {
			this.checkInAbierto = true;
			this.estadoCheckInTexto = "CHECK-IN ABIERTO";
			double horasTranscurridas = 48.0 - (segundosHastaSalida / 3600.0);
			this.porcentajeCheckIn = Math.min(100.0, Math.max(20.0, (horasTranscurridas / 47.0) * 100.0));
			this.etapaActualCronograma = 1;
		} else {
			this.checkInAbierto = false;
			this.estadoCheckInTexto = "CHECK-IN CERRADO";
			this.porcentajeCheckIn = 100.0;
			this.etapaActualCronograma = 3;
		}
	}

	public boolean isReservaCanceladaPorItinerario(Integer idReserva) {
		if (idReserva == null) {
			return false;
		}
		try {
			Optional<Reserva> opt = reservaService.findById(idReserva);
			if (opt.isPresent()) {
				Reserva reserva = opt.get();
				if (reserva.getEstadoReservaBean() != null && reserva.getEstadoReservaBean().getDescripcion() != null) {
					return reserva.getEstadoReservaBean().getDescripcion().toLowerCase().contains("cancelad");
				}
			}
		} catch (Exception e) {
			Logger.logError("Error al verificar estado de la reserva #" + idReserva + ": " + e.getMessage());
		}

		return false;
	}

	public void irACheckIn() {
		if (itinerarioSeleccionado == null) return;
		irACheckIn(itinerarioSeleccionado);
	}

	public void irACheckIn(ItinerarioResumenDTO it) {
		if (it == null) return;
		this.itinerarioSeleccionado = it;
		this.idReservaSeleccionada = it.getIdReserva();

		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null && context.getExternalContext() != null) {
			Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
			sessionMap.put("CHECKIN_ID_RESERVA", it.getIdReserva());
			sessionMap.put("CHECKIN_ID_ITINERARIO", it.getIdItinerario());

			String contextPath = context.getExternalContext().getRequestContextPath();
			String url = contextPath + "/home/checkin.xhtml";

			try {
				if (PrimeFaces.current() != null && PrimeFaces.current().isAjaxRequest()) {
					PrimeFaces.current().executeScript("window.location.href = '" + url + "';");
				} else {
					context.getExternalContext().redirect(url);
					context.responseComplete();
				}
			} catch (Exception e) {
				Logger.logError("Error redirigiendo a /home/checkin.xhtml: " + e.getMessage());
			}
		}
	}

	public StreamedContent descargarComprobantePdf() {
		if (this.idReservaSeleccionada == null && this.itinerarioSeleccionado != null) {
			this.idReservaSeleccionada = this.itinerarioSeleccionado.getIdReserva();
		}
		Logger.logInfo("📄 [PDF RESUMEN] Solicitando descarga para idReservaSeleccionada: " + this.idReservaSeleccionada);
		return descargarComprobantePdf(this.idReservaSeleccionada);
	}

	public StreamedContent descargarETicketPdf() {
		return descargarComprobantePdf();
	}

	public StreamedContent descargarETicketPdf(Integer idReserva) {
		return descargarComprobantePdf(idReserva);
	}

	public StreamedContent descargarBoardingPassesPdf() {
		if (this.idReservaSeleccionada == null && this.itinerarioSeleccionado != null) {
			this.idReservaSeleccionada = this.itinerarioSeleccionado.getIdReserva();
		}
		Logger.logInfo("🎟️ [PDF BOARDING PASSES] Solicitando descarga para idReservaSeleccionada: " + this.idReservaSeleccionada);

		// 1. Si ya tenemos ticketInfos cargados en memoria (desde getTicket), generar el PDF directamente
		if (this.ticketInfos != null && !this.ticketInfos.isEmpty()) {
			try {
				byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(this.ticketInfos);
				if (pdfBytes != null && pdfBytes.length > 0) {
					String pnr = this.ticketInfos.get(0).getCodigoPnr();
					Logger.logInfo("✅ [PDF BOARDING PASSES] PDF generado directamente desde ticketInfos en memoria (" + pdfBytes.length + " bytes).");
					return DefaultStreamedContent.builder()
							.name("Tarjetas_Embarque_SkyWay_" + (pnr != null ? pnr : "Viaje") + ".pdf")
							.contentType("application/pdf")
							.stream(() -> new ByteArrayInputStream(pdfBytes))
							.build();
				}
			} catch (Exception e) {
				Logger.logError("❌ [PDF BOARDING PASSES] Error generando PDF desde ticketInfos: " + e.getMessage());
			}
		}

		// 2. Si no hay ticketInfos en memoria, verificar idReservaSeleccionada y check-in
		if (this.idReservaSeleccionada == null || !reservaService.tieneCheckInCompletado(this.idReservaSeleccionada)) {
			FacesContext context = FacesContext.getCurrentInstance();
			if (context != null) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Check-In Requerido", "Debes completar el Check-In Online antes de descargar tus tarjetas de embarque."));
			}
			return null;
		}

		return descargarBoardingPassesPdf(this.idReservaSeleccionada);
	}


	/**
	 * Descarga nativa del PDF 1: Resumen de Reserva e Itinerario (con trazas de log)
	 */
	public StreamedContent descargarComprobantePdf(Integer idReserva) {
		if (idReserva == null) {
			idReserva = this.idReservaSeleccionada;
		}
		if (idReserva == null && this.itinerarioSeleccionado != null) {
			idReserva = this.itinerarioSeleccionado.getIdReserva();
		}
		Logger.logInfo("📄 [PDF E-TICKET] Solicitando descarga para idReserva: " + idReserva);

		if (idReserva == null) {
			Logger.logWarn("⚠️ [PDF E-TICKET] idReserva recibido es NULL. Se cancela la descarga.");
			return null;
		}

		if (isReservaCanceladaPorItinerario(idReserva)) {
			Logger.logWarn("⚠️ [PDF E-TICKET] Intento de descarga para reserva cancelada ID: " + idReserva);
			FacesContext context = FacesContext.getCurrentInstance();
			if (context != null) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Reserva Cancelada", "El E-Ticket no se encuentra disponible para reservas canceladas."));
			}
			return null;
		}

		try {
			Optional<Reserva> optReserva = reservaService.findById(idReserva);
			if (optReserva.isEmpty()) {
				Logger.logWarn("⚠️ [PDF E-TICKET] No se encontró la entidad Reserva para ID: " + idReserva);
				return null;
			}

			Reserva reserva = optReserva.get();
			String pnr = reserva.getCodigoReserva() != null ? reserva.getCodigoReserva() : ("R" + reserva.getIdReserva());
			Logger.logInfo("📄 [PDF E-TICKET] Generando bytes de PDF para PNR: " + pnr);

			byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfResumenReserva(reserva);

			if (pdfBytes == null || pdfBytes.length == 0) {
				Logger.logError("❌ [PDF E-TICKET] Los bytes del PDF generados están vacíos o nulos.");
				return null;
			}

			Logger.logInfo("✅ [PDF E-TICKET] PDF generado correctamente (" + pdfBytes.length + " bytes). Iniciando descarga StreamedContent...");

			return DefaultStreamedContent.builder()
					.name("ETicket_SkyWay_" + pnr + ".pdf")
					.contentType("application/pdf")
					.stream(() -> new ByteArrayInputStream(pdfBytes))
					.build();

		} catch (Exception e) {
			Logger.logError("❌ [PDF E-TICKET] Excepción inesperada al descargar comprobante: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}



	/**
	 * Descarga nativa del PDF 2: Tarjetas de Embarque / Boarding Passes (con trazas de log)
	 */
	public StreamedContent descargarBoardingPassesPdf(Integer idReserva) {
		if (idReserva == null) {
			idReserva = this.idReservaSeleccionada;
		}
		if (idReserva == null && this.itinerarioSeleccionado != null) {
			idReserva = this.itinerarioSeleccionado.getIdReserva();
		}
		Logger.logInfo("🎟️ [PDF BOARDING PASSES] Solicitando descarga para idReserva: " + idReserva);

		if (idReserva == null) {
			Logger.logWarn("⚠️ [PDF BOARDING PASSES] idReserva recibido es NULL. Se cancela la descarga.");
			return null;
		}

		if (!reservaService.tieneCheckInCompletado(idReserva)) {
			Logger.logWarn("⚠️ [PDF BOARDING PASSES] Intento de descarga sin Check-In completado para reserva ID: " + idReserva);
			FacesContext context = FacesContext.getCurrentInstance();
			if (context != null) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Check-In Requerido", "Debes completar el Check-In Online antes de descargar tus tarjetas de embarque."));
			}
			return null;
		}

		try {
			// Si ya tenemos ticketInfos cargados para esta reserva, usarlos directamente
			if (this.ticketInfos != null && !this.ticketInfos.isEmpty() && idReserva.equals(this.idReservaSeleccionada)) {
				byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(this.ticketInfos);
				if (pdfBytes != null && pdfBytes.length > 0) {
					String pnr = this.ticketInfos.get(0).getCodigoPnr();
					return DefaultStreamedContent.builder()
							.name("Tarjetas_Embarque_SkyWay_" + (pnr != null ? pnr : "Viaje") + ".pdf")
							.contentType("application/pdf")
							.stream(() -> new ByteArrayInputStream(pdfBytes))
							.build();
				}
			}

			// Intentar cargar tickets vía reservaService si se tiene el contexto del itinerario
			if (usuario != null && itinerarioSeleccionado != null && idReserva.equals(itinerarioSeleccionado.getIdReserva())) {
				List<TicketInfo> tickets = reservaService.getTicket(usuario.getRut(), idReserva, itinerarioSeleccionado.getIdItinerario());
				if (tickets != null && !tickets.isEmpty()) {
					byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(tickets);
					if (pdfBytes != null && pdfBytes.length > 0) {
						String pnr = tickets.get(0).getCodigoPnr();
						return DefaultStreamedContent.builder()
								.name("Tarjetas_Embarque_SkyWay_" + (pnr != null ? pnr : "Viaje") + ".pdf")
								.contentType("application/pdf")
								.stream(() -> new ByteArrayInputStream(pdfBytes))
								.build();
					}
				}
			}

			Optional<Reserva> optReserva = reservaService.findById(idReserva);
			if (optReserva.isEmpty()) {
				Logger.logWarn("⚠️ [PDF BOARDING PASSES] No se encontró la entidad Reserva para ID: " + idReserva);
				return null;
			}

			Reserva reserva = optReserva.get();
			Logger.logInfo("🎟️ [PDF BOARDING PASSES] Generando tarjetas de embarque para PNR: " + reserva.getCodigoReserva());

			byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(reserva);

			if (pdfBytes == null || pdfBytes.length == 0) {
				Logger.logError("❌ [PDF BOARDING PASSES] Los bytes del PDF generados están vacíos o nulos.");
				return null;
			}

			Logger.logInfo("✅ [PDF BOARDING PASSES] PDF generado correctamente (" + pdfBytes.length + " bytes). Iniciando descarga StreamedContent...");

			return DefaultStreamedContent.builder()
					.name("Tarjetas_Embarque_SkyWay_" + reserva.getCodigoReserva() + ".pdf")
					.contentType("application/pdf")
					.stream(() -> new ByteArrayInputStream(pdfBytes))
					.build();

		} catch (Exception e) {
			Logger.logError("❌ [PDF BOARDING PASSES] Excepción inesperada al descargar pases de abordar: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void getTicket(ItinerarioResumenDTO itinerario) {
		if (itinerario == null || usuario == null) return;

		// 1. Validar cancelación
		if (isReservaCanceladaPorItinerario(itinerario.getIdReserva())) {
			FacesContext context = FacesContext.getCurrentInstance();
			if (context != null) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Reserva Cancelada", "No es posible acceder a las tarjetas de una reserva cancelada."));
			}
			this.ticketInfos = null;
			PrimeFaces pf = PrimeFaces.current();
			if (pf != null) pf.ajax().addCallbackParam("checkInInvalido", true);
			return;
		}

		// 2. Validar que el Check-In esté completado
		boolean checkInListo = reservaService.tieneCheckInCompletado(itinerario.getIdReserva());
		if (!checkInListo) {
			FacesContext context = FacesContext.getCurrentInstance();
			Date fechaApertura = getFechaAperturaCheckIn(itinerario.getHoraSalida());
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy 'a las' HH:mm 'hrs'");
			String detalleFecha = (fechaApertura != null) ? " Podrás realizar tu Check-In a partir del " + sdf.format(fechaApertura) + "." : "";

			if (context != null) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Check-In Requerido",
						"Debes completar el Check-In Online para acceder a tus Tarjetas de Embarque." + detalleFecha));
			}
			this.ticketInfos = null;
			PrimeFaces pf = PrimeFaces.current();
			if (pf != null) pf.ajax().addCallbackParam("checkInInvalido", true);
			return;
		}

		this.itinerarioSeleccionado = itinerario;
		this.idReservaSeleccionada = itinerario.getIdReserva();
		this.checkInCompletado = true;
		ticketInfos = reservaService.getTicket(usuario.getRut(), itinerario.getIdReserva(), itinerario.getIdItinerario());
		if (ticketInfos != null) {
			for (TicketInfo t : ticketInfos) {
				t.setCodigoQr(BoardingPassSecurityService.generarCodigoSeguro(
						t.getCodigoPnr(), t.getNumeroVuelo(), t.getNumeroAsiento(), usuario.getRut()
				));
			}
		}
	}

	public boolean isCheckInCompletado(Integer idReserva) {
		if (idReserva == null) return false;
		return reservaService.tieneCheckInCompletado(idReserva);
	}

	public boolean isCheckInDisponible(Date horaSalida) {
		if (horaSalida == null) return false;
		long diffMillis = horaSalida.getTime() - System.currentTimeMillis();
		return diffMillis <= (48L * 3600 * 1000) && diffMillis >= (3600 * 1000);
	}

	public Date getFechaAperturaCheckIn(Date horaSalida) {
		if (horaSalida == null) return null;
		return new Date(horaSalida.getTime() - (48L * 3600 * 1000));
	}

	public String getTextoEstadoCheckIn(ItinerarioResumenDTO it) {
		if (it == null) return "";
		if (isReservaCanceladaPorItinerario(it.getIdReserva())) {
			return "Reserva Cancelada";
		}
		if (isCheckInCompletado(it.getIdReserva())) {
			return "Check-In Completado";
		}
		if (it.getHoraSalida() == null) {
			return "Check-In Pendiente";
		}
		long diffMillis = it.getHoraSalida().getTime() - System.currentTimeMillis();
		if (diffMillis < (3600 * 1000)) {
			return "Check-In Cerrado";
		}
		if (diffMillis <= (48L * 3600 * 1000)) {
			return "Check-In Abierto";
		}
		Date apertura = getFechaAperturaCheckIn(it.getHoraSalida());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return "Check-In disponible desde el " + sdf.format(apertura) + " hrs";
	}

	public boolean isCheckInCompletado() {
		if (this.idReservaSeleccionada != null) {
			return reservaService.tieneCheckInCompletado(this.idReservaSeleccionada);
		}
		return checkInCompletado;
	}

	public void setCheckInCompletado(boolean checkInCompletado) {
		this.checkInCompletado = checkInCompletado;
	}

	/**
	 * Genera el Data URL del código de barras 2D PDF417 (IATA BCBP) para renderizar en la vista web
	 * de manera local y autónoma sin depender de APIs externas.
	 */
	public String obtenerCodigoBarrasBase64(String codigo) {
		return BarcodeGeneratorUtil.generarPdf417DataUrl(codigo);
	}

	private Integer idReservaSeleccionada;

	// Getters y Setters
	public LocalDate getFechaInicioFiltro() { return fechaInicioFiltro; }
	public void setFechaInicioFiltro(LocalDate fechaInicioFiltro) { this.fechaInicioFiltro = fechaInicioFiltro; }

	public LocalDate getFechaFinFiltro() { return fechaFinFiltro; }
	public void setFechaFinFiltro(LocalDate fechaFinFiltro) { this.fechaFinFiltro = fechaFinFiltro; }

	public Usuario getUsuario() { return usuario; }
	public boolean isReservaCancelada() { return reservaCancelada; }
	public List<ItinerarioResumenDTO> getListaItinerarios() { return listaItinerarios; }
	public List<ItinerarioResumenDTO> getListaItinerariosFiltrados() { return listaItinerariosFiltrados; }
	public ItinerarioResumenDTO getItinerarioSeleccionado() { return itinerarioSeleccionado; }
	public Itinerario getItinerarioDetalle() { return itinerarioDetalle; }
	public List<InfoAsientoReservaDTO> getListaAsientosReservados() { return listaAsientosReservados; }
	public List<Equipaje> getListaEquipajeReserva() { return listaEquipajeReserva; }
	public List<TicketInfo> getTicketInfos() { return ticketInfos; }
	public Tarifa getTarifaActual() { return tarifaActual; }
	public List<TarifaCaracteristica> getTarifaCaracteristicas() { return tarifaCaracteristicas; }

	public Integer getIdReservaSeleccionada() { return idReservaSeleccionada; }
	public void setIdReservaSeleccionada(Integer idReservaSeleccionada) { this.idReservaSeleccionada = idReservaSeleccionada; }

	public String getMotivoCancelacion() { return motivoCancelacion; }
	public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }

	public String getEstadoVueloVisual() { return estadoVueloVisual; }
	public String getEstadoVueloBadgeClass() { return estadoVueloBadgeClass; }
	public String getEstadoVueloIcono() { return estadoVueloIcono; }
	public String getTiempoRestanteFormateado() { return tiempoRestanteFormateado; }
	public long getSegundosTotalesSalida() { return segundosTotalesSalida; }
	public boolean isCheckInAbierto() { return checkInAbierto; }
	public double getPorcentajeCheckIn() { return porcentajeCheckIn; }
	public String getEstadoCheckInTexto() { return estadoCheckInTexto; }
	public String getCintaEquipaje() { return cintaEquipaje; }

	// ==========================================
	// GESTIÓN DE ASCENSO DE TARIFA (FARE UPGRADE)
	// ==========================================

	public void prepararUpgrade(ItinerarioResumenDTO it) {
		if (it == null) return;
		this.itinerarioSeleccionado = it;
		this.opcionesUpgrade = upgradeTarifaService.obtenerOpcionesUpgrade(it.getIdReserva(), it.getIdItinerario());
		if (this.opcionesUpgrade != null && !this.opcionesUpgrade.isEmpty()) {
			this.opcionUpgradeSeleccionada = this.opcionesUpgrade.get(0);
		} else {
			this.opcionUpgradeSeleccionada = null;
		}
	}

	public void seleccionarOpcionUpgrade(OpcionUpgradeDTO opcion) {
		this.opcionUpgradeSeleccionada = opcion;
	}

	public void ejecutarUpgrade() {
		if (itinerarioSeleccionado == null || opcionUpgradeSeleccionada == null) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Selección Requerida", "Debes seleccionar una tarifa superior para ascender."));
			return;
		}

		try {
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
			HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
			HttpSession session = (HttpSession) externalContext.getSession(true);

			// 1. Guardar Solicitud de Upgrade en Sesión
			SolicitudUpgradeDTO upgradeDto = new SolicitudUpgradeDTO(
					itinerarioSeleccionado.getIdReserva(),
					itinerarioSeleccionado.getIdItinerario(),
					opcionUpgradeSeleccionada.getIdItinerarioTarifaNuevo(),
					opcionUpgradeSeleccionada.getDiferenciaAPagar(),
					opcionUpgradeSeleccionada.getNombreTarifa(),
					"perfil",
					idMetodoPagoUpgrade
			);
			session.setAttribute("SOLICITUD_UPGRADE_PENDIENTE", upgradeDto);

			// 2. Determinar método de pago (1: WebPay, 2: PayPal, etc.)
			MetodoPagoEnum metodo = (idMetodoPagoUpgrade != null && idMetodoPagoUpgrade == 2)
					? MetodoPagoEnum.PAYPAL
					: MetodoPagoEnum.WEBPAY;

			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();
			String contextPath = request.getContextPath();
			String portSegment = ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
					? "" : ":" + serverPort;
			String baseUrl = String.format("%s://%s%s%s", scheme, serverName, portSegment, contextPath);

			SolicitudPagoDTO solicitud = new SolicitudPagoDTO();
			solicitud.setOrdenCompra("UPG-" + System.currentTimeMillis() % 1000000000L);
			solicitud.setSessionId(session.getId());
			solicitud.setDescripcion("Ascenso Tarifa SkyWay - " + opcionUpgradeSeleccionada.getNombreTarifa());

			if (metodo == MetodoPagoEnum.PAYPAL) {
				solicitud.setMoneda("USD");
				double montoUsd = opcionUpgradeSeleccionada.getDiferenciaAPagar().doubleValue() / 950.0;
				solicitud.setMonto(BigDecimal.valueOf(montoUsd).setScale(2, RoundingMode.HALF_UP));
				solicitud.setReturnUrl(baseUrl + "/paypal/commit");
				solicitud.setCancelUrl(baseUrl + "/paypal/cancel");
			} else {
				solicitud.setMoneda("CLP");
				solicitud.setMonto(opcionUpgradeSeleccionada.getDiferenciaAPagar());
				solicitud.setReturnUrl(baseUrl + "/webpay/commit");
				solicitud.setCancelUrl(baseUrl + "/perfil/pasajero/index.xhtml");
			}

			PasarelaPagoStrategy pasarela = pagoFactoryService.obtenerEstrategia(metodo);
			String redirectUrl = pasarela.iniciarTransaccion(solicitud);
			Logger.logInfo("🚀 [UPGRADE-PERFIL] Redirigiendo a pasarela: " + redirectUrl);
			externalContext.redirect(redirectUrl);

		} catch (Exception e) {
			Logger.logError("❌ Error al iniciar pago de Upgrade en pasarela: " + e.getMessage());
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Pago", "No fue posible redirigir a la pasarela de pago seleccionada."));
		}
	}

	public boolean isUpgradeDisponible() {
		return opcionesUpgrade != null && !opcionesUpgrade.isEmpty();
	}

	public List<OpcionUpgradeDTO> getOpcionesUpgrade() { return opcionesUpgrade; }
	public OpcionUpgradeDTO getOpcionUpgradeSeleccionada() { return opcionUpgradeSeleccionada; }
	public void setOpcionUpgradeSeleccionada(OpcionUpgradeDTO opcionUpgradeSeleccionada) { this.opcionUpgradeSeleccionada = opcionUpgradeSeleccionada; }
	public Integer getIdMetodoPagoUpgrade() { return idMetodoPagoUpgrade; }
	public void setIdMetodoPagoUpgrade(Integer idMetodoPagoUpgrade) { this.idMetodoPagoUpgrade = idMetodoPagoUpgrade; }

	public int getEtapaActualCronograma() {
		return etapaActualCronograma;
	}

	public ResultadoUpgradeDTO getResultadoUpgrade() { return resultadoUpgrade; }
	public void setResultadoUpgrade(ResultadoUpgradeDTO resultadoUpgrade) { this.resultadoUpgrade = resultadoUpgrade; }

	public boolean isMostrarModalResultadoUpgrade() { return mostrarModalResultadoUpgrade; }
	public void setMostrarModalResultadoUpgrade(boolean mostrarModalResultadoUpgrade) { this.mostrarModalResultadoUpgrade = mostrarModalResultadoUpgrade; }

	public void cerrarModalResultadoUpgrade() {
		this.mostrarModalResultadoUpgrade = false;
	}

	public void actualizarSchedule() {
		this.scheduleModel = new DefaultScheduleModel();
		List<ItinerarioResumenDTO> fuente = (this.listaItinerariosFiltrados != null) ? this.listaItinerariosFiltrados : this.listaItinerarios;
		if (fuente == null || fuente.isEmpty()) {
			this.fechaInicialSchedule = LocalDate.now();
			return;
		}

		LocalDate primeraFecha = null;

		for (ItinerarioResumenDTO it : fuente) {
			if (it.getHoraSalida() == null) continue;

			LocalDateTime start = it.getHoraSalida().toInstant()
					.atZone(ZoneId.of("America/Santiago"))
					.toLocalDateTime();

			if (primeraFecha == null || (start.toLocalDate().isAfter(LocalDate.now().minusDays(1)) && primeraFecha.isBefore(LocalDate.now()))) {
				primeraFecha = start.toLocalDate();
			}

			LocalDateTime end;
			if (it.getHoraLlegada() != null) {
				end = it.getHoraLlegada().toInstant()
						.atZone(ZoneId.of("America/Santiago"))
						.toLocalDateTime();
			} else {
				end = start.plusHours(2);
			}

			boolean cancelada = isReservaCanceladaPorItinerario(it.getIdReserva());
			boolean checkInListo = isCheckInCompletado(it.getIdReserva());

			String estiloClase = "vuelo-evento-normal";
			String bg = "#0284c7";
			String border = "#0369a1";

			if (cancelada) {
				estiloClase = "vuelo-evento-cancelado";
				bg = "#64748b";
				border = "#475569";
			} else if (checkInListo) {
				estiloClase = "vuelo-evento-checkin";
				bg = "#10b981";
				border = "#059669";
			} else if (start.isBefore(LocalDateTime.now())) {
				estiloClase = "vuelo-evento-pasado";
				bg = "#334155";
				border = "#1e293b";
			}

			String title = (cancelada ? "🚫 " : "✈ ") + it.getCodigoIataOrigen() + " ➔ " + it.getCodigoIataDestino();

			ScheduleEvent<?> event = DefaultScheduleEvent.builder()
					.title(title)
					.startDate(start)
					.endDate(end)
					.data(it)
					.description("Itinerario #" + it.getIdItinerario() + " - Reserva #" + it.getIdReserva())
					.styleClass(estiloClase)
					.borderColor(border)
					.backgroundColor(bg)
					.textColor("#ffffff")
					.build();

			this.scheduleModel.addEvent(event);
		}

		this.fechaInicialSchedule = primeraFecha != null ? primeraFecha : LocalDate.now();
	}

	public void onEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent) {
		if (selectEvent == null || selectEvent.getObject() == null) return;
		ScheduleEvent<?> event = selectEvent.getObject();
		if (event.getData() instanceof ItinerarioResumenDTO) {
			ItinerarioResumenDTO item = (ItinerarioResumenDTO) event.getData();
			verDetalleCompletoReserva(item);
			PrimeFaces.current().ajax().update("formHubModal:dlgHubModal");
			PrimeFaces.current().executeScript("PF('dlgHubModal').show();");
		}
	}

	public ScheduleModel getScheduleModel() {
		return scheduleModel;
	}

	public void setScheduleModel(ScheduleModel scheduleModel) {
		this.scheduleModel = scheduleModel;
	}

	public String getVistaActiva() {
		return vistaActiva;
	}

	public void setVistaActiva(String vistaActiva) {
		this.vistaActiva = vistaActiva;
	}

	public LocalDate getFechaInicialSchedule() {
		return fechaInicialSchedule;
	}

	public void setFechaInicialSchedule(LocalDate fechaInicialSchedule) {
		this.fechaInicialSchedule = fechaInicialSchedule;
	}

	public Reserva getReservaDetalle() {
		return reservaDetalle;
	}

	public void setReservaDetalle(Reserva reservaDetalle) {
		this.reservaDetalle = reservaDetalle;
	}

	public String getModeloAvionItinerario() {
		if (itinerarioDetalle != null && itinerarioDetalle.getItinerarioVuelos() != null && !itinerarioDetalle.getItinerarioVuelos().isEmpty()) {
			ItinerarioVuelo iv = itinerarioDetalle.getItinerarioVuelos().get(0);
			if (iv.getVuelo() != null && iv.getVuelo().getAvion() != null && iv.getVuelo().getAvion().getModeloAvion() != null) {
				return iv.getVuelo().getAvion().getModeloAvion().getNombre();
			}
		}
		return "Boeing 787-9 Dreamliner";
	}

	public Date getHoraEmbarqueEstimada() {
		if (itinerarioSeleccionado != null && itinerarioSeleccionado.getHoraSalida() != null) {
			return new Date(itinerarioSeleccionado.getHoraSalida().getTime() - (45L * 60L * 1000L));
		}
		return null;
	}

	public String getMetodoPagoReserva() {
		if (reservaDetalle != null && reservaDetalle.getPagos() != null && !reservaDetalle.getPagos().isEmpty()) {
			Pago p = reservaDetalle.getPagos().get(0);
			if (p.getMetodoPago() != null && p.getMetodoPago().getDescripcion() != null) {
				return p.getMetodoPago().getDescripcion();
			}
		}
		return "WebPay Plus / Transbank";
	}

	public String getUbicacionAsiento(String numeroAsiento) {
		if (numeroAsiento == null || numeroAsiento.isEmpty()) return "Estándar";
		char letra = Character.toUpperCase(numeroAsiento.charAt(numeroAsiento.length() - 1));
		if (letra == 'A' || letra == 'F' || letra == 'K') return "Ventana";
		if (letra == 'C' || letra == 'D' || letra == 'G' || letra == 'H') return "Pasillo";
		return "Centro";
	}

	public String getNombreTitularReserva() {
		if (reservaDetalle != null && reservaDetalle.getPasajero() != null && reservaDetalle.getPasajero().getUsuario() != null) {
			return reservaDetalle.getPasajero().getUsuario().getNombre() + " " + reservaDetalle.getPasajero().getUsuario().getApellido();
		}
		if (usuario != null) {
			return usuario.getNombre() + " " + usuario.getApellido();
		}
		return "Pasajero SkyWay";
	}

	private void cargarPasajerosReserva() {
		this.pasajerosReservaMap.clear();
		this.listaPasajerosReserva.clear();

		if (reservaDetalle == null) return;

		// 1. Titular
		Pasajero tit = reservaDetalle.getPasajero();
		String rutTitular = (tit != null) ? tit.getRut() : (usuario != null ? usuario.getRut() : null);
		if (tit != null) {
			Usuario u = tit.getUsuario();
			if (u == null && pasajeroService != null) {
				Pasajero pLoaded = pasajeroService.findById(tit.getRut()).orElse(null);
				if (pLoaded != null) {
					tit = pLoaded;
					u = pLoaded.getUsuario();
				}
			}
			String nombre = (u != null) ? (u.getNombre() + " " + u.getApellido()) : "Titular";
			String correo = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "";
			pasajerosReservaMap.put(tit.getRut(), new PasajeroReservaInfo(tit.getRut(), nombre, correo, true));
		} else if (usuario != null && rutTitular != null) {
			pasajerosReservaMap.put(rutTitular, new PasajeroReservaInfo(rutTitular, usuario.getNombre() + " " + usuario.getApellido(), usuario.getCorreoElectronico(), true));
		}

		// 2. Pasajeros asociados en pasajero_reserva
		if (reservaDetalle.getPasajerosReserva() != null) {
			for (PasajeroReserva pr : reservaDetalle.getPasajerosReserva()) {
				if (pr == null) continue;
				Pasajero p = pr.getPasajero();
				String rut = (p != null) ? p.getRut() : null;
				if (rut != null && !pasajerosReservaMap.containsKey(rut)) {
					Usuario u = (p != null) ? p.getUsuario() : null;
					if (u == null && pasajeroService != null) {
						Pasajero pLoaded = pasajeroService.findById(rut).orElse(null);
						if (pLoaded != null) {
							p = pLoaded;
							u = pLoaded.getUsuario();
						}
					}
					String nombre = (u != null) ? (u.getNombre() + " " + u.getApellido()) : ("Pasajero " + rut);
					String correo = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "";
					boolean esTit = (rutTitular != null && rutTitular.equalsIgnoreCase(rut));
					pasajerosReservaMap.put(rut, new PasajeroReservaInfo(rut, nombre, correo, esTit));
				}
			}
		}

		// 3. Pasajeros en reserva_asiento (garantiza captura de acompañantes con asiento asignado)
		if (reservaDetalle.getReservaAsientos() != null) {
			for (ReservaAsiento ra : reservaDetalle.getReservaAsientos()) {
				if (ra == null) continue;
				String rut = ra.getRut();
				if (rut != null && !pasajerosReservaMap.containsKey(rut)) {
					Pasajero p = ra.getPasajero();
					Usuario u = (p != null) ? p.getUsuario() : null;
					if (u == null && pasajeroService != null) {
						Pasajero pLoaded = pasajeroService.findById(rut).orElse(null);
						if (pLoaded != null) {
							p = pLoaded;
							u = pLoaded.getUsuario();
						}
					}
					String nombre = (u != null) ? (u.getNombre() + " " + u.getApellido()) : ("Pasajero " + rut);
					String correo = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "";
					boolean esTit = (rutTitular != null && rutTitular.equalsIgnoreCase(rut));
					pasajerosReservaMap.put(rut, new PasajeroReservaInfo(rut, nombre, correo, esTit));
				}

				// Asignar asiento al pasajero correspondiente
				if (rut != null && ra.getAsiento() != null && ra.getAsiento().getNumeroAsiento() != null) {
					PasajeroReservaInfo pInfo = pasajerosReservaMap.get(rut);
					if (pInfo != null && !pInfo.getAsientos().contains(ra.getAsiento().getNumeroAsiento())) {
						pInfo.getAsientos().add(ra.getAsiento().getNumeroAsiento());
					}
				}
			}
		}

		this.listaPasajerosReserva.addAll(this.pasajerosReservaMap.values());
	}

	public PasajeroReservaInfo getPasajeroParaAsiento(InfoAsientoReservaDTO asientoDTO) {
		if (asientoDTO == null) return null;

		// 1. Buscar coincidencia exacta por idVuelo e (idAsiento o numeroAsiento) en reservaAsientos
		if (reservaDetalle != null && reservaDetalle.getReservaAsientos() != null) {
			for (ReservaAsiento ra : reservaDetalle.getReservaAsientos()) {
				if (ra == null) continue;
				boolean matchVuelo = (ra.getVuelo() != null && ra.getVuelo().getIdVuelo() == asientoDTO.getIdVuelo());
				boolean matchAsiento = false;
				if (ra.getAsiento() != null) {
					if (ra.getAsiento().getIdAsiento() == asientoDTO.getIdAsiento()) {
						matchAsiento = true;
					} else if (asientoDTO.getNumeroAsiento() != null 
							&& asientoDTO.getNumeroAsiento().equalsIgnoreCase(ra.getAsiento().getNumeroAsiento())) {
						matchAsiento = true;
					}
				}
				if (matchVuelo && matchAsiento) {
					String rut = ra.getRut();
					if (rut != null && pasajerosReservaMap.containsKey(rut)) {
						return pasajerosReservaMap.get(rut);
					}
					// Construir info si no estaba en el mapa
					Pasajero p = ra.getPasajero();
					Usuario u = (p != null) ? p.getUsuario() : null;
					if (u == null && pasajeroService != null && rut != null) {
						Pasajero pLoaded = pasajeroService.findById(rut).orElse(null);
						if (pLoaded != null) u = pLoaded.getUsuario();
					}
					String nombre = (u != null) ? (u.getNombre() + " " + u.getApellido()) : ("Pasajero " + rut);
					String correo = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "";
					boolean titular = (reservaDetalle.getPasajero() != null && rut != null && rut.equalsIgnoreCase(reservaDetalle.getPasajero().getRut()));
					PasajeroReservaInfo info = new PasajeroReservaInfo(rut, nombre, correo, titular);
					if (rut != null) {
						pasajerosReservaMap.put(rut, info);
						listaPasajerosReserva.add(info);
					}
					return info;
				}
			}

			// Fallback relajado por número de asiento
			for (ReservaAsiento ra : reservaDetalle.getReservaAsientos()) {
				if (ra != null && ra.getAsiento() != null && asientoDTO.getNumeroAsiento() != null
						&& asientoDTO.getNumeroAsiento().equalsIgnoreCase(ra.getAsiento().getNumeroAsiento())) {
					String rut = ra.getRut();
					if (rut != null && pasajerosReservaMap.containsKey(rut)) {
						return pasajerosReservaMap.get(rut);
					}
				}
			}
		}

		// 2. Fallback al titular
		if (reservaDetalle != null && reservaDetalle.getPasajero() != null) {
			String rutTit = reservaDetalle.getPasajero().getRut();
			if (pasajerosReservaMap.containsKey(rutTit)) {
				return pasajerosReservaMap.get(rutTit);
			}
		}

		return new PasajeroReservaInfo(
				usuario != null ? usuario.getRut() : "",
				getNombreTitularReserva(),
				usuario != null ? usuario.getCorreoElectronico() : "",
				true
		);
	}

	public String getNombrePasajeroAsiento(InfoAsientoReservaDTO asientoDTO) {
		PasajeroReservaInfo info = getPasajeroParaAsiento(asientoDTO);
		return (info != null && info.getNombreCompleto() != null) ? info.getNombreCompleto() : getNombreTitularReserva();
	}

	public String getTipoPasajeroAsiento(InfoAsientoReservaDTO asientoDTO) {
		PasajeroReservaInfo info = getPasajeroParaAsiento(asientoDTO);
		return (info != null) ? info.getTipoPasajero() : "Titular de Reserva";
	}

	public String getRutPasajeroAsiento(InfoAsientoReservaDTO asientoDTO) {
		PasajeroReservaInfo info = getPasajeroParaAsiento(asientoDTO);
		return (info != null && info.getRut() != null) ? info.getRut() : "";
	}

	public List<PasajeroReservaInfo> getListaPasajerosReserva() {
		return listaPasajerosReserva;
	}

	public int getCantidadPasajerosReserva() {
		return !listaPasajerosReserva.isEmpty() ? listaPasajerosReserva.size() : 1;
	}

	public static class PasajeroReservaInfo implements Serializable {
		private static final long serialVersionUID = 1L;
		private String rut;
		private String nombreCompleto;
		private String correo;
		private boolean titular;
		private List<String> asientos = new ArrayList<>();

		public PasajeroReservaInfo(String rut, String nombreCompleto, String correo, boolean titular) {
			this.rut = rut;
			this.nombreCompleto = nombreCompleto;
			this.correo = correo;
			this.titular = titular;
		}

		public String getRut() { return rut; }
		public String getNombreCompleto() { return nombreCompleto; }
		public String getCorreo() { return correo; }
		public boolean isTitular() { return titular; }
		public String getTipoPasajero() { return titular ? "Titular de Reserva" : "Acompañante"; }
		public List<String> getAsientos() { return asientos; }
		public void setAsientos(List<String> asientos) { this.asientos = asientos; }
		public String getAsientosFormateados() {
			if (asientos == null || asientos.isEmpty()) return "Sin asignar";
			return String.join(", ", asientos);
		}
	}

	public void inicializarMetricasDelta() {
		if (listaItinerarios == null || listaItinerarios.isEmpty()) {
			this.listaItinerariosHoy = new ArrayList<>();
			this.listaItinerariosFuturos = new ArrayList<>();
			this.listaItinerariosPasados = new ArrayList<>();
			this.acompanantesHabituales = new ArrayList<>();
			return;
		}

		LocalDate hoy = LocalDate.now();
		this.listaItinerariosHoy = new ArrayList<>();
		this.listaItinerariosFuturos = new ArrayList<>();
		this.listaItinerariosPasados = new ArrayList<>();

		Map<String, AcompananteDTO> acompMap = new LinkedHashMap<>();

		if (ciudadIataCache.isEmpty() && aeropuertoService != null) {
			try {
				List<Aeropuerto> aeropuertos = aeropuertoService.findAll();
				if (aeropuertos != null) {
					for (Aeropuerto a : aeropuertos) {
						if (a != null && a.getCodigoIata() != null) {
							String ciudad = (a.getCiudad() != null) ? a.getCiudad().getNombre() : a.getNombreAeropuerto();
							String pais = (a.getCiudad() != null && a.getCiudad().getPai() != null) ? a.getCiudad().getPai().getNombre() : "";
							String ubicacion = (pais == null || pais.isEmpty()) ? ciudad : (ciudad + ", " + pais);
							ciudadIataCache.put(a.getCodigoIata().trim().toUpperCase(), ubicacion);
						}
					}
				}
			} catch (Exception e) {
				Logger.logWarn("No se pudo pre-cargar catálogo de aeropuertos: " + e.getMessage());
			}
		}

		Date ahora = new Date();
		for (ItinerarioResumenDTO it : listaItinerarios) {
			if (it == null) continue;

			if (it.getHoraSalida() != null) {
				LocalDate fechaSalida = it.getHoraSalida().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				if (fechaSalida.isEqual(hoy)) {
					listaItinerariosHoy.add(it);
				}
				if (it.getHoraSalida().after(ahora)) {
					listaItinerariosFuturos.add(it);
				} else {
					listaItinerariosPasados.add(it);
				}
			}

			if (it.getIdReserva() != null && !pnrCache.containsKey(it.getIdReserva())) {
				try {
					Optional<Reserva> opt = reservaService.findById(it.getIdReserva());
					if (opt.isPresent()) {
						Reserva r = opt.get();
						reservaEntityCache.put(it.getIdReserva(), r);
						pnrCache.put(it.getIdReserva(), (r.getCodigoReserva() != null && !r.getCodigoReserva().isEmpty()) ? r.getCodigoReserva() : "SW-" + it.getIdReserva());

						List<String> asientosList = new ArrayList<>();
						Set<String> nombresSet = new LinkedHashSet<>();

						if (r.getPasajero() != null && r.getPasajero().getUsuario() != null) {
							Usuario u = r.getPasajero().getUsuario();
							nombresSet.add(u.getNombre() + " " + u.getApellido());
						}

						if (r.getReservaAsientos() != null) {
							for (ReservaAsiento ra : r.getReservaAsientos()) {
								if (ra != null) {
									if (ra.getAsiento() != null && ra.getAsiento().getNumeroAsiento() != null) {
										asientosList.add(ra.getAsiento().getNumeroAsiento());
									}
									String rutPax = ra.getRut();
									Pasajero p = ra.getPasajero();
									Usuario u = (p != null) ? p.getUsuario() : null;
									if (u == null && pasajeroService != null && rutPax != null) {
										Pasajero pLoaded = pasajeroService.findById(rutPax).orElse(null);
										if (pLoaded != null) {
											p = pLoaded;
											u = pLoaded.getUsuario();
										}
									}
									String nom = (u != null) ? (u.getNombre() + " " + u.getApellido()) : ("Pasajero " + rutPax);
									String correo = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "";

									if (rutPax != null && usuario != null && !rutPax.equalsIgnoreCase(usuario.getRut()) && !acompMap.containsKey(rutPax)) {
										String iniciales = obtenerIniciales(nom);
										acompMap.put(rutPax, new AcompananteDTO(iniciales, nom, rutPax, correo));
									}
									nombresSet.add(nom);
								}
							}
						}

						if (r.getPasajerosReserva() != null) {
							for (PasajeroReserva pr : r.getPasajerosReserva()) {
								if (pr != null && pr.getPasajero() != null) {
									String rutPax = pr.getPasajero().getRut();
									Pasajero p = pr.getPasajero();
									Usuario u = (p != null) ? p.getUsuario() : null;
									if (u == null && pasajeroService != null && rutPax != null) {
										Pasajero pLoaded = pasajeroService.findById(rutPax).orElse(null);
										if (pLoaded != null) {
											p = pLoaded;
											u = pLoaded.getUsuario();
										}
									}
									String nom = (u != null) ? (u.getNombre() + " " + u.getApellido()) : ("Pasajero " + rutPax);
									String correo = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "";

									if (rutPax != null && usuario != null && !rutPax.equalsIgnoreCase(usuario.getRut()) && !acompMap.containsKey(rutPax)) {
										String iniciales = obtenerIniciales(nom);
										acompMap.put(rutPax, new AcompananteDTO(iniciales, nom, rutPax, correo));
									}
									nombresSet.add(nom);
								}
							}
						}

						if (asientosList.isEmpty() && asientoService != null && it.getIdItinerario() != null) {
							try {
								List<InfoAsientoReservaDTO> list = asientoService.getAsientosReservados(it.getIdItinerario(), it.getIdReserva());
								if (list != null) {
									for (InfoAsientoReservaDTO a : list) {
										if (a.getNumeroAsiento() != null && !asientosList.contains(a.getNumeroAsiento())) {
											asientosList.add(a.getNumeroAsiento());
										}
									}
								}
							} catch (Exception ignored) {}
						}

						if (nombresSet.isEmpty() && usuario != null) {
							nombresSet.add(usuario.getNombre() + " " + usuario.getApellido());
						}

						asientosReservaCache.put(it.getIdReserva(), asientosList.isEmpty() ? "Asignado en Check-In" : String.join(", ", asientosList));
						cantidadPasajerosCache.put(it.getIdReserva(), Math.max(1, nombresSet.size()));
						nombresPasajerosCache.put(it.getIdReserva(), String.join(", ", nombresSet));
					}
				} catch (Exception e) {
					Logger.logWarn("Error al procesar reserva #" + it.getIdReserva() + " para métricas Delta: " + e.getMessage());
				}
			}
		}

		this.acompanantesHabituales = new ArrayList<>(acompMap.values());
	}

	public String obtenerIniciales(String nombreCompleto) {
		if (nombreCompleto == null || nombreCompleto.trim().isEmpty()) return "PA";
		String[] partes = nombreCompleto.trim().split("\\s+");
		if (partes.length >= 2) {
			return (partes[0].substring(0, 1) + partes[1].substring(0, 1)).toUpperCase();
		}
		return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
	}

	public String getFechaDia(Date fecha) {
		if (fecha == null) return "--";
		return new SimpleDateFormat("dd").format(fecha);
	}

	public String getFechaMes(Date fecha) {
		if (fecha == null) return "---";
		return new SimpleDateFormat("MMM", new Locale("es", "CL")).format(fecha).toUpperCase().replace(".", "");
	}

	public String getCiudadRuta(String origenIata, String destinoIata) {
		String cOrig = getNombreCiudad(origenIata);
		String cDest = getNombreCiudad(destinoIata);
		return cOrig + " ➔ " + cDest;
	}

	public String getNombreCiudad(String iata) {
		if (iata == null) return "Aeropuerto Comercial";
		String key = iata.trim().toUpperCase();
		if (ciudadIataCache.containsKey(key)) {
			return ciudadIataCache.get(key);
		}
		switch (key) {
			case "SCL": return "Santiago, Chile";
			case "MVD": return "Montevideo, Uruguay";
			case "GRU": return "São Paulo, Brasil";
			case "JFK": return "Nueva York (JFK), EE.UU.";
			case "LAX": return "Los Ángeles (LAX), EE.UU.";
			case "EZE": return "Buenos Aires, Argentina";
			case "MIA": return "Miami, FL, EE.UU.";
			case "BOG": return "Bogotá, Colombia";
			case "LIM": return "Lima, Perú";
			case "MAD": return "Madrid, España";
			default: return key;
		}
	}

	public String getCodigoPnr(Integer idReserva) {
		if (idReserva == null) return "SW-PNR";
		return pnrCache.getOrDefault(idReserva, "SW-" + idReserva);
	}

	public String getAsientosPorReserva(Integer idReserva, Integer idItinerario) {
		if (idReserva == null) return "Por asignar";
		String val = asientosReservaCache.get(idReserva);
		if (val != null && !val.isEmpty() && !val.equals("Asignado en Check-In")) {
			return val;
		}
		if (asientoService != null && idItinerario != null) {
			try {
				List<InfoAsientoReservaDTO> list = asientoService.getAsientosReservados(idItinerario, idReserva);
				if (list != null && !list.isEmpty()) {
					List<String> nums = list.stream()
							.map(InfoAsientoReservaDTO::getNumeroAsiento)
							.filter(Objects::nonNull)
							.distinct()
							.collect(Collectors.toList());
					if (!nums.isEmpty()) {
						String joined = String.join(", ", nums);
						asientosReservaCache.put(idReserva, joined);
						return joined;
					}
				}
			} catch (Exception ignored) {}
		}
		return (val != null && !val.isEmpty()) ? val : "Asignado en Check-In";
	}

	public int getCantidadPasajerosPorReserva(Integer idReserva) {
		if (idReserva == null) return 1;
		return cantidadPasajerosCache.getOrDefault(idReserva, 1);
	}

	public String getNombresPasajerosPorReserva(Integer idReserva) {
		if (idReserva == null) return getNombreTitularReserva();
		return nombresPasajerosCache.getOrDefault(idReserva, getNombreTitularReserva());
	}

	public String getEstadoVueloPorItinerario(ItinerarioResumenDTO it) {
		if (it == null) return "A Tiempo";
		if (isReservaCanceladaPorItinerario(it.getIdReserva())) {
			return "Cancelada por Pasajero";
		}
		Date ahora = new Date();
		if (it.getHoraLlegada() != null && ahora.after(it.getHoraLlegada())) {
			return "Aterrizado";
		}
		if (it.getHoraSalida() != null) {
			long diffMin = (it.getHoraSalida().getTime() - ahora.getTime()) / (60 * 1000);
			if (diffMin <= 0 && (it.getHoraLlegada() == null || ahora.before(it.getHoraLlegada()))) {
				return "En Vuelo";
			}
			if (diffMin > 0 && diffMin <= 15) {
				return "Última Llamada";
			}
			if (diffMin > 15 && diffMin <= 45) {
				return "Embarcando";
			}
			if (diffMin > 45 && diffMin <= 60) {
				if (!isCheckInCompletado(it.getIdReserva())) {
					return "Check-In Urgente";
				}
				return "A Tiempo";
			}
		}
		return "A Tiempo";
	}

	public String getEstadoBadgeClass(ItinerarioResumenDTO it) {
		String est = getEstadoVueloPorItinerario(it);
		switch (est) {
			case "A Tiempo": return "bg-success text-white font-weight-bold";
			case "Check-In Urgente": return "bg-warning text-dark font-weight-bold";
			case "Embarcando": return "bg-warning text-dark font-weight-bold";
			case "Última Llamada": return "bg-danger text-white font-weight-bold";
			case "En Vuelo": return "bg-info text-dark font-weight-bold";
			case "Cancelada por Pasajero":
			case "Cancelada":
			case "Cancelado": return "bg-danger text-white font-weight-bold";
			case "Aterrizado":
			case "Finalizado": return "bg-secondary text-white font-weight-bold";
			default: return "bg-primary text-white font-weight-bold";
		}
	}

	public String getEstadoIconClass(ItinerarioResumenDTO it) {
		String est = getEstadoVueloPorItinerario(it);
		switch (est) {
			case "A Tiempo": return "pi-check-circle";
			case "Check-In Urgente": return "pi-bolt";
			case "Embarcando": return "pi-users";
			case "Última Llamada": return "pi-exclamation-triangle";
			case "En Vuelo": return "pi-send";
			case "Cancelada por Pasajero":
			case "Cancelada":
			case "Cancelado": return "pi-times-circle";
			case "Aterrizado":
			case "Finalizado": return "pi-check";
			default: return "pi-circle-fill";
		}
	}

	public boolean tieneAvisoOperacional(ItinerarioResumenDTO it) {
		if (it == null || it.getHoraSalida() == null) return false;
		if (isReservaCanceladaPorItinerario(it.getIdReserva())) return false;
		Date ahora = new Date();
		if (it.getHoraLlegada() != null && ahora.after(it.getHoraLlegada())) return false;
		long diffMin = (it.getHoraSalida().getTime() - ahora.getTime()) / (60 * 1000);
		return diffMin > 0 && diffMin <= 120;
	}

	public String getTituloAvisoOperacional(ItinerarioResumenDTO it) {
		if (it == null || it.getHoraSalida() == null) return "";
		Date ahora = new Date();
		long diffMin = (it.getHoraSalida().getTime() - ahora.getTime()) / (60 * 1000);
		boolean checkinListo = isCheckInCompletado(it.getIdReserva());

		if (diffMin <= 15) {
			return "ÚLTIMA LLAMADA: EMBARQUE FINALIZANDO";
		}
		if (diffMin <= 45) {
			return "PROCESO DE EMBARQUE INICIADO";
		}
		if (diffMin <= 60) {
			return checkinListo
					? "PRESENTACIÓN EN PUERTA DE EMBARQUE"
					: "ATENCIÓN: CIERRE DE CHECK-IN EN MENOS DE 60 MINUTOS";
		}
		return checkinListo
				? "INFORMACIÓN DE VIAJE: RECEPCIÓN Y DESPACHO DE EQUIPAJE"
				: "INFORMACIÓN DE VUELO: CHECK-IN ONLINE DISPONIBLE";
	}

	public String getMensajeAvisoOperacional(ItinerarioResumenDTO it) {
		if (it == null || it.getHoraSalida() == null) return "";
		Date ahora = new Date();
		long diffMin = (it.getHoraSalida().getTime() - ahora.getTime()) / (60 * 1000);
		boolean checkinListo = isCheckInCompletado(it.getIdReserva());

		if (diffMin <= 15) {
			return "El embarque se encuentra en su fase final. Le solicitamos presentarse de inmediato en la puerta asignada con su documento de identidad.";
		}
		if (diffMin <= 45) {
			return "El llamado de abordaje está activo. Por favor acérquese a la puerta de embarque indicada portando su tarjeta de embarque y documento oficial.";
		}
		if (diffMin <= 60) {
			return checkinListo
					? "Su vuelo iniciará el proceso de abordaje en breve. Le sugerimos cruzar los controles de seguridad y dirigirse a su sala de embarque."
					: "El plazo para realizar Check-In y entrega de equipaje está por finalizar. Complete su registro a la brevedad para garantizar la emisión de su tarjeta de embarque.";
		}
		return checkinListo
				? "Si viaja con equipaje facturado para bodega, preséntese en los mostradores de SkyWay antes del cierre reglamentario del vuelo."
				: "Su vuelo despega en menos de dos horas. Le recomendamos emitir su tarjeta de embarque de forma anticipada para agilizar su ingreso en el aeropuerto.";
	}

	public String getTipoAvisoOperacionalClass(ItinerarioResumenDTO it) {
		if (it == null || it.getHoraSalida() == null) return "alert-info";
		Date ahora = new Date();
		long diffMin = (it.getHoraSalida().getTime() - ahora.getTime()) / (60 * 1000);
		boolean checkinListo = isCheckInCompletado(it.getIdReserva());

		if (diffMin <= 15) return "alert-critical";
		if (diffMin <= 45) return "alert-boarding";
		if (diffMin <= 60) return checkinListo ? "alert-boarding" : "alert-urgent";
		return checkinListo ? "alert-info" : "alert-warning";
	}

	public String getIconoAvisoOperacional(ItinerarioResumenDTO it) {
		if (it == null || it.getHoraSalida() == null) return "pi-info-circle";
		Date ahora = new Date();
		long diffMin = (it.getHoraSalida().getTime() - ahora.getTime()) / (60 * 1000);
		boolean checkinListo = isCheckInCompletado(it.getIdReserva());

		if (diffMin <= 15) return "pi-exclamation-circle text-danger";
		if (diffMin <= 45) return "pi-users text-warning";
		if (diffMin <= 60) return checkinListo ? "pi-compass text-info" : "pi-exclamation-triangle text-warning";
		return checkinListo ? "pi-info-circle text-info" : "pi-clock text-warning";
	}

	public String getPuertaEmbarquePorItinerario(Integer idReserva, Integer idItinerario) {
		if (idReserva == null || idItinerario == null || usuario == null) return "Puerta: Por Asignar";
		String key = idReserva + "_" + idItinerario;
		if (puertaEmbarqueCache.containsKey(key)) {
			return puertaEmbarqueCache.get(key);
		}
		try {
			List<TicketInfo> tickets = reservaService.getTicket(usuario.getRut(), idReserva, idItinerario);
			if (tickets != null && !tickets.isEmpty()) {
				String p = tickets.get(0).getCodigoPuerta();
				String t = tickets.get(0).getTerminal();
				if (p != null && !p.trim().isEmpty()) {
					String res = (t != null && !t.trim().isEmpty() ? "Term. " + t + " • " : "") + "Puerta " + p;
					puertaEmbarqueCache.put(key, res);
					return res;
				}
			}
		} catch (Exception e) {
			// Silencioso
		}
		puertaEmbarqueCache.put(key, "Puerta: Por Asignar");
		return "Puerta: Por Asignar";
	}

	public String getAnioRegistro() {
		if (usuario != null && usuario.getFechaRegistro() != null) {
			return new SimpleDateFormat("yyyy").format(usuario.getFechaRegistro());
		}
		return "2024";
	}

	public String getFechaNacimientoFormateada() {
		if (pasajeroLogueado != null && pasajeroLogueado.getFechaNacimiento() != null) {
			return new SimpleDateFormat("dd/MM/yyyy").format(pasajeroLogueado.getFechaNacimiento());
		}
		if (usuario != null && usuario.getFechaNacimiento() != null) {
			return new SimpleDateFormat("dd/MM/yyyy").format(usuario.getFechaNacimiento());
		}
		return "No registrada";
	}

	public String getNacionalidadPasajero() {
		if (pasajeroLogueado != null && pasajeroLogueado.getNacionalidad() != null && !pasajeroLogueado.getNacionalidad().isEmpty()) {
			return pasajeroLogueado.getNacionalidad();
		}
		return "Chilena";
	}

	public String getTotalReservaFormateado(Integer idReserva) {
		if (idReserva == null) return "$0 CLP";
		Reserva r = reservaEntityCache.get(idReserva);
		if (r == null && reservaService != null) {
			r = reservaService.findById(idReserva).orElse(null);
			if (r != null) reservaEntityCache.put(idReserva, r);
		}
		if (r != null && r.getTotal() != null) {
			return String.format(Locale.GERMANY, "$%,.0f CLP", r.getTotal());
		}
		return "$0 CLP";
	}

	public String getEstadoReservaNombre(Integer idReserva) {
		if (idReserva == null) return "Confirmada";
		Reserva r = reservaEntityCache.get(idReserva);
		if (r == null && reservaService != null) {
			r = reservaService.findById(idReserva).orElse(null);
			if (r != null) reservaEntityCache.put(idReserva, r);
		}
		if (r != null && r.getEstadoReservaBean() != null && r.getEstadoReservaBean().getDescripcion() != null) {
			return r.getEstadoReservaBean().getDescripcion();
		}
		return "Confirmada";
	}

	public String getNombreTarifaItinerario(Integer idReserva, Integer idItinerario) {
		if (idReserva == null) return "Tarifa Estándar";
		Reserva r = reservaEntityCache.get(idReserva);
		if (r == null && reservaService != null) {
			r = reservaService.findById(idReserva).orElse(null);
			if (r != null) reservaEntityCache.put(idReserva, r);
		}
		if (r != null && r.getReservaItinerarios() != null) {
			for (ReservaItinerario ri : r.getReservaItinerarios()) {
				if (ri.getItinerario() != null && ri.getItinerario().getIdItinerario().equals(idItinerario)) {
					if (ri.getItinerarioTarifa() != null && ri.getItinerarioTarifa().getTarifa() != null) {
						return ri.getItinerarioTarifa().getTarifa().getNombre();
					}
				}
			}
		}
		return "Tarifa Estándar";
	}

	public boolean isCuentaActiva() {
		return usuario != null && usuario.getRut() != null;
	}

	public String getEstadoCuenta() {
		return (usuario != null && usuario.getRut() != null) ? "Activa" : "Inactiva";
	}

	public String getEquipajeResumenPorReserva(Integer idReserva) {
		if (idReserva == null || equipajeService == null) return "Sin equipaje en bodega";
		if (equipajeResumenCache.containsKey(idReserva)) {
			return equipajeResumenCache.get(idReserva);
		}
		try {
			List<com.SkyWay.modules.equipaje.domain.model.Equipaje> list = equipajeService.buscarPorReserva(idReserva);
			if (list != null && !list.isEmpty()) {
				int bultos = list.size();
				BigDecimal pesoTotal = BigDecimal.ZERO;
				for (com.SkyWay.modules.equipaje.domain.model.Equipaje eq : list) {
					if (eq.getPeso() != null) {
						pesoTotal = pesoTotal.add(eq.getPeso());
					}
				}
				String res = bultos + (bultos == 1 ? " bulto registrado (" : " bultos registrados (") + pesoTotal.intValue() + " kg en bodega)";
				equipajeResumenCache.put(idReserva, res);
				return res;
			}
		} catch (Exception ignored) {}
		String res = "Sin equipaje de bodega adicional";
		equipajeResumenCache.put(idReserva, res);
		return res;
	}

	public ItinerarioDetalleDTO getDetalleItinerarioInfo(Integer idItinerario) {
		if (idItinerario == null || itinerarioService == null) return null;
		if (!itinerarioDetalleCache.containsKey(idItinerario)) {
			try {
				List<ItinerarioDetalleDTO> detalles = itinerarioService.obtenerDetalleItinerario(idItinerario);
				if (detalles != null && !detalles.isEmpty()) {
					itinerarioDetalleCache.put(idItinerario, detalles.get(0));
				}
			} catch (Exception ignored) {}
		}
		return itinerarioDetalleCache.get(idItinerario);
	}

	public String getModeloAvionPorItinerario(Integer idItinerario) {
		ItinerarioDetalleDTO d = getDetalleItinerarioInfo(idItinerario);
		return (d != null && d.getModeloAvion() != null && !d.getModeloAvion().isEmpty()) ? d.getModeloAvion() : "Flota Comercial SkyWay";
	}

	public String getNumeroVueloPorItinerario(Integer idItinerario) {
		ItinerarioDetalleDTO d = getDetalleItinerarioInfo(idItinerario);
		return (d != null && d.getVuelo() != null && !d.getVuelo().isEmpty()) ? d.getVuelo() : "SW-" + idItinerario;
	}

	public String getDuracionTotalPorItinerario(Integer idItinerario) {
		ItinerarioDetalleDTO d = getDetalleItinerarioInfo(idItinerario);
		return (d != null && d.getDuracionTotal() != null && !d.getDuracionTotal().isEmpty()) ? d.getDuracionTotal() : "Itinerario Programado";
	}

	public String getEscalasResumenPorItinerario(Integer idItinerario) {
		ItinerarioDetalleDTO d = getDetalleItinerarioInfo(idItinerario);
		if (d != null && d.getNumeroEscalas() != null) {
			return d.getNumeroEscalas() == 0 ? "Vuelo Directo" : d.getNumeroEscalas() + " Escala(s)";
		}
		return "Vuelo Directo";
	}

	public int getTotalVuelosHoy() {
		return listaItinerariosHoy != null ? listaItinerariosHoy.size() : 0;
	}

	public int getTotalVuelosFuturos() {
		return listaItinerariosFuturos != null ? listaItinerariosFuturos.size() : 0;
	}

	public int getTotalVuelosPasados() {
		return listaItinerariosPasados != null ? listaItinerariosPasados.size() : 0;
	}

	public List<ItinerarioResumenDTO> getListaItinerariosHoy() {
		return listaItinerariosHoy;
	}

	public List<ItinerarioResumenDTO> getListaItinerariosFuturos() {
		return listaItinerariosFuturos;
	}

	public List<ItinerarioResumenDTO> getListaItinerariosPasados() {
		return listaItinerariosPasados;
	}

	public List<AcompananteDTO> getAcompanantesHabituales() {
		return acompanantesHabituales;
	}

	public Pasajero getPasajeroLogueado() {
		return pasajeroLogueado;
	}

	public static class AcompananteDTO implements Serializable {
		private static final long serialVersionUID = 1L;
		private String iniciales;
		private String nombreCompleto;
		private String rut;
		private String correo;

		public AcompananteDTO(String iniciales, String nombreCompleto, String rut, String correo) {
			this.iniciales = iniciales;
			this.nombreCompleto = nombreCompleto;
			this.rut = rut;
			this.correo = correo;
		}

		public String getIniciales() { return iniciales; }
		public String getNombreCompleto() { return nombreCompleto; }
		public String getRut() { return rut; }
		public String getCorreo() { return correo; }
	}
}