package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.SkyWay.model.Piloto;

import java.util.Optional;

@Repository
public interface PilotoRepositoy extends JpaRepository<Piloto, String>{

    @Query("select p FROM Piloto p where p.correoElectronico = ?1")
    Optional<Piloto> findByCorreoElectronico(String correoElectronico);


}
