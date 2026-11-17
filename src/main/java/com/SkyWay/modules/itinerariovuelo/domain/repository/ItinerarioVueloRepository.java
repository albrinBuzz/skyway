package com.SkyWay.modules.itinerariovuelo.domain.repository;



import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ItinerarioVueloRepository extends JpaRepository<ItinerarioVuelo, Integer> {

    // Encuentra todos los vuelos por ID de itinerario
    List<ItinerarioVuelo> findByItinerarioIdItinerario(Integer idItinerario);

    // Encuentra los vuelos de un itinerario ordenados secuencialmente
    List<ItinerarioVuelo> findByItinerarioIdItinerarioOrderByOrdenAsc(Integer idItinerario);

    // Encuentra los IDs de itinerario donde participa un vuelo dado
    @Query("SELECT DISTINCT iv.itinerario.idItinerario FROM ItinerarioVuelo iv WHERE iv.vuelo.idVuelo = :idVuelo")
    List<Integer> findIdsItinerariosPorVuelo(@Param("idVuelo") Integer idVuelo);

    // Encuentra todos los vuelos por tipo de conexión
    List<ItinerarioVuelo> findByTipoConexion(String tipoConexion);
}
