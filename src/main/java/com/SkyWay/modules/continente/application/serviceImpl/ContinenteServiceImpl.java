package com.SkyWay.modules.continente.application.serviceImpl;

import com.SkyWay.modules.continente.domain.model.Continente;
import com.SkyWay.modules.continente.domain.repository.ContinenteRepository;
import com.SkyWay.modules.continente.domain.service.ContinenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ContinenteServiceImpl implements ContinenteService {

    @Autowired
    private ContinenteRepository continenteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Continente> findAll() {
        return continenteRepository.findAllByOrderByNombreAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Continente> findById(Integer id) {
        if (id == null) return Optional.empty();
        return continenteRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Continente> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return Optional.empty();
        return continenteRepository.findByNombreIgnoreCase(nombre.trim());
    }

    @Override
    @Transactional
    public Continente guardar(Continente continente) {
        if (continente == null || continente.getNombre() == null || continente.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del continente es obligatorio.");
        }
        return continenteRepository.save(continente);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        if (id != null && continenteRepository.existsById(id)) {
            continenteRepository.deleteById(id);
        }
    }
}