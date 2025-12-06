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
		/*StoredProcedureQuery query = em.createStoredProcedureQuery("sp_getAsientosAvion");
	    
	    // Registrar los parámetros del procedimiento
	    query.registerStoredProcedureParameter("p_id_avion", Integer.class, ParameterMode.IN);
	    query.registerStoredProcedureParameter("cursor_asientos", void.class, ParameterMode.REF_CURSOR);
	    
	    // Establecer los valores de los parámetros
	    query.setParameter("p_id_avion", idAvion);
	    //query.setParameter(2, ParameterMode.REF_CURSOR);
	 
	    
	    // Ejecutar el procedimiento
	    query.execute();
	    
	    ResultSet cursor = (ResultSet) query.getOutputParameterValue("cursor_asientos");
	    
	     List<Object[]> resultList = query.getResultList();
	    // Mapeo a objetos DTO
	    
	    // Obtener el cursor
	
	
	    
	    for (Object[] row : resultList) {
	        InfoVueloDTO vuelo = new InfoVueloDTO();
	        InfoAsientoDTO asiento=new InfoAsientoDTO();
	        asiento.setIdAsiento((Integer) row[0]);
	        asiento.setNumeroAsiento((String) row[1]);
	  

	        //asiento.setEstado( (estado) ? "libre" : "ocupado"));

	        
	        asientos.add(asiento);
	    }
	    
	

	   /* try (Connection conn = dataSource.getConnection();
	    		   CallableStatement stmt = conn.prepareCall("CALL sp_getAsientosAvion(?, ?)")) {
	        
	        stmt.setInt(1, idAvion);
	        stmt.registerOutParameter(2, Types.REF_CURSOR);

	        // Ejecutar el procedimiento
	        stmt.execute();

	        // Obtener el cursor
	        try (ResultSet rs = (ResultSet) stmt.getObject(2)) {
	            while (rs.next()) {
	                InfoAsientoDTO asiento = new InfoAsientoDTO();
	                asiento.setIdAsiento(rs.getInt(1));
	                asiento.setNumeroAsiento(rs.getString(2));
	                String estado= rs.getString(3);
	    	        
	    	        if(estado.equals("libre")) {
	    	          	asiento.setEstado(true);
	    	        }else {
	    	          	asiento.setEstado(false);
	    	        }
	                asientos.add(asiento);
	            }
	        }
	    } catch (SQLException e) {
	    	System.out.println(e.getMessage());
	    	e.printStackTrace();
		  
    		//throw new RuntimeException("Error al llamar al procedimiento: " + e.getMessage());
		}*/
		
	      /*Query query = em.createNativeQuery(
	        		 "select * from fn_getAsientosAvion(:idAvion,:p_vuelo)",
	        		    InfoAsientoDTO.class);
	          		query.setParameter("idAvion", idAvion);
	          		query.setParameter("p_vuelo", idVuelo);*/
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
	@Transactional
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
			LOGGER.info(res);
			Optional<String> resultado = Optional.ofNullable(res);




			if (resultado.isPresent()&&resultado.get().startsWith("ERROR:")) {
				// Forzar rollback lanzando excepción
				com.SkyWay.util.Logger.logInfo(resultado.get());
				throw new RuntimeException(resultado.get().substring(6).trim());
			}

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
		return "";
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
