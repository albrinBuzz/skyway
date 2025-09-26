package com.SkyWay.modules.notificacion.domain.service;

import com.SkyWay.config.dataBase.PgNotifyConfig;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.Executors;

@Service
public class NotificacionListenerService {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PgNotifyConfig config;

    @Autowired
    private JavaMailSender mailSender;

    @PostConstruct
    public void startListener() {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                Connection conn = DriverManager.getConnection(
                        config.getJdbcUrl(),
                        config.getUsername(),
                        config.getPassword()
                );

                //Connection conn = DataSourceUtils.getConnection(dataSource);

                PGConnection pgconn = conn.unwrap(PGConnection.class);

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN nuevo_correo");
                }

                Logger.logInfo("📡 Escuchando canal 'nuevo_correo'...");

                while (true) {
                    PGNotification[] notifications = pgconn.getNotifications();
                    //Logger.logInfo(Arrays.toString(notifications));
                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            String idNotificacion = notification.getParameter();
                            Logger.logInfo("🔔 Notificación recibida: ID = " + idNotificacion);
                            procesarNotificacion(conn, idNotificacion);
                        }
                    }

                    // Esperar 1 segundo para no saturar el CPU
                    Thread.sleep(1000);

                    // Requiere una consulta vacía para que PostgreSQL envíe notificaciones
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void procesarNotificacion(Connection conn, String idNotificacion) {
        String query = """
            SELECT n.id_notificacion, n.titulo, n.mensaje, u.correo_electronico
            FROM notificacion n
            JOIN usuario u ON u.rut = n.rut_destinatario
            WHERE n.id_notificacion = ? AND n.enviada = FALSE AND n.canal = 'Email'
        """;

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, Integer.parseInt(idNotificacion));
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                System.out.println("❌ Notificación ya enviada o no válida: " + idNotificacion);
                return;
            }

            String email = rs.getString("correo_electronico");
            String titulo = rs.getString("titulo");
            String mensaje = rs.getString("mensaje");

            // Mostrar
            Logger.logInfo("📬 Para: " + email);
            Logger.logInfo("✉️  Asunto: " + titulo);
                    Logger.logInfo("📝 Mensaje:\n" + mensaje);


            // Enviar correo
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom("ferremascontra78@gmail.com");
            mail.setTo(email);
            mail.setSubject(titulo);
            mail.setText(mensaje);
            mailSender.send(mail);

            // Marcar como enviada
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE notificacion SET enviada = TRUE WHERE id_notificacion = ?"
            )) {
                update.setInt(1, Integer.parseInt(idNotificacion));
                update.executeUpdate();
            }

            Logger.logInfo("✅ Correo enviado a " + email);

        } catch (Exception e) {
            Logger.logInfo("❌ Error al procesar notificación: " + e.getMessage());
        }
    }
}
