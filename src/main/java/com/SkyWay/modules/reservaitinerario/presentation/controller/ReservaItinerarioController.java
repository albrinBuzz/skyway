package com.SkyWay.modules.reservaitinerario.presentation.controller;

import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reserva-itinerarios")
public class ReservaItinerarioController {

    @Autowired
    private ReservaItinerarioService service;

    @GetMapping
    public List<ReservaItinerario> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaItinerario> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ReservaItinerario create(@RequestBody ReservaItinerario reservaItinerario) {
        return service.save(reservaItinerario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaItinerario> update(@PathVariable Integer id, @RequestBody ReservaItinerario reservaItinerario) {
        return service.findById(id)
                .map(existing -> {
                    reservaItinerario.setIdReservaItinerario(id);
                    return ResponseEntity.ok(service.update(reservaItinerario));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /*@DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        return service.findById(id)
                .map(existing -> {
                    service.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }*/
}
