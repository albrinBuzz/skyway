package com.SkyWay.modules.asiento.domain.repository;

import java.util.List;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.avion.domain.model.Avion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Integer>{

	List<Asiento> findByAvion(Avion avion);
	
}
