package com.SkyWay.modules.tripulacion.domain.service;

import com.SkyWay.modules.tripulacion.presentation.dto.TripulacionPanelDtos.*;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Servicio de consultas operacionales reales para el Portal de Tripulación de Cabina.
 * Toda métrica, detalle de vuelo, comandante, ocupación por clase, salidas de emergencia,
 * equipaje y compañeros de tripulación proviene directamente de PostgreSQL V3.
 */
@Service
public class TripulacionPanelService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public PanelTripulacionDataDTO cargarPanelTripulacion(String rutTripulante) {
        if (rutTripulante == null || rutTripulante.isBlank()) {
            return new PanelTripulacionDataDTO(kpisVacios(), null, List.of(), List.of(),
                    FatigaDescansoHelper.analizar(List.of()).getResumen());
        }

        List<TurnoResumenTripulacionDTO> turnos = ejecutarConsultaTurnos(rutTripulante, null);
        ResumenFatigaDTO fatiga = aplicarAnalisisFatiga(turnos);
        KpisTripulacionDTO kpis = construirKpisReales(turnos, rutTripulante);
        TurnoResumenTripulacionDTO proximoTurno = seleccionarProximoTurno(turnos);
        List<NotificacionTripulacionDTO> notificaciones = consultarNotificaciones(rutTripulante);

