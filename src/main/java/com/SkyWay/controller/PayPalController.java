package com.SkyWay.controller;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
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
public class PayPalController {

    @Autowired
    private ReservaProcesadorService procesarReservaUseCase;

    private final PagoFactoryService pagoFactoryService;

    public PayPalController(PagoFactoryService pagoFactoryService) {
        this.pagoFactoryService = pagoFactoryService;
    }

    /**
     * Endpoint invocado por PayPal al completar la aprobación del pago (returnUrl).
     * PayPal devuelve el identificador de la orden en el parámetro query 'token'.
     */
    @GetMapping("/paypal/commit")
    public RedirectView responderPayPal(
            @RequestParam(value = "token", required = false) Optional<String> tokenPayPal,
            HttpServletRequest request) {

        Logger.logInfo("================================================================================");
        Logger.logInfo("🌐 [PAYPAL-CONTROLLER] Iniciando confirmación de retorno desde PayPal");
        Logger.logInfo("================================================================================");

        if (tokenPayPal.isPresent() && !tokenPayPal.get().isBlank()) {
            String token = tokenPayPal.get();
            Logger.logInfo("🔑 Token de PayPal recibido: " + token);

            // Usamos la estrategia mediante el Factory
            PasarelaPagoStrategy payPalStrategy = pagoFactoryService.obtenerEstrategia(MetodoPagoEnum.PAYPAL);
            boolean pagoAutorizado = payPalStrategy.confirmarPago(token);

            if (!pagoAutorizado) {
                Logger.logError("❌ PayPal no autorizó o falló la captura del pago para el token: " + token);
                return new RedirectView("/home/errorPago.xhtml?reason=REJECTED");
            }

            Logger.logInfo("✅ [PAYPAL] Pago AUTORIZADO exitosamente en la pasarela");

            // Recuperar DTO guardado en la sesión
            HttpSession session = request.getSession(false);
            SolicitudReservaDTO dto = (session != null)
                    ? (SolicitudReservaDTO) session.getAttribute("SOLICITUD_RESERVA_PENDIENTE")
                    : null;

            if (dto == null) {
                Logger.logError("❌ No se encontraron los datos DTO de la reserva en la sesión.");
                return new RedirectView("/home/errorPago.xhtml?reason=EXPIRED_SESSION");
            }

            try {
                // ID_METODO_PAGO = 4 (PayPal según la tabla Metodo_Pago)
                int idMetodoPagoPayPal = 2;
                Logger.logInfo("💾 Persistiendo reserva con ID Método de Pago PAYPAL (" + idMetodoPagoPayPal + ")...");

                Integer idReserva = procesarReservaUseCase.ejecutarPersistenciaReserva(dto, idMetodoPagoPayPal);

                session = request.getSession(false);
                if (session != null) {
                    session.setAttribute("ULTIMO_ID_RESERVA", idReserva);
                }

                session.removeAttribute("SOLICITUD_RESERVA_PENDIENTE");
                Logger.logInfo("🎉 [WEBPAY-CONTROLLER] Reserva ID: " + idReserva + " procesada con éxito. Redirigiendo a pago-exitoso.xhtml");


                Logger.logInfo("🎉 [PAYPAL-CONTROLLER] Reserva ID: " + idReserva + " procesada con éxito. Redirigiendo a pago-exitoso.xhtml");
                return new RedirectView("/home/pago-exitoso.xhtml");
                //return new RedirectView("/home/pago-exitoso.xhtml?idReserva=" + idReserva);
            } catch (Exception e) {
                Logger.logError("💥 Error al escribir la reserva tras pago con PayPal: " + e.getMessage());
                return new RedirectView("/home/errorPago.xhtml?reason=PERSISTENCE_ERROR");
            }
        }

        Logger.logError("❌ Token de PayPal inválido o ausente en la petición");
        return new RedirectView("/home/errorPago.xhtml?reason=INVALID_TOKEN");
    }

    /**
     * Endpoint invocado por PayPal si el usuario decide cancelar la transacción (cancelUrl).
     */
    @GetMapping("/paypal/cancel")
    public RedirectView cancelarPayPal() {
        Logger.logInfo("⚠️ El usuario canceló la compra en la pasarela de PayPal.");
        return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
    }
}