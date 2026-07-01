package com.SkyWay.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.SkyWay.modules.piloto.domain.service.PiloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/pilotos")
public class PilotoController {
	
	@Autowired
	private PiloService piloService;


	private static final Logger logger = LoggerFactory.getLogger(PilotoController.class);


	//@GetMapping(value =  "/",produces = {"application/json"})
	/*@GetMapping
	public @ResponseBody List<Piloto> getAll() {

		com.SkyWay.util.Logger.logInfo("Obteniendo sotos");
		//System.out.println(piloService.findAll());
		return new ResponseEntity<>(HttpStatus.OK);
		//return piloService.findAll();
	}*/

	/*@GetMapping("/piloto/{pilotoId}")
	public ResponseEntity<Object> getVuelosByPiloto(@PathVariable String pilotoId) {
		Optional<Piloto> piloto = piloService.findByCorreo(pilotoId);

		if (piloto.isPresent()) {
			logger.info("Piloto encontrado: {}", pilotoId);  // Log cuando se encuentra el piloto
			return ResponseEntity.ok(piloto.get());  // Retornar el piloto con 200 OK
		} else {
			logger.warn("Piloto no encontrado con el RUT: {}", pilotoId);  // Log cuando no se encuentra el piloto
			// Devolver un objeto de error con un mensaje descriptivo
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(new ErrorResponse("Piloto no encontrado", "No se encontró un piloto con el RUT: " + pilotoId));
		}
	}*/

	static class ErrorResponse {
		private String error;
		private String message;

		public ErrorResponse(String error, String message) {
			this.error = error;
			this.message = message;
		}

		// Getters y setters
		public String getError() {
			return error;
		}

		public void setError(String error) {
			this.error = error;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}
	
}
