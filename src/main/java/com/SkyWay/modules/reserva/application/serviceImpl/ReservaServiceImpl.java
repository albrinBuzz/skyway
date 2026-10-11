package com.SkyWay.modules.reserva.application.serviceImpl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.util.Logger;
import com.SkyWay.util.PnrGenerator;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SkyWay.dto.BoletoDTO;
import com.SkyWay.dto.ReservaVueloDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Service
public class ReservaServiceImpl implements ReservaService {


	@Autowired
	private ReservaRepository reservaRepository;
	@PersistenceContext
	private EntityManager entityManager;

	@PersistenceContext
	private EntityManager em;

	@Override
	public List<Reserva> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Reserva> findById(Integer id) {
		// TODO Auto-generated method stub
		return reservaRepository.findById(id);
	}

	@Override
	@Transactional
	public Reserva save(Reserva reserva) {
		// TODO Auto-generated method stub
		return reservaRepository.save(reserva);
	}

	@Override
	public void deleteById(Integer id) {
		// TODO Auto-generated method stub
	}

	@Override
	@Transactional
	public List<ReservaVueloDTO> getReservasUsuario(String rut) {
		// Crear la llamada al procedimiento almacenado
		StoredProcedureQuery query = em.createStoredProcedureQuery("sp_vuelosPasajero");

		// Registrar los parámetros del procedimiento
		query.registerStoredProcedureParameter("p_rut_pasajero", String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("cursos_reservas", void.class, ParameterMode.REF_CURSOR);

		// Establecer los valores de los parámetros
		query.setParameter("p_rut_pasajero", rut);

		// Ejecutar el procedimiento
		query.execute();

		// Obtener el cursor (o el resultado)
		List<Object[]> resultList = query.getResultList();

		// Mapeo a objetos DTO
		List<ReservaVueloDTO> reservas = new ArrayList<>();
		for (Object[] row : resultList) {
			ReservaVueloDTO reserva = new ReservaVueloDTO();
			// Mapeo de cada columna a la clase DTO
			reserva.setIdVuelo((Integer) row[0]);
			reserva.setId_reserva((Integer) row[1]);
			reserva.setNumeroVuelo((String) row[2]);
			reserva.setFechaHoraSalida((Timestamp) row[3]);
			reserva.setFechaHoraLlegada((Timestamp) row[4]);
			reserva.setAeropuertoSalida((String) row[5]);
			reserva.setAeropuertoLlegada((String) row[6]);
			reserva.setFechaReserva((Timestamp) row[7]);
			reserva.setEstadoReserva((String) row[8]);
			reservas.add(reserva);
		}

		return reservas;
	}

	@Override
	@Transactional
	public void cancelarReserva(Integer id) {
		// Llamar al procedimiento almacenado
		StoredProcedureQuery query = em.createStoredProcedureQuery("sp_cancelar_reserva");

		// Registrar los parámetros del procedimiento
		query.registerStoredProcedureParameter("p_id_reserva", Integer.class, ParameterMode.IN);

		// Establecer los valores de los parámetros
		query.setParameter("p_id_reserva", id);

		// Ejecutar el procedimiento
		try {
			query.execute();
			Logger.logInfo("Reserva cancelada con éxito");
		} catch (Exception e) {
			Logger.logInfo("Error al cancelar la reserva: " + e.getMessage());
			throw new RuntimeException("Error al cancelar la reserva", e);
		}
	}

	public List<Reserva> obtenerReservasPorPasajero(Pasajero pasajero) {
		return reservaRepository.findByPasajero(pasajero);
	}

	public List<Reserva> obtenerReservasPorRut(String rut) {
		return reservaRepository.findByPasajero_Rut(rut);
	}

	public Page<Reserva> obtenerReservasPorRut(String rut, Pageable pageable) {
		return reservaRepository.findByPasajero_Rut(rut, pageable);
	}

	@Override
	@Transactional
	public BoletoDTO getBoleto(Integer id) {
		// Llamar al procedimiento almacenado
		StoredProcedureQuery query = em.createStoredProcedureQuery("sp_getBoletoReserva");

		// Registrar los parámetros del procedimiento almacenado
		query.registerStoredProcedureParameter("id_reserva_input", Integer.class, ParameterMode.IN);
		query.registerStoredProcedureParameter("resultado_cursor", void.class, ParameterMode.REF_CURSOR);

		// Establecer los valores de los parámetros
		query.setParameter("id_reserva_input", id);

		BoletoDTO boleto = new BoletoDTO();

		try {
			query.execute();

			// Obtener el resultado (cursor)
			List<Object[]> resultList = query.getResultList();

			// Mapeo de cada fila del resultado al DTO correspondiente
			for (Object[] row : resultList) {
				boleto.setNombreCompleto((String) row[0]);  // nombre_completo
				boleto.setDocumentoIdentidad((String) row[1]); // documento_identidad
				boleto.setCorreoElectronico((String) row[2]);  // correo_electronico
				boleto.setNumeroVuelo((String) row[3]);  // numero_vuelo
				boleto.setFechaSalidaCompleta((String) row[4]);  // fecha_salida_completa
				boleto.setHoraSalida((String) row[5]);  // hora_salida
				boleto.setAeropuertoSalida((String) row[6]);  // aeropuerto_salida
				boleto.setAeropuertoLlegada((String) row[7]);  // aeropuerto_llegada

				// Para la duración, asumimos que se devuelve un tipo de datos de tiempo (Intervalo o equivalente)
				BigDecimal duracionVuelo = (BigDecimal) row[8];  // duracion_vuelo, si es necesario convertir
				boleto.setDuracionVuelo(duracionVuelo);

				// Precio total del vuelo
				Long precioTotal = (Long) row[9];  // precio_total
				boleto.setPrecioTotal(precioTotal);
			}

			Logger.logInfo("Boleto generado con éxito");
		} catch (Exception e) {
			Logger.logInfo("Error al generar el boleto: " + e.getMessage());
			throw new RuntimeException("Error al generar el boleto", e);
		}

		// Retornar el DTO con los datos mapeados
		return boleto;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<TicketInfo> getTicket(String rut, Integer idReserva, int idItinerario) {
		Query query = em.createNativeQuery(
				"SELECT * FROM fn_getticket(:p_rut, :p_reserva, :p_idItinerario);",
				TicketInfo.class);

		query.setParameter("p_rut", rut);
		query.setParameter("p_reserva", idReserva);
		query.setParameter("p_idItinerario", idItinerario);

		return (List<TicketInfo>) query.getResultList();
	}

	@Override
	@Transactional
	public String confirmarReserva(int idVuelo, Integer[] asientos, String rutPasajero, Integer idReserva) throws SQLException {
		//String sql = "SELECT confirmar_reserva(:idVuelo, :asientos, :rutPasajero)";
		StoredProcedureQuery query = em.createStoredProcedureQuery("spConfirmar_reserva");

		Logger.logInfo("parametros: Vuelo-> "+idVuelo+" idReserva-> "+idReserva);

		try {

			query.registerStoredProcedureParameter("p_idVuelo", Integer.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_idReserva", Integer.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_asientos", Integer[].class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_rutPasajero", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_resultado", String.class, ParameterMode.OUT);

			query.setParameter("p_idVuelo", idVuelo);
			query.setParameter("p_idReserva", idReserva);
			query.setParameter("p_asientos", asientos);
			query.setParameter("p_rutPasajero", rutPasajero);

			query.execute();

			String resultado = (String) query.getOutputParameterValue("p_resultado");

			if (resultado.startsWith("ERROR:")) {
				// Forzar rollback lanzando excepción
				Logger.logInfo(resultado);
				throw new RuntimeException(resultado.substring(6).trim());
			}
			return resultado.substring(3).trim(); // Solo mensaje de éxito

		} catch (Exception ex) {
			Throwable causa = ex.getCause();
			if (causa instanceof SQLException sqlException) {
				// Extraer mensaje personalizado (RAISE EXCEPTION '...') de PostgreSQL
				String error = extractErrorMessage(sqlException); // tu método personalizado
				Logger.logInfo("Mensaje de Postgres: " + error);
				throw new SQLException(error); // Relanza la excepción con el mensaje real
			}

			// Si no fue SQLException, relanzar la original
			throw ex;
		}
	}

	private String logJDBCExceptionDetails(SQLException sqlException) {
		String error = "";

		// Capturamos los detalles de la SQLException subyacente, si existe
		if (sqlException != null) {
			// Procesamos la causa específica de la SQLException
			Throwable cause = sqlException.getCause();
			if (cause != null) {
				error = extractErrorMessage(cause);
			}
		}

		// Capturamos los detalles de la causa de la excepción JDBC, si existe
		Throwable cause = sqlException.getCause();
		if (cause != null) {
			error = extractErrorMessage(cause);
		}

		return error;
	}
	private String extractErrorMessage(Throwable cause) {
		// Extraemos el mensaje de error después del primer ":"
		String message = cause.getMessage();

		if (message != null && message.contains(":")) {
			String mensaje=message.substring(message.indexOf(":") + 1,
					message.lastIndexOf(":")).trim();
			int idx= mensaje.indexOf("Where");
			return mensaje.substring(0,idx).trim();
			//return mensaje.substring()


		}
		return "Error desconocido";
	}



	@Override
	@Transactional
	public void cancelarReserva(Integer idReserva, String rutPasajero, String motivo) {
		String ipUsuario = "127.0.0.1";
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc != null && fc.getExternalContext() != null) {
			HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
			if (request != null) {
				ipUsuario = request.getRemoteAddr();
			}
		}

		StoredProcedureQuery query = em.createStoredProcedureQuery("sp_cancelarReservaPasajero");
		query.registerStoredProcedureParameter(1, Integer.class, ParameterMode.IN);
		query.registerStoredProcedureParameter(2, String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter(3, String.class, ParameterMode.IN);
		query.registerStoredProcedureParameter(4, String.class, ParameterMode.IN);

		query.setParameter(1, idReserva);
		query.setParameter(2, rutPasajero);
		query.setParameter(3, motivo != null && !motivo.isBlank() ? motivo : "Cancelación voluntaria por el pasajero desde la web.");
		query.setParameter(4, ipUsuario);

		query.execute();
	}


	@Override
	@Transactional
	public void procesarCheckInWeb(Integer idReserva, Integer idItinerario) {
		if (idReserva == null) {
			throw new IllegalArgumentException("El ID de reserva no puede ser nulo.");
		}

		try {
			// Inserción en la tabla Checkin utilizando la secuencia checkin_seq y la tabla de PostgreSQL
			Query query = em.createNativeQuery(
					"INSERT INTO checkin (id_reserva, fecha_hora, metodo, estado, codigo_boarding_pass) " +
							"VALUES (:p_reserva, NOW(), 'Web', 'COMPLETADO', :p_code)"
			);

			String boardingPassCode = "BP-SW-" + idReserva + "-" + System.currentTimeMillis();

			query.setParameter("p_reserva", idReserva);
			query.setParameter("p_code", boardingPassCode);

			query.executeUpdate();
			Logger.logInfo("Check-In Web registrado con éxito para la reserva N°: " + idReserva);

		} catch (Exception e) {
			Logger.logError("Error al registrar Check-In Web: " + e.getMessage());
			throw new RuntimeException("Error al procesar el Check-In Web en la base de datos.", e);
		}
	}



	@Override
	@Transactional(readOnly = true)
	public Integer buscarIdPorCodigo(String codigoReserva) {
		if (codigoReserva == null || codigoReserva.isBlank()) {
			return null;
		}

		String codigoLimpio = codigoReserva.trim();

		// 1. PRIORIDAD ALTA: Búsqueda exacta por código PNR (ej: "SKW-000002")
		try {
			List<Integer> resultados = entityManager.createQuery(
							"SELECT r.idReserva FROM Reserva r WHERE UPPER(r.codigoReserva) = :codigo", Integer.class)
					.setParameter("codigo", codigoLimpio.toUpperCase())
					.setMaxResults(1)
					.getResultList();

			if (!resultados.isEmpty()) {
				return resultados.get(0);
			}
		} catch (Exception e) {
			Logger.logWarn("Error buscando por codigoReserva PNR: " + e.getMessage());
		}

		// 2. PRIORIDAD MEDIA: Si el usuario ingresó únicamente un ID numérico (ej: "2")
		try {
			Integer id = Integer.parseInt(codigoLimpio);
			Long count = entityManager.createQuery(
							"SELECT COUNT(r) FROM Reserva r WHERE r.idReserva = :id", Long.class)
					.setParameter("id", id)
					.getSingleResult();

			if (count != null && count > 0) {
				return id;
			}
		} catch (NumberFormatException ignored) {
			// El valor no es numérico entero, continuamos a la búsqueda por RUT
		}

		// 3. PRIORIDAD FALLBACK: Búsqueda por RUT del Pasajero asociado
		try {
			List<Integer> resultadosRut = entityManager.createQuery(
							"SELECT r.idReserva FROM Reserva r WHERE UPPER(r.pasajero.rut) = :rut ORDER BY r.fechaReserva DESC", Integer.class)
					.setParameter("rut", codigoLimpio.toUpperCase())
					.setMaxResults(1)
					.getResultList();

			if (!resultadosRut.isEmpty()) {
				return resultadosRut.get(0);
			}
		} catch (Exception e) {
			Logger.logError("Error buscando ID de reserva por RUT: " + e.getMessage());
		}

		Logger.logWarn("No se encontró ninguna reserva para el código/ID/RUT: " + codigoLimpio);
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Integer obtenerIdItinerarioPorReserva(Integer idReserva) {
		if (idReserva == null) {
			return null;
		}

		try {
			// Mapeo directo: ri.reserva.idReserva -> ri.itinerario.idItinerario
			List<Integer> resultados = entityManager.createQuery(
							"SELECT ri.itinerario.idItinerario FROM ReservaItinerario ri WHERE ri.reserva.idReserva = :idReserva", Integer.class)
					.setParameter("idReserva", idReserva)
					.setMaxResults(1)
					.getResultList();

			return resultados.isEmpty() ? null : resultados.get(0);
		} catch (Exception e) {
			Logger.logError("Error obteniendo itinerario de la reserva N° " + idReserva + ": " + e.getMessage());
			return null;
		}
	}

	@Override
	@Transactional
	public Reserva guardarOGenerarPnr(Integer idReserva) {
		if (idReserva == null) return null;

		Reserva reserva = entityManager.find(Reserva.class, idReserva);
		if (reserva != null) {
			if (reserva.getCodigoReserva() == null || reserva.getCodigoReserva().isBlank()) {
				String nuevoPnr;
				Long existe;
				// Garantizar unicidad en la base de datos
				do {
					nuevoPnr = PnrGenerator.generarPnr();
					existe = entityManager.createQuery(
									"SELECT COUNT(r) FROM Reserva r WHERE r.codigoReserva = :pnr", Long.class)
							.setParameter("pnr", nuevoPnr)
							.getSingleResult();
				} while (existe != null && existe > 0);

				reserva.setCodigoReserva(nuevoPnr);
				reserva = entityManager.merge(reserva);
			}
		}

		Logger.logInfo(reserva.toString());
		return reserva;
	}


	@Override
	@Transactional(readOnly = true)
	public Integer buscarIdPorPnrYPasajero(String pnr, String apellidoRut) {
		if (pnr == null || pnr.isBlank() || apellidoRut == null || apellidoRut.isBlank()) {
			return null;
		}

		try {
			List<Integer> resultados = entityManager.createQuery(
							"SELECT r.idReserva FROM Reserva r " +
									"WHERE UPPER(r.codigoReserva) = :pnr " +
									"AND (UPPER(r.pasajero.rut) = :filtro OR UPPER(r.pasajero.usuario.apellido) = :filtro)", Integer.class)
					.setParameter("pnr", pnr.trim().toUpperCase())
					.setParameter("filtro", apellidoRut.trim().toUpperCase())
					.setMaxResults(1)
					.getResultList();

			return resultados.isEmpty() ? null : resultados.get(0);
		} catch (Exception e) {
			Logger.logError("Error validando PNR y Pasajero: " + e.getMessage());
			return null;
		}
	}



	@Override
	public Date obtenerFechaSalidaVuelo(Integer idItinerario) {
		if (idItinerario == null) {
			return null;
		}

		try {
			// Consulta la hora de salida del primer vuelo/segmento asociado al itinerario
			String jpql = "SELECT itv.vuelo.fechaHoraSalida " +
					"FROM ItinerarioVuelo itv " +
					"WHERE itv.itinerario.idItinerario = :idItinerario " +
					"ORDER BY itv.vuelo.fechaHoraSalida ASC";

			List<Date> resultados = em.createQuery(jpql, Date.class)
					.setParameter("idItinerario", idItinerario)
					.setMaxResults(1)
					.getResultList();

			if (!resultados.isEmpty()) {
				return resultados.get(0);
			}

			// Fallback Native Query en caso de que ItinerarioVuelo use la entidad Vuelo/Segmento directamente
			String nativeSql = "SELECT COALESCE(sgm.hora_salida, vl.fecha_hora_salida) " +
					"FROM itinerario_vuelo itv " +
					"JOIN vuelo vl ON vl.id_vuelo = itv.id_vuelo " +
					"LEFT JOIN segmento_vuelo sgm ON sgm.id_vuelo = vl.id_vuelo " +
					"WHERE itv.id_itinerario = :idItinerario " +
					"ORDER BY COALESCE(sgm.hora_salida, vl.fecha_hora_salida) ASC LIMIT 1";

			List<?> nativeRes = em.createNativeQuery(nativeSql)
					.setParameter("idItinerario", idItinerario)
					.getResultList();

			if (!nativeRes.isEmpty()) {
				Object obj = nativeRes.get(0);
				if (obj instanceof java.sql.Timestamp) {
					return new Date(((java.sql.Timestamp) obj).getTime());
				} else if (obj instanceof Date) {
					return (Date) obj;
				}
			}

		} catch (Exception e) {
			Logger.logError("Error al obtener fecha de salida para el itinerario " + idItinerario + ": " + e.getMessage());
		}

		return null;
	}

}
