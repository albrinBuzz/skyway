package com.SkyWay.views.reserva;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ETicketPdfGeneratorService;
import com.SkyWay.modules.reserva.domain.service.EmailReservaService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
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

        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        // 🔍 RECUPERAR EL ID DESDE LA SESIÓN DE JSF
        Integer idReserva = (Integer) sessionMap.get("ULTIMO_ID_RESERVA");

        if (idReserva != null) {
            try {
                // 🧹 BORRAR DE INMEDIATO DE LA SESIÓN PARA EVITAR RECARGAS MALICIOSAS O REUTILIZACIÓN
                sessionMap.remove("ULTIMO_ID_RESERVA");

                this.reserva = reservaService.guardarOGenerarPnr(idReserva);

                if (this.reserva != null) {
                    this.pnr = this.reserva.getCodigoReserva();
                    emailReservaService.enviarComprobanteReservaAsync(this.reserva);

                    // 1. Limpiar la memoria del CacheService (WebSockets / RAM)
                    String sessionId = (String) sessionMap.get("reservaSessionId");
                    if (sessionId != null) {
                        asientoCacheService.liberarTodosLosAsientosDeSesion(sessionId);
                        sessionMap.remove("reservaSessionId");
                    }

                    // 🧹 2. PURGA DE LOS DATOS DE COMPRA TEMPORALES
                    sessionMap.remove("asientosSeleccionados");
                    sessionMap.remove("itinerarios");
                    sessionMap.remove("tarifasItinerios");
                    sessionMap.remove("pasajeros");
                    sessionMap.remove("equipajePorPasajero");
                    sessionMap.remove("totalEquipaje");
                    sessionMap.remove("SOLICITUD_RESERVA_PENDIENTE");
                    sessionMap.remove("ultimaBusquedaAsientosQuery");

                    Logger.logInfo("🧹 [PagoExitosoController] Reserva cargada desde sesión de forma segura.");
                }
            } catch (Exception e) {
                Logger.logError("Error al cargar comprobante desde sesión: " + e.getMessage());
            }
        } else {
            Logger.logWarn("⚠️ Intento de acceso a pago-exitoso sin ID en sesión o por recarga directa.");
            // ❌ Ya no hacemos redirect aquí dentro para evitar el conflicto de commit.
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