package com.SkyWay.modules.notificacion.domain.service;

import com.SkyWay.config.dataBase.PgNotifyConfig;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NotificacionListenerService {

    @Autowired
    private PgNotifyConfig config;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private NotificacionListenerService self;

    private final ExecutorService listenerExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running = true;

    @PostConstruct
    public void startListener() {
        Logger.logInfo("🚀 [INIT] Iniciando listener PG_NOTIFY para notificaciones de vuelos SkyWay...");
        listenerExecutor.submit(this::listenToChannel);
    }

    @PreDestroy
    public void stopListener() {
        this.running = false;
        listenerExecutor.shutdownNow();
        Logger.logInfo("🛑 Listener PG_NOTIFY detenido.");
    }

    private void listenToChannel() {
        while (running) {
            try (Connection conn = DriverManager.getConnection(
                    config.getJdbcUrl(), config.getUsername(), config.getPassword())) {

                PGConnection pgconn = conn.unwrap(PGConnection.class);

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("LISTEN nuevo_correo");
                }

                Logger.logInfo("📡 Escuchando canal 'nuevo_correo' (LISTEN activo)...");

                while (running && !conn.isClosed()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                    }

                    PGNotification[] notifications = pgconn.getNotifications(3000);

                    if (notifications != null && notifications.length > 0) {
                        for (PGNotification notification : notifications) {
                            String idNotificacion = notification.getParameter();
                            Logger.logInfo("🔔 Notificación recibida desde PG_NOTIFY -> ID = " + idNotificacion);
                            if (self != null) {
                                self.procesarNotificacion(idNotificacion);
                            } else {
                                procesarNotificacion(idNotificacion);
                            }
                        }
                    }

                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                if (running) {
                    Logger.logInfo("⚠️ Conexión con PG_NOTIFY interrumpida. Reintentando en 5s... (" + e.getMessage() + ")");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ignored) {}
                }
            }
        }
    }

    @Async
    public void procesarNotificacion(String idNotificacion) {
        String query = """
            SELECT n.id_notificacion, n.titulo, n.mensaje, u.correo_electronico
            FROM notificacion n
            JOIN usuario u ON u.rut = n.rut_destinatario
            WHERE n.id_notificacion = ? AND (n.enviada IS FALSE OR n.enviada IS NULL) AND (n.canal = 'Email' OR n.canal IS NULL)
        """;

        try (Connection conn = DriverManager.getConnection(
                config.getJdbcUrl(), config.getUsername(), config.getPassword());
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, Integer.parseInt(idNotificacion));
            ResultSet rs = ps.executeQuery();

            if (!rs.next()) {
                Logger.logWarn("❌ Notificación ya enviada o no válida: " + idNotificacion);
                return;
            }

            String email = rs.getString("correo_electronico");
            String titulo = rs.getString("titulo");
            String mensaje = rs.getString("mensaje");

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(new InternetAddress("ferremascontra78@gmail.com", "SkyWay Airlines"));
            helper.setTo(email);
            helper.setSubject(titulo);

            // Plantilla HTML unificada con EmailReservaService (Estilo Oscuro SkyWay)
            String htmlTemplate = """
            <div style="font-family: 'Segoe UI', Helvetica, Arial, sans-serif; background:#0b132b; padding:30px;">
                <div style="max-width:620px; margin:auto; background:#0f172a; border-radius:12px; border:1px solid #1e293b; overflow:hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5);">

                    <!-- ENCABEZADO CORPORATIVO -->
                    <div style="background:#0f172a; padding:25px 20px; text-align:center; border-bottom:1px solid #1e293b;">
                        <h1 style="color:#ffffff; margin:0; font-size:24px; font-weight:700; letter-spacing:1px;">SKYWAY AIRLINES</h1>
                        <p style="color:#60a5fa; margin-top:5px; font-size:12px; font-weight:600; text-transform:uppercase;">Centro de Control Operativo - Estado de Vuelo</p>
                    </div>

                    <!-- CUERPO PRINCIPAL -->
                    <div style="padding:25px; color:#f8fafc;">
                        <h2 style="color:#60a5fa; margin-top:0; font-size:20px; text-align:center;">%s</h2>

                        <div style="color:#cbd5e1; font-size:14px; line-height:1.6; margin-top:15px;">
                            %s
                        </div>

                        <!-- RECOMENDACIÓN IMPORTANTE -->
                        <div style="background:#1e293b; border-radius:8px; padding:15px; margin:20px 0; border-left:4px solid #3b82f6;">
                            <span style="color:#f8fafc; font-size:13px; font-weight:700; display:block; margin-bottom:4px;">📌 Información Importante:</span>
                            <p style="color:#94a3b8; font-size:13px; margin:0; line-height:1.5;">
                                Le recomendamos presentarse en el aeropuerto con la debida anticipación y consultar el estado actualizado de su boarding pass en nuestro portal.
                            </p>
                        </div>

                        <!-- BOTÓN DE ACCIÓN -->
                        <div style="text-align:center; margin:25px 0 10px;">
                            <a href="https://skyway.cl/home/checkin.xhtml" style="background:#16a34a; padding:12px 30px; border-radius:6px; color:#ffffff; text-decoration:none; font-size:14px; font-weight:700; display:inline-block; box-shadow:0 4px 12px rgba(22, 163, 74, 0.3);">
                                Ver Estado de Vuelo
                            </a>
                        </div>
                    </div>

                    <!-- PIE DE PÁGINA -->
                    <div style="background:#0b132b; padding:15px; text-align:center; border-top:1px solid #1e293b;">
                        <p style="color:#64748b; font-size:11px; margin:0;">SkyWay Airlines SpA &bull; Este es un correo automático de notificación, por favor no responda a este mensaje.</p>
                    </div>

                </div>
            </div>
            """;

            String htmlBody = String.format(htmlTemplate, titulo, mensaje);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);

            // Marcar notificación como enviada en la base de datos
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE notificacion SET enviada = TRUE WHERE id_notificacion = ?"
            )) {
                update.setInt(1, Integer.parseInt(idNotificacion));
                update.executeUpdate();
            }

            Logger.logInfo("✅ Correo enviado exitosamente a " + email);

        } catch (Exception e) {
            Logger.logError("❌ Error al procesar notificación ID " + idNotificacion + ": " + e.getMessage());
        }
    }
}