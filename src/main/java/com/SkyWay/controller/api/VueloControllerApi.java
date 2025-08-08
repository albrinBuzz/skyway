package com.SkyWay.controller.api;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.SkyWay.util.Logger;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.SkyWay.dto.VueloDTO;
import com.SkyWay.model.Aeropuerto;
import com.SkyWay.model.Avion;
import com.SkyWay.model.EstadoVuelo;
import com.SkyWay.model.Piloto;
import com.SkyWay.model.PrecioAsiento;
import com.SkyWay.model.Vuelo;
import com.SkyWay.service.AeropuertoService;
import com.SkyWay.service.AvionService;
import com.SkyWay.service.ClaseAsientoService;
import com.SkyWay.service.EstadoVueloService;
import com.SkyWay.service.PiloService;
import com.SkyWay.service.PrecioAsientoService;
import com.SkyWay.service.VueloService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vuelos")
public class VueloControllerApi {

    @Autowired
    private VueloService vueloService;

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private AvionService avionService;

    @Autowired
    private EstadoVueloService estadoVueloService;

    @Autowired
    private PiloService pilotoService;
    
    private HashMap<String,Piloto >pilotos;
    
    private List<Piloto> listaPilotos;
    
	@Autowired
	private ClaseAsientoService claseAsientoService;
	
	@Autowired
	private PrecioAsientoService precioAsientoService;

    // Crear un nuevo vuelo utilizando VueloDTO
    @PostMapping("/crear")
    public ResponseEntity<?> createVuelo(@Valid @RequestBody VueloDTO vueloDTO) {
        // Validaciones de fechas
        System.out.println(vueloDTO);
        
        listaPilotos = pilotoService.findAll();
        pilotos=new HashMap<String, Piloto>();
        listaPilotos.forEach(t -> pilotos.put(t.getRutUsuario(), t));
    	
    	LocalDateTime fechaActual = LocalDateTime.now();
        
        
        if (vueloDTO.getFechaHoraSalida().isBefore(fechaActual)) {
            return new ResponseEntity<>("La fecha de salida no puede ser anterior a la fecha actual.", HttpStatus.BAD_REQUEST);
        }

        if (vueloDTO.getFechaHoraLlegada().isBefore(fechaActual)) {
            return new ResponseEntity<>("La fecha de llegada no puede ser anterior a la fecha actual.", HttpStatus.BAD_REQUEST);
        }

        if (vueloDTO.getFechaHoraLlegada().isBefore(vueloDTO.getFechaHoraSalida())) {
            return new ResponseEntity<>("La fecha de llegada no puede ser anterior a la fecha de salida.", HttpStatus.BAD_REQUEST);
        }

        // Asignar los valores de las relaciones usando IDs
        Aeropuerto aeropuertoSalida = aeropuertoService.findById(vueloDTO.getAeropuerto1Id()).orElseThrow();
        Aeropuerto aeropuertoLlegada = aeropuertoService.findById(vueloDTO.getAeropuerto2Id()).orElseThrow();
        EstadoVuelo estadoVuelo = estadoVueloService.findById(vueloDTO.getEstadoVueloId()).orElseThrow();
        //Piloto piloto = pilotoService.findByRut(vueloDTO.getPilotoRut()).orElseThrow();
        Avion avion = avionService.findById(vueloDTO.getAvionId()).orElseThrow();

        // Convertir VueloDTO a Vuelo (entidad)
        Vuelo vuelo = new Vuelo();
        vuelo.setPiloto(pilotos.get(vueloDTO.getPilotoRut()));
        vuelo.setFechaHoraSalida(vueloDTO.getFechaHoraSalida());
        vuelo.setFechaHoraLlegada(vueloDTO.getFechaHoraLlegada());
        vuelo.setNumeroVuelo(vueloDTO.getNumeroVuelo());
        vuelo.setPrecio(vueloDTO.getPrecio());
        vuelo.setAeropuerto1(aeropuertoSalida);
        vuelo.setAeropuerto2(aeropuertoLlegada);
        vuelo.setEstadoVuelo(estadoVuelo);
        vuelo.setAvion(avion);

        System.out.println(  vuelo);
        // Guardar el vuelo
        try {
    	    var vueloGuardado = vueloService.save(vuelo);

    	    // Configurar precios de asiento
    	    PrecioAsiento precioEco = new PrecioAsiento();
    	    PrecioAsiento precioPrim = new PrecioAsiento();
    	    PrecioAsiento precioEje = new PrecioAsiento();

    	    // Asignar clase de asiento y precios
    	    precioEco.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(1).get());
    	    precioEje.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(2).get());
    	    precioPrim.setClaseAsiento(claseAsientoService.obtenerClaseAsientoPorId(3).get());

    	    precioEco.setPrecio(vueloDTO.getPreciosClases().get(1));
    	    precioEje.setPrecio(vueloDTO.getPreciosClases().get(2));
    	    precioPrim.setPrecio(vueloDTO.getPreciosClases().get(3));
    	    
    	    List<PrecioAsiento> precios = Arrays.asList(precioEco, precioEje, precioPrim);
    	    for (PrecioAsiento precio : precios) {
    	        precio.setVuelo(vueloGuardado);  // Asociar el vuelo
    	        precioAsientoService.guardarPrecioAsiento(precio);
    	    }

