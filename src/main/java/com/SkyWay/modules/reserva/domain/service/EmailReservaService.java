package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

// Librerías OpenPDF / iText
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class EmailReservaService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envía el correo de confirmación de reserva con el E-Ticket PDF adjunto de forma asíncrona.
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
            Logger.logInfo("📧 [EMAIL] Generando PDF y preparando envío a: " + emailDestino);

            // 1. Generar el PDF en memoria
            byte[] pdfBytes = generarPdfBytes(reserva);

            // 2. Preparar el correo MIME
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ferremascontra78@gmail.com");
            helper.setTo(emailDestino);
            helper.setSubject("¡Reserva Confirmada! Tu E-Ticket SkyWay [PNR: " + reserva.getCodigoReserva() + "]");

            String nombrePasajero = (usr != null) ? usr.getNombre() + " " + usr.getApellido() : "Pasajero";
            String rut = (pax != null) ? pax.getRut() : "N/A";
            String pnr = (reserva.getCodigoReserva() != null) ? reserva.getCodigoReserva() : "S/PNR";

            // 3. Plantilla HTML del Correo (Sintaxis limpia con String.format)
            String htmlTemplate = """
            <div style="font-family: 'Segoe UI', Helvetica, Arial, sans-serif; background:#0b132b; padding:30px;">
                <div style="max-width:600px; margin:auto; background:#0f172a; border-radius:12px; border:1px solid #1e293b; overflow:hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.5);">
                    
                    <!-- HEADER -->
                    <div style="background:#0f172a; padding:25px 20px; text-align:center; border-bottom:1px solid #1e293b;">
                        <h1 style="color:#ffffff; margin:0; font-size:24px; font-weight:700; letter-spacing:1px;">SKYWAY AIRLINES</h1>
                        <p style="color:#60a5fa; margin-top:5px; font-size:12px; font-weight:600;">COMPROBANTE OFICIAL DE RESERVA Y E-TICKET</p>
                    </div>

                    <!-- CUERPO -->
                    <div style="padding:25px; color:#f8fafc;">
                        <h2 style="color:#34d399; margin-top:0; font-size:20px; text-align:center;">¡Tu pago ha sido procesado con éxito!</h2>
                        <p style="color:#cbd5e1; font-size:14px; line-height:1.5;">Hola <strong>%s</strong>,</p>
                        <p style="color:#cbd5e1; font-size:14px; line-height:1.5;">Tu reserva está confirmada. Adjunto a este correo encontrarás tu <strong>E-Ticket oficial en formato PDF</strong>.</p>

                        <!-- BOX PNR -->
                        <div style="background:#0b132b; border:1px solid #3b82f6; border-radius:8px; padding:15px; text-align:center; margin:20px 0;">
                            <span style="display:block; color:#94a3b8; font-size:11px; font-weight:700; letter-spacing:1px;">CÓDIGO DE RESERVA (PNR)</span>
                            <span style="font-size:28px; font-weight:800; color:#3b82f6; letter-spacing:2px;">%s</span>
                        </div>

                        <!-- ALERTA INVITADOS -->
                        <div style="background:rgba(217, 119, 6, 0.1); border:1px solid rgba(245, 158, 11, 0.4); border-radius:8px; padding:12px; color:#cbd5e1; font-size:13px; margin-bottom:20px;">
                            <strong style="color:#f59e0b;">¿Viajas como invitado?</strong><br>
                            Usa tu código PNR (<strong>%s</strong>) y tu RUT (<strong>%s</strong>) para realizar tu Check-In Online y obtener tus pases de abordar.
                        </div>

                        <!-- CTA BUTTON -->
                        <div style="text-align:center; margin:25px 0 10px;">
                            <a href="https://skyway.cl/home/checkin.xhtml" style="background:#16a34a; padding:12px 28px; border-radius:6px; color:#ffffff; text-decoration:none; font-size:14px; font-weight:700; display:inline-block;">Ir a Check-In Online</a>
                        </div>
                    </div>

                    <!-- FOOTER -->
                    <div style="background:#0b132b; padding:15px; text-align:center; border-top:1px solid #1e293b;">
                        <p style="color:#64748b; font-size:11px; margin:0;">SkyWay Airlines SpA &bull; Este es un correo automático, por favor no respondas a este mensaje.</p>
                    </div>

                </div>
            </div>
            """;

            String htmlBody = String.format(htmlTemplate, nombrePasajero, pnr, pnr, rut);
            helper.setText(htmlBody, true);

            // 4. Adjuntar el archivo PDF
            if (pdfBytes != null && pdfBytes.length > 0) {
                String nombreArchivo = "E-Ticket_SkyWay_" + pnr + ".pdf";
                helper.addAttachment(nombreArchivo, new ByteArrayResource(pdfBytes), "application/pdf");
            }

            // 5. Enviar correo
            mailSender.send(mimeMessage);
            Logger.logInfo("✅ [EMAIL] Correo enviado exitosamente con PDF adjunto a " + emailDestino);

        } catch (Exception e) {
            Logger.logError("❌ Error al enviar el correo de la reserva: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Genera dinámicamente los bytes del PDF del E-Ticket.
     */
    private byte[] generarPdfBytes(Reserva reserva) throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 28, 28, 28, 28);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            Color navyPrimary = new Color(15, 23, 42);
            Color blueAccent = new Color(59, 130, 246);
            Color steelBlue = new Color(96, 165, 250);
            Color bgHeader = new Color(241, 245, 249);
            Color textMain = new Color(30, 41, 59);
            Color textMuted = new Color(100, 116, 139);
            Color statusGreen = new Color(34, 197, 94);

            Font headerTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
            Font headerSubFont = FontFactory.getFont(FontFactory.HELVETICA, 8, steelBlue);
            Font pnrValFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, blueAccent);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, navyPrimary);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, textMain);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, textMuted);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            DecimalFormat currencyFormat = new DecimalFormat("#,###");

            Pasajero pax = reserva.getPasajero();
            Usuario usr = (pax != null) ? pax.getUsuario() : null;
            String rut = (pax != null) ? pax.getRut() : "N/A";

            // Banner Header
            PdfPTable bannerTable = new PdfPTable(2);
            bannerTable.setWidthPercentage(100);
            bannerTable.setWidths(new float[]{65f, 35f});

            PdfPCell leftBanner = new PdfPCell();
            leftBanner.setBackgroundColor(navyPrimary);
            leftBanner.setPadding(12);
            leftBanner.setBorder(PdfPCell.NO_BORDER);
            leftBanner.addElement(new Paragraph("SKYWAY AIRLINES", headerTitleFont));
            leftBanner.addElement(new Paragraph("PASSENGER E-TICKET & RECEIPT / CONFIRMACIÓN DE VIAJE", headerSubFont));
            bannerTable.addCell(leftBanner);

            PdfPCell rightBanner = new PdfPCell();
            rightBanner.setBackgroundColor(navyPrimary);
            rightBanner.setPadding(12);
            rightBanner.setBorder(PdfPCell.NO_BORDER);
            rightBanner.setHorizontalAlignment(Element.ALIGN_RIGHT);

            Paragraph eticketNum = new Paragraph("e-Ticket: 789-" + String.format("%07d", reserva.getIdReserva()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE));
            eticketNum.setAlignment(Element.ALIGN_RIGHT);
            rightBanner.addElement(eticketNum);

            Paragraph fechaEmision = new Paragraph("Emisión: " + (reserva.getFechaReserva() != null ? dateFormat.format(reserva.getFechaReserva()) : "N/A"), FontFactory.getFont(FontFactory.HELVETICA, 8, steelBlue));
            fechaEmision.setAlignment(Element.ALIGN_RIGHT);
            rightBanner.addElement(fechaEmision);

            bannerTable.addCell(rightBanner);
            bannerTable.setSpacingAfter(10);
            document.add(bannerTable);

            // Card PNR + Barcode
            PdfPTable pnrCard = new PdfPTable(3);
            pnrCard.setWidthPercentage(100);
            pnrCard.setWidths(new float[]{50f, 25f, 25f});

            PdfPCell cellPnr = new PdfPCell();
            cellPnr.setPadding(10);
            cellPnr.setBorderColor(new Color(226, 232, 240));
            cellPnr.setBackgroundColor(new Color(248, 250, 252));
            cellPnr.addElement(new Paragraph("CÓDIGO DE RESERVA / PNR", boldFont));
            cellPnr.addElement(new Paragraph(reserva.getCodigoReserva(), pnrValFont));
            cellPnr.addElement(new Paragraph("Conserve este código PNR para realizar su Check-In Online y gestionar su vuelo.", normalFont));
            pnrCard.addCell(cellPnr);

            PdfPCell cellState = new PdfPCell();
            cellState.setPadding(10);
            cellState.setBorderColor(new Color(226, 232, 240));
            cellState.setBackgroundColor(new Color(248, 250, 252));
            cellState.addElement(new Paragraph("ESTADO DE VUELO", boldFont));
            Paragraph pStatus = new Paragraph("CONFIRMADO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, statusGreen));
            pStatus.setSpacingBefore(4);
            cellState.addElement(pStatus);
            cellState.addElement(new Paragraph("Pago Aprobado", normalFont));
            pnrCard.addCell(cellState);

            PdfPCell cellBarcode = new PdfPCell();
            cellBarcode.setPadding(6);
            cellBarcode.setBorderColor(new Color(226, 232, 240));
            cellBarcode.setBackgroundColor(new Color(248, 250, 252));
            cellBarcode.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBarcode.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                PdfContentByte cb = writer.getDirectContent();
                Barcode128 code128 = new Barcode128();
                code128.setCode(reserva.getCodigoReserva());
                code128.setCodeType(Barcode128.CODE128);
                Image barcodeImage = code128.createImageWithBarcode(cb, null, null);
                barcodeImage.setAlignment(Element.ALIGN_CENTER);
                cellBarcode.addElement(barcodeImage);
            } catch (Exception e) {
                cellBarcode.addElement(new Paragraph("PNR: " + reserva.getCodigoReserva(), boldFont));
            }
            pnrCard.addCell(cellBarcode);

            pnrCard.setSpacingAfter(12);
            document.add(pnrCard);

            // 1. Pasajero
            document.add(new Paragraph("1. PASAJERO Y TITULAR DE LA RESERVA", sectionTitleFont));
            PdfPTable paxTable = new PdfPTable(4);
            paxTable.setWidthPercentage(100);
            paxTable.setWidths(new float[]{32f, 22f, 28f, 18f});

            addHeaderCell(paxTable, "Nombre Completo", boldFont, bgHeader);
            addHeaderCell(paxTable, "RUT / Pasaporte", boldFont, bgHeader);
            addHeaderCell(paxTable, "Correo Electrónico", boldFont, bgHeader);
            addHeaderCell(paxTable, "Teléfono", boldFont, bgHeader);

            String nombre = (usr != null) ? usr.getNombre() + " " + usr.getApellido() : "Pasajero Invitado";
            String correo = (usr != null && usr.getCorreoElectronico() != null) ? usr.getCorreoElectronico() : "No especificado";
            String telefono = (usr != null) ? usr.getTelefono() : "N/A";

            addTableCell(paxTable, nombre, boldFont);
            addTableCell(paxTable, rut, normalFont);
            addTableCell(paxTable, correo, normalFont);
            addTableCell(paxTable, telefono, normalFont);
            paxTable.setSpacingAfter(12);
            document.add(paxTable);

            // 2. Itinerario
            document.add(new Paragraph("2. ITINERARIO DE VUELO Y TRAMOS", sectionTitleFont));
            PdfPTable flightTable = new PdfPTable(6);
            flightTable.setWidthPercentage(100);
            flightTable.setWidths(new float[]{14f, 22f, 22f, 18f, 12f, 12f});

            addHeaderCell(flightTable, "N° Vuelo", boldFont, bgHeader);
            addHeaderCell(flightTable, "Origen / Salida", boldFont, bgHeader);
            addHeaderCell(flightTable, "Destino / Llegada", boldFont, bgHeader);
            addHeaderCell(flightTable, "Aerolínea / Avión", boldFont, bgHeader);
            addHeaderCell(flightTable, "Asiento", boldFont, bgHeader);
            addHeaderCell(flightTable, "Clase", boldFont, bgHeader);

            if (reserva.getReservaItinerarios() != null && !reserva.getReservaItinerarios().isEmpty()) {
                for (ReservaItinerario ri : reserva.getReservaItinerarios()) {
                    Itinerario it = ri.getItinerario();
                    if (it != null && it.getItinerarioVuelos() != null && !it.getItinerarioVuelos().isEmpty()) {
                        for (ItinerarioVuelo iv : it.getItinerarioVuelos()) {
                            Vuelo v = iv.getVuelo();
                            String numVuelo = (v != null && v.getNumeroVuelo() != null) ? v.getNumeroVuelo() : "SK-" + it.getIdItinerario();
                            Date fechaSalida = (v != null && v.getFechaHoraSalida() != null) ? v.getFechaHoraSalida() : (it.getHoraSalida() != null ? new Date(it.getHoraSalida().getTime()) : null);
                            Date fechaLlegada = (v != null && v.getFechaHoraLlegada() != null) ? v.getFechaHoraLlegada() : (it.getHoraLlegada() != null ? new Date(it.getHoraLlegada().getTime()) : null);

                            String origenIata = (it.getAeropuertoOrigen() != null) ? it.getAeropuertoOrigen().getCodigoIata() : "Origen";
                            String destinoIata = (it.getAeropuertoDestino() != null) ? it.getAeropuertoDestino().getCodigoIata() : "Destino";

                            String aerolineaStr = (v != null && v.getAerolinea() != null) ? v.getAerolinea().getNombre() : "SkyWay";
                            if (v != null && v.getAvion() != null && v.getAvion().getModeloAvion() != null) {
                                aerolineaStr += "\n" + v.getAvion().getModeloAvion().getNombre();
                            }

                            addTableCell(flightTable, numVuelo, boldFont);
                            addTableCell(flightTable, origenIata + "\n" + (fechaSalida != null ? dateFormat.format(fechaSalida) : "S/F"), normalFont);
                            addTableCell(flightTable, destinoIata + "\n" + (fechaLlegada != null ? dateFormat.format(fechaLlegada) : "S/F"), normalFont);
                            addTableCell(flightTable, aerolineaStr, normalFont);
                            addTableCell(flightTable, obtenerAsientoReserva(reserva, v != null ? v.getIdVuelo() : null), boldFont);
                            addTableCell(flightTable, "Economy", normalFont);
                        }
                    }
                }
            } else {
                addTableCell(flightTable, "SK-101", boldFont);
                addTableCell(flightTable, "SCL (Origen)", normalFont);
                addTableCell(flightTable, "LAX (Destino)", normalFont);
                addTableCell(flightTable, "Airbus A320", normalFont);
                addTableCell(flightTable, "Asignado", boldFont);
                addTableCell(flightTable, "Economy", normalFont);
            }
            flightTable.setSpacingAfter(12);
            document.add(flightTable);

            // 3. Equipaje
            document.add(new Paragraph("3. FRANQUICIA DE EQUIPAJE REGISTRADA Y PERMITIDA", sectionTitleFont));
            PdfPTable equipTable = new PdfPTable(3);
            equipTable.setWidthPercentage(100);
            equipTable.setWidths(new float[]{30f, 35f, 35f});

            addHeaderCell(equipTable, "Tipo de Bulto", boldFont, bgHeader);
            addHeaderCell(equipTable, "Dimensiones Máximas", boldFont, bgHeader);
            addHeaderCell(equipTable, "Peso / Condición", boldFont, bgHeader);

            if (reserva.getEquipajes() != null && !reserva.getEquipajes().isEmpty()) {
                for (Equipaje eq : reserva.getEquipajes()) {
                    addTableCell(equipTable, eq.getTipo(), boldFont);
                    addTableCell(equipTable, eq.getDimensiones(), normalFont);
                    addTableCell(equipTable, eq.getPeso() + " kg (Registrado)", normalFont);
                }
            }
            equipTable.setSpacingAfter(12);
            document.add(equipTable);

            // 4. Pago
            document.add(new Paragraph("4. DESGLOSE DE PAGO Y TRANSACCIÓN", sectionTitleFont));
            PdfPTable payTable = new PdfPTable(4);
            payTable.setWidthPercentage(100);
            payTable.setWidths(new float[]{25f, 25f, 25f, 25f});

            addHeaderCell(payTable, "Monto Total Pagado", boldFont, bgHeader);
            addHeaderCell(payTable, "Medio de Pago", boldFont, bgHeader);
            addHeaderCell(payTable, "Moneda", boldFont, bgHeader);
            addHeaderCell(payTable, "Estado Transacción", boldFont, bgHeader);

            String totalStr = (reserva.getTotal() != null) ? "$" + currencyFormat.format(reserva.getTotal()) : "$0";
            String medioPagoStr = "Webpay Plus / Transbank";
            if (reserva.getPagos() != null && !reserva.getPagos().isEmpty()) {
                Pago p = reserva.getPagos().get(0);
                if (p.getMetodoPago() != null && p.getMetodoPago().getDescripcion() != null) {
                    medioPagoStr = p.getMetodoPago().getDescripcion();
                }
            }

            addTableCell(payTable, totalStr + " CLP", boldFont);
            addTableCell(payTable, medioPagoStr, normalFont);
            addTableCell(payTable, "CLP (Peso Chileno)", normalFont);
            addTableCell(payTable, "APROBADO", boldFont);

            payTable.setSpacingAfter(12);
            document.add(payTable);

            // Pie
            Paragraph footer = new Paragraph("SkyWay Airlines SpA - Comprobante Oficial de Reserva Generado Automáticamente.", FontFactory.getFont(FontFactory.HELVETICA, 7, textMuted));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return out.toByteArray();
        }
    }

    private String obtenerAsientoReserva(Reserva reserva, Integer idVuelo) {
        if (reserva != null && reserva.getReservaAsientos() != null && !reserva.getReservaAsientos().isEmpty()) {
            for (ReservaAsiento ra : reserva.getReservaAsientos()) {
                if (idVuelo == null || (ra.getVuelo() != null && idVuelo.equals(ra.getVuelo().getIdVuelo()))) {
                    if (ra.getAsiento() != null && ra.getAsiento().getNumeroAsiento() != null) {
                        return ra.getAsiento().getNumeroAsiento();
                    }
                }
            }
        }
        return "1A";
    }

    private void addHeaderCell(PdfPTable table, String text, Font font, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        cell.setBorderColor(new Color(203, 213, 225));
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(Color.WHITE);
        cell.setPadding(5);
        cell.setBorderColor(new Color(226, 232, 240));
        table.addCell(cell);
    }
}