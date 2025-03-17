package com.SkyWay.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.SkyWay.model.Usuario;

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
