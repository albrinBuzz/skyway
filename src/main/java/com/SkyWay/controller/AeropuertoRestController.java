package com.SkyWay.controller;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/aeropuertos")
@CrossOrigin(origins = "*") // Permite el consumo desde Angular (CORS)
public class AeropuertoRestController {

    @Autowired
    private AeropuertoService aeropuertoService;

    /**
     * Devuelve la lista completa de aeropuertos para ser consumidos por el Frontend (Angular)
     * GET /api/v1/aeropuertos
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listarAeropuertos() {
        List<Aeropuerto> aeropuertos = aeropuertoService.findAll();

        List<Map<String, Object>> respuesta = aeropuertos.stream()
                .filter(a -> a.getCodigoIata() != null && !a.getCodigoIata().isBlank())
                .map(a -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("codigoIata", a.getCodigoIata());
                    item.put("nombre", a.getNombreAeropuerto() != null ? a.getNombreAeropuerto() : "");
                    item.put("ciudad", (a.getCiudad() != null && a.getCiudad().getNombre() != null)
                            ? a.getCiudad().getNombre() : "Desconocida");

                    // Label amigable formateado para un select/autocomplete de Angular
                    String label = item.get("ciudad") + " - " + item.get("nombre") + " (" + a.getCodigoIata() + ")";
                    item.put("label", label);

                    return item;
                })
                .toList();

        return ResponseEntity.ok(respuesta);
    }
}