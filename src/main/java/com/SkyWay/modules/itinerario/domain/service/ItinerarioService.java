package com.SkyWay.modules.itinerario.domain.service;



import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.presentation.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ItinerarioService {

    // Crear un nuevo itinerario
    Itinerario save(Itinerario itinerario);

    // Buscar todos los itinerarios
    List<Itinerario> findAll();

    // Buscar un itinerario por su id
    Itinerario findById(Integer id);

    // Eliminar un itinerario por su id
    void deleteById(Integer id);

    //public List<ItinerarioDTO> buscarItinerarios(String ciudadSalida, String ciudadLlegada, String fechaInicio) throws ParseException;

    public List<ItinerarioDTO> buscarItinerarios(String codigoIataOrigen, String codigoIataDestino, String fecha) throws ParseException;


    public List<ItinerarioDetalleDTO> obtenerDetalleItinerario(Integer idItinerario);

    Page<Itinerario> findItinerariosByRut(@Param("rut") String rut, Pageable pageable);

    List<ItinerarioResumenDTO> findResumenByRut(
            @Param("rut") String rut,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    List<PuntoMapaDTO> obtenerRutaMapa(Integer idItinerario);

    List<ItinerarioResumenDTO> buscarConFiltroFechas(String rut, LocalDate fechaInicio, LocalDate fechaFin, int limit, int offset);
}
