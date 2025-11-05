package com.SkyWay.beans;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;


import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioResumenDTO;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;
import jakarta.faces.view.ViewScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import com.SkyWay.dto.ReservaVueloDTO;




import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
@Named("perfilView")
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

	private List<ItinerarioResumenDTO> listaItinerarios;             // Todos los itinerarios
	private List<ItinerarioResumenDTO> listaItinerariosFiltrados;    // Itinerarios filtrados
	private Itinerario itinerarioDetalle;
	private List<InfoAsientoReservaDTO> listaAsientosReservados;

	private LocalDate fechaInicioFiltro;
	private LocalDate fechaFinFiltro;


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
		Logger.logInfo("buscando los asientos para "+itinerario.getIdReserva()+"-"+itinerario.getIdItinerario());

		listaAsientosReservados=asientoService.getAsientosReservados(itinerario.getIdItinerario(),itinerario.getIdReserva());

		/*for (InfoAsientoReservaDTO asientoReservaDTO : listaAsientosReservados) {
			Logger.logInfo(asientoReservaDTO.toString());
		}*/

		//this.itinerarioDetalle = itinerarioService.findById(itinerario.getIdItinerario());;
		PrimeFaces.current().executeScript("PF('dialogAsientos').show();");
	}
	public void getTicket(ItinerarioResumenDTO itinerario){
		Logger.logInfo("Obteniendo el ticket para  "+itinerario.getIdReserva()+"-"+itinerario.getIdItinerario());

		var ticket= reservaService.getTicket(usuario.getRut(),itinerario.getIdReserva());
		for (TicketInfo ticketInfo : ticket) {
			Logger.logInfo(ticketInfo.toString());
		}
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
}

