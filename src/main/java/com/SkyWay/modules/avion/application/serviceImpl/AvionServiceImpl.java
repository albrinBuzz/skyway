package com.SkyWay.modules.avion.application.serviceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import com.SkyWay.modules.avion.domain.exception.AvionValidationException;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.repository.AvionRepository;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.avion.presentation.dto.FlotaResumenDTO;
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
	public List<Avion> findAllAdmin() {
		return avionRepository.findAllAdmin();
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
				avionRepository.softDeleteById(id);
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new AvionValidationException("No es posible eliminar la aeronave. Registra vuelos, itinerarios o tickets vigentes.");
		}
	}

	@Override
	@Transactional
	public void reactivar(Integer id) {
		avionRepository.reactivarById(id);
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
		Optional<Avion> existente = avionRepository.findByNumeroDeRegistro(matClean);
		if (existente.isPresent() && (!esEdicion || !Objects.equals(existente.get().getIdAvion(), avion.getIdAvion()))) {
			throw new AvionValidationException("Ya existe una aeronave registrada con la matrícula " + matClean + ".");
		}
		avion.setNumeroDeRegistro(matClean);

		if (avion.getModeloAvion() == null) {
			throw new AvionValidationException("Debe seleccionar un modelo de avión.");
		}

		int anoActual = LocalDate.now().getYear();
		if (avion.getAnoDeFabricacion() == null) {
			throw new AvionValidationException("El año de fabricación es obligatorio.");
		}
		if (avion.getAnoDeFabricacion() < 1950 || avion.getAnoDeFabricacion() > anoActual) {
			throw new AvionValidationException("El año de fabricación no puede ser menor a 1950 ni mayor al año actual (" + anoActual + ").");
		}

		if (avion.getCapacidadDeCarga() == null || avion.getCapacidadDeCarga() <= 0) {
			throw new AvionValidationException("La capacidad de carga debe ser mayor a 0 Kg.");
		}

		Map<Integer, Integer> capacidadesNormalizadas = normalizarCapacidades(capacidadesPorClase);
		int sumaCapacidad = capacidadesNormalizadas.values().stream()
				.filter(c -> c != null && c > 0)
				.mapToInt(Integer::intValue)
				.sum();

		if (sumaCapacidad <= 0) {
			throw new AvionValidationException("Debe asignar al menos 1 asiento en cualquiera de las clases.");
		}

		if (sumaCapacidad > 850) {
			throw new AvionValidationException("La capacidad máxima total de pasajeros (" + sumaCapacidad + ") supera el estándar aeronáutico permitido (850 pax).");
		}

		avion.setCapacidadDePasajeros(sumaCapacidad);

		boolean regenerarAsientos = true;
		if (esEdicion) {
			Integer idModeloAnterior = obtenerIdModeloActual(avion.getIdAvion());
			Integer idModeloNuevo = avion.getModeloAvion().getIdModelo();
			boolean modeloCambio = !Objects.equals(idModeloAnterior, idModeloNuevo);
			boolean capacidadesCambiaron = !capacidadesPositivas(capacidadesNormalizadas)
					.equals(obtenerCapacidadesActuales(avion.getIdAvion()));
			boolean tieneAsientos = contarAsientosAvion(avion.getIdAvion()) > 0;

			regenerarAsientos = modeloCambio || capacidadesCambiaron || !tieneAsientos;
			if (regenerarAsientos && tieneAsientosEnUso(avion.getIdAvion())) {
				throw new AvionValidationException("No es posible modificar el modelo o la cantidad de asientos porque esta aeronave ya tiene asientos registrados en reservas o historial.");
			}
		}

		// 2. GUARDAR AERONAVE EN BD
		Avion guardado = avionRepository.save(avion);
		em.flush();

		if (!regenerarAsientos) {
			return guardado;
		}

		// 3. LIMPIAR ASIENTOS Y CAPACIDADES ANTERIORES EN CASO DE EDICIÓN
		if (esEdicion) {
			em.createNativeQuery("DELETE FROM asiento WHERE id_avion = :idAvion")
					.setParameter("idAvion", guardado.getIdAvion())
					.executeUpdate();
			em.createNativeQuery("DELETE FROM capacidad_clase WHERE id_avion = :idAvion")
					.setParameter("idAvion", guardado.getIdAvion())
					.executeUpdate();
			em.flush();
		}

		// 4. INSERTAR CAPACIDADES POR CLASE (GENERA ASIENTOS AUTOMÁTICAMENTE)
		capacidadesNormalizadas.forEach((idClase, cantidad) -> {
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

	private Map<Integer, Integer> capacidadesPositivas(Map<Integer, Integer> origen) {
		Map<Integer, Integer> resultado = new LinkedHashMap<>();
		origen.forEach((idClase, cantidad) -> {
			if (idClase != null && cantidad != null && cantidad > 0) {
				resultado.put(idClase, cantidad);
			}
		});
		return resultado;
	}

	private Map<Integer, Integer> obtenerCapacidadesActuales(Integer idAvion) {
		Map<Integer, Integer> actuales = new LinkedHashMap<>();
		List<?> rows = em.createNativeQuery("SELECT id_clase, cantidad FROM capacidad_clase WHERE id_avion = :idAvion")
				.setParameter("idAvion", idAvion)
				.getResultList();
		for (Object obj : rows) {
			Object[] row = (Object[]) obj;
			Integer idClase = row[0] != null ? ((Number) row[0]).intValue() : null;
			Integer cantidad = row[1] != null ? ((Number) row[1]).intValue() : null;
			if (idClase != null && cantidad != null && cantidad > 0) {
				actuales.put(idClase, cantidad);
			}
		}
		return actuales;
	}

	private Integer obtenerIdModeloActual(Integer idAvion) {
		List<?> res = em.createNativeQuery("SELECT id_modelo FROM avion WHERE id_avion = :idAvion")
				.setParameter("idAvion", idAvion)
				.getResultList();
		if (res.isEmpty() || res.get(0) == null) {
			return null;
		}
		return ((Number) res.get(0)).intValue();
	}

	private long contarAsientosAvion(Integer idAvion) {
		Object res = em.createNativeQuery("SELECT COUNT(*) FROM asiento WHERE id_avion = :idAvion")
				.setParameter("idAvion", idAvion)
				.getSingleResult();
		return res != null ? ((Number) res).longValue() : 0L;
	}

	private boolean tieneAsientosEnUso(Integer idAvion) {
		Object res = em.createNativeQuery(
				"SELECT (EXISTS (SELECT 1 FROM reserva_asiento ra JOIN asiento a ON a.id_asiento = ra.id_asiento WHERE a.id_avion = :idAvion) "
				+ "OR EXISTS (SELECT 1 FROM cancelacion_asiento ca JOIN asiento a ON a.id_asiento = ca.id_asiento WHERE a.id_avion = :idAvion))"
		).setParameter("idAvion", idAvion).getSingleResult();
		return Boolean.TRUE.equals(res);
	}

	private Map<Integer, Integer> normalizarCapacidades(Map<?, ?> origen) {
		Map<Integer, Integer> resultado = new LinkedHashMap<>();
		if (origen == null) {
			return resultado;
		}
		for (Map.Entry<?, ?> entry : origen.entrySet()) {
			Integer idClase = parseEnteroSeguro(entry.getKey());
			Integer cantidad = parseEnteroSeguro(entry.getValue());
			if (idClase != null && cantidad != null) {
				resultado.put(idClase, cantidad);
			}
		}
		return resultado;
	}

	private Integer parseEnteroSeguro(Object valor) {
		if (valor == null) {
			return null;
		}
		if (valor instanceof Number num) {
			return num.intValue();
		}
		String texto = valor.toString().trim();
		if (texto.isEmpty()) {
			return 0;
		}
		try {
			return Integer.valueOf(texto);
		} catch (NumberFormatException ex) {
			throw new AvionValidationException("La cantidad de asientos debe ser un número entero válido.");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public long contarAvionesActivos() {
		return avionRepository.countByActivoTrue();
	}

	@Override
	@Transactional(readOnly = true)
	public List<FlotaResumenDTO> obtenerResumenFlotaPortal() {
		List<Object[]> rows = avionRepository.findResumenFlotaAgrupado();
		List<FlotaResumenDTO> lista = new ArrayList<>(rows.size());
		for (Object[] r : rows) {
			lista.add(new FlotaResumenDTO(
					r[0] != null ? r[0].toString() : "",
					r[1] != null ? r[1].toString() : "",
					r[2] != null ? ((Number) r[2]).longValue() : 0L,
					r[3] != null ? ((Number) r[3]).intValue() : 0,
					r[4] != null ? ((Number) r[4]).intValue() : 0,
					r[5] != null ? ((Number) r[5]).intValue() : 0
			));
		}
		return lista;
	}
}