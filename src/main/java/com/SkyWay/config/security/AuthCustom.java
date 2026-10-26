package com.SkyWay.config.security;

import java.io.IOException;


import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;


import com.SkyWay.modules.usuario.domain.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthCustom implements AuthenticationSuccessHandler {
	
	@Autowired
	private UsuarioService usService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // Obtener el usuario autenticado
        //Logger.logInfo("En el authSuccess se debe corrigir ");
        Object principal = authentication.getPrincipal();
        //Logger.logInfo(String.valueOf(principal.getClass()));
        if (principal instanceof UserDetails userDetails) {

            Usuario usuario=usService.buscarPorCorreo(userDetails.getUsername());

            // Copia desacoplada de JPA y SIN hash de contrasena: es lo que otros beans leen de la sesion.
            Usuario seguro = new Usuario();
            seguro.setRut(usuario.getRut());
            seguro.setNombre(usuario.getNombre());
            seguro.setApellido(usuario.getApellido());
            seguro.setCorreoElectronico(usuario.getCorreoElectronico());
            seguro.setTelefono(usuario.getTelefono());
            seguro.setDocumentoIdentidad(usuario.getDocumentoIdentidad());
            seguro.setFechaNacimiento(usuario.getFechaNacimiento());
            seguro.setFechaRegistro(usuario.getFechaRegistro());
            seguro.setRoles(usuario.getRoles() == null ? null : new java.util.ArrayList<>(usuario.getRoles()));

            request.getSession().setAttribute("usuario", seguro);
            request.getSession().setAttribute("usuarioRut", usuario.getRut());

        }else {
            Logger.logWarn("Autenticacion sin UserDetails");
        }

    }


}
