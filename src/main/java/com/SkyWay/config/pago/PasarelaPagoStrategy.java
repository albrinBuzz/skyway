package com.SkyWay.config.pago;

public interface PasarelaPagoStrategy {

    /**
     * Identificador del método de pago (ej: WEBPAY, MERCADOPAGO, PAYPAL)
     */
    MetodoPagoEnum getMetodoPago();

    /**
     * Inicia la transacción con la pasarela seleccionada.
     * @return URL de redirección a la que JSF enviará al usuario.
     */
    String iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception;

    /**
     * Confirma el pago cuando el proveedor redirige de vuelta a nuestro sistema.
     */
    boolean confirmarPago(String tokenOIdentificador);
}