package com.SkyWay.modules.asiento.application.serviceImpl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.asiento.domain.repository.AsientoRepository;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;

import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AsientoServiceImpl implements AsientoService {

	@Autowired
	private AsientoRepository asientoRepository;
	
    @Autowired
    private ClaseAsientoRepository claseAsientoRepository;

	private final Logger LOGGER = LoggerFactory.getLogger(AsientoServiceImpl.class);

	@Autowired
	private EntityManager em;
	
	@Autowired
    private  DataSource dataSource;
	
    private EntityManagerFactory emf = null;
    
	  public EntityManager getEntityManager() {
		  	emf = Persistence.createEntityManagerFactory("mainPU");
	        return emf.createEntityManager();
	    }
	  
	
	@Override
	public List<Asiento> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Asiento> findById(Integer id) {
		// TODO Auto-generated method stub
		return asientoRepository.findById(id);
	}

	@Override
	public Avion save(Asiento vuelo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteById(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Asiento> findByAvion(Avion avion) {
		// TODO Auto-generated method stub
		return asientoRepository.findByAvion(avion);
	}
	
	 public List<ClaseAsiento> obtenerTodasLasClasesAsientos() {
	        return claseAsientoRepository.findAll();
	    }
	
	
	@Override
	@Transactional
	public List<InfoAsientoDTO> getAsientosDisponibles(Integer idVuelo) {
		// TODO Auto-generated method stub

		List<InfoAsientoDTO> asientos;

		com.SkyWay.util.Logger.logInfo("Vuelo-> :"+idVuelo);
		Query query = em.createNativeQuery(
				"select * from fn_getAsientosAvion(:p_vuelo)",
				InfoAsientoDTO.class);
		query.setParameter("p_vuelo", idVuelo);

	
	   	asientos= query.getResultList();

		return asientos;
	}

	@Override
	public List<InfoAsientoReservaDTO> getAsientosReservados(Integer idReserva, Integer idItinerario) {
		  List<InfoAsientoReservaDTO>asientos;
		Query query = em.createNativeQuery(
				"SELECT * FROM fn_getAsientosPorItinerarioYReserva(:p_itinerario, :p_reserva);",
				InfoAsientoReservaDTO.class);
		query.setParameter("p_itinerario", idReserva);
		query.setParameter("p_reserva", idItinerario);

		asientos= query.getResultList();

		return asientos;
	}

	@Override
	//@Transactional(readOnly = true)
	public String verificarDisponibilidad(int idVuelo, Integer[] asientos) throws SQLException {
		StoredProcedureQuery query = em.createStoredProcedureQuery("spVerificarDisponinibilidadAsientos");

		//com.SkyWay.util.Logger.logInfo("parametros: Vuelo-> "+idVuelo+" idReserva-> "+idReserva);

		try {

			query.registerStoredProcedureParameter("p_idVuelo", Integer.class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_asientos", Integer[].class, ParameterMode.IN);
			query.registerStoredProcedureParameter("p_resultado", String.class, ParameterMode.OUT);

			query.setParameter("p_idVuelo", idVuelo);
			query.setParameter("p_asientos", asientos);

			query.execute();

			String res = (String) query.getOutputParameterValue("p_resultado");

			com.SkyWay.util.Logger.logInfo(res);
			Optional<String> resultado = Optional.ofNullable(res);



			if (res == null || res.trim().isEmpty()) {
				// Si el SP devuelve null, algo salió mal en la lógica del SP
				throw new RuntimeException("El sistema de verificación no retornó un estado válido.");
			}

			if (resultado.isPresent()&&resultado.get().startsWith("ERROR:")) {
				// Forzar rollback lanzando excepción
				com.SkyWay.util.Logger.logInfo(resultado.get());
				throw new RuntimeException(resultado.get().substring(6).trim());
			}

			if ("OK".equalsIgnoreCase(res)) return "";
			return res;

		} catch (Exception ex) {
			Throwable causa = ex.getCause();
			if (causa instanceof SQLException sqlException) {
				// Extraer mensaje personalizado (RAISE EXCEPTION '...') de PostgreSQL
				String error = extractErrorMessage(sqlException); // tu método personalizado
				com.SkyWay.util.Logger.logInfo("Mensaje de Postgres: " + error);
				throw new SQLException(error); // Relanza la excepción con el mensaje real
			}

			// Si no fue SQLException, relanzar la original
			throw ex;
		}
	}


	@Override
	@Transactional
	public List<InfoAsientoDTO> getAsientosVuelo(Integer idReserva, Integer idVuelo) {
		List<InfoAsientoDTO> asientos;
		  Query query = em.createNativeQuery(
				"select * from fn_getAsientosAvion(:p_vuelo,:p_reserva)",
				InfoAsientoDTO.class);
		query.setParameter("p_vuelo", idVuelo);
		query.setParameter("p_reserva", idReserva);
		LOGGER.info("Vuelo a buscar {} ",idVuelo);

	 return  query.getResultList();
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
