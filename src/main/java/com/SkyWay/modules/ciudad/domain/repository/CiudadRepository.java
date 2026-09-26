package com.SkyWay.modules.ciudad.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CiudadRepository extends JpaRepository<Ciudad, Integer> {

    List<Ciudad> findByNombre(String nombre);

    List<Ciudad> findByPaiIdPaisOrderByNombreAsc(Integer idPais);

    Optional<Ciudad> findByNombreIgnoreCaseAndPaiIdPais(String nombre, Integer idPais);

    @Query("SELECT c FROM Ciudad c JOIN FETCH c.pai WHERE LOWER(c.nombre) = LOWER(:nombre)")
    List<Ciudad> buscarConPaisPorNombre(@Param("nombre") String nombre);
}
