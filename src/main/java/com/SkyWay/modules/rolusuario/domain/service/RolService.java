package com.SkyWay.modules.rolusuario.domain.service;





import com.SkyWay.modules.rolusuario.domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RolService {

    // Método para obtener todos los roles
    List<Role> getAllRoles();

    // Método para obtener un rol por su ID
    Optional<Role> getRoleById(Integer id);

    // Método para crear o actualizar un rol
    Role saveRole(Role rol);

    // Método para eliminar un rol
    void deleteRole(Integer id);

    // Puedes agregar otros métodos si es necesario, como buscar por nombre o descripción
    // Rol findByNombre(String nombre);
}