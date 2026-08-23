package com.SkyWay.views.perfil;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator.TarifaCaracteristicaValidator;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
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
//import jakarta.faces.view.ViewScoped;
import org.omnifaces.cdi.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
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
	private HttpSession session;

	@Autowired
	private ReservaItinerarioService reservaItinerarioService;

	@Autowired
	private ItinerarioTarifaService itinerarioTarifaService;

	@Autowired
	private TarifaCaracteristicaValidator tarifaValidator;

	private List<ItinerarioResumenDTO> listaItinerarios;             // Todos los itinerarios
	private List<ItinerarioResumenDTO> listaItinerariosFiltrados;    // Itinerarios filtrados
	private Itinerario itinerarioDetalle;
	private List<InfoAsientoReservaDTO> listaAsientosReservados;

	private LocalDate fechaInicioFiltro;
	private LocalDate fechaFinFiltro;
	private List<TicketInfo>ticketInfos;
	private Tarifa tarifaActual;


	@Autowired
	private VueloService vueloService;

	private List<Vuelo> vuelos;
	Usuario usuario;
	@PostConstruct
	public void init() {
		long startTime = System.currentTimeMillis();


		 usuario = (Usuario) session.getAttribute("usuario");

		for (Role role : usuario.getRoles()) {
			Logger.logInfo(role.getNombre());

			if ("Pasajero".equals(role.getNombre())) {
				long subStart = System.currentTimeMillis();

				//reservaService.obtenerReservasPorRut(usuario.getRut());

				// Obtener resumen optimizado
				listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);

				long subEnd = System.currentTimeMillis();
				Logger.logInfo("⏱ Tiempo en obtener los itinerarios: " + (subEnd - subStart) + " ms");

				// Filtro inicial
				//filtrarVuelosFuturos();
				filtrarVuelosHoy();
			}
		}

		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en init(): " + (endTime - startTime) + " ms");
	}

	public void filtrarVuelosFuturos() {
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida().after(ahora))
				.collect(Collectors.toList());

	}

	public void filtrarVuelosHoy() {

		Logger.logInfo("buscando Vuelos de hoy");

		LocalDate hoy = LocalDate.now();

		long startTime = System.currentTimeMillis();

		listaItinerariosFiltrados = itinerarioService.buscarConFiltroFechas(
				usuario.getRut(),
				hoy,
				hoy,
				100,
				0
		);



		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en buscar vuelos de hoy: " + (endTime - startTime) + " ms");
		Logger.logInfo("⏱ total itinerarios " +listaItinerariosFiltrados.size());


	}

	public void filtrarVuelosPasados() {
		Date ahora = new Date();
		listaItinerariosFiltrados = listaItinerarios.stream()
				.filter(it -> it.getHoraSalida().before(ahora))
				.collect(Collectors.toList());
	}

	public void verDetalleItinerario(ItinerarioResumenDTO itinerario) {

		this.itinerarioDetalle = itinerarioService.findById(itinerario.getIdItinerario());;
		PrimeFaces.current().executeScript("PF('dlgDetalle').show();");
	}

	public void verAsientoItinerario(ItinerarioResumenDTO itinerario) {
		Logger.logInfo("IdReserva: "+itinerario.getIdReserva()+" IdItinerario: "+itinerario.getIdItinerario());

		listaAsientosReservados=asientoService.getAsientosReservados(itinerario.getIdItinerario(),itinerario.getIdReserva());

		var reservaItinerario= reservaItinerarioService.obtenerReservaItinerario(itinerario.getIdReserva(),itinerario.getIdItinerario());

		tarifaActual =reservaItinerario.getItinerarioTarifa().getTarifa();
		Logger.logInfo(tarifaActual.toString());



		//itinerarioTarifaService.getTarifasItinerario(itinerario.getIdItinerario());

		for (InfoAsientoReservaDTO asientoReservaDTO : listaAsientosReservados) {
			Logger.logInfo(asientoReservaDTO.toString());
		}

		//this.itinerarioDetalle = itinerarioService.findById(itinerario.getIdItinerario());;
		PrimeFaces.current().executeScript("PF('dialogAsientos').show();");
	}
	public void cambiarAsiento(InfoAsientoReservaDTO infoAsientoDTO) throws IOException {
		if (!tarifaValidator.permiteCambio(tarifaActual.getTarifaCaracteristicas())) {
			Logger.logInfo("la tarifa no permite cambios");


			//FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Error","La tarifa no permite cambios");

			//PrimeFaces.current().dialog().showMessageDynamic(message);

			//addMessage(FacesMessage.SEVERITY_ERROR, "Error", "La tarifa no permite cambios");
			FacesContext.getCurrentInstance().
					addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "La tarifa no permite cambios"));

			// No redirect aquí para que el mensaje se muestre en la misma vista
		} else {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect("/perfil/pasajero/cambioAsiento.xhtml?idVuelo=" + infoAsientoDTO.getIdVuelo()
							+ "&idReserva=" + infoAsientoDTO.getIdReserva());
		}
	}

	public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
		FacesContext.getCurrentInstance().
				addMessage(null, new FacesMessage(severity, summary, detail));
	}

	public void getTicket(ItinerarioResumenDTO itinerario){
		Logger.logInfo("Obteniendo el ticket para  "+itinerario.getIdReserva()+"-"+itinerario.getIdItinerario());
		Logger.logInfo("Obteniendo el ticket para  reserva "+itinerario.getIdReserva()+" Itinerario "+itinerario.getIdItinerario()+
				" Pasajero "+usuario.getRut());

		ticketInfos= reservaService.getTicket(usuario.getRut(),itinerario.getIdReserva(),itinerario.getIdItinerario());
		//Logger.logInfo(ticketInfos.toString());

		for (TicketInfo ticketInfo : ticketInfos) {
			Logger.logInfo(ticketInfo.toString());
		}
		System.out.println("\n");
	}

	public String getNumeroVuelo(int idVuelo){
	 return 	vueloService.findById(idVuelo).get().getNumeroVuelo();
	}

	public void buscarPorRangoFechas() {
		Usuario usuario = (Usuario) session.getAttribute("usuario");

		Logger.logInfo("buscando por fecha");
		Logger.logInfo(fechaInicioFiltro.toString());
		Logger.logInfo(fechaFinFiltro.toString());

		LocalDate inicio = (fechaInicioFiltro != null) ? LocalDate.from(fechaInicioFiltro.atStartOfDay()) : null;
		LocalDate fin = (fechaFinFiltro != null) ? LocalDate.from(fechaFinFiltro.atTime(LocalTime.MAX)) : null;


		long startTime = System.currentTimeMillis();

		listaItinerariosFiltrados = itinerarioService.buscarConFiltroFechas(
				usuario.getRut(),
				inicio,
				fin,
				100,
				0
		);



		long endTime = System.currentTimeMillis();
		Logger.logInfo("⏱ Tiempo total en BuscarFechas(): " + (endTime - startTime) + " ms");
		Logger.logInfo("⏱ total itinerarios " +listaItinerariosFiltrados.size());

	}




	public void setFechaFinFiltro(LocalDate fechaFinFiltro) {
		this.fechaFinFiltro = fechaFinFiltro;
	}

	public void setFechaInicioFiltro(LocalDate fechaInicioFiltro) {
		this.fechaInicioFiltro = fechaInicioFiltro;
	}

	public LocalDate getFechaFinFiltro() {
		return fechaFinFiltro;
	}

	public LocalDate getFechaInicioFiltro() {
		return fechaInicioFiltro;
	}

	public List<ItinerarioResumenDTO> getListaItinerarios() {
		return listaItinerarios;
	}

	public List<ItinerarioResumenDTO> getListaItinerariosFiltrados() {
		return listaItinerariosFiltrados;
	}

	public Itinerario getItinerarioDetalle() {
		return itinerarioDetalle;
	}


	public List<Vuelo> getVuelos() {
		return vuelos;
	}

	public List<InfoAsientoReservaDTO> getListaAsientosReservados() {
		return listaAsientosReservados;
	}

	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public List<TicketInfo> getTicketInfos() {
		return ticketInfos;
	}

	public void setTicketInfos(List<TicketInfo> ticketInfos) {
		this.ticketInfos = ticketInfos;
	}
}

