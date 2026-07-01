package com.SkyWay.modules.precioasiento.application.serviceImpl;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.SkyWay.modules.precioasiento.domain.repository.PrecioAsientoRepository;
import com.SkyWay.modules.precioasiento.domain.service.PrecioAsientoService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    @Override
    @Transactional(readOnly = true)
    public Optional<PrecioAsiento> findByVueloAndClase(Integer idVuelo, Integer idClase) {
        return precioAsientoRepository.findByVueloAndClase(idVuelo, idClase);
    }
}
