package com.SkyWay.modules.capacidadclase.presentation.controller;

import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.capacidadclase.domain.service.CapacidadClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/capacidadclase")
public class CapacidadClaseController {

    @Autowired
    private CapacidadClaseService service;

    @GetMapping
    public List<CapacidadClase> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapacidadClase> getById(@PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public CapacidadClase create(@RequestBody CapacidadClase capacidadClase) {
        return service.save(capacidadClase);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CapacidadClase> update(@PathVariable Integer id, @RequestBody CapacidadClase capacidadClase) {
        return service.findById(id)
                .map(existing -> {
                    capacidadClase.setIdCapacidadClase(id);
                    CapacidadClase updated = service.save(capacidadClase);
                    return ResponseEntity.ok(updated);
                }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        if (service.findById(id).isPresent()) {
            service.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/by-avion/{idAvion}")
    public List<CapacidadClase> getByAvionId(@PathVariable Integer idAvion) {
        return service.findByAvionId(idAvion);
    }


}
