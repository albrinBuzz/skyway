package com.SkyWay.modules.reserva.domain.repository;

import com.SkyWay.mobile.service.MobileException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class MobileReservaQueriesImpl implements MobileReservaQueries {

    @PersistenceContext
    private EntityManager em;

    private static final String ES_PASAJERO_RESERVA = """
        (REPLACE(UPPER(TRIM(r.rut_pasajero)), '.', '') = REPLACE(UPPER(TRIM(:rut)), '.', '') OR EXISTS (
            SELECT 1 FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva
              AND REPLACE(UPPER(TRIM(pr.rut)), '.', '') = REPLACE(UPPER(TRIM(:rut)), '.', '')
        ))
    """;

    private static final String FILTRO_BUSQUEDA_RESERVA = """
        (COALESCE(:q, '') = ''
         OR UPPER(COALESCE(r.codigo_reserva, 'RES-' || r.id_reserva)) LIKE '%' || UPPER(:q) || '%'
         OR CAST(r.id_reserva AS TEXT) LIKE '%' || :q || '%'
         OR EXISTS (
             SELECT 1 FROM reserva_itinerario ri_q
             JOIN itinerario it_q ON it_q.id_itinerario = ri_q.id_itinerario
             LEFT JOIN aeropuerto ao_q ON ao_q.id_aeropuerto = it_q.origen_aeropuerto
             LEFT JOIN aeropuerto ad_q ON ad_q.id_aeropuerto = it_q.destino_aeropuerto
             LEFT JOIN ciudad co_q ON co_q.id_ciudad = ao_q.id_ciudad
             LEFT JOIN ciudad cd_q ON cd_q.id_ciudad = ad_q.id_ciudad
             WHERE ri_q.id_reserva = r.id_reserva
               AND (UPPER(COALESCE(ao_q.codigo_iata, '')) LIKE '%' || UPPER(:q) || '%'
                    OR UPPER(COALESCE(ad_q.codigo_iata, '')) LIKE '%' || UPPER(:q) || '%'
                    OR UPPER(COALESCE(co_q.nombre, '')) LIKE '%' || UPPER(:q) || '%'
                    OR UPPER(COALESCE(cd_q.nombre, '')) LIKE '%' || UPPER(:q) || '%')
         ))
    """;

    private static final String ES_RESERVA_ACTIVA = """
        (COALESCE(LOWER(er.descripcion), '') <> 'cancelada' AND EXISTS (
            SELECT 1 FROM reserva_itinerario ri
            JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
            JOIN vuelo v ON v.id_vuelo = iv.id_vuelo
            LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
            WHERE ri.id_reserva = r.id_reserva
            AND COALESCE(v.fecha_hora_llegada, v.fecha_hora_salida) >= :ahora
            AND COALESCE(LOWER(ev.estado), '') NOT IN ('cancelado', 'inactivo', 'finalizado')
        ))
    """;

    @SuppressWarnings("unchecked")
    private List<Object[]> ejecutarConsulta(String sql, Object... params) {
        Query q = em.createNativeQuery(sql);
        for (int i = 0; i < params.length; i += 2) {
            q.setParameter((String) params[i], params[i + 1]);
        }
        return q.getResultList();
    }

    @Override
    public List<Object[]> mobileReservas(String rut, boolean activas, LocalDateTime ahora, String q, int limit, int offset) {
        String filtro = (q == null) ? "" : q.trim();
        String sql = """
            SELECT r.id_reserva,
                   r.fecha_reserva,
                   r.estado_reserva,
                   er.descripcion,
                   r.total,
                   COALESCE(it_main.hora_salida, nx.fecha_hora_salida) AS proxima_salida,
                   COALESCE(ao_it.codigo_iata, ao.codigo_iata) AS origen,
                   COALESCE(ad_it.codigo_iata, ad.codigo_iata) AS destino,
                   COALESCE(r.codigo_reserva, 'RES-' || r.id_reserva) AS codigo_pnr,
                   (REPLACE(UPPER(TRIM(r.rut_pasajero)), '.', '') = REPLACE(UPPER(TRIM(:rut)), '.', '')) AS es_titular,
                   (EXISTS (SELECT 1 FROM checkin ck WHERE ck.id_reserva = r.id_reserva)
                    OR LOWER(COALESCE(er.descripcion, '')) LIKE '%check-in%') AS checkin_realizado,
                   GREATEST(1, (
                       SELECT COUNT(DISTINCT pax.rut_norm) FROM (
                           SELECT REPLACE(UPPER(TRIM(r.rut_pasajero)), '.', '') AS rut_norm
                           UNION ALL
                           SELECT REPLACE(UPPER(TRIM(pr.rut)), '.', '') AS rut_norm
                           FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva
                       ) pax WHERE pax.rut_norm <> ''
                   ))::int AS total_pasajeros
            FROM reserva r
            LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
            LEFT JOIN LATERAL (
                SELECT it.hora_salida, it.origen_aeropuerto, it.destino_aeropuerto
                FROM reserva_itinerario ri
                JOIN itinerario it ON it.id_itinerario = ri.id_itinerario
                WHERE ri.id_reserva = r.id_reserva
                ORDER BY CASE WHEN it.hora_salida >= :ahora THEN 0 ELSE 1 END, it.hora_salida ASC NULLS LAST
                LIMIT 1
            ) it_main ON true
            LEFT JOIN aeropuerto ao_it ON ao_it.id_aeropuerto = it_main.origen_aeropuerto
            LEFT JOIN aeropuerto ad_it ON ad_it.id_aeropuerto = it_main.destino_aeropuerto
            LEFT JOIN LATERAL (
                SELECT v.id_vuelo, v.fecha_hora_salida
                FROM reserva_itinerario ri 
                JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
                JOIN vuelo v ON v.id_vuelo = iv.id_vuelo
                WHERE ri.id_reserva = r.id_reserva
                ORDER BY CASE WHEN v.fecha_hora_salida >= :ahora THEN 0 ELSE 1 END, v.fecha_hora_salida ASC LIMIT 1
            ) nx ON true
            LEFT JOIN LATERAL (
                SELECT id_aeropuerto_origen FROM segmento_vuelo WHERE id_vuelo = nx.id_vuelo ORDER BY orden_segmento ASC LIMIT 1
            ) so ON true
            LEFT JOIN LATERAL (
                SELECT id_aeropuerto_destino FROM segmento_vuelo WHERE id_vuelo = nx.id_vuelo ORDER BY orden_segmento DESC LIMIT 1
            ) sd ON true
            LEFT JOIN aeropuerto ao ON ao.id_aeropuerto = so.id_aeropuerto_origen
            LEFT JOIN aeropuerto ad ON ad.id_aeropuerto = sd.id_aeropuerto_destino
            WHERE 
            """ + ES_PASAJERO_RESERVA + " AND " + (activas ? ES_RESERVA_ACTIVA : "NOT " + ES_RESERVA_ACTIVA)
                + " AND " + FILTRO_BUSQUEDA_RESERVA
                + " ORDER BY r.fecha_reserva DESC LIMIT :lim OFFSET :off";

        return ejecutarConsulta(sql, "rut", rut, "ahora", ahora, "q", filtro, "lim", limit, "off", offset);
    }

    @Override
    public List<Object[]> mobileMapa(int vueloId, int reservaId, String rut) {
        String sql = """
            SELECT a.id_asiento, a.numero_asiento, a.id_clase, c.descripcion, pa.precio,
                   CASE 
                       WHEN ra.id_reserva = :reserva AND REPLACE(UPPER(TRIM(ra.rut)), '.', '') = REPLACE(UPPER(TRIM(:rut)), '.', '') THEN 'seleccionado'
                       WHEN ra.id_reserva_asiento IS NOT NULL THEN 'ocupado'
                       ELSE 'disponible'
                   END AS estado,
                   a.fila, a.letra,
                   COALESCE(a.es_pasillo, FALSE) AS es_pasillo,
                   COALESCE(a.es_emergencia, FALSE) AS es_emergencia
            FROM vuelo v
            JOIN asiento a ON a.id_avion = v.id_avion
            LEFT JOIN clase_asiento c ON c.id_clase = a.id_clase
            LEFT JOIN precio_asiento pa ON pa.id_vuelo = v.id_vuelo AND pa.id_clase = a.id_clase
            LEFT JOIN reserva_asiento ra ON ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
            WHERE v.id_vuelo = :vuelo
            ORDER BY COALESCE(a.fila, NULLIF(regexp_replace(a.numero_asiento, '[^0-9]', '', 'g'), '')::int, 0) ASC,
                     COALESCE(NULLIF(a.letra, ''), regexp_replace(a.numero_asiento, '[0-9]', '', 'g')) ASC
        """;
        return ejecutarConsulta(sql, "vuelo", vueloId, "reserva", reservaId, "rut", rut);
    }

    @Override
    public void mobileLockVuelo(int id) {
        Query q = em.createNativeQuery("SELECT id_vuelo FROM vuelo WHERE id_vuelo = :id FOR UPDATE");
        q.setParameter("id", id);
        if (q.getResultList().isEmpty()) {
            throw MobileException.notFound();
        }
    }

    @Override
    public int mobileAsignarAsiento(int reservaId, int vueloId, String rut, int asientoId) {
        Query q = em.createNativeQuery("""
            INSERT INTO reserva_asiento (id_reserva, id_vuelo, id_asiento, rut)
            VALUES (:reserva, :vuelo, :asiento, :rut)
        """);
        q.setParameter("reserva", reservaId);
        q.setParameter("vuelo", vueloId);
        q.setParameter("asiento", asientoId);
        q.setParameter("rut", rut);
        return q.executeUpdate();
    }

    @Override
    public int mobileCambiarAsiento(int asignacionId, int reservaId, int vueloId, String rut, int nuevoAsientoId) {
        Query q = em.createNativeQuery("""
            UPDATE reserva_asiento 
            SET id_asiento = :nuevoAsiento
            WHERE id_reserva_asiento = :asignacion AND id_reserva = :reserva AND id_vuelo = :vuelo AND rut = :rut
        """);
        q.setParameter("nuevoAsiento", nuevoAsientoId);
        q.setParameter("asignacion", asignacionId);
        q.setParameter("reserva", reservaId);
        q.setParameter("vuelo", vueloId);
        q.setParameter("rut", rut);
        return q.executeUpdate();
    }

    @Override
    public void mobileInsertCheckin(int reservaId, LocalDateTime ahora) {
        String bpCode = "BP-MOB-" + reservaId + "-" + System.currentTimeMillis();
        Number idCheckinNum = (Number) em.createNativeQuery("""
            INSERT INTO checkin (id_reserva, fecha_hora, metodo, estado, codigo_boarding_pass)
            VALUES (:id, :ahora, 'App', 'COMPLETADO', :bpCode)
            RETURNING id_checkin
        """)
        .setParameter("id", reservaId)
        .setParameter("ahora", ahora)
        .setParameter("bpCode", bpCode)
        .getSingleResult();

        int idCheckin = idCheckinNum.intValue();

        em.createNativeQuery("""
            INSERT INTO checkin_pasajero (id_checkin, rut, declaracion_equipaje_aceptada, fecha_registro)
            SELECT :p_checkin, p.rut, TRUE, NOW()
            FROM (
                SELECT r.rut_pasajero AS rut FROM reserva r WHERE r.id_reserva = :p_reserva
                UNION
                SELECT pr.rut AS rut FROM pasajero_reserva pr WHERE pr.id_reserva = :p_reserva
                UNION
                SELECT ra.rut AS rut FROM reserva_asiento ra WHERE ra.id_reserva = :p_reserva
            ) p
            JOIN pasajero pas ON pas.rut = p.rut
            WHERE p.rut IS NOT NULL
            ON CONFLICT (id_checkin, rut) DO NOTHING
        """)
        .setParameter("p_checkin", idCheckin)
        .setParameter("p_reserva", reservaId)
        .executeUpdate();

        Query qEstado = em.createNativeQuery("""
            UPDATE reserva 
            SET estado_reserva = (SELECT id_estado_reserva FROM estado_reserva WHERE LOWER(descripcion) = 'check-in realizado' LIMIT 1)
            WHERE id_reserva = :id
        """);
        qEstado.setParameter("id", reservaId);
        qEstado.executeUpdate();
    }

    @Override
    public long mobileCountReservas(String rut, boolean activas, LocalDateTime ahora, String q) {
        String filtro = (q == null) ? "" : q.trim();
        Query qr = em.createNativeQuery("SELECT count(*) FROM reserva r LEFT JOIN estado_reserva er ON er.id_estado_reserva=r.estado_reserva WHERE "
                + ES_PASAJERO_RESERVA + " AND " + (activas ? ES_RESERVA_ACTIVA : "NOT " + ES_RESERVA_ACTIVA)
                + " AND " + FILTRO_BUSQUEDA_RESERVA);
        qr.setParameter("rut", rut);
        qr.setParameter("ahora", ahora);
        qr.setParameter("q", filtro);
        return ((Number) qr.getSingleResult()).longValue();
    }

    @Override public Object[] mobileReserva(String rut, int id, boolean lock) {
        if (lock) {
            Query qLock = em.createNativeQuery("SELECT r.id_reserva FROM reserva r WHERE r.id_reserva=:id AND " + ES_PASAJERO_RESERVA + " FOR UPDATE OF r");
            qLock.setParameter("id", id);
            qLock.setParameter("rut", rut);
            if (qLock.getResultList().isEmpty()) throw MobileException.notFound();
        }
        return ejecutarConsulta("SELECT r.id_reserva, r.fecha_reserva, r.estado_reserva, er.descripcion, r.total, r.rut_pasajero, COALESCE(r.codigo_reserva, 'RES-' || r.id_reserva) FROM reserva r LEFT JOIN estado_reserva er ON er.id_estado_reserva=r.estado_reserva WHERE r.id_reserva=:id AND " + ES_PASAJERO_RESERVA, "id", id, "rut", rut).stream().findFirst().orElseThrow(MobileException::notFound);
    }

    @Override public List<Object[]> mobileItinerarios(int id) {
        return ejecutarConsulta("SELECT ri.id_reserva_itinerario, i.id_itinerario, i.hora_salida, i.hora_llegada, i.numero_escalas, i.precio_base, ao.id_aeropuerto, ao.nombre_aeropuerto, ao.codigo_iata, co.nombre, po.nombre, ST_Y(ao.posicion::geometry), ST_X(ao.posicion::geometry), ad.id_aeropuerto, ad.nombre_aeropuerto, ad.codigo_iata, cd.nombre, pd.nombre, ST_Y(ad.posicion::geometry), ST_X(ad.posicion::geometry), it.id_itinerario_tarifa, t.id_tarifa, t.nombre, it.precio FROM reserva_itinerario ri JOIN itinerario i ON i.id_itinerario=ri.id_itinerario JOIN itinerario_tarifa it ON it.id_itinerario_tarifa=ri.id_itinerario_tarifa JOIN tarifa t ON t.id_tarifa=it.id_tarifa LEFT JOIN aeropuerto ao ON ao.id_aeropuerto=i.origen_aeropuerto LEFT JOIN ciudad co ON co.id_ciudad=ao.id_ciudad LEFT JOIN pais po ON po.id_pais=co.id_pais LEFT JOIN aeropuerto ad ON ad.id_aeropuerto=i.destino_aeropuerto LEFT JOIN ciudad cd ON cd.id_ciudad=ad.id_ciudad LEFT JOIN pais pd ON pd.id_pais=cd.id_pais WHERE ri.id_reserva=:id ORDER BY i.hora_salida NULLS LAST", "id", id);
    }

    @Override
    public List<Object[]> mobileConexiones(int id) {
        String sql = """
            SELECT DISTINCT
                iv.id_itinerario,
                iv.id_itinerario_vuelo,
                iv.orden,
                CAST(iv.tiempo_espera AS text),
                iv.tipo_conexion,
                v.id_vuelo,
                v.numero_vuelo,
                v.fecha_hora_salida,
                v.fecha_hora_llegada,
                v.id_avion,
                ev.id_estado_vuelo,
                ev.estado,
                ev.descripcion,
                NULLIF(BTRIM(ma.nombre), '') AS modelo_avion
            FROM reserva_itinerario ri
            JOIN itinerario_vuelo iv ON iv.id_itinerario = ri.id_itinerario
            JOIN vuelo v ON v.id_vuelo = iv.id_vuelo
            LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo = v.id_estado_vuelo
            LEFT JOIN avion a ON a.id_avion = v.id_avion
            LEFT JOIN modelo_avion ma ON ma.id_modelo = a.id_modelo
            WHERE ri.id_reserva = :id
            ORDER BY iv.id_itinerario, iv.orden
            """;
        return ejecutarConsulta(sql, "id", id);
    }

    @Override public List<Object[]> mobileSegmentos(int id) {
        return ejecutarConsulta("SELECT DISTINCT s.id_vuelo, s.id_segmento, s.orden_segmento, s.hora_salida, s.hora_llegada, ao.id_aeropuerto, ao.nombre_aeropuerto, ao.codigo_iata, co.nombre, po.nombre, ST_Y(ao.posicion::geometry), ST_X(ao.posicion::geometry), ad.id_aeropuerto, ad.nombre_aeropuerto, ad.codigo_iata, cd.nombre, pd.nombre, ST_Y(ad.posicion::geometry), ST_X(ad.posicion::geometry), p.id_puerta, p.codigo_puerta, p.terminal, p.id_aeropuerto FROM reserva_itinerario ri JOIN itinerario_vuelo iv ON iv.id_itinerario=ri.id_itinerario JOIN segmento_vuelo s ON s.id_vuelo=iv.id_vuelo LEFT JOIN aeropuerto ao ON ao.id_aeropuerto=s.id_aeropuerto_origen LEFT JOIN ciudad co ON co.id_ciudad=ao.id_ciudad LEFT JOIN pais po ON po.id_pais=co.id_pais LEFT JOIN aeropuerto ad ON ad.id_aeropuerto=s.id_aeropuerto_destino LEFT JOIN ciudad cd ON cd.id_ciudad=ad.id_ciudad LEFT JOIN pais pd ON pd.id_pais=cd.id_pais LEFT JOIN asignacion_puerta ap ON ap.id_segmento=s.id_segmento LEFT JOIN puerta_embarque p ON p.id_puerta=ap.id_puerta WHERE ri.id_reserva=:id ORDER BY s.id_vuelo, s.orden_segmento", "id", id);
    }

    @Override public List<Object[]> mobileCaracteristicas(int id) {
        return ejecutarConsulta("SELECT DISTINCT tc.id_tarifa, tc.id_tarifa_caracteristica, c.id_caracteristica, c.nombre, c.descripcion, c.tipo_dato, tc.valor, tc.valor_bool, tc.valor_int FROM reserva_itinerario ri JOIN itinerario_tarifa it ON it.id_itinerario_tarifa=ri.id_itinerario_tarifa JOIN tarifa_caracteristica tc ON tc.id_tarifa=it.id_tarifa JOIN caracteristica_tarifa c ON c.id_caracteristica=tc.id_caracteristica WHERE ri.id_reserva=:id ORDER BY tc.id_tarifa, c.id_caracteristica", "id", id);
    }

    @Override public List<Object[]> mobileEquipajes(int id, String rut) {
        return ejecutarConsulta("SELECT e.id_equipaje, e.peso, e.dimensiones, e.tipo, e.id_tipo, t.nombre FROM equipaje e LEFT JOIN tipo_equipaje t ON t.id_tipo=e.id_tipo WHERE e.id_reserva=:id AND REPLACE(UPPER(TRIM(e.rut_pasajero)), '.', '')=REPLACE(UPPER(TRIM(:rut)), '.', '') ORDER BY e.id_equipaje", "id", id, "rut", rut);
    }

    @Override public List<Object[]> mobileAsignados(int id, String rut) {
        return ejecutarConsulta("SELECT ra.id_vuelo, ra.id_reserva_asiento, a.id_asiento, a.numero_asiento, a.id_clase, c.descripcion, pa.precio FROM reserva_asiento ra JOIN asiento a ON a.id_asiento=ra.id_asiento JOIN vuelo v ON v.id_vuelo=ra.id_vuelo AND v.id_avion=a.id_avion LEFT JOIN clase_asiento c ON c.id_clase=a.id_clase LEFT JOIN precio_asiento pa ON pa.id_vuelo=v.id_vuelo AND pa.id_clase=a.id_clase WHERE ra.id_reserva=:id AND REPLACE(UPPER(TRIM(ra.rut)), '.', '')=REPLACE(UPPER(TRIM(:rut)), '.', '') ORDER BY ra.id_vuelo", "id", id, "rut", rut);
    }

    @Override public List<Object[]> mobileCheckins(int id) {
        return ejecutarConsulta("SELECT id_checkin, id_reserva, fecha_hora, metodo FROM checkin WHERE id_reserva=:id ORDER BY id_checkin LIMIT 1", "id", id);
    }

    @Override public List<Object[]> mobileNext(String rut, LocalDateTime ahora) {
        return ejecutarConsulta("SELECT DISTINCT r.id_reserva, v.id_vuelo, v.fecha_hora_salida FROM reserva r JOIN estado_reserva er ON er.id_estado_reserva=r.estado_reserva JOIN reserva_itinerario ri ON ri.id_reserva=r.id_reserva JOIN itinerario_vuelo iv ON iv.id_itinerario=ri.id_itinerario JOIN vuelo v ON v.id_vuelo=iv.id_vuelo JOIN estado_vuelo ev ON ev.id_estado_vuelo=v.id_estado_vuelo WHERE " + ES_PASAJERO_RESERVA + " AND LOWER(er.descripcion)<>'cancelada' AND v.fecha_hora_salida>:ahora AND LOWER(ev.estado) NOT IN ('cancelado','inactivo','finalizado') ORDER BY v.fecha_hora_salida ASC LIMIT 1", "rut", rut, "ahora", ahora);
    }

    @Override public List<Object[]> mobileFaltanAsientos(int reserva, LocalDateTime ahora) {
        return ejecutarConsulta("WITH pasajeros AS (SELECT rut FROM pasajero_reserva WHERE id_reserva=:id UNION SELECT rut FROM reserva_asiento WHERE id_reserva=:id UNION SELECT rut_pasajero FROM reserva WHERE id_reserva=:id AND NOT EXISTS(SELECT 1 FROM pasajero_reserva WHERE id_reserva=:id)), vuelos AS (SELECT DISTINCT iv.id_vuelo FROM reserva_itinerario ri JOIN itinerario_vuelo iv ON iv.id_itinerario=ri.id_itinerario JOIN vuelo v ON v.id_vuelo=iv.id_vuelo WHERE ri.id_reserva=:id AND v.fecha_hora_salida>:ahora) SELECT p.rut, v.id_vuelo FROM pasajeros p CROSS JOIN vuelos v WHERE (SELECT count(*) FROM reserva_asiento ra JOIN asiento a ON a.id_asiento=ra.id_asiento JOIN vuelo vl ON vl.id_vuelo=ra.id_vuelo AND vl.id_avion=a.id_avion WHERE ra.id_reserva=:id AND ra.rut=p.rut AND ra.id_vuelo=v.id_vuelo)<>1", "id", reserva, "ahora", ahora);
    }
}