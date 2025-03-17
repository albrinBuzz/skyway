package com.SkyWay.serviceImpl;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Reserva;
import com.SkyWay.model.ReservaAsiento;
import com.SkyWay.repository.ReservaAsientoRepository;
import com.SkyWay.service.ReservaAsientoService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Persistence;
import jakarta.persistence.StoredProcedureQuery;

@Service
public class ReservaAsientoServiceImpl implements ReservaAsientoService{
	
	private final Logger LOGGER = LoggerFactory.getLogger(ReservaAsientoServiceImpl.class);

    @Autowired
    private ReservaAsientoRepository reservaAsientoRepository;

    private EntityManagerFactory emf = null;
    
	  public EntityManager getEntityManager() {
		  	emf = Persistence.createEntityManagerFactory("mainPU");
	        return emf.createEntityManager();
	    }
    
    // Crear o actualizar una reserva
    public ReservaAsiento guardarReserva(ReservaAsiento reservaAsiento) {
        return reservaAsientoRepository.save(reservaAsiento);
    }

    // Obtener todas las reservas
    public List<ReservaAsiento> obtenerTodasLasReservas() {
        return reservaAsientoRepository.findAll();
    }

    // Obtener una reserva por ID
    public Optional<ReservaAsiento> obtenerReservaPorId(Integer id) {
        return reservaAsientoRepository.findById(id);
    }

    // Eliminar una reserva por ID
    public void eliminarReserva(Integer id) {
        reservaAsientoRepository.deleteById(id);
    }

	@Override
	public boolean cambiarAsiento(Integer idAsiento, Integer idReserva,Integer id_asiento_org) {

	
		EntityManager em = getEntityManager();
		
		StoredProcedureQuery procedureQuery = em
	              .createStoredProcedureQuery("sp_cambiarAsiento");
		
		procedureQuery.registerStoredProcedureParameter("p_id_asiento", Integer.class, ParameterMode.IN);
		procedureQuery.registerStoredProcedureParameter("p_id_reserva", Integer.class, ParameterMode.IN);
		procedureQuery.registerStoredProcedureParameter("p_id_asiento_org", Integer.class, ParameterMode.IN);
		
	    // Establecer los valores de los parámetros
		procedureQuery.setParameter("p_id_asiento", idAsiento);
		procedureQuery.setParameter("p_id_reserva", idReserva);
		procedureQuery.setParameter("p_id_asiento_org", id_asiento_org);
		
		 // Iniciar la transacción
	    em.getTransaction().begin();
	    try {
	        // Ejecutar el procedimiento
	    	procedureQuery.execute();
	        em.getTransaction().commit();  // Confirmar los cambios
	        LOGGER.info("asiento cambiado");
	        return true;
	    } catch (Exception e) {
	        // Si hay un error, hacer rollback
	        em.getTransaction().rollback();
	        LOGGER.error("Error al cancelar  al cambiar asiento: " + e.getMessage(), e);
	        return false;
	    } finally {
	        em.close();  // Asegúrate de cerrar el EntityManager
	    }
		

	}

	@Override
	public ReservaAsiento findByReserva(Reserva reserva) {
		// TODO Auto-generated method stub
		return reservaAsientoRepository.findByReserva(reserva);
	}
	
}
