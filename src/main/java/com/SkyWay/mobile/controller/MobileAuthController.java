package com.SkyWay.mobile.controller;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.mobile.service.MobileAuthService;
import com.SkyWay.mobile.service.MobileReadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mobile/auth")
public class MobileAuthController {
    private final MobileAuthService auth;
    private final MobileReadService readService;
    public MobileAuthController(MobileAuthService auth, MobileReadService readService) {
        this.auth = auth;
        this.readService = readService;
    }

    @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest body) { return auth.login(body); }

    @PostMapping("/consulta-pnr")
    public ResponseEntity<ConsultaPnrResponse> consultarPnr(@Valid @RequestBody PnrLookupRequest body) {
        return ResponseEntity.ok(readService.consultarPorPnr(body.codigoPnr(), body.apellido()));
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponse> registro(@Valid @RequestBody RegistroRequest body) {
        return ResponseEntity.ok(auth.registrar(body));
    }

    @GetMapping("/me") public Perfil me(@AuthenticationPrincipal Jwt jwt) { return auth.perfil(jwt.getSubject()); }

    @PatchMapping("/perfil/correo")
    public Perfil actualizarCorreo(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ActualizarCorreoRequest body) {
        return auth.actualizarCorreo(jwt.getSubject(), body);
    }

    @PatchMapping("/perfil/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CambiarContrasenaRequest body) {
        auth.cambiarContrasena(jwt.getSubject(), body);
        return ResponseEntity.noContent().build();
    }
}
