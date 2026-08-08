package com.SkyWay.modules.avion.domain.repository;

import com.SkyWay.modules.avion.domain.model.Avion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface AvionRepository extends JpaRepository<Avion, Integer>{

	
}
