package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailReservaService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ETicketPdfGeneratorService eTicketPdfGeneratorService;

    /**
     * Envía el correo de confirmación de reserva adjuntando los 2 PDFs especializados (Resumen y Boarding Passes).
     */
    @Async
    public void enviarComprobanteReservaAsync(Reserva reserva) {
        if (reserva == null) {
            Logger.logWarn("No se puede enviar correo: la entidad Reserva es nula.");
            return;
        }

        Pasajero pax = reserva.getPasajero();
        Usuario usr = (pax != null) ? pax.getUsuario() : null;
        String emailDestino = (usr != null && usr.getCorreoElectronico() != null) ? usr.getCorreoElectronico() : null;

        if (emailDestino == null || emailDestino.isBlank()) {
            Logger.logWarn("No se encontró correo electrónico para la reserva ID: " + reserva.getIdReserva());
            return;
        }

        try {
            Logger.logInfo("📧 [EMAIL] Generando PDFs de Resumen y Boarding Passes para envío a: " + emailDestino);

            // 1. Generar los 2 PDFs independientes
            byte[] pdfResumenBytes = eTicketPdfGeneratorService.generarPdfResumenReserva(reserva);
            byte[] pdfBoardingPassesBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(reserva);

            // 2. Preparar mensaje MIME
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ferremascontra78@gmail.com");
            helper.setTo(emailDestino);
            helper.setSubject("✈️ ¡Reserva Confirmada! Tu E-Ticket y Pases de Abordar SkyWay [PNR: " + reserva.getCodigoReserva() + "]");

            // Todo dato controlado por el comprador se escapa antes de ir al HTML (evita phishing con remitente oficial).
            String nombrePasajero = HtmlUtils.htmlEscape((usr != null) ? usr.getNombre() + " " + usr.getApellido() : "Estimado/a Pasajero/a");
            String rut = HtmlUtils.htmlEscape((pax != null && pax.getRut() != null) ? pax.getRut() : "N/A");
            String pnr = HtmlUtils.htmlEscape((reserva.getCodigoReserva() != null) ? reserva.getCodigoReserva() : "S/PNR");

            // 3. Plantilla HTML profesional orientada a invitados y múltiples pasajeros
            String htmlTemplate = """
            <div style="font-family: 'Segoe UI', Helvetica, Arial, sans-serif; background:#0b132b; padding:30px;">
                <div style="max-width:620px; margin:auto; background:#0f172a; border-radius:12px; border:1px solid #1e293b; overflow:hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5);">
                    
                    <!-- ENCABEZADO CORPORATIVO -->
                    <div style="background:#0f172a; padding:25px 20px; text-align:center; border-bottom:1px solid #1e293b;">
                        <h1 style="color:#ffffff; margin:0; font-size:24px; font-weight:700; letter-spacing:1px;">SKYWAY AIRLINES</h1>
                        <p style="color:#60a5fa; margin-top:5px; font-size:12px; font-weight:600; text-transform:uppercase;">Confirmación Oficial de Vuelo y Documentos de Viaje</p>
                    </div>

                    <!-- CUERPO PRINCIPAL -->
                    <div style="padding:25px; color:#f8fafc;">
                        <h2 style="color:#34d399; margin-top:0; font-size:20px; text-align:center;">¡Tu reserva ha sido confirmada con éxito!</h2>
                        <p style="color:#cbd5e1; font-size:14px; line-height:1.6;">Hola <strong>%s</strong>,</p>
                        <p style="color:#cbd5e1; font-size:14px; line-height:1.6;">Agradecemos tu preferencia. Adjunto a este correo encontrarás <strong>2 archivos PDF</strong> con la información detallada de tu vuelo y tus pases de abordar.</p>

                        <!-- BOX DESTACADO PNR -->
                        <div style="background:#0b132b; border:1px solid #3b82f6; border-radius:10px; padding:18px; text-align:center; margin:20px 0;">
                            <span style="display:block; color:#94a3b8; font-size:11px; font-weight:700; letter-spacing:1px;">CÓDIGO DE RESERVA (PNR)</span>
                            <span style="font-size:32px; font-weight:800; color:#3b82f6; letter-spacing:2px; margin-top:4px; display:block;">%s</span>
                        </div>

                        <!-- ARCHIVOS ADJUNTOS EN EL CORREO -->
                        <div style="background:#1e293b; border-radius:8px; padding:15px; margin-bottom:20px;">
                            <span style="color:#f8fafc; font-size:13px; font-weight:700; display:block; margin-bottom:8px;">📎 Documentos Adjuntos en este Correo:</span>
                            <ul style="color:#94a3b8; font-size:13px; margin:0; padding-left:20px; line-height:1.6;">
                                <li><strong style="color:#60a5fa;">Resumen_Reserva_SkyWay_%s.pdf:</strong> Detalle de pago, itinerario completo, equipaje y políticas de viaje.</li>
                                <li><strong style="color:#34d399;">Tarjetas_Embarque_SkyWay_%s.pdf:</strong> Pases de abordar individuales por pasajero con sus asientos asignados.</li>
                            </ul>
                        </div>

                        <!-- SECCIÓN PARA INVITADOS (GUEST USER) -->
                        <div style="background:rgba(217, 119, 6, 0.1); border:1px solid rgba(245, 158, 11, 0.4); border-radius:8px; padding:15px; color:#cbd5e1; font-size:13px; margin-bottom:20px;">
                            <strong style="color:#f59e0b; font-size:14px; display:block; margin-bottom:4px;">🔑 Acceso para Pasajeros Invitados (Sin Cuenta)</strong>
                            Si realizaste la compra sin iniciar sesión o deseas gestionar la reserva para tus acompañantes, puedes ingresar en cualquier momento usando:
                            <div style="margin-top:8px; padding:8px; background:rgba(0,0,0,0.3); border-radius:6px; font-size:12px;">
                                • <strong>PNR:</strong> <span style="color:#60a5fa;">%s</span><br>
                                • <strong>RUT Titular:</strong> <span style="color:#60a5fa;">%s</span>
                            </div>
                        </div>

                        <!-- BOTÓN DE ACCIÓN (CHECK-IN) -->
                        <div style="text-align:center; margin:25px 0 10px;">
                            <a href="https://skyway.cl/home/checkin.xhtml" style="background:#16a34a; padding:12px 30px; border-radius:6px; color:#ffffff; text-decoration:none; font-size:14px; font-weight:700; display:inline-block; box-shadow:0 4px 12px rgba(22, 163, 74, 0.3);">Ir al Portal de Check-In Online</a>
                        </div>
                    </div>

                    <!-- PIE DE PÁGINA -->
                    <div style="background:#0b132b; padding:15px; text-align:center; border-top:1px solid #1e293b;">
                        <p style="color:#64748b; font-size:11px; margin:0;">SkyWay Airlines SpA &bull; Este es un correo automático de confirmación, por favor no respondas a este mensaje.</p>
                    </div>

                </div>
            </div>
            """;

            String htmlBody = String.format(htmlTemplate, nombrePasajero, pnr, pnr, pnr, pnr, rut);
            helper.setText(htmlBody, true);

            // 4. Adjuntar PDF 1: Resumen de Reserva e Itinerario
            if (pdfResumenBytes != null && pdfResumenBytes.length > 0) {
                String nombrePdfResumen = "Resumen_Reserva_SkyWay_" + pnr + ".pdf";
                helper.addAttachment(nombrePdfResumen, new ByteArrayResource(pdfResumenBytes), "application/pdf");
            }

            // 5. Adjuntar PDF 2: Tarjetas de Embarque (Boarding Passes)
            if (pdfBoardingPassesBytes != null && pdfBoardingPassesBytes.length > 0) {
                String nombrePdfBoarding = "Tarjetas_Embarque_SkyWay_" + pnr + ".pdf";
                helper.addAttachment(nombrePdfBoarding, new ByteArrayResource(pdfBoardingPassesBytes), "application/pdf");
            }

            // 6. Realizar el envío
            mailSender.send(mimeMessage);
            Logger.logInfo("✅ [EMAIL] Correo enviado exitosamente con 2 PDFs adjuntos a " + emailDestino);

        } catch (Exception e) {
            Logger.logError("❌ Error al enviar el correo de la reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }
}