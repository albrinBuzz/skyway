package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Avion;
import com.SkyWay.repository.AvionRepository;
import com.SkyWay.service.AvionService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
@Service
public class AvionServiceImpl implements AvionService{

	@Autowired
	private AvionRepository avionRepository;

	@Autowired
	private EntityManager em;
	
	@Autowired
    private  DataSource dataSource;
	
    private EntityManagerFactory emf = null;
    
	  public EntityManager getEntityManager() {
		  	emf = Persistence.createEntityManagerFactory("mainPU");
	        return emf.createEntityManager();
	    }
	  
	
	
	@Override
	public List<Avion> findAll() {
		// TODO Auto-generated method stub
		return avionRepository.findAll();
	}

	@Override
	public Optional<Avion> findById(Integer id) {
		// TODO Auto-generated method stub
		return avionRepository.findById(id);
	}

	@Override
	public Avion save(Avion vuelo) {
		// TODO Auto-generated method stub

		return avionRepository.save(vuelo);
	}

	@Override
	public  boolean deleteById(Integer id) {
		// TODO Auto-generated method stub
		if(avionRepository.existsById(id)) {
			avionRepository.deleteById(id);
			return true;
		}
		
		return false;
	
	}



	@Override
	public Avion updateAvion(Avion avion) {
		// TODO Auto-generated method stub 

		return avionRepository.save(avion);
	}
	
	
	
}
