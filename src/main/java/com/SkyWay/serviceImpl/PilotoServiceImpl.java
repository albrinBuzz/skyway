package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Piloto;
import com.SkyWay.repository.PilotoRepositoy;
import com.SkyWay.service.PiloService;

@Service
public class PilotoServiceImpl implements PiloService{

	@Autowired 
	private PilotoRepositoy pilotoRepositoy;
	
	@Override
	public Piloto save(Piloto us) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Piloto> findByRut(String id) {
		// TODO Auto-generated method stub
		return pilotoRepositoy.findById(id);
	}


	@Override
	public void update(Piloto us) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Piloto> findAll() {
		// TODO Auto-generated method stub
		return pilotoRepositoy.findAll();
	}

	@Override
	public void create(Piloto us) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<Piloto> findByCorreo(String correoElectronico) {
		return pilotoRepositoy.findByCorreoElectronico(correoElectronico);
	}

}
