package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Pasajero;
import com.SkyWay.repository.PasajeroRepository;
import com.SkyWay.service.PasajeroService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

@Service
public class PasajeroServiceImpl implements PasajeroService{

	@Autowired
	
	private PasajeroRepository pasajeroRepository;
	
    private EntityManagerFactory emf = null;
    
	  public EntityManager getEntityManager() {
		  	emf = Persistence.createEntityManagerFactory("mainPU");
	        return emf.createEntityManager();
	    }
	  
	
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

	@Override
	public Optional<Pasajero> findByCorreoElectronico(String correoElectronico) {
		return pasajeroRepository.findByCorreoElectronico(correoElectronico);
	}
}
