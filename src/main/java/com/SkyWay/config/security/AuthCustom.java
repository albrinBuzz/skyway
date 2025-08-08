package com.SkyWay.config.security;

import java.io.IOException;

import com.SkyWay.model.Pasajero;
import com.SkyWay.model.Piloto;

import com.SkyWay.util.Logger;
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


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // Obtener el usuario autenticado
        Logger.logInfo("En el authSuccess");
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {

            //userDetails.getAuthorities().forEach(arg0 -> System.out.println(arg0.getAuthority()));;
            Usuario usuario=usService.buscarPorCorreo(userDetails.getUsername());

        	if( usuario instanceof Pasajero) {

              Logger.logInfo( "Es un pasajero " +usuario);
        	}else if( usuario instanceof Piloto) {
                Logger.logInfo( "Es un Piloto " +usuario);
        	}

            request.getSession().setAttribute("usuario", usuario);

        }else {
            Logger.logInfo("No tiene UserDetails");
        }

    }


}
