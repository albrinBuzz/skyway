package com.SkyWay.modules.reservaitinerario.domain.service;

import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.repository.ReservaItinerarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReservaItinerarioService {

    @Autowired
    private ReservaItinerarioRepository reservaItinerarioRepository;

    public List<ReservaItinerario> findAll() {
        return reservaItinerarioRepository.findAll();
    }

    public Optional<ReservaItinerario> findById(Integer id) {
        return reservaItinerarioRepository.findById(id);
    }

    public ReservaItinerario save(ReservaItinerario reservaItinerario) {
        return reservaItinerarioRepository.save(reservaItinerario);
    }

    public ReservaItinerario update(ReservaItinerario reservaItinerario) {
        // Se asume que el objeto ya tiene un ID válido
        return reservaItinerarioRepository.save(reservaItinerario);
    }

    public void deleteById(Integer id) {
        reservaItinerarioRepository.deleteById(id);
    }

    public void delete(ReservaItinerario reservaItinerario) {
        reservaItinerarioRepository.delete(reservaItinerario);
    }
}
