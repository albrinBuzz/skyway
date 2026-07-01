package com.SkyWay.modules.ciudad.domain.repository;


import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {

    // Puedes agregar consultas personalizadas si es necesario
    // Ejemplo: Buscar por nombre de la ciudad
    List<Ciudad> findByNombre(String nombre);

    // Otras consultas personalizadas, si las necesitas
}
