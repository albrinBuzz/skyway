package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.ClaseAsiento;
import com.SkyWay.repository.ClaseAsientoRepository;
import com.SkyWay.service.ClaseAsientoService;

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
