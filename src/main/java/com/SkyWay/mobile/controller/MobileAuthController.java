package com.SkyWay.mobile.controller;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.mobile.service.MobileAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mobile/auth")
public class MobileAuthController {
    private final MobileAuthService auth;
    public MobileAuthController(MobileAuthService auth) { this.auth=auth; }

    @PostMapping("/login") public LoginResponse login(@Valid @RequestBody LoginRequest body) { return auth.login(body); }

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
