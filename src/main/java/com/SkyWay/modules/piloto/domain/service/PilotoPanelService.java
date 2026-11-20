package com.SkyWay.modules.piloto.domain.service;

import com.SkyWay.modules.piloto.presentation.dto.PilotoPanelDtos.*;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.BloqueServicio;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.IntervaloServicioInfo;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.ResultadoAnalisisFatiga;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.ResumenFatigaDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Servicio de consultas operacionales reales para el Panel del Piloto.
 * Toda métrica, conteo de pasajeros, check-in, equipaje, flota, tramos y tripulación
 * proviene directamente de las tablas de PostgreSQL V3 sin valores ficticios.
 */
@Service
public class PilotoPanelService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public PanelPilotoDataDTO cargarPanelPiloto(String rutPiloto) {
        if (rutPiloto == null || rutPiloto.isBlank()) {
            return new PanelPilotoDataDTO(kpisVacios(), null, List.of(), List.of(), List.of(),
                    FatigaDescansoHelper.analizar(List.of()).getResumen());
        }

        List<VueloResumenPilotoDTO> vuelos = consultarVuelosPiloto(rutPiloto);
        ResumenFatigaDTO fatiga = aplicarAnalisisFatiga(vuelos);
        KpisPilotoDTO kpis = construirKpisReales(vuelos, rutPiloto);
        VueloResumenPilotoDTO proximoVuelo = seleccionarProximoVuelo(vuelos);
        List<FlotaPilotoDTO> flota = construirFlotaDesdeVuelos(vuelos);
        List<NotificacionPilotoDTO> notificaciones = consultarNotificaciones(rutPiloto);

