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
    public Itinerario crearItinerario(Itinerario itinerario) {
        return itinerarioRepository.save(itinerario);
    }

    @Override
    public List<Itinerario> obtenerTodosItinerarios() {
        return itinerarioRepository.findAll();
    }



    @Override
    public Itinerario obtenerItinerarioPorId(Integer id) {
        Optional<Itinerario> itinerario = itinerarioRepository.findById(id);
        return itinerario.orElse(null);  // Retorna null si no se encuentra el itinerario
    }

    @Override
    public void eliminarItinerario(Integer id) {
        itinerarioRepository.deleteById(id);
    }

    @Override
    public List<ItinerarioDTO> buscarItinerarios(String ciudadSalida, String ciudadLlegada, String fechaInicio) throws ParseException {

        // Convertir fecha de String a java.sql.Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        java.util.Date parsedDate = dateFormat.parse(fechaInicio);  // 'fechaInicio' es el String
        Date sqlDate = new Date(parsedDate.getTime());  // Convertir a java.sql.Date

        // Log
        Logger.logInfo("buscando para la fecha " + sqlDate.toString());
        Logger.logInfo("ciudad salida: " + ciudadSalida);
        Logger.logInfo("ciudad llegada: " + ciudadLlegada);

        // Consultar directamente en la base de datos con SQL nativo
        String queryString = "SELECT * FROM fnBuscarVuelo(:p_ciudad_salida, :p_ciudad_llegada, :p_fecha_inicio)";

        Query query = entityManager.createNativeQuery(queryString, ItinerarioDTO.class);
        query.setParameter("p_ciudad_salida", ciudadSalida);
        query.setParameter("p_ciudad_llegada", ciudadLlegada);
        query.setParameter("p_fecha_inicio", sqlDate);

        // Ejecutar la consulta y devolver el resultado
        List<ItinerarioDTO> itinerarios = query.getResultList();

        // Log de resultado
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
