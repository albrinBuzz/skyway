package com.SkyWay.views;

import java.io.IOException;

import com.SkyWay.modules.auditoria.application.LogAuditoriaService;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

@Named("loginBean")
@RequestScoped
public class LoginBean {

    private String username;
    private String password;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationSuccessHandler auth;

    @Autowired
    private FilterChain chain;

    @Autowired
    private SecurityContextRepository securityContextRepository;

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @PostConstruct
    public void init() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Usuario ya autenticado
        }
    }

    public void login() {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            if (authentication.isAuthenticated()) {
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(authentication);
                securityContextRepository.saveContext(context, request, response);

                auth.onAuthenticationSuccess(request, response, chain, authentication);

                // 📝 REGISTRO EN AUDITORÍA: INICIO DE SESIÓN EXITOSO (DETALLE COMPLETO)
                try {
                    String detalleExito = String.format(
                            "Autenticación completada | Usuario: '%s' | Principal: '%s' | Autoridades/Roles: %s | Detalles Autenticación: %s",
                            username,
                            authentication.getName(),
                            authentication.getAuthorities(),
                            authentication.getDetails() != null ? authentication.getDetails().toString() : "N/A"
                    );

                    logAuditoriaService.registrarLog(
                            username,
                            "LOGIN_EXITOSO",
                            "Usuario",
                            null,
                            detalleExito,
                            request
                    );
                } catch (Exception ex) {
                    Logger.logError("Error al registrar log de auditoría (Login Exitoso): " + ex.getMessage());
                }

                Logger.logInfo("Autenticación exitosa");
                redigir(request, response);
            }
        } catch (BadCredentialsException e) {
            // 📝 REGISTRO EN AUDITORÍA: INTENTO FALLIDO DE CREDENCIALES
            try {
                String detalleFallido = String.format(
                        "Fallo de Autenticación | Credenciales Erróneas | Usuario Intentado: '%s' | Longitud Clave: %d caracteres | Causa: BadCredentialsException",
                        username,
                        password != null ? password.length() : 0
                );

                logAuditoriaService.registrarLog(
                        username,
                        "INTENTO_LOGIN_FALLIDO",
                        "Usuario",
                        null,
                        detalleFallido,
                        request
                );
            } catch (Exception ex) {
                Logger.logError("Error al registrar log de auditoría (Credenciales erróneas): " + ex.getMessage());
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales incorrectas", "Nombre de usuario o contraseña incorrectos"));
        } catch (Exception e) {
            // 📝 REGISTRO EN AUDITORÍA: ERROR NO CONTROLADO
            try {
                String detalleError = String.format(
                        "Excepción Grave en Autenticación | Usuario Intentado: '%s' | Tipo Excepción: %s | Mensaje: %s",
                        username,
                        e.getClass().getName(),
                        e.getMessage()
                );

                logAuditoriaService.registrarLog(
                        username,
                        "ERROR_AUTENTICACION",
                        "Usuario",
                        null,
                        detalleError,
                        request
                );
            } catch (Exception ex) {
                Logger.logError("Error al registrar log de excepción de Login: " + ex.getMessage());
            }

            System.out.println(e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de autenticación", "Hubo un problema con el login"));
        }
    }

    private void redigir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        String redirectUrl = "/";

        if (savedRequest != null) {
            redirectUrl = savedRequest.getRedirectUrl();
            Logger.logInfo("Redirección a URL original detectada: " + redirectUrl);
        } else {
            redirectUrl = request.getRequestURI();
            Logger.logInfo("No se detectó URL original guardada. Redirigiendo a: " + redirectUrl);
        }

        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectUrl);
        } catch (IOException e) {
            Logger.logError("Error durante la redirección: " + e.getMessage());
            throw e;
        }
    }

    public String logout() {
        Authentication authContext = SecurityContextHolder.getContext().getAuthentication();
        if (authContext != null && authContext.isAuthenticated()) {
            try {
                String detalleLogout = String.format(
                        "Cierre de sesión | Usuario: '%s' | Principal: '%s' | Roles: %s",
                        username != null ? username : authContext.getName(),
                        authContext.getName(),
                        authContext.getAuthorities()
                );

                logAuditoriaService.registrarLog(
                        authContext.getName(),
                        "LOGOUT",
                        "Usuario",
                        null,
                        detalleLogout,
                        request
                );
            } catch (Exception ex) {
                Logger.logError("Error al registrar log de auditoría (Logout): " + ex.getMessage());
            }
        }

        SecurityContextHolder.clearContext();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        request.getSession().invalidate();
        return "login.xhtml";
    }
}