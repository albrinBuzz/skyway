package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.mobile.security.MobileSecurityConfig;
import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class MobileAuthService {
    private final UsuarioRepository usuarios;
    private final PasajeroRepository pasajeros;
    private final PasswordEncoder passwords;
    private final JwtEncoder tokens;
    private final Clock clock;
    private final long ttl;
    private final String dummyHash;
    public MobileAuthService(UsuarioRepository usuarios, PasajeroRepository pasajeros,
            PasswordEncoder passwords, JwtEncoder tokens, Clock clock,
            @Value("${skyway.mobile.token-seconds:900}") long ttl) {
        this.usuarios = usuarios; this.pasajeros = pasajeros; this.passwords = passwords;
        this.tokens = tokens; this.clock = clock;
        if (ttl < 60 || ttl > 3600) throw new IllegalArgumentException("token-seconds debe estar entre 60 y 3600.");
        this.ttl = ttl;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }
    public LoginResponse login(LoginRequest request) {
        var usuario = usuarios.findByCorreoElectronicoIgnoreCase(request.correoElectronico().trim()).orElse(null);
        String hash = usuario != null && usuario.getContrasena() != null ? usuario.getContrasena() : dummyHash;
        boolean valid = passwords.matches(request.contrasena(), hash);
        if (!valid || usuario == null || !pasajeros.existsById(usuario.getRut()))
            throw new MobileException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos.");
        var now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(MobileSecurityConfig.ISSUER)
                .audience(List.of(MobileSecurityConfig.AUDIENCE)).subject(usuario.getRut())
                .issuedAt(now).expiresAt(now.plusSeconds(ttl)).id(UUID.randomUUID().toString())
                .claim("scope", "mobile").build();
        var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return new LoginResponse(tokens.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(),
                "Bearer", ttl, perfil(usuario.getRut()));
    }
    public void requirePassenger(String rut) {
        if (!pasajeros.existsById(rut))
            throw new MobileException(HttpStatus.FORBIDDEN, "SOLO_PASAJEROS", "La cuenta no corresponde a un pasajero.");
    }
    public Perfil perfil(String rut) {
        var pasajero = pasajeros.findById(rut).orElseThrow(() ->
                new MobileException(HttpStatus.FORBIDDEN, "SOLO_PASAJEROS", "La cuenta no corresponde a un pasajero."));
        var usuario = usuarios.findById(rut).orElseThrow(MobileException::notFound);
        // java.sql.Date no admite toInstant(); se preserva la fecha civil de PostgreSQL.
        LocalDate birth = pasajero.getFechaNacimiento() == null ? null
                : new java.sql.Date(pasajero.getFechaNacimiento().getTime()).toLocalDate();
        return new Perfil(new UsuarioDto(rut, usuario.getNombre(), usuario.getApellido(), usuario.getCorreoElectronico()),
                new PasajeroDto(rut, pasajero.getTipoDocumento(), pasajero.getNumeroDocumento(), birth, pasajero.getNacionalidad()));
    }
}
