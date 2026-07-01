package com.SkyWay.modules.tipoturno.application.serviceImpl;


import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;
import com.SkyWay.modules.tipoturno.domain.repository.TipoTurnoRepository;

import com.SkyWay.modules.tipoturno.domain.service.TipoTurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TipoTurnoServiceImpl implements TipoTurnoService {

    @Autowired
    private TipoTurnoRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<TipoTurno> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TipoTurno> findById(Integer id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public TipoTurno save(TipoTurno tipoTurno) {
        return repository.save(tipoTurno);
    }

    @Override
    @Transactional
    public TipoTurno update(Integer id, TipoTurno tipoTurno) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setNombre(tipoTurno.getNombre());
                    // No solemos actualizar la lista de turnos aquí para evitar efectos colaterales
                    return repository.save(existing);
                }).orElseThrow(() -> new RuntimeException("TipoTurno no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public void deleteById(Integer id) {
        repository.deleteById(id);
    }
}