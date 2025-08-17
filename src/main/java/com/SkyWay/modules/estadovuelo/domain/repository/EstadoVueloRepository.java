package com.SkyWay.modules.estadovuelo.domain.repository;

import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface EstadoVueloRepository extends JpaRepository<EstadoVuelo, Integer> {
    // Aquí puedes agregar métodos personalizados si es necesario
}
