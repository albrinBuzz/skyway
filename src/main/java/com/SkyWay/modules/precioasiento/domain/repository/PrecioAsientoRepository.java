package com.SkyWay.modules.precioasiento.domain.repository;

import java.util.List;

import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




@Repository
public interface PrecioAsientoRepository extends JpaRepository<PrecioAsiento, Integer> {
    // Puedes agregar consultas personalizadas si es necesario, usando Query Methods de Spring Data JPA.
	
	List<PrecioAsiento> findByVuelo(Vuelo vuelo);
}
