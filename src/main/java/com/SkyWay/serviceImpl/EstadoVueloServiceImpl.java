package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.EstadoVuelo;
import com.SkyWay.repository.EstadoVueloRepository;
import com.SkyWay.service.EstadoVueloService;

@Service
public class EstadoVueloServiceImpl implements EstadoVueloService {

    private final EstadoVueloRepository estadoVueloRepository;

    @Autowired
    public EstadoVueloServiceImpl(EstadoVueloRepository estadoVueloRepository) {
        this.estadoVueloRepository = estadoVueloRepository;
    }

    @Override
    public EstadoVuelo save(EstadoVuelo estadoVuelo) {
        return estadoVueloRepository.save(estadoVuelo);
    }

    @Override
    public List<EstadoVuelo> findAll() {
        return estadoVueloRepository.findAll();
    }

    @Override
    public Optional<EstadoVuelo> findById(Integer id) {
        return estadoVueloRepository.findById(id);
    }

    @Override
    public void deleteById(Integer id) {
        estadoVueloRepository.deleteById(id);
    }
}
