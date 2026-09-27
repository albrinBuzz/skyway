package com.SkyWay.modules.usuario.domain.repository;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface UsuarioRepository extends JpaRepository<Usuario, String>{

	Usuario findByCorreoElectronico(String correoElectronico);

	@Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.roles")
    List<Usuario> findAllWithRoles();

	Optional<Usuario> findByCorreoElectronicoIgnoreCase(String correoElectronico);

	boolean existsByCorreoElectronicoIgnoreCaseAndRutNot(String correo, String rut);

}
