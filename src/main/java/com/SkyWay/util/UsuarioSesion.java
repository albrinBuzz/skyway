package com.SkyWay.util;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/** Obtiene el RUT del usuario autenticado desde el contexto del servidor (nunca desde parametros del cliente). */
public final class UsuarioSesion {

    private UsuarioSesion() {}

    /** @return RUT del usuario autenticado, o null si no hay sesion autenticada. */
    public static String rutAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra == null) {
            return null;
        }
        Object u = ra.getAttribute("usuario", RequestAttributes.SCOPE_SESSION);
        return (u instanceof Usuario usuario) ? usuario.getRut() : null;
    }
}
