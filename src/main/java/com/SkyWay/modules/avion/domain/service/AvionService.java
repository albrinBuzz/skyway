package com.SkyWay.modules.avion.domain.service;

import com.SkyWay.modules.avion.domain.model.Avion;

import java.util.List;
import java.util.Optional;



public interface AvionService {

	  public List<Avion> findAll();
	  public Optional<Avion> findById(Integer id);	  
	  public Avion save(Avion vuelo);
	  public boolean deleteById(Integer id);
	  public Avion updateAvion(Avion avion);
	  
	
}
