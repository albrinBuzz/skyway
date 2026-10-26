package com.SkyWay.config.pago;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Pago iniciado por el servidor y guardado en la sesion HTTP (estado PENDIENTE).
 * Es la unica referencia valida contra la que se compara lo que devuelve la pasarela.
 */
public class PagoPendiente implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MetodoPagoEnum proveedor;
    private final String ordenCompra;
    private final String sessionId;
    /** Monto enviado a la pasarela (en la moneda de la pasarela). */
    private final BigDecimal monto;
    private final String moneda;
    /** Monto de la reserva en CLP, tomado del servidor. */
    private final BigDecimal montoReserva;
    /** Token WebPay, orderId PayPal o preferenceId MercadoPago devueltos al iniciar. */
    private final String idExternoInicio;

    public PagoPendiente(MetodoPagoEnum proveedor, String ordenCompra, String sessionId,
                         BigDecimal monto, String moneda, BigDecimal montoReserva, String idExternoInicio) {
        this.proveedor = proveedor;
        this.ordenCompra = ordenCompra;
        this.sessionId = sessionId;
        this.monto = monto;
        this.moneda = moneda;
        this.montoReserva = montoReserva;
        this.idExternoInicio = idExternoInicio;
    }

    public MetodoPagoEnum getProveedor() { return proveedor; }
    public String getOrdenCompra() { return ordenCompra; }
    public String getSessionId() { return sessionId; }
    public BigDecimal getMonto() { return monto; }
    public String getMoneda() { return moneda; }
    public BigDecimal getMontoReserva() { return montoReserva; }
    public String getIdExternoInicio() { return idExternoInicio; }
}
