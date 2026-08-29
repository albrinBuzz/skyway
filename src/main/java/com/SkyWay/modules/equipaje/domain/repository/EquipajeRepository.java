package com.SkyWay.modules.equipaje.domain.repository;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface EquipajeRepository extends JpaRepository<Equipaje, Long> {

    // Buscar todos los equipajes de una reserva específica
    List<Equipaje> findByReservaIdReserva(Long idReserva);

    // Buscar equipajes asignados a un pasajero por su RUT
    List<Equipaje> findByPasajeroRut(String rut);

    // Sumar el peso total registrado por un pasajero en una reserva específica
    @Query("SELECT COALESCE(SUM(e.peso), 0) FROM Equipaje e WHERE e.reserva.idReserva = :idReserva AND e.pasajero.rut = :rut")
    BigDecimal sumPesoByReservaAndPasajero(@Param("idReserva") Long idReserva, @Param("rut") String rut);

    // Contar cuántos equipajes de un tipo específico tiene asignada una reserva
    long countByReservaIdReservaAndTipoEquipajeIdTipo(Long idReserva, Long idTipoEquipaje);
}