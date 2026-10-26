package com.SkyWay.config.pago;

public interface PasarelaPagoStrategy {

    /**
     * Identificador del método de pago (ej: WEBPAY, MERCADOPAGO, PAYPAL)
     */
    MetodoPagoEnum getMetodoPago();

    /**
     * Inicia la transacción con la pasarela seleccionada.
     * @return URL de redirección y id externo creado por la pasarela.
     */
    InicioPago iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception;

    /**
     * Confirma el pago cuando el proveedor redirige de vuelta a nuestro sistema, verificando contra
     * la respuesta real de la pasarela que orden, monto y moneda coinciden con el pago pendiente.
     */
    PagoConfirmado confirmarPago(String tokenOIdentificador, PagoPendiente esperado);

    /** Reembolsa/anula un pago ya capturado. @return true si la pasarela acepto el reembolso. */
    boolean reembolsar(PagoConfirmado pago);
}
