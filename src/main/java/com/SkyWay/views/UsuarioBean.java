package com.SkyWay.views;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;
import com.SkyWay.util.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

import java.io.Serializable;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

@Named("usuarioBean")
@RequestScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UsuarioService usuarioService;

    // Inyección para Auto-Login con Spring Security
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

    public UsuarioBean() {
        usuario = new Usuario();
    }

    // --- PROPIEDAD DINÁMICA DE FECHA MÁXIMA (18 AÑOS CUMPLES HOY) ---
    public Date getFechaMaxMayorEdad() {
        LocalDate limite = LocalDate.now().minusYears(18);
        return Date.from(limite.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // --- GETTERS Y SETTERS ---
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getConfirmarContrasena() { return confirmarContrasena; }
    public void setConfirmarContrasena(String confirmarContrasena) { this.confirmarContrasena = confirmarContrasena; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNacionalidad() { return nacionalidad; }
    public void setNacionalidad(String nacionalidad) { this.nacionalidad = nacionalidad; }

    @Transactional
    public void registrar() {
        Logger.logInfo("Iniciando proceso de registro para RUT: " + usuario.getRut());
        Date fechaNacDate = usuario.getFechaNacimiento();

        if (!usuario.getContrasena().equals(confirmarContrasena)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación", "Las contraseñas no coinciden."));
            return;
        }

        LocalDate fechaNac = fechaNacDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();

        // 1. Validar que la fecha sea coherente (ej: no menor a 1925)
        if (fechaNac.getYear() < 1925) {
            FacesContext.getCurrentInstance().addMessage("registroForm:fechaNacimiento",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida", "Año de nacimiento no válido."));
            return;
        }

        // 2. Validar Mayoría de Edad (18 años exactos)
        int edad = Period.between(fechaNac, hoy).getYears();
        if (edad < 18) {
            FacesContext.getCurrentInstance().addMessage("registroForm:fechaNacimiento",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Restricción de edad", "Debes tener al menos 18 años para registrarte en SkyWay."));
            return;
        }

        try {
            if (isCorreoExistente(usuario.getCorreoElectronico())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo electrónico ya se encuentra registrado."));
                return;
            }

            String rawPassword = usuario.getContrasena();
            String userEmail = usuario.getCorreoElectronico();

            // 1. Crear el registro en Base de Datos
            usuarioService.crearPasajeroConDetalle(usuario, tipoDocumento, nacionalidad);

            Logger.logInfo("Usuario " + userEmail + " registrado con éxito. Ejecutando inicio de sesión automático...");

            // 2. Auto-Login
            autoLogin(userEmail, rawPassword);

        } catch (Exception e) {
            Logger.logError("Error al registrar usuario: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al procesar el registro."));
        }
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

    private boolean isCorreoExistente(String correo) {
        try {
            long count = entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.correoElectronico = :correo AND u.contrasena IS NOT NULL", Long.class)
                    .setParameter("correo", correo)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}