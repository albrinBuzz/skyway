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
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.BarcodeGeneratorUtil;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
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


	private List<ItinerarioResumenDTO> listaItinerarios;
	private List<ItinerarioResumenDTO> listaItinerariosFiltrados;
	private ItinerarioResumenDTO itinerarioSeleccionado;
	private Itinerario itinerarioDetalle;
	private List<InfoAsientoReservaDTO> listaAsientosReservados;
	private List<Equipaje> listaEquipajeReserva;

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
			}
		}

		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en init(): " + (endTime - startTime) + " ms");
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
	}

	public void filtrarVuelosFuturos() {
		if (listaItinerarios == null) return;
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida() != null && it.getHoraSalida().after(ahora))
				.collect(Collectors.toList());
	}

	public void filtrarVuelosPasados() {
		if (listaItinerarios == null) return;
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida() != null && it.getHoraSalida().before(ahora))
				.collect(Collectors.toList());
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
		if (ri != null && ri.getReserva() != null && ri.getReserva().getEstadoReservaBean() != null) {
			String estadoNombre = ri.getReserva().getEstadoReservaBean().getDescripcion();
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

	public int getEtapaActualCronograma() {
		return etapaActualCronograma;
	}
}