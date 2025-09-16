package com.SkyWay.modules.usuario.application.serviceImpl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;





public class UsuarioDetails implements UserDetails{

	private Usuario usuario;
	
	

	
	public UsuarioDetails(Usuario usuario) {
		super();
		this.usuario = usuario;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		var roles= usuario.getRoles().stream()
				.map(rol -> new SimpleGrantedAuthority("ROLE_" +rol.getNombre().toUpperCase()))
				.toList();

		//Logger.logInfo(roles.toString());

		//SimpleGrantedAuthority authorities=new  SimpleGrantedAuthority("ROLE_" +usuario.getRol().getNombre().toUpperCase());


		//authorities.add();
		
		//return List.of(authorities);
		//Logger.logInfo("corregir setear roles all usuario");
		return roles;
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
