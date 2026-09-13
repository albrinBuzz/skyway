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
public class MercadoPagoController {

    @Autowired
    private ReservaProcesadorService procesarReservaUseCase;

    private final PagoFactoryService pagoFactoryService;

    public MercadoPagoController(PagoFactoryService pagoFactoryService) {
        this.pagoFactoryService = pagoFactoryService;
    }

    /**
     * Endpoint al cual redirige Mercado Pago tras completar el proceso de pago.
     * Recibe query params como payment_id, status y external_reference.
     */
    @GetMapping("/mercadopago/commit")
    public RedirectView responderMercadoPago(
            @RequestParam(value = "payment_id", required = false) Optional<String> paymentId,
            @RequestParam(value = "status", required = false) Optional<String> status,
            HttpServletRequest request) {

        // Si el estado enviado por URL es cancelado o rechazado explícitamente
        if (status.isPresent() && "rejected".equalsIgnoreCase(status.get())) {
            Logger.logInfo("El pago fue rechazado por Mercado Pago.");
            return new RedirectView("/home/errorPago.xhtml?reason=REJECTED");
        }

        if (paymentId.isPresent() && !paymentId.get().isBlank()) {
            String idPago = paymentId.get();

            // Obtener la estrategia de Mercado Pago desde la fábrica
            PasarelaPagoStrategy mpStrategy = pagoFactoryService.obtenerEstrategia(MetodoPagoEnum.MERCADOPAGO);
            boolean pagoAutorizado = mpStrategy.confirmarPago(idPago);

            if (!pagoAutorizado) {
                Logger.logError("❌ Mercado Pago no autorizó la transacción ID: " + idPago);
                return new RedirectView("/home/errorPago.xhtml?reason=REJECTED");
            }

            // Obtener la sesión pendiente
            HttpSession session = request.getSession(false);
            SolicitudReservaDTO dto = (session != null)
                    ? (SolicitudReservaDTO) session.getAttribute("SOLICITUD_RESERVA_PENDIENTE")
                    : null;

            if (dto == null) {
                Logger.logError("❌ No se encontraron los datos DTO de la reserva en la sesión.");
                return new RedirectView("/home/errorPago.xhtml?reason=EXPIRED_SESSION");
            }

            try {
                // Persistir reserva en la BD
                Integer idReserva = procesarReservaUseCase.ejecutarPersistenciaReserva(dto);
                session.removeAttribute("SOLICITUD_RESERVA_PENDIENTE");
                return new RedirectView("/home/pago-exitoso.xhtml?idReserva=" + idReserva);
            } catch (Exception e) {
                Logger.logError("💥 Error al procesar reserva tras pago Mercado Pago: " + e.getMessage());
                return new RedirectView("/home/errorPago.xhtml?reason=PERSISTENCE_ERROR");
            }
        }

        return new RedirectView("/home/errorPago.xhtml?reason=INVALID_TOKEN");
    }

    /**
     * Endpoint de falla o cancelación configurado en la preferencia.
     */
    @GetMapping("/mercadopago/cancel")
    public RedirectView cancelarMercadoPago() {
        Logger.logInfo("El usuario canceló el pago en el formulario de Mercado Pago.");
        return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
    }
}