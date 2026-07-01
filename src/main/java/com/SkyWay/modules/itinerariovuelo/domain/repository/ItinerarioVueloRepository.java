package com.SkyWay.modules.itinerariovuelo.domain.repository;



import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ItinerarioVueloRepository extends JpaRepository<ItinerarioVuelo, Integer> {

    // Encuentra todos los vuelos por ID de itinerario
    List<ItinerarioVuelo> findByItinerarioIdItinerario(Integer idItinerario);

    // Encuentra todos los vuelos por tipo de conexión
    List<ItinerarioVuelo> findByTipoConexion(String tipoConexion);
}
