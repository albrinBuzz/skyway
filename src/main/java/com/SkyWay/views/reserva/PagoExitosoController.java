package com.SkyWay.views.reserva;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.EmailReservaService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

// Librerías de OpenPDF / iText (Paquetes Estándar Nativos)
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

@Named("pagoExitosoController")
@ViewScoped
public class PagoExitosoController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ReservaService reservaService;
    @Autowired
    private EmailReservaService emailReservaService;

    private Reserva reserva;
    private String pnr;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null || facesContext.getExternalContext() == null) return;

        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
        String idReservaParam = params.get("idReserva");

        if (idReservaParam != null && !idReservaParam.isBlank()) {
            try {
                Integer idReserva = Integer.parseInt(idReservaParam.trim());
                this.reserva = reservaService.guardarOGenerarPnr(idReserva);

                if (this.reserva != null) {
                    this.pnr = this.reserva.getCodigoReserva();
                    emailReservaService.enviarComprobanteReservaAsync(this.reserva);

                    Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
                    sessionMap.remove("itinerarios");
                    sessionMap.remove("tarifasItinerios");
                    sessionMap.remove("asientosSeleccionados");
                    sessionMap.remove("pasajeros");
                    sessionMap.remove("equipajePorPasajero");
                    sessionMap.remove("totalEquipaje");
                    sessionMap.remove("SOLICITUD_RESERVA_PENDIENTE");
                    sessionMap.remove("ultimaBusquedaAsientosQuery");
                }
            } catch (Exception e) {
                Logger.logError("Error al cargar comprobante de pago exitoso: " + e.getMessage());
            }
        }
    }

    /**
     * Genera un E-Ticket ultra profesional (Fancy Layout) pensado para usuarios invitados.
     */
    public StreamedContent descargarComprobantePdf() {
        if (this.reserva == null) {
            Logger.logWarn("Intento de descargar PDF con objeto Reserva nulo.");
            return null;
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 28, 28, 28, 28);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            // === PALETA DE COLORES Y TIPOGRAFÍAS CORPORATIVAS ===
            Color navyPrimary = new Color(15, 23, 42);      // #0F172A
            Color blueAccent = new Color(59, 130, 246);     // #3B82F6
            Color steelBlue = new Color(96, 165, 250);      // #60A5FA
            Color bgHeader = new Color(241, 245, 249);      // #F1F5F9
            Color textMain = new Color(30, 41, 59);        // #1E293B
            Color textMuted = new Color(100, 116, 139);     // #64748B
            Color statusGreen = new Color(34, 197, 94);     // #22C55E

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

            // 1. BANNER / ENCABEZADO SUPERIOR NAVY (FANCY HEADER)
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

            // 2. CAJA PRINCIPAL DE RESERVA (PNR + BARCODE 128 NATIVO)
            PdfPTable pnrCard = new PdfPTable(3);
            pnrCard.setWidthPercentage(100);
            pnrCard.setWidths(new float[]{50f, 25f, 25f});

            // Columna PNR
            PdfPCell cellPnr = new PdfPCell();
            cellPnr.setPadding(10);
            cellPnr.setBorderColor(new Color(226, 232, 240));
            cellPnr.setBackgroundColor(new Color(248, 250, 252));

            cellPnr.addElement(new Paragraph("CÓDIGO DE RESERVA / PNR", boldFont));
            cellPnr.addElement(new Paragraph(this.reserva.getCodigoReserva(), pnrValFont));
            cellPnr.addElement(new Paragraph("Conserve este código PNR para realizar su Check-In Online y gestionar su vuelo.", normalFont));
            pnrCard.addCell(cellPnr);

            // Columna Estado
            PdfPCell cellState = new PdfPCell();
            cellState.setPadding(10);
            cellState.setBorderColor(new Color(226, 232, 240));
            cellState.setBackgroundColor(new Color(248, 250, 252));
            cellState.addElement(new Paragraph("ESTADO DE VUELO", boldFont));

            Paragraph pStatus = new Paragraph("CONFIRMADO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, statusGreen));
            pStatus.setSpacingBefore(4);
            cellState.addElement(pStatus);

            Paragraph pPago = new Paragraph("Pago Aprobado", normalFont);
            cellState.addElement(pPago);
            pnrCard.addCell(cellState);

            // Columna Barcode 128 Nativo (100% nativo de OpenPDF)
            PdfPCell cellBarcode = new PdfPCell();
            cellBarcode.setPadding(6);
            cellBarcode.setBorderColor(new Color(226, 232, 240));
            cellBarcode.setBackgroundColor(new Color(248, 250, 252));
            cellBarcode.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellBarcode.setVerticalAlignment(Element.ALIGN_MIDDLE);

            try {
                PdfContentByte cb = writer.getDirectContent();
                Barcode128 code128 = new Barcode128();
                code128.setCode(this.reserva.getCodigoReserva());
                code128.setCodeType(Barcode128.CODE128);
                Image barcodeImage = code128.createImageWithBarcode(cb, null, null);
                barcodeImage.setAlignment(Element.ALIGN_CENTER);
                cellBarcode.addElement(barcodeImage);
            } catch (Exception e) {
                cellBarcode.addElement(new Paragraph("PNR: " + this.reserva.getCodigoReserva(), boldFont));
            }
            pnrCard.addCell(cellBarcode);

            pnrCard.setSpacingAfter(12);
            document.add(pnrCard);

            // 3. INFORMACIÓN DEL PASAJERO
            document.add(createSectionHeader("1. PASAJERO Y TITULAR DE LA RESERVA", sectionTitleFont));

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

            // 4. ITINERARIO Y DETALLE DE TRAMOS DE VUELO
            document.add(createSectionHeader("2. ITINERARIO DE VUELO Y TRAMOS", sectionTitleFont));

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

                            String fechaSalidaStr = (fechaSalida != null) ? dateFormat.format(fechaSalida) : "S/F";
                            String fechaLlegadaStr = (fechaLlegada != null) ? dateFormat.format(fechaLlegada) : "S/F";

                            String origenIata = (it.getAeropuertoOrigen() != null) ? it.getAeropuertoOrigen().getCodigoIata() : "Origen";
                            String destinoIata = (it.getAeropuertoDestino() != null) ? it.getAeropuertoDestino().getCodigoIata() : "Destino";

                            String aerolineaStr = (v != null && v.getAerolinea() != null) ? v.getAerolinea().getNombre() : "SkyWay";
                            if (v != null && v.getAvion() != null && v.getAvion().getModeloAvion() != null) {
                                aerolineaStr += "\n" + v.getAvion().getModeloAvion().getNombre();
                            }

                            String asientoStr = obtenerAsientoReserva(v != null ? v.getIdVuelo() : null);

                            addTableCell(flightTable, numVuelo, boldFont);
                            addTableCell(flightTable, origenIata + "\n" + fechaSalidaStr, normalFont);
                            addTableCell(flightTable, destinoIata + "\n" + fechaLlegadaStr, normalFont);
                            addTableCell(flightTable, aerolineaStr, normalFont);
                            addTableCell(flightTable, asientoStr, boldFont);
                            addTableCell(flightTable, "Economy", normalFont);
                        }
                    } else if (it != null) {
                        String origenIata = (it.getAeropuertoOrigen() != null) ? it.getAeropuertoOrigen().getCodigoIata() : "SCL";
                        String destinoIata = (it.getAeropuertoDestino() != null) ? it.getAeropuertoDestino().getCodigoIata() : "LAX";
                        String fechaSalidaStr = (it.getHoraSalida() != null) ? dateFormat.format(it.getHoraSalida()) : "S/F";
                        String fechaLlegadaStr = (it.getHoraLlegada() != null) ? dateFormat.format(it.getHoraLlegada()) : "S/F";

                        addTableCell(flightTable, "SK-" + it.getIdItinerario(), boldFont);
                        addTableCell(flightTable, origenIata + "\n" + fechaSalidaStr, normalFont);
                        addTableCell(flightTable, destinoIata + "\n" + fechaLlegadaStr, normalFont);
                        addTableCell(flightTable, "SkyWay Fleet", normalFont);
                        addTableCell(flightTable, obtenerAsientoReserva(null), boldFont);
                        addTableCell(flightTable, "Economy", normalFont);
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

            // 5. FRANQUICIA DE EQUIPAJE PERMITIDA
            document.add(createSectionHeader("3. FRANQUICIA DE EQUIPAJE REGISTRADA Y PERMITIDA", sectionTitleFont));

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

            // 6. DETALLE DE PAGO Y TRANSACCIÓN
            document.add(createSectionHeader("4. DESGLOSE DE PAGO Y TRANSACCIÓN", sectionTitleFont));

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

            // 7. INSTRUCCIONES Y NORMATIVA DE VIAJE
            PdfPTable infoBox = new PdfPTable(1);
            infoBox.setWidthPercentage(100);

            PdfPCell infoCell = new PdfPCell();
            infoCell.setBackgroundColor(new Color(254, 243, 199));
            infoCell.setBorderColor(new Color(245, 158, 11));
            infoCell.setPadding(8);

            Paragraph pInfoTitle = new Paragraph("INSTRUCCIONES CLAVE PARA EL PASAJERO (GUEST CHECK-IN)", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(180, 83, 9)));
            infoCell.addElement(pInfoTitle);

            Paragraph pInfoBody = new Paragraph(
                    "1. CHECK-IN ONLINE: Disponible 48h antes del vuelo en www.skyway.cl/checkin.xhtml con PNR: " + this.reserva.getCodigoReserva() + " y RUT: " + rut + ".\n" +
                            "2. PRESENTACIÓN EN AEROPUERTO: 2 horas antes para vuelos nacionales y 3 horas antes para vuelos internacionales.\n" +
                            "3. CIERRE DE EMBARQUE: La puerta de embarque cierra estrictamente 15 minutos antes de la hora de despegue.\n" +
                            "4. DOCUMENTACIÓN OBLIGATORIA: Deberá presentar su Cédula de Identidad o Pasaporte vigente al momento de abordar.", normalFont);
            infoCell.addElement(pInfoBody);

            infoBox.addCell(infoCell);
            infoBox.setSpacingAfter(10);
            document.add(infoBox);

            // Pie de página oficial
            Paragraph footer = new Paragraph("SkyWay Airlines SpA - Comprobante Oficial de Reserva Generado Automáticamente.", FontFactory.getFont(FontFactory.HELVETICA, 7, textMuted));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            byte[] pdfBytes = out.toByteArray();

            return DefaultStreamedContent.builder()
                    .name("E-Ticket_SkyWay_" + this.reserva.getCodigoReserva() + ".pdf")
                    .contentType("application/pdf")
                    .stream(() -> new ByteArrayInputStream(pdfBytes))
                    .build();

        } catch (Exception e) {
            Logger.logError("Error al generar PDF detallado: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private Paragraph createSectionHeader(String titleText, Font font) {
        Paragraph p = new Paragraph(titleText, font);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        return p;
    }

    private String obtenerAsientoReserva(Integer idVuelo) {
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

    public Reserva getReserva() { return reserva; }
    public String getPnr() { return pnr; }
}