package com.SkyWay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Asiento;
import com.SkyWay.model.Avion;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Integer>{

	List<Asiento> findByAvion(Avion avion);
	
}
