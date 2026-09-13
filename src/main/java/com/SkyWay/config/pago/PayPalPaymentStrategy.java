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
    public String iniciarTransaccion(SolicitudPagoDTO solicitud) throws Exception {
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
        return order.links().stream()
                .filter(link -> "approve".equals(link.rel()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No se encontró link de aprobación en PayPal"))
                .href();
    }

    @Override
    public boolean confirmarPago(String tokenOrderPayPal) {
        try {
            OrdersCaptureRequest request = new OrdersCaptureRequest(tokenOrderPayPal);
            HttpResponse<Order> response = payPalClient.execute(request);
            return "COMPLETED".equals(response.result().status());
        } catch (Exception e) {
            return false;
        }
    }
}