package com.SkyWay.controller;

import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import java.util.List;
import java.util.Optional;
/*curl -X GET http://localhost:8080/api2/vuelos
curl -X GET http://localhost:8080/api2/vuelos/123
curl -X GET http://localhost:8080/api2/vuelos/piloto/12345678-9
curl -X GET http://localhost:8080/api2/vuelos/reservas/12345678-9

*/



/*
curl -X GET http://localhost:8080/api2/vuelos
curl -X GET http://localhost:8080/api2/vuelos/123
curl -X GET http://localhost:8080/api2/vuelos/piloto/12345678-9
curl -X GET http://localhost:8080/api2/vuelos/reservas/12345678-9


 */
@RestController
@RequestMapping("/api2/vuelos")
public class VueloController {

    private final VueloService vueloService;
    private final AeropuertoService aeropuertoService;
    private final AvionService avionService;
    private final PiloService piloService;
    private final EstadoVueloService estadoVueloService;
    private final ReservaService reservaService;

    @Autowired
    public VueloController(VueloService vueloService, AeropuertoService aeropuertoService,
                           AvionService avionService, PiloService piloService,
                           EstadoVueloService estadoVueloService, ReservaService reservaService) {
        this.vueloService = vueloService;
        this.aeropuertoService = aeropuertoService;
        this.avionService = avionService;
        this.piloService = piloService;
        this.estadoVueloService = estadoVueloService;
        this.reservaService = reservaService;
    }

    // Obtener todos los vuelos
    @GetMapping
    public List<Vuelo> getAllVuelos() {
        Logger.logInfo("Obteniendo sotos");
        return vueloService.findAll();
    }

    // Obtener un vuelo por ID
    @GetMapping("/{id}")
    public ResponseEntity<Vuelo> getVueloById(@PathVariable Integer id) {
        Optional<Vuelo> vuelo = vueloService.findById(id);
        if (vuelo.isPresent()) {
            return new ResponseEntity<>(vuelo.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtener vuelos por piloto
    @GetMapping("/piloto/{pilotoId}")
    public ResponseEntity<List<Vuelo>> getVuelosByPiloto(@PathVariable String pilotoId) {
        Optional<Piloto> piloto = piloService.findByRut(pilotoId);

        if (piloto.isPresent()) {
            List<Vuelo> vuelos = vueloService.findByPiloto(piloto.get());
            return new ResponseEntity<>(vuelos, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Obtener reservas de un usuario
    @GetMapping("/reservas/{usuarioRut}")
    public ResponseEntity<List<ReservaVueloDTO>> getReservasUsuario(@PathVariable String usuarioRut) {
        List<ReservaVueloDTO> reservas = reservaService.getReservasUsuario(usuarioRut);
        if (reservas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(reservas, HttpStatus.OK);
        }
    }

    // Crear un nuevo vuelo
    /*@PostMapping
    public ResponseEntity<Vuelo> createVuelo(@RequestBody Vuelo vuelo) {
        // Validación de los datos del vuelo (en caso de ser necesario)
        if (vuelo.getNumeroVuelo() == null || vuelo.getFechaHoraSalida() == null ||
                vuelo.getFechaHoraLlegada() == null || vuelo.getAeropuerto1() == null ||
                vuelo.getAeropuerto1() == null || vuelo.getAvion() == null || vuelo.getPiloto() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Vuelo savedVuelo = vueloService.save(vuelo);
        return new ResponseEntity<>(savedVuelo, HttpStatus.CREATED);
    }

    // Actualizar un vuelo existente
    @PutMapping("/{id}")
    public ResponseEntity<Vuelo> updateVuelo(@PathVariable Integer id, @RequestBody Vuelo vuelo) {
        Optional<Vuelo> existingVuelo = vueloService.findById(id);
        if (existingVuelo.isPresent()) {
            vuelo.setIdVuelo(id);  // Se asegura de que el ID sea el correcto
            Vuelo updatedVuelo = vueloService.save(vuelo);
            return new ResponseEntity<>(updatedVuelo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar un vuelo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVuelo(@PathVariable Integer id) {
        Optional<Vuelo> existingVuelo = vueloService.findById(id);
        if (existingVuelo.isPresent()) {
            vueloService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // Obtener todos los aeropuertos
    @GetMapping("/aeropuertos")
    public List<Aeropuerto> getAllAeropuertos() {
        return aeropuertoService.findAll();
    }

    // Obtener todos los aviones
    @GetMapping("/aviones")
    public List<Avion> getAllAviones() {
        return avionService.findAll();
    }

    // Obtener todos los pilotos
    @GetMapping("/pilotos")
    public List<Piloto> getAllPilotos() {
        return piloService.findAll();
    }*/
}
