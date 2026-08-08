package com.SkyWay.modules.precioasiento.domain.repository;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;




@Repository
public interface PrecioAsientoRepository extends JpaRepository<PrecioAsiento, Integer> {
    // Puedes agregar consultas personalizadas si es necesario, usando Query Methods de Spring Data JPA.
	
	List<PrecioAsiento> findByVuelo(Vuelo vuelo);

	@Query("SELECT p FROM PrecioAsiento p WHERE p.vuelo.idVuelo = :idVuelo AND p.claseAsiento.idClase = :idClase")
	Optional<PrecioAsiento> findByVueloAndClase(@Param("idVuelo") Integer idVuelo, @Param("idClase") Integer idClase);
}
