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
public class WebPayController {

    private final PagoRetornoService pagoRetornoService;

    public WebPayController(PagoRetornoService pagoRetornoService) {
        this.pagoRetornoService = pagoRetornoService;
    }

    @GetMapping("/webpay/commit")
    public RedirectView responderWebPay(
            @RequestParam(value = "token_ws", required = false) Optional<String> tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) Optional<String> tbkToken,
            HttpServletRequest request) {

        if (tbkToken.isPresent()) {
            return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
        }
        return new RedirectView(pagoRetornoService.procesar(MetodoPagoEnum.WEBPAY,
                tokenWs.orElse(null), request.getSession(false)));
    }

    @GetMapping("/webpay/cancel")
    public RedirectView cancelar() {
        Logger.logInfo("El usuario canceló el pago en la pasarela.");
        return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
    }
}
