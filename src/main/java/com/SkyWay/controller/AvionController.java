package com.SkyWay.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.SkyWay.model.Avion;
import com.SkyWay.modules.avion.domain.service.AvionService;


@RestController
@RequestMapping("/avion")
public class AvionController {

	@Autowired
	private AvionService avionService;
	
	private final Logger LOGGER = LoggerFactory.getLogger(AvionController.class);
	

	/*@GetMapping(value =  "/",produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody List<Avion> getAll() {

		System.out.println( avionService.findAll());
		return avionService.findAll();
	}
	
	@PostMapping( "/avion/{id}")
	public ResponseEntity<Avion> actualizarAvion(@PathVariable Integer id, @RequestBody Avion avion) {
	    Optional<Avion> avionExistente = avionService.findById(id);
	    if (!avionExistente.isPresent()) {
	        return ResponseEntity.notFound().build();
	    }
	    avion.setIdAvion(id);
	    avionService.save(avion);
	    return ResponseEntity.ok(avion);
	}

	
	@DeleteMapping("/eliminar/{id}")
	public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
	   
		if(avionService.deleteById(id)) {
			return ResponseEntity.noContent().build();
		}
		
		return ResponseEntity.notFound().build(); // 404 Not Found
	}
	
	
	@PutMapping("/agregarAvion/")
	public ResponseEntity<Avion> agregarAvion(@RequestBody Avion avion) {
	    LOGGER.info("Avion a agregar {}",avion);
	    avionService.save(avion);
	    LOGGER.info("Avion agregado {}",avion);
	    return ResponseEntity.ok(avion);
	}

	
	
	@GetMapping(value =  "/getAvion/{avionId}",produces = {"application/json"})
	public @ResponseBody Avion getMethodName(@PathVariable Integer avionId) {
		Avion avion= avionService.findById(avionId).get();
		System.out.println(avion);
		return avion;
	}*/
	
	
}
