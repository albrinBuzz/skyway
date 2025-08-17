package com.SkyWay.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SkyWay.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;

@RestController
@RequestMapping("/api/aeropuertos")
public class AeropuertoController {

    private final AeropuertoService aeropuertoService;

    @Autowired
    public AeropuertoController(AeropuertoService aeropuertoService) {
        this.aeropuertoService = aeropuertoService;
    }

    // Obtener todos los aeropuertos
    /*@GetMapping
    public List<Aeropuerto> getAllAeropuertos() {
        return aeropuertoService.findAll();
    }

    // Obtener un aeropuerto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Aeropuerto> getAeropuertoById(@PathVariable Integer id) {
        Optional<Aeropuerto> aeropuerto = aeropuertoService.findById(id);
        if (aeropuerto.isPresent()) {
            return new ResponseEntity<>(aeropuerto.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Crear un nuevo aeropuerto
    @PostMapping
    public ResponseEntity<Aeropuerto> createAeropuerto(@RequestBody Aeropuerto aeropuerto) {
        Aeropuerto savedAeropuerto = aeropuertoService.save(aeropuerto);
        return new ResponseEntity<>(savedAeropuerto, HttpStatus.CREATED);
    }

    // Actualizar un aeropuerto
    @PutMapping("/{id}")
    public ResponseEntity<Aeropuerto> updateAeropuerto(@PathVariable Integer id, @RequestBody Aeropuerto aeropuerto) {
        Optional<Aeropuerto> existingAeropuerto = aeropuertoService.findById(id);
        if (existingAeropuerto.isPresent()) {
            aeropuerto.setIdAeropuerto(id);
            Aeropuerto updatedAeropuerto = aeropuertoService.update(aeropuerto);
            return new ResponseEntity<>(updatedAeropuerto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar un aeropuerto
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAeropuerto(@PathVariable Integer id) {
        Optional<Aeropuerto> existingAeropuerto = aeropuertoService.findById(id);
        if (existingAeropuerto.isPresent()) {
            aeropuertoService.delete(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }*/
}
