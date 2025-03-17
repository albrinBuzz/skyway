package com.SkyWay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.PrecioAsiento;
import com.SkyWay.model.Vuelo;


@Repository
public interface PrecioAsientoRepository extends JpaRepository<PrecioAsiento, Integer> {
    // Puedes agregar consultas personalizadas si es necesario, usando Query Methods de Spring Data JPA.
	
	List<PrecioAsiento> findByVuelo(Vuelo vuelo);
}
