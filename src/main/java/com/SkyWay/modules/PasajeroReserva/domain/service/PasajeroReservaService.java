package com.SkyWay.modules.PasajeroReserva.domain.service;


import com.SkyWay.modules.PasajeroReserva.domain.model.PasajeroReserva;
import com.SkyWay.modules.PasajeroReserva.domain.repository.PasajeroReservaRepository;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PasajeroReservaService {

    private final PasajeroReservaRepository repository;

    @Autowired
    public PasajeroReservaService(PasajeroReservaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PasajeroReserva> obtenerPasajerosPorReserva(Reserva reserva) {
        return repository.findByReserva(reserva);
    }

    @Transactional
    public PasajeroReserva agregarPasajeroAReserva(Reserva reserva, Pasajero pasajero) {
        Optional<PasajeroReserva> existente = repository.findByReservaAndPasajero(reserva, pasajero);
        if (existente.isPresent()) {
            throw new IllegalArgumentException("El pasajero ya está registrado en esta reserva.");
        }
        PasajeroReserva pr = new PasajeroReserva();
        pr.setReserva(reserva);
        pr.setPasajero(pasajero);
        return repository.save(pr);
    }

    @Transactional
    public void eliminarPasajeroDeReserva(Reserva reserva, Pasajero pasajero) {
        repository.findByReservaAndPasajero(reserva, pasajero)
                .ifPresent(repository::delete);
    }

    @Transactional
    public void eliminarTodosLosPasajerosDeReserva(Reserva reserva) {
        repository.deleteByReserva(reserva);
    }
}
