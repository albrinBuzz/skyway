package com.SkyWay.config.health;

import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Recopila telemetría operacional en vivo del negocio SkyWay con caché TTL de 30s
 * y refresco asíncrono no bloqueante para no frenar la UI ni saturar Azure F1 / PostgreSQL.
 */
@Component
public class AirlineOperationalTelemetryTracker {

    private static final long OPS_CACHE_TTL_MS = 30_000L;

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final ObjectProvider<AsientoCacheService> asientoCacheProvider;
    private volatile long lastOpsSqlTimestamp = 0L;
    private volatile Map<String, Object> cachedSqlOps = crearMapaVacioPorDefecto();
    private final AtomicBoolean refrescoEnCurso = new AtomicBoolean(false);

    public AirlineOperationalTelemetryTracker(
            ObjectProvider<DataSource> dataSourceProvider,
            ObjectProvider<AsientoCacheService> asientoCacheProvider
    ) {
        this.dataSourceProvider = dataSourceProvider;
        this.asientoCacheProvider = asientoCacheProvider;
    }

    public Map<String, Object> getCachedOrQuickMetrics() {
        Map<String, Object> ops = new LinkedHashMap<>(cachedSqlOps);
        agregarMetricasMemoria(ops);
        return ops;
    }

    public boolean necesitaRefresco() {
        return lastOpsSqlTimestamp == 0L || (System.currentTimeMillis() - lastOpsSqlTimestamp) >= OPS_CACHE_TTL_MS;
    }

    public Map<String, Object> collectMetrics() {
        return collectMetrics(false);
    }

    public Map<String, Object> collectMetrics(boolean forzarRefresco) {
        Map<String, Object> ops = new LinkedHashMap<>();
        agregarMetricasMemoria(ops);

        DataSource ds = dataSourceProvider.getIfAvailable();
        if (ds == null) {
            ops.putAll(crearMapaVacioPorDefecto());
            agregarMetricasMemoria(ops);
            return ops;
        }

        if (forzarRefresco || necesitaRefresco()) {
            refrescarDesdeBd();
        }

        ops.putAll(cachedSqlOps);
        agregarMetricasMemoria(ops);
        return ops;
    }

    public void refrescarDesdeBd() {
        if (!refrescoEnCurso.compareAndSet(false, true)) {
            return;
        }
        try {
            DataSource ds = dataSourceProvider.getIfAvailable();
            if (ds == null) {
                return;
            }
            Map<String, Object> freshSql = new LinkedHashMap<>(crearMapaVacioPorDefecto());
            try (Connection conn = ds.getConnection()) {
                cargarConteosRapidos(conn, freshSql);
                cargarPagosYCheckinsDesglose(conn, freshSql);
                cargarProximosVuelos(conn, freshSql);
                cargarAuditoriaReciente(conn, freshSql);
            } catch (Exception ignored) {
            }
            cachedSqlOps = freshSql;
            lastOpsSqlTimestamp = System.currentTimeMillis();
        } finally {
            refrescoEnCurso.set(false);
        }
    }

    private void agregarMetricasMemoria(Map<String, Object> ops) {
        AsientoCacheService cache = asientoCacheProvider.getIfAvailable();
        ops.put("cached_flight_seatmaps", cache != null ? cache.getCantidadVuelosEnCache() : 0);
        ops.put("active_seat_locks", cache != null ? cache.getCantidadAsientosRetenidos() : 0);
    }

    private Map<String, Object> crearMapaVacioPorDefecto() {
        Map<String, Object> def = new LinkedHashMap<>();
        def.put("reservas_hoy", 0);
        def.put("reservas_24h", 0);
        def.put("ingresos_hoy", 0);
        def.put("ingresos_24h", 0);
        def.put("checkins_hoy", 0);
        def.put("vuelos_24h", 0);
        def.put("vuelos_en_aire", 0);
        def.put("itinerarios_24h", 0);
        def.put("directos_24h", 0);
        def.put("aeropuertos_activos", 0);
        def.put("aviones_activos", 0);
        def.put("usuarios_hoy", 0);
        def.put("notificaciones_hoy", 0);
        def.put("notificaciones_pendientes", 0);
        def.put("auditoria_24h", 0);
        def.put("reembolsos_pendientes", 0);
        def.put("monto_reembolsos_pendientes", 0);
        def.put("equipajes_hoy", 0);
        def.put("peso_equipaje_hoy_kg", 0);
        def.put("tokens_recuperacion_activos", 0);
        def.put("checkins_by_channel_hoy", Map.of());
        def.put("payments_by_method_24h", List.of());
        def.put("upcoming_flights", List.of());
        def.put("recent_audit_events", List.of());
        return def;
    }

