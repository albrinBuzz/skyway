package com.SkyWay.modules.itinerario.application.serviceImpl;



import com.SkyWay.modules.itinerario.domain.model.Itinerario;

import com.SkyWay.modules.itinerario.domain.repository.ItinerarioRepository;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import com.SkyWay.util.Logger;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.List;

@Service
public class ItinerarioServiceImpl implements ItinerarioService {

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Autowired
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    private String getDatabaseType() {
        try (Connection conn = dataSource.getConnection()) {
            String databaseProductName = conn.getMetaData().getDatabaseProductName();
            return databaseProductName.toLowerCase();
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener el tipo de base de datos", e);
        }
    }


    @Override
    public Itinerario save(Itinerario itinerario) {
        return itinerarioRepository.save(itinerario);
    }

    @Override
    public List<Itinerario> findAll() {
        return itinerarioRepository.findAll();
    }



    @Override
    public Itinerario findById(Integer id) {
        Optional<Itinerario> itinerario = itinerarioRepository.findById(id);
        return itinerario.orElse(null);  // Retorna null si no se encuentra el itinerario
    }

    @Override
    public void deleteById(Integer id) {
        itinerarioRepository.deleteById(id);
    }


    @Override
    public List<ItinerarioDTO> buscarItinerarios(String codigoIataOrigen, String codigoIataDestino, String fechaInicio) throws ParseException {
        // Convertir la fecha de String a java.sql.Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate = dateFormat.parse(fechaInicio);
        Date sqlDate = new Date(parsedDate.getTime());

        // Logs informativos
        Logger.logInfo("Buscando vuelos para el día: " + sqlDate);
        Logger.logInfo("Código IATA Origen: " + codigoIataOrigen);
        Logger.logInfo("Código IATA Destino: " + codigoIataDestino);

        // Consulta SQL actualizada con los nuevos nombres de parámetros
        String queryString = "SELECT * FROM fnBuscarVuelo(:p_codigo_origen, :p_codigo_destino, :p_fecha_inicio)";

        Query query = entityManager.createNativeQuery(queryString, ItinerarioDTO.class);
        query.setParameter("p_codigo_origen", codigoIataOrigen);
        query.setParameter("p_codigo_destino", codigoIataDestino);
        query.setParameter("p_fecha_inicio", sqlDate);

        // Ejecutar consulta
        List<ItinerarioDTO> itinerarios = query.getResultList();

        Logger.logInfo("Se encontraron " + itinerarios.size() + " itinerarios.");

        return itinerarios;
    }



    @Override
    public List<ItinerarioDetalleDTO> obtenerDetalleItinerario(Integer idItinerario) {
        // Crear la consulta nativa para la función 'fnDTinitinerario'
        String queryString = "SELECT * FROM fnDTinitinerario(:p_id_itinerario)";

        Query query = entityManager.createNativeQuery(queryString, ItinerarioDetalleDTO.class);
        query.setParameter("p_id_itinerario", idItinerario);

        // Ejecutar la consulta y obtener los resultados
        List<ItinerarioDetalleDTO> detalles = query.getResultList();

        return detalles;
    }
}
