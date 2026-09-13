package com.SkyWay.config.pago;

import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.exception.TransactionCommitException;
import cl.transbank.webpay.exception.TransactionCreateException;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.SkyWay.util.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WebPayStrategy implements PasarelaPagoStrategy {

    private final WebpayOptions webpayOptions;

    public WebPayStrategy(WebpayOptions webpayOptions) {
        this.webpayOptions = webpayOptions;
    }

    @Override
    public MetodoPagoEnum getMetodoPago() {
        return MetodoPagoEnum.WEBPAY;
    }

    /**
     * Inicia la transacción con Transbank Webpay Plus y retorna la URL de redirección
     * concatenada con el token_ws requerido.
     */
    @Override
    public String iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception {
        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);

            // Webpay opera en pesos chilenos (CLP) y requiere el monto como un double
            double montoDouble = solicitud.getMonto().doubleValue();

            WebpayPlusTransactionCreateResponse response = transaction.create(
                    solicitud.getOrdenCompra(),
                    solicitud.getSessionId(),
                    montoDouble,
                    solicitud.getReturnUrl()
            );

            Logger.logInfo("WebPay Transaction Iniciada - Orden: "
                    + solicitud.getOrdenCompra() + " | Token: " + response.getToken());

            // La respuesta de Transbank entrega la URL del formulario de pago y el token
            return response.getUrl() + "?token_ws=" + response.getToken();

        } catch (TransactionCreateException | IOException e) {
            Logger.logError("Error al crear la transacción en Webpay: " + e.getMessage());
            throw new Exception("No fue posible comunicarse con Webpay. Intente nuevamente.", e);
        }
    }

    /**
     * Confirma la transacción en Webpay a partir del token_ws retornado.
     *
     * @param tokenWS El parámetro token_ws que envía Transbank por GET/POST al returnUrl.
     * @return true si la transacción fue exitosa (status "AUTHORIZED"), false en caso contrario.
     */
    @Override
    public boolean confirmarPago(String tokenWS) {
        if (tokenWS == null || tokenWS.isBlank()) {
            Logger.logError("No se recibió token_ws para confirmar la transacción en Webpay.");
            return false;
        }

        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);
            var response = transaction.commit(tokenWS);

            Logger.logInfo("Respuesta commit Webpay - Orden: " + response.getBuyOrder()
                    + " | Estado: " + response.getStatus()
                    + " | Código de Autorización: " + response.getAuthorizationCode());

            // En Webpay Plus, "AUTHORIZED" indica pago exitoso y aprobado
            return "AUTHORIZED".equalsIgnoreCase(response.getStatus());

        } catch (TransactionCommitException | IOException e) {
            Logger.logError("Error al confirmar transacción (commit) en Webpay: " + e.getMessage());
            return false;
        }
    }
}