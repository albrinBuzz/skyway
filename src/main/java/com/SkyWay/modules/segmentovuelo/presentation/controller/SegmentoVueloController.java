package com.SkyWay.modules.segmentovuelo.presentation.controller;

import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.service.SegmentoVueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/segmentos")
public class SegmentoVueloController {

    @Autowired
    private SegmentoVueloService segmentoVueloService;

    // Crear un nuevo segmento de vuelo
    @PostMapping
    public ResponseEntity<SegmentoVuelo> crearSegmentoVuelo(@RequestBody SegmentoVuelo segmentoVuelo) {
        SegmentoVuelo segmentoGuardado = segmentoVueloService.save(segmentoVuelo);
        return new ResponseEntity<>(segmentoGuardado, HttpStatus.CREATED);
    }

    // Obtener todos los segmentos de vuelo
    @GetMapping
    public List<SegmentoVuelo> obtenerTodosLosSegmentos() {
        return segmentoVueloService.findAll();
    }

    // Obtener un segmento de vuelo por ID
    @GetMapping("/{idSegmento}")
    public ResponseEntity<SegmentoVuelo> obtenerSegmentoPorId(@PathVariable Integer idSegmento) {
        Optional<SegmentoVuelo> segmentoVuelo = segmentoVueloService.findById(idSegmento);
        return segmentoVuelo.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Actualizar un segmento de vuelo
    @PutMapping("/{idSegmento}")
    public ResponseEntity<SegmentoVuelo> actualizarSegmentoVuelo(
            @PathVariable Integer idSegmento,
            @RequestBody SegmentoVuelo segmentoVuelo) {
        if (!idSegmento.equals(segmentoVuelo.getIdSegmento())) {
            return ResponseEntity.badRequest().build();
        }
        try {
            SegmentoVuelo segmentoActualizado = segmentoVueloService.update(segmentoVuelo);
            return ResponseEntity.ok(segmentoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Eliminar un segmento de vuelo
    @DeleteMapping("/{idSegmento}")
    public ResponseEntity<Void> eliminarSegmentoVuelo(@PathVariable Integer idSegmento) {
        try {
            segmentoVueloService.deleteById(idSegmento);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
