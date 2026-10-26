package com.SkyWay.config.pago;

import java.math.BigDecimal;

/**
 * Resultado de confirmar un pago contra la pasarela.
 * capturado: la pasarela ya tomo el dinero. aprobado: ademas coincide con el pago pendiente.
 * Si capturado && !aprobado hay que reembolsar.
 */
public class PagoConfirmado {
    private final boolean capturado;
    private final boolean aprobado;
    private final String idExterno;
    private final String idCaptura;
    private final BigDecimal monto;
    private final String moneda;

    public PagoConfirmado(boolean capturado, boolean aprobado, String idExterno, String idCaptura,
                          BigDecimal monto, String moneda) {
        this.capturado = capturado;
        this.aprobado = aprobado;
        this.idExterno = idExterno;
        this.idCaptura = idCaptura;
        this.monto = monto;
        this.moneda = moneda;
    }

    public static PagoConfirmado rechazado(String idExterno) {
        return new PagoConfirmado(false, false, idExterno, null, null, null);
    }

    public boolean isCapturado() { return capturado; }
    public boolean isAprobado() { return aprobado; }
    public String getIdExterno() { return idExterno; }
    /** Id de captura (PayPal) necesario para reembolsar; null en otras pasarelas. */
    public String getIdCaptura() { return idCaptura; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
}
