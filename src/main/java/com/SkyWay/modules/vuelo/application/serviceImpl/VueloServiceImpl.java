package com.SkyWay.modules.vuelo.application.serviceImpl;

import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.repository.VueloRepository;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.dto.InfoVueloDTO;


import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;


@Service

public class VueloServiceImpl implements VueloService {

	private final Logger LOGGER = LoggerFactory.getLogger(VueloServiceImpl.class);
	
    @Autowired
    private VueloRepository vueloRepository;
    
    @PersistenceContext
    private EntityManager em;
    

    @Override
    public List<Vuelo> findAll() {
        return vueloRepository.findAll();
    }
    @Override
    public Optional<Vuelo> findById(Integer id) {
        return vueloRepository.findById(id);
    }
    
    @Override
    public Vuelo save(Vuelo vuelo) {
        return vueloRepository.save(vuelo);
    }

    @Override
    public void deleteById(Integer id) {
        vueloRepository.deleteById(id);
    }



	@Override
	@Transactional
	public List<InfoVueloDTO> buscarVuelo(String departureCity, String arrivalCity, String departureDate, String returnDate) {
		
		// Crear la llamada al procedimiento almacenado
	    /*StoredProcedureQuery query = em.createStoredProcedureQuery("obtener_info_vuelo");
	    
	    // Registrar los parámetros del procedimiento
	    query.registerStoredProcedureParameter("p_ciudad_salida", String.class, ParameterMode.IN);
	    query.registerStoredProcedureParameter("p_ciudad_llegada", String.class, ParameterMode.IN);
	    query.registerStoredProcedureParameter("p_fecha_inicio", String.class, ParameterMode.IN);
	    query.registerStoredProcedureParameter("p_fecha_fin", String.class, ParameterMode.IN);
	    query.registerStoredProcedureParameter("p_cursor", void.class, ParameterMode.REF_CURSOR);
	    
	    // Establecer los valores de los parámetros
	    query.setParameter("p_ciudad_salida", departureCity);
	    query.setParameter("p_ciudad_llegada", arrivalCity);
	    query.setParameter("p_fecha_inicio", departureDate);
	    query.setParameter("p_fecha_fin", returnDate);
	    
	    // Ejecutar el procedimiento
	    query.execute();
	    
	    // Obtener el cursor
	    List<Object[]> resultList = query.getResultList();
	    
	    // Mapeo a objetos DTO
	    List<InfoVueloDTO> vuelos = new ArrayList<>();
	    for (Object[] row : resultList) {
	        InfoVueloDTO vuelo = new InfoVueloDTO();
	        vuelo.setIdAvion((Integer) row[0]);
	        vuelo.setIdVuelo((Integer) row[1]);
	        vuelo.setNumeroVuelo((String) row[2]);
	        vuelo.setCiudadSalida((String) row[3]);
	        vuelo.setCiudadLlegada((String) row[4]);
	        vuelo.setFechaHoraSalida((Timestamp) row[5]);
	        vuelo.setFechaHoraLlegada((Timestamp) row[6]);
	        vuelo.setPrecio((String) row[7]);
	        vuelo.setModeloAvion((String) row[8]);
	        vuelo.setDuracion((String) row[9]);
	        vuelos.add(vuelo);
	    }*/
		
		
		

        Query query = em.createNativeQuery(
       		 "SELECT * FROM public.obtener_info_vuelo(:ciudadSalida,:ciudadLlegada, :fechaIn,:fechaFin)",
       		    InfoVueloDTO.class);
         		query.setParameter("ciudadSalida", departureCity);
         		query.setParameter("ciudadLlegada", arrivalCity);
         		query.setParameter("fechaIn", departureDate);
         		query.setParameter("fechaFin", returnDate);
         		
         		
         		//LOGGER.info("Resultado {}",query.getResultList());
         		List<InfoVueloDTO> vuelos= query.getResultList();
         		
		return vuelos;
	}

	@Override
	@Transactional
	public List<InfoVueloDTO> vuelosProximos() {

        Query query = em.createNativeQuery(
        		"select * from fn_VuelosProximos()",
          		    InfoVueloDTO.class);
        			
            		//LOGGER.info("Resultado {}",query.getResultList());
            		List<InfoVueloDTO> vuelos= query.getResultList();
            		
   		return vuelos;
	}

	@Override
	public InfoVueloDTO getInfoVuelo(int idVuelo) {
		 Query query = em.createNativeQuery(
	        		"select * from fn_getVueloInfo(:idVuelo);",
	          		    InfoVueloDTO.class);
		
		 query.setParameter("idVuelo", idVuelo); 
		 
		InfoVueloDTO vuelo=(InfoVueloDTO) query.getSingleResult();
		
		return vuelo;
	}

	@Override
	public List<Vuelo> findByPiloto(Piloto piloto) {
		// TODO Auto-generated method stub
		List<Vuelo> vuelos=vueloRepository.findByPiloto(piloto);
		//System.out.println(vuelos);
		return vuelos;
	}

	@Override
	public Vuelo updateVuelo(Vuelo vuelo) {
		return vueloRepository.save(vuelo);
	}


}
