package com.SkyWay.modules.metodopago.domain.service;

import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.metodopago.domain.repository.MetodoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MetodoPagoServiceImpl implements MetodoPagoService {

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Override
    @Transactional
    public MetodoPago save(MetodoPago metodoPago) {
        return metodoPagoRepository.save(metodoPago);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetodoPago> findById(Integer idMetodoPago) {
        return metodoPagoRepository.findById(idMetodoPago);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MetodoPago> findByDescripcion(String descripcion) {
        return metodoPagoRepository.findByDescripcionIgnoreCase(descripcion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetodoPago> findAll() {
        return metodoPagoRepository.findAll();
    }
}