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
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import com.SkyWay.modules.rolusuario.domain.model.Role;
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
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
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

		context.getExternalContext()
				.redirect("/perfil/pasajero/cambioAsiento.xhtml?idVuelo=" + infoAsientoDTO.getIdVuelo()
						+ "&idReserva=" + infoAsientoDTO.getIdReserva());
	}

	public void getTicket(ItinerarioResumenDTO itinerario) {
		if (itinerario == null || usuario == null) return;
		ticketInfos = reservaService.getTicket(usuario.getRut(), itinerario.getIdReserva(), itinerario.getIdItinerario());
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
		long horasHastaSalida = duracionHastaSalida.toHours();

		if (horasHastaSalida > 48) {
			this.checkInAbierto = false;
			this.estadoCheckInTexto = String.format("Abre en %d días", (horasHastaSalida - 48) / 24 + 1);
			this.porcentajeCheckIn = 15.0;
		} else if (horasHastaSalida >= 1 && segundosHastaSalida > 0) {
			this.checkInAbierto = true;
			this.estadoCheckInTexto = "CHECK-IN ABIERTO";
			double horasTranscurridas = 48.0 - (segundosHastaSalida / 3600.0);
			this.porcentajeCheckIn = Math.min(100.0, Math.max(20.0, (horasTranscurridas / 47.0) * 100.0));
		} else {
			this.checkInAbierto = false;
			this.estadoCheckInTexto = "CHECK-IN CERRADO";
			this.porcentajeCheckIn = 100.0;
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
		context.getExternalContext().redirect(
				context.getExternalContext().getRequestContextPath() +
						"/home/checkin.xhtml?idReserva=" + itinerarioSeleccionado.getIdReserva() +
						"&idItinerario=" + itinerarioSeleccionado.getIdItinerario()
		);
	}

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