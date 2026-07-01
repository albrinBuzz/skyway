package com.SkyWay.modules.puertaembarque.domain.service;

import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import com.SkyWay.modules.puertaembarque.domain.repository.PuertaEmbarqueRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PuertaEmbarqueService {

    @Autowired
    private PuertaEmbarqueRepository puertaRepo;

    public List<PuertaEmbarque> findAll() {
        return puertaRepo.findAll();
    }

    public Optional<PuertaEmbarque> findById(Integer id) {
        return puertaRepo.findById(id);
    }

    public PuertaEmbarque create(PuertaEmbarque puerta) {
        return puertaRepo.save(puerta);
    }

    public PuertaEmbarque update(Integer id, PuertaEmbarque updatedPuerta) {
        return puertaRepo.findById(id)
                .map(p -> {
                    p.setCodigoPuerta(updatedPuerta.getCodigoPuerta());
                    p.setTerminal(updatedPuerta.getTerminal());
                    p.setAeropuerto(updatedPuerta.getAeropuerto());
                    return puertaRepo.save(p);
                })
                .orElseThrow(() -> new EntityNotFoundException("PuertaEmbarque no encontrada con ID: " + id));
    }

    public void delete(Integer id) {
        if (!puertaRepo.existsById(id)) {
            throw new EntityNotFoundException("PuertaEmbarque no encontrada con ID: " + id);
        }
        puertaRepo.deleteById(id);
    }

    public List<PuertaEmbarque> findByAeropuerto(Integer idAeropuerto) {
        return puertaRepo.findByAeropuertoIdAeropuerto(idAeropuerto);
    }

}
