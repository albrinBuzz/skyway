package com.SkyWay.modules.segmentovuelo.application.serviceImpl;




import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.repository.SegmentoVueloRepository;
import com.SkyWay.modules.segmentovuelo.domain.service.SegmentoVueloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SegmentoVueloServiceImpl implements SegmentoVueloService {

    private final SegmentoVueloRepository segmentoVueloRepository;

    @Autowired
    public SegmentoVueloServiceImpl(SegmentoVueloRepository segmentoVueloRepository) {
        this.segmentoVueloRepository = segmentoVueloRepository;
    }

    @Override
    public SegmentoVuelo save(SegmentoVuelo segmentoVuelo) {
        return segmentoVueloRepository.save(segmentoVuelo);
    }

    @Override
    public SegmentoVuelo update(SegmentoVuelo segmentoVuelo) {
        // Validar existencia antes de actualizar
        if (segmentoVuelo.getIdSegmento() == null || !segmentoVueloRepository.existsById(segmentoVuelo.getIdSegmento())) {
            throw new IllegalArgumentException("SegmentoVuelo not found or ID is null.");
        }
        return segmentoVueloRepository.save(segmentoVuelo);
    }

    @Override
    public void deleteById(Integer id) {
        if (!segmentoVueloRepository.existsById(id)) {
            throw new IllegalArgumentException("SegmentoVuelo with ID " + id + " does not exist.");
        }
        segmentoVueloRepository.deleteById(id);
    }

    @Override
    public Optional<SegmentoVuelo> findById(Integer id) {
        return segmentoVueloRepository.findById(id);
    }

    @Override
    public List<SegmentoVuelo> findAll() {
        return segmentoVueloRepository.findAll();
    }

    @Override
    public List<SegmentoVuelo> findByIdVuelo(Integer idVuelo) {
        return segmentoVueloRepository.buscarPorVuelo(idVuelo);
    }
}
