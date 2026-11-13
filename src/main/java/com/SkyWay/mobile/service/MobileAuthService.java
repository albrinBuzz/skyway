package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.*;
import com.SkyWay.mobile.security.MobileSecurityConfig;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;
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
    private final UsuarioService usuarioService;
    public MobileAuthService(UsuarioRepository usuarios, PasajeroRepository pasajeros,
            PasswordEncoder passwords, JwtEncoder tokens, Clock clock,
            UsuarioService usuarioService,
            @Value("${skyway.mobile.token-seconds:7200}") long ttl) {
        this.usuarios = usuarios; this.pasajeros = pasajeros; this.passwords = passwords;
        this.tokens = tokens; this.clock = clock; this.usuarioService = usuarioService;
        if (ttl < 60 || ttl > 86400) throw new IllegalArgumentException("token-seconds debe estar entre 60 y 86400.");
        this.ttl = ttl;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }
    public LoginResponse login(LoginRequest request) {
        var usuario = usuarios.findByCorreoElectronicoIgnoreCase(request.correoElectronico().trim()).orElse(null);
        String hash = usuario != null && usuario.getContrasena() != null ? usuario.getContrasena() : dummyHash;
        boolean valid = passwords.matches(request.contrasena(), hash);
        if (!valid || usuario == null || !pasajeros.existsById(usuario.getRut()))
            throw new MobileException(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos.");
        long sessionTtl = Boolean.TRUE.equals(request.recordarSesion()) ? 86400L : ttl;
        var now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(MobileSecurityConfig.ISSUER)
                .audience(List.of(MobileSecurityConfig.AUDIENCE)).subject(usuario.getRut())
                .issuedAt(now).expiresAt(now.plusSeconds(sessionTtl)).id(UUID.randomUUID().toString())
                .claim("scope", "mobile").build();
        var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return new LoginResponse(tokens.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(),
                "Bearer", sessionTtl, perfil(usuario.getRut()));
    }
    @Transactional
    public LoginResponse registrar(RegistroRequest request) {
        String rut = request.rut() != null ? request.rut().trim() : "";
        String cleanRut = rut.replace(".", "").toUpperCase();
        if (cleanRut.length() > 12) {
            cleanRut = cleanRut.substring(0, 12);
        }
        String correo = request.correoElectronico() != null ? request.correoElectronico().trim().toLowerCase() : "";
        if (cleanRut.isBlank()) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "RUT_REQUERIDO", "El RUT o documento de identidad es obligatorio.");
        }
        if (correo.isBlank()) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "CORREO_REQUERIDO", "El correo electrónico es obligatorio.");
        }
        if (usuarios.existsById(cleanRut) || usuarios.existsById(rut) || pasajeros.existsById(cleanRut) || pasajeros.existsById(rut)) {
            throw new MobileException(HttpStatus.CONFLICT, "RUT_REGISTRADO", "El RUT ya se encuentra registrado en el sistema.");
        }
        if (usuarios.findByCorreoElectronicoIgnoreCase(correo).isPresent()) {
            throw new MobileException(HttpStatus.CONFLICT, "CORREO_REGISTRADO", "El correo electrónico ya se encuentra registrado en el sistema.");
        }

        LocalDate hoy = LocalDate.now(clock);
        if (request.fechaNacimiento() == null) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "FECHA_NACIMIENTO_REQUERIDA", "La fecha de nacimiento es obligatoria.");
        }
        if (request.fechaNacimiento().isAfter(hoy) || Period.between(request.fechaNacimiento(), hoy).getYears() < 18) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "MENOR_DE_EDAD", "Debes tener al menos 18 años para registrarte en SkyWay.");
        }

        String doc = request.documentoIdentidad() != null && !request.documentoIdentidad().isBlank()
                ? request.documentoIdentidad().trim()
                : cleanRut;
        String tipoDoc = request.tipoDocumento() != null && !request.tipoDocumento().isBlank()
                ? request.tipoDocumento().trim()
                : "RUT";
        String nac = request.nacionalidad() != null && !request.nacionalidad().isBlank()
                ? request.nacionalidad().trim()
                : "Chilena";

        Usuario nuevo = new Usuario();
        nuevo.setRut(cleanRut);
        nuevo.setNombre(request.nombre().trim());
        nuevo.setApellido(request.apellido().trim());
        nuevo.setCorreoElectronico(correo);
        nuevo.setTelefono(request.telefono().trim());
        nuevo.setDocumentoIdentidad(doc);
        nuevo.setFechaNacimiento(java.sql.Date.valueOf(request.fechaNacimiento()));
        nuevo.setContrasena(request.contrasena());

        usuarioService.crearPasajeroConDetalle(nuevo, tipoDoc, nac);

        var now = clock.instant();
        var claims = JwtClaimsSet.builder().issuer(MobileSecurityConfig.ISSUER)
                .audience(List.of(MobileSecurityConfig.AUDIENCE)).subject(cleanRut)
                .issuedAt(now).expiresAt(now.plusSeconds(ttl)).id(UUID.randomUUID().toString())
                .claim("scope", "mobile").build();
        var header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        return new LoginResponse(tokens.encode(JwtEncoderParameters.from(header, claims)).getTokenValue(),
                "Bearer", ttl, perfil(cleanRut));
    }
    public void requirePassenger(String rut) {
        if (!pasajeros.existsById(rut))
            throw new MobileException(HttpStatus.FORBIDDEN, "SOLO_PASAJEROS", "La cuenta no corresponde a un pasajero.");
    }
    @Transactional
    public Perfil actualizarCorreo(String rut, ActualizarCorreoRequest request) {
        requirePassenger(rut);
        var usuario = usuarios.findById(rut).orElseThrow(MobileException::notFound);
        if (usuario.getContrasena() == null || !passwords.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw new MobileException(HttpStatus.UNAUTHORIZED, "CONTRASENA_INVALIDA", "La contraseña actual no es correcta.");
        }
        String correo = request.correoElectronico().trim();
        if (usuarios.existsByCorreoElectronicoIgnoreCaseAndRutNot(correo, rut)) {
            throw new MobileException(HttpStatus.CONFLICT, "CORREO_EN_USO", "El correo ya está asociado a otra cuenta.");
        }
        usuario.setCorreoElectronico(correo);
        usuarios.save(usuario);
        return perfil(rut);
    }

    @Transactional
    public void cambiarContrasena(String rut, CambiarContrasenaRequest request) {
        requirePassenger(rut);
        var usuario = usuarios.findById(rut).orElseThrow(MobileException::notFound);
        if (usuario.getContrasena() == null || !passwords.matches(request.contrasenaActual(), usuario.getContrasena())) {
            throw new MobileException(HttpStatus.UNAUTHORIZED, "CONTRASENA_INVALIDA", "La contraseña actual no es correcta.");
        }
        if (passwords.matches(request.nuevaContrasena(), usuario.getContrasena())) {
            throw new MobileException(HttpStatus.BAD_REQUEST, "MISMA_CONTRASENA", "La nueva contraseña debe ser distinta a la actual.");
        }
        usuario.setContrasena(passwords.encode(request.nuevaContrasena()));
        usuarios.save(usuario);
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
