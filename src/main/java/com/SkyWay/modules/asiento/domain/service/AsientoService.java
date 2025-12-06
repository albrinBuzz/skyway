package com.SkyWay.modules.asiento.domain.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;


public interface AsientoService {

	
	  public List<Asiento> findAll();
	  public Optional<Asiento> findById(Integer id);	  
	  public Avion save(Asiento vuelo);
	  public void deleteById(Integer id);
	  public List<InfoAsientoDTO> getAsientosDisponibles(Integer idVuelo);
	  public List<InfoAsientoDTO> getAsientosVuelo(Integer idReserva,Integer idVuelo);
	public List<InfoAsientoReservaDTO> getAsientosReservados(Integer idReserva, Integer idItinerario);
	public String verificarDisponibilidad (int idVuelo, Integer[] asientos) throws SQLException;

	public List<Asiento> findByAvion(Avion avion);
	  public List<ClaseAsiento> obtenerTodasLasClasesAsientos();
	
}
