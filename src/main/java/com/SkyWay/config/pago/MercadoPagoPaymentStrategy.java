package com.SkyWay.config.pago;

import com.SkyWay.util.Logger;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.*;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class MercadoPagoPaymentStrategy implements PasarelaPagoStrategy {

    public MercadoPagoPaymentStrategy(@Value("${mercadopago.access-token}") String accessToken) {
        // Inicializar la configuración global de Mercado Pago con el Access Token
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    @Override
    public MetodoPagoEnum getMetodoPago() {
        return MetodoPagoEnum.MERCADOPAGO; // Asegúrate de tener este Enum configurado
    }

    /**
     * Crea una Preferencia de Pago en Mercado Pago y retorna la URL del Checkout Pro
     * a la que se debe redirigir al usuario.
     */
    @Override
    public String iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception {
        try {
            PreferenceClient client = new PreferenceClient();

            // 1. Crear el ítem que se va a cobrar
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(solicitud.getOrdenCompra())
                    .title("Reserva de Vuelo / Servicio - SkyWay")
                    .quantity(1)
                    .currencyId(solicitud.getMoneda() != null ? solicitud.getMoneda() : "CLP")
                    .unitPrice(solicitud.getMonto())
                    .build();

            List<PreferenceItemRequest> items = Collections.singletonList(itemRequest);

            // 2. Definir URLs de retorno (Back URLs)
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(solicitud.getReturnUrl())
                    .failure(solicitud.getCancelUrl())
                    .pending(solicitud.getReturnUrl())
                    .build();

            // 3. Crear el Request de la Preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .externalReference(solicitud.getOrdenCompra())
                    .backUrls(backUrls)
                    .autoReturn("approved") // Redirige automáticamente al aprobar
                    .build();

            // 4. Ejecutar la llamada a la API de Mercado Pago
            Preference preference = client.create(preferenceRequest);

            Logger.logInfo("Preferencia MercadoPago Creada - ID: " + preference.getId()
                    + " | Orden: " + solicitud.getOrdenCompra());

            // sandboxInitPoint para pruebas, initPoint para producción.
            // `preference.getInitPoint()` redirige automáticamente según las credenciales.
            return preference.getInitPoint();

        } catch (Exception e) {
            Logger.logError("Error al iniciar la transacción en Mercado Pago: " + e.getMessage());
            throw new Exception("No fue posible iniciar el pago con Mercado Pago.", e);
        }
    }

    /**
     * Confirma la transacción consultando el estado del pago mediante el payment_id.
     *
     * @param paymentId Identificador del pago retornado por Mercado Pago en la query param 'payment_id'.
     * @return true si el pago fue aprobado ('approved').
     */
    @Override
    public boolean confirmarPago(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            Logger.logError("No se recibió payment_id para confirmar en Mercado Pago.");
            return false;
        }

        try {
            PaymentClient paymentClient = new PaymentClient();
            Long id = Long.parseLong(paymentId);

            // Consultar el estado del pago directamente en Mercado Pago
            Payment payment = paymentClient.get(id);

            Logger.logInfo("Respuesta Pago MercadoPago - ID: " + payment.getId()
                    + " | Estado: " + payment.getStatus()
                    + " | Referencia Externa: " + payment.getExternalReference());

            // Verificar si el estado es 'approved'
            return "approved".equalsIgnoreCase(payment.getStatus());

        } catch (Exception e) {
            Logger.logError("Error al confirmar el pago en Mercado Pago para ID: " + paymentId + " - " + e.getMessage());
            return false;
        }
    }
}