        return new PanelPilotoDataDTO(kpis, proximoVuelo, vuelos, flota, notificaciones, fatiga);
    }

    private ResumenFatigaDTO aplicarAnalisisFatiga(List<VueloResumenPilotoDTO> vuelos) {
        List<BloqueServicio> activos = vuelos.stream()
                .filter(v -> !v.isEsCancelado() && v.getFechaHoraSalida() != null && v.getFechaHoraLlegada() != null)
                .map(v -> new BloqueServicio(v.getIdVuelo(), v.getNumeroVuelo(), v.getFechaHoraSalida(), v.getFechaHoraLlegada()))
                .toList();
        ResultadoAnalisisFatiga analisis = FatigaDescansoHelper.analizar(activos);
        for (VueloResumenPilotoDTO v : vuelos) {
            IntervaloServicioInfo info = analisis.getPorId().get(v.getIdVuelo());
            if (info != null) {
                v.setIntervaloPrevio(info);
            }
        }
        return analisis.getResumen();
    }

    @Transactional(readOnly = true)
    public BriefingVueloDTO cargarBriefingVuelo(String rutPiloto, Integer idVuelo) {
        if (rutPiloto == null || rutPiloto.isBlank() || idVuelo == null) {
            return null;
        }

        VueloResumenPilotoDTO resumen = consultarVueloPorIdYPiloto(rutPiloto, idVuelo);
        if (resumen == null) {
            return null;
        }

        return new BriefingVueloDTO(
                resumen,
                consultarTramosVuelo(idVuelo),
                consultarOcupacionPorClase(idVuelo, resumen.getIdAvion()),
                consultarEquipajePorTipo(idVuelo),
                consultarTripulacionVuelo(idVuelo),
                consultarManifiestoPasajeros(idVuelo)
        );
    }

    @Transactional
    public boolean marcarNotificacionLeida(String rutPiloto, Integer idNotificacion) {
        if (rutPiloto == null || idNotificacion == null) {
            return false;
        }
        int updated = em.createNativeQuery(
                        "UPDATE notificacion SET leido = TRUE WHERE id_notificacion = :id AND rut_destinatario = :rut")
                .setParameter("id", idNotificacion)
                .setParameter("rut", rutPiloto)
                .executeUpdate();
        return updated > 0;
    }

    @Transactional
    public int marcarTodasNotificacionesLeidas(String rutPiloto) {
        if (rutPiloto == null || rutPiloto.isBlank()) {
            return 0;
        }
        return em.createNativeQuery(
                        "UPDATE notificacion SET leido = TRUE WHERE rut_destinatario = :rut AND COALESCE(leido, FALSE) = FALSE")
                .setParameter("rut", rutPiloto)
                .executeUpdate();
    }

    private List<VueloResumenPilotoDTO> consultarVuelosPiloto(String rutPiloto) {
        return ejecutarConsultaVuelos(rutPiloto, null);
    }

    private VueloResumenPilotoDTO consultarVueloPorIdYPiloto(String rutPiloto, Integer idVuelo) {
        List<VueloResumenPilotoDTO> lista = ejecutarConsultaVuelos(rutPiloto, idVuelo);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @SuppressWarnings("unchecked")
    private List<VueloResumenPilotoDTO> ejecutarConsultaVuelos(String rutPiloto, Integer idVueloFiltro) {
        String sql = """
                SELECT
                    v.id_vuelo,
                    v.numero_vuelo,
                    v.fecha_hora_salida,
                    v.fecha_hora_llegada,
                    COALESCE(ev.descripcion, 'Sin estado') AS estado_desc,
                    COALESCE(ev.estado, '') AS estado_cod,
                    COALESCE(al.nombre, 'SkyWay') AS aerolinea_nombre,
                    COALESCE(al.codigo, '') AS aerolinea_cod,
                    orig.iata AS orig_iata,
                    orig.ciudad AS orig_ciudad,
                    orig.aeropuerto AS orig_aeropuerto,
                    orig.pais AS orig_pais,
                    orig.puerta AS orig_puerta,
                    orig.terminal AS orig_terminal,
                    dest.iata AS dest_iata,
                    dest.ciudad AS dest_ciudad,
                    dest.aeropuerto AS dest_aeropuerto,
                    dest.pais AS dest_pais,
                    COALESCE(seg_agg.tramos, 0) AS tramos,
                    COALESCE(seg_agg.distancia_km, 0.0) AS distancia_km,
                    av.id_avion,
                    COALESCE(av.numero_de_registro, 'Por asignar') AS matricula,
                    COALESCE(fb.nombre, '') AS fabricante,
                    COALESCE(ma.nombre, 'Por asignar') AS modelo,
                    av.ano_de_fabricacion,
                    COALESCE(av.capacidad_de_pasajeros, 0) AS cap_pax,
                    COALESCE(av.capacidad_de_carga, 0) AS cap_carga,
                    COALESCE(av.estado_de_mantenimiento, 'No informado') AS mant_estado,
                    av.fecha_proximo_mantenimiento,
                    COALESCE(pax_agg.pax_reservados, 0) AS pax_reservados,
                    COALESCE(pax_agg.pax_checkin, 0) AS pax_checkin,
                    COALESCE(bag_agg.piezas, 0) AS piezas_equipaje,
                    COALESCE(bag_agg.peso_kg, 0.0) AS peso_equipaje_kg,
                    COALESCE(crew_agg.total_crew, 0) AS crew_total,
                    COALESCE(crew_agg.confirmados, 0) AS crew_conf,
                    COALESCE(crew_agg.pendientes, 0) AS crew_pend,
                    COALESCE(crew_agg.rechazados, 0) AS crew_rech
                FROM vuelo v
                LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
                LEFT JOIN aerolinea al ON al.id_aerolinea = v.id_aerolinea
                LEFT JOIN avion av ON av.id_avion = v.id_avion
                LEFT JOIN modelo_avion ma ON ma.id_modelo = av.id_modelo
                LEFT JOIN fabricante fb ON fb.id_fabricante = ma.id_fabricante
                LEFT JOIN LATERAL (
                    SELECT
                        ao.codigo_iata AS iata,
                        co.nombre AS ciudad,
                        ao.nombre_aeropuerto AS aeropuerto,
                        po.nombre AS pais,
                        pe.codigo_puerta AS puerta,
                        pe.terminal AS terminal
                    FROM segmento_vuelo sv
                    LEFT JOIN aeropuerto ao ON ao.id_aeropuerto = sv.id_aeropuerto_origen
                    LEFT JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
                    LEFT JOIN pais po ON po.id_pais = co.id_pais
                    LEFT JOIN asignacion_puerta ap ON ap.id_segmento = sv.id_segmento
                    LEFT JOIN puerta_embarque pe ON pe.id_puerta = ap.id_puerta
                    WHERE sv.id_vuelo = v.id_vuelo
                    ORDER BY sv.orden_segmento ASC NULLS LAST, sv.id_segmento ASC
                    LIMIT 1
                ) orig ON TRUE
                LEFT JOIN LATERAL (
                    SELECT
                        ad.codigo_iata AS iata,
                        cd.nombre AS ciudad,
                        ad.nombre_aeropuerto AS aeropuerto,
                        pd.nombre AS pais
                    FROM segmento_vuelo sv
                    LEFT JOIN aeropuerto ad ON ad.id_aeropuerto = sv.id_aeropuerto_destino
                    LEFT JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
                    LEFT JOIN pais pd ON pd.id_pais = cd.id_pais
                    WHERE sv.id_vuelo = v.id_vuelo
                    ORDER BY sv.orden_segmento DESC NULLS LAST, sv.id_segmento DESC
                    LIMIT 1
                ) dest ON TRUE
                LEFT JOIN LATERAL (
                    SELECT
                        COUNT(*)::int AS tramos,
                        COALESCE(ROUND(SUM(ST_Distance(ao2.posicion, ad2.posicion) / 1000.0)::numeric, 1), 0.0) AS distancia_km
                    FROM segmento_vuelo sv2
                    LEFT JOIN aeropuerto ao2 ON ao2.id_aeropuerto = sv2.id_aeropuerto_origen
                    LEFT JOIN aeropuerto ad2 ON ad2.id_aeropuerto = sv2.id_aeropuerto_destino
                    WHERE sv2.id_vuelo = v.id_vuelo
                ) seg_agg ON TRUE
                LEFT JOIN LATERAL (
                    SELECT
                        COUNT(DISTINCT ra.id_reserva_asiento)::int AS pax_reservados,
                        COUNT(DISTINCT CASE WHEN EXISTS (
                            SELECT 1
                            FROM checkin ch
                            LEFT JOIN checkin_pasajero cp ON cp.id_checkin = ch.id_checkin
                            WHERE ch.id_reserva = ra.id_reserva
                              AND UPPER(COALESCE(ch.estado, '')) = 'COMPLETADO'
                              AND (cp.rut = ra.rut OR NOT EXISTS (
                                  SELECT 1 FROM checkin_pasajero cp2 WHERE cp2.id_checkin = ch.id_checkin
                              ))
                        ) THEN ra.id_reserva_asiento END)::int AS pax_checkin
                    FROM reserva_asiento ra
                    JOIN reserva r ON r.id_reserva = ra.id_reserva
                    LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                    WHERE ra.id_vuelo = v.id_vuelo
                      AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%'
                ) pax_agg ON TRUE
                LEFT JOIN LATERAL (
                    SELECT
                        COUNT(*)::int AS piezas,
                        COALESCE(SUM(eq.peso), 0.0)::double precision AS peso_kg
                    FROM equipaje eq
                    WHERE EXISTS (
                        SELECT 1
                        FROM reserva_asiento ra
                        JOIN reserva r ON r.id_reserva = ra.id_reserva
                        LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                        WHERE ra.id_vuelo = v.id_vuelo
                          AND ra.id_reserva = eq.id_reserva
                          AND ra.rut = eq.rut_pasajero
                          AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%'
                    )
                ) bag_agg ON TRUE
                LEFT JOIN LATERAL (
                    SELECT
                        COUNT(*)::int AS total_crew,
                        COUNT(*) FILTER (WHERE UPPER(COALESCE(tt.estado, '')) LIKE 'CONFIRM%')::int AS confirmados,
                        COUNT(*) FILTER (WHERE UPPER(COALESCE(tt.estado, '')) = 'PENDIENTE')::int AS pendientes,
                        COUNT(*) FILTER (WHERE UPPER(COALESCE(tt.estado, '')) LIKE 'RECHAZ%')::int AS rechazados
                    FROM turno t
                    JOIN turno_tripulacion tt ON tt.id_turno = t.id_turno
                    WHERE t.id_vuelo = v.id_vuelo
                ) crew_agg ON TRUE
                WHERE v.rut_piloto = :rutPiloto
                """
                + (idVueloFiltro != null ? " AND v.id_vuelo = :idVuelo " : "")
                + """
                ORDER BY v.fecha_hora_salida ASC NULLS LAST, v.id_vuelo ASC
                LIMIT 250
                """;

        Query query = em.createNativeQuery(sql).setParameter("rutPiloto", rutPiloto);
        if (idVueloFiltro != null) {
            query.setParameter("idVuelo", idVueloFiltro);
        }

        List<Object[]> rows = query.getResultList();
        Timestamp ahora = Timestamp.from(Instant.now());
        List<VueloResumenPilotoDTO> result = new ArrayList<>(rows.size());

        for (Object[] r : rows) {
            Integer idVuelo = toInt(r[0]);
            String numeroVuelo = toStr(r[1], "Vuelo #" + idVuelo);
            Timestamp salida = toTimestamp(r[2]);
            Timestamp llegada = toTimestamp(r[3]);
            long minutosDuracion = calcularMinutos(salida, llegada);
            String duracionTexto = formatearDuracion(minutosDuracion);

            String estadoDesc = toStr(r[4], "Sin estado");
            String estadoCod = toStr(r[5], "");
            boolean esCancelado = esEstadoCancelado(estadoDesc, estadoCod);
            boolean esFinalizado = esEstadoFinalizado(estadoDesc, estadoCod, llegada, ahora);
            boolean esProximo = !esCancelado && !esFinalizado;
            String estadoSeveridad = resolverSeveridadEstado(estadoDesc, estadoCod, esCancelado, esFinalizado);
            String ventana = calcularVentanaOperativa(salida, llegada, ahora, esCancelado, esFinalizado);

            int capPax = toInt(r[25]);
            int capCargaKg = toInt(r[26]);
            String mantEstado = toStr(r[27], "No informado");
            Timestamp proxMant = toTimestamp(r[28]);
            boolean alertaMant = evaluarAlertaMantenimiento(mantEstado, proxMant, salida, ahora);

            int paxRes = toInt(r[29]);
            int paxCheckin = toInt(r[30]);
            int piezas = toInt(r[31]);
            double pesoKg = redondear1(toDouble(r[32]));
            double ocupacionPct = capPax > 0 ? redondear1((paxRes * 100.0) / capPax) : 0.0;
            double checkinPct = paxRes > 0 ? redondear1((paxCheckin * 100.0) / paxRes) : 0.0;
            double cargaPct = capCargaKg > 0 ? redondear1((pesoKg * 100.0) / capCargaKg) : 0.0;

            result.add(new VueloResumenPilotoDTO(
                    idVuelo,
                    numeroVuelo,
                    salida,
                    llegada,
                    minutosDuracion,
                    duracionTexto,
                    estadoDesc,
                    estadoCod,
                    estadoSeveridad,
                    esCancelado,
                    esFinalizado,
                    esProximo,
                    ventana,
                    toStr(r[6], "SkyWay"),
                    toStr(r[7], ""),
                    toStr(r[8], "N/D"),
                    toStr(r[9], ""),
                    toStr(r[10], ""),
                    toStr(r[11], ""),
                    toStr(r[14], "N/D"),
                    toStr(r[15], ""),
                    toStr(r[16], ""),
                    toStr(r[17], ""),
                    toInt(r[18]),
                    redondear1(toDouble(r[19])),
                    toStr(r[12], ""),
                    toStr(r[13], ""),
                    toNullableInt(r[20]),
                    toStr(r[21], "Por asignar"),
                    toStr(r[22], ""),
                    toStr(r[23], "Por asignar"),
                    toNullableInt(r[24]),
                    capPax,
                    capCargaKg,
                    mantEstado,
                    proxMant,
                    alertaMant,
                    paxRes,
                    paxCheckin,
                    ocupacionPct,
                    checkinPct,
                    piezas,
                    pesoKg,
                    cargaPct,
                    toInt(r[33]),
                    toInt(r[34]),
                    toInt(r[35]),
                    toInt(r[36])
            ));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<SegmentoBriefingDTO> consultarTramosVuelo(Integer idVuelo) {
        String sql = """
                SELECT
                    COALESCE(sv.orden_segmento, 1) AS orden_seg,
                    COALESCE(ao.codigo_iata, 'N/D') AS orig_iata,
                    COALESCE(ao.nombre_aeropuerto, '') AS orig_aero,
                    COALESCE(co.nombre, '') AS orig_ciudad,
                    COALESCE(po.nombre, '') AS orig_pais,
                    COALESCE(ad.codigo_iata, 'N/D') AS dest_iata,
                    COALESCE(ad.nombre_aeropuerto, '') AS dest_aero,
                    COALESCE(cd.nombre, '') AS dest_ciudad,
                    COALESCE(pd.nombre, '') AS dest_pais,
                    sv.hora_salida,
                    sv.hora_llegada,
                    COALESCE(ROUND((ST_Distance(ao.posicion, ad.posicion) / 1000.0)::numeric, 1), 0.0) AS dist_km,
                    COALESCE(pe.codigo_puerta, '') AS codigo_puerta,
                    COALESCE(pe.terminal, '') AS terminal,
                    ap.hora_asignacion
                FROM segmento_vuelo sv
                LEFT JOIN aeropuerto ao ON ao.id_aeropuerto = sv.id_aeropuerto_origen
                LEFT JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
                LEFT JOIN pais po ON po.id_pais = co.id_pais
                LEFT JOIN aeropuerto ad ON ad.id_aeropuerto = sv.id_aeropuerto_destino
                LEFT JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
                LEFT JOIN pais pd ON pd.id_pais = cd.id_pais
                LEFT JOIN asignacion_puerta ap ON ap.id_segmento = sv.id_segmento
                LEFT JOIN puerta_embarque pe ON pe.id_puerta = ap.id_puerta
                WHERE sv.id_vuelo = :idVuelo
                ORDER BY sv.orden_segmento ASC NULLS LAST, sv.id_segmento ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("idVuelo", idVuelo)
                .getResultList();

        List<SegmentoBriefingDTO> tramos = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Timestamp hSal = toTimestamp(r[9]);
            Timestamp hLleg = toTimestamp(r[10]);
            tramos.add(new SegmentoBriefingDTO(
                    toInt(r[0]),
                    toStr(r[1], "N/D"),
                    toStr(r[2], ""),
                    toStr(r[3], ""),
                    toStr(r[4], ""),
                    toStr(r[5], "N/D"),
                    toStr(r[6], ""),
                    toStr(r[7], ""),
                    toStr(r[8], ""),
                    hSal,
                    hLleg,
                    formatearDuracion(calcularMinutos(hSal, hLleg)),
                    redondear1(toDouble(r[11])),
                    toStr(r[12], ""),
                    toStr(r[13], ""),
                    toTimestamp(r[14])
            ));
        }
        return tramos;
    }

    @SuppressWarnings("unchecked")
    private List<OcupacionClaseDTO> consultarOcupacionPorClase(Integer idVuelo, Integer idAvion) {
        if (idAvion == null) {
            return List.of();
        }
        String sql = """
                SELECT
                    ca.descripcion AS clase_nombre,
                    COALESCE(cc.cantidad, 0) AS capacidad_clase,
                    COALESCE(occ.ocupados, 0) AS asientos_ocupados,
                    COALESCE(occ.checkins, 0) AS checkins_completados
                FROM capacidad_clase cc
                JOIN clase_asiento ca ON ca.id_clase = cc.id_clase
                LEFT JOIN LATERAL (
                    SELECT
                        COUNT(DISTINCT ra.id_reserva_asiento)::int AS ocupados,
                        COUNT(DISTINCT CASE WHEN EXISTS (
                            SELECT 1
                            FROM checkin ch
                            LEFT JOIN checkin_pasajero cp ON cp.id_checkin = ch.id_checkin
                            WHERE ch.id_reserva = ra.id_reserva
                              AND UPPER(COALESCE(ch.estado, '')) = 'COMPLETADO'
                              AND (cp.rut = ra.rut OR NOT EXISTS (
                                  SELECT 1 FROM checkin_pasajero cp2 WHERE cp2.id_checkin = ch.id_checkin
                              ))
                        ) THEN ra.id_reserva_asiento END)::int AS checkins
                    FROM reserva_asiento ra
                    JOIN asiento a ON a.id_asiento = ra.id_asiento
                    JOIN reserva r ON r.id_reserva = ra.id_reserva
                    LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                    WHERE ra.id_vuelo = :idVuelo
                      AND a.id_clase = cc.id_clase
                      AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%'
                ) occ ON TRUE
                WHERE cc.id_avion = :idAvion
                ORDER BY cc.id_clase ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("idVuelo", idVuelo)
                .setParameter("idAvion", idAvion)
                .getResultList();

        List<OcupacionClaseDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            int cap = toInt(r[1]);
            int occ = toInt(r[2]);
            int chk = toInt(r[3]);
            double pct = cap > 0 ? redondear1((occ * 100.0) / cap) : 0.0;
            result.add(new OcupacionClaseDTO(toStr(r[0], "Cabina"), cap, occ, chk, pct));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EquipajeResumenDTO> consultarEquipajePorTipo(Integer idVuelo) {
        String sql = """
                SELECT
                    COALESCE(te.nombre, eq.tipo, 'Equipaje') AS tipo_nombre,
                    COUNT(*)::int AS piezas,
                    COALESCE(SUM(eq.peso), 0.0)::double precision AS peso_total
                FROM equipaje eq
                LEFT JOIN tipo_equipaje te ON te.id_tipo = eq.id_tipo
                WHERE EXISTS (
                    SELECT 1
                    FROM reserva_asiento ra
                    JOIN reserva r ON r.id_reserva = ra.id_reserva
                    LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                    WHERE ra.id_vuelo = :idVuelo
                      AND ra.id_reserva = eq.id_reserva
                      AND ra.rut = eq.rut_pasajero
                      AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%'
                )
                GROUP BY COALESCE(te.nombre, eq.tipo, 'Equipaje')
                ORDER BY piezas DESC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("idVuelo", idVuelo)
                .getResultList();

        List<EquipajeResumenDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new EquipajeResumenDTO(
                    toStr(r[0], "Equipaje"),
                    toInt(r[1]),
                    redondear1(toDouble(r[2]))
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<TripulanteBriefingDTO> consultarTripulacionVuelo(Integer idVuelo) {
        String sql = """
                SELECT
                    tr.rut,
                    TRIM(CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.apellido, ''))) AS nombre_completo,
                    COALESCE(tr.cargo, 'Tripulante') AS cargo,
                    COALESCE(tip.nombre, 'Turno de Vuelo') AS tipo_turno,
                    t.hora_inicio,
                    t.hora_fin,
                    COALESCE(tt.estado, 'PENDIENTE') AS estado_conf,
                    tt.fecha_respuesta,
                    tt.motivo_rechazo,
                    COALESCE(u.telefono, '') AS telefono,
                    COALESCE(u.correo_electronico, '') AS correo
                FROM turno t
                LEFT JOIN tipo_turno tip ON tip.id_tipo = t.id_tipo_turno
                JOIN turno_tripulacion tt ON tt.id_turno = t.id_turno
                JOIN tripulacion tr ON tr.rut = tt.rut_tripulacion
                JOIN usuario u ON u.rut = tr.rut
                WHERE t.id_vuelo = :idVuelo
                ORDER BY
                    CASE WHEN UPPER(COALESCE(tr.cargo, '')) LIKE '%COPILOT%' THEN 0
                         WHEN UPPER(COALESCE(tr.cargo, '')) LIKE '%JEFE%' THEN 1
                         ELSE 2 END,
                    nombre_completo ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("idVuelo", idVuelo)
                .getResultList();

        List<TripulanteBriefingDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new TripulanteBriefingDTO(
                    toStr(r[0], ""),
                    toStr(r[1], "Tripulante"),
                    toStr(r[2], "Tripulación"),
                    toStr(r[3], "Turno de Vuelo"),
                    toTimestamp(r[4]),
                    toTimestamp(r[5]),
                    toStr(r[6], "PENDIENTE"),
                    toTimestamp(r[7]),
                    toStr(r[8], ""),
                    toStr(r[9], ""),
                    toStr(r[10], "")
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ManifiestoPasajeroDTO> consultarManifiestoPasajeros(Integer idVuelo) {
        String sql = """
                SELECT
                    ra.rut,
                    TRIM(CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.apellido, ''))) AS nombre_completo,
                    COALESCE(r.codigo_reserva, CONCAT('#', r.id_reserva)) AS codigo_reserva,
                    COALESCE(er.descripcion, 'Activa') AS estado_reserva,
                    COALESCE(a.numero_asiento, '-') AS numero_asiento,
                    COALESCE(ca.descripcion, '-') AS clase_asiento,
                    COALESCE(chk.estado, 'SIN CHECK-IN') AS checkin_estado,
                    COALESCE(chk.metodo, '-') AS checkin_metodo,
                    COALESCE(chk.codigo_bp, '') AS codigo_bp,
                    COALESCE(bag.piezas, 0) AS piezas_equipaje,
                    COALESCE(bag.peso_kg, 0.0) AS peso_equipaje_kg
                FROM reserva_asiento ra
                JOIN reserva r ON r.id_reserva = ra.id_reserva
                LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                JOIN usuario u ON u.rut = ra.rut
                LEFT JOIN asiento a ON a.id_asiento = ra.id_asiento
                LEFT JOIN clase_asiento ca ON ca.id_clase = a.id_clase
                LEFT JOIN LATERAL (
                    SELECT
                        ch.estado,
                        ch.metodo,
                        ch.codigo_boarding_pass AS codigo_bp
                    FROM checkin ch
                    LEFT JOIN checkin_pasajero cp ON cp.id_checkin = ch.id_checkin
                    WHERE ch.id_reserva = ra.id_reserva
                      AND UPPER(COALESCE(ch.estado, '')) = 'COMPLETADO'
                      AND (cp.rut = ra.rut OR NOT EXISTS (
                          SELECT 1 FROM checkin_pasajero cp2 WHERE cp2.id_checkin = ch.id_checkin
                      ))
                    ORDER BY ch.fecha_hora DESC
                    LIMIT 1
                ) chk ON TRUE
                LEFT JOIN LATERAL (
                    SELECT
                        COUNT(*)::int AS piezas,
                        COALESCE(SUM(eq.peso), 0.0)::double precision AS peso_kg
                    FROM equipaje eq
                    WHERE eq.id_reserva = ra.id_reserva
                      AND eq.rut_pasajero = ra.rut
                ) bag ON TRUE
                WHERE ra.id_vuelo = :idVuelo
                  AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%'
                ORDER BY a.fila ASC NULLS LAST, a.letra ASC NULLS LAST, nombre_completo ASC
                LIMIT 300
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("idVuelo", idVuelo)
                .getResultList();

        List<ManifiestoPasajeroDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new ManifiestoPasajeroDTO(
                    toStr(r[0], ""),
                    toStr(r[1], "Pasajero"),
                    toStr(r[2], ""),
                    toStr(r[3], ""),
                    toStr(r[4], "-"),
                    toStr(r[5], "-"),
                    toStr(r[6], "SIN CHECK-IN"),
                    toStr(r[7], "-"),
                    toStr(r[8], ""),
                    toInt(r[9]),
                    redondear1(toDouble(r[10]))
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<NotificacionPilotoDTO> consultarNotificaciones(String rutPiloto) {
        String sql = """
                SELECT
                    n.id_notificacion,
                    COALESCE(n.titulo, 'Notificación Operacional'),
                    COALESCE(n.mensaje, ''),
                    n.fecha,
                    COALESCE(n.leido, FALSE),
                    COALESCE(n.canal, 'Sistema'),
                    COALESCE(n.enviada, FALSE)
                FROM notificacion n
                WHERE n.rut_destinatario = :rutPiloto
                ORDER BY n.fecha DESC NULLS LAST, n.id_notificacion DESC
                LIMIT 30
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("rutPiloto", rutPiloto)
                .getResultList();

        List<NotificacionPilotoDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new NotificacionPilotoDTO(
                    toInt(r[0]),
                    toStr(r[1], "Notificación"),
                    toStr(r[2], ""),
                    toTimestamp(r[3]),
                    toBool(r[4]),
                    toStr(r[5], "Sistema"),
                    toBool(r[6])
            ));
        }
        return result;
    }

    private KpisPilotoDTO construirKpisReales(List<VueloResumenPilotoDTO> vuelos, String rutPiloto) {
        if (vuelos.isEmpty()) {
            return kpisVacios();
        }

        long minCompletados = 0L;
        long minProgramados = 0L;
        int proximos = 0;
        int completados = 0;
        int cancelados = 0;
        long sumPax = 0L;
        long sumCheckin = 0L;
        long sumCap = 0L;
        int alertasMant = 0;

        Map<String, Integer> conteoModelos = new HashMap<>();
        Set<Integer> avionesIds = new HashSet<>();

        for (VueloResumenPilotoDTO v : vuelos) {
            if (v.isEsCancelado()) {
                cancelados++;
                continue;
            }
            if (v.isEsFinalizado()) {
                completados++;
                minCompletados += v.getMinutosDuracion();
            } else {
                proximos++;
                minProgramados += v.getMinutosDuracion();
            }

            if (v.getAvionCapacidadPax() > 0) {
                sumPax += v.getPasajerosReservados();
                sumCheckin += v.getPasajerosCheckin();
                sumCap += v.getAvionCapacidadPax();
            }
            if (v.getIdAvion() != null) {
                avionesIds.add(v.getIdAvion());
            }
            if (v.getAvionModelo() != null && !v.getAvionModelo().isBlank() && !"Por asignar".equals(v.getAvionModelo())) {
                String nombreCompletoModelo = v.getModeloCompleto();
                conteoModelos.merge(nombreCompletoModelo, 1, Integer::sum);
            }
            if (v.isAlertaMantenimiento() && !v.isEsFinalizado()) {
                alertasMant++;
            }
        }

        String modeloPrincipal = conteoModelos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Por asignar");

        double ocupacionReal = sumCap > 0 ? redondear1((sumPax * 100.0) / sumCap) : 0.0;
        double checkinReal = sumPax > 0 ? redondear1((sumCheckin * 100.0) / sumPax) : 0.0;
        int aeropuertosVisitados = contarAeropuertosDistintosPiloto(rutPiloto);

        return new KpisPilotoDTO(
                minCompletados,
                minProgramados,
                formatearDuracion(minCompletados),
                formatearDuracion(minProgramados),
                (minCompletados + minProgramados) / 60L,
                vuelos.size(),
                proximos,
                completados,
                cancelados,
                sumPax,
                sumCheckin,
                sumCap,
                ocupacionReal,
                checkinReal,
                modeloPrincipal,
                avionesIds.size(),
                aeropuertosVisitados,
                alertasMant
        );
    }

    private int contarAeropuertosDistintosPiloto(String rutPiloto) {
        String sql = """
                SELECT COUNT(DISTINCT id_aero)::int
                FROM (
                    SELECT sv.id_aeropuerto_origen AS id_aero
                    FROM segmento_vuelo sv
                    JOIN vuelo v ON v.id_vuelo = sv.id_vuelo
                    LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
                    WHERE v.rut_piloto = :rutPiloto
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%'
                      AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%'
                    UNION
                    SELECT sv.id_aeropuerto_destino AS id_aero
                    FROM segmento_vuelo sv
                    JOIN vuelo v ON v.id_vuelo = sv.id_vuelo
                    LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
                    WHERE v.rut_piloto = :rutPiloto
                      AND UPPER(COALESCE(ev.descripcion, '')) NOT LIKE '%CANCEL%'
                      AND UPPER(COALESCE(ev.estado, '')) NOT LIKE '%CANCEL%'
                ) a
                WHERE id_aero IS NOT NULL
                """;
        Number n = (Number) em.createNativeQuery(sql)
                .setParameter("rutPiloto", rutPiloto)
                .getSingleResult();
        return n != null ? n.intValue() : 0;
    }

    private VueloResumenPilotoDTO seleccionarProximoVuelo(List<VueloResumenPilotoDTO> vuelos) {
        return vuelos.stream()
                .filter(VueloResumenPilotoDTO::isEsProximo)
                .min(Comparator
                        .comparing((VueloResumenPilotoDTO v) -> "En progreso".equals(v.getVentanaOperativa()) ? 0 : 1)
                        .thenComparing(VueloResumenPilotoDTO::getFechaHoraSalida, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private List<FlotaPilotoDTO> construirFlotaDesdeVuelos(List<VueloResumenPilotoDTO> vuelos) {
        Map<Integer, List<VueloResumenPilotoDTO>> porAvion = new LinkedHashMap<>();
        for (VueloResumenPilotoDTO v : vuelos) {
            if (v.getIdAvion() != null && !v.isEsCancelado()) {
                porAvion.computeIfAbsent(v.getIdAvion(), k -> new ArrayList<>()).add(v);
            }
        }
        List<FlotaPilotoDTO> flota = new ArrayList<>(porAvion.size());
        for (List<VueloResumenPilotoDTO> grupo : porAvion.values()) {
            VueloResumenPilotoDTO ref = grupo.get(0);
            long minTotales = grupo.stream().mapToLong(VueloResumenPilotoDTO::getMinutosDuracion).sum();
            boolean alerta = grupo.stream().anyMatch(VueloResumenPilotoDTO::isAlertaMantenimiento);
            flota.add(new FlotaPilotoDTO(
                    ref.getIdAvion(),
                    ref.getAvionMatricula(),
                    ref.getAvionFabricante(),
                    ref.getAvionModelo(),
                    ref.getAvionAnoFabricacion(),
                    ref.getAvionCapacidadPax(),
                    ref.getAvionCapacidadCargaKg(),
                    ref.getAvionEstadoMantenimiento(),
                    ref.getAvionFechaProximoMant(),
                    alerta,
                    grupo.size(),
                    formatearDuracion(minTotales)
            ));
        }
        flota.sort(Comparator.comparingInt(FlotaPilotoDTO::getTotalVuelos).reversed());
        return flota;
    }

    private KpisPilotoDTO kpisVacios() {
        return new KpisPilotoDTO(0L, 0L, "0h 00m", "0h 00m", 0L, 0, 0, 0, 0, 0L, 0L, 0L, 0.0, 0.0, "N/A", 0, 0, 0);
    }

    private static boolean esEstadoCancelado(String desc, String cod) {
        String d = desc.toUpperCase(Locale.ROOT);
        String c = cod.toUpperCase(Locale.ROOT);
        return d.contains("CANCEL") || c.contains("CANCEL") || "INACTIVO".equals(c);
    }

    private static boolean esEstadoFinalizado(String desc, String cod, Timestamp llegada, Timestamp ahora) {
        String d = desc.toUpperCase(Locale.ROOT);
        String c = cod.toUpperCase(Locale.ROOT);
        if (d.contains("ATERRIZ") || "FINALIZADO".equals(c)) {
            return true;
        }
        if (d.contains("EN VUELO")) {
            return false;
        }
        return llegada != null && llegada.before(ahora);
    }

    private static String resolverSeveridadEstado(String desc, String cod, boolean cancelado, boolean finalizado) {
        if (cancelado) return "danger";
        String d = desc.toUpperCase(Locale.ROOT);
        if (d.contains("RETRAS") || d.contains("DEMOR")) return "warning";
        if (d.contains("EN VUELO")) return "success";
        if (finalizado) return "secondary";
        return "info";
    }

    private static String calcularVentanaOperativa(
            Timestamp salida,
            Timestamp llegada,
            Timestamp ahora,
            boolean cancelado,
            boolean finalizado
    ) {
        if (cancelado) return "Cancelado";
        if (finalizado) return "Finalizado";
        if (salida == null) return "Programado";

        long ahoraMs = ahora.getTime();
        long salMs = salida.getTime();
        long llegMs = llegada != null ? llegada.getTime() : Long.MAX_VALUE;

        if (ahoraMs >= salMs && ahoraMs <= llegMs) {
            return "En progreso";
        }
        if (salMs > ahoraMs) {
            long mins = Math.max(1L, (salMs - ahoraMs) / 60_000L);
            if (mins < 60L) {
                return "En " + mins + " min";
            }
            long horas = mins / 60L;
            long remMin = mins % 60L;
            if (horas < 24L) {
                return "En " + horas + "h " + (remMin < 10 ? "0" : "") + remMin + "m";
            }
            long dias = horas / 24L;
            long remH = horas % 24L;
            return "En " + dias + "d " + remH + "h";
        }
        return "Programado";
    }

    private static boolean evaluarAlertaMantenimiento(
            String estadoMant,
            Timestamp proximoMant,
            Timestamp salida,
            Timestamp ahora
    ) {
        if (estadoMant != null) {
            String upper = estadoMant.toUpperCase(Locale.ROOT);
            if (!upper.contains("OPERAT") && !upper.contains("OPTIM") && !upper.contains("DISPON")) {
                return true;
            }
        }
        if (proximoMant != null) {
            long refMs = salida != null ? Math.max(ahora.getTime(), salida.getTime()) : ahora.getTime();
            long diffDias = Duration.ofMillis(proximoMant.getTime() - refMs).toDays();
            return diffDias <= 7L;
        }
        return false;
    }

    private static long calcularMinutos(Timestamp inicio, Timestamp fin) {
        if (inicio == null || fin == null || fin.before(inicio)) {
            return 0L;
        }
        return (fin.getTime() - inicio.getTime()) / 60_000L;
    }

    private static String formatearDuracion(long minutos) {
        long h = Math.max(0L, minutos) / 60L;
        long m = Math.max(0L, minutos) % 60L;
        return h + "h " + (m < 10 ? "0" : "") + m + "m";
    }

    private static double redondear1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static int toInt(Object o) {
        return o instanceof Number n ? n.intValue() : 0;
    }

    private static Integer toNullableInt(Object o) {
        return o instanceof Number n ? n.intValue() : null;
    }

    private static double toDouble(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0.0;
    }

    private static boolean toBool(Object o) {
        return o instanceof Boolean b && b;
    }

    private static Timestamp toTimestamp(Object o) {
        if (o instanceof Timestamp ts) return ts;
        if (o instanceof java.util.Date d) return new Timestamp(d.getTime());
        return null;
    }

    private static String toStr(Object o, String fallback) {
        if (o == null) return fallback;
        String s = String.valueOf(o).trim();
        return s.isEmpty() ? fallback : s;
    }
}
