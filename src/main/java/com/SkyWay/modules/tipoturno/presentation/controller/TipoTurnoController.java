package com.SkyWay.modules.tipoturno.presentation.controller;


import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;

import com.SkyWay.modules.tipoturno.domain.service.TipoTurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-turno")
@CrossOrigin(origins = "*") // Ajustar según tus necesidades de seguridad
public class TipoTurnoController {

    @Autowired
    private TipoTurnoService service;

    @GetMapping
    public ResponseEntity<List<TipoTurno>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    /*@GetMapping("/{id}")
    public ResponseEntity<TipoTurno> getById(@ freshman @PathVariable Integer id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }*/

    @PostMapping
    public ResponseEntity<TipoTurno> create(@RequestBody TipoTurno tipoTurno) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(tipoTurno));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoTurno> update(@PathVariable Integer id, @RequestBody TipoTurno tipoTurno) {
        try {
            return ResponseEntity.ok(service.update(id, tipoTurno));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}