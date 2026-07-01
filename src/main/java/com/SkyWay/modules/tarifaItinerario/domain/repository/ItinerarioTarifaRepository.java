package com.SkyWay.modules.tarifaItinerario.domain.repository;


import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItinerarioTarifaRepository extends JpaRepository<ItinerarioTarifa, Integer> {

    // Ejemplo de métodos derivados personalizados:
    List<ItinerarioTarifa> findByItinerarioIdItinerario(Integer idItinerario);


    List<ItinerarioTarifa> findByTarifaIdTarifa(Integer idTarifa);

    //ItinerarioTarifa getByItinerarioIdItinerarioAndTarifaIdTarifa(Integer idTarifa, Integer idItinerario);

    @Query("SELECT it FROM ItinerarioTarifa it WHERE it.tarifa.idTarifa = :idTarifa AND it.itinerario.idItinerario = :idItinerario")
    ItinerarioTarifa getByTarifaAndItinerario(@Param("idItinerario") Integer idItinerario, @Param("idTarifa") Integer idTarifa);


    boolean existsByItinerarioIdItinerarioAndTarifaIdTarifa(Integer idItinerario, Integer idTarifa);
}
