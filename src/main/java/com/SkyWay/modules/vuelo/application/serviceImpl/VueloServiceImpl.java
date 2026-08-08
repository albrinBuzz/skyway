package com.SkyWay.modules.vuelo.application.serviceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.segmentovuelo.domain.repository.SegmentoVueloRepository;
import com.SkyWay.modules.segmentovuelo.presentation.dto.SegmentoMapaProjection;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.repository.VueloRepository;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO;
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
	@Autowired
	private AeropuertoRepository aeropuertoRepository;
	@Autowired
	private SegmentoVueloRepository segmentoVueloRepository;

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
	public List<Vuelo> buscarVuelo(String departureCity, String arrivalCity, String departureDate) {
		String sql;
		Query query;

		if (departureDate != null && !departureDate.isEmpty()) {
			sql = "SELECT * FROM public.FnbuscarVuelos(:ciudadSalida, :ciudadLlegada, :fechaIn)";
			query = em.createNativeQuery(sql, Vuelo.class);
			query.setParameter("fechaIn", java.sql.Date.valueOf(LocalDate.parse(departureDate)));
		} else {
			sql = "SELECT * FROM public.FnbuscarVuelos(:ciudadSalida, :ciudadLlegada)";
			query = em.createNativeQuery(sql, Vuelo.class);
		}

		query.setParameter("ciudadSalida", departureCity);
		query.setParameter("ciudadLlegada", arrivalCity);

		List<Vuelo> vuelos = query.getResultList();


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

	public List<AeropuertoMapaDTO> findAllParaMapa() {
		return aeropuertoRepository.findAllConCoordenadas().stream()
				.map(p -> new AeropuertoMapaDTO(
						p.getCodigoIata(), p.getNombreAeropuerto(), p.getCiudad(),
						p.getLatitud(), p.getLongitud()))
				.toList();
	}

	public VueloMapaDTO construirGeometriaVuelo(Vuelo vuelo) {
		List<SegmentoMapaProjection> filas = segmentoVueloRepository.findGeometriaPorVuelo(vuelo.getIdVuelo());
		if (filas.isEmpty()) return null;

		List<VueloMapaDTO.PuntoSegmentoDTO> puntos = new ArrayList<>();

		for (int i = 0; i < filas.size(); i++) {
			SegmentoMapaProjection seg = filas.get(i);

			// Origen del segmento: solo lo agregamos si es el primero,
			// o si no coincide con el destino del segmento anterior (evita duplicados en escalas)
			if (i == 0) {
				puntos.add(new VueloMapaDTO.PuntoSegmentoDTO(
						seg.getIataOrigen(), seg.getNombreOrigen(),
						seg.getLatOrigen(), seg.getLngOrigen(), "ORIGEN"));
			}

			boolean esUltimo = (i == filas.size() - 1);
			puntos.add(new VueloMapaDTO.PuntoSegmentoDTO(
					seg.getIataDestino(), seg.getNombreDestino(),
					seg.getLatDestino(), seg.getLngDestino(),
					esUltimo ? "DESTINO" : "ESCALA"));
		}

		return new VueloMapaDTO(vuelo.getIdVuelo(), vuelo.getNumeroVuelo(), puntos);
	}


	public List<VueloMapaDTO> construirGeometriaVuelos(List<Vuelo> vuelos) {
		List<VueloMapaDTO> resultado = new ArrayList<>();
		for (Vuelo v : vuelos) {
			VueloMapaDTO dto = construirGeometriaVuelo(v);
			if (dto != null) resultado.add(dto);
		}
		return resultado;
	}

}
