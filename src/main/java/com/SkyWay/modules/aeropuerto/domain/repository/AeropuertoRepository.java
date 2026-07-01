package com.SkyWay.modules.aeropuerto.domain.repository;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface AeropuertoRepository extends JpaRepository<Aeropuerto, Integer>{

}
