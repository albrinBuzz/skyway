package com.SkyWay.modules.reserva.domain.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.SkyWay.dto.BoletoDTO;
import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.modules.reserva.domain.model.Reserva;


public interface ReservaService {

	  public List<Reserva> findAll();
	  public Optional<Reserva> findById(Integer id);	  
	  public Reserva save(Reserva reserva);
	  public void deleteById(Integer id);
	  public List<ReservaVueloDTO>getReservasUsuario(String rut);
	  public void cancelarReserva(Integer id);
	  public BoletoDTO getBoleto(Integer id);


    String confirmarReserva(int idVuelo, Integer[] asientosArray, String rutUsuario,Integer idReserva) throws SQLException;
}