    private void cargarConteosRapidos(Connection conn, Map<String, Object> ops) {
        // Consultas separadas y sargables con timeout de 1s para evitar bloqueos en tablas masivas
        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int FROM Vuelo WHERE Fecha_Hora_Salida >= NOW() AND Fecha_Hora_Salida <= NOW() + INTERVAL '24 hours'",
                rs -> ops.put("vuelos_24h", rs.getInt(1)));

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int FROM Vuelo WHERE Fecha_Hora_Salida <= NOW() AND Fecha_Hora_Llegada >= NOW()",
                rs -> ops.put("vuelos_en_aire", rs.getInt(1)));

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int AS tot, COALESCE(SUM(CASE WHEN COALESCE(NUMERO_ESCALAS, 0) = 0 THEN 1 ELSE 0 END), 0)::int AS dir FROM Itinerario WHERE HORA_SALIDA >= NOW() AND HORA_SALIDA <= NOW() + INTERVAL '24 hours'",
                rs -> {
                    ops.put("itinerarios_24h", rs.getInt("tot"));
                    ops.put("directos_24h", rs.getInt("dir"));
                });

        ejecutarConteoSeguro(conn,
                "SELECT (SELECT COUNT(*)::int FROM Aeropuerto WHERE activo = TRUE), (SELECT COUNT(*)::int FROM Avion WHERE activo = TRUE)",
                rs -> {
                    ops.put("aeropuertos_activos", rs.getInt(1));
                    ops.put("aviones_activos", rs.getInt(2));
                });

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int AS hoy, (SELECT COUNT(*)::int FROM Reserva WHERE Fecha_Reserva >= NOW() - INTERVAL '24 hours') AS h24 FROM Reserva WHERE Fecha_Reserva >= CURRENT_DATE",
                rs -> {
                    ops.put("reservas_hoy", rs.getInt("hoy"));
                    ops.put("reservas_24h", rs.getInt("h24"));
                });

        ejecutarConteoSeguro(conn,
                "SELECT COALESCE(SUM(CASE WHEN Fecha >= CURRENT_DATE THEN monto_pagado ELSE 0 END), 0) AS hoy, COALESCE(SUM(monto_pagado), 0) AS h24 FROM Pago WHERE Fecha >= NOW() - INTERVAL '24 hours'",
                rs -> {
                    ops.put("ingresos_hoy", rs.getBigDecimal("hoy"));
                    ops.put("ingresos_24h", rs.getBigDecimal("h24"));
                });

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int FROM Checkin WHERE Fecha_Hora >= CURRENT_DATE",
                rs -> ops.put("checkins_hoy", rs.getInt(1)));

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int FROM Usuario WHERE Fecha_Registro >= CURRENT_DATE",
                rs -> ops.put("usuarios_hoy", rs.getInt(1)));

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int FROM Log_Auditoria WHERE fecha_hora >= NOW() - INTERVAL '24 hours'",
                rs -> ops.put("auditoria_24h", rs.getInt(1)));

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int, COALESCE(SUM(MONTO), 0) FROM Reembolso WHERE ESTADO IN ('SOLICITADO', 'DATOS_BANCARIOS_ENVIADOS', 'DATOS_CONFIRMADOS')",
                rs -> {
                    ops.put("reembolsos_pendientes", rs.getInt(1));
                    ops.put("monto_reembolsos_pendientes", rs.getBigDecimal(2));
                });

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int, COALESCE(SUM(e.Peso), 0) FROM Equipaje e JOIN Reserva r ON r.ID_RESERVA = e.ID_RESERVA WHERE r.Fecha_Reserva >= CURRENT_DATE",
                rs -> {
                    ops.put("equipajes_hoy", rs.getInt(1));
                    ops.put("peso_equipaje_hoy_kg", rs.getBigDecimal(2));
                });

        ejecutarConteoSeguro(conn,
                "SELECT (SELECT COUNT(*)::int FROM Notificacion WHERE Fecha >= CURRENT_DATE), (SELECT COUNT(*)::int FROM Notificacion WHERE enviada = FALSE)",
                rs -> {
                    ops.put("notificaciones_hoy", rs.getInt(1));
                    ops.put("notificaciones_pendientes", rs.getInt(2));
                });

        ejecutarConteoSeguro(conn,
                "SELECT COUNT(*)::int FROM Token_Recuperacion WHERE usado = FALSE AND revocado = FALSE AND fecha_expiracion > NOW()",
                rs -> ops.put("tokens_recuperacion_activos", rs.getInt(1)));
    }

    private interface RowHandler {
        void handle(ResultSet rs) throws Exception;
    }

    private void ejecutarConteoSeguro(Connection conn, String sql, RowHandler handler) {
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    handler.handle(rs);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void cargarPagosYCheckinsDesglose(Connection conn, Map<String, Object> ops) {
        String sqlPagos = """
                SELECT COALESCE(mp.Descripcion, 'Otro') AS metodo,
                       COUNT(*)::int AS cantidad,
                       COALESCE(SUM(p.monto_pagado), 0) AS total
                FROM Pago p
                LEFT JOIN Metodo_Pago mp ON mp.ID_METODO_PAGO = p.ID_METODO_PAGO
                WHERE p.Fecha >= NOW() - INTERVAL '24 hours'
                GROUP BY mp.Descripcion
                ORDER BY total DESC
                """;
        List<Map<String, Object>> pagos = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sqlPagos)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("method", rs.getString("metodo"));
                    item.put("count", rs.getInt("cantidad"));
                    item.put("total", rs.getBigDecimal("total"));
                    pagos.add(item);
                }
            }
        } catch (Exception ignored) {
        }
        ops.put("payments_by_method_24h", pagos);

        String sqlCheckin = """
                SELECT Metodo AS canal, COUNT(*)::int AS cantidad
                FROM Checkin
                WHERE Fecha_Hora >= CURRENT_DATE
                GROUP BY Metodo
                """;
        Map<String, Integer> canales = new LinkedHashMap<>();
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sqlCheckin)) {
                while (rs.next()) {
                    canales.put(rs.getString("canal"), rs.getInt("cantidad"));
                }
            }
        } catch (Exception ignored) {
        }
        ops.put("checkins_by_channel_hoy", canales);
    }

    private void cargarProximosVuelos(Connection conn, Map<String, Object> ops) {
        String sql = """
                SELECT
                    COALESCE(v.Numero_Vuelo, 'SW-' || v.ID_VUELO) AS vuelo,
                    TO_CHAR(v.Fecha_Hora_Salida, 'DD/MM HH24:MI') AS salida_fmt,
                    COALESCE(ev.Descripcion, 'Programado') AS estado,
                    COALESCE((
                        SELECT ao.Codigo_IATA || ' → ' || ad.Codigo_IATA
                        FROM Segmento_Vuelo sv
                        JOIN Aeropuerto ao ON ao.ID_AEROPUERTO = sv.ID_AEROPUERTO_ORIGEN
                        JOIN Aeropuerto ad ON ad.ID_AEROPUERTO = sv.ID_AEROPUERTO_DESTINO
                        WHERE sv.ID_VUELO = v.ID_VUELO
                        ORDER BY sv.ORDEN_SEGMENTO ASC
                        LIMIT 1
                    ), '-') AS ruta,
                    COALESCE(av.Numero_de_Registro, '-') AS aeronave
                FROM Vuelo v
                LEFT JOIN Estado_Vuelo ev ON ev.ID_ESTADO_VUELO = v.ID_ESTADO_VUELO
                LEFT JOIN Avion av ON av.ID_AVION = v.ID_AVION
                WHERE v.Fecha_Hora_Salida >= NOW() - INTERVAL '2 hours'
                  AND v.Fecha_Hora_Salida <= NOW() + INTERVAL '24 hours'
                ORDER BY v.Fecha_Hora_Salida ASC
                LIMIT 6
                """;
        List<Map<String, Object>> vuelos = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("flight_number", rs.getString("vuelo"));
                    item.put("departure", rs.getString("salida_fmt"));
                    item.put("status", rs.getString("estado"));
                    item.put("route", rs.getString("ruta"));
                    item.put("aircraft", rs.getString("aeronave"));
                    vuelos.add(item);
                }
            }
        } catch (Exception ignored) {
        }
        ops.put("upcoming_flights", vuelos);
    }

    private void cargarAuditoriaReciente(Connection conn, Map<String, Object> ops) {
        String sql = """
                SELECT
                    TO_CHAR(fecha_hora AT TIME ZONE COALESCE(current_setting('TIMEZONE', true), 'UTC'), 'DD/MM HH24:MI:SS') AS fecha_fmt,
                    accion,
                    nivel::text AS nivel_txt,
                    COALESCE(tabla_afectada, '-') AS tabla,
                    COALESCE(rut_usuario, 'SYSTEM') AS rut,
                    COALESCE(direccion_ip, '-') AS ip
                FROM Log_Auditoria
                ORDER BY fecha_hora DESC
                LIMIT 6
                """;
        List<Map<String, Object>> eventos = new ArrayList<>();
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(1);
            try (ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("fecha", rs.getString("fecha_fmt"));
                    item.put("accion", rs.getString("accion"));
                    item.put("nivel", rs.getString("nivel_txt"));
                    item.put("tabla", rs.getString("tabla"));
                    item.put("rut", rs.getString("rut"));
                    item.put("ip", rs.getString("ip"));
                    eventos.add(item);
                }
            }
        } catch (Exception ignored) {
        }
        ops.put("recent_audit_events", eventos);
    }
}
