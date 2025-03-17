package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.model.Piloto;

public interface PiloService {

	
	public Piloto save(Piloto us);

	public Optional<Piloto>findByRut(String id);
	
	public void update(Piloto us);

	public void delete(Integer id);
	
	public List<Piloto>findAll();

	public void create(Piloto us);

	Optional<Piloto> findByCorreo(String correo);

	
	
}
