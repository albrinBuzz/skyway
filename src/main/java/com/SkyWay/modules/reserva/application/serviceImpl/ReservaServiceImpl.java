package com.SkyWay.modules.reserva.application.serviceImpl;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.util.Logger;
import jakarta.persistence.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SkyWay.dto.BoletoDTO;
import com.SkyWay.dto.ReservaVueloDTO;


@Service
public class ReservaServiceImpl implements ReservaService {


	@Autowired
	private ReservaRepository reservaRepository;

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
	@Transactional
	public String confirmarReserva(int idVuelo, Integer[] asientos, String rutPasajero) throws SQLException {
		//String sql = "SELECT confirmar_reserva(:idVuelo, :asientos, :rutPasajero)";
		StoredProcedureQuery query = em.createStoredProcedureQuery("spConfirmar_reserva");

		try {

			query.registerStoredProcedureParameter("p_idVuelo", Integer.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_asientos", Integer[].class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_rutPasajero", String.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_resultado", String.class, ParameterMode.OUT);

			query.setParameter("p_idVuelo", idVuelo);
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

}
