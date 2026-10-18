package com.SkyWay.views.reserva;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ETicketPdfGeneratorService;
import com.SkyWay.modules.reserva.domain.service.EmailReservaService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Map;

@Named("pagoExitosoController")
@ViewScoped
public class PagoExitosoController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired private ReservaService reservaService;
    @Autowired private EmailReservaService emailReservaService;
    @Autowired private ETicketPdfGeneratorService eTicketPdfGeneratorService;
    @Autowired private AsientoCacheService asientoCacheService;


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

                    // 1. Limpiar la memoria del CacheService (WebSockets / RAM)
                    String sessionId = facesContext.getExternalContext().getSessionId(false);
                    if (sessionId != null) {
                        asientoCacheService.liberarTodosLosAsientosDeSesion(sessionId);
                    }

                    // 🧹 2. PURGA COMPLETA DE LA SESIÓN DE JSF (MANDATORIO PARA PERMITIR NUEVAS COMPRAS)
                    Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
                    sessionMap.remove("asientosSeleccionados");
                    sessionMap.remove("itinerarios");
                    sessionMap.remove("tarifasItinerios");
                    sessionMap.remove("pasajeros");
                    sessionMap.remove("equipajePorPasajero");
                    sessionMap.remove("totalEquipaje");
                    sessionMap.remove("SOLICITUD_RESERVA_PENDIENTE");
                    sessionMap.remove("ultimaBusquedaAsientosQuery");

                    Logger.logInfo("🧹 [PagoExitosoController] Sesión de compra de JSF limpiada exitosamente para futuras reservas.");
                }
            } catch (Exception e) {
                Logger.logError("Error al cargar comprobante: " + e.getMessage());
            }
        }
    }

    /**
     * Descarga 1: Resumen de Reserva e Itinerario
     */
    public StreamedContent descargarComprobantePdf() {
        if (this.reserva == null) return null;
        byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfResumenReserva(this.reserva);
        return DefaultStreamedContent.builder()
                .name("Resumen_Reserva_SkyWay_" + this.reserva.getCodigoReserva() + ".pdf")
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(pdfBytes))
                .build();
    }

    /**
     * Descarga 2: Tarjetas de Embarque Individuales
     */
    public StreamedContent descargarBoardingPassesPdf() {
        if (this.reserva == null) return null;
        byte[] pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(this.reserva);
        return DefaultStreamedContent.builder()
                .name("Tarjetas_Embarque_SkyWay_" + this.reserva.getCodigoReserva() + ".pdf")
                .contentType("application/pdf")
                .stream(() -> new ByteArrayInputStream(pdfBytes))
                .build();
    }

    public Reserva getReserva() { return reserva; }
    public String getPnr() { return pnr; }
}