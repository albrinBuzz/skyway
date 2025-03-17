package com.SkyWay.serviceImpl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.dto.BoletoDTO;
import com.SkyWay.dto.ReservaVueloDTO;
import com.SkyWay.model.Reserva;
import com.SkyWay.repository.ReservaRepository;
import com.SkyWay.service.ReservaService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Persistence;
import jakarta.persistence.StoredProcedureQuery;

@Service
public class ReservaServiceImpl implements ReservaService{

	private final Logger LOGGER = LoggerFactory.getLogger(ReservaServiceImpl.class);
	
	@Autowired
	private ReservaRepository reservaRepository;
	
	@Autowired
	private EntityManager em;
	
	private EntityManagerFactory emf ;
	
	  public EntityManager getEntityManager() {
		  	emf = Persistence.createEntityManagerFactory("mainPU");
	        return emf.createEntityManager();
	    }
	  
	
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
	public List<ReservaVueloDTO> getReservasUsuario(String rut) {
	    // Crear la llamada al procedimiento almacenado
		
		EntityManager em = getEntityManager();
		
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
	        //reserva.setPrecio((Integer) row[9]);
	        reservas.add(reserva);
	    }

	    return reservas;
	}


	@Override
	public void cancelarReserva(Integer id) {
		EntityManager em = getEntityManager();
		

		
	    StoredProcedureQuery query = em.createStoredProcedureQuery("sp_cancelar_reserva");
	    
	    // Registrar los parámetros del procedimiento
	    query.registerStoredProcedureParameter("p_id_reserva", Integer.class, ParameterMode.IN);

	    // Establecer los valores de los parámetros
	    query.setParameter("p_id_reserva", id);
	    // Ejecutar el procedimiento
	    // Iniciar la transacción
	    em.getTransaction().begin();
	    try {
	        // Ejecutar el procedimiento
	        query.execute();
	        em.getTransaction().commit();  // Confirmar los cambios
	        LOGGER.info("Cancelado reserva");
	    } catch (Exception e) {
	        // Si hay un error, hacer rollback
	        em.getTransaction().rollback();
	        LOGGER.error("Error al cancelar la reserva: " + e.getMessage(), e);
	    } finally {
	        em.close();  // Asegúrate de cerrar el EntityManager
	    }
		
	}


	@Override
	public BoletoDTO getBoleto(Integer id) {

	    // Obtener el EntityManager
	    EntityManager em = getEntityManager();
	    
	    // Llamar al procedimiento almacenado
	    StoredProcedureQuery query = em.createStoredProcedureQuery("sp_getBoletoReserva");
	    
	    // Registrar los parámetros del procedimiento almacenado
	    query.registerStoredProcedureParameter("id_reserva_input", Integer.class, ParameterMode.IN);
	    query.registerStoredProcedureParameter("resultado_cursor", void.class, ParameterMode.REF_CURSOR);
	    
	    // Establecer los valores de los parámetros
	    query.setParameter("id_reserva_input", id);
	    
	    BoletoDTO boleto = new BoletoDTO();
	    em.getTransaction().begin();
	    try {
		    query.execute();
		    
		    // Obtener el resultado (cursor)
		    List<Object[]> resultList = query.getResultList();
		    
		    // Crear el DTO para almacenar el boleto


		    // Mapeo de cada fila del resultado al DTO correspondiente
		    for (Object[] row : resultList) {
		        // Asumimos que cada fila tiene la misma estructura (según la consulta SQL)
		        
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
		    
	        em.getTransaction().commit();  // Confirmar los cambios
	        LOGGER.info("Cancelado reserva");
	    } catch (Exception e) {
	        // Si hay un error, hacer rollback
	        em.getTransaction().rollback();
	        LOGGER.error("Error al cancelar la reserva: " + e.getMessage(), e);
	    } finally {
	        em.close();  // Asegúrate de cerrar el EntityManager
	    }
	    
	    
	    // Ejecutar el procedimiento almacenado

	    // Retornar el DTO con los datos mapeados
	    return boleto;
	}


}
