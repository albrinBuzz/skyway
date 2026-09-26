package com.SkyWay.modules.avion.application.serviceImpl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.repository.AvionRepository;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.capacidadclase.domain.repository.CapacidadClaseRepository;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;

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
	private CapacidadClaseRepository capacidadClaseRepository;

	@Autowired
	private ClaseAsientoRepository claseAsientoRepository;

	@Autowired
	private DataSource dataSource;

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<Avion> findAll() {
		return avionRepository.findAllOptimized();
	}

	@Override
	public Optional<Avion> findById(Integer id) {
		return avionRepository.findById(id);
	}

	@Override
	public Avion save(Avion vuelo) {
		return avionRepository.save(vuelo);
	}

	@Override
	public boolean deleteById(Integer id) {
		if(avionRepository.existsById(id)) {
			avionRepository.deleteById(id);
			return true;
		}
		return false;
	}

	@Override
	public Avion updateAvion(Avion avion) {
		return avionRepository.save(avion);
	}

	@Override
	@Transactional
	public Avion saveConCapacidades(Avion avion, Map<Integer, Integer> capacidadesPorClase) {
		boolean esEdicion = (avion.getIdAvion() != null);

		// Calcular la capacidad total sumando los asientos asignados por clase
		int sumaCapacidad = capacidadesPorClase.values().stream()
				.filter(c -> c != null && c > 0)
				.mapToInt(Integer::intValue)
				.sum();

		avion.setCapacidadDePasajeros(sumaCapacidad);

		Avion guardado = avionRepository.save(avion);

		// Si estamos editando, limpiamos capacidades previas para insertar las nuevas
		if (esEdicion) {
			capacidadClaseRepository.deleteByAvion1_IdAvion(guardado.getIdAvion());
		}

		// Insertar en Capacidad_Clase -> ESTO DISPARA EL TRIGGER POSTGRESQL fn_insertarAsientos()
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