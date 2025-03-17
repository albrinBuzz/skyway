package com.SkyWay.repository;




import com.SkyWay.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    // Puedes agregar consultas personalizadas si es necesario, por ejemplo:
    // Rol findByNombre(String nombre);
}
