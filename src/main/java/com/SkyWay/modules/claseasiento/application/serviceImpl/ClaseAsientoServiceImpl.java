package com.SkyWay.modules.claseasiento.application.serviceImpl;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;
import com.SkyWay.modules.claseasiento.domain.service.ClaseAsientoService;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class ClaseAsientoServiceImpl implements ClaseAsientoService {

    private final ClaseAsientoRepository claseAsientoRepository;

    @Autowired
    public ClaseAsientoServiceImpl(ClaseAsientoRepository claseAsientoRepository) {
        this.claseAsientoRepository = claseAsientoRepository;
    }

    @Override
    public ClaseAsiento guardarClaseAsiento(ClaseAsiento claseAsiento) {
        return claseAsientoRepository.save(claseAsiento);
    }

    @Override
    public Optional<ClaseAsiento> obtenerClaseAsientoPorId(Integer id) {
        return claseAsientoRepository.findById(id);
    }

    @Override
    public List<ClaseAsiento> obtenerTodasLasClasesAsiento() {
        return claseAsientoRepository.findAll();
    }

    @Override
    public ClaseAsiento actualizarClaseAsiento(Integer id, ClaseAsiento claseAsiento) {
        if (claseAsientoRepository.existsById(id)) {
            claseAsiento.setIdClase(id);
            return claseAsientoRepository.save(claseAsiento);
        }
        return null; // O lanzar una excepción si el id no existe.
    }

    @Override
    public void eliminarClaseAsiento(Integer id) {
        if (claseAsientoRepository.existsById(id)) {
            claseAsientoRepository.deleteById(id);
        } else {
            // Lógica para manejar el caso de que no exista el ID.
        }
    }
}
