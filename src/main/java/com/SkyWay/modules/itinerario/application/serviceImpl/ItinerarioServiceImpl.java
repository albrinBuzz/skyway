package com.SkyWay.modules.itinerario.application.serviceImpl;



import com.SkyWay.modules.itinerario.domain.model.Itinerario;

import com.SkyWay.modules.itinerario.domain.repository.ItinerarioRepository;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.*;
import com.SkyWay.modules.segmentovuelo.domain.repository.SegmentoVueloRepository;
import com.SkyWay.modules.segmentovuelo.presentation.dto.PuntoRutaProjection;
import com.SkyWay.util.Logger;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Autowired
    private SegmentoVueloRepository segmentoVueloRepository;

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
    public List<ItinerarioResumenDTO> buscarConFiltroFechas(String rut, LocalDate fechaInicio, LocalDate fechaFin, int limit, int offset) {
        Timestamp tsInicio = null;
        Timestamp tsFin = null;

        if (fechaInicio != null) {
            tsInicio = Timestamp.valueOf(fechaInicio.atStartOfDay());
        }
        if (fechaFin != null) {
            tsFin = Timestamp.valueOf(fechaFin.atTime(23, 59, 59));
        }

        String sql = "SELECT * FROM fn_getItinerariosPorRutYFechas(:rut, :limit, :offset, :fechaIni, :fechaFin)";

        Query query = entityManager.createNativeQuery(sql, ItinerarioResumenDTO.class);
        query.setParameter("rut", rut);
        query.setParameter("limit", limit);
        query.setParameter("offset", offset);
        query.setParameter("fechaIni", tsInicio); // tipo java.sql.Timestamp
        query.setParameter("fechaFin", tsFin);    // tipo java.sql.Timestamp

        return (List<ItinerarioResumenDTO>) query.getResultList();
        //return itinerarioRepository.buscarConFiltroFechas(rut, tsInicio, tsFin, limit, offset);
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

    @Override
    public Page<Itinerario> findItinerariosByRut(String rut, Pageable pageable) {
        return itinerarioRepository.findItinerariosByRut(rut,pageable);
    }

    @Override
    public List<ItinerarioResumenDTO> findResumenByRut(String rut, int limit, int offset) {
        //return itinerarioRepository.findResumenByRut(rut, limit, offset);
        //SELECT * FROM fn_getItinerariosRut('12345678-9', 100, 0);
        Logger.logInfo("buscado iitinerarios");
        String queryString = "SELECT * FROM fn_getItinerariosPorRutYFechas(:p_rut,:p_limit,:offset)";

        Query query = entityManager.createNativeQuery(queryString, ItinerarioResumenDTO.class);
        //query.setParameter("p_id_itinerario", idItinerario);
        query.setParameter("p_rut", rut);
        query.setParameter("p_limit", limit);
        query.setParameter("offset", offset);

        // Ejecutar la consulta y obtener los resultados
        return (List<ItinerarioResumenDTO>) query.getResultList();
    }




    public List<PuntoMapaDTO> obtenerRutaMapa(Integer idItinerario) {
        List<PuntoRutaProjection> filas = segmentoVueloRepository.obtenerRutaPorItinerario(idItinerario);

        // Colapsar duplicados consecutivos: el DESTINO de un segmento suele
        // coincidir con el ORIGEN del siguiente (misma escala física)
        List<PuntoRutaProjection> puntos = new ArrayList<>();
        for (PuntoRutaProjection fila : filas) {
            if (!puntos.isEmpty() &&
                    puntos.get(puntos.size() - 1).getIdAeropuerto().equals(fila.getIdAeropuerto())) {
                continue;
            }
            puntos.add(fila);
        }

        List<PuntoMapaDTO> resultado = new ArrayList<>();
        for (int i = 0; i < puntos.size(); i++) {
            PuntoRutaProjection p = puntos.get(i);
            String tipo = (i == 0) ? "ORIGEN" : (i == puntos.size() - 1) ? "DESTINO" : "ESCALA";
            resultado.add(new PuntoMapaDTO(
                    p.getCodigoIata(), p.getNombreAeropuerto(), p.getCiudad(),
                    p.getLatitud(), p.getLongitud(), tipo));
        }
        return resultado;
    }



}
