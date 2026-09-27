package com.SkyWay.modules.avion.application.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import com.SkyWay.modules.avion.domain.exception.AvionValidationException;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.repository.AvionRepository;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.capacidadclase.domain.repository.CapacidadClaseRepository;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvionServiceImpl implements AvionService {

	@Autowired
	private AvionRepository avionRepository;

	@Autowired
	private ModeloAvionRepository modeloAvionRepository;

	@Autowired
	private CapacidadClaseRepository capacidadClaseRepository;

	@Autowired
	private ClaseAsientoRepository claseAsientoRepository;

	@Autowired
	private DataSource dataSource;

	@PersistenceContext
	private EntityManager em;

	@Override
	@Transactional(readOnly = true)
	public List<Avion> findAll() {
		return avionRepository.findAllOptimized();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Avion> findById(Integer id) {
		return avionRepository.findById(id);
	}

	@Override
	@Transactional
	public Avion save(Avion vuelo) {
		return avionRepository.save(vuelo);
	}

	@Override
	@Transactional
	public boolean deleteById(Integer id) {
		try {
			if (avionRepository.existsById(id)) {
				avionRepository.deleteById(id);
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new AvionValidationException("No es posible eliminar la aeronave. Registra vuelos, itinerarios o tickets vigentes.");
		}
	}

	@Override
	@Transactional
	public Avion updateAvion(Avion avion) {
		return avionRepository.save(avion);
	}

	@Override
	@Transactional
	public Avion saveConCapacidades(Avion avion, Map<Integer, Integer> capacidadesPorClase) {
		boolean esEdicion = (avion.getIdAvion() != null);

		// 1. VALIDACIONES PREVIAS DE NEGOCIO
		if (avion.getNumeroDeRegistro() == null || avion.getNumeroDeRegistro().isBlank()) {
			throw new AvionValidationException("La matrícula de la aeronave es obligatoria.");
		}

		String matClean = avion.getNumeroDeRegistro().trim().toUpperCase();
		if (!matClean.matches("^[A-Z0-9-]{3,12}$")) {
			throw new AvionValidationException("Formato de matrícula inválido. Debe tener entre 3 y 12 caracteres.");
		}
		avion.setNumeroDeRegistro(matClean);

		int sumaCapacidad = capacidadesPorClase.values().stream()
				.filter(c -> c != null && c > 0)
				.mapToInt(Integer::intValue)
				.sum();

		if (sumaCapacidad <= 0) {
			throw new AvionValidationException("Debe asignar al menos 1 asiento en cualquiera de las clases.");
		}

		if (sumaCapacidad > 850) {
			throw new AvionValidationException("La capacidad máxima total de pasajeros (" + sumaCapacidad + ") supera el estándar aeronáutico permitidos (850 pax).");
		}

		avion.setCapacidadDePasajeros(sumaCapacidad);

		// 2. GUARDAR AEROANVE EN BD
		Avion guardado = avionRepository.save(avion);

		// 3. LIMPIAR CAPACIDADES EN CASO DE EDICIÓN
		if (esEdicion) {
			capacidadClaseRepository.deleteByAvion1_IdAvion(guardado.getIdAvion());
		}

		// 4. INSERTAR EN CAPACIDAD_CLASE (ESTO ACTIVA EL TRIGGER BD DE ASIENTOS)
		capacidadesPorClase.forEach((idClase, cantidad) -> {
			if (cantidad != null && cantidad > 0) {
				ClaseAsiento clase = claseAsientoRepository.findById(idClase).orElse(null);
				if (clase != null) {
					CapacidadClase cc = new CapacidadClase();
					cc.setAvion1(guardado);
					cc.setClaseAsiento1(clase);
					cc.setCantidad(cantidad);
					capacidadClaseRepository.save(cc);
				}
			}
		});

		return guardado;
	}
}