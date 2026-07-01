package com.SkyWay.modules.aeropuerto.application.serviceImpl;
import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class AeropuertoServiceImpl implements AeropuertoService {
	@Autowired
    private AeropuertoRepository aeropuertoRepository;


    @Override
    public List<Aeropuerto> findAll() {
        return aeropuertoRepository.findAll();
    }

    @Override
    public Optional<Aeropuerto> findById(Integer id) {
        return aeropuertoRepository.findById(id);
    }

    @Override
    public Aeropuerto save(Aeropuerto aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    @Override
    public void delete(Integer id) {
        aeropuertoRepository.deleteById(id);
    }

    @Override
    public Aeropuerto update(Aeropuerto aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }
}
