package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.BoardingPassSecurityService;
import com.SkyWay.util.Logger;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
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
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Service
public class ETicketPdfGeneratorService {

    @PersistenceContext
    private EntityManager em;

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

            // Cargar logotipo de SkyWay si está disponible en classpath o disco
            Image logoImg = null;
            try {
                URL logoRes = getClass().getResource("/META-INF/resources/resources/assets/icons/Logotipo_SkyWay.png");
                if (logoRes == null) {
                    logoRes = getClass().getResource("/META-INF/resources/assets/icons/Logotipo_SkyWay.png");
                }
                if (logoRes != null) {
                    logoImg = Image.getInstance(logoRes);
                } else {
                    File f = new File("src/main/resources/META-INF/resources/resources/assets/icons/Logotipo_SkyWay.png");
                    if (f.exists()) {
                        logoImg = Image.getInstance(f.getAbsolutePath());
                    }
                }
            } catch (Exception ignored) {}

            // 1. HEADER BANNER CON LOGOTIPO
            PdfPTable header = new PdfPTable(3);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{22f, 48f, 30f});

            PdfPCell hLogo = new PdfPCell();
            hLogo.setBackgroundColor(navy);
            hLogo.setPadding(10);
            hLogo.setBorder(PdfPCell.NO_BORDER);
            hLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
            if (logoImg != null) {
                try {
                    Image lImg = Image.getInstance(logoImg);
                    lImg.scaleToFit(110f, 36f);
                    hLogo.addElement(lImg);
                } catch (Exception e) {
                    hLogo.addElement(new Paragraph("SKYWAY", fTitle));
                }
            } else {
                hLogo.addElement(new Paragraph("SKYWAY", fTitle));
            }
            header.addCell(hLogo);

            PdfPCell hLeft = new PdfPCell();
            hLeft.setBackgroundColor(navy);
            hLeft.setPadding(10);
            hLeft.setBorder(PdfPCell.NO_BORDER);
            hLeft.setVerticalAlignment(Element.ALIGN_MIDDLE);
            hLeft.addElement(new Paragraph("SKYWAY AIRLINES", fTitle));
            hLeft.addElement(new Paragraph("COMPROBANTE DE RESERVA & RESUMEN DE VIAJE", fSubTitle));
            header.addCell(hLeft);

            PdfPCell hRight = new PdfPCell();
            hRight.setBackgroundColor(navy);
            hRight.setPadding(10);
            hRight.setBorder(PdfPCell.NO_BORDER);
            hRight.setVerticalAlignment(Element.ALIGN_MIDDLE);

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

            // 5. ITINERARIO DE VUELOS (OPTIMIZADO CON CLASES Y RUTAS REALES)
            document.add(createHeader("2. ITINERARIO DE VUELOS Y ASIENTOS", fSection));
            PdfPTable tFlight = new PdfPTable(6);
            tFlight.setWidthPercentage(100);
            tFlight.setWidths(new float[]{15f, 19f, 19f, 17f, 14f, 16f});

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

                            // Origen y destino específicos del tramo de vuelo desde sus segmentos
                            String codigoOrigen = (it.getAeropuertoOrigen() != null) ? it.getAeropuertoOrigen().getCodigoIata() : "Origen";
                            String codigoDestino = (it.getAeropuertoDestino() != null) ? it.getAeropuertoDestino().getCodigoIata() : "Destino";
                            Date dSalida = (v != null && v.getFechaHoraSalida() != null) ? v.getFechaHoraSalida() : (it.getHoraSalida() != null ? new Date(it.getHoraSalida().getTime()) : null);
                            Date dLlegada = (v != null && v.getFechaHoraLlegada() != null) ? v.getFechaHoraLlegada() : (it.getHoraLlegada() != null ? new Date(it.getHoraLlegada().getTime()) : null);

                            if (v != null && v.getSegmentoVuelos() != null && !v.getSegmentoVuelos().isEmpty()) {
                                List<SegmentoVuelo> segs = v.getSegmentoVuelos();
                                SegmentoVuelo pSeg = segs.get(0);
                                SegmentoVuelo uSeg = segs.get(segs.size() - 1);
                                if (pSeg.getAeropuertoOrigen() != null && pSeg.getAeropuertoOrigen().getCodigoIata() != null) {
                                    codigoOrigen = pSeg.getAeropuertoOrigen().getCodigoIata();
                                }
                                if (uSeg.getAeropuertoDestino() != null && uSeg.getAeropuertoDestino().getCodigoIata() != null) {
                                    codigoDestino = uSeg.getAeropuertoDestino().getCodigoIata();
                                }
                                if (pSeg.getHoraSalida() != null) {
                                    dSalida = new Date(pSeg.getHoraSalida().getTime());
                                }
                                if (uSeg.getHoraLlegada() != null) {
                                    dLlegada = new Date(uSeg.getHoraLlegada().getTime());
                                }
                            }

                            String origenStr = codigoOrigen + "\n" + (dSalida != null ? dateFormat.format(dSalida) : "S/F");
                            String destinoStr = codigoDestino + "\n" + (dLlegada != null ? dateFormat.format(dLlegada) : "S/F");

                            // Asientos y Clases reales asignadas en este vuelo
                            List<String> listaAsientosVuelo = new ArrayList<>();
                            Set<String> clasesVuelo = new LinkedHashSet<>();

                            if (reserva.getReservaAsientos() != null) {
                                for (ReservaAsiento ra : reserva.getReservaAsientos()) {
                                    if (ra.getVuelo() != null && v != null && v.getIdVuelo().equals(ra.getVuelo().getIdVuelo())) {
                                        if (ra.getAsiento() != null) {
                                            if (ra.getAsiento().getNumeroAsiento() != null) {
                                                listaAsientosVuelo.add(ra.getAsiento().getNumeroAsiento());
                                            }
                                            if (ra.getAsiento().getClaseAsiento() != null && ra.getAsiento().getClaseAsiento().getDescripcion() != null) {
                                                clasesVuelo.add(ra.getAsiento().getClaseAsiento().getDescripcion());
                                            }
                                        }
                                    }
                                }
                            }

                            if (listaAsientosVuelo.isEmpty()) {
                                for (PasajeroInfo p : pasajeros) {
                                    String ast = obtenerAsientoEspecifico(reserva, v != null ? v.getIdVuelo() : null, p.rut);
                                    if (ast != null && !ast.isBlank()) {
                                        listaAsientosVuelo.add(ast);
                                    }
                                }
                            }

                            if (clasesVuelo.isEmpty()) {
                                String claseDef = (ri.getItinerarioTarifa() != null && ri.getItinerarioTarifa().getTarifa() != null && ri.getItinerarioTarifa().getTarifa().getNombre() != null)
                                        ? ri.getItinerarioTarifa().getTarifa().getNombre()
                                        : "Económica";
                                clasesVuelo.add(claseDef);
                            }

                            String asientosStr = String.join(", ", listaAsientosVuelo);
                            String clasesStr = String.join(", ", clasesVuelo);

                            addCell(tFlight, numVuelo, fBold);
                            addCell(tFlight, origenStr, fNormal);
                            addCell(tFlight, destinoStr, fNormal);
                            addCell(tFlight, (v != null && v.getAerolinea() != null) ? v.getAerolinea().getNombre() : "SkyWay Airlines", fNormal);
                            addCell(tFlight, asientosStr, fBold);
                            addCell(tFlight, clasesStr, fNormal);
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
     * PDF 2: Tarjetas de Embarque Individuales por Pasajero (Diseño Tipo Boarding Pass Oficial SkyWay)
     */
    public byte[] generarPdfBoardingPasses(List<TicketInfo> tickets) {
        if (tickets == null || tickets.isEmpty()) return new byte[0];

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 20, 20, 20, 20);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // Cargar logotipo de SkyWay si está disponible en classpath o disco
            Image logoImg = null;
            try {
                URL logoRes = getClass().getResource("/META-INF/resources/resources/assets/icons/Logotipo_SkyWay.png");
                if (logoRes == null) {
                    logoRes = getClass().getResource("/META-INF/resources/assets/icons/Logotipo_SkyWay.png");
                }
                if (logoRes != null) {
                    logoImg = Image.getInstance(logoRes);
                } else {
                    File f = new File("src/main/resources/META-INF/resources/resources/assets/icons/Logotipo_SkyWay.png");
                    if (f.exists()) {
                        logoImg = Image.getInstance(f.getAbsolutePath());
                    }
                }
            } catch (Exception ignored) {}

            int count = 0;
            for (TicketInfo t : tickets) {
                if (count > 0 && count % 2 == 0) {
                    document.newPage();
                }
                PdfPTable card = crearTarjetaEmbarquePdf(t, writer, logoImg);
                document.add(card);
                count++;
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            Logger.logError("Error al generar Boarding Passes: " + e.getMessage());
            e.printStackTrace();
            return new byte[0];
        }
    }

    public byte[] generarPdfBoardingPasses(Reserva reserva) {
        if (reserva == null) return new byte[0];

        List<TicketInfo> tickets = new ArrayList<>();
        String rutTitular = (reserva.getPasajero() != null) ? reserva.getPasajero().getRut() : null;

        // Intentar obtener tickets con datos operacionales completos via función nativa fn_getticket
        if (em != null && reserva.getIdReserva() != null && rutTitular != null && reserva.getReservaItinerarios() != null) {
            try {
                for (ReservaItinerario ri : reserva.getReservaItinerarios()) {
                    if (ri.getItinerario() != null) {
                        Query query = em.createNativeQuery(
                                "SELECT * FROM fn_getticket(:p_rut, :p_reserva, :p_idItinerario);",
                                TicketInfo.class);
                        query.setParameter("p_rut", rutTitular);
                        query.setParameter("p_reserva", reserva.getIdReserva());
                        query.setParameter("p_idItinerario", ri.getItinerario().getIdItinerario());
                        @SuppressWarnings("unchecked")
                        List<TicketInfo> rList = query.getResultList();
                        if (rList != null && !rList.isEmpty()) {
                            tickets.addAll(rList);
                        }
                    }
                }
            } catch (Exception e) {
                Logger.logError("Error al obtener tickets via fn_getticket en generador PDF: " + e.getMessage());
            }
        }

        // Respaldo resiliente a partir del grafo de entidades JPA
        if (tickets.isEmpty()) {
            List<PasajeroInfo> pasajeros = obtenerListaPasajeros(reserva);
            if (reserva.getReservaItinerarios() != null) {
                for (ReservaItinerario ri : reserva.getReservaItinerarios()) {
                    Itinerario it = ri.getItinerario();
                    if (it != null && it.getItinerarioVuelos() != null) {
                        for (ItinerarioVuelo iv : it.getItinerarioVuelos()) {
                            Vuelo v = iv.getVuelo();
                            String numVuelo = (v != null && v.getNumeroVuelo() != null) ? v.getNumeroVuelo() : "SK-" + it.getIdItinerario();
                            
                            String origen = "SCL";
                            String destino = "LAX";
                            String puerta = "TBD";
                            String terminal = "Terminal Internacional";
                            Timestamp fSalida = (v != null && v.getFechaHoraSalida() != null) ? new Timestamp(v.getFechaHoraSalida().getTime()) : null;
                            Timestamp fLlegada = (v != null && v.getFechaHoraLlegada() != null) ? new Timestamp(v.getFechaHoraLlegada().getTime()) : null;

                            if (v != null && v.getSegmentoVuelos() != null && !v.getSegmentoVuelos().isEmpty()) {
                                List<SegmentoVuelo> segs = new ArrayList<>(v.getSegmentoVuelos());
                                segs.sort(Comparator.comparing(SegmentoVuelo::getOrdenSegmento, Comparator.nullsLast(Comparator.naturalOrder())));
                                SegmentoVuelo primerSeg = segs.get(0);
                                SegmentoVuelo ultimoSeg = segs.get(segs.size() - 1);
                                if (primerSeg.getAeropuertoOrigen() != null) origen = primerSeg.getAeropuertoOrigen().getCodigoIata();
                                if (ultimoSeg.getAeropuertoDestino() != null) destino = ultimoSeg.getAeropuertoDestino().getCodigoIata();
                                if (primerSeg.getHoraSalida() != null) fSalida = primerSeg.getHoraSalida();
                                if (ultimoSeg.getHoraLlegada() != null) fLlegada = ultimoSeg.getHoraLlegada();
                                if (primerSeg.getAsignacionPuertas() != null && !primerSeg.getAsignacionPuertas().isEmpty()) {
                                    var asg = primerSeg.getAsignacionPuertas().iterator().next();
                                    if (asg.getPuertaEmbarque() != null) {
                                        if (asg.getPuertaEmbarque().getCodigoPuerta() != null) puerta = asg.getPuertaEmbarque().getCodigoPuerta();
                                        if (asg.getPuertaEmbarque().getTerminal() != null) terminal = asg.getPuertaEmbarque().getTerminal();
                                    }
                                }
                            } else {
                                if (it.getAeropuertoOrigen() != null) origen = it.getAeropuertoOrigen().getCodigoIata();
                                if (it.getAeropuertoDestino() != null) destino = it.getAeropuertoDestino().getCodigoIata();
                            }

                            for (PasajeroInfo pax : pasajeros) {
                                String asiento = obtenerAsientoEspecifico(reserva, v != null ? v.getIdVuelo() : null, pax.rut);
                                String clase = "Económica";
                                if (reserva.getReservaAsientos() != null) {
                                    for (ReservaAsiento ra : reserva.getReservaAsientos()) {
                                        if (ra.getVuelo() != null && v != null && v.getIdVuelo().equals(ra.getVuelo().getIdVuelo())
                                                && ra.getPasajero() != null && pax.rut != null && pax.rut.equals(ra.getPasajero().getRut())) {
                                            if (ra.getAsiento() != null && ra.getAsiento().getClaseAsiento() != null
                                                    && ra.getAsiento().getClaseAsiento().getDescripcion() != null) {
                                                clase = ra.getAsiento().getClaseAsiento().getDescripcion();
                                            }
                                        }
                                    }
                                }

                                TicketInfo t = new TicketInfo();
                                t.setCodigoPnr(reserva.getCodigoReserva());
                                t.setNumeroVuelo(numVuelo);
                                t.setOrigenTramo(origen);
                                t.setDestinoTramo(destino);
                                t.setHoraSalida(fSalida);
                                t.setHoraLlegada(fLlegada);
                                t.setCodigoPuerta(puerta);
                                t.setTerminal(terminal);
                                t.setNumeroAsiento(asiento != null ? asiento : "---");
                                t.setClaseAsiento(clase);
                                t.setNombre(pax.nombreCompleto);
                                t.setCodigoQr(BoardingPassSecurityService.generarCodigoSeguro(reserva.getCodigoReserva(), numVuelo, asiento != null ? asiento : "---", pax.rut));
                                tickets.add(t);
                            }
                        }
                    }
                }
            }
        }

        return generarPdfBoardingPasses(tickets);
    }

    private PdfPTable crearTarjetaEmbarquePdf(TicketInfo t, PdfWriter writer, Image baseLogoImg) {
        PdfPTable card = new PdfPTable(2);
        card.setWidthPercentage(100);
        try {
            card.setWidths(new float[]{71f, 29f});
        } catch (Exception ignored) {}
        card.setSpacingAfter(18f);
        card.setKeepTogether(true);

        Color skyBlue = new Color(2, 132, 199);      // #0284C7
        Color blueDark = new Color(3, 105, 161);     // #0369A1
        Color blueMedium = new Color(14, 165, 233);  // #0EA5E9
        Color cardBg = new Color(248, 250, 252);     // #F8FAFC
        Color fieldBg = new Color(224, 242, 254);    // #E0F2FE
        Color inkDark = new Color(17, 24, 39);       // #111827
        Color grayText = new Color(100, 116, 139);   // #64748B
        Color borderCard = new Color(203, 213, 225); // #CBD5E1

        SimpleDateFormat dfDate = new SimpleDateFormat("dd.MM.yy");
        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm");
        SimpleDateFormat dfFull = new SimpleDateFormat("dd.MM.yy HH:mm");

        String strFecha = (t.getHoraSalida() != null) ? dfDate.format(t.getHoraSalida()) : "---";
        String strSalida = (t.getHoraSalida() != null) ? dfTime.format(t.getHoraSalida()) : "--:--";
        String strLlegada = (t.getHoraLlegada() != null) ? dfFull.format(t.getHoraLlegada()) : "---";
        String strLimite = (t.getHoraLimiteEmbarque() != null) ? dfTime.format(t.getHoraLimiteEmbarque()) : "--:--";

        // ==========================================
        // 1. LADO IZQUIERDO: CUERPO PRINCIPAL
        // ==========================================
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBackgroundColor(cardBg);
        leftCell.setBorderColor(borderCard);
        leftCell.setBorderWidth(1f);
        leftCell.setPadding(0);

        PdfPTable mainTable = new PdfPTable(1);
        mainTable.setWidthPercentage(100);

        // A. Header Principal
        PdfPTable mainHeader = new PdfPTable(2);
        mainHeader.setWidthPercentage(100);
        try { mainHeader.setWidths(new float[]{65f, 35f}); } catch (Exception ignored) {}

        PdfPCell mhBrand = new PdfPCell();
        mhBrand.setBackgroundColor(skyBlue);
        mhBrand.setBorder(PdfPCell.NO_BORDER);
        mhBrand.setPadding(6f);
        if (baseLogoImg != null) {
            try {
                Image bl = Image.getInstance(baseLogoImg);
                bl.scaleToFit(85f, 22f);
                mhBrand.addElement(bl);
            } catch (Exception e) {
                mhBrand.addElement(new Paragraph("✈  SkyWay Airlines", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            }
        } else {
            mhBrand.addElement(new Paragraph("✈  SkyWay Airlines", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
        }
        mainHeader.addCell(mhBrand);

        PdfPCell mhPnr = new PdfPCell();
        mhPnr.setBackgroundColor(skyBlue);
        mhPnr.setBorder(PdfPCell.NO_BORDER);
        mhPnr.setPadding(7f);
        Paragraph pnrP = new Paragraph("PNR: " + (t.getCodigoPnr() != null ? t.getCodigoPnr() : "---"), FontFactory.getFont(FontFactory.COURIER_BOLD, 9.5f, Color.WHITE));
        pnrP.setAlignment(Element.ALIGN_RIGHT);
        mhPnr.addElement(pnrP);
        mainHeader.addCell(mhPnr);

        PdfPCell mhWrapper = new PdfPCell(mainHeader);
        mhWrapper.setBorder(PdfPCell.NO_BORDER);
        mhWrapper.setPadding(0);
        mainTable.addCell(mhWrapper);

        // B. Body Principal (3 Columnas)
        PdfPTable mainBody = new PdfPTable(3);
        mainBody.setWidthPercentage(100);
        try { mainBody.setWidths(new float[]{36f, 38f, 26f}); } catch (Exception ignored) {}

        // Columna 1: Pasajero, Origen, Destino, Clase, Terminal
        PdfPCell c1 = new PdfPCell();
        c1.setBackgroundColor(cardBg);
        c1.setBorder(PdfPCell.NO_BORDER);
        c1.setPadding(7f);

        addLabeledBox(c1, "PASAJERO", t.getNombre() != null ? t.getNombre() : "---", fieldBg, inkDark, 8f);
        addLabeledBox(c1, "ORIGEN", t.getOrigenTramo() != null ? t.getOrigenTramo() : "---", fieldBg, inkDark, 8.5f);
        addLabeledBox(c1, "DESTINO", t.getDestinoTramo() != null ? t.getDestinoTramo() : "---", fieldBg, inkDark, 8.5f);
        addLabeledBox(c1, "CLASE", t.getClaseAsiento() != null ? t.getClaseAsiento() : "Económica", fieldBg, inkDark, 8f);
        addLabeledBox(c1, "TERMINAL", t.getTerminal() != null ? t.getTerminal() : "Terminal Internacional", fieldBg, inkDark, 7.5f);
        mainBody.addCell(c1);

        // Columna 2: Fecha, Salida, Llegada, QR
        PdfPCell c2 = new PdfPCell();
        c2.setBackgroundColor(cardBg);
        c2.setBorder(PdfPCell.NO_BORDER);
        c2.setPadding(7f);

        Paragraph pFecha = new Paragraph();
        pFecha.add(new Phrase("FECHA  ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, inkDark)));
        pFecha.add(new Phrase(strFecha, FontFactory.getFont(FontFactory.COURIER_BOLD, 12f, blueDark)));
        c2.addElement(pFecha);

        Paragraph pSalida = new Paragraph();
        pSalida.add(new Phrase("SALIDA ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, inkDark)));
        pSalida.add(new Phrase(strSalida, FontFactory.getFont(FontFactory.COURIER_BOLD, 15f, blueDark)));
        c2.addElement(pSalida);

        Paragraph pLlegada = new Paragraph("Llegada: " + strLlegada, FontFactory.getFont(FontFactory.COURIER, 7f, grayText));
        pLlegada.setSpacingAfter(6f);
        c2.addElement(pLlegada);

        // Generar Código 2D PDF417 (Estándar oficial IATA BCBP Res. 792 para Tarjetas de Embarque sin APIs externas)
        Image qrImg = null;
        String qrData = (t.getCodigoQr() != null && !t.getCodigoQr().isBlank())
                ? t.getCodigoQr()
                : (t.getCodigoPnr() + "-" + t.getNumeroVuelo() + "-" + t.getNumeroAsiento());
        try {
            com.lowagie.text.pdf.BarcodePDF417 pdf417 = new com.lowagie.text.pdf.BarcodePDF417();
            pdf417.setText(qrData.getBytes(StandardCharsets.UTF_8));
            qrImg = pdf417.getImage();
            if (qrImg != null) {
                qrImg.scaleToFit(85f, 50f);
            }
        } catch (Exception e) {
            try {
                Barcode128 code128 = new Barcode128();
                code128.setCode(qrData);
                code128.setCodeType(Barcode128.CODE128);
                qrImg = code128.createImageWithBarcode(writer.getDirectContent(), null, null);
                qrImg.scaleToFit(95f, 40f);
            } catch (Exception ignored) {}
        }

        if (qrImg != null) {
            PdfPTable qrTable = new PdfPTable(1);
            qrTable.setWidthPercentage(55);
            qrTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            PdfPCell qrCell = new PdfPCell(qrImg, true);
            qrCell.setBackgroundColor(Color.WHITE);
            qrCell.setBorderColor(new Color(226, 232, 240));
            qrCell.setBorderWidth(1f);
            qrCell.setPadding(3f);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrTable.addCell(qrCell);
            c2.addElement(qrTable);
        }
        mainBody.addCell(c2);

        // Columna 3: Vuelo, Puerta, Asiento (Bloques azules verticales)
        PdfPCell c3 = new PdfPCell();
        c3.setBackgroundColor(cardBg);
        c3.setBorder(PdfPCell.NO_BORDER);
        c3.setPadding(7f);

        addBoxCol(c3, "VUELO", t.getNumeroVuelo() != null ? t.getNumeroVuelo() : "---", blueDark, 10f);
        addBoxCol(c3, "PUERTA", t.getCodigoPuerta() != null ? t.getCodigoPuerta() : "TBD", blueMedium, 10f);
        addBoxCol(c3, "ASIENTO", t.getNumeroAsiento() != null ? t.getNumeroAsiento() : "---", blueDark, 11f);
        mainBody.addCell(c3);

        PdfPCell mbWrapper = new PdfPCell(mainBody);
        mbWrapper.setBorder(PdfPCell.NO_BORDER);
        mbWrapper.setPadding(0);
        mainTable.addCell(mbWrapper);

        // C. Footer Nota Importante
        PdfPCell noteCell = new PdfPCell();
        noteCell.setBackgroundColor(cardBg);
        noteCell.setBorder(PdfPCell.NO_BORDER);
        noteCell.setPaddingLeft(8f);
        noteCell.setPaddingRight(8f);
        noteCell.setPaddingBottom(6f);

        Paragraph pNote = new Paragraph();
        pNote.add(new Phrase("NOTA IMPORTANTE: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6.5f, blueDark)));
        pNote.add(new Phrase("Debes estar en la puerta de embarque antes de " + strLimite, FontFactory.getFont(FontFactory.COURIER, 6.5f, grayText)));
        noteCell.addElement(pNote);
        mainTable.addCell(noteCell);

        leftCell.addElement(mainTable);
        card.addCell(leftCell);

        // ==========================================
        // 2. LADO DERECHO: TALÓN DEL PASAJERO (STUB)
        // ==========================================
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBackgroundColor(cardBg);
        rightCell.setBorderColor(borderCard);
        rightCell.setBorderWidth(1f);
        rightCell.setPadding(0);

        PdfPTable stubTable = new PdfPTable(1);
        stubTable.setWidthPercentage(100);

        // A. Header Talón
        PdfPCell shCell = new PdfPCell();
        shCell.setBackgroundColor(skyBlue);
        shCell.setBorder(PdfPCell.NO_BORDER);
        shCell.setPadding(7f);
        Paragraph pSh = new Paragraph("··· TARJETA DE EMBARQUE ···", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, Color.WHITE));
        pSh.setAlignment(Element.ALIGN_CENTER);
        shCell.addElement(pSh);
        stubTable.addCell(shCell);

        // B. Body Talón
        PdfPCell sbCell = new PdfPCell();
        sbCell.setBackgroundColor(cardBg);
        sbCell.setBorder(PdfPCell.NO_BORDER);
        sbCell.setPadding(7f);

        addLabeledBox(sbCell, "PASAJERO", t.getNombre() != null ? t.getNombre() : "---", Color.WHITE, inkDark, 7f);
        addLabeledBox(sbCell, "CLASE", t.getClaseAsiento() != null ? t.getClaseAsiento() : "Económica", Color.WHITE, inkDark, 7.5f);
        addLabeledBox(sbCell, "ASIENTO", t.getNumeroAsiento() != null ? t.getNumeroAsiento() : "---", Color.WHITE, inkDark, 8f);

        // Ruta Talón (Vuelo badge + Ruta IATA)
        PdfPTable rTable = new PdfPTable(1);
        rTable.setWidthPercentage(100);
        PdfPCell rCell = new PdfPCell();
        rCell.setBackgroundColor(fieldBg);
        rCell.setBorderColor(new Color(186, 230, 253));
        rCell.setPadding(4f);

        Paragraph pVueloBadge = new Paragraph("VUELO " + (t.getNumeroVuelo() != null ? t.getNumeroVuelo() : "---"), FontFactory.getFont(FontFactory.COURIER_BOLD, 7f, blueDark));
        rCell.addElement(pVueloBadge);

        Paragraph pRutaIata = new Paragraph((t.getOrigenTramo() != null ? t.getOrigenTramo() : "---") + " ➔ " + (t.getDestinoTramo() != null ? t.getDestinoTramo() : "---"),
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13f, inkDark));
        pRutaIata.setAlignment(Element.ALIGN_CENTER);
        rCell.addElement(pRutaIata);
        rTable.addCell(rCell);

        sbCell.addElement(rTable);
        stubTable.addCell(sbCell);

        // C. Logo inferior en el Talón
        PdfPCell sBrandCell = new PdfPCell();
        sBrandCell.setBackgroundColor(cardBg);
        sBrandCell.setBorder(PdfPCell.NO_BORDER);
        sBrandCell.setPaddingBottom(7f);
        sBrandCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        boolean logoDrawn = false;
        if (baseLogoImg != null) {
            try {
                Image stubLogo = Image.getInstance(baseLogoImg);
                stubLogo.scaleToFit(85f, 18f);
                stubLogo.setAlignment(Element.ALIGN_CENTER);
                sBrandCell.addElement(stubLogo);
                logoDrawn = true;
            } catch (Exception ignored) {}
        }
        if (!logoDrawn) {
            Paragraph pLogoText = new Paragraph("✈  SkyWay Airlines", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f, blueDark));
            pLogoText.setAlignment(Element.ALIGN_CENTER);
            sBrandCell.addElement(pLogoText);
        }
        stubTable.addCell(sBrandCell);

        rightCell.addElement(stubTable);
        card.addCell(rightCell);

        return card;
    }

    private void addLabeledBox(PdfPCell container, String label, String value, Color bg, Color textCol, float fontSize) {
        Paragraph pLabel = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6f, new Color(15, 23, 42)));
        pLabel.setSpacingBefore(1f);
        pLabel.setSpacingAfter(1f);
        container.addElement(pLabel);

        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.COURIER_BOLD, fontSize, textCol)));
        c.setBackgroundColor(bg);
        c.setBorderColor(new Color(226, 232, 240));
        c.setBorderWidth(0.5f);
        c.setPadding(2f);
        box.addCell(c);
        box.setSpacingAfter(2f);
        container.addElement(box);
    }

    private void addBoxCol(PdfPCell container, String label, String value, Color bg, float valSize) {
        PdfPTable box = new PdfPTable(1);
        box.setWidthPercentage(100);
        PdfPCell c = new PdfPCell();
        c.setBackgroundColor(bg);
        c.setBorder(PdfPCell.NO_BORDER);
        c.setPadding(3f);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLbl = new Paragraph(label, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 6f, Color.WHITE));
        pLbl.setAlignment(Element.ALIGN_CENTER);
        c.addElement(pLbl);

        Paragraph pVal = new Paragraph(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, valSize, Color.WHITE));
        pVal.setAlignment(Element.ALIGN_CENTER);
        c.addElement(pVal);

        box.addCell(c);
        box.setSpacingAfter(3f);
        container.addElement(box);
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