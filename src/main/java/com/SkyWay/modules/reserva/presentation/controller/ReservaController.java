package com.SkyWay.modules.reserva.presentation.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.SkyWay.config.security.AccesoRut;
import com.SkyWay.modules.reserva.domain.service.ReservaService;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private static final Set<String> CAMPOS_ORDEN = Set.of("fechaReserva", "idReserva", "codigoReserva", "total");

    /** DTO de salida: nunca se serializa la entidad (arrastraba Pasajero/Usuario con PII y hash). */
    public record ReservaResumen(Integer idReserva, String codigoReserva, java.sql.Timestamp fechaReserva,
                                 java.math.BigDecimal total) {}

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AccesoRut accesoRut;

    //GET /api/reservas/por-rut/12345678-9?page=0&size=20&sortBy=fechaReserva&direction=desc
    @GetMapping("/por-rut/{rut}")
    public Page<ReservaResumen> getReservasPorRut(
            @PathVariable String rut,
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaReserva") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        if (!accesoRut.puedeAcceder(auth, rut)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        if (!CAMPOS_ORDEN.contains(sortBy)) {
            sortBy = "fechaReserva";
        }
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), sort);
        return reservaService.obtenerReservasPorRut(rut, pageable)
                .map(r -> new ReservaResumen(r.getIdReserva(), r.getCodigoReserva(), r.getFechaReserva(), r.getTotal()));
    }
}
