package com.SkyWay.controller.api;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.SkyWay.modules.avion.domain.service.AvionService;

@RestController
@RequestMapping("/api/aviones")
public class AvionControllerApi {

    @Autowired
    private AvionService avionService;

    /*@GetMapping
    public List<Avion> getAllAviones() {
        return avionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Avion> getAvionById(@PathVariable Integer id) {
        Optional<Avion> avion = avionService.findById(id);
        return avion.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Avion> createAvion(@RequestBody Avion avion) {
    	System.out.println("Avion a crear"+ avion);
        Avion savedAvion = avionService.save(avion);
        return ResponseEntity.status(201).body(savedAvion);
    }

    @PutMapping
    public ResponseEntity<Avion>update(@RequestBody Avion avion){
        Avion savedAvion = avionService.updateAvion(avion);
        return ResponseEntity.status(201).body(savedAvion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvion(@PathVariable Integer id) {
        avionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }*/
    
    
}