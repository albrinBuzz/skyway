package com.SkyWay.modules.usuario.application.serviceImpl;

import com.SkyWay.modules.auditoria.application.LogAuditoriaService;
import com.SkyWay.modules.usuario.domain.model.TokenRecuperacion;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.repository.TokenRecuperacionRepository;
import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import com.SkyWay.modules.usuario.domain.service.RecuperacionContrasenaService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de recuperación de contraseñas respaldado por PostgreSQL.
 * Genera tokens criptográficos de 256 bits (Base64URL), persiste únicamente su hash SHA-256
 * en {@code Token_Recuperacion} y garantiza consumo único transaccional con bloqueo pesimista.
 */
@Service
public class RecuperacionContrasenaServiceImpl implements RecuperacionContrasenaService {

    private static final Logger log = LoggerFactory.getLogger(RecuperacionContrasenaServiceImpl.class);

    private static final long TTL_MINUTOS = 15L;
    private static final long COOLDOWN_REENVIO_SEGUNDOS = 60L;
    private static final int BYTES_ENTROPIA_TOKEN = 32; // 256 bits criptográficos (~43 caracteres URL-safe)
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;

    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PATRON_TOKEN = Pattern.compile("^[A-Za-z0-9_-]{32,128}$");

    private static final String MENSAJE_SOLICITUD_NEUTRO =
            "Si el correo ingresado está asociado a una cuenta de SkyWay, enviamos un enlace y token de seguridad válido por 15 minutos.";

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final LogAuditoriaService logAuditoriaService;
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, Instant> cooldownCorreosInexistentes = new ConcurrentHashMap<>();
    private final ExecutorService mailExecutor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "skyway-recovery-mail");
        thread.setDaemon(true);
        return thread;
    });

    @PreDestroy
    public void cerrarEjecutorCorreos() {
        mailExecutor.shutdown();
    }

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public RecuperacionContrasenaServiceImpl(
            UsuarioRepository usuarioRepository,
            TokenRecuperacionRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            ObjectProvider<JavaMailSender> mailSenderProvider,
            LogAuditoriaService logAuditoriaService,
            ObjectProvider<JdbcTemplate> jdbcTemplateProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderProvider = mailSenderProvider;
        this.logAuditoriaService = logAuditoriaService;
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    @Override
    @Transactional
    public ResultadoOperacion solicitarRecuperacion(String correo, String urlBaseApp, HttpServletRequest request) {
        String correoNormalizado = normalizarCorreo(correo);
        if (!StringUtils.hasText(correoNormalizado) || !PATRON_CORREO.matcher(correoNormalizado).matches()) {
            return ResultadoOperacion.error("Ingresa un correo electrónico válido.");
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        limpiarCooldownInexistentes(ahora.toInstant());

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoElectronicoIgnoreCase(correoNormalizado);
        if (usuarioOpt.isEmpty()) {
            Instant ultimoIntento = cooldownCorreosInexistentes.get(correoNormalizado);
            if (ultimoIntento != null) {
                long restante = COOLDOWN_REENVIO_SEGUNDOS - Duration.between(ultimoIntento, ahora.toInstant()).getSeconds();
                if (restante > 0) {
                    return ResultadoOperacion.espera(
                            "Debes esperar " + restante + " segundos antes de solicitar un nuevo token.",
                            restante
                    );
                }
            }
            cooldownCorreosInexistentes.put(correoNormalizado, ahora.toInstant());
            return ResultadoOperacion.ok(MENSAJE_SOLICITUD_NEUTRO, COOLDOWN_REENVIO_SEGUNDOS);
        }

        Usuario usuario = usuarioOpt.get();
        Optional<TokenRecuperacion> ultimoActivoOpt = tokenRepository
                .findFirstByRutUsuarioAndUsadoFalseAndRevocadoFalseOrderByFechaCreacionDesc(usuario.getRut());

        if (ultimoActivoOpt.isPresent()) {
            long transcurrido = Duration.between(ultimoActivoOpt.get().getFechaCreacion(), ahora).getSeconds();
            long restante = COOLDOWN_REENVIO_SEGUNDOS - transcurrido;
            if (restante > 0) {
                return ResultadoOperacion.espera(
                        "Debes esperar " + restante + " segundos antes de solicitar un nuevo token.",
                        restante
                );
            }
        }

        JavaMailSender mailSender = obtenerMailSenderConfigurado();
        if (mailSender == null) {
            log.warn("No se envió correo de recuperación para RUT {}: configuración SMTP incompleta.", usuario.getRut());
            return ResultadoOperacion.error(
                    "No fue posible enviar el correo de recuperación en este momento. Inténtalo nuevamente más tarde."
            );
        }

        String tokenPlano = generarTokenCriptografico();
        String tokenHash = hashSha256Hex(tokenPlano);

        // Revoca tokens previos y purga expirados antiguos antes de persistir el nuevo token.
        tokenRepository.revocarTokensActivosPorUsuario(usuario.getRut());
        tokenRepository.eliminarExpiradosAntesDe(ahora.minusHours(24));

        TokenRecuperacion entidad = new TokenRecuperacion();
        entidad.setRutUsuario(usuario.getRut());
        entidad.setCorreoElectronico(usuario.getCorreoElectronico());
        entidad.setTokenHash(tokenHash);
        entidad.setFechaCreacion(ahora);
        entidad.setFechaExpiracion(ahora.plusMinutes(TTL_MINUTOS));
        entidad.setUsado(false);
        entidad.setRevocado(false);
        entidad.setIpSolicitud(extraerIp(request));
        tokenRepository.save(entidad);

        logAuditoriaService.registrarLog(
                usuario.getRut(),
                "RECUPERACION_CLAVE_SOLICITADA",
                "Token_Recuperacion",
                null,
                "Emisión de token criptográfico temporal en base de datos para recuperación de contraseña.",
                request
        );

        String correoDestino = usuario.getCorreoElectronico();
        String enlaceDirecto = construirEnlaceDirecto(urlBaseApp, tokenPlano);
        String html = construirHtmlToken(construirNombreSaludo(usuario), tokenPlano, enlaceDirecto);
        programarCorreoAsyncDespuesDeCommit(
                mailSender,
                correoDestino,
                "SkyWay Airlines — Restablece tu contraseña",
                html
        );

        return ResultadoOperacion.ok(MENSAJE_SOLICITUD_NEUTRO, COOLDOWN_REENVIO_SEGUNDOS);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultadoOperacion validarToken(String token) {
        String tokenLimpio = token != null ? token.trim() : "";
        if (!PATRON_TOKEN.matcher(tokenLimpio).matches()) {
            return ResultadoOperacion.error("El token de recuperación no es válido o tiene formato incorrecto.");
        }

        Optional<TokenRecuperacion> registroOpt = tokenRepository.findByTokenHash(hashSha256Hex(tokenLimpio));
        if (registroOpt.isEmpty()) {
            return ResultadoOperacion.error("El enlace o token de recuperación no existe o ya no es válido.");
        }

        TokenRecuperacion registro = registroOpt.get();
        if (registro.isUsado()) {
            return ResultadoOperacion.error("Este token ya fue utilizado. Solicita una nueva recuperación.");
        }
        if (registro.isRevocado()) {
            return ResultadoOperacion.error("Este token fue reemplazado por una solicitud más reciente.");
        }
        if (!registro.getFechaExpiracion().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            return ResultadoOperacion.error("El token de recuperación expiró (vigencia máxima de " + TTL_MINUTOS + " minutos).");
        }

        return ResultadoOperacion.ok("Token verificado. Ingresa tu nueva contraseña.");
    }

    @Override
    @Transactional
    public ResultadoOperacion restablecerContrasena(
            String token,
            String nuevaContrasena,
            String confirmarContrasena,
            HttpServletRequest request
    ) {
        String tokenLimpio = token != null ? token.trim() : "";
        if (!PATRON_TOKEN.matcher(tokenLimpio).matches()) {
            return ResultadoOperacion.error("El token de seguridad ingresado no es válido.");
        }

        String errorContrasena = validarFortalezaContrasena(nuevaContrasena, confirmarContrasena);
        if (errorContrasena != null) {
            return ResultadoOperacion.error(errorContrasena);
        }

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        Optional<TokenRecuperacion> registroOpt = tokenRepository.findByTokenHashForUpdate(hashSha256Hex(tokenLimpio));
        if (registroOpt.isEmpty()) {
            return ResultadoOperacion.error("El token de recuperación no existe o es inválido.");
        }

        TokenRecuperacion registro = registroOpt.get();
        if (registro.isUsado()) {
            return ResultadoOperacion.error("Este token ya fue utilizado. Solicita un nuevo enlace de recuperación.");
        }
        if (registro.isRevocado()) {
            return ResultadoOperacion.error("Este token fue revocado por una solicitud posterior.");
        }
        if (!registro.getFechaExpiracion().isAfter(ahora)) {
            registro.setRevocado(true);
            tokenRepository.save(registro);
            return ResultadoOperacion.error("El token ha expirado. Solicita un nuevo enlace de recuperación.");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(registro.getRutUsuario());
        if (usuarioOpt.isEmpty()) {
            return ResultadoOperacion.error("No fue posible localizar la cuenta asociada al token.");
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuarioRepository.save(usuario);

        registro.setUsado(true);
        registro.setFechaUso(ahora);
        registro.setIpUso(extraerIp(request));
        tokenRepository.save(registro);
        tokenRepository.revocarTokensActivosPorUsuario(usuario.getRut());

        logAuditoriaService.registrarLog(
                usuario.getRut(),
                "RECUPERACION_CLAVE_EXITOSA",
                "Usuario",
                null,
                "Contraseña actualizada mediante token criptográfico de un solo uso en base de datos.",
                request
        );

        enviarCorreoConfirmacionCambioAsync(usuario);

        return ResultadoOperacion.ok("Tu contraseña ha sido actualizada correctamente. Ya puedes iniciar sesión.");
    }

    private String validarFortalezaContrasena(String nuevaContrasena, String confirmarContrasena) {
        if (!StringUtils.hasText(nuevaContrasena) || !StringUtils.hasText(confirmarContrasena)) {
            return "Debes ingresar y confirmar tu nueva contraseña.";
        }
        if (nuevaContrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            return "La nueva contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.";
        }
        boolean tieneLetra = nuevaContrasena.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = nuevaContrasena.chars().anyMatch(Character::isDigit);
        if (!tieneLetra || !tieneNumero) {
            return "La contraseña debe incluir al menos una letra y un número.";
        }
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            return "Las contraseñas ingresadas no coinciden.";
        }
        return null;
    }

    private void enviarCorreoConfirmacionCambioAsync(Usuario usuario) {
        JavaMailSender mailSender = obtenerMailSenderConfigurado();
        if (mailSender == null) {
            return;
        }

        String correoDestino = usuario.getCorreoElectronico();
        String html = construirHtmlConfirmacion(construirNombreSaludo(usuario));
        programarCorreoAsyncDespuesDeCommit(
                mailSender,
                correoDestino,
                "SkyWay Airlines — Tu contraseña ha sido actualizada",
                html
        );
    }

    private void programarCorreoAsyncDespuesDeCommit(
            JavaMailSender mailSender,
            String destinatario,
            String asunto,
            String html
    ) {
        Runnable tareaEnvio = () -> mailExecutor.execute(() -> despacharCorreo(mailSender, destinatario, asunto, html));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    tareaEnvio.run();
                }
            });
        } else {
            tareaEnvio.run();
        }
    }

    private JavaMailSender obtenerMailSenderConfigurado() {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailFrom) || !StringUtils.hasText(mailPassword)) {
            return null;
        }
        return mailSender;
    }

    private boolean despacharCorreo(JavaMailSender mailSender, String destinatario, String asunto, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(mailFrom);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(html, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException | MailException ex) {
            log.error("Error al despachar correo de recuperación a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }

    private String construirEnlaceDirecto(String urlBaseApp, String tokenPlano) {
        if (!StringUtils.hasText(urlBaseApp)) {
            return null;
        }
        String baseLimpia = urlBaseApp.endsWith("/") ? urlBaseApp.substring(0, urlBaseApp.length() - 1) : urlBaseApp;
        String tokenEnc = URLEncoder.encode(tokenPlano, StandardCharsets.UTF_8);
        return baseLimpia + "/home/recuperar-clave.xhtml?token=" + tokenEnc;
    }

    private String construirNombreSaludo(Usuario usuario) {
        String nombre = usuario.getNombre() != null ? usuario.getNombre().trim() : "";
        String apellido = usuario.getApellido() != null ? usuario.getApellido().trim() : "";
        String completo = (nombre + " " + apellido).trim();
        return StringUtils.hasText(completo) ? escaparHtml(completo) : "Pasajero";
    }

    private String construirHtmlToken(String nombreSeguro, String tokenPlano, String enlaceDirecto) {
        String bloqueEnlace = enlaceDirecto != null
                ? "<div style='margin:24px 0;text-align:center;'>"
                + "<a href='" + escaparHtml(enlaceDirecto) + "' "
                + "style='display:inline-block;background:#1d4ed8;color:#ffffff;text-decoration:none;"
                + "font-weight:700;padding:12px 24px;border-radius:8px;font-size:14px;'>"
                + "Restablecer contraseña</a></div>"
                : "";

        return "<!DOCTYPE html><html><body style='margin:0;padding:24px;background:#0b1220;font-family:Arial,sans-serif;color:#e2e8f0;'>"
                + "<div style='max-width:560px;margin:0 auto;background:#101b33;border:1px solid #1e3150;border-radius:14px;padding:28px;'>"
                + "<div style='font-size:12px;letter-spacing:1.4px;text-transform:uppercase;color:#93c5fd;font-weight:700;'>SKYWAY AIRLINES · SEGURIDAD DE CUENTA</div>"
                + "<h1 style='margin:10px 0 14px;font-size:22px;color:#f8fafc;'>Recuperación de contraseña</h1>"
                + "<p style='margin:0 0 16px;font-size:14px;line-height:1.6;color:#cbd5e1;'>Hola, <strong>" + nombreSeguro + "</strong>.</p>"
                + "<p style='margin:0 0 16px;font-size:14px;line-height:1.6;color:#cbd5e1;'>Recibimos una solicitud para restablecer la contraseña de tu cuenta SkyWay. Haz clic en el botón para continuar de forma segura:</p>"
                + bloqueEnlace
                + "<p style='margin:0 0 8px;font-size:13px;color:#94a3b8;'>Si lo prefieres, copia y pega este token único de seguridad en el formulario de recuperación:</p>"
                + "<div style='background:#0b1220;border:1px solid #1e3150;border-radius:8px;padding:12px;margin:0 0 18px;word-break:break-all;font-family:monospace;font-size:13px;color:#93c5fd;text-align:center;'>"
                + escaparHtml(tokenPlano)
                + "</div>"
                + "<p style='margin:0 0 10px;font-size:13px;line-height:1.5;color:#94a3b8;'>Este token es único, de un solo uso y caduca en <strong>" + TTL_MINUTOS + " minutos</strong>.</p>"
                + "<p style='margin:0;font-size:12px;line-height:1.5;color:#64748b;'>Si no realizaste esta solicitud, puedes ignorar este mensaje; tu contraseña actual no cambiará.</p>"
                + "</div></body></html>";
    }

    private String construirHtmlConfirmacion(String nombreSeguro) {
        return "<!DOCTYPE html><html><body style='margin:0;padding:24px;background:#0b1220;font-family:Arial,sans-serif;color:#e2e8f0;'>"
                + "<div style='max-width:560px;margin:0 auto;background:#101b33;border:1px solid #1e3150;border-radius:14px;padding:28px;'>"
                + "<div style='font-size:12px;letter-spacing:1.4px;text-transform:uppercase;color:#93c5fd;font-weight:700;'>SKYWAY AIRLINES · AVISO DE SEGURIDAD</div>"
                + "<h1 style='margin:10px 0 14px;font-size:22px;color:#f8fafc;'>Contraseña actualizada</h1>"
                + "<p style='margin:0 0 14px;font-size:14px;line-height:1.6;color:#cbd5e1;'>Hola, <strong>" + nombreSeguro + "</strong>.</p>"
                + "<p style='margin:0 0 14px;font-size:14px;line-height:1.6;color:#cbd5e1;'>Te confirmamos que la contraseña de tu cuenta SkyWay fue modificada correctamente.</p>"
                + "<p style='margin:0;font-size:12px;line-height:1.5;color:#64748b;'>Si no reconoces este cambio, solicita un nuevo restablecimiento de inmediato desde el portal de acceso.</p>"
                + "</div></body></html>";
    }

    private String generarTokenCriptografico() {
        byte[] bytes = new byte[BYTES_ENTROPIA_TOKEN];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashSha256Hex(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible en el entorno JVM.", ex);
        }
    }

    private void limpiarCooldownInexistentes(Instant ahora) {
        cooldownCorreosInexistentes.entrySet().removeIf(
                entry -> Duration.between(entry.getValue(), ahora).getSeconds() >= COOLDOWN_REENVIO_SEGUNDOS
        );
    }

    private String normalizarCorreo(String correo) {
        return correo == null ? "" : correo.trim().toLowerCase(Locale.ROOT);
    }

    private String extraerIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            int coma = forwarded.indexOf(',');
            String ip = (coma > 0 ? forwarded.substring(0, coma) : forwarded).trim();
            return ip.length() > 45 ? ip.substring(0, 45) : ip;
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null && remoteAddr.length() > 45 ? remoteAddr.substring(0, 45) : remoteAddr;
    }

    private String escaparHtml(String texto) {
        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
