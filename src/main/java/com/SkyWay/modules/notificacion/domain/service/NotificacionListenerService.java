package com.SkyWay.modules.notificacion.domain.service;

import com.SkyWay.config.dataBase.PgNotifyConfig;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NotificacionListenerService {

    @Autowired
    private PgNotifyConfig config;

    @Autowired
    private JavaMailSender mailSender;

    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void startListener() {
        listenerExecutor.submit(this::listenToChannel);
    }

    private void listenToChannel() {
        while (running) {
            // Manejo automático de reconexión si cae la BD
            try (Connection conn = DriverManager.getConnection(
                    config.getJdbcUrl(), config.getUsername(), config.getPassword())) {

                PGConnection pgconn = conn.unwrap(PGConnection.class);

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN nuevo_correo");
                }

                Logger.logInfo("📡 Escuchando canal 'nuevo_correo' (PG LISTEN activo)...");

                while (running && !conn.isClosed()) {
                    // getNotifications(5000) bloquea el hilo hasta 5 segundos esperando un evento.
                    // NO requiere ejecutar "SELECT 1" ni Thread.sleep()
                    PGNotification[] notifications = pgconn.getNotifications(5000);

                    if (notifications != null) {
                        for (PGNotification notification : notifications) {
                            String idNotificacion = notification.getParameter();
                            Logger.logInfo(" Notificación recibida ID = " + idNotificacion);

                            // Delegar el procesamiento pesado a un hilo asíncrono
                            procesarNotificacion(idNotificacion);
                        }
                    }
                }
            } catch (SQLException e) {
                Logger.logInfo("⚠️ Conexión con PG_NOTIFY perdida. Reintentando en 5 segundos...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    @Async
    protected void procesarNotificacion(String idNotificacion) {
        String query = """
            SELECT n.id_notificacion, n.titulo, n.mensaje, u.correo_electronico
            FROM notificacion n
            JOIN usuario u ON u.rut = n.rut_destinatario
            WHERE n.id_notificacion = ? AND n.enviada = FALSE AND n.canal = 'Email'
        """;

        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(), config.getUsername(), config.getPassword());
             PreparedStatement ps = conn.prepareStatement(query)) {
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
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ferremascontra78@gmail.com");
            helper.setTo(email);
            helper.setSubject(titulo);

            String html = """
        <div style="font-family: 'Segoe UI', Helvetica, Arial, sans-serif; background:#e8edf3; padding:30px;">
            <div style="max-width:600px; margin:auto; background:white; border-radius:10px; 
                        box-shadow:0 6px 20px rgba(0,0,0,0.15); overflow:hidden;">

                <!-- HEADER CORPORATIVO -->
                <div style="background:#0a1d3b; padding:30px 20px; text-align:center;">
                    <h1 style="color:white; margin:0; font-size:26px; font-weight:600;">
                        SKYWAY AIRLINES
                    </h1>
                    <p style="color:#b7c4d3; margin-top:8px; font-size:13px;">
                        Experiencia de vuelo superior
                    </p>
                </div>

                <!-- CUERPO DEL MENSAJE -->
                <div style="padding:30px 25px;">

                    <h2 style="color:#0a1d3b; margin-top:0; text-align:center; font-size:22px;">
                        """ + titulo + """
                    </h2>

                    <p style="font-size:15px; color:#333; line-height:1.6;">
                        """ + mensaje + """
                    </p>

                    <!-- Tarjeta elegante de información -->
                    <div style="margin-top:25px; background:#f7f9fc; padding:20px; 
                                border-radius:8px; border-left:6px solid #0a1d3b;">
                        <p style="margin:0; color:#0a1d3b; font-size:15px; font-weight:600;">
                            Información importante:
                        </p>
                        <p style="margin-top:8px; color:#333; font-size:14px;">
                            Te recomendamos revisar el estado actualizado de tu vuelo y presentarte con
                            anticipación en el aeropuerto para evitar inconvenientes.
                        </p>
                    </div>

                    <!-- BOTÓN CTA -->
                    <div style="text-align:center; margin:35px 0 20px;">
                        <a href="https://skyway.com/vuelos"
                           style="background:#11346b; padding:14px 28px; border-radius:6px; 
                                  color:white; text-decoration:none; font-size:14px; font-weight:600;
                                  display:inline-block;">
                            Ver estado del vuelo
                        </a>
                    </div>

                </div>

                <!-- FOOTER -->
                <div style="background:#f1f3f7; padding:15px; text-align:center;">
                    <p style="color:#6c7886; font-size:12px; margin:0;">
                        Este correo fue enviado automáticamente por SkyWay Airlines.<br>
                        No respondas a este mensaje.
                    </p>
                </div>

            </div>
        </div>
        """;



            helper.setText(html, true); // <-- true indica que es HTML

            mailSender.send(mimeMessage);

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
