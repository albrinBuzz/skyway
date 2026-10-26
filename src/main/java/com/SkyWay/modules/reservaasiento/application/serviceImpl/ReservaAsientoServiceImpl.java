package com.SkyWay.modules.reservaasiento.application.serviceImpl;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.reservaasiento.domain.repository.ReservaAsientoRepository;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.SkyWay.util.UsuarioSesion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;


import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservaAsientoServiceImpl implements ReservaAsientoService {
	
	private final Logger LOGGER = LoggerFactory.getLogger(ReservaAsientoServiceImpl.class);

    @Autowired
    private ReservaAsientoRepository reservaAsientoRepository;

    /*private EntityManagerFactory emf = null;
    
	  public EntityManager getEntityManager() {
		  	emf = Persistence.createEntityManagerFactory("mainPU");
	        return emf.createEntityManager();
	    }*/

	@PersistenceContext
	private EntityManager em;
    
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
	@Transactional
	public boolean cambiarAsiento(Integer idAsiento, Integer idReserva,Integer id_asiento_org) {

		// El solicitante sale de la sesion del servidor; el SP valida dueno, vuelo/avion y clase.
		String rut = UsuarioSesion.rutAutenticado();
		if (rut == null) {
			throw new AccessDeniedException("Debes iniciar sesión para cambiar asientos.");
		}

		StoredProcedureQuery procedureQuery = em
	              .createStoredProcedureQuery("sp_cambiarAsiento");
		
		procedureQuery.registerStoredProcedureParameter("p_id_asiento", Integer.class, ParameterMode.IN);
		procedureQuery.registerStoredProcedureParameter("p_id_reserva", Integer.class, ParameterMode.IN);
		procedureQuery.registerStoredProcedureParameter("p_id_asiento_org", Integer.class, ParameterMode.IN);
		procedureQuery.registerStoredProcedureParameter("p_rut_solicitante", String.class, ParameterMode.IN);
		
		procedureQuery.setParameter("p_id_asiento", idAsiento);
		procedureQuery.setParameter("p_id_reserva", idReserva);
		procedureQuery.setParameter("p_id_asiento_org", id_asiento_org);
		procedureQuery.setParameter("p_rut_solicitante", rut);

		try {
			procedureQuery.execute();
			LOGGER.info("asiento cambiado");
			return true;
		} catch (Exception e) {
			LOGGER.error("Error al cambiar asiento: {}", e.getMessage(), e);
			// Se propaga para que el llamador no informe un exito falso (y haga rollback).
			throw new IllegalStateException("No se pudo cambiar el asiento: " + mensajeLimpio(e), e);
		}
	}

	private static String mensajeLimpio(Throwable e) {
		Throwable t = e;
		while (t.getCause() != null) {
			t = t.getCause();
		}
		String m = t.getMessage();
		if (m == null) {
			return "error desconocido";
		}
		int idx = m.indexOf("\n");
		return idx > 0 ? m.substring(0, idx).replaceFirst("^ERROR:\\s*", "") : m;
	}

	@Override
	public ReservaAsiento findByReserva(Reserva reserva) {
		// TODO Auto-generated method stub
		return reservaAsientoRepository.findByReserva(reserva);
	}
	
}
