package com.SkyWay.modules.itinerario.application.serviceImpl;



import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.repository.ItinerarioRepository;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.*;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.repository.ItinerarioVueloRepository;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.segmentovuelo.domain.repository.SegmentoVueloRepository;
import com.SkyWay.modules.segmentovuelo.presentation.dto.PuntoRutaProjection;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class ItinerarioServiceImpl implements ItinerarioService {

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private SegmentoVueloRepository segmentoVueloRepository;

    @Autowired
    private ItinerarioVueloRepository itinerarioVueloRepository;

    @Autowired
    private ItinerarioTarifaRepository itinerarioTarifaRepository;

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
        return itinerarioRepository.findAllOptimized();
    }



    @Override
    public Itinerario findById(Integer id) {
        Optional<Itinerario> itinerario = itinerarioRepository.findById(id);
        return itinerario.orElse(null);  // Retorna null si no se encuentra el itinerario
    }

    @Override
    @SuppressWarnings("unchecked")
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

        // Se pasa la cadena del ResultSetMapping registrado en el DTO
        Query query = entityManager.createNativeQuery(sql, "ItinerarioResumenDTOMapping");
        query.setParameter("rut", rut);
        query.setParameter("limit", limit);
        query.setParameter("offset", offset);
        query.setParameter("fechaIni", tsInicio);
        query.setParameter("fechaFin", tsFin);

        return (List<ItinerarioResumenDTO>) query.getResultList();
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

    @Override
    @Transactional(readOnly = true)
    public long contarReservasTotalesPorItinerario(Integer idItinerario) {
        if (idItinerario == null || idItinerario <= 0) {
            return 0L;
        }
        Object res = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT ri.id_reserva) FROM reserva_itinerario ri WHERE ri.id_itinerario = :idItin"
        ).setParameter("idItin", idItinerario).getSingleResult();
        return res != null ? ((Number) res).longValue() : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarReservasActivasPorItinerario(Integer idItinerario) {
        if (idItinerario == null || idItinerario <= 0) {
            return 0L;
        }
        Object res = entityManager.createNativeQuery(
                "SELECT COUNT(DISTINCT ri.id_reserva) " +
                "FROM reserva_itinerario ri " +
                "JOIN reserva r ON r.id_reserva = ri.id_reserva " +
                "LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva " +
                "WHERE ri.id_itinerario = :idItin " +
                "AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%' " +
                "AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%ANUL%'"
        ).setParameter("idItin", idItinerario).getSingleResult();
        return res != null ? ((Number) res).longValue() : 0L;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Itinerario guardarItinerarioTransaccional(
            Itinerario itinerario,
            List<ItinerarioVuelo> tramos,
            Map<Integer, BigDecimal> preciosPorTarifa,
            boolean esModificacion) {
        return guardarItinerarioTransaccional(itinerario, tramos, preciosPorTarifa, esModificacion, 10);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Itinerario guardarItinerarioTransaccional(
            Itinerario itinerario,
            List<ItinerarioVuelo> tramos,
            Map<Integer, BigDecimal> preciosPorTarifa,
            boolean esModificacion,
            int maxHorasEspera) {

        long maxEsperaMin = Math.max(1, maxHorasEspera) * 60L;

        if (itinerario == null || itinerario.getAeropuertoOrigen() == null || itinerario.getAeropuertoDestino() == null) {
            throw new IllegalArgumentException("El itinerario debe tener aeropuerto de origen y destino definidos.");
        }
        if (tramos == null || tramos.isEmpty()) {
            throw new IllegalArgumentException("Debe vincular al menos un vuelo operativo al itinerario.");
        }
        if (preciosPorTarifa == null || preciosPorTarifa.isEmpty()) {
            throw new IllegalArgumentException("Debe definir precios válidos para las tarifas del itinerario.");
        }

        List<ItinerarioVuelo> tramosOrdenados = new ArrayList<>(tramos);
        tramosOrdenados.sort(Comparator.comparingInt(iv -> iv.getOrden() != null ? iv.getOrden() : 0));

        // Validar vuelos desde BD y continuidad mínima de escala (30 min)
        List<Vuelo> vuelosValidados = new ArrayList<>();
        for (int i = 0; i < tramosOrdenados.size(); i++) {
            Vuelo vRef = tramosOrdenados.get(i).getVuelo();
            if (vRef == null || vRef.getIdVuelo() == null) {
                throw new IllegalArgumentException("Uno de los tramos seleccionados no corresponde a un vuelo guardado.");
            }
            Vuelo vueloDb = entityManager.find(Vuelo.class, vRef.getIdVuelo());
            if (vueloDb == null) {
                throw new IllegalArgumentException("El vuelo ID " + vRef.getIdVuelo() + " ya no existe en base de datos.");
            }
            if (!esVueloOperableParaItinerario(vueloDb)) {
                String est = vueloDb.getEstadoVuelo() != null ? vueloDb.getEstadoVuelo().getDescripcion() : "No operable";
                throw new IllegalStateException("El vuelo " + vueloDb.getNumeroVuelo()
                        + " se encuentra en estado '" + est + "' y no puede formar parte de un itinerario.");
            }
            if (i > 0) {
                Vuelo previo = vuelosValidados.get(i - 1);
                if (previo.getFechaHoraLlegada() != null && vueloDb.getFechaHoraSalida() != null) {
                    long minutosEspera = Duration.between(
                            previo.getFechaHoraLlegada().toInstant(),
                            vueloDb.getFechaHoraSalida().toInstant()
                    ).toMinutes();
                    if (minutosEspera < 30) {
                        throw new IllegalStateException("Escala insuficiente entre " + previo.getNumeroVuelo()
                                + " y " + vueloDb.getNumeroVuelo() + " (" + minutosEspera
                                + " min; mínimo requerido: 30 min).");
                    }
                    if (minutosEspera > maxEsperaMin) {
                        throw new IllegalStateException("Espera excesiva entre " + previo.getNumeroVuelo()
                                + " y " + vueloDb.getNumeroVuelo() + " (" + formatearDuracionMinutos(minutosEspera)
                                + "; máximo permitido: " + maxHorasEspera + " hrs).");
                    }
                }
            }
            vuelosValidados.add(vueloDb);
        }

        boolean bloquearCambioEstructural = false;
        if (esModificacion && itinerario.getIdItinerario() != null) {
            long totalReservas = contarReservasTotalesPorItinerario(itinerario.getIdItinerario());
            bloquearCambioEstructural = totalReservas > 0;

            if (bloquearCambioEstructural) {
                List<ItinerarioVuelo> actuales = itinerarioVueloRepository
                        .findByItinerarioIdItinerarioOrderByOrdenAsc(itinerario.getIdItinerario());
                if (!mismosVuelosEnOrden(actuales, vuelosValidados)) {
                    throw new IllegalStateException("Este itinerario posee " + totalReservas
                            + " reserva(s) asociada(s). Por integridad comercial no se pueden modificar sus tramos; cree un itinerario nuevo.");
                }
                Itinerario persistido = entityManager.find(Itinerario.class, itinerario.getIdItinerario());
                if (persistido != null && (
                        !Objects.equals(persistido.getAeropuertoOrigen().getIdAeropuerto(), itinerario.getAeropuertoOrigen().getIdAeropuerto())
                        || !Objects.equals(persistido.getAeropuertoDestino().getIdAeropuerto(), itinerario.getAeropuertoDestino().getIdAeropuerto()))) {
                    throw new IllegalStateException("No se puede cambiar el origen o destino de un itinerario con reservas registradas.");
                }
            }
        }

        // Sincronizar fechas y escalas; respetar precio base definido manualmente
        if (itinerario.getPrecioBase() == null || itinerario.getPrecioBase() <= 0) {
            throw new IllegalArgumentException("Debe fijar un precio base referencial válido para el itinerario.");
        }
        Vuelo primerVuelo = vuelosValidados.get(0);
        Vuelo ultimoVuelo = vuelosValidados.get(vuelosValidados.size() - 1);
        itinerario.setHoraSalida(primerVuelo.getFechaHoraSalida());
        itinerario.setHoraLlegada(ultimoVuelo.getFechaHoraLlegada());
        itinerario.setNumeroEscalas(Math.max(0, vuelosValidados.size() - 1));

        Itinerario itinerarioGuardado = itinerarioRepository.save(itinerario);

        if (!bloquearCambioEstructural) {
            if (esModificacion) {
                List<ItinerarioVuelo> tramosViejos = itinerarioVueloRepository
                        .findByItinerarioIdItinerario(itinerarioGuardado.getIdItinerario());
                if (tramosViejos != null && !tramosViejos.isEmpty()) {
                    itinerarioVueloRepository.deleteAll(tramosViejos);
                    itinerarioVueloRepository.flush();
                }
            }
            for (int i = 0; i < tramosOrdenados.size(); i++) {
                ItinerarioVuelo ivNuevo = new ItinerarioVuelo();
                ivNuevo.setItinerario(itinerarioGuardado);
                ivNuevo.setVuelo(vuelosValidados.get(i));
                ivNuevo.setOrden(i + 1);
                ivNuevo.setTipoConexion(tramosOrdenados.get(i).getTipoConexion());
                itinerarioVueloRepository.save(ivNuevo);
            }
        }

        // Actualizar tarifas en el mismo registro (preserva IDs referenciados por Reserva_Itinerario)
        for (Map.Entry<Integer, BigDecimal> entry : preciosPorTarifa.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            Tarifa tarifaDb = entityManager.find(Tarifa.class, entry.getKey());
            if (tarifaDb == null) {
                continue;
            }
            ItinerarioTarifa existente = itinerarioTarifaRepository
                    .getByTarifaAndItinerario(itinerarioGuardado.getIdItinerario(), entry.getKey());
            if (existente != null) {
                existente.setPrecio(entry.getValue());
                itinerarioTarifaRepository.save(existente);
            } else {
                ItinerarioTarifa nueva = new ItinerarioTarifa();
                nueva.setItinerario(itinerarioGuardado);
                nueva.setTarifa(tarifaDb);
                nueva.setPrecio(entry.getValue());
                itinerarioTarifaRepository.save(nueva);
            }
        }

        return itinerarioGuardado;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void eliminarItinerarioControlado(Integer idItinerario) {
        if (idItinerario == null || idItinerario <= 0) {
            throw new IllegalArgumentException("Identificador de itinerario inválido.");
        }
        Itinerario itin = itinerarioRepository.findById(idItinerario)
                .orElseThrow(() -> new IllegalArgumentException("El itinerario SKW-ITI-" + idItinerario + " no existe."));

        long totalReservas = contarReservasTotalesPorItinerario(idItinerario);
        if (totalReservas > 0) {
            throw new IllegalStateException("No se puede eliminar SKW-ITI-" + idItinerario
                    + " porque registra " + totalReservas + " reserva(s) asociada(s). "
                    + "Para detener viajes afectados, cancele el vuelo operativo desde Gestión de Vuelos.");
        }

        List<ItinerarioTarifa> tarifas = itinerarioTarifaRepository.findByItinerarioIdItinerario(idItinerario);
        if (tarifas != null && !tarifas.isEmpty()) {
            itinerarioTarifaRepository.deleteAll(tarifas);
        }
        List<ItinerarioVuelo> tramos = itinerarioVueloRepository.findByItinerarioIdItinerario(idItinerario);
        if (tramos != null && !tramos.isEmpty()) {
            itinerarioVueloRepository.deleteAll(tramos);
        }
        itinerarioRepository.delete(itin);
    }

    private boolean esVueloOperableParaItinerario(Vuelo vuelo) {
        if (vuelo == null || vuelo.getEstadoVuelo() == null) {
            return true;
        }
        EstadoVuelo ev = vuelo.getEstadoVuelo();
        String desc = ev.getDescripcion() != null ? ev.getDescripcion().toUpperCase() : "";
        String flag = ev.getEstado() != null ? ev.getEstado().toUpperCase() : "";
        return !desc.contains("CANCEL")
                && !flag.contains("CANCEL")
                && !"INACTIVO".equals(flag)
                && !desc.contains("EN VUELO")
                && !desc.contains("ATERRIZ")
                && !desc.contains("FINALIZ")
                && !"FINALIZADO".equals(flag);
    }

    private boolean mismosVuelosEnOrden(List<ItinerarioVuelo> actuales, List<Vuelo> nuevos) {
        if (actuales == null || nuevos == null || actuales.size() != nuevos.size()) {
            return false;
        }
        for (int i = 0; i < actuales.size(); i++) {
            Integer idActual = actuales.get(i).getVuelo() != null ? actuales.get(i).getVuelo().getIdVuelo() : null;
            Integer idNuevo = nuevos.get(i).getIdVuelo();
            if (!Objects.equals(idActual, idNuevo)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vuelo> buscarVuelosOperables(String origenIata, String destinoIata, LocalDate fechaSalida) {
        return buscarVuelosOperablesPaginados(origenIata, destinoIata, fechaSalida, 0, 200);
    }

    @Override
    @Transactional(readOnly = true)
    public long contarVuelosOperables(String origenIata, String destinoIata, LocalDate fechaSalida) {
        String origFiltro = (origenIata != null && !origenIata.trim().isEmpty()) ? origenIata.trim().toUpperCase() : null;
        String destFiltro = (destinoIata != null && !destinoIata.trim().isEmpty()) ? destinoIata.trim().toUpperCase() : null;

        if (origFiltro == null && destFiltro == null && fechaSalida == null) {
            try {
                Object estObj = entityManager.createNativeQuery(
                        "SELECT reltuples::BIGINT FROM pg_class WHERE relname = 'vuelo' LIMIT 1"
                ).getSingleResult();
                long est = estObj != null ? ((Number) estObj).longValue() : -1L;
                if (est > 10_000L) {
                    return est;
                }
            } catch (Exception ignored) {
            }
        }

        String sql = "SELECT COUNT(*) " + construirFromWhereVuelosOperablesSql(origFiltro, destFiltro, fechaSalida);
        Query q = entityManager.createNativeQuery(sql);
        aplicarParametrosVuelosOperablesSql(q, origFiltro, destFiltro, fechaSalida);
        Object res = q.getSingleResult();
        return res != null ? ((Number) res).longValue() : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Vuelo> buscarVuelosOperablesPaginados(String origenIata, String destinoIata, LocalDate fechaSalida, int first, int pageSize) {
        String origFiltro = (origenIata != null && !origenIata.trim().isEmpty()) ? origenIata.trim().toUpperCase() : null;
        String destFiltro = (destinoIata != null && !destinoIata.trim().isEmpty()) ? destinoIata.trim().toUpperCase() : null;

        String sqlIds = "SELECT v.id_vuelo "
                + construirFromWhereVuelosOperablesSql(origFiltro, destFiltro, fechaSalida)
                + " ORDER BY v.fecha_hora_salida ASC, v.id_vuelo ASC";

        Query qIds = entityManager.createNativeQuery(sqlIds);
        aplicarParametrosVuelosOperablesSql(qIds, origFiltro, destFiltro, fechaSalida);
        qIds.setFirstResult(Math.max(0, first));
        qIds.setMaxResults(Math.max(1, pageSize));

        List<?> rawIds = qIds.getResultList();
        List<Integer> ids = new ArrayList<>(rawIds.size());
        for (Object o : rawIds) {
            if (o instanceof Number n) {
                ids.add(n.intValue());
            }
        }
        return cargarVuelosCompletosPorIdsEnOrden(ids);
    }

    private String construirFromWhereVuelosOperablesSql(String origFiltro, String destFiltro, LocalDate fechaSalida) {
        StringBuilder sb = new StringBuilder(
                "FROM vuelo v " +
                "LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo "
        );
        if (origFiltro != null) {
            sb.append("JOIN segmento_vuelo s1 ON s1.id_vuelo = v.id_vuelo AND s1.orden_segmento = 1 " +
                      "JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen ");
        }
        if (destFiltro != null) {
            sb.append("JOIN segmento_vuelo s2 ON s2.id_vuelo = v.id_vuelo " +
                      "AND NOT EXISTS (SELECT 1 FROM segmento_vuelo s2b WHERE s2b.id_vuelo = v.id_vuelo AND s2b.orden_segmento > s2.orden_segmento) " +
                      "JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino ");
        }
        sb.append(
                "WHERE (ev.id_estado_vuelo IS NULL OR (" +
                "    UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%' " +
                "    AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%' " +
                "    AND UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO' " +
                "    AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%EN VUELO%' " +
                "    AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%ATERRIZ%' " +
                "    AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%FINALIZ%' " +
                "    AND UPPER(COALESCE(ev.estado, '')) <> 'FINALIZADO'" +
                ")) "
        );
        if (origFiltro != null) {
            sb.append("AND ao.codigo_iata = :orig ");
        } else {
            sb.append("AND EXISTS (SELECT 1 FROM segmento_vuelo sCheck WHERE sCheck.id_vuelo = v.id_vuelo) ");
        }
        if (destFiltro != null) {
            sb.append("AND ad.codigo_iata = :dest ");
        }
        if (fechaSalida != null) {
            sb.append("AND v.fecha_hora_salida >= :fechaInicio AND v.fecha_hora_salida < :fechaFin ");
        }
        return sb.toString();
    }

    private void aplicarParametrosVuelosOperablesSql(Query q, String origFiltro, String destFiltro, LocalDate fechaSalida) {
        if (origFiltro != null) {
            q.setParameter("orig", origFiltro);
        }
        if (destFiltro != null) {
            q.setParameter("dest", destFiltro);
        }
        if (fechaSalida != null) {
            ZoneId zona = ZoneId.of("America/Santiago");
            q.setParameter("fechaInicio", Timestamp.from(fechaSalida.atStartOfDay(zona).toInstant()));
            q.setParameter("fechaFin", Timestamp.from(fechaSalida.plusDays(1).atStartOfDay(zona).toInstant()));
        }
    }

    private List<Vuelo> cargarVuelosCompletosPorIdsEnOrden(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Vuelo> cargados = entityManager.createQuery(
                "SELECT DISTINCT v FROM Vuelo v " +
                "LEFT JOIN FETCH v.estadoVuelo " +
                "LEFT JOIN FETCH v.avion av " +
                "LEFT JOIN FETCH av.modeloAvion " +
                "LEFT JOIN FETCH v.piloto pil " +
                "LEFT JOIN FETCH pil.usuario " +
                "LEFT JOIN FETCH v.segmentoVuelos sv " +
                "LEFT JOIN FETCH sv.aeropuertoOrigen " +
                "LEFT JOIN FETCH sv.aeropuertoDestino " +
                "WHERE v.idVuelo IN :ids",
                Vuelo.class
        ).setParameter("ids", ids).getResultList();

        Map<Integer, Vuelo> porId = new LinkedHashMap<>();
        for (Vuelo v : cargados) {
            v.setSegmentoVuelos(obtenerSegmentosOrdenados(v));
            porId.put(v.getIdVuelo(), v);
        }
        List<Vuelo> ordenados = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            Vuelo v = porId.get(id);
            if (v != null) {
                ordenados.add(v);
            }
        }
        return ordenados;
    }

    @Override
    @Transactional(readOnly = true)
    public long contarItinerariosPaginados(String filtroGlobal) {
        String term = (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) ? filtroGlobal.trim().toLowerCase() : null;
        if (term == null) {
            try {
                Object estObj = entityManager.createNativeQuery(
                        "SELECT reltuples::BIGINT FROM pg_class WHERE relname = 'itinerario' LIMIT 1"
                ).getSingleResult();
                long est = estObj != null ? ((Number) estObj).longValue() : -1L;
                if (est > 10_000L) {
                    return est;
                }
            } catch (Exception ignored) {
            }
            Long exact = entityManager.createQuery("SELECT COUNT(i.idItinerario) FROM Itinerario i", Long.class)
                    .getSingleResult();
            return exact != null ? exact : 0L;
        }
        String jpql = "SELECT COUNT(DISTINCT i.idItinerario) FROM Itinerario i " +
                "LEFT JOIN i.aeropuertoOrigen ao " +
                "LEFT JOIN i.aeropuertoDestino ad WHERE 1=1 " + construirWhereItinerario();
        Long res = entityManager.createQuery(jpql, Long.class)
                .setParameter("term", "%" + term + "%")
                .getSingleResult();
        return res != null ? res : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Itinerario> buscarItinerariosPaginados(int first, int pageSize, String filtroGlobal, String sortField, boolean asc) {
        String term = (filtroGlobal != null && !filtroGlobal.trim().isEmpty()) ? filtroGlobal.trim().toLowerCase() : null;
        String orderCol = resolverColumnaOrdenItinerario(sortField);
        String orderDir = asc ? "ASC" : "DESC";

        StringBuilder jpqlIds;
        if (term == null) {
            jpqlIds = new StringBuilder("SELECT i.idItinerario FROM Itinerario i");
        } else {
            jpqlIds = new StringBuilder(
                    "SELECT i.idItinerario FROM Itinerario i " +
                    "LEFT JOIN i.aeropuertoOrigen ao " +
                    "LEFT JOIN i.aeropuertoDestino ad WHERE 1=1 "
            ).append(construirWhereItinerario());
        }
        jpqlIds.append(" ORDER BY ").append(orderCol).append(" ").append(orderDir).append(", i.idItinerario DESC");

        jakarta.persistence.TypedQuery<Integer> qIds = entityManager.createQuery(jpqlIds.toString(), Integer.class);
        if (term != null) {
            qIds.setParameter("term", "%" + term + "%");
        }
        qIds.setFirstResult(Math.max(0, first));
        qIds.setMaxResults(Math.max(1, pageSize));

        List<Integer> ids = qIds.getResultList();
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<Itinerario> cargados = entityManager.createQuery(
                "SELECT DISTINCT i FROM Itinerario i " +
                "LEFT JOIN FETCH i.aeropuertoOrigen " +
                "LEFT JOIN FETCH i.aeropuertoDestino " +
                "WHERE i.idItinerario IN :ids",
                Itinerario.class
        ).setParameter("ids", ids).getResultList();

        Map<Integer, Itinerario> porId = new LinkedHashMap<>();
        for (Itinerario it : cargados) {
            porId.put(it.getIdItinerario(), it);
        }
        List<Itinerario> ordenados = new ArrayList<>(ids.size());
        for (Integer id : ids) {
            Itinerario it = porId.get(id);
            if (it != null) {
                ordenados.add(it);
            }
        }
        return ordenados;
    }

    private String construirWhereItinerario() {
        return " AND (" +
                "CAST(i.idItinerario AS string) LIKE :term " +
                "OR CONCAT('skw-iti-', CAST(i.idItinerario AS string)) LIKE :term " +
                "OR LOWER(COALESCE(ao.codigoIata, '')) LIKE :term " +
                "OR LOWER(COALESCE(ao.nombreAeropuerto, '')) LIKE :term " +
                "OR LOWER(COALESCE(ad.codigoIata, '')) LIKE :term " +
                "OR LOWER(COALESCE(ad.nombreAeropuerto, '')) LIKE :term" +
                ")";
    }

    private String resolverColumnaOrdenItinerario(String sortField) {
        if ("numeroEscalas".equals(sortField)) return "i.numeroEscalas";
        if ("precioBase".equals(sortField)) return "i.precioBase";
        return "i.idItinerario";
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Integer, Long> obtenerMapaAsientosVendidosPorVuelo() {
        List<Object[]> filas = entityManager.createNativeQuery(
                "SELECT id_vuelo, COUNT(*) FROM reserva_asiento GROUP BY id_vuelo"
        ).getResultList();
        Map<Integer, Long> mapa = new HashMap<>();
        for (Object[] fila : filas) {
            if (fila[0] != null && fila[1] != null) {
                mapa.put(((Number) fila[0]).intValue(), ((Number) fila[1]).longValue());
            }
        }
        return mapa;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Map<Integer, Long> obtenerMapaAsientosVendidosPorVuelos(List<Integer> idsVuelos) {
        Map<Integer, Long> mapa = new HashMap<>();
        if (idsVuelos == null || idsVuelos.isEmpty()) {
            return mapa;
        }
        List<Object[]> filas = entityManager.createNativeQuery(
                "SELECT id_vuelo, COUNT(*) FROM reserva_asiento WHERE id_vuelo IN (:ids) GROUP BY id_vuelo"
        ).setParameter("ids", idsVuelos).getResultList();
        for (Object[] fila : filas) {
            if (fila[0] != null && fila[1] != null) {
                mapa.put(((Number) fila[0]).intValue(), ((Number) fila[1]).longValue());
            }
        }
        return mapa;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CombinacionRutaSugeridaDTO> sugerirCombinacionesRuta(String origenIata, String destinoIata, String criterioOrden) {
        return sugerirCombinacionesRuta(origenIata, destinoIata, criterioOrden, 10);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CombinacionRutaSugeridaDTO> sugerirCombinacionesRuta(String origenIata, String destinoIata, String criterioOrden, int maxHorasEspera) {
        if (origenIata == null || origenIata.isBlank() || destinoIata == null || destinoIata.isBlank()
                || origenIata.equalsIgnoreCase(destinoIata)) {
            return new ArrayList<>();
        }
        long maxEsperaMin = Math.max(1, maxHorasEspera) * 60L;

        String origObjetivo = origenIata.trim().toUpperCase();
        String destObjetivo = destinoIata.trim().toUpperCase();

        // Proyección SQL ultraliviana en ventana acotada (sin hidratar entidades JPA de 1M+ vuelos)
        Timestamp ventanaDesde = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        Timestamp ventanaHasta = Timestamp.valueOf(LocalDateTime.now().plusDays(30));

        List<Object[]> filasGrafo = consultarAristasVuelosVentana(origObjetivo, destObjetivo, ventanaDesde, ventanaHasta);
        if (filasGrafo.isEmpty()) {
            // Respaldo acotado si todos los datos simulados están fuera de la ventana de 30 días
            filasGrafo = consultarAristasVuelosRecientes(origObjetivo, destObjetivo);
        }
        if (filasGrafo.isEmpty()) {
            return new ArrayList<>();
        }

        List<AristaVuelo> aristas = new ArrayList<>(filasGrafo.size());
        List<Integer> idsPool = new ArrayList<>(filasGrafo.size());
        Map<String, List<AristaVuelo>> grafoPorOrigen = new HashMap<>();

        for (Object[] f : filasGrafo) {
            Integer idVuelo = ((Number) f[0]).intValue();
            String numVuelo = f[1] != null ? f[1].toString() : "#" + idVuelo;
            Timestamp salida = f[2] instanceof Timestamp ts ? ts : Timestamp.valueOf(f[2].toString());
            Timestamp llegada = f[3] instanceof Timestamp ts ? ts : Timestamp.valueOf(f[3].toString());
            String orig = f[4].toString().toUpperCase();
            String dest = f[5].toString().toUpperCase();
            int cap = f[6] != null ? ((Number) f[6]).intValue() : 0;

            AristaVuelo arista = new AristaVuelo(idVuelo, numVuelo, salida, llegada, orig, dest, cap);
            aristas.add(arista);
            idsPool.add(idVuelo);
            grafoPorOrigen.computeIfAbsent(orig, k -> new ArrayList<>()).add(arista);
        }

        Map<Integer, Long> vendidosPorVuelo = obtenerMapaAsientosVendidosPorVuelos(idsPool);
        List<List<AristaVuelo>> caminosGanadores = new ArrayList<>();
        Set<String> visitados = new HashSet<>();
        visitados.add(origObjetivo);

        buscarPermutacionesAristasDFS(
                origObjetivo,
                destObjetivo,
                grafoPorOrigen,
                new ArrayList<>(),
                visitados,
                caminosGanadores,
                4,
                80,
                maxEsperaMin
        );

        if (caminosGanadores.isEmpty()) {
            return new ArrayList<>();
        }

        List<CombinacionRutaSugeridaDTO> dtos = new ArrayList<>(caminosGanadores.size());
        for (List<AristaVuelo> camino : caminosGanadores) {
            dtos.add(construirDtoDesdeAristas(camino, vendidosPorVuelo));
        }

        String crit = criterioOrden != null ? criterioOrden.trim().toUpperCase() : "OPORTUNIDAD";
        if ("DURACION".equals(crit)) {
            dtos.sort(Comparator
                    .comparingLong(CombinacionRutaSugeridaDTO::getDuracionTotalMinutos)
                    .thenComparingInt(CombinacionRutaSugeridaDTO::getNumeroEscalas));
        } else if ("ESCALAS".equals(crit)) {
            dtos.sort(Comparator
                    .comparingInt(CombinacionRutaSugeridaDTO::getNumeroEscalas)
                    .thenComparingLong(CombinacionRutaSugeridaDTO::getDuracionTotalMinutos));
        } else {
            dtos.sort(Comparator
                    .comparingInt(CombinacionRutaSugeridaDTO::getOcupacionPromedioPct)
                    .thenComparingInt(CombinacionRutaSugeridaDTO::getNumeroEscalas)
                    .thenComparingLong(CombinacionRutaSugeridaDTO::getDuracionTotalMinutos));
        }

        if (dtos.size() > 15) {
            dtos = new ArrayList<>(dtos.subList(0, 15));
        }

        // Hidratar en 1 sola consulta batch únicamente los Vuelo ganadores (máx ~20-30 filas)
        Set<Integer> idsGanadores = new HashSet<>();
        for (CombinacionRutaSugeridaDTO dto : dtos) {
            for (Vuelo vStub : dto.getVuelos()) {
                idsGanadores.add(vStub.getIdVuelo());
            }
        }
        List<Vuelo> vuelosHidratados = cargarVuelosCompletosPorIdsEnOrden(new ArrayList<>(idsGanadores));
        Map<Integer, Vuelo> mapaHidratado = new HashMap<>();
        for (Vuelo vh : vuelosHidratados) {
            mapaHidratado.put(vh.getIdVuelo(), vh);
        }
        for (CombinacionRutaSugeridaDTO dto : dtos) {
            List<Vuelo> reales = new ArrayList<>(dto.getVuelos().size());
            for (Vuelo vStub : dto.getVuelos()) {
                Vuelo real = mapaHidratado.get(vStub.getIdVuelo());
                if (real != null) {
                    reales.add(real);
                }
            }
            dto.setVuelos(reales);
        }
        return dtos;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> consultarAristasVuelosVentana(String origObjetivo, String destObjetivo, Timestamp desde, Timestamp hasta) {
        String sql = """
            WITH primeros_tramos AS (
                SELECT DISTINCT
                    v.id_vuelo, v.numero_vuelo, v.fecha_hora_salida, v.fecha_hora_llegada,
                    ao.codigo_iata AS orig_iata, ad.codigo_iata AS dest_iata,
                    COALESCE(av.capacidad_de_pasajeros, 0) AS capacidad
                FROM vuelo v
                LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
                LEFT JOIN avion av ON av.id_avion = v.id_avion
                JOIN segmento_vuelo s1 ON s1.id_vuelo = v.id_vuelo AND s1.orden_segmento = 1
                JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
                JOIN segmento_vuelo s2 ON s2.id_vuelo = v.id_vuelo
                     AND NOT EXISTS (SELECT 1 FROM segmento_vuelo s2b WHERE s2b.id_vuelo = v.id_vuelo AND s2b.orden_segmento > s2.orden_segmento)
                JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
                WHERE ao.codigo_iata = :orig
                  AND v.fecha_hora_salida >= :desde
                  AND v.fecha_hora_salida <= :hasta
                  AND (ev.id_estado_vuelo IS NULL OR (
                      UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%'
                      AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%'
                      AND UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO'
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%EN VUELO%'
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%ATERRIZ%'
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%FINALIZ%'
                      AND UPPER(COALESCE(ev.estado, '')) <> 'FINALIZADO'
                  ))
                ORDER BY v.fecha_hora_salida ASC
                LIMIT 80
            ),
            escalas_intermedias AS (
                SELECT DISTINCT dest_iata AS iata FROM primeros_tramos WHERE dest_iata <> :dest
            ),
            segundos_tramos AS (
                SELECT DISTINCT
                    v.id_vuelo, v.numero_vuelo, v.fecha_hora_salida, v.fecha_hora_llegada,
                    ao.codigo_iata AS orig_iata, ad.codigo_iata AS dest_iata,
                    COALESCE(av.capacidad_de_pasajeros, 0) AS capacidad
                FROM vuelo v
                LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
                LEFT JOIN avion av ON av.id_avion = v.id_avion
                JOIN segmento_vuelo s1 ON s1.id_vuelo = v.id_vuelo AND s1.orden_segmento = 1
                JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
                JOIN segmento_vuelo s2 ON s2.id_vuelo = v.id_vuelo
                     AND NOT EXISTS (SELECT 1 FROM segmento_vuelo s2b WHERE s2b.id_vuelo = v.id_vuelo AND s2b.orden_segmento > s2.orden_segmento)
                JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
                WHERE (ao.codigo_iata IN (SELECT iata FROM escalas_intermedias) OR ad.codigo_iata = :dest)
                  AND v.fecha_hora_salida >= :desde
                  AND v.fecha_hora_salida <= :hasta
                  AND (ev.id_estado_vuelo IS NULL OR (
                      UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%'
                      AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%'
                      AND UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO'
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%EN VUELO%'
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%ATERRIZ%'
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%FINALIZ%'
                      AND UPPER(COALESCE(ev.estado, '')) <> 'FINALIZADO'
                  ))
                ORDER BY v.fecha_hora_salida ASC
                LIMIT 180
            )
            SELECT * FROM primeros_tramos
            UNION
            SELECT * FROM segundos_tramos
            """;
        return entityManager.createNativeQuery(sql)
                .setParameter("orig", origObjetivo)
                .setParameter("dest", destObjetivo)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> consultarAristasVuelosRecientes(String origObjetivo, String destObjetivo) {
        String sql = """
            (SELECT
                v.id_vuelo, v.numero_vuelo, v.fecha_hora_salida, v.fecha_hora_llegada,
                ao.codigo_iata AS orig_iata, ad.codigo_iata AS dest_iata,
                COALESCE(av.capacidad_de_pasajeros, 0) AS capacidad
            FROM vuelo v
            LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
            LEFT JOIN avion av ON av.id_avion = v.id_avion
            JOIN segmento_vuelo s1 ON s1.id_vuelo = v.id_vuelo AND s1.orden_segmento = 1
            JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
            JOIN segmento_vuelo s2 ON s2.id_vuelo = v.id_vuelo
                 AND NOT EXISTS (SELECT 1 FROM segmento_vuelo s2b WHERE s2b.id_vuelo = v.id_vuelo AND s2b.orden_segmento > s2.orden_segmento)
            JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
            WHERE ao.codigo_iata = :orig
              AND (ev.id_estado_vuelo IS NULL OR (UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO' AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%'))
            ORDER BY v.fecha_hora_salida DESC
            LIMIT 60)
            UNION
            (SELECT
                v.id_vuelo, v.numero_vuelo, v.fecha_hora_salida, v.fecha_hora_llegada,
                ao.codigo_iata AS orig_iata, ad.codigo_iata AS dest_iata,
                COALESCE(av.capacidad_de_pasajeros, 0) AS capacidad
            FROM vuelo v
            LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
            LEFT JOIN avion av ON av.id_avion = v.id_avion
            JOIN segmento_vuelo s1 ON s1.id_vuelo = v.id_vuelo AND s1.orden_segmento = 1
            JOIN aeropuerto ao ON ao.id_aeropuerto = s1.id_aeropuerto_origen
            JOIN segmento_vuelo s2 ON s2.id_vuelo = v.id_vuelo
                 AND NOT EXISTS (SELECT 1 FROM segmento_vuelo s2b WHERE s2b.id_vuelo = v.id_vuelo AND s2b.orden_segmento > s2.orden_segmento)
            JOIN aeropuerto ad ON ad.id_aeropuerto = s2.id_aeropuerto_destino
            WHERE ad.codigo_iata = :dest
              AND (ev.id_estado_vuelo IS NULL OR (UPPER(COALESCE(ev.estado, '')) <> 'INACTIVO' AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%'))
            ORDER BY v.fecha_hora_salida DESC
            LIMIT 120)
            """;
        return entityManager.createNativeQuery(sql)
                .setParameter("orig", origObjetivo)
                .setParameter("dest", destObjetivo)
                .getResultList();
    }

    private record AristaVuelo(
            Integer idVuelo,
            String numeroVuelo,
            Timestamp fechaHoraSalida,
            Timestamp fechaHoraLlegada,
            String origIata,
            String destIata,
            int capacidad
    ) {}

    private void buscarPermutacionesAristasDFS(
            String aeropuertoActual,
            String destinoObjetivo,
            Map<String, List<AristaVuelo>> grafoPorOrigen,
            List<AristaVuelo> caminoActual,
            Set<String> visitados,
            List<List<AristaVuelo>> resultados,
            int maxTramos,
            int maxResultadosBrutos,
            long maxEsperaMin) {

        if (resultados.size() >= maxResultadosBrutos || caminoActual.size() >= maxTramos) {
            return;
        }
        List<AristaVuelo> salientes = grafoPorOrigen.get(aeropuertoActual);
        if (salientes == null || salientes.isEmpty()) {
            return;
        }
        AristaVuelo anterior = caminoActual.isEmpty() ? null : caminoActual.get(caminoActual.size() - 1);

        for (AristaVuelo cand : salientes) {
            if (resultados.size() >= maxResultadosBrutos) {
                break;
            }
            if (anterior != null) {
                long esperaMin = Duration.between(
                        anterior.fechaHoraLlegada().toInstant(),
                        cand.fechaHoraSalida().toInstant()
                ).toMinutes();
                if (esperaMin < 30 || esperaMin > maxEsperaMin) {
                    continue;
                }
            }
            if (visitados.contains(cand.destIata())) {
                continue;
            }
            caminoActual.add(cand);
            if (destinoObjetivo.equals(cand.destIata())) {
                resultados.add(new ArrayList<>(caminoActual));
            } else if (caminoActual.size() < maxTramos) {
                visitados.add(cand.destIata());
                buscarPermutacionesAristasDFS(cand.destIata(), destinoObjetivo, grafoPorOrigen,
                        caminoActual, visitados, resultados, maxTramos, maxResultadosBrutos, maxEsperaMin);
                visitados.remove(cand.destIata());
            }
            caminoActual.remove(caminoActual.size() - 1);
        }
    }

    private CombinacionRutaSugeridaDTO construirDtoDesdeAristas(List<AristaVuelo> camino, Map<Integer, Long> vendidosPorVuelo) {
        CombinacionRutaSugeridaDTO dto = new CombinacionRutaSugeridaDTO();
        List<Vuelo> stubs = new ArrayList<>(camino.size());
        for (AristaVuelo a : camino) {
            Vuelo stub = new Vuelo();
            stub.setIdVuelo(a.idVuelo());
            stubs.add(stub);
        }
        dto.setVuelos(stubs);
        dto.setNumeroTramos(camino.size());
        dto.setNumeroEscalas(Math.max(0, camino.size() - 1));

        StringBuilder sbCodigos = new StringBuilder();
        StringBuilder sbRuta = new StringBuilder();
        DateTimeFormatter fmtHora = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");
        ZoneId zona = ZoneId.of("America/Santiago");

        int capMin = Integer.MAX_VALUE;
        int libresMin = Integer.MAX_VALUE;
        int sumaVendidos = 0;
        int sumaPct = 0;
        int maxPct = 0;

        for (int i = 0; i < camino.size(); i++) {
            AristaVuelo a = camino.get(i);
            if (i > 0) {
                sbCodigos.append(" → ");
                AristaVuelo prev = camino.get(i - 1);
                long esperaMin = Duration.between(prev.fechaHoraLlegada().toInstant(), a.fechaHoraSalida().toInstant()).toMinutes();
                sbRuta.append(" → ").append(a.origIata()).append(" (").append(formatearDuracionMinutos(esperaMin)).append(")");
            } else {
                sbRuta.append(a.origIata());
            }
            sbCodigos.append(a.numeroVuelo());
            if (i == camino.size() - 1) {
                sbRuta.append(" → ").append(a.destIata());
            }

            int cap = a.capacidad();
            int vendidos = vendidosPorVuelo.getOrDefault(a.idVuelo(), 0L).intValue();
            int libres = Math.max(0, cap - vendidos);
            int pct = cap > 0 ? Math.min(100, (int) Math.round((vendidos * 100.0) / cap)) : 0;

            if (cap > 0 && cap < capMin) capMin = cap;
            if (cap > 0 && libres < libresMin) libresMin = libres;
            sumaVendidos += vendidos;
            sumaPct += pct;
            if (pct > maxPct) maxPct = pct;
        }

        AristaVuelo primero = camino.get(0);
        AristaVuelo ultimo = camino.get(camino.size() - 1);
        long duracionMin = Math.max(0L, Duration.between(
                primero.fechaHoraSalida().toInstant(),
                ultimo.fechaHoraLlegada().toInstant()
        ).toMinutes());

        dto.setSecuenciaCodigos(sbCodigos.toString());
        dto.setRutaEscalas(sbRuta.toString());
        dto.setDuracionTotalMinutos(duracionMin);
        dto.setDuracionTotalTexto(formatearDuracionMinutos(duracionMin));
        dto.setHorarioSalidaTexto(primero.fechaHoraSalida().toInstant().atZone(zona).format(fmtHora));
        dto.setHorarioLlegadaTexto(ultimo.fechaHoraLlegada().toInstant().atZone(zona).format(fmtHora));

        dto.setCapacidadMinima(capMin == Integer.MAX_VALUE ? 0 : capMin);
        dto.setAsientosVendidosTotal(sumaVendidos);
        dto.setAsientosLibresMinimos(libresMin == Integer.MAX_VALUE ? 0 : libresMin);
        int promPct = camino.isEmpty() ? 0 : (int) Math.round((double) sumaPct / camino.size());
        dto.setOcupacionPromedioPct(promPct);
        dto.setOcupacionMaximaPct(maxPct);

        if (dto.getAsientosLibresMinimos() <= 10 && dto.getCapacidadMinima() > 0) {
            dto.setNivelEstrategia("CRITICA");
            dto.setEtiquetaEstrategia("Pocos cupos (" + dto.getAsientosLibresMinimos() + " disp.)");
        } else if (promPct < 45 && dto.getAsientosLibresMinimos() >= 20) {
            dto.setNivelEstrategia("OPORTUNIDAD");
            dto.setEtiquetaEstrategia("Mayor disponibilidad");
        } else {
            dto.setNivelEstrategia("BALANCEADA");
            dto.setEtiquetaEstrategia("Disponibilidad media");
        }

        return dto;
    }

    private List<SegmentoVuelo> obtenerSegmentosOrdenados(Vuelo v) {
        if (v == null || v.getSegmentoVuelos() == null) {
            return new ArrayList<>();
        }
        List<SegmentoVuelo> segs = new ArrayList<>(v.getSegmentoVuelos());
        segs.sort(Comparator.comparingInt(s -> s.getOrdenSegmento() != null ? s.getOrdenSegmento() : 0));
        return segs;
    }

    private String formatearDuracionMinutos(long mins) {
        if (mins <= 0) return "0m";
        long h = mins / 60;
        long m = mins % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }

}
