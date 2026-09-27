package com.SkyWay.modules.fabricante.domain.repository;

import com.SkyWay.modules.fabricante.domain.model.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FabricanteRepository extends JpaRepository<Fabricante, Integer> {

    /**
     * Busca un fabricante por su nombre exacto ignora mayúsculas/minúsculas.
     */
    Optional<Fabricante> findByNombreIgnoreCase(String nombre);

    /**
     * Verifica si existe un fabricante con el mismo nombre.
     */
    boolean existsByNombreIgnoreCase(String nombre);
}