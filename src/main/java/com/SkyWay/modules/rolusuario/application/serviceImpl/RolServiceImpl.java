package com.SkyWay.modules.rolusuario.application.serviceImpl;



import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.rolusuario.domain.repository.RolRepository;

import com.SkyWay.modules.rolusuario.domain.service.RolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;

    @Autowired
    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public List<Role> getAllRoles() {
        return rolRepository.findAll();
    }

    @Override
    public Optional<Role> getRoleById(Integer id) {
        return rolRepository.findById(id);
    }

    @Override
    public Role saveRole(Role rol) {
        return rolRepository.save(rol);
    }

    @Override
    public void deleteRole(Integer id) {
        rolRepository.deleteById(id);
    }

    // Puedes agregar otros métodos si es necesario
    // @Override
    // public Rol findByNombre(String nombre) {
    //     return rolRepository.findByNombre(nombre);
    // }
}