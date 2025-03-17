package com.SkyWay.repository;

import com.SkyWay.model.Piloto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Pasajero;

import java.util.Optional;

@Repository
public interface PasajeroRepository extends JpaRepository<Pasajero, String>{



    Optional<Pasajero> findByCorreoElectronico(String correoElectronico);
}
