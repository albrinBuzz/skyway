package com.SkyWay.modules.vuelo.domain.service;

import java.util.List;
import java.util.Optional;

import com.SkyWay.dto.InfoVueloDTO;

import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO;


public interface VueloService {
	
	
	  public List<Vuelo> findAll();
	  /*
	   Optional<Vuelo> vueloOpt = vueloRepository.findById(vueloId);
	if (vueloOpt.isPresent()) {
	    Vuelo vuelo = vueloOpt.get();
	    Duration duracion = vuelo.getDuracion();
	    System.out.println("Duración del vuelo: " + duracion.toHours() + " horas y " + (duracion.toMinutes() % 60) + " minutos.");
	}

	    */
	  public Optional<Vuelo> findById(Integer id);	
	  
	  public Vuelo save(Vuelo vuelo);
	  /*
	   Vuelo vuelo = new Vuelo();
		vuelo.setOrigen("Madrid");
		vuelo.setDestino("Barcelona");
		vuelo.setDuracion(Duration.ofHours(1).plusMinutes(30)); // 1 hora y 30 minutos
		vueloRepository.save(vuelo);
	   */
	  public void deleteById(Integer id);
	  public  List<Vuelo>  buscarVuelo(String departureCity,String arrivalCity, String fachaSalida);
	  public List<InfoVueloDTO> vuelosProximos();
	  public InfoVueloDTO getInfoVuelo(int idVuelo);
	  List<Vuelo> findByPiloto(Piloto piloto);
	  public Vuelo updateVuelo(Vuelo vuelo);
	public List<AeropuertoMapaDTO> findAllParaMapa();
	public VueloMapaDTO construirGeometriaVuelo(Vuelo vuelo);
	public List<VueloMapaDTO> construirGeometriaVuelos(List<Vuelo> vuelos);
}
