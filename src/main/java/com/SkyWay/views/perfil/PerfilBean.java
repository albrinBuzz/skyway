package com.SkyWay.views.perfil;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator.TarifaCaracteristicaValidator;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioResumenDTO;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
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
//import org.omnifaces.cdi.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("perfilPasajeroView")
@ViewScoped
public class PerfilBean implements Serializable {
	private static final long serialVersionUID = 1L;

	@Autowired
	private ItinerarioService itinerarioService;

	@Autowired
	private PiloService piloService;

	@Autowired
	private ReservaService reservaService;

	@Autowired
	private AsientoService asientoService;

	@Autowired
	private ReservaItinerarioService reservaItinerarioService;

	@Autowired
	private ItinerarioTarifaService itinerarioTarifaService;

	@Autowired
	private TarifaCaracteristicaValidator tarifaValidator;

	@Autowired
	private VueloService vueloService;

	private List<ItinerarioResumenDTO> listaItinerarios;
	private List<ItinerarioResumenDTO> listaItinerariosFiltrados;
	private Itinerario itinerarioDetalle;
	private List<InfoAsientoReservaDTO> listaAsientosReservados;

	private LocalDate fechaInicioFiltro;
	private LocalDate fechaFinFiltro;
	private List<TicketInfo> ticketInfos;
	private Tarifa tarifaActual;
	private Usuario usuario;

	// Cache local en memoria para evitar consultas N+1 repetidas a la BD por cada asiento
	private final Map<Integer, String> numeroVueloCache = new HashMap<>();

	@PostConstruct
	public void init() {
		long startTime = System.currentTimeMillis();

		// ✅ Obtención segura de la sesión HTTP a través de FacesContext
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null && facesContext.getExternalContext() != null) {
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
			if (session != null) {
				usuario = (Usuario) session.getAttribute("usuario");
			}
		}

		if (usuario != null && usuario.getRoles() != null) {
			for (Role role : usuario.getRoles()) {
				if ("Pasajero".equalsIgnoreCase(role.getNombre())) {
					listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);
					filtrarVuelosHoy();
					break;
				}
			}
		}

		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en init(): " + (endTime - startTime) + " ms");
	}

	public void filtrarVuelosFuturos() {
		if (listaItinerarios == null) return;
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida() != null && it.getHoraSalida().after(ahora))
				.collect(Collectors.toList());
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

	public void filtrarVuelosPasados() {
		if (listaItinerarios == null) return;
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida() != null && it.getHoraSalida().before(ahora))
				.collect(Collectors.toList());
	}

	public void verDetalleItinerario(ItinerarioResumenDTO itinerario) {
		if (itinerario == null) return;
		this.itinerarioDetalle = itinerarioService.findById(itinerario.getIdItinerario());
	}

	public void verAsientoItinerario(ItinerarioResumenDTO itinerario) {
		if (itinerario == null) return;

		listaAsientosReservados = asientoService.getAsientosReservados(itinerario.getIdItinerario(), itinerario.getIdReserva());

		var reservaItinerario = reservaItinerarioService.obtenerReservaItinerario(itinerario.getIdReserva(), itinerario.getIdItinerario());

		if (reservaItinerario != null && reservaItinerario.getItinerarioTarifa() != null) {
			tarifaActual = reservaItinerario.getItinerarioTarifa().getTarifa();
		}
	}

	public void cambiarAsiento(InfoAsientoReservaDTO infoAsientoDTO) throws IOException {
		if (tarifaActual != null && !tarifaValidator.permiteCambio(tarifaActual.getTarifaCaracteristicas())) {
			Logger.logInfo("La tarifa no permite cambios para la reserva/itinerario actual.");

			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cambio No Permitido", "Tu tarifa asignada no permite realizar cambios de asiento."));
		} else {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("/perfil/pasajero/cambioAsiento.xhtml?idVuelo=" + infoAsientoDTO.getIdVuelo()
							+ "&idReserva=" + infoAsientoDTO.getIdReserva());
		}
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

	// Getters y Setters
	public LocalDate getFechaInicioFiltro() { return fechaInicioFiltro; }
	public void setFechaInicioFiltro(LocalDate fechaInicioFiltro) { this.fechaInicioFiltro = fechaInicioFiltro; }

	public LocalDate getFechaFinFiltro() { return fechaFinFiltro; }
	public void setFechaFinFiltro(LocalDate fechaFinFiltro) { this.fechaFinFiltro = fechaFinFiltro; }

	public Usuario getUsuario() {
		return usuario;
	}

	public List<ItinerarioResumenDTO> getListaItinerarios() { return listaItinerarios; }
	public List<ItinerarioResumenDTO> getListaItinerariosFiltrados() { return listaItinerariosFiltrados; }
	public Itinerario getItinerarioDetalle() { return itinerarioDetalle; }
	public List<InfoAsientoReservaDTO> getListaAsientosReservados() { return listaAsientosReservados; }
	public List<TicketInfo> getTicketInfos() { return ticketInfos; }
}