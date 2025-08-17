package com.SkyWay.modules.usuario.domain.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;



public interface UsuarioService extends UserDetailsService{

	public Usuario save(Usuario us);

	public Optional<Usuario>findByRut(String id);
	
	public void update(Usuario us);

	public void delete(Integer id);
	
	public List<Usuario>findAll();

	public void create(Usuario us);
	
	public Usuario findUserByEmail(String email);
	
	public Usuario findByNombre(String nombre);
	
	public Usuario buscarPorCorreo(String correo);
	
	
}
