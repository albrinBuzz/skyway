package com.SkyWay.modules.aerolinea.application.serviceImpl;


import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import com.SkyWay.modules.aerolinea.domain.repository.AerolineaRepository;
import com.SkyWay.modules.aerolinea.domain.service.AerolineaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AerolineaServiceImpl implements AerolineaService {

    @Autowired
    private AerolineaRepository repository;

    @Override
    public List<Aerolinea> findAll() {
        return repository.findAll();
    }

    @Override
    public Aerolinea findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Aerolinea save(Aerolinea aerolinea) {
        return repository.save(aerolinea);
    }

    @Override
    public Aerolinea update(Aerolinea aerolinea) {
        if (aerolinea.getIdAerolinea() != null && repository.existsById(aerolinea.getIdAerolinea())) {
            return repository.save(aerolinea);
        }
        return null; // o lanzar excepción
    }

    @Override
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}
