package com.SkyWay.serviceImpl;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.SkyWay.model.Usuario;



public class UsuarioDetails implements UserDetails{

	private Usuario usuario;
	
	
	private final Logger LOGGER = LoggerFactory.getLogger(UsuarioDetails.class);
	
	
	public UsuarioDetails(Usuario usuario) {
		super();
		this.usuario = usuario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		SimpleGrantedAuthority authorities=new  SimpleGrantedAuthority("ROLE_" +usuario.getRol().getNombre().toUpperCase());

		LOGGER.info("Rol {} ",authorities);
		//authorities.add();
		
		return List.of(authorities);
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return usuario.getContrasena();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return usuario.getCorreoElectronico();
	}

}
