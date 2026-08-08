package com.SkyWay.modules.reservaitinerario.domain.repository;

import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ReservaItinerarioRepository extends JpaRepository<ReservaItinerario, Integer> {
    // Puedes agregar métodos personalizados si es necesario

    Optional<ReservaItinerario> findByReserva_IdReservaAndItinerario_IdItinerario(Integer idReserva, Integer idItinerario);
}
