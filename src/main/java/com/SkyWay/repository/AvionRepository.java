package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Avion;

@Repository
public interface AvionRepository extends JpaRepository<Avion, Integer>{

	
}
