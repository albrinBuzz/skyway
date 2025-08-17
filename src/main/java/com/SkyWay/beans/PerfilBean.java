package com.SkyWay.beans;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.model.Piloto;
import com.SkyWay.model.RolEnum;
import com.SkyWay.model.Usuario;
import com.SkyWay.model.Vuelo;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

@Named("perfilView")
@RequestScoped
public class PerfilBean {

	@Autowired
	private ReservaService reservaService;

	@Autowired
	private HttpSession session;

	List<ReservaVueloDTO>reservas;
	
	private List<Vuelo>vuelos;

	@Autowired
	private VueloService vueloService;
	
	
	@PostConstruct
	public void init() {

		//	Usuario usuario=(Usuario) new UserBean(session).getUsuario();
		Usuario usuario=	(Usuario) session.getAttribute("usuario");
	   //System.out.println(usuario);
	
		if(usuario.getRol().getNombre().equals(RolEnum.PASAJERO.getDescripcion())) {
			reservas=reservaService.getReservasUsuario(usuario.getRutUsuario());
			
		}else if (usuario.getRol().getNombre().equals(RolEnum.PILOTO.getDescripcion())) {
			Piloto piloto=(Piloto) usuario;
			//vuelos=vueloService.findByPiloto(piloto);
		
		}
			
	}
	
	public void cambiarAsiento(Integer idVuelo ,Integer idReserva) {
		
		System.out.println("reserva :"+idReserva+" Vuelo: "+idVuelo);
		
	}
	
	
	 public String getReservaRowClass(String estadoReserva) {
	        if ("Vuelo Programado".equals(estadoReserva)) {
	            return "table-success"; // Fila verde para vuelo programado
	        } else if ("Vuelo Cancelado".equals(estadoReserva)) {
	            return "table-danger"; // Fila roja para vuelo cancelado
	        } else if ("Vuelo Retrasado".equals(estadoReserva)) {
	            return "table-warning"; // Fila amarilla para vuelo retrasado
	        } else {
	            return ""; // Sin clase especial
	        }
	    }
	
	public List<Vuelo> getVuelos() {
		return vuelos;
	}
	
	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public List<ReservaVueloDTO> getReservas() {
		return reservas;
	}
	public void setReservas(List<ReservaVueloDTO> reservas) {
		this.reservas = reservas;
	}

	
}
