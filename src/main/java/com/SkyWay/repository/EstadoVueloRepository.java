package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.EstadoVuelo;

@Repository
public interface EstadoVueloRepository extends JpaRepository<EstadoVuelo, Integer> {
    // Aquí puedes agregar métodos personalizados si es necesario
}
