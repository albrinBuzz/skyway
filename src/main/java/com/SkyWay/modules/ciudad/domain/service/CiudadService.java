package com.SkyWay.modules.ciudad.domain.service;

import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.repository.CiudadRepository;
import com.SkyWay.modules.pai.domain.model.Pai;
import com.SkyWay.modules.pai.domain.service.PaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CiudadService {

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private PaiService paiService;

    // Métodos existentes
    public Ciudad saveCiudad(Ciudad ciudad) {
        return ciudadRepository.save(ciudad);
    }

    public List<Ciudad> getAllCiudades() {
        return ciudadRepository.findAll();
    }

    public Optional<Ciudad> getCiudadById(Integer id) {
        return ciudadRepository.findById(id);
    }

    public void deleteCiudad(Integer id) {
        ciudadRepository.deleteById(id);
    }

    public List<Ciudad> findCiudadesByNombre(String nombre) {
        return ciudadRepository.findByNombre(nombre);
    }

    // Métodos extendidos para desacoplar el ManagedBean
    @Transactional(readOnly = true)
    public List<Ciudad> obtenerCiudadesPorPais(Integer idPais) {
        if (idPais == null) return List.of();
        return ciudadRepository.findByPaiIdPaisOrderByNombreAsc(idPais);
    }

    @Transactional
    public Ciudad registrarCiudadAutoDetectada(Integer idPais, String nombreCiudad) {
        if (idPais == null || nombreCiudad == null || nombreCiudad.isBlank()) {
            throw new IllegalArgumentException("El país y la ciudad son obligatorios.");
        }
        String nombreLimpio = nombreCiudad.trim();
        return ciudadRepository.findByNombreIgnoreCaseAndPaiIdPais(nombreLimpio, idPais)
                .orElseGet(() -> guardarCiudadParaPais(idPais, nombreLimpio));
    }

    @Transactional
    public Ciudad guardarCiudadParaPais(Integer idPais, String nombreCiudad) {
        Pai pais = paiService.obtenerPorId(idPais);
        Ciudad ciudad = new Ciudad();
        ciudad.setNombre(nombreCiudad.trim());
        ciudad.setPai(pais);
        return ciudadRepository.save(ciudad);
    }

    @Transactional(readOnly = true)
    public Optional<Ciudad> buscarCiudadConPaisPorNombre(String nombreCiudad) {
        if (nombreCiudad == null || nombreCiudad.isBlank()) return Optional.empty();
        return ciudadRepository.buscarConPaisPorNombre(nombreCiudad.trim()).stream().findFirst();
    }
}