package com.SkyWay.modules.segmentovuelo.application.serviceImpl;




import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.repository.SegmentoVueloRepository;
import com.SkyWay.modules.segmentovuelo.domain.service.SegmentoVueloService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SegmentoVueloServiceImpl implements SegmentoVueloService {

    private final SegmentoVueloRepository segmentoVueloRepository;
    @Autowired
    private EntityManager entityManager;

    @Autowired
    public SegmentoVueloServiceImpl(SegmentoVueloRepository segmentoVueloRepository) {
        this.segmentoVueloRepository = segmentoVueloRepository;
    }

    @Override
    @Transactional
    public SegmentoVuelo save(SegmentoVuelo segmentoVuelo) {
        segmentoVuelo = segmentoVueloRepository.save(segmentoVuelo);

        // Refrescar usando native query para forzar recarga desde DB

        return (SegmentoVuelo) entityManager
                .createNativeQuery("SELECT * FROM Segmento_Vuelo WHERE id_segmento = ?", SegmentoVuelo.class)
                .setParameter(1, segmentoVuelo.getIdSegmento())
                .getSingleResult();
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
