package com.SkyWay.views.reserva;

import com.SkyWay.modules.reserva.domain.service.ReservaWalService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Named("errorPagoBean")
@ViewScoped
public class ErrorPagoBean implements Serializable {

    private String reason;
    private Long walId;
    private BigDecimal monto;
    private String motivo;
    private boolean reembolsoSolicitado = false;

    @Autowired
    private ReservaWalService reservaWalService;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) return;

        ExternalContext ext = context.getExternalContext();
        Map<String, String> params = ext.getRequestParameterMap();
        Map<String, Object> sessionMap = ext.getSessionMap();

        this.reason = params.get("reason");

        String walParam = params.get("walId");
        if (walParam != null && !walParam.isBlank()) {
            try {
                this.walId = Long.parseLong(walParam);
            } catch (NumberFormatException ignored) {}
        }
        if (this.walId == null && sessionMap.containsKey("WAL_FALLO_ID")) {
            Object idObj = sessionMap.get("WAL_FALLO_ID");
            if (idObj instanceof Number) {
                this.walId = ((Number) idObj).longValue();
            }
        }

        if (sessionMap.containsKey("WAL_FALLO_MONTO")) {
            Object montoObj = sessionMap.get("WAL_FALLO_MONTO");
            if (montoObj instanceof BigDecimal) {
                this.monto = (BigDecimal) montoObj;
            }
        }

        if (sessionMap.containsKey("WAL_FALLO_MOTIVO")) {
            this.motivo = (String) sessionMap.get("WAL_FALLO_MOTIVO");
        }

        if (this.monto == null && this.walId != null) {
            reservaWalService.obtenerMontoPorWalId(this.walId).ifPresent(m -> this.monto = m);
        }
    }

    public boolean isPagoCapturadoConFallo() {
        return "PERSISTENCE_ERROR".equalsIgnoreCase(reason) || walId != null;
    }

    public void solicitarReembolso() {
        if (walId != null) {
            reservaWalService.registrarSolicitudReembolso(walId, "Reembolso solicitado directamente por el cliente desde la vista de error.");
            this.reembolsoSolicitado = true;

            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Devolución Solicitada",
                    "Hemos registrado tu solicitud de devolución por " + getMontoFormateado() + ". Comprobante de respaldo: #SKW-PAG-" + walId
            ));
        }
    }

    public String getMontoFormateado() {
        if (monto == null) return "$0";
        try {
            java.text.NumberFormat nf = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-CL"));
            nf.setMaximumFractionDigits(0);
            return nf.format(monto);
        } catch (Exception e) {
            return "$" + monto.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        }
    }

    public String getCodigoReferencia() {
        return walId != null ? "SKW-PAG-" + walId : "PENDIENTE";
    }

    public String getReason() { return reason; }
    public Long getWalId() { return walId; }
    public BigDecimal getMonto() { return monto; }
    public String getMotivo() { return motivo; }
    public boolean isReembolsoSolicitado() { return reembolsoSolicitado; }
}
