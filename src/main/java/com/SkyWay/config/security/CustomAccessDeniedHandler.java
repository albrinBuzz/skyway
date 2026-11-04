package com.SkyWay.config.security;

import com.SkyWay.modules.auditoria.application.LogAuditoriaService;
import com.SkyWay.util.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Manejador centralizado de autorizaciones fallidas HTTP 403 (Forbidden).
 * Registra formalmente el incidente en Log_Auditoria y redirige al usuario
 * a la vista corporativa /home/403.xhtml sin desvelar arquitectura interna.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = (auth != null && auth.getName() != null) ? auth.getName() : "Anonimo";
        String roles = (auth != null && auth.getAuthorities() != null) ? auth.getAuthorities().toString() : "[]";
        String uri = request.getRequestURI();

        Logger.logWarn(String.format("⛔ [ACCESO DENEGADO 403] Usuario: '%s' | Roles: %s | Recurso: '%s'",
                usuario, roles, uri));

        try {
            String detalleAuditoria = String.format("Acceso no autorizado bloqueado por perfil de usuario | Recurso: '%s' | Roles asignados: %s",
                    uri, roles);

            logAuditoriaService.registrarLogExhaustivo(
                    usuario,
                    "ACCESO_DENEGADO_403",
                    "SECURITY",
                    "Seguridad",
                    null,
                    null,
                    null,
                    detalleAuditoria,
                    accessDeniedException,
                    request
            );
        } catch (Exception ex) {
            Logger.logError("Error al registrar auditoría de acceso denegado (403): " + ex.getMessage());
        }

        // Redirección silenciosa a la página de inicio para mitigar enumeración de endpoints (Path Discovery)
        response.sendRedirect(request.getContextPath() + "/home/index.xhtml");
    }
}
