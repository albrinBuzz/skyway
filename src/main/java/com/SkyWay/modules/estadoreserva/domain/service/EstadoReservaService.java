package com.SkyWay.modules.estadoreserva.domain.service;

import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import com.SkyWay.modules.estadoreserva.domain.repository.EstadoReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EstadoReservaService {

    @Autowired
    private EstadoReservaRepository estadoReservaRepository;

    // Create or update a EstadoReserva
    public EstadoReserva save(EstadoReserva reservationStatus) {
        return estadoReservaRepository.save(reservationStatus);
    }

    // Get all ReservationStatuses
    public List<EstadoReserva> findAll() {
        return estadoReservaRepository.findAll();
    }

    // Get a EstadoReserva by its ID
    public Optional<EstadoReserva> findById(Integer id) {
        return estadoReservaRepository.findById(id);
    }

    // Delete a EstadoReserva by its ID
    public void deleteById(Integer id) {
        estadoReservaRepository.deleteById(id);
    }

    // Check if a EstadoReserva exists by its ID
    public boolean existsById(Integer id) {
        return estadoReservaRepository.existsById(id);
    }
}
