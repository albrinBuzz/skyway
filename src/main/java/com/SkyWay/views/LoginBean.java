package com.SkyWay.views;

import java.io.IOException;

import com.SkyWay.util.Logger;
import org.slf4j.LoggerFactory;
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
import jakarta.faces.context.ExternalContext;
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
    private  AuthenticationSuccessHandler auth;

    @Autowired
    private FilterChain chain;
    
    @Autowired
    private  SecurityContextRepository securityContextRepository;
    
    //@Autowired
    //private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    
    
    @Autowired
    HttpServletRequest request;
    
    @Autowired
    HttpServletResponse response;
    
    // Getters y Setters
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
        // Verificar si ya existe una sesión activa
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // Si ya está autenticado, redirigir a la página de inicio

        }
    }
    
    // Método de login
    public void login() {
        try {
            // Crear un token de autenticación con las credenciales del usuario
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Autenticar el token con el AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            if (authentication.isAuthenticated()) {
                // Configurar el contexto de seguridad
                SecurityContext context = SecurityContextHolder.getContext();
                context.setAuthentication(authentication);
                securityContextRepository.saveContext(context, request, response);

                // Manejo exitoso de autenticación
                auth.onAuthenticationSuccess(request, response, chain, authentication);

                //SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);



                Logger.logInfo("Autenticacion exitosas");
                redigir(request, response);
                //redigir();
            }
        } catch (BadCredentialsException e) {
            // Error específico para credenciales incorrectas
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales incorrectas", "Nombre de usuario o contraseña incorrectos"));
        } catch (Exception e) {
            // Manejo general de errores
            System.out.println(e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de autenticación", "Hubo un problema con el login"));
        }
    }

    private void redigir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Guardar el vuelo seleccionado en el contexto de la sesión o como atributo en el bean
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        // Variable para la URL de redirección
        String redirectUrl = "/";

        // Verificar si hay una URL guardada en la sesión (cuando la solicitud fue interceptada)
        if (savedRequest != null) {
            // Obtener la URL original a la que el usuario quería acceder
            redirectUrl = savedRequest.getRedirectUrl();

            // Registrar información detallada sobre la redirección
            Logger.logInfo("Redirección a URL original detectada.");
            Logger.logInfo("URL original (guardada en sesión): " + redirectUrl);
            Logger.logInfo("Método HTTP original: " + savedRequest.getMethod());
            Logger.logInfo("Query String original: " + savedRequest.getRedirectUrl());
            Logger.logInfo("Context Path: " + request.getContextPath());
        } else {
            // Si no hay URL guardada, redirigir al URI actual
            redirectUrl = request.getRequestURI();

            // Registrar información detallada sobre la redirección cuando no hay URL guardada
            Logger.logInfo("No se detectó URL original guardada en sesión.");
            Logger.logInfo("Redirigiendo a la URI actual: " + redirectUrl);
        }

        // Registrar más información sobre la solicitud
        Logger.logInfo("Redirigiendo a URL final: " + redirectUrl);
        Logger.logInfo("Request URI: " + request.getRequestURI());
        Logger.logInfo("Request URL: " + request.getRequestURL().toString());

        // Realizar la redirección a la URL correspondiente
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect(redirectUrl);
        } catch (IOException e) {
            Logger.logError("Error durante la redirección: " + e.getMessage());
            throw e;
        }
    }



    // Método de logout
    public String logout() {
        SecurityContextHolder.clearContext();  // Limpiar el contexto de seguridad
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        request.getSession().invalidate();
        return "login.xhtml";  // Redirige al login
    }
}
