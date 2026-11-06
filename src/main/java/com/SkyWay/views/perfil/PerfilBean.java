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

		if (tarifaCaracteristicas == null || !tarifaValidator.permiteCambio(tarifaCaracteristicas)) {
			Logger.logInfo("La tarifa no permite cambios para la reserva/itinerario actual.");
			context.addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR,
							"Cambio No Permitido",
							"Tu tarifa asignada no permite realizar cambios de asiento en este vuelo."));
			return;
		}

		if (itinerarioDetalle != null && itinerarioDetalle.getHoraSalida() != null) {
			LocalDateTime fechaSalida = LocalDateTime.ofInstant(itinerarioDetalle.getHoraSalida().toInstant(), ZoneId.systemDefault());
			long horasRestantes = Duration.between(LocalDateTime.now(), fechaSalida).toHours();

			if (!tarifaValidator.cumpleHorasMinimasCambio(tarifaCaracteristicas, (int) horasRestantes)) {
				context.addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN,
								"Plazo Expirado",
								"El cambio de asiento debe realizarse con mayor anticipación según las políticas de tu tarifa."));
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

		} else if (segundosHastaSalida <= 2700 && segundosHastaSalida > 0) {
			this.estadoVueloVisual = "Embarcando / Puerta Cierra";
			this.estadoVueloBadgeClass = "bg-warning text-dark";
			this.estadoVueloIcono = "pi-exclamation-triangle";
			this.tiempoRestanteFormateado = String.format("%02dm %02ds", duracionHastaSalida.toMinutesPart(), duracionHastaSalida.toSecondsPart());
			this.cintaEquipaje = "Despacho Cerrado";

		} else {
			this.estadoVueloVisual = "A Tiempo";
			this.estadoVueloBadgeClass = "bg-success";
			this.estadoVueloIcono = "pi-circle-fill";

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

	public void irACheckIn() throws IOException {
		if (itinerarioSeleccionado == null) return;

		FacesContext context = FacesContext.getCurrentInstance();
		if (context != null && context.getExternalContext() != null) {
			Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
			sessionMap.put("CHECKIN_ID_RESERVA", itinerarioSeleccionado.getIdReserva());
			sessionMap.put("CHECKIN_ID_ITINERARIO", itinerarioSeleccionado.getIdItinerario());

			context.getExternalContext().redirect(
					context.getExternalContext().getRequestContextPath() + "/home/checkin.xhtml"
			);
		}
	}

	public void irACheckIn(ItinerarioResumenDTO it) throws IOException {
		if (it == null) return;
		this.itinerarioSeleccionado = it;
		irACheckIn();
	}

	public StreamedContent descargarComprobantePdf() {
		Logger.logInfo("📄 [PDF RESUMEN] Solicitando descarga para idReservaSeleccionada: " + this.idReservaSeleccionada);
		return descargarComprobantePdf(this.idReservaSeleccionada);
	}

	public StreamedContent descargarBoardingPassesPdf() {
		Logger.logInfo("🎟️ [PDF BOARDING PASSES] Solicitando descarga para idReservaSeleccionada: " + this.idReservaSeleccionada);
		if (this.idReservaSeleccionada == null || !reservaService.tieneCheckInCompletado(this.idReservaSeleccionada)) {
			FacesContext context = FacesContext.getCurrentInstance();
			if (context != null) {
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
						"Check-In Requerido", "Debes completar el Check-In Online antes de descargar tus tarjetas de embarque."));
			}
			return null;
		}
		if (this.ticketInfos != null && !this.ticketInfos.isEmpty()) {
			try {
				byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(this.ticketInfos);
				if (pdfBytes != null && pdfBytes.length > 0) {
					String pnr = this.ticketInfos.get(0).getCodigoPnr();
					Logger.logInfo("✅ [PDF BOARDING PASSES] PDF generado desde ticketInfos (" + pdfBytes.length + " bytes).");
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
		return descargarBoardingPassesPdf(this.idReservaSeleccionada);
	}


	/**
	 * Descarga nativa del PDF 1: Resumen de Reserva e Itinerario (con trazas de log)
	 */
	public StreamedContent descargarComprobantePdf(Integer idReserva) {
		Logger.logInfo("📄 [PDF RESUMEN] Solicitando descarga para idReserva: " + idReserva);

		if (idReserva == null) {
			Logger.logWarn("⚠️ [PDF RESUMEN] idReserva recibido es NULL. Se cancela la descarga.");
			return null;
		}

		try {
			Optional<Reserva> optReserva = reservaService.findById(idReserva);
			if (optReserva.isEmpty()) {
				Logger.logWarn("⚠️ [PDF RESUMEN] No se encontró la entidad Reserva para ID: " + idReserva);
				return null;
			}

			Reserva reserva = optReserva.get();
			Logger.logInfo("📄 [PDF RESUMEN] Generando bytes de PDF para PNR: " + reserva.getCodigoReserva());

			byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfResumenReserva(reserva);

			if (pdfBytes == null || pdfBytes.length == 0) {
				Logger.logError("❌ [PDF RESUMEN] Los bytes del PDF generados están vacíos o nulos.");
				return null;
			}

			Logger.logInfo("✅ [PDF RESUMEN] PDF generado correctamente (" + pdfBytes.length + " bytes). Iniciando descarga StreamedContent...");

			return DefaultStreamedContent.builder()
					.name("Resumen_Reserva_SkyWay_" + reserva.getCodigoReserva() + ".pdf")
					.contentType("application/pdf")
					.stream(() -> new ByteArrayInputStream(pdfBytes))
					.build();

		} catch (Exception e) {
			Logger.logError("❌ [PDF RESUMEN] Excepción inesperada al descargar comprobante: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}



	/**
	 * Descarga nativa del PDF 2: Tarjetas de Embarque / Boarding Passes (con trazas de log)
	 */
	public StreamedContent descargarBoardingPassesPdf(Integer idReserva) {
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
}