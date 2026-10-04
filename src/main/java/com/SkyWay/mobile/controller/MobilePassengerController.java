package com.SkyWay.mobile.controller;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.mobile.service.MobileOperationsService;
import com.SkyWay.mobile.service.MobileReadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mobile")
@CrossOrigin(origins = "*")
public class MobilePassengerController {

    private final MobileReadService readService;
    private final MobileOperationsService operationsService;

    public MobilePassengerController(MobileReadService readService, MobileOperationsService operationsService) {
        this.readService = readService;
        this.operationsService = operationsService;
    }

    // ------------------------------------------------------------------
    // 1. INICIO Y HOME CONTEXTUAL
    // ------------------------------------------------------------------
    @GetMapping("/inicio")
    public ResponseEntity<Inicio> inicio(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(readService.inicio(jwt.getSubject()));
    }

    // ------------------------------------------------------------------
    // 2. GESTIÓN DE RESERVAS E ITINERARIO
    // ------------------------------------------------------------------
    @GetMapping("/reservas")
    public ResponseEntity<Pagina<ResumenReserva>> listarReservas(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "activas") String grupo,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(readService.listar(jwt.getSubject(), grupo, pagina, tamano));
    }

    @GetMapping("/reservas/{idReserva}")
    public ResponseEntity<DetalleReserva> detalleReserva(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva) {
        return ResponseEntity.ok(readService.detalle(jwt.getSubject(), idReserva));
    }

    @GetMapping("/reservas/{idReserva}/vuelos/{idVuelo}")
    public ResponseEntity<VueloDto> obtenerVuelo(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva,
            @PathVariable("idVuelo") int idVuelo) {
        return ResponseEntity.ok(readService.vuelo(jwt.getSubject(), idReserva, idVuelo));
    }

    @GetMapping("/reservas/{idReserva}/equipajes")
    public ResponseEntity<List<EquipajeDto>> listarEquipajes(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva) {
        return ResponseEntity.ok(readService.detalle(jwt.getSubject(), idReserva).equipajes());
    }

    // ------------------------------------------------------------------
    // 3. MAPA DE ASIENTOS Y SELECCIÓN
    // ------------------------------------------------------------------
    @GetMapping("/reservas/{idReserva}/vuelos/{idVuelo}/asientos")
    public ResponseEntity<MapaAsientos> obtenerMapaAsientos(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva,
            @PathVariable("idVuelo") int idVuelo) {
        return ResponseEntity.ok(operationsService.mapa(jwt.getSubject(), idReserva, idVuelo));
    }

    @PutMapping("/reservas/{idReserva}/vuelos/{idVuelo}/asiento")
    public ResponseEntity<MapaAsientos> cambiarAsiento(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva,
            @PathVariable("idVuelo") int idVuelo,
            @Valid @RequestBody CambioAsientoRequest body) {
        return ResponseEntity.ok(operationsService.cambiar(jwt.getSubject(), idReserva, idVuelo, body.idAsiento()));
    }

    // ------------------------------------------------------------------
    // 4. CHECK-IN Y PASE DE ABORDAR CON QR
    // ------------------------------------------------------------------
    @PostMapping("/reservas/{idReserva}/checkin")
    public ResponseEntity<CheckinDto> registrarCheckin(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva) {
        return ResponseEntity.ok(operationsService.checkin(jwt.getSubject(), idReserva));
    }

    @GetMapping("/reservas/{idReserva}/vuelos/{idVuelo}/pase-abordar")
    public ResponseEntity<PaseAbordar> obtenerPaseAbordar(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idReserva") int idReserva,
            @PathVariable("idVuelo") int idVuelo) {
        return ResponseEntity.ok(readService.pase(jwt.getSubject(), idReserva, idVuelo));
    }

    // ------------------------------------------------------------------
    // 5. NOTIFICACIONES
    // ------------------------------------------------------------------
    @GetMapping("/notificaciones")
    public ResponseEntity<Pagina<NotificacionDto>> listarNotificaciones(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(readService.notificaciones(jwt.getSubject(), pagina, tamano));
    }

    @PatchMapping("/notificaciones/{idNotificacion}/leida")
    public ResponseEntity<NotificacionDto> marcarLeida(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("idNotificacion") int idNotificacion) {
        return ResponseEntity.ok(operationsService.leer(jwt.getSubject(), idNotificacion));
    }
}