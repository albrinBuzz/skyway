package com.SkyWay.modules.tipoturno.domain.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;

@Repository
public interface TipoTurnoRepository extends JpaRepository<TipoTurno, Integer> {
    // Aquí podrías añadir consultas personalizadas como:
    // Optional<TipoTurno> findByNombre(String nombre);
}