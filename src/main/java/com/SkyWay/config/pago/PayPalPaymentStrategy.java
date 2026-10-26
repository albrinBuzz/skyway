package com.SkyWay.config.pago;
import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.paypal.core.PayPalEnvironment;
import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.SkyWay.util.Logger;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalPaymentStrategy implements PasarelaPagoStrategy {

    private final PayPalHttpClient payPalClient;

    public PayPalPaymentStrategy(
            @Value("${paypal.client-id}") String clientId,
            @Value("${paypal.client-secret}") String clientSecret,
            @Value("${paypal.mode:sandbox}") String mode) {

        PayPalEnvironment environment = "live".equalsIgnoreCase(mode)
                ? new PayPalEnvironment.Live(clientId, clientSecret)
                : new PayPalEnvironment.Sandbox(clientId, clientSecret);

        this.payPalClient = new PayPalHttpClient(environment);
    }

    @Override
    public MetodoPagoEnum getMetodoPago() {
        return MetodoPagoEnum.PAYPAL;
    }

    @Override
    public InicioPago iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        // Contexto de experiencia (redirecciones)
        ApplicationContext applicationContext = new ApplicationContext()
                .returnUrl(solicitud.getReturnUrl())
                .cancelUrl(solicitud.getCancelUrl());
        orderRequest.applicationContext(applicationContext);

        // Detalle de la compra (Nota: PayPal suele requerir USD o monedas soportadas)
        List<PurchaseUnitRequest> purchaseUnitRequests = new ArrayList<>();
        PurchaseUnitRequest purchaseUnitRequest = new PurchaseUnitRequest()
                .referenceId(solicitud.getOrdenCompra())
                .amountWithBreakdown(new AmountWithBreakdown()
                        .currencyCode(solicitud.getMoneda()) // ej: "USD"
                        .value(solicitud.getMonto().toString()));
        purchaseUnitRequests.add(purchaseUnitRequest);
        orderRequest.purchaseUnits(purchaseUnitRequests);

        OrdersCreateRequest request = new OrdersCreateRequest().requestBody(orderRequest);
        HttpResponse<Order> response = payPalClient.execute(request);
        Order order = response.result();

        // Extraer la URL de aprobación a la que el usuario debe ser redirigido
        String href = order.links().stream()
                .filter(link -> "approve".equals(link.rel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró link de aprobación en PayPal"))
                .href();
        return new InicioPago(href, order.id());
    }

    @Override
    public PagoConfirmado confirmarPago(String tokenOrderPayPal, PagoPendiente esperado) {
        if (tokenOrderPayPal == null || tokenOrderPayPal.isBlank() || esperado == null) {
            return PagoConfirmado.rechazado(tokenOrderPayPal);
        }
        try {
            // 1. Verificar la orden ANTES de capturar: debe ser la orden creada para esta sesion.
            Order orden = payPalClient.execute(new OrdersGetRequest(tokenOrderPayPal)).result();
            PurchaseUnit unidad = orden.purchaseUnits().get(0);
            boolean ordenOk = tokenOrderPayPal.equals(esperado.getIdExternoInicio())
                    && esperado.getOrdenCompra().equals(unidad.referenceId())
                    && esperado.getMoneda().equalsIgnoreCase(unidad.amountWithBreakdown().currencyCode())
                    && new BigDecimal(unidad.amountWithBreakdown().value()).compareTo(esperado.getMonto()) == 0
                    && "APPROVED".equals(orden.status());
            if (!ordenOk) {
                Logger.logError("Orden PayPal no coincide con el pago pendiente de la sesion.");
                return PagoConfirmado.rechazado(tokenOrderPayPal);
            }

            // 2. Capturar y revisar la captura real.
            Order capturada = payPalClient.execute(new OrdersCaptureRequest(tokenOrderPayPal)).result();
            if (!"COMPLETED".equals(capturada.status())) {
                return PagoConfirmado.rechazado(tokenOrderPayPal);
            }
            Capture captura = capturada.purchaseUnits().get(0).payments().captures().get(0);
            BigDecimal monto = new BigDecimal(captura.amount().value());
            boolean coincide = "COMPLETED".equals(captura.status())
                    && esperado.getMoneda().equalsIgnoreCase(captura.amount().currencyCode())
                    && monto.compareTo(esperado.getMonto()) == 0;
            return new PagoConfirmado(true, coincide, tokenOrderPayPal, captura.id(), monto, captura.amount().currencyCode());
        } catch (Exception e) {
            Logger.logError("Error al confirmar pago PayPal: " + e.getMessage());
            return PagoConfirmado.rechazado(tokenOrderPayPal);
        }
    }

    @Override
    public boolean reembolsar(PagoConfirmado pago) {
        try {
            com.paypal.payments.CapturesRefundRequest request =
                    new com.paypal.payments.CapturesRefundRequest(pago.getIdCaptura());
            request.requestBody(new com.paypal.payments.RefundRequest());
            payPalClient.execute(request);
            return true;
        } catch (Exception e) {
            Logger.logError("Error al reembolsar en PayPal: " + e.getMessage());
            return false;
        }
    }
}
