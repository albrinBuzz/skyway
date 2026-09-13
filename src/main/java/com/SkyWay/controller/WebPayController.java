package com.SkyWay.controller;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.webPay.WebPayService;

import com.SkyWay.modules.reserva.domain.service.ReservaProcesadorService;
import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.util.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;

@RestController
public class WebPayController {

    @Autowired private WebPayService webPayService;
    @Autowired private ReservaProcesadorService procesarReservaUseCase;


    private final PagoFactoryService pagoFactoryService;

    public WebPayController(PagoFactoryService pagoFactoryService) {
        this.pagoFactoryService = pagoFactoryService;
    }

    @GetMapping("/webpay/commit")
    public RedirectView responderWebPay(
            @RequestParam(value = "token_ws", required = false) Optional<String> tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) Optional<String> tbkToken,
            HttpServletRequest request) {

        // El usuario canceló la compra en la pasarela
        if (tbkToken.isPresent()) {
            return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
        }




        if (tokenWs.isPresent() && !tokenWs.get().isBlank()) {

            String token = tokenWs.get();
            boolean pagoAutorizado = webPayService.confirmarPagoWebPay(token);

            //PasarelaPagoStrategy webpayStrategy = pagoFactoryService.obtenerEstrategia(MetodoPagoEnum.WEBPAY);
            //boolean pagoAprobado = webpayStrategy.confirmarPago(String.valueOf(tokenWs));

            if (!pagoAutorizado) {
                return new RedirectView("/home/errorPago.xhtml?reason=REJECTED");
            }

            HttpSession session = request.getSession(false);
            SolicitudReservaDTO dto = (session != null) ? (SolicitudReservaDTO) session.getAttribute("SOLICITUD_RESERVA_PENDIENTE") : null;

            if (dto == null) {
                Logger.logError("❌ No se encontraron los datos DTO de la reserva en la sesión.");
                return new RedirectView("/home/errorPago.xhtml?reason=EXPIRED_SESSION");
            }

            try {
                Integer idReserva = procesarReservaUseCase.ejecutarPersistenciaReserva(dto);
                session.removeAttribute("SOLICITUD_RESERVA_PENDIENTE");
                return new RedirectView("/home/pago-exitoso.xhtml?idReserva=" + idReserva);
            } catch (Exception e) {
                Logger.logError("💥 Error al escribir la reserva: " + e.getMessage());
                return new RedirectView("/home/errorPago.xhtml?reason=PERSISTENCE_ERROR");
            }
        }

        return new RedirectView("/home/errorPago.xhtml?reason=INVALID_TOKEN");
    }
}