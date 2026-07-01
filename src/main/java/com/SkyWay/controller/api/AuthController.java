package com.SkyWay.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	  @Autowired
	    private AuthenticationManager authenticationManager;
	  
	  
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UserLoginRequest loginRequest) {
        // Verifica las credenciales del usuario (esto puede ser consultando una base de datos, por ejemplo)
    	
        try {
            // Crear un token de autenticación con las credenciales del usuario
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword());

            // Autenticar el token con el AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            if (authentication.isAuthenticated()) {
                return ResponseEntity.ok("Login exitoso");
            }
            else {
            	    return ResponseEntity.status(401).body("Credenciales incorrectas");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body("Credenciales incorrectas");
        }
    
        
    }

    public static class UserLoginRequest {
        private String username;
        private String password;
        
        public String getPassword() {
			return password;
		}
        public String getUsername() {
			return username;
		}

        // Getters y setters
    }
}
