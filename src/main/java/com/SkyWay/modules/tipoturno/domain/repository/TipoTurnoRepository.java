package com.SkyWay.modules.tipoturno.domain.repository;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;

@Repository
public interface TipoTurnoRepository extends JpaRepository<TipoTurno, Integer> {

    java.util.List<TipoTurno> findByActivoTrue();

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM tipo_turno ORDER BY id_tipo ASC", nativeQuery = true)
    java.util.List<TipoTurno> findAllAdmin();

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE tipo_turno SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_tipo = :id", nativeQuery = true)
    void softDeleteById(@org.springframework.data.repository.query.Param("id") Integer id);

    @org.springframework.data.jpa.repository.Modifying(clearAutomatically = true)
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "UPDATE tipo_turno SET activo = true, fecha_baja = NULL WHERE id_tipo = :id", nativeQuery = true)
    void reactivarById(@org.springframework.data.repository.query.Param("id") Integer id);
    // Aquí podrías añadir consultas personalizadas como:
    // Optional<TipoTurno> findByNombre(String nombre);
}