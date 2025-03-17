package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.Avion;

public interface AvionService {

	  public List<Avion> findAll();
	  public Optional<Avion> findById(Integer id);	  
	  public Avion save(Avion vuelo);
	  public boolean deleteById(Integer id);
	  public Avion updateAvion(Avion avion);
	  
	
}
