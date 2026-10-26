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
import java.math.BigDecimal;
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
    public InicioPago iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception {
        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);

            double montoDouble = solicitud.getMonto().doubleValue();

            WebpayPlusTransactionCreateResponse response = transaction.create(
                    solicitud.getOrdenCompra(),
                    solicitud.getSessionId(),
                    montoDouble,
                    solicitud.getReturnUrl()
            );

            Logger.logInfo("WebPay Transaction Iniciada - Orden: " + solicitud.getOrdenCompra());

            return new InicioPago(response.getUrl() + "?token_ws=" + response.getToken(), response.getToken());

        } catch (TransactionCreateException | IOException e) {
            Logger.logError("Error al crear la transacción en Webpay: " + e.getMessage());
            throw new Exception("No fue posible comunicarse con Webpay. Intente nuevamente.", e);
        }
    }

    /**
     * Confirma la transacción en Webpay (commit) y valida buyOrder, sessionId y monto contra el pago pendiente.
     */
    @Override
    public PagoConfirmado confirmarPago(String tokenWS, PagoPendiente esperado) {
        if (tokenWS == null || tokenWS.isBlank() || esperado == null) {
            Logger.logError("Retorno WebPay sin token o sin pago pendiente.");
            return PagoConfirmado.rechazado(tokenWS);
        }

        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);

            // Pre-chequeo sin capturar: evita hacer commit de un token que no es de esta sesion.
            try {
                var estado = transaction.status(tokenWS);
                BigDecimal montoEstado = BigDecimal.valueOf(estado.getAmount());
                if (!esperado.getOrdenCompra().equals(estado.getBuyOrder())
                        || !esperado.getSessionId().equals(estado.getSessionId())
                        || montoEstado.compareTo(esperado.getMonto()) != 0) {
                    Logger.logError("Token WebPay no corresponde al pago pendiente de esta sesion.");
                    return PagoConfirmado.rechazado(tokenWS);
                }
            } catch (Exception e) {
                // Si la consulta previa falla se valida igualmente despues del commit (y se reembolsa si no coincide).
                Logger.logWarn("No se pudo consultar estado previo WebPay: " + e.getMessage());
            }

            var response = transaction.commit(tokenWS);
            boolean autorizado = "AUTHORIZED".equalsIgnoreCase(response.getStatus()) && response.getResponseCode() == 0;
            BigDecimal monto = BigDecimal.valueOf(response.getAmount());

            Logger.logInfo("Respuesta commit Webpay - Orden: " + response.getBuyOrder()
                    + " | Estado: " + response.getStatus());

            if (!autorizado) {
                return PagoConfirmado.rechazado(tokenWS);
            }

            boolean coincide = esperado.getOrdenCompra().equals(response.getBuyOrder())
                    && esperado.getSessionId().equals(response.getSessionId())
                    && monto.compareTo(esperado.getMonto()) == 0;

            return new PagoConfirmado(true, coincide, tokenWS, null, monto, "CLP");

        } catch (TransactionCommitException | IOException e) {
            Logger.logError("Error al confirmar transacción (commit) en Webpay: " + e.getMessage());
            return PagoConfirmado.rechazado(tokenWS);
        }
    }

    @Override
    public boolean reembolsar(PagoConfirmado pago) {
        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);
            transaction.refund(pago.getIdExterno(), pago.getMonto().doubleValue());
            return true;
        } catch (Exception e) {
            Logger.logError("Error al reembolsar en Webpay: " + e.getMessage());
            return false;
        }
    }
}
