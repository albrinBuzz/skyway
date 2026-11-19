package com.SkyWay.modules.usuario.application.serviceImpl;

import com.SkyWay.modules.auditoria.application.LogAuditoriaService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import com.SkyWay.modules.usuario.domain.service.UsuarioPerfilService;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de gestión y seguridad del perfil de usuario autenticado.
 */
@Service
public class UsuarioPerfilServiceImpl implements UsuarioPerfilService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioPerfilServiceImpl.class);

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final int LONGITUD_MAXIMA_CONTRASENA = 72;

    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PATRON_TELEFONO = Pattern.compile(
            "^[+0-9][0-9\\s()-]{6,19}$"
    );

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogAuditoriaService logAuditoriaService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    private final ExecutorService mailExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "skyway-profile-mail");
        thread.setDaemon(true);
        return thread;
    });

    @Value("${spring.mail.username:}")
    private String mailFrom;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    public UsuarioPerfilServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            LogAuditoriaService logAuditoriaService,
            ObjectProvider<JavaMailSender> mailSenderProvider
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.logAuditoriaService = logAuditoriaService;
        this.mailSenderProvider = mailSenderProvider;
    }

    @PreDestroy
    public void cerrarEjecutor() {
        mailExecutor.shutdown();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorRut(String rut) {
        if (!StringUtils.hasText(rut)) {
            return Optional.empty();
        }
        return usuarioRepository.findById(rut.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorCorreo(String correo) {
        if (!StringUtils.hasText(correo)) {
            return Optional.empty();
        }
        return usuarioRepository.findByCorreoElectronicoIgnoreCase(correo.trim());
    }

    @Override
    @Transactional
    public ResultadoPerfil actualizarPerfil(
            String rut,
            String nombre,
            String apellido,
            String correoElectronico,
            String telefono,
            Date fechaNacimiento,
            HttpServletRequest request
    ) {
        if (!StringUtils.hasText(rut)) {
            return ResultadoPerfil.error("No se pudo identificar tu cuenta activa.");
        }

        String nombreLimpio = limpiarTexto(nombre);
        String apellidoLimpio = limpiarTexto(apellido);
        String correoLimpio = limpiarTexto(correoElectronico).toLowerCase(Locale.ROOT);
        String telefonoLimpio = limpiarTexto(telefono);

        if (nombreLimpio.length() < 2 || nombreLimpio.length() > 120) {
            return ResultadoPerfil.error("El nombre debe tener entre 2 y 120 caracteres.");
        }
        if (apellidoLimpio.length() < 2 || apellidoLimpio.length() > 120) {
            return ResultadoPerfil.error("El apellido debe tener entre 2 y 120 caracteres.");
        }
        if (!PATRON_CORREO.matcher(correoLimpio).matches() || correoLimpio.length() > 100) {
            return ResultadoPerfil.error("Ingresa un correo electrónico válido.");
        }
        if (!PATRON_TELEFONO.matcher(telefonoLimpio).matches()) {
            return ResultadoPerfil.error("Ingresa un teléfono válido (entre 7 y 20 caracteres, por ejemplo +56 9 1234 5678).");
        }

        String errorFecha = validarFechaNacimiento(fechaNacimiento);
        if (errorFecha != null) {
            return ResultadoPerfil.error(errorFecha);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(rut.trim());
        if (usuarioOpt.isEmpty()) {
            return ResultadoPerfil.error("No se encontró el usuario en base de datos.");
        }

        Usuario usuario = usuarioOpt.get();
        Optional<Usuario> existenteCorreo = usuarioRepository.findByCorreoElectronicoIgnoreCase(correoLimpio);
        if (existenteCorreo.isPresent() && !existenteCorreo.get().getRut().equalsIgnoreCase(usuario.getRut())) {
            return ResultadoPerfil.error("El correo electrónico ingresado ya está asociado a otra cuenta.");
        }

        String correoAnterior = usuario.getCorreoElectronico();
        usuario.setNombre(nombreLimpio);
        usuario.setApellido(apellidoLimpio);
        usuario.setCorreoElectronico(correoLimpio);
        usuario.setTelefono(telefonoLimpio);
        usuario.setFechaNacimiento(new java.sql.Date(fechaNacimiento.getTime()));

        Usuario guardado = usuarioRepository.save(usuario);

        logAuditoriaService.registrarLog(
                guardado.getRut(),
                "PERFIL_ACTUALIZADO",
                "Usuario",
                null,
                "Actualización de datos personales de perfil"
                        + (!correoLimpio.equalsIgnoreCase(correoAnterior) ? " (incluye cambio de correo)." : "."),
                request
        );

        return ResultadoPerfil.ok("Tu perfil se actualizó correctamente.", guardado);
    }

    @Override
    @Transactional
    public ResultadoPerfil cambiarContrasena(
            String rut,
            String contrasenaActual,
            String nuevaContrasena,
            String confirmarContrasena,
            HttpServletRequest request
    ) {
        if (!StringUtils.hasText(rut)) {
            return ResultadoPerfil.error("No se pudo identificar tu cuenta activa.");
        }
        if (!StringUtils.hasText(contrasenaActual)) {
            return ResultadoPerfil.error("Ingresa tu contraseña actual para confirmar el cambio.");
        }

        String errorFortaleza = validarFortalezaContrasena(nuevaContrasena, confirmarContrasena);
        if (errorFortaleza != null) {
            return ResultadoPerfil.error(errorFortaleza);
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(rut.trim());
        if (usuarioOpt.isEmpty()) {
            return ResultadoPerfil.error("No se encontró el usuario en base de datos.");
        }

        Usuario usuario = usuarioOpt.get();
        if (!StringUtils.hasText(usuario.getContrasena())
                || !passwordEncoder.matches(contrasenaActual, usuario.getContrasena())) {
            return ResultadoPerfil.error("La contraseña actual ingresada es incorrecta.");
        }

        if (passwordEncoder.matches(nuevaContrasena, usuario.getContrasena())) {
            return ResultadoPerfil.error("La nueva contraseña debe ser diferente a tu contraseña actual.");
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        Usuario guardado = usuarioRepository.save(usuario);

        logAuditoriaService.registrarLog(
                guardado.getRut(),
                "CAMBIO_CONTRASENA_PERFIL",
                "Usuario",
                null,
                "Cambio voluntario de contraseña desde la gestión de perfil.",
                request
        );

        enviarAvisoSeguridadCambioClaveAsync(guardado);

        return ResultadoPerfil.ok("Tu contraseña ha sido actualizada correctamente.", guardado);
    }

    private String validarFechaNacimiento(Date fechaNacimiento) {
        if (fechaNacimiento == null) {
            return "La fecha de nacimiento es obligatoria.";
        }
        LocalDate nacimiento = new java.sql.Date(fechaNacimiento.getTime()).toLocalDate();
        LocalDate hoy = LocalDate.now(ZoneId.systemDefault());
        if (!nacimiento.isBefore(hoy)) {
            return "La fecha de nacimiento debe ser anterior a la fecha actual.";
        }
        if (nacimiento.isBefore(LocalDate.of(1915, 1, 1))) {
            return "Ingresa una fecha de nacimiento válida.";
        }
        return null;
    }

    private String validarFortalezaContrasena(String nuevaContrasena, String confirmarContrasena) {
        if (!StringUtils.hasText(nuevaContrasena) || !StringUtils.hasText(confirmarContrasena)) {
            return "Debes ingresar y confirmar la nueva contraseña.";
        }
        if (nuevaContrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            return "La nueva contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.";
        }
        if (nuevaContrasena.length() > LONGITUD_MAXIMA_CONTRASENA) {
            return "La nueva contraseña no puede superar los " + LONGITUD_MAXIMA_CONTRASENA + " caracteres.";
        }
        boolean tieneLetra = nuevaContrasena.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = nuevaContrasena.chars().anyMatch(Character::isDigit);
        if (!tieneLetra || !tieneNumero) {
            return "La nueva contraseña debe incluir al menos una letra y un número.";
        }
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            return "Las nuevas contraseñas no coinciden.";
        }
        return null;
    }

    private void enviarAvisoSeguridadCambioClaveAsync(Usuario usuario) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null || !StringUtils.hasText(mailFrom) || !StringUtils.hasText(mailPassword)) {
            return;
        }
        String correoDestino = usuario.getCorreoElectronico();
        if (!StringUtils.hasText(correoDestino)) {
            return;
        }
        String nombreCompleto = ((usuario.getNombre() != null ? usuario.getNombre() : "") + " "
                + (usuario.getApellido() != null ? usuario.getApellido() : "")).trim();
        String saludoSeguro = escaparHtml(StringUtils.hasText(nombreCompleto) ? nombreCompleto : "Usuario");
        String html = "<!DOCTYPE html><html><body style='margin:0;padding:24px;background:#0b1220;font-family:Arial,sans-serif;color:#e2e8f0;'>"
                + "<div style='max-width:560px;margin:0 auto;background:#101b33;border:1px solid #1e3150;border-radius:14px;padding:28px;'>"
                + "<div style='font-size:12px;letter-spacing:1.4px;text-transform:uppercase;color:#93c5fd;font-weight:700;'>SKYWAY AIRLINES · SEGURIDAD DE CUENTA</div>"
                + "<h1 style='margin:10px 0 14px;font-size:21px;color:#f8fafc;'>Cambio de contraseña confirmado</h1>"
                + "<p style='margin:0 0 14px;font-size:14px;line-height:1.6;color:#cbd5e1;'>Hola, <strong>" + saludoSeguro + "</strong>.</p>"
                + "<p style='margin:0 0 14px;font-size:14px;line-height:1.6;color:#cbd5e1;'>Tu contraseña de acceso a SkyWay fue actualizada desde tu perfil de usuario.</p>"
                + "<p style='margin:0;font-size:12px;line-height:1.5;color:#64748b;'>Si no realizaste este cambio, utiliza de inmediato la recuperación de contraseña en el portal.</p>"
                + "</div></body></html>";

        Runnable tarea = () -> mailExecutor.execute(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
                helper.setFrom(mailFrom);
                helper.setTo(correoDestino);
                helper.setSubject("SkyWay Airlines — Contraseña actualizada desde tu perfil");
                helper.setText(html, true);
                mailSender.send(message);
            } catch (MessagingException | MailException ex) {
                log.warn("No se pudo enviar aviso de cambio de clave a {}: {}", correoDestino, ex.getMessage());
            }
        });

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    tarea.run();
                }
            });
        } else {
            tarea.run();
        }
    }

    private String limpiarTexto(String valor) {
        return valor == null ? "" : valor.trim();
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
