package com.SkyWay.modules.continente.domain.repository;

import com.SkyWay.modules.continente.domain.model.Continente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContinenteRepository extends JpaRepository<Continente, Integer> {

    List<Continente> findAllByOrderByNombreAsc();

    Optional<Continente> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}