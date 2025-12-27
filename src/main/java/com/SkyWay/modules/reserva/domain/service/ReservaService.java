package com.SkyWay.modules.reserva.domain.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.SkyWay.dto.BoletoDTO;
import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;

import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReservaService {

	  public List<Reserva> findAll();
	  public Optional<Reserva> findById(Integer id);	  
	  public Reserva save(Reserva reserva);
	  public void deleteById(Integer id);
	  public List<ReservaVueloDTO>getReservasUsuario(String rut);
	  public void cancelarReserva(Integer id);
	  public BoletoDTO getBoleto(Integer id);
	  public List<TicketInfo>getTicket(String rut,Integer idReserva,int idItinerario);
	  List<Reserva> obtenerReservasPorPasajero(Pasajero pasajero);
	  List<Reserva> obtenerReservasPorRut(String rut);
		Page<Reserva> obtenerReservasPorRut(String rut, Pageable pageable);

	String confirmarReserva(int idVuelo, Integer[] asientosArray, String rutUsuario,Integer idReserva) throws SQLException;
}
