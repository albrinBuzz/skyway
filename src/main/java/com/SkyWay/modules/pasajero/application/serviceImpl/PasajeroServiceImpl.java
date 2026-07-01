package com.SkyWay.modules.pasajero.application.serviceImpl;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class PasajeroServiceImpl implements PasajeroService {

	@Autowired
	private PasajeroRepository pasajeroRepository;

	
	@Override
	public List<Pasajero> findAll() {
		// TODO Auto-generated method stub
		return pasajeroRepository.findAll();
	}

	@Override
	public Optional<Pasajero> findById(String id) {
		// TODO Auto-generated method stub
		return pasajeroRepository.findById(id);
	}

	@Override
	public Pasajero save(Pasajero pasajero) {
		// TODO Auto-generated method stub
		return pasajeroRepository.save(pasajero);
	}

	@Override
	public void deleteById(String id) {
		// TODO Auto-generated method stub
		pasajeroRepository.deleteById(id);
		
	}


}
