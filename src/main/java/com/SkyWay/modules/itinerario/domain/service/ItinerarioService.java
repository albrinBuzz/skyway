package com.SkyWay.modules.itinerario.domain.service;



import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;

import java.text.ParseException;
import java.util.List;

public interface ItinerarioService {

    // Crear un nuevo itinerario
    Itinerario crearItinerario(Itinerario itinerario);

    // Buscar todos los itinerarios
    List<Itinerario> obtenerTodosItinerarios();

    // Buscar un itinerario por su id
    Itinerario obtenerItinerarioPorId(Integer id);

    // Eliminar un itinerario por su id
    void eliminarItinerario(Integer id);

    public List<ItinerarioDTO> buscarItinerarios(String ciudadSalida, String ciudadLlegada, String fechaInicio) throws ParseException;

    public List<ItinerarioDetalleDTO> obtenerDetalleItinerario(Integer idItinerario);
}
