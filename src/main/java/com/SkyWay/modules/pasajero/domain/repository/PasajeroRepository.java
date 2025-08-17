package com.SkyWay.modules.pasajero.domain.repository;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface PasajeroRepository extends JpaRepository<Pasajero, String>{



}
