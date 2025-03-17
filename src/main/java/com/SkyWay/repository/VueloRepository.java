package com.SkyWay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Piloto;
import com.SkyWay.model.Vuelo;

import jakarta.enterprise.context.ApplicationScoped;




@Repository
@ApplicationScoped
public interface VueloRepository extends JpaRepository<Vuelo, Integer>{

	List<Vuelo> findByPiloto(Piloto piloto);

	
}
