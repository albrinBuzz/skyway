package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.model.Asiento;
import com.SkyWay.model.Avion;
import com.SkyWay.model.ClaseAsiento;

public interface AsientoService {

	
	  public List<Asiento> findAll();
	  public Optional<Asiento> findById(Integer id);	  
	  public Avion save(Asiento vuelo);
	  public void deleteById(Integer id);
	  public List<InfoAsientoDTO> getAsientosDisponibles(Integer idAvion,Integer idVuelo);
	  public List<InfoAsientoDTO> getAsientosVuelo(Integer idReserva,Integer idVuelo);
	  public List<Asiento> findByAvion(Avion avion);
	  public List<ClaseAsiento> obtenerTodasLasClasesAsientos();
	
}
