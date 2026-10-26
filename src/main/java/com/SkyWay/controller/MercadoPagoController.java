package com.SkyWay.controller;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoRetornoService;
import com.SkyWay.util.Logger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
public class MercadoPagoController {

    private final PagoRetornoService pagoRetornoService;

    public MercadoPagoController(PagoRetornoService pagoRetornoService) {
        this.pagoRetornoService = pagoRetornoService;
    }

    /** Retorno de Mercado Pago: se verifica contra la API de MP, no contra los query params. */
    @GetMapping("/mercadopago/commit")
    public RedirectView responderMercadoPago(
            @RequestParam(value = "payment_id", required = false) Optional<String> paymentId,
            @RequestParam(value = "status", required = false) Optional<String> status,
            HttpServletRequest request) {

        if (status.isPresent() && "rejected".equalsIgnoreCase(status.get())) {
            return new RedirectView("/home/errorPago.xhtml?reason=REJECTED");
        }
        return new RedirectView(pagoRetornoService.procesar(MetodoPagoEnum.MERCADOPAGO,
                paymentId.orElse(null), request.getSession(false)));
    }

    @GetMapping("/mercadopago/cancel")
    public RedirectView cancelar() {
        Logger.logInfo("El usuario canceló el pago en la pasarela.");
        return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
    }
}
