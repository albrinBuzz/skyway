package com.SkyWay.modules.pai.domain.service;


import com.SkyWay.modules.pai.domain.model.Pai;

import com.SkyWay.modules.pai.domain.repository.PaisRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaiServiceImpl implements PaiService {

    @Autowired
    private PaisRepository paiRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Pai> findAllOrdenados() {
        return paiRepository.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional
    public Pai guardarPais(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del país no puede estar vacío.");
        }
        String nombreLimpio = nombre.trim();
        return buscarPorNombre(nombreLimpio)
                .orElseGet(() -> {
                    Pai p = new Pai();
                    p.setNombre(nombreLimpio);
                    return paiRepository.save(p);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pai> buscarPorNombre(String nombre) {
        return paiRepository.findByNombreIgnoreCase(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Pai obtenerPorId(Integer idPais) {
        return paiRepository.findById(idPais)
                .orElseThrow(() -> new IllegalArgumentException("El país con ID " + idPais + " no existe."));
    }
}