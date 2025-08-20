package com.SkyWay.modules.itinerario.presentation.controller;



import com.SkyWay.modules.itinerario.domain.model.Itinerario;

import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/itinerarios")
public class ItinerarioController {

    @Autowired
    private ItinerarioService itinerarioService;

    @PostMapping
    public Itinerario crearItinerario(@RequestBody Itinerario itinerario) {
        return itinerarioService.crearItinerario(itinerario);
    }

    @GetMapping
    public List<Itinerario> obtenerTodosItinerarios() {
        return itinerarioService.obtenerTodosItinerarios();
    }

    @GetMapping("/{id}")
    public Itinerario obtenerItinerarioPorId(@PathVariable Integer id) {
        return itinerarioService.obtenerItinerarioPorId(id);
    }

    @DeleteMapping("/{id}")
    public void eliminarItinerario(@PathVariable Integer id) {
        itinerarioService.eliminarItinerario(id);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ItinerarioDTO>> buscarItinerarios(
            @RequestParam("ciudad_salida") String ciudadSalida,
            @RequestParam("ciudad_llegada") String ciudadLlegada,
            @RequestParam("fecha_inicio") String fechaInicio) {

        Logger.logInfo("buscado itinerarios");
        try {
            List<ItinerarioDTO> itinerarios = itinerarioService.buscarItinerarios(ciudadSalida, ciudadLlegada, fechaInicio);
            return ResponseEntity.ok(itinerarios);
        } catch (Exception e) {
            // Manejar la excepción adecuadamente (por ejemplo, formato incorrecto de la fecha)
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/detalle/{id_itinerario}")
    public ResponseEntity<List<ItinerarioDetalleDTO>> obtenerDetalleItinerario(@PathVariable Integer id_itinerario) {
        List<ItinerarioDetalleDTO> detalles = itinerarioService.obtenerDetalleItinerario(id_itinerario);
        return ResponseEntity.ok(detalles);
    }

    //GET /api/v1/itinerarios/detalle?ids=1001,1002
    @GetMapping("/api/v1/itinerarios/detalle")
    public ResponseEntity<List<ItinerarioDetalleDTO>> getDetalles(
            @RequestParam List<Integer> ids) {

        List<ItinerarioDetalleDTO> detalles=new ArrayList<>();
        //List<ItinerarioDetalleDTO> detalles = itinerarioService.getDetallesByIds(ids);
        return ResponseEntity.ok(detalles);
    }



}