        return new PanelTripulacionDataDTO(kpis, proximoTurno, turnos, notificaciones, fatiga);
    }

    private ResumenFatigaDTO aplicarAnalisisFatiga(List<TurnoResumenTripulacionDTO> turnos) {
        List<BloqueServicio> activos = turnos.stream()
                .filter(t -> !t.isEsRechazado() && !t.isVueloCancelado() && t.getHoraInicio() != null && t.getHoraFin() != null)
                .map(t -> new BloqueServicio(
                        t.getIdTurnoTripulacion(),
                        t.getNumeroVuelo() != null ? t.getNumeroVuelo() : "#" + t.getIdTurnoTripulacion(),
                        t.getHoraInicio(),
                        t.getHoraFin()))
                .toList();
        ResultadoAnalisisFatiga analisis = FatigaDescansoHelper.analizar(activos);
        for (TurnoResumenTripulacionDTO t : turnos) {
            IntervaloServicioInfo info = analisis.getPorId().get(t.getIdTurnoTripulacion());
            if (info != null) {
                t.setIntervaloPrevio(info);
            }
        }
        return analisis.getResumen();
    }

    @Transactional(readOnly = true)
    public BriefingTurnoDTO cargarBriefingTurno(String rutTripulante, Integer idTurnoTripulacion) {
        if (rutTripulante == null || rutTripulante.isBlank() || idTurnoTripulacion == null) {
            return null;
        }

        List<TurnoResumenTripulacionDTO> lista = ejecutarConsultaTurnos(rutTripulante, idTurnoTripulacion);
        if (lista.isEmpty()) {
            return null;
        }

        TurnoResumenTripulacionDTO resumen = lista.get(0);
        Integer idVuelo = resumen.getIdVuelo();

        List<SegmentoTripulacionDTO> tramos = idVuelo != null ? consultarTramosVuelo(idVuelo) : List.of();
        List<CabinaClaseDTO> cabina = (idVuelo != null && resumen.getIdAvion() != null)
                ? consultarCabinaPorClase(idVuelo, resumen.getIdAvion())
                : List.of();
        List<EquipajeCabinaDTO> equipaje = idVuelo != null ? consultarEquipajePorTipo(idVuelo) : List.of();
        List<CompaneroTripulacionDTO> companeros = consultarCompaneros(resumen.getIdTurno(), idVuelo, rutTripulante);
        List<ManifiestoCabinaDTO> manifiesto = idVuelo != null ? consultarManifiestoCabina(idVuelo) : List.of();

        return new BriefingTurnoDTO(resumen, tramos, cabina, equipaje, companeros, manifiesto);
    }

    @Transactional
    public boolean verificarPropiedadTurno(String rutTripulante, Integer idTurnoTripulacion) {
        if (rutTripulante == null || idTurnoTripulacion == null) {
            return false;
        }
        Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*)::int FROM turno_tripulacion WHERE id_turno_tripulacion = :id AND rut_tripulacion = :rut")
                .setParameter("id", idTurnoTripulacion)
                .setParameter("rut", rutTripulante)
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    @Transactional
    public boolean marcarNotificacionLeida(String rutTripulante, Integer idNotificacion) {
        if (rutTripulante == null || idNotificacion == null) {
            return false;
        }
        int updated = em.createNativeQuery(
                        "UPDATE notificacion SET leido = TRUE WHERE id_notificacion = :id AND rut_destinatario = :rut")
                .setParameter("id", idNotificacion)
                .setParameter("rut", rutTripulante)
                .executeUpdate();
        return updated > 0;
    }

    @Transactional
    public int marcarTodasNotificacionesLeidas(String rutTripulante) {
        if (rutTripulante == null || rutTripulante.isBlank()) {
            return 0;
        }
        return em.createNativeQuery(
                        "UPDATE notificacion SET leido = TRUE WHERE rut_destinatario = :rut AND COALESCE(leido, FALSE) = FALSE")
                .setParameter("rut", rutTripulante)
                .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    private List<TurnoResumenTripulacionDTO> ejecutarConsultaTurnos(String rutTripulante, Integer idTurnoTripFiltro) {
        String sql = """
                SELECT
                    tt.id_turno_tripulacion,
                    t.id_turno,
                    COALESCE(tip.nombre, 'Servicio de Cabina') AS tipo_servicio,
                    COALESCE(v.fecha_hora_salida, t.hora_inicio) AS hora_inicio,
                    COALESCE(v.fecha_hora_llegada, t.hora_fin) AS hora_fin,
                    COALESCE(tt.estado, 'PENDIENTE') AS estado_resp,
                    tt.fecha_respuesta,
                    tt.motivo_rechazo,
                    v.id_vuelo,
                    COALESCE(v.numero_vuelo, 'Guardia / Base') AS numero_vuelo,
                    v.fecha_hora_salida,
                    v.fecha_hora_llegada,
                    COALESCE(ev.descripcion, 'Sin estado') AS vuelo_estado_desc,
                    COALESCE(ev.estado, '') AS vuelo_estado_cod,
                    COALESCE(al.nombre, 'SkyWay') AS aerolinea_nombre,
                    COALESCE(orig.iata, 'N/D') AS orig_iata,
                    COALESCE(orig.ciudad, '') AS orig_ciudad,
                    COALESCE(orig.aeropuerto, '') AS orig_aeropuerto,
                    COALESCE(orig.puerta, '') AS orig_puerta,
                    COALESCE(orig.terminal, '') AS orig_terminal,
                    COALESCE(dest.iata, 'N/D') AS dest_iata,
                    COALESCE(dest.ciudad, '') AS dest_ciudad,
                    COALESCE(dest.aeropuerto, '') AS dest_aeropuerto,
                    COALESCE(seg_cnt.tramos, 0) AS tramos,
                    COALESCE(NULLIF(TRIM(CONCAT(COALESCE(upil.nombre, ''), ' ', COALESCE(upil.apellido, ''))), ''), 'Por asignar') AS piloto_nombre,
                    COALESCE(pil.licencia, '') AS piloto_licencia,
                    COALESCE(upil.telefono, '') AS piloto_telefono,
                    av.id_avion,
                    COALESCE(av.numero_de_registro, 'Por asignar') AS matricula,
                    COALESCE(fb.nombre, '') AS fabricante,
                    COALESCE(ma.nombre, 'Por asignar') AS modelo,
                    COALESCE(av.capacidad_de_pasajeros, 0) AS cap_pax,
                    COALESCE(av.estado_de_mantenimiento, 'No informado') AS mant_estado,
                    COALESCE(pax_agg.pax_reservados, 0) AS pax_reservados,
                    COALESCE(pax_agg.pax_checkin, 0) AS pax_checkin,
                    COALESCE(pax_agg.pax_emergencia, 0) AS pax_emergencia,
                    COALESCE(bag_agg.piezas, 0) AS piezas_equipaje,
                    COALESCE(bag_agg.peso_kg, 0.0) AS peso_equipaje_kg,
                    COALESCE(crew_agg.crew_total, 0) AS crew_total,
                    COALESCE(crew_agg.crew_conf, 0) AS crew_conf
                FROM turno_tripulacion tt
                JOIN turno t ON t.id_turno = tt.id_turno
                LEFT JOIN tipo_turno tip ON tip.id_tipo = t.id_tipo_turno
                LEFT JOIN vuelo v ON v.id_vuelo = t.id_vuelo
                LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
                LEFT JOIN aerolinea al ON al.id_aerolinea = v.id_aerolinea
                LEFT JOIN piloto pil ON pil.rut = v.rut_piloto
                LEFT JOIN usuario upil ON upil.rut = pil.rut
                LEFT JOIN avion av ON av.id_avion = v.id_avion
                LEFT JOIN modelo_avion ma ON ma.id_modelo = av.id_modelo
                LEFT JOIN fabricante fb ON fb.id_fabricante = ma.id_fabricante
                LEFT JOIN LATERAL (
                    SELECT
                        ao.codigo_iata AS iata,
                        co.nombre AS ciudad,
                        ao.nombre_aeropuerto AS aeropuerto,
                        pe.codigo_puerta AS puerta,
                        pe.terminal AS terminal
                    FROM segmento_vuelo sv
                    LEFT JOIN aeropuerto ao ON ao.id_aeropuerto = sv.id_aeropuerto_origen
                    LEFT JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
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
                        ad.nombre_aeropuerto AS aeropuerto
                    FROM segmento_vuelo sv
                    LEFT JOIN aeropuerto ad ON ad.id_aeropuerto = sv.id_aeropuerto_destino
                    LEFT JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
                    WHERE sv.id_vuelo = v.id_vuelo
                    ORDER BY sv.orden_segmento DESC NULLS LAST, sv.id_segmento DESC
                    LIMIT 1
                ) dest ON TRUE
                LEFT JOIN LATERAL (
                    SELECT COUNT(*)::int AS tramos
                    FROM segmento_vuelo sv2
                    WHERE sv2.id_vuelo = v.id_vuelo
                ) seg_cnt ON TRUE
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
                        ) THEN ra.id_reserva_asiento END)::int AS pax_checkin,
                        COUNT(DISTINCT CASE WHEN COALESCE(a.es_emergencia, FALSE) THEN ra.id_reserva_asiento END)::int AS pax_emergencia
                    FROM reserva_asiento ra
                    JOIN reserva r ON r.id_reserva = ra.id_reserva
                    LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                    LEFT JOIN asiento a ON a.id_asiento = ra.id_asiento
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
                        COUNT(*)::int AS crew_total,
                        COUNT(*) FILTER (WHERE UPPER(COALESCE(tt2.estado, '')) LIKE 'CONFIRM%')::int AS crew_conf
                    FROM turno t2
                    JOIN turno_tripulacion tt2 ON tt2.id_turno = t2.id_turno
                    WHERE (v.id_vuelo IS NOT NULL AND t2.id_vuelo = v.id_vuelo)
                       OR (v.id_vuelo IS NULL AND t2.id_turno = t.id_turno)
                ) crew_agg ON TRUE
                WHERE tt.rut_tripulacion = :rutTripulante
                """
                + (idTurnoTripFiltro != null ? " AND tt.id_turno_tripulacion = :idTurnoTrip " : "")
                + """
                ORDER BY COALESCE(v.fecha_hora_salida, t.hora_inicio) ASC NULLS LAST, tt.id_turno_tripulacion ASC
                LIMIT 250
                """;

        Query query = em.createNativeQuery(sql).setParameter("rutTripulante", rutTripulante);
        if (idTurnoTripFiltro != null) {
            query.setParameter("idTurnoTrip", idTurnoTripFiltro);
        }

        List<Object[]> rows = query.getResultList();
        Timestamp ahora = Timestamp.from(Instant.now());
        List<TurnoResumenTripulacionDTO> result = new ArrayList<>(rows.size());

        for (Object[] r : rows) {
            Timestamp inicio = toTimestamp(r[3]);
            Timestamp fin = toTimestamp(r[4]);
            long minDur = calcularMinutos(inicio, fin);
            String durTxt = formatearDuracion(minDur);

            String estadoResp = toStr(r[5], "PENDIENTE").toUpperCase(Locale.ROOT);
            boolean esConf = estadoResp.startsWith("CONFIRM");
            boolean esRech = estadoResp.startsWith("RECHAZ");
            boolean esPend = !esConf && !esRech;
            String respSev = esConf ? "success" : (esRech ? "danger" : "warning");

            String vueloEstadoDesc = toStr(r[12], "Sin estado");
            String vueloEstadoCod = toStr(r[13], "");
            boolean vueloCancelado = esEstadoCancelado(vueloEstadoDesc, vueloEstadoCod);

            boolean esFinalizado = (fin != null && fin.before(ahora)) || vueloEstadoDesc.toUpperCase(Locale.ROOT).contains("ATERRIZ");
            boolean esProximo = !esRech && !vueloCancelado && !esFinalizado;
            String vueloSev = resolverSeveridadVuelo(vueloEstadoDesc, vueloCancelado, esFinalizado);
            String ventana = calcularVentanaOperativa(inicio, fin, ahora, esRech || vueloCancelado, esFinalizado);

            int capPax = toInt(r[31]);
            int paxRes = toInt(r[33]);
            int paxChk = toInt(r[34]);
            int paxEmerg = toInt(r[35]);
            double occPct = capPax > 0 ? redondear1((paxRes * 100.0) / capPax) : 0.0;
            double chkPct = paxRes > 0 ? redondear1((paxChk * 100.0) / paxRes) : 0.0;

            result.add(new TurnoResumenTripulacionDTO(
                    toInt(r[0]),
                    toInt(r[1]),
                    toStr(r[2], "Servicio de Cabina"),
                    inicio,
                    fin,
                    minDur,
                    durTxt,
                    estadoResp,
                    respSev,
                    toTimestamp(r[6]),
                    toStr(r[7], ""),
                    esPend,
                    esConf,
                    esRech,
                    esFinalizado,
                    esProximo,
                    ventana,
                    toNullableInt(r[8]),
                    toStr(r[9], "Guardia / Base"),
                    toTimestamp(r[10]),
                    toTimestamp(r[11]),
                    vueloEstadoDesc,
                    vueloSev,
                    vueloCancelado,
                    toStr(r[14], "SkyWay"),
                    toStr(r[15], "N/D"),
                    toStr(r[16], ""),
                    toStr(r[17], ""),
                    toStr(r[20], "N/D"),
                    toStr(r[21], ""),
                    toStr(r[22], ""),
                    toInt(r[23]),
                    toStr(r[18], ""),
                    toStr(r[19], ""),
                    toStr(r[24], "Por asignar"),
                    toStr(r[25], ""),
                    toStr(r[26], ""),
                    toNullableInt(r[27]),
                    toStr(r[28], "Por asignar"),
                    toStr(r[29], ""),
                    toStr(r[30], "Por asignar"),
                    capPax,
                    toStr(r[32], "No informado"),
                    paxRes,
                    paxChk,
                    paxEmerg,
                    occPct,
                    chkPct,
                    toInt(r[36]),
                    redondear1(toDouble(r[37])),
                    toInt(r[38]),
                    toInt(r[39])
            ));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private List<SegmentoTripulacionDTO> consultarTramosVuelo(Integer idVuelo) {
        String sql = """
                SELECT
                    COALESCE(sv.orden_segmento, 1),
                    COALESCE(ao.codigo_iata, 'N/D'),
                    COALESCE(ao.nombre_aeropuerto, ''),
                    COALESCE(co.nombre, ''),
                    COALESCE(ad.codigo_iata, 'N/D'),
                    COALESCE(ad.nombre_aeropuerto, ''),
                    COALESCE(cd.nombre, ''),
                    sv.hora_salida,
                    sv.hora_llegada,
                    COALESCE(pe.codigo_puerta, ''),
                    COALESCE(pe.terminal, '')
                FROM segmento_vuelo sv
                LEFT JOIN aeropuerto ao ON ao.id_aeropuerto = sv.id_aeropuerto_origen
                LEFT JOIN ciudad co ON co.id_ciudad = ao.id_ciudad
                LEFT JOIN aeropuerto ad ON ad.id_aeropuerto = sv.id_aeropuerto_destino
                LEFT JOIN ciudad cd ON cd.id_ciudad = ad.id_ciudad
                LEFT JOIN asignacion_puerta ap ON ap.id_segmento = sv.id_segmento
                LEFT JOIN puerta_embarque pe ON pe.id_puerta = ap.id_puerta
                WHERE sv.id_vuelo = :idVuelo
                ORDER BY sv.orden_segmento ASC NULLS LAST, sv.id_segmento ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("idVuelo", idVuelo).getResultList();
        List<SegmentoTripulacionDTO> tramos = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Timestamp sal = toTimestamp(r[7]);
            Timestamp lleg = toTimestamp(r[8]);
            tramos.add(new SegmentoTripulacionDTO(
                    toInt(r[0]),
                    toStr(r[1], "N/D"),
                    toStr(r[2], ""),
                    toStr(r[3], ""),
                    toStr(r[4], "N/D"),
                    toStr(r[5], ""),
                    toStr(r[6], ""),
                    sal,
                    lleg,
                    formatearDuracion(calcularMinutos(sal, lleg)),
                    toStr(r[9], ""),
                    toStr(r[10], "")
            ));
        }
        return tramos;
    }

    @SuppressWarnings("unchecked")
    private List<CabinaClaseDTO> consultarCabinaPorClase(Integer idVuelo, Integer idAvion) {
        String sql = """
                SELECT
                    ca.descripcion,
                    COALESCE(cc.cantidad, 0),
                    COALESCE(occ.ocupados, 0),
                    COALESCE(occ.checkins, 0)
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
        List<CabinaClaseDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            int cap = toInt(r[1]);
            int occ = toInt(r[2]);
            int chk = toInt(r[3]);
            result.add(new CabinaClaseDTO(
                    toStr(r[0], "Cabina"),
                    cap,
                    occ,
                    chk,
                    cap > 0 ? redondear1((occ * 100.0) / cap) : 0.0
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<EquipajeCabinaDTO> consultarEquipajePorTipo(Integer idVuelo) {
        String sql = """
                SELECT
                    COALESCE(te.nombre, eq.tipo, 'Equipaje'),
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
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("idVuelo", idVuelo).getResultList();
        List<EquipajeCabinaDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new EquipajeCabinaDTO(toStr(r[0], "Equipaje"), toInt(r[1]), redondear1(toDouble(r[2]))));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<CompaneroTripulacionDTO> consultarCompaneros(Integer idTurno, Integer idVuelo, String rutActual) {
        String sql = """
                SELECT
                    tr.rut,
                    TRIM(CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.apellido, ''))) AS nombre_completo,
                    COALESCE(tr.cargo, 'Tripulante'),
                    COALESCE(tip.nombre, 'Servicio'),
                    COALESCE(tt.estado, 'PENDIENTE'),
                    COALESCE(u.telefono, ''),
                    COALESCE(u.correo_electronico, '')
                FROM turno t
                LEFT JOIN tipo_turno tip ON tip.id_tipo = t.id_tipo_turno
                JOIN turno_tripulacion tt ON tt.id_turno = t.id_turno
                JOIN tripulacion tr ON tr.rut = tt.rut_tripulacion
                JOIN usuario u ON u.rut = tr.rut
                WHERE (
                    (:idVuelo IS NOT NULL AND t.id_vuelo = :idVuelo)
                    OR (:idVuelo IS NULL AND t.id_turno = :idTurno)
                )
                ORDER BY
                    CASE WHEN UPPER(COALESCE(tr.cargo, '')) LIKE '%JEFE%' THEN 0
                         WHEN UPPER(COALESCE(tr.cargo, '')) LIKE '%COPILOT%' THEN 1
                         ELSE 2 END,
                    nombre_completo ASC
                """;
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("idVuelo", idVuelo)
                .setParameter("idTurno", idTurno)
                .getResultList();
        List<CompaneroTripulacionDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            String rut = toStr(r[0], "");
            result.add(new CompaneroTripulacionDTO(
                    rut,
                    toStr(r[1], "Tripulante"),
                    toStr(r[2], "Tripulación"),
                    toStr(r[3], "Servicio"),
                    toStr(r[4], "PENDIENTE").toUpperCase(Locale.ROOT),
                    toStr(r[5], ""),
                    toStr(r[6], ""),
                    rutActual != null && rutActual.equals(rut)
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ManifiestoCabinaDTO> consultarManifiestoCabina(Integer idVuelo) {
        String sql = """
                SELECT
                    TRIM(CONCAT(COALESCE(u.nombre, ''), ' ', COALESCE(u.apellido, ''))) AS nombre_completo,
                    ra.rut,
                    COALESCE(a.numero_asiento, '-'),
                    COALESCE(ca.descripcion, '-'),
                    COALESCE(a.es_emergencia, FALSE),
                    COALESCE(r.codigo_reserva, CONCAT('#', r.id_reserva)),
                    COALESCE(chk.estado, 'SIN CHECK-IN'),
                    COALESCE(chk.metodo, '-'),
                    COALESCE(bag.piezas, 0),
                    COALESCE(bag.peso_kg, 0.0)
                FROM reserva_asiento ra
                JOIN reserva r ON r.id_reserva = ra.id_reserva
                LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
                JOIN usuario u ON u.rut = ra.rut
                LEFT JOIN asiento a ON a.id_asiento = ra.id_asiento
                LEFT JOIN clase_asiento ca ON ca.id_clase = a.id_clase
                LEFT JOIN LATERAL (
                    SELECT ch.estado, ch.metodo
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
                    SELECT COUNT(*)::int AS piezas, COALESCE(SUM(eq.peso), 0.0)::double precision AS peso_kg
                    FROM equipaje eq
                    WHERE eq.id_reserva = ra.id_reserva AND eq.rut_pasajero = ra.rut
                ) bag ON TRUE
                WHERE ra.id_vuelo = :idVuelo
                  AND UPPER(COALESCE(er.descripcion, '')) NOT LIKE '%CANCEL%'
                ORDER BY COALESCE(a.es_emergencia, FALSE) DESC, a.fila ASC NULLS LAST, a.letra ASC NULLS LAST
                LIMIT 300
                """;
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("idVuelo", idVuelo).getResultList();
        List<ManifiestoCabinaDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new ManifiestoCabinaDTO(
                    toStr(r[0], "Pasajero"),
                    toStr(r[1], ""),
                    toStr(r[2], "-"),
                    toStr(r[3], "-"),
                    toBool(r[4]),
                    toStr(r[5], ""),
                    toStr(r[6], "SIN CHECK-IN"),
                    toStr(r[7], "-"),
                    toInt(r[8]),
                    redondear1(toDouble(r[9]))
            ));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<NotificacionTripulacionDTO> consultarNotificaciones(String rutTripulante) {
        String sql = """
                SELECT
                    n.id_notificacion,
                    COALESCE(n.titulo, 'Notificación Operacional'),
                    COALESCE(n.mensaje, ''),
                    n.fecha,
                    COALESCE(n.leido, FALSE),
                    COALESCE(n.canal, 'Sistema')
                FROM notificacion n
                WHERE n.rut_destinatario = :rut
                ORDER BY n.fecha DESC NULLS LAST, n.id_notificacion DESC
                LIMIT 30
                """;
        List<Object[]> rows = em.createNativeQuery(sql).setParameter("rut", rutTripulante).getResultList();
        List<NotificacionTripulacionDTO> result = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            result.add(new NotificacionTripulacionDTO(
                    toInt(r[0]),
                    toStr(r[1], "Notificación"),
                    toStr(r[2], ""),
                    toTimestamp(r[3]),
                    toBool(r[4]),
                    toStr(r[5], "Sistema")
            ));
        }
        return result;
    }

    private KpisTripulacionDTO construirKpisReales(List<TurnoResumenTripulacionDTO> turnos, String rutTripulante) {
        if (turnos.isEmpty()) {
            return kpisVacios();
        }

        int pendientes = 0;
        int confirmados = 0;
        int rechazados = 0;
        int proximos = 0;
        int completados = 0;
        long minConfirmados = 0L;
        long minPendientes = 0L;
        long totalPax = 0L;
        Set<Integer> vuelosUnicos = new HashSet<>();

        List<TurnoResumenTripulacionDTO> activosOrdenados = new ArrayList<>();
        for (TurnoResumenTripulacionDTO t : turnos) {
            if (t.isEsPendiente()) {
                pendientes++;
                minPendientes += t.getMinutosDuracion();
            } else if (t.isEsConfirmado()) {
                confirmados++;
                minConfirmados += t.getMinutosDuracion();
            } else if (t.isEsRechazado()) {
                rechazados++;
            }

            if (t.isEsProximo()) {
                proximos++;
            } else if (t.isEsFinalizado() && !t.isEsRechazado()) {
                completados++;
            }

            if (!t.isEsRechazado() && !t.isVueloCancelado() && t.getIdVuelo() != null) {
                if (vuelosUnicos.add(t.getIdVuelo())) {
                    totalPax += t.getPasajerosReservados();
                }
            }
            if (!t.isEsRechazado() && t.getHoraInicio() != null && t.getHoraFin() != null) {
                activosOrdenados.add(t);
            }
        }

        activosOrdenados.sort(Comparator.comparing(TurnoResumenTripulacionDTO::getHoraInicio));
        boolean descansoOk = true;
        String detalleDescanso = "Sin conflictos de horario";
        double minRestHrs = Double.MAX_VALUE;

        for (int i = 1; i < activosOrdenados.size(); i++) {
            TurnoResumenTripulacionDTO prev = activosOrdenados.get(i - 1);
            TurnoResumenTripulacionDTO curr = activosOrdenados.get(i);
            long diffMs = curr.getHoraInicio().getTime() - prev.getHoraFin().getTime();
            double hrs = diffMs / (1000.0 * 3600.0);
            if (hrs < minRestHrs) {
                minRestHrs = hrs;
            }
        }

        if (activosOrdenados.size() >= 2) {
            if (minRestHrs < 0.0) {
                descansoOk = false;
                detalleDescanso = "Solapamiento entre turnos asignados";
            } else if (minRestHrs < 8.0) {
                descansoOk = false;
                detalleDescanso = String.format(Locale.US, "Descanso mín.: %.1fh (Req. 8h)", minRestHrs);
            } else {
                detalleDescanso = String.format(Locale.US, "Descanso mín.: %.1fh (≥ 8h legal)", minRestHrs);
            }
        }

        int aeropuertos = contarAeropuertosDistintosTripulante(rutTripulante);

        return new KpisTripulacionDTO(
                turnos.size(),
                pendientes,
                confirmados,
                rechazados,
                proximos,
                completados,
                formatearDuracion(minConfirmados),
                formatearDuracion(minPendientes),
                formatearDuracion(minConfirmados + minPendientes),
                vuelosUnicos.size(),
                aeropuertos,
                totalPax,
                descansoOk,
                detalleDescanso
        );
    }

    private int contarAeropuertosDistintosTripulante(String rutTripulante) {
        String sql = """
                SELECT COUNT(DISTINCT id_aero)::int
                FROM (
                    SELECT sv.id_aeropuerto_origen AS id_aero
                    FROM turno_tripulacion tt
                    JOIN turno t ON t.id_turno = tt.id_turno
                    JOIN segmento_vuelo sv ON sv.id_vuelo = t.id_vuelo
                    WHERE tt.rut_tripulacion = :rut
                      AND UPPER(COALESCE(tt.estado, '')) NOT LIKE 'RECHAZ%'
                    UNION
                    SELECT sv.id_aeropuerto_destino AS id_aero
                    FROM turno_tripulacion tt
                    JOIN turno t ON t.id_turno = tt.id_turno
                    JOIN segmento_vuelo sv ON sv.id_vuelo = t.id_vuelo
                    WHERE tt.rut_tripulacion = :rut
                      AND UPPER(COALESCE(tt.estado, '')) NOT LIKE 'RECHAZ%'
                ) a
                WHERE id_aero IS NOT NULL
                """;
        Number n = (Number) em.createNativeQuery(sql).setParameter("rut", rutTripulante).getSingleResult();
        return n != null ? n.intValue() : 0;
    }

    private TurnoResumenTripulacionDTO seleccionarProximoTurno(List<TurnoResumenTripulacionDTO> turnos) {
        return turnos.stream()
                .filter(TurnoResumenTripulacionDTO::isEsProximo)
                .min(Comparator
                        .comparing((TurnoResumenTripulacionDTO t) -> "En progreso".equals(t.getVentanaOperativa()) ? 0 : 1)
                        .thenComparing(TurnoResumenTripulacionDTO::getHoraInicio, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    private KpisTripulacionDTO kpisVacios() {
        return new KpisTripulacionDTO(0, 0, 0, 0, 0, 0, "0h 00m", "0h 00m", "0h 00m", 0, 0, 0L, true, "Sin turnos asignados");
    }

    private static boolean esEstadoCancelado(String desc, String cod) {
        String d = desc.toUpperCase(Locale.ROOT);
        String c = cod.toUpperCase(Locale.ROOT);
        return d.contains("CANCEL") || c.contains("CANCEL") || "INACTIVO".equals(c);
    }

    private static String resolverSeveridadVuelo(String desc, boolean cancelado, boolean finalizado) {
        if (cancelado) return "danger";
        String d = desc.toUpperCase(Locale.ROOT);
        if (d.contains("RETRAS") || d.contains("DEMOR")) return "warning";
        if (d.contains("EN VUELO")) return "success";
        if (finalizado) return "secondary";
        return "info";
    }

    private static String calcularVentanaOperativa(
            Timestamp inicio, Timestamp fin, Timestamp ahora, boolean inactivo, boolean finalizado
    ) {
        if (inactivo) return "Inactivo / Cancelado";
        if (finalizado) return "Finalizado";
        if (inicio == null) return "Programado";

        long ahoraMs = ahora.getTime();
        long iniMs = inicio.getTime();
        long finMs = fin != null ? fin.getTime() : Long.MAX_VALUE;

        if (ahoraMs >= iniMs && ahoraMs <= finMs) {
            return "En progreso";
        }
        if (iniMs > ahoraMs) {
            long mins = Math.max(1L, (iniMs - ahoraMs) / 60_000L);
            if (mins < 60L) return "En " + mins + " min";
            long horas = mins / 60L;
            long remM = mins % 60L;
            if (horas < 24L) return "En " + horas + "h " + (remM < 10 ? "0" : "") + remM + "m";
            long dias = horas / 24L;
            long remH = horas % 24L;
            return "En " + dias + "d " + remH + "h";
        }
        return "Programado";
    }

    private static long calcularMinutos(Timestamp inicio, Timestamp fin) {
        if (inicio == null || fin == null || fin.before(inicio)) return 0L;
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
