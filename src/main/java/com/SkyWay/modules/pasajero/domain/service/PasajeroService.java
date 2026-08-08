package com.SkyWay.modules.pasajero.domain.service;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;

import java.util.List;
import java.util.Optional;



public interface PasajeroService {

	  public List<Pasajero> findAll();
	  public Optional<Pasajero> findById(String id);	  
	  public Pasajero save(Pasajero pasajero);
	  public void deleteById(String id);


	  
	
}
