package com.SkyWay.modules.tarifa.domain.repository;


import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    // Puedes definir métodos adicionales si los necesitas, por ejemplo:
    Tarifa findByNombre(String nombre);
}
