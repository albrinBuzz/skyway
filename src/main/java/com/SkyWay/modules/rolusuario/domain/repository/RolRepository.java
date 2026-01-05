package com.SkyWay.modules.rolusuario.domain.repository;






import com.SkyWay.modules.rolusuario.domain.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Role, Integer> {
    // Puedes agregar consultas personalizadas si es necesario, por ejemplo:
     Role findByNombre(String nombre);
}
