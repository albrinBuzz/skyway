package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.Pasajero;

public interface PasajeroService {

	  public List<Pasajero> findAll();
	  public Optional<Pasajero> findById(String id);	  
	  public Pasajero save(Pasajero pasajero);
	  public void deleteById(String id);
	 Optional<Pasajero> findByCorreoElectronico(String correoElectronico);


	  
	
}
