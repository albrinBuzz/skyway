package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.PrecioAsiento;
import com.SkyWay.model.Vuelo;
import com.SkyWay.repository.PrecioAsientoRepository;
import com.SkyWay.service.PrecioAsientoService;

@Service
public class PrecioAsientoServiceImpl implements PrecioAsientoService {

    private final PrecioAsientoRepository precioAsientoRepository;

    @Autowired
    public PrecioAsientoServiceImpl(PrecioAsientoRepository precioAsientoRepository) {
        this.precioAsientoRepository = precioAsientoRepository;
    }

    @Override
    public PrecioAsiento guardarPrecioAsiento(PrecioAsiento precioAsiento) {
        return precioAsientoRepository.save(precioAsiento);
    }

    @Override
    public Optional<PrecioAsiento> obtenerPrecioAsientoPorId(Integer id) {
        return precioAsientoRepository.findById(id);
    }

    @Override
    public List<PrecioAsiento> obtenerTodosLosPreciosAsiento() {
        return precioAsientoRepository.findAll();
    }

    @Override
    public PrecioAsiento actualizarPrecioAsiento(Integer id, PrecioAsiento precioAsiento) {
        if (precioAsientoRepository.existsById(id)) {
            precioAsiento.setIdPrecioAsiento(id);
            return precioAsientoRepository.save(precioAsiento);
        }
        return null; // O lanzar una excepción si no se encuentra el ID
    }

    @Override
    public void eliminarPrecioAsiento(Integer id) {
        if (precioAsientoRepository.existsById(id)) {
            precioAsientoRepository.deleteById(id);
        } else {
            // Lógica para manejar el caso de que no exista el ID, si es necesario.
        }
    }

	@Override
	public List<PrecioAsiento> findByVuelo(Vuelo vuelo) {
		// TODO Auto-generated method stub
		return precioAsientoRepository.findByVuelo(vuelo);
	}
}
