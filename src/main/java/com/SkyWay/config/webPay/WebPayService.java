package com.SkyWay.config.webPay;

import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.exception.TransactionCommitException;
import cl.transbank.webpay.exception.TransactionCreateException;
import cl.transbank.webpay.exception.TransactionRefundException;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import com.SkyWay.config.webPay.entity.RefundRequest;
import com.SkyWay.util.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class WebPayService {

    private final WebpayOptions webpayOptions;

    public WebPayService(WebpayOptions webpayOptions) {
        this.webpayOptions = webpayOptions;
    }

    /**
     * Crea una transacción en WebPay Plus utilizando la librería oficial de Transbank.
     */
    public WebpayPlusTransactionCreateResponse createTransaction(String buyOrder, String sessionId, double amount, String returnUrl) throws TransactionCreateException, IOException {
        WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);
        return transaction.create(buyOrder, sessionId, amount, returnUrl);
    }

    /**
     * Confirma y valida la autorización de un pago con WebPay Plus mediante su token.
     */
    public boolean confirmarPagoWebPay(String token) {
        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);
            var respuesta = transaction.commit(token);

            Logger.logInfo("Respuesta WebPay: " + respuesta);
            return "AUTHORIZED".equals(respuesta.getStatus());
        } catch (TransactionCommitException | IOException e) {
            Logger.logError("Error al confirmar transacción en WebPay: " + e.getMessage());
        }
        return false;
    }

    /**
     * Realiza un rollback o anula la transacción.
     */
    public String refundTransaction(String token, RefundRequest request) {
        try {
            WebpayPlus.Transaction transaction = new WebpayPlus.Transaction(webpayOptions);
            var respuesta = transaction.refund(token, request.getAmount());
            return respuesta.toString();
        } catch (TransactionRefundException | IOException e) {
            return "Error al anular la transacción: " + e.getMessage();
        }
    }
}