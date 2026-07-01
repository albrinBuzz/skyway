package com.SkyWay.modules.piloto.domain.repository;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



import java.util.Optional;

@Repository
public interface PilotoRepositoy extends JpaRepository<Piloto, String>{

    @Query("select p FROM Piloto p where p.rut = ?1")
    Optional<Piloto> findByCorreoElectronico(String correoElectronico);


}
