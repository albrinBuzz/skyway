package com.SkyWay.modules.asignacionpuerta.application.serviceImpl;



import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.asignacionpuerta.domain.repository.AsignacionPuertaRepository;
import com.SkyWay.modules.asignacionpuerta.domain.service.AsignacionPuertaService;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsignacionPuertaServiceImpl implements AsignacionPuertaService {

    private final AsignacionPuertaRepository asignacionPuertaRepository;

    @Autowired
    public AsignacionPuertaServiceImpl(AsignacionPuertaRepository asignacionPuertaRepository) {
        this.asignacionPuertaRepository = asignacionPuertaRepository;
    }

    @Override
    public AsignacionPuerta save(AsignacionPuerta asignacionPuerta) {
        return asignacionPuertaRepository.save(asignacionPuerta);
    }

    @Override
    public AsignacionPuerta update(AsignacionPuerta asignacionPuerta) {
        if (asignacionPuerta.getIdAsignacion() == null || !asignacionPuertaRepository.existsById(asignacionPuerta.getIdAsignacion())) {
            throw new IllegalArgumentException("Asignación no encontrada o ID nulo.");
        }
        return asignacionPuertaRepository.save(asignacionPuerta);
    }

    @Override
    public void deleteById(Integer id) {
        if (!asignacionPuertaRepository.existsById(id)) {
            throw new IllegalArgumentException("Asignación con ID " + id + " no existe.");
        }
        asignacionPuertaRepository.deleteById(id);
    }

    @Override
    public Optional<AsignacionPuerta> findById(Integer id) {
        return asignacionPuertaRepository.findById(id);
    }

    @Override
    public List<AsignacionPuerta> findAll() {
        return asignacionPuertaRepository.findAll();
    }

    @Override
    public List<AsignacionPuerta> findBySegmentoVuelo(SegmentoVuelo segmentoVuelo) {
        return asignacionPuertaRepository.findBySegmentoVuelo(segmentoVuelo);
    }

}
