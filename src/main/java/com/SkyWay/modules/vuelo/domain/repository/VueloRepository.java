package com.SkyWay.modules.vuelo.domain.repository;

import java.util.List;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;




import jakarta.enterprise.context.ApplicationScoped;




@Repository
@ApplicationScoped
public interface VueloRepository extends JpaRepository<Vuelo, Integer>{

	List<Vuelo> findByPiloto(Piloto piloto);

	
}
