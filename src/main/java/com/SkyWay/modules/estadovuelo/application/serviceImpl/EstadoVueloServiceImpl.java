package com.SkyWay.modules.estadovuelo.application.serviceImpl;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.estadovuelo.domain.repository.EstadoVueloRepository;
import com.SkyWay.modules.estadovuelo.domain.service.EstadoVueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



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
