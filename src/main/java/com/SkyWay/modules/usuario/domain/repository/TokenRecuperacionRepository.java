package com.SkyWay.modules.usuario.domain.repository;

import com.SkyWay.modules.usuario.domain.model.TokenRecuperacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Repositorio JPA para la persistencia y ciclo de vida de tokens de recuperación de contraseña.
 */
@Repository
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {

    Optional<TokenRecuperacion> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TokenRecuperacion t WHERE t.tokenHash = :tokenHash")
    Optional<TokenRecuperacion> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    Optional<TokenRecuperacion> findFirstByRutUsuarioAndUsadoFalseAndRevocadoFalseOrderByFechaCreacionDesc(String rutUsuario);

    @Modifying
    @Query("UPDATE TokenRecuperacion t SET t.revocado = true WHERE t.rutUsuario = :rutUsuario AND t.usado = false AND t.revocado = false")
    int revocarTokensActivosPorUsuario(@Param("rutUsuario") String rutUsuario);

    @Modifying
    @Query("DELETE FROM TokenRecuperacion t WHERE t.fechaExpiracion < :limite")
    int eliminarExpiradosAntesDe(@Param("limite") OffsetDateTime limite);
}
