package com.SkyWay.views;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.repository.UsuarioRepository;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;
import com.SkyWay.util.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Named("usuarioBean")
@RequestScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final int LONGITUD_MAXIMA_CONTRASENA = 72;
    private static final Pattern PATRON_CORREO = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PATRON_TELEFONO = Pattern.compile(
            "^[+0-9][0-9\\s()-]{6,19}$"
    );
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private AuthenticationSuccessHandler authSuccessHandler;

    @Autowired
    private FilterChain chain;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    private Usuario usuario;
    private String confirmarContrasena;
    private String tipoDocumento = "CEDULA";
    private String nacionalidad = "Chilena";
    private Integer diaNacimiento;
    private Integer mesNacimiento;
    private Integer anioNacimiento;

    public UsuarioBean() {
        usuario = new Usuario();
    }

    public List<Integer> getDiasNacimiento() {
        List<Integer> dias = new ArrayList<>(31);
        for (int d = 1; d <= 31; d++) {
            dias.add(d);
        }
        return dias;
    }

    public List<Integer> getAniosNacimiento() {
        int maxAnio = LocalDate.now().minusYears(18).getYear();
        List<Integer> anios = new ArrayList<>(maxAnio - 1925 + 1);
        for (int a = maxAnio; a >= 1925; a--) {
            anios.add(a);
        }
        return anios;
    }

    public Integer getDiaNacimiento() {
        return diaNacimiento;
    }

    public void setDiaNacimiento(Integer diaNacimiento) {
        this.diaNacimiento = diaNacimiento;
    }

    public Integer getMesNacimiento() {
        return mesNacimiento;
    }

    public void setMesNacimiento(Integer mesNacimiento) {
        this.mesNacimiento = mesNacimiento;
    }

    public Integer getAnioNacimiento() {
        return anioNacimiento;
    }

    public void setAnioNacimiento(Integer anioNacimiento) {
        this.anioNacimiento = anioNacimiento;
    }

    public Date getFechaMaxMayorEdad() {
        LocalDate limite = LocalDate.now().minusYears(18);
        return Date.from(limite.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public String getFechaLimiteTexto() {
        return LocalDate.now().minusYears(18).format(FORMATO_FECHA);
    }

    public String getAnioMaximoMayorEdad() {
        return String.valueOf(LocalDate.now().minusYears(18).getYear());
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    @Transactional
    public void registrar() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (usuario == null) {
            return;
        }

        normalizarCampos();

        if (!StringUtils.hasText(usuario.getRut()) || usuario.getRut().length() < 6) {
            agregarError(fc, "RUT inválido", "Ingresa un RUT o identificador válido.");
            return;
        }
        if (!StringUtils.hasText(usuario.getNombre()) || usuario.getNombre().length() < 3) {
            agregarError(fc, "Nombre inválido", "Ingresa tu nombre completo (mínimo 3 caracteres).");
            return;
        }
        if (!StringUtils.hasText(usuario.getApellido()) || usuario.getApellido().length() < 4) {
            agregarError(fc, "Apellido inválido", "Ingresa tu apellido (mínimo 4 caracteres).");
            return;
        }
        if (!PATRON_CORREO.matcher(usuario.getCorreoElectronico()).matches()) {
            agregarError(fc, "Correo inválido", "Ingresa un correo electrónico con formato válido.");
            return;
        }
        if (!PATRON_TELEFONO.matcher(usuario.getTelefono()).matches()) {
            agregarError(fc, "Teléfono inválido", "Ingresa un teléfono móvil válido (ej: +56 9 1111 1111).");
            return;
        }
        if (!StringUtils.hasText(usuario.getDocumentoIdentidad()) || usuario.getDocumentoIdentidad().length() < 5) {
            agregarError(fc, "Documento inválido", "Ingresa un número de documento válido.");
            return;
        }

        LocalDate fechaNac = null;
        if (diaNacimiento != null && mesNacimiento != null && anioNacimiento != null) {
            try {
                fechaNac = LocalDate.of(anioNacimiento, mesNacimiento, diaNacimiento);
                usuario.setFechaNacimiento(java.sql.Date.valueOf(fechaNac));
            } catch (DateTimeException ex) {
                agregarError(fc, "Fecha inválida", "La combinación de día, mes y año ingresada no existe en el calendario.");
                return;
            }
        } else if (usuario.getFechaNacimiento() != null) {
            fechaNac = new java.sql.Date(usuario.getFechaNacimiento().getTime()).toLocalDate();
        } else {
            agregarError(fc, "Fecha requerida", "Selecciona día, mes y año de nacimiento (solo mayores de 18 años).");
            return;
        }

        LocalDate hoy = LocalDate.now();
        if (fechaNac.getYear() < 1925) {
            agregarError(fc, "Fecha inválida", "El año de nacimiento ingresado no es válido.");
            return;
        }
        if (fechaNac.isAfter(hoy.minusYears(18))) {
            int edad = Math.max(0, Period.between(fechaNac, hoy).getYears());
            agregarError(fc, "Restricción de edad (+18)",
                    "Debes tener al menos 18 años para registrarte en SkyWay (edad calculada: " + edad + " años).");
            return;
        }

        String errorClave = validarContrasena(usuario.getContrasena(), confirmarContrasena);
        if (errorClave != null) {
            agregarError(fc, "Seguridad de contraseña", errorClave);
            return;
        }

        try {
            Optional<Usuario> existenteRut = usuarioService.findByRut(usuario.getRut());
            if (existenteRut.isPresent() && StringUtils.hasText(existenteRut.get().getContrasena())) {
                agregarError(fc, "Cuenta existente", "Ya existe una cuenta registrada con este RUT.");
                return;
            }

            Optional<Usuario> existenteCorreo = usuarioRepository.findByCorreoElectronicoIgnoreCase(usuario.getCorreoElectronico());
            if (existenteCorreo.isPresent()
                    && (StringUtils.hasText(existenteCorreo.get().getContrasena())
                    || !existenteCorreo.get().getRut().equalsIgnoreCase(usuario.getRut()))) {
                agregarError(fc, "Correo registrado", "El correo electrónico ya está asociado a otra cuenta.");
                return;
            }

            Optional<Usuario> existenteDoc = usuarioRepository.findByDocumentoIdentidadIgnoreCase(usuario.getDocumentoIdentidad());
            if (existenteDoc.isPresent() && !existenteDoc.get().getRut().equalsIgnoreCase(usuario.getRut())) {
                agregarError(fc, "Documento registrado", "El número de documento ya está asociado a otra cuenta.");
                return;
            }

            String rawPassword = usuario.getContrasena();
            String userEmail = usuario.getCorreoElectronico();

            usuarioService.crearPasajeroConDetalle(usuario, tipoDocumento, nacionalidad);
            Logger.logInfo("Usuario " + userEmail + " registrado con éxito.");

            autoLogin(userEmail, rawPassword);
        } catch (Exception e) {
            Logger.logError("Error al registrar usuario: " + e.getMessage());
            agregarError(fc, "Error en el registro", "No se pudo completar el registro. Verifica tus datos e inténtalo nuevamente.");
        }
    }

    private void normalizarCampos() {
        usuario.setRut(formatearRutConDv(usuario.getRut()));
        usuario.setNombre(limpiar(usuario.getNombre()));
        usuario.setApellido(limpiar(usuario.getApellido()));
        usuario.setCorreoElectronico(limpiar(usuario.getCorreoElectronico()).toLowerCase(Locale.ROOT));
        usuario.setTelefono(limpiar(usuario.getTelefono()));
        usuario.setDocumentoIdentidad(limpiar(usuario.getDocumentoIdentidad()).toUpperCase(Locale.ROOT));
    }

    private String formatearRutConDv(String raw) {
        String limpio = limpiar(raw).toUpperCase(Locale.ROOT).replaceAll("[^0-9K-]", "");
        if (!StringUtils.hasText(limpio)) {
            return "";
        }
        int guionIdx = limpio.indexOf('-');
        if (guionIdx > 0) {
            String cuerpo = limpio.substring(0, guionIdx).replaceAll("[^0-9]", "");
            String dvRaw = limpio.substring(guionIdx + 1).replaceAll("[^0-9K]", "");
            if (cuerpo.length() > 8) {
                cuerpo = cuerpo.substring(0, 8);
            }
            if (!StringUtils.hasText(cuerpo)) {
                return "";
            }
            String dv = StringUtils.hasText(dvRaw) ? dvRaw.substring(0, 1) : calcularDigitoVerificador(cuerpo);
            return cuerpo + "-" + dv;
        }

        String sinGuion = limpio.replace("-", "");
        if (sinGuion.length() == 9) {
            String cuerpo = sinGuion.substring(0, 8).replaceAll("[^0-9]", "");
            String dv = sinGuion.substring(8, 9);
            return StringUtils.hasText(cuerpo) ? cuerpo + "-" + dv : sinGuion;
        }
        if (sinGuion.length() >= 7 && sinGuion.length() <= 8 && sinGuion.chars().allMatch(Character::isDigit)) {
            return sinGuion + "-" + calcularDigitoVerificador(sinGuion);
        }
        if (sinGuion.length() > 9) {
            String cuerpo = sinGuion.substring(0, 8).replaceAll("[^0-9]", "");
            String dv = sinGuion.substring(8, 9);
            return StringUtils.hasText(cuerpo) ? cuerpo + "-" + dv : sinGuion;
        }
        return sinGuion;
    }

    private String calcularDigitoVerificador(String cuerpoNumerico) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = cuerpoNumerico.length() - 1; i >= 0; i--) {
            suma += (cuerpoNumerico.charAt(i) - '0') * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return "0";
        }
        if (resto == 10) {
            return "K";
        }
        return String.valueOf(resto);
    }

    private String validarContrasena(String clave, String confirmacion) {
        if (!StringUtils.hasText(clave) || !StringUtils.hasText(confirmacion)) {
            return "Debes ingresar y confirmar tu contraseña.";
        }
        if (clave.length() < LONGITUD_MINIMA_CONTRASENA) {
            return "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.";
        }
        if (clave.length() > LONGITUD_MAXIMA_CONTRASENA) {
            return "La contraseña no debe superar los " + LONGITUD_MAXIMA_CONTRASENA + " caracteres.";
        }
        boolean tieneLetra = clave.chars().anyMatch(Character::isLetter);
        boolean tieneNumero = clave.chars().anyMatch(Character::isDigit);
        if (!tieneLetra || !tieneNumero) {
            return "La contraseña debe incluir al menos una letra y un número.";
        }
        if (!clave.equals(confirmacion)) {
            return "Las contraseñas ingresadas no coinciden.";
        }
        return null;
    }

    private void autoLogin(String email, String password) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            Authentication authentication = authenticationManager.authenticate(authToken);

            if (authentication.isAuthenticated()) {
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(authentication);
                securityContextRepository.saveContext(context, request, response);

                if (authSuccessHandler != null) {
                    authSuccessHandler.onAuthenticationSuccess(request, response, chain, authentication);
                }

                ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
                ec.getFlash().setKeepMessages(true);
                ec.getFlash().put("mensajeExito", "¡Bienvenido a SkyWay! Tu cuenta ha sido creada con éxito.");
                ec.redirect(request.getContextPath() + "/");
            }
        } catch (Exception e) {
            Logger.logError("Error durante el inicio de sesión automático: " + e.getMessage());
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(request.getContextPath() + "/home/login.xhtml");
            } catch (Exception ex) {
                Logger.logError("Error en redirección secundaria: " + ex.getMessage());
            }
        }
    }

    private void agregarError(FacesContext fc, String resumen, String detalle) {
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, resumen, detalle));
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
