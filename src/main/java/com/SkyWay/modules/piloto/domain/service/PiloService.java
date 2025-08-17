package com.SkyWay.modules.piloto.domain.service;

import com.SkyWay.modules.piloto.domain.model.Piloto;

import java.util.List;
import java.util.Optional;



public interface PiloService {

	
	public Piloto save(Piloto us);

	public Optional<Piloto>findByRut(String id);
	
	public void update(Piloto us);

	public void delete(Integer id);
	
	public List<Piloto>findAll();

	public void create(Piloto us);

	Optional<Piloto> findByCorreo(String correo);

	
	
}
