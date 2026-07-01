package com.SkyWay.modules.puertaembarque.presentation.controller;

import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import com.SkyWay.modules.puertaembarque.domain.service.PuertaEmbarqueService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puertas")
public class PuertaEmbarqueController {

    @Autowired
    private PuertaEmbarqueService puertaService;

    @GetMapping
    public List<PuertaEmbarque> getAllPuertas() {
        return puertaService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PuertaEmbarque> getPuertaById(@PathVariable Integer id) {
        return puertaService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PuertaEmbarque> createPuerta(@RequestBody PuertaEmbarque puerta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(puertaService.create(puerta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PuertaEmbarque> updatePuerta(@PathVariable Integer id, @RequestBody PuertaEmbarque puerta) {
        try {
            return ResponseEntity.ok(puertaService.update(id, puerta));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePuerta(@PathVariable Integer id) {
        try {
            puertaService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/aeropuerto/{idAeropuerto}")
    public ResponseEntity<List<PuertaEmbarque>> getPuertasByAeropuerto(@PathVariable Integer idAeropuerto) {
        List<PuertaEmbarque> puertas = puertaService.findByAeropuerto(idAeropuerto);
        if (puertas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(puertas);
    }

}
