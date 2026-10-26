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
public class PayPalController {

    private final PagoRetornoService pagoRetornoService;

    public PayPalController(PagoRetornoService pagoRetornoService) {
        this.pagoRetornoService = pagoRetornoService;
    }

    /** Retorno de PayPal: 'token' es el id de la orden; se valida contra el pago pendiente de la sesion. */
    @GetMapping("/paypal/commit")
    public RedirectView responderPayPal(
            @RequestParam(value = "token", required = false) Optional<String> tokenPayPal,
            HttpServletRequest request) {

        return new RedirectView(pagoRetornoService.procesar(MetodoPagoEnum.PAYPAL,
                tokenPayPal.orElse(null), request.getSession(false)));
    }

    @GetMapping("/paypal/cancel")
    public RedirectView cancelar() {
        Logger.logInfo("El usuario canceló el pago en la pasarela.");
        return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
    }
}
