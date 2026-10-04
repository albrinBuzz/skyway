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
        (r.rut_pasajero = :rut OR EXISTS (
            SELECT 1 FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva AND pr.rut = :rut
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
    public List<Object[]> mobileReservas(String rut, boolean activas, LocalDateTime ahora, int limit, int offset) {
        String sql = """
            SELECT r.id_reserva, r.fecha_reserva, r.estado_reserva, er.descripcion, r.total,
                   nx.fecha_hora_salida, ao.codigo_iata AS origen, ad.codigo_iata AS destino
            FROM reserva r
            LEFT JOIN estado_reserva er ON er.id_estado_reserva = r.estado_reserva
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
            """ + ES_PASAJERO_RESERVA + " AND " + (activas ? ES_RESERVA_ACTIVA : "NOT " + ES_RESERVA_ACTIVA) +
                " ORDER BY r.fecha_reserva DESC LIMIT :lim OFFSET :off";

        return ejecutarConsulta(sql, "rut", rut, "ahora", ahora, "lim", limit, "off", offset);
    }

    @Override
    public List<Object[]> mobileMapa(int vueloId, int reservaId, String rut) {
        String sql = """
            SELECT a.id_asiento, a.numero_asiento, a.id_clase, c.descripcion, pa.precio,
                   CASE 
                       WHEN ra.id_reserva = :reserva AND ra.rut = :rut THEN 'seleccionado'
                       WHEN ra.id_reserva_asiento IS NOT NULL THEN 'ocupado'
                       ELSE 'disponible'
                   END AS estado
            FROM vuelo v
            JOIN asiento a ON a.id_avion = v.id_avion
            LEFT JOIN clase_asiento c ON c.id_clase = a.id_clase
            LEFT JOIN precio_asiento pa ON pa.id_vuelo = v.id_vuelo AND pa.id_clase = a.id_clase
            LEFT JOIN reserva_asiento ra ON ra.id_vuelo = v.id_vuelo AND ra.id_asiento = a.id_asiento
            WHERE v.id_vuelo = :vuelo
            ORDER BY a.fila ASC, a.letra ASC
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
        Query qCheckin = em.createNativeQuery("INSERT INTO checkin (id_reserva, fecha_hora, metodo) VALUES (:id, :ahora, 'App')");
        qCheckin.setParameter("id", reservaId);
        qCheckin.setParameter("ahora", ahora);
        qCheckin.executeUpdate();

        Query qEstado = em.createNativeQuery("""
            UPDATE reserva 
            SET estado_reserva = (SELECT id_estado_reserva FROM estado_reserva WHERE LOWER(descripcion) = 'check-in realizado' LIMIT 1)
            WHERE id_reserva = :id
        """);
        qEstado.setParameter("id", reservaId);
        qEstado.executeUpdate();
    }

    @Override
    public long mobileCountReservas(String rut, boolean activas, LocalDateTime ahora) {
        Query q = em.createNativeQuery("SELECT count(*) FROM reserva r LEFT JOIN estado_reserva er ON er.id_estado_reserva=r.estado_reserva WHERE " + ES_PASAJERO_RESERVA + " AND " + (activas ? ES_RESERVA_ACTIVA : "NOT " + ES_RESERVA_ACTIVA));
        q.setParameter("rut", rut);
        q.setParameter("ahora", ahora);
        return ((Number) q.getSingleResult()).longValue();
    }

    @Override public Object[] mobileReserva(String rut, int id, boolean lock) {
        if (lock) {
            Query qLock = em.createNativeQuery("SELECT r.id_reserva FROM reserva r WHERE r.id_reserva=:id AND " + ES_PASAJERO_RESERVA + " FOR UPDATE OF r");
            qLock.setParameter("id", id);
            qLock.setParameter("rut", rut);
            if (qLock.getResultList().isEmpty()) throw MobileException.notFound();
        }
        return ejecutarConsulta("SELECT r.id_reserva, r.fecha_reserva, r.estado_reserva, er.descripcion, r.total, r.rut_pasajero FROM reserva r LEFT JOIN estado_reserva er ON er.id_estado_reserva=r.estado_reserva WHERE r.id_reserva=:id AND " + ES_PASAJERO_RESERVA, "id", id, "rut", rut).stream().findFirst().orElseThrow(MobileException::notFound);
    }

    @Override public List<Object[]> mobileItinerarios(int id) {
        return ejecutarConsulta("SELECT ri.id_reserva_itinerario, i.id_itinerario, i.hora_salida, i.hora_llegada, i.numero_escalas, i.precio_base, ao.id_aeropuerto, ao.nombre_aeropuerto, ao.codigo_iata, ad.id_aeropuerto, ad.nombre_aeropuerto, ad.codigo_iata, it.id_itinerario_tarifa, t.id_tarifa, t.nombre, it.precio FROM reserva_itinerario ri JOIN itinerario i ON i.id_itinerario=ri.id_itinerario JOIN itinerario_tarifa it ON it.id_itinerario_tarifa=ri.id_itinerario_tarifa JOIN tarifa t ON t.id_tarifa=it.id_tarifa LEFT JOIN aeropuerto ao ON ao.id_aeropuerto=i.origen_aeropuerto LEFT JOIN aeropuerto ad ON ad.id_aeropuerto=i.destino_aeropuerto WHERE ri.id_reserva=:id ORDER BY i.hora_salida NULLS LAST", "id", id);
    }

    @Override public List<Object[]> mobileConexiones(int id) {
        return ejecutarConsulta("SELECT DISTINCT iv.id_itinerario, iv.id_itinerario_vuelo, iv.orden, CAST(iv.tiempo_espera AS text), iv.tipo_conexion, v.id_vuelo, v.numero_vuelo, v.fecha_hora_salida, v.fecha_hora_llegada, v.id_avion, ev.id_estado_vuelo, ev.estado, ev.descripcion FROM reserva_itinerario ri JOIN itinerario_vuelo iv ON iv.id_itinerario=ri.id_itinerario JOIN vuelo v ON v.id_vuelo=iv.id_vuelo LEFT JOIN estado_vuelo ev ON ev.id_estado_vuelo=v.id_estado_vuelo WHERE ri.id_reserva=:id ORDER BY iv.id_itinerario, iv.orden", "id", id);
    }

    @Override public List<Object[]> mobileSegmentos(int id) {
        return ejecutarConsulta("SELECT DISTINCT s.id_vuelo, s.id_segmento, s.orden_segmento, s.hora_salida, s.hora_llegada, ao.id_aeropuerto, ao.nombre_aeropuerto, ao.codigo_iata, ad.id_aeropuerto, ad.nombre_aeropuerto, ad.codigo_iata, p.id_puerta, p.codigo_puerta, p.terminal, p.id_aeropuerto FROM reserva_itinerario ri JOIN itinerario_vuelo iv ON iv.id_itinerario=ri.id_itinerario JOIN segmento_vuelo s ON s.id_vuelo=iv.id_vuelo LEFT JOIN aeropuerto ao ON ao.id_aeropuerto=s.id_aeropuerto_origen LEFT JOIN aeropuerto ad ON ad.id_aeropuerto=s.id_aeropuerto_destino LEFT JOIN asignacion_puerta ap ON ap.id_segmento=s.id_segmento LEFT JOIN puerta_embarque p ON p.id_puerta=ap.id_puerta WHERE ri.id_reserva=:id ORDER BY s.id_vuelo, s.orden_segmento", "id", id);
    }

    @Override public List<Object[]> mobileCaracteristicas(int id) {
        return ejecutarConsulta("SELECT DISTINCT tc.id_tarifa, tc.id_tarifa_caracteristica, c.id_caracteristica, c.nombre, c.descripcion, c.tipo_dato, tc.valor, tc.valor_bool, tc.valor_int FROM reserva_itinerario ri JOIN itinerario_tarifa it ON it.id_itinerario_tarifa=ri.id_itinerario_tarifa JOIN tarifa_caracteristica tc ON tc.id_tarifa=it.id_tarifa JOIN caracteristica_tarifa c ON c.id_caracteristica=tc.id_caracteristica WHERE ri.id_reserva=:id ORDER BY tc.id_tarifa, c.id_caracteristica", "id", id);
    }

    @Override public List<Object[]> mobileEquipajes(int id, String rut) {
        return ejecutarConsulta("SELECT e.id_equipaje, e.peso, e.dimensiones, e.tipo, e.id_tipo, t.nombre FROM equipaje e LEFT JOIN tipo_equipaje t ON t.id_tipo=e.id_tipo WHERE e.id_reserva=:id AND e.rut_pasajero=:rut ORDER BY e.id_equipaje", "id", id, "rut", rut);
    }

    @Override public List<Object[]> mobileAsignados(int id, String rut) {
        return ejecutarConsulta("SELECT ra.id_vuelo, ra.id_reserva_asiento, a.id_asiento, a.numero_asiento, a.id_clase, c.descripcion FROM reserva_asiento ra JOIN asiento a ON a.id_asiento=ra.id_asiento JOIN vuelo v ON v.id_vuelo=ra.id_vuelo AND v.id_avion=a.id_avion LEFT JOIN clase_asiento c ON c.id_clase=a.id_clase WHERE ra.id_reserva=:id AND ra.rut=:rut ORDER BY ra.id_vuelo", "id", id, "rut", rut);
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