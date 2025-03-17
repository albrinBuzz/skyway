package com.SkyWay.config.security;

import java.io.IOException;

import com.SkyWay.model.Pasajero;
import com.SkyWay.model.Piloto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.SkyWay.model.Usuario;
import com.SkyWay.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthCustom implements AuthenticationSuccessHandler {
	
	@Autowired
	private UsuarioService usService;

	private final Logger LOGGER = LoggerFactory.getLogger(AuthCustom.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // Obtener el usuario autenticado

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            //userDetails.getAuthorities().forEach(arg0 -> System.out.println(arg0.getAuthority()));;
            Usuario usuario=usService.buscarPorCorreo(userDetails.getUsername());

        	if( usuario instanceof Pasajero) {
        		LOGGER.info( "Es un pasajero{}",usuario);
        	}else if( usuario instanceof Piloto) {
        		LOGGER.info( "Es un piloto {}",usuario);
        	}

            request.getSession().setAttribute("usuario", usuario);

        }

    }


}
