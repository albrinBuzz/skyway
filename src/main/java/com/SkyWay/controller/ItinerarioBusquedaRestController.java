package com.SkyWay.controller;

import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import com.SkyWay.modules.itinerario.presentation.dto.PuntoMapaDTO;
import com.SkyWay.modules.tarifa.presentation.dto.TarifaDTO;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1/itinerarios")
public class ItinerarioBusquedaRestController {

    @Autowired
    private ItinerarioService itinerarioService;

    @Autowired
    private ItinerarioTarifaService itinerarioTarifaService;

    /**
     * Endpoint que ejecuta la búsqueda de itinerarios imitando el ciclo init() del Bean.
     * GET /api/v1/itinerarios/buscar?salida=SCL&llegada=BOG&fechaIda=2026-09-01&fechaRegreso=2026-09-10&trip=RT&adultos=2
     */
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarItinerarios(
            @RequestParam String salida,
            @RequestParam String llegada,
            @RequestParam String fechaIda,
            @RequestParam(required = false) String fechaRegreso,
            @RequestParam(defaultValue = "OW") String trip,
            @RequestParam(defaultValue = "1") Integer adultos) throws ParseException {

        Map<String, Object> response = new HashMap<>();
        String tipoViaje = trip.toUpperCase();

        // Validaciones equivalentes al Bean
        if (salida.isBlank() || llegada.isBlank()) {
            response.put("error", "Debes completar origen, destino y fecha.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (salida.equalsIgnoreCase(llegada)) {
            response.put("error", "El destino no puede ser igual al origen.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        LocalDate fIda = LocalDate.parse(fechaIda);
        if (fIda.isBefore(LocalDate.now())) {
            response.put("error", "La fecha de ida no puede estar en el pasado.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        LocalDate fRegreso = null;
        if ("RT".equals(tipoViaje) && fechaRegreso != null && !fechaRegreso.isBlank()) {
            fRegreso = LocalDate.parse(fechaRegreso);
            if (fRegreso.isBefore(fIda)) {
                response.put("error", "La fecha de regreso no puede ser antes que la de ida.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }

        // Búsqueda Vuelos Ida
        List<ItinerarioDTO> vuelosIda = itinerarioService.buscarItinerarios(salida, llegada, fIda.toString());
        if (vuelosIda != null) {
            vuelosIda.sort(Comparator.comparing(ItinerarioDTO::getHoraLlegada24h));
        } else {
            vuelosIda = Collections.emptyList();
        }

        // Búsqueda Vuelos Regreso (en caso de RT)
        List<ItinerarioDTO> vuelosRegreso = new ArrayList<>();
        if ("RT".equals(tipoViaje) && fRegreso != null) {
            vuelosRegreso = itinerarioService.buscarItinerarios(llegada, salida, fRegreso.toString());
            if (vuelosRegreso != null) {
                vuelosRegreso.sort(Comparator.comparing(ItinerarioDTO::getHoraLlegada24h));
            }
        }

        response.put("salida", salida);
        response.put("llegada", llegada);
        response.put("fechaIda", fIda.toString());
        response.put("fechaRegreso", fRegreso != null ? fRegreso.toString() : null);
        response.put("tipoViaje", tipoViaje);
        response.put("adultos", adultos);
        response.put("vuelosIda", vuelosIda);
        response.put("vuelosRegreso", vuelosRegreso);

        return ResponseEntity.ok(response);
    }

    /**
     * Devuelve los detalles de las escalas/paradas de un itinerario específico.
     * GET /api/v1/itinerarios/123/paradas
     */
    @GetMapping("/{idItinerario}/paradas")
    public ResponseEntity<List<ItinerarioDetalleDTO>> getParadas(@PathVariable Integer idItinerario) {
        List<ItinerarioDetalleDTO> paradas = itinerarioService.obtenerDetalleItinerario(idItinerario);
        return ResponseEntity.ok(paradas != null ? paradas : Collections.emptyList());
    }

    /**
     * Devuelve las tarifas asociadas a un itinerario.
     * GET /api/v1/itinerarios/123/tarifas
     */
    @GetMapping("/{idItinerario}/tarifas")
    public ResponseEntity<List<TarifaDTO>> getTarifas(@PathVariable Integer idItinerario) {
        List<TarifaDTO> tarifas = itinerarioTarifaService.getTarifasItinerario(idItinerario);
        return ResponseEntity.ok(tarifas != null ? tarifas : Collections.emptyList());
    }

    /**
     * Devuelve la información geográfica para renderizar el mapa de la ruta.
     * GET /api/v1/itinerarios/123/mapa
     */
    @GetMapping("/{idItinerario}/mapa")
    public ResponseEntity<List<PuntoMapaDTO>> getMapaRuta(@PathVariable Integer idItinerario) {
        List<PuntoMapaDTO> ruta = itinerarioService.obtenerRutaMapa(idItinerario);
        return ResponseEntity.ok(ruta != null ? ruta : Collections.emptyList());
    }
}