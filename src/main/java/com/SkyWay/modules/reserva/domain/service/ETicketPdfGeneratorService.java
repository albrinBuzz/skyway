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

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
public class ETicketPdfGeneratorService {

    /**
     * PDF 1: Resumen de Reserva, Confirmación de Pago y Guía de Viaje para Invitados
     */
    public byte[] generarPdfResumenReserva(Reserva reserva) {
        if (reserva == null) return new byte[0];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 25, 25, 25, 25);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Colores Premium SkyWay
            Color navy = new Color(15, 23, 42);         // #0F172A
            Color blue = new Color(37, 99, 235);        // #2563EB
            Color lightBg = new Color(248, 250, 252);   // #F8FAFC
            Color textDark = new Color(30, 41, 59);      // #1E293B
            Color textGray = new Color(100, 116, 139);   // #64748B
            Color successGreen = new Color(22, 163, 74); // #16A34A
            Color accentBorder = new Color(226, 232, 240);

            // Fuentes
            Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
            Font fSubTitle = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(147, 197, 253));
            Font fPnr = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, blue);
            Font fSection = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, navy);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, textDark);
            Font fNormal = FontFactory.getFont(FontFactory.HELVETICA, 8, textGray);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            DecimalFormat currencyFormat = new DecimalFormat("#,###");

            Pasajero titular = reserva.getPasajero();
            String rutTitular = (titular != null) ? titular.getRut() : "N/A";

            // 1. HEADER BANNER
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{65f, 35f});

            PdfPCell hLeft = new PdfPCell();
            hLeft.setBackgroundColor(navy);
            hLeft.setPadding(12);
            hLeft.setBorder(PdfPCell.NO_BORDER);
            hLeft.addElement(new Paragraph("SKYWAY AIRLINES", fTitle));
            hLeft.addElement(new Paragraph("COMPROBANTE DE RESERVA & RESUMEN DE VIAJE", fSubTitle));
            header.addCell(hLeft);

            PdfPCell hRight = new PdfPCell();
            hRight.setBackgroundColor(navy);
            hRight.setPadding(12);
            hRight.setBorder(PdfPCell.NO_BORDER);

            Paragraph pEticket = new Paragraph("e-Ticket: 789-" + String.format("%07d", reserva.getIdReserva()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE));
            pEticket.setAlignment(Element.ALIGN_RIGHT);
            hRight.addElement(pEticket);

            Paragraph pFecha = new Paragraph("Fecha: " + (reserva.getFechaReserva() != null ? dateFormat.format(reserva.getFechaReserva()) : "N/A"), fSubTitle);
            pFecha.setAlignment(Element.ALIGN_RIGHT);
            hRight.addElement(pFecha);
            header.addCell(hRight);
            header.setSpacingAfter(10);
            document.add(header);

            // 2. BOX PNR & BARCODE
            PdfPTable pnrBox = new PdfPTable(3);
            pnrBox.setWidthPercentage(100);
            pnrBox.setWidths(new float[]{45f, 25f, 30f});

            PdfPCell cPnr = createCardCell(lightBg, accentBorder);
            cPnr.addElement(new Paragraph("CÓDIGO DE RESERVA / PNR", fBold));
            cPnr.addElement(new Paragraph(reserva.getCodigoReserva(), fPnr));
            cPnr.addElement(new Paragraph("Guarde este código para gestionar su viaje.", fNormal));
            pnrBox.addCell(cPnr);

            PdfPCell cState = createCardCell(lightBg, accentBorder);
            cState.addElement(new Paragraph("ESTADO DE RESERVA", fBold));
            Paragraph pSt = new Paragraph("CONFIRMADO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, successGreen));
            pSt.setSpacingBefore(4);
            cState.addElement(pSt);
            cState.addElement(new Paragraph("Pago Verificado", fNormal));
            pnrBox.addCell(cState);

            PdfPCell cBar = createCardCell(lightBg, accentBorder);
            cBar.setHorizontalAlignment(Element.ALIGN_CENTER);
            cBar.setVerticalAlignment(Element.ALIGN_MIDDLE);
            try {
                PdfContentByte cb = writer.getDirectContent();
                Barcode128 code128 = new Barcode128();
                code128.setCode(reserva.getCodigoReserva());
                code128.setCodeType(Barcode128.CODE128);
                Image img = code128.createImageWithBarcode(cb, null, null);
                img.setAlignment(Element.ALIGN_CENTER);
                cBar.addElement(img);
            } catch (Exception e) {
                cBar.addElement(new Paragraph(reserva.getCodigoReserva(), fBold));
            }
            pnrBox.addCell(cBar);
            pnrBox.setSpacingAfter(10);
            document.add(pnrBox);

            // 3. SECCIÓN ESPECIAL USUARIO INVITADO (GUEST PORTAL)
            PdfPTable guestBox = new PdfPTable(1);
            guestBox.setWidthPercentage(100);
            PdfPCell gCell = new PdfPCell();
            gCell.setBackgroundColor(new Color(238, 242, 255)); // Indigo suave
            gCell.setBorderColor(blue);
            gCell.setPadding(10);

            Paragraph pGTitle = new Paragraph("🔑 ACCESO PARA PASAJEROS SIN CUENTA (INVITADOS)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, blue));
            gCell.addElement(pGTitle);
            Paragraph pGBody = new Paragraph("Usted puede realizar Check-In, agregar equipaje o seleccionar asientos ingresando a www.skyway.cl/checkin.xhtml con su PNR (" + reserva.getCodigoReserva() + ") y su RUT (" + rutTitular + "). No requiere crear una contraseña.", fNormal);
            pGBody.setSpacingBefore(3);
            gCell.addElement(pGBody);

            guestBox.addCell(gCell);
            guestBox.setSpacingAfter(12);
            document.add(guestBox);

            // 4. LISTA DE PASAJEROS
            List<PasajeroInfo> pasajeros = obtenerListaPasajeros(reserva);
            document.add(createHeader("1. PASAJEROS EN ESTA RESERVA (" + pasajeros.size() + ")", fSection));

            PdfPTable tPax = new PdfPTable(4);
            tPax.setWidthPercentage(100);
            tPax.setWidths(new float[]{35f, 20f, 30f, 15f});
            addCellHeader(tPax, "Nombre Pasajero", fBold, lightBg);
            addCellHeader(tPax, "RUT / Pasaporte", fBold, lightBg);
            addCellHeader(tPax, "Correo Electrónico", fBold, lightBg);
            addCellHeader(tPax, "Tipo", fBold, lightBg);

            for (PasajeroInfo p : pasajeros) {
                addCell(tPax, p.nombreCompleto, fBold);
                addCell(tPax, p.rut, fNormal);
                addCell(tPax, p.correo, fNormal);
                addCell(tPax, p.esTitular ? "Titular" : "Acompañante", fNormal);
            }
            tPax.setSpacingAfter(12);
            document.add(tPax);

            // 5. ITINERARIO DE VUELOS (OPTIMIZADO SIN REPETICIÓN)
            document.add(createHeader("2. ITINERARIO DE VUELOS Y ASIENTOS", fSection));
            PdfPTable tFlight = new PdfPTable(6);
            tFlight.setWidthPercentage(100);
            tFlight.setWidths(new float[]{15f, 20f, 20f, 20f, 15f, 10f}); // Anchos corregidos

            addCellHeader(tFlight, "N° Vuelo", fBold, lightBg);
            addCellHeader(tFlight, "Salida", fBold, lightBg);
            addCellHeader(tFlight, "Llegada", fBold, lightBg);
            addCellHeader(tFlight, "Aerolínea/Avión", fBold, lightBg);
            addCellHeader(tFlight, "Asiento(s)", fBold, lightBg);
            addCellHeader(tFlight, "Clase", fBold, lightBg);

            if (reserva.getReservaItinerarios() != null) {
                for (ReservaItinerario ri : reserva.getReservaItinerarios()) {
                    Itinerario it = ri.getItinerario();
                    if (it != null && it.getItinerarioVuelos() != null) {
                        for (ItinerarioVuelo iv : it.getItinerarioVuelos()) {
                            Vuelo v = iv.getVuelo();
                            String numVuelo = (v != null && v.getNumeroVuelo() != null) ? v.getNumeroVuelo() : "SK-" + it.getIdItinerario();
                            Date dSalida = (v != null && v.getFechaHoraSalida() != null) ? v.getFechaHoraSalida() : (it.getHoraSalida() != null ? new Date(it.getHoraSalida().getTime()) : null);
                            Date dLlegada = (v != null && v.getFechaHoraLlegada() != null) ? v.getFechaHoraLlegada() : (it.getHoraLlegada() != null ? new Date(it.getHoraLlegada().getTime()) : null);

                            String origenStr = ((it.getAeropuertoOrigen() != null) ? it.getAeropuertoOrigen().getCodigoIata() : "Origen") + "\n" + (dSalida != null ? dateFormat.format(dSalida) : "S/F");
                            String destinoStr = ((it.getAeropuertoDestino() != null) ? it.getAeropuertoDestino().getCodigoIata() : "Destino") + "\n" + (dLlegada != null ? dateFormat.format(dLlegada) : "S/F");

                            // Unificar asientos asignados en un string legible
                            StringBuilder asientosSb = new StringBuilder();
                            for (PasajeroInfo p : pasajeros) {
                                String ast = obtenerAsientoEspecifico(reserva, v != null ? v.getIdVuelo() : null, p.rut);
                                if (asientosSb.length() > 0) asientosSb.append(", ");
                                asientosSb.append(ast);
                            }

                            addCell(tFlight, numVuelo, fBold);
                            addCell(tFlight, origenStr, fNormal);
                            addCell(tFlight, destinoStr, fNormal);
                            addCell(tFlight, (v != null && v.getAerolinea() != null) ? v.getAerolinea().getNombre() : "SkyWay Airlines", fNormal);
                            addCell(tFlight, asientosSb.toString(), fBold);
                            addCell(tFlight, "Economy", fNormal); // No se corta el texto
                        }
                    }
                }
            }
            tFlight.setSpacingAfter(12);
            document.add(tFlight);

            // 6. EQUIPAJE
            document.add(createHeader("3. FRANQUICIA DE EQUIPAJE REGISTRADA", fSection));
            PdfPTable tEq = new PdfPTable(3);
            tEq.setWidthPercentage(100);
            tEq.setWidths(new float[]{30f, 35f, 35f});
            addCellHeader(tEq, "Bulto", fBold, lightBg);
            addCellHeader(tEq, "Dimensiones", fBold, lightBg);
            addCellHeader(tEq, "Condición / Peso", fBold, lightBg);

            if (reserva.getEquipajes() != null && !reserva.getEquipajes().isEmpty()) {
                for (Equipaje eq : reserva.getEquipajes()) {
                    addCell(tEq, eq.getTipo(), fBold);
                    addCell(tEq, eq.getDimensiones() != null ? eq.getDimensiones() : "Estándar", fNormal);
                    addCell(tEq, eq.getPeso() + " kg (Permitido)", fNormal);
                }
            }


            tEq.setSpacingAfter(12);
            document.add(tEq);

            // 7. DESGLOSE PAGO
            document.add(createHeader("4. DETALLE DEL PAGO", fSection));
            PdfPTable tPay = new PdfPTable(4);
            tPay.setWidthPercentage(100);
            tPay.setWidths(new float[]{25f, 25f, 25f, 25f});
            addCellHeader(tPay, "Total Pagado", fBold, lightBg);
            addCellHeader(tPay, "Medio de Pago", fBold, lightBg);
            addCellHeader(tPay, "Moneda", fBold, lightBg);
            addCellHeader(tPay, "Estado", fBold, lightBg);

            String totalStr = (reserva.getTotal() != null) ? "$" + currencyFormat.format(reserva.getTotal()) : "$0";
            String medioPago = "PayPal / Webpay";
            if (reserva.getPagos() != null && !reserva.getPagos().isEmpty()) {
                Pago p = reserva.getPagos().get(0);
                if (p.getMetodoPago() != null && p.getMetodoPago().getDescripcion() != null) {
                    medioPago = p.getMetodoPago().getDescripcion();
                }
            }

            addCell(tPay, totalStr + " CLP", fBold);
            addCell(tPay, medioPago, fNormal);
            addCell(tPay, "CLP", fNormal);
            addCell(tPay, "APROBADO", fBold);
            tPay.setSpacingAfter(15);
            document.add(tPay);

            // FOOTER
            Paragraph foot = new Paragraph("SkyWay Airlines SpA • Documento de Reserva e Itinerario Oficial", fNormal);
            foot.setAlignment(Element.ALIGN_CENTER);
            document.add(foot);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            Logger.logError("Error al generar PDF de Resumen: " + e.getMessage());
            return new byte[0];
        }
    }

    /**
     * PDF 2: Tarjetas de Embarque Individuales por Pasajero (Boarding Passes Layout)
     */
    public byte[] generarPdfBoardingPasses(Reserva reserva) {
        if (reserva == null) return new byte[0];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            Color navy = new Color(15, 23, 42);
            Color blue = new Color(37, 99, 235);
            Color bgLight = new Color(241, 245, 249);

            Font fTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.WHITE);
            Font fBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, navy);
            Font fVal = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, blue);
            Font fSub = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            List<PasajeroInfo> pasajeros = obtenerListaPasajeros(reserva);

            for (PasajeroInfo pax : pasajeros) {
                if (reserva.getReservaItinerarios() != null) {
                    for (ReservaItinerario ri : reserva.getReservaItinerarios()) {
                        Itinerario it = ri.getItinerario();
                        if (it != null && it.getItinerarioVuelos() != null) {
                            for (ItinerarioVuelo iv : it.getItinerarioVuelos()) {
                                Vuelo v = iv.getVuelo();
                                String numVuelo = (v != null && v.getNumeroVuelo() != null) ? v.getNumeroVuelo() : "SK-" + it.getIdItinerario();
                                String asiento = obtenerAsientoEspecifico(reserva, v != null ? v.getIdVuelo() : null, pax.rut);
                                String origen = (it.getAeropuertoOrigen() != null) ? it.getAeropuertoOrigen().getCodigoIata() : "SCL";
                                String destino = (it.getAeropuertoDestino() != null) ? it.getAeropuertoDestino().getCodigoIata() : "LAX";

                                // Tarjeta de Embarque Box
                                PdfPTable card = new PdfPTable(2);
                                card.setWidthPercentage(100);
                                card.setWidths(new float[]{70f, 30f});

                                // LADO IZQUIERDO: DETALLES DEL VUELO
                                PdfPCell left = new PdfPCell();
                                left.setBackgroundColor(bgLight);
                                left.setPadding(10);
                                left.setBorderColor(navy);

                                // Header de la tarjeta
                                PdfPTable headerCard = new PdfPTable(1);
                                PdfPCell hc = new PdfPCell(new Phrase("BOARDING PASS / TARJETA DE EMBARQUE - SKYWAY", fTitle));
                                hc.setBackgroundColor(navy);
                                hc.setPadding(6);
                                headerCard.addCell(hc);
                                left.addElement(headerCard);

                                left.addElement(new Paragraph("PASAJERO: " + pax.nombreCompleto.toUpperCase(), fBold));
                                left.addElement(new Paragraph("RUT: " + pax.rut + " | PNR: " + reserva.getCodigoReserva(), fSub));

                                Paragraph pRuta = new Paragraph(origen + " ➔ " + destino, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, navy));
                                pRuta.setSpacingBefore(5);
                                left.addElement(pRuta);

                                left.addElement(new Paragraph("VUELO: " + numVuelo + " | SALIDA: " + (v != null && v.getFechaHoraSalida() != null ? dateFormat.format(v.getFechaHoraSalida()) : "S/F"), fSub));
                                card.addCell(left);

                                // LADO DERECHO: ASIENTO Y BARCODE
                                PdfPCell right = new PdfPCell();
                                right.setBackgroundColor(Color.WHITE);
                                right.setPadding(10);
                                right.setBorderColor(navy);
                                right.setHorizontalAlignment(Element.ALIGN_CENTER);

                                right.addElement(new Paragraph("ASIENTO", fSub));
                                right.addElement(new Paragraph(asiento, fVal));

                                right.addElement(new Paragraph("PUERTA", fSub));
                                right.addElement(new Paragraph("TBD", fBold));

                                try {
                                    PdfContentByte cb = writer.getDirectContent();
                                    Barcode128 code128 = new Barcode128();
                                    code128.setCode(reserva.getCodigoReserva() + "-" + asiento);
                                    code128.setCodeType(Barcode128.CODE128);
                                    Image img = code128.createImageWithBarcode(cb, null, null);
                                    img.setAlignment(Element.ALIGN_CENTER);
                                    right.addElement(img);
                                } catch (Exception e) {}

                                card.addCell(right);
                                card.setSpacingAfter(15);
                                document.add(card);
                            }
                        }
                    }
                }
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            Logger.logError("Error al generar Boarding Passes: " + e.getMessage());
            return new byte[0];
        }
    }

    // --- MÉTODOS DE SOPORTE ---
    private static class PasajeroInfo {
        String rut;
        String nombreCompleto;
        String correo;
        boolean esTitular;

        public PasajeroInfo(String rut, String nombreCompleto, String correo, boolean esTitular) {
            this.rut = rut;
            this.nombreCompleto = nombreCompleto;
            this.correo = correo;
            this.esTitular = esTitular;
        }
    }

    private List<PasajeroInfo> obtenerListaPasajeros(Reserva reserva) {
        Map<String, PasajeroInfo> map = new LinkedHashMap<>();
        Pasajero t = reserva.getPasajero();
        if (t != null) {
            Usuario u = t.getUsuario();
            String n = (u != null) ? u.getNombre() + " " + u.getApellido() : "Titular";
            String e = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "N/A";
            map.put(t.getRut(), new PasajeroInfo(t.getRut(), n, e, true));
        }
        if (reserva.getReservaAsientos() != null) {
            for (ReservaAsiento ra : reserva.getReservaAsientos()) {
                if (ra.getRut() != null && !map.containsKey(ra.getRut())) {
                    Pasajero p = ra.getPasajero();
                    Usuario u = (p != null) ? p.getUsuario() : null;
                    String n = (u != null) ? u.getNombre() + " " + u.getApellido() : "Pasajero " + ra.getRut();
                    String e = (u != null && u.getCorreoElectronico() != null) ? u.getCorreoElectronico() : "N/A";
                    map.put(ra.getRut(), new PasajeroInfo(ra.getRut(), n, e, false));
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private String obtenerAsientoEspecifico(Reserva reserva, Integer idVuelo, String rut) {
        if (reserva != null && reserva.getReservaAsientos() != null) {
            for (ReservaAsiento ra : reserva.getReservaAsientos()) {
                boolean cV = (idVuelo == null) || (ra.getVuelo() != null && idVuelo.equals(ra.getVuelo().getIdVuelo()));
                boolean cR = (rut != null && rut.equals(ra.getRut()));
                if (cV && cR && ra.getAsiento() != null) {
                    return ra.getAsiento().getNumeroAsiento();
                }
            }
        }
        return "1A";
    }

    private Paragraph createHeader(String t, Font f) {
        Paragraph p = new Paragraph(t, f);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        return p;
    }

    private PdfPCell createCardCell(Color bg, Color border) {
        PdfPCell c = new PdfPCell();
        c.setPadding(8);
        c.setBackgroundColor(bg);
        c.setBorderColor(border);
        return c;
    }

    private void addCellHeader(PdfPTable t, String txt, Font f, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(bg);
        c.setPadding(5);
        c.setBorderColor(new Color(226, 232, 240));
        t.addCell(c);
    }

    private void addCell(PdfPTable t, String txt, Font f) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(Color.WHITE);
        c.setPadding(5);
        c.setBorderColor(new Color(226, 232, 240));
        t.addCell(c);
    }
}