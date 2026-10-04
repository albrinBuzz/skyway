package com.SkyWay.config.health;

import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabasePingService {

    @Autowired
    private DataSource dataSource;

    /**
     * Mide el tiempo de respuesta ejecutando una consulta ligera ("SELECT 1") mediante JDBC.
     *
     * @return Tiempo en milisegundos (ms) que tardó la consulta, o -1 en caso de error.
     */
    public long checkDatabaseQueryLatencyMs() {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            long duration = System.currentTimeMillis() - start;
            Logger.logInfo(String.format(" Latencia JDBC/SQL (SELECT 1): %d ms", duration));
            return duration;
        } catch (Exception e) {
            Logger.logError("Error al medir latencia JDBC: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Mide la latencia de red TCP estableciendo un Socket directo hacia el host y puerto de la BD.
     *
     * @param timeoutMs Tiempo máximo de espera en ms.
     * @return Tiempo en milisegundos (ms) del Handshake TCP, o -1 en caso de fallo.
     */
    public long checkTcpNetworkLatencyMs(int timeoutMs) {
        try (Connection conn = dataSource.getConnection()) {
            String jdbcUrl = conn.getMetaData().getURL();
            // Formato estándar: jdbc:postgresql://host:port/database
            URI uri = URI.create(jdbcUrl.substring(5)); // Remover "jdbc:"
            String host = uri.getHost();
            int port = uri.getPort() != -1 ? uri.getPort() : 5432;

            long start = System.currentTimeMillis();
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), timeoutMs);
                long duration = System.currentTimeMillis() - start;
                Logger.logInfo(String.format(" Latencia de Red TCP hacia [%s:%d]: %d ms", host, port, duration));
                return duration;
            } catch (IOException e) {
                Logger.logError(String.format(" No se pudo abrir Socket TCP hacia [%s:%d]: %s", host, port, e.getMessage()));
                return -1;
            }
        } catch (Exception e) {
            Logger.logError("Error extrayendo metadatos del DataSource: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Ejecuta una prueba completa de latencia de BD al iniciar la aplicación.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void runLatencyDiagnosticsOnStartup() {
        Logger.logInfo("==================================================");
        Logger.logInfo(" Iniciando diagnóstico de latencia de Base de Datos...");
        checkTcpNetworkLatencyMs(3000);
        checkDatabaseQueryLatencyMs();
        Logger.logInfo("==================================================");
    }
}