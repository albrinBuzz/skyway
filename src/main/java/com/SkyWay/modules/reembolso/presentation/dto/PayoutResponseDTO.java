package com.SkyWay.modules.reembolso.presentation.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Respuesta normalizada de una pasarela bancaria o proveedor de pagos salientes (Payouts / TEF).
 */
public class PayoutResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean aprobado;
    private String codigoOperacion;
    private String codigoRespuestaBanco;
    private String codigoBancoSbif;
    private String canalCompensacion;
    private String idempotencyKey;
    private BigDecimal montoTransferido;
    private long latenciaMs;
    private Date fechaProceso;
    private String mensajeBanco;

    public boolean isAprobado() { return aprobado; }
    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }

    public String getCodigoOperacion() { return codigoOperacion; }
    public void setCodigoOperacion(String codigoOperacion) { this.codigoOperacion = codigoOperacion; }

    public String getCodigoRespuestaBanco() { return codigoRespuestaBanco; }
    public void setCodigoRespuestaBanco(String codigoRespuestaBanco) { this.codigoRespuestaBanco = codigoRespuestaBanco; }

    public String getCodigoBancoSbif() { return codigoBancoSbif; }
    public void setCodigoBancoSbif(String codigoBancoSbif) { this.codigoBancoSbif = codigoBancoSbif; }

    public String getCanalCompensacion() { return canalCompensacion; }
    public void setCanalCompensacion(String canalCompensacion) { this.canalCompensacion = canalCompensacion; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public BigDecimal getMontoTransferido() { return montoTransferido; }
    public void setMontoTransferido(BigDecimal montoTransferido) { this.montoTransferido = montoTransferido; }

    public long getLatenciaMs() { return latenciaMs; }
    public void setLatenciaMs(long latenciaMs) { this.latenciaMs = latenciaMs; }

    public Date getFechaProceso() { return fechaProceso; }
    public void setFechaProceso(Date fechaProceso) { this.fechaProceso = fechaProceso; }

    public String getMensajeBanco() { return mensajeBanco; }
    public void setMensajeBanco(String mensajeBanco) { this.mensajeBanco = mensajeBanco; }
}
