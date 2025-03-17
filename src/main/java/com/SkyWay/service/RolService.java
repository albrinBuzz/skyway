package com.SkyWay.service;



import com.SkyWay.model.Rol;

import java.util.List;
import java.util.Optional;

public interface RolService {

    // Método para obtener todos los roles
    List<Rol> getAllRoles();

    // Método para obtener un rol por su ID
    Optional<Rol> getRoleById(Integer id);

    // Método para crear o actualizar un rol
    Rol saveRole(Rol rol);

    // Método para eliminar un rol
    void deleteRole(Integer id);

    // Puedes agregar otros métodos si es necesario, como buscar por nombre o descripción
    // Rol findByNombre(String nombre);
}