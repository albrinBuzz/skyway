package com.SkyWay.modules.itinerario.domain.repository;


import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface ItinerarioRepository extends JpaRepository<Itinerario, Integer> {

    // Ejecutar la función fnBuscarVuelo en PostgreSQL y mapear el resultado al DTO
    @Query(value = "SELECT * FROM fnBuscarVuelo(:p_ciudad_salida, :p_ciudad_llegada, :p_fecha_inicio)", nativeQuery = true)
    List<ItinerarioDTO> buscarItinerariosPorCiudadesYFecha(
            @Param("p_ciudad_salida") String ciudadSalida,
            @Param("p_ciudad_llegada") String ciudadLlegada,
            @Param("p_fecha_inicio") String fechaInicio
    );

    @Query(value = "SELECT * FROM fnBuscarVuelo(:p_ciudad_salida, :p_ciudad_llegada, :p_fecha_inicio)", nativeQuery = true)
    List<ItinerarioDTO> buscarItinerariosPorCiudadesYFechaPostgres(
            @Param("p_ciudad_salida") String ciudadSalida,
            @Param("p_ciudad_llegada") String ciudadLlegada,
            @Param("p_fecha_inicio") Date fechaInicio
    );


}
