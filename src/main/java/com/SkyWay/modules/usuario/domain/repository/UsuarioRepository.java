package com.SkyWay.modules.usuario.domain.repository;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;





public interface UsuarioRepository extends JpaRepository<Usuario, String>{

	Usuario findByCorreoElectronico(String correoElectronico);
	
}
