package com.SkyWay.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;

/** Decide si el usuario autenticado puede ver datos asociados a un RUT (el suyo, o ADMIN). */
@Component
public class AccesoRut {

	@Autowired
	private UsuarioService usuarioService;

	public boolean puedeAcceder(Authentication auth, String rut) {
		if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken || rut == null) {
			return false;
		}
		if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
			return true;
		}
		Usuario u = usuarioService.findUserByEmail(auth.getName());
		return u != null && rut.equalsIgnoreCase(u.getRut());
	}
}
