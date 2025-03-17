package com.SkyWay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SkyWay.model.Usuario;



public interface UsuarioRepository extends JpaRepository<Usuario, String>{

	Usuario findByCorreoElectronico(String correoElectronico);
	
}
