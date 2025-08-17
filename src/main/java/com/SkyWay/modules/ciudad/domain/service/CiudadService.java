package com.SkyWay.modules.ciudad.domain.service;



import com.SkyWay.modules.ciudad.domain.repository.CiudadRepository;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CiudadService {

    @Autowired
    private CiudadRepository ciudadRepository;

    // Crear o actualizar una ciudad
    public Ciudad saveCiudad(Ciudad ciudad) {
        return ciudadRepository.save(ciudad);
    }

    // Obtener todas las ciudades
    public List<Ciudad> getAllCiudades() {
        return ciudadRepository.findAll();
    }

    // Obtener ciudad por ID
    public Optional<Ciudad> getCiudadById(Integer id) {
        return ciudadRepository.findById(id);
    }

    // Eliminar una ciudad
    public void deleteCiudad(Integer id) {
        ciudadRepository.deleteById(id);
    }

    // Buscar ciudad por nombre
    public List<Ciudad> findCiudadesByNombre(String nombre) {
        return ciudadRepository.findByNombre(nombre);
    }
}