            return new ResponseEntity<>("Vuelo creado corretamente",HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al guardar el vuelo: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    // Obtener todos los vuelos
    @GetMapping("/get")
    public ResponseEntity<List<VueloDTO>> getAllVuelos() {
        List<Vuelo> vuelos = vueloService.findAll();
        List<VueloDTO> vueloDTOs = vuelos.stream().map(vuelo -> {
            VueloDTO dto = new VueloDTO();
            dto.setIdVuelo(vuelo.getIdVuelo());
            dto.setFechaHoraLlegada(vuelo.getFechaHoraLlegada());
            dto.setFechaHoraSalida(vuelo.getFechaHoraSalida());
            dto.setNumeroVuelo(vuelo.getNumeroVuelo());
            dto.setPrecio(vuelo.getPrecio());
            dto.setAeropuerto1Id(vuelo.getAeropuerto1().getIdAeropuerto());
            dto.setAeropuerto2Id(vuelo.getAeropuerto2().getIdAeropuerto());
            dto.setAvionId(vuelo.getAvion().getIdAvion());
            dto.setEstadoVueloId(vuelo.getEstadoVuelo().getIdEstadoVuelo());
            dto.setPilotoRut(vuelo.getPiloto().getRutUsuario());
            
            return dto;
        }).toList();
        return new ResponseEntity<>(vueloDTOs, HttpStatus.OK);
    }

    // Obtener un vuelo por ID
    @GetMapping("/{id}")
    public ResponseEntity<VueloDTO> getVueloById(@PathVariable Integer id) {
        Logger.logInfo("Obteniendo sotos");
        Optional<Vuelo> vuelo = vueloService.findById(id);
        return vuelo.map(v -> {
            VueloDTO dto = new VueloDTO();
            dto.setIdVuelo(v.getIdVuelo());
            dto.setFechaHoraLlegada(v.getFechaHoraLlegada());
            dto.setFechaHoraSalida(v.getFechaHoraSalida());
            dto.setNumeroVuelo(v.getNumeroVuelo());
            dto.setPrecio(v.getPrecio());
            dto.setAeropuerto1Id(v.getAeropuerto1().getIdAeropuerto());
            dto.setAeropuerto2Id(v.getAeropuerto2().getIdAeropuerto());
            dto.setAvionId(v.getAvion().getIdAvion());
            dto.setEstadoVueloId(v.getEstadoVuelo().getIdEstadoVuelo());
            dto.setPilotoRut(v.getPiloto().getRutUsuario());
            return new ResponseEntity<>(dto, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Actualizar un vuelo
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVuelo(@PathVariable Integer id, @Valid @RequestBody VueloDTO vueloDTO) {
        Optional<Vuelo> existingVuelo = vueloService.findById(id);
        if (existingVuelo.isPresent()) {
            Vuelo updatedVuelo = existingVuelo.get();
            updatedVuelo.setFechaHoraSalida(vueloDTO.getFechaHoraSalida());
            updatedVuelo.setFechaHoraLlegada(vueloDTO.getFechaHoraLlegada());
            updatedVuelo.setNumeroVuelo(vueloDTO.getNumeroVuelo());
            updatedVuelo.setPrecio(vueloDTO.getPrecio());

            // Asignar relaciones con los IDs
            Aeropuerto aeropuertoSalida = aeropuertoService.findById(vueloDTO.getAeropuerto1Id()).orElseThrow();
            Aeropuerto aeropuertoLlegada = aeropuertoService.findById(vueloDTO.getAeropuerto2Id()).orElseThrow();
            EstadoVuelo estadoVuelo = estadoVueloService.findById(vueloDTO.getEstadoVueloId()).orElseThrow();
            Piloto piloto = pilotoService.findByRut(vueloDTO.getPilotoRut()).orElseThrow();
            Avion avion = avionService.findById(vueloDTO.getAvionId()).orElseThrow();

            updatedVuelo.setAeropuerto1(aeropuertoSalida);
            updatedVuelo.setAeropuerto2(aeropuertoLlegada);
            updatedVuelo.setEstadoVuelo(estadoVuelo);
            updatedVuelo.setPiloto(piloto);
            updatedVuelo.setAvion(avion);

            Vuelo savedVuelo = vueloService.save(updatedVuelo);
            return new ResponseEntity<>(savedVuelo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Vuelo no encontrado", HttpStatus.NOT_FOUND);
        }
    }

    // Eliminar un vuelo
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVuelo(@PathVariable Integer id) {
        boolean isDeleted = vueloService.findById(id).isPresent();
        if(isDeleted) {
            vueloService.deleteById(id);
        }
        return isDeleted ? new ResponseEntity<>(HttpStatus.NO_CONTENT)
                         : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @GetMapping("aviones")
    public ResponseEntity<List<Avion>>getAvions(){
    	  return new ResponseEntity<>(avionService.findAll(), HttpStatus.OK);
  
    };
    @GetMapping("aeropuertos")
    public ResponseEntity<List<Aeropuerto>>getAeropuertos(){
    	  return new ResponseEntity<>(aeropuertoService.findAll(), HttpStatus.OK);
  
    };
    @GetMapping("pilotos")
    public ResponseEntity<List<Piloto>>getPilotos(){
    	  return new ResponseEntity<>(pilotoService.findAll(), HttpStatus.OK);
  
    };
    
    @GetMapping("estadoVuelo")
    public ResponseEntity<List<EstadoVuelo>>getEstados(){
    	  return new ResponseEntity<>(estadoVueloService.findAll(), HttpStatus.OK);
  
    };
    
	@GetMapping(value =  "/getAvion/{avion_id}",produces = {"application/json"})
	public @ResponseBody Avion getMethodName(@PathVariable Integer avion_id) {
		System.out.println(avion_id);
		Avion avion= avionService.findById(avion_id).get();
		System.out.println(avion);
		return avion;
	}
	
}
