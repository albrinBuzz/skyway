package com.SkyWay.modules.tarifaItinerario.domain.repository;


import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItinerarioTarifaRepository extends JpaRepository<ItinerarioTarifa, Integer> {

    @Query("SELECT it FROM ItinerarioTarifa it LEFT JOIN FETCH it.tarifa t WHERE it.itinerario.idItinerario = :idItinerario AND (t.activo = true OR t IS NULL)")
    List<ItinerarioTarifa> findByItinerarioIdItinerario(@Param("idItinerario") Integer idItinerario);

    @Query("SELECT it FROM ItinerarioTarifa it LEFT JOIN FETCH it.tarifa t WHERE it.itinerario.idItinerario IN :ids AND (t.activo = true OR t IS NULL)")
    List<ItinerarioTarifa> findByItinerarioIdItinerarioIn(@Param("ids") List<Integer> ids);


    List<ItinerarioTarifa> findByTarifaIdTarifa(Integer idTarifa);

    //ItinerarioTarifa getByItinerarioIdItinerarioAndTarifaIdTarifa(Integer idTarifa, Integer idItinerario);

    @Query("SELECT it FROM ItinerarioTarifa it WHERE it.tarifa.idTarifa = :idTarifa AND it.itinerario.idItinerario = :idItinerario")
    ItinerarioTarifa getByTarifaAndItinerario(@Param("idItinerario") Integer idItinerario, @Param("idTarifa") Integer idTarifa);


    boolean existsByItinerarioIdItinerarioAndTarifaIdTarifa(Integer idItinerario, Integer idTarifa);
}
