package com.SkyWay.modules.tarifa.domain.repository;


import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Integer> {

    // Puedes definir métodos adicionales si los necesitas, por ejemplo:
    Tarifa findByNombre(String nombre);

    Optional<Tarifa> findByNombreIgnoreCase(String nombre);
    boolean existsByNombreIgnoreCaseAndIdTarifaNot(String nombre, Integer idTarifa);
}
