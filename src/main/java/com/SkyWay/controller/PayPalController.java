package com.SkyWay.controller;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.modules.reserva.domain.service.ReservaProcesadorService;
import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.modules.reservaasiento.presentation.dto.SolicitudCambioAsientoDTO;
import com.SkyWay.modules.tarifa.domain.service.UpgradeTarifaService;
import com.SkyWay.modules.tarifa.presentation.dto.ResultadoUpgradeDTO;
import com.SkyWay.modules.tarifa.presentation.dto.SolicitudUpgradeDTO;
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
    @Autowired
    private UpgradeTarifaService upgradeTarifaService;
    @Autowired
    private com.SkyWay.modules.reservaasiento.domain.service.CambioAsientoProcesadorService cambioAsientoProcesadorService;

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

            // Recuperar sesión
            HttpSession session = request.getSession(false);

            // 1. Verificar si corresponde a un Ascenso de Tarifa (Upgrade)
            SolicitudUpgradeDTO upgradeDto = (session != null)
                    ? (SolicitudUpgradeDTO) session.getAttribute("SOLICITUD_UPGRADE_PENDIENTE")
                    : null;

            if (upgradeDto != null) {
                Logger.logInfo("🎯 [PAYPAL] Confirmando Ascenso de Tarifa para Reserva ID: " + upgradeDto.getIdReserva());
                int idMetodoPagoPayPal = 2;
                ResultadoUpgradeDTO resultadoUpgrade = upgradeTarifaService.procesarYConfirmarUpgrade(
                        upgradeDto.getIdReserva(),
                        upgradeDto.getIdItinerario(),
                        upgradeDto.getIdItinerarioTarifaNuevo(),
                        idMetodoPagoPayPal,
                        "PayPal Express Checkout"
                );
                session.removeAttribute("SOLICITUD_UPGRADE_PENDIENTE");

                if (resultadoUpgrade != null) {
                    resultadoUpgrade.setOrigen(upgradeDto.getOrigen());
                    session.setAttribute("RESULTADO_UPGRADE_CONFIRMADO", resultadoUpgrade);

                    if ("checkin".equalsIgnoreCase(upgradeDto.getOrigen())) {
                        session.setAttribute("CHECKIN_ID_RESERVA", upgradeDto.getIdReserva());
                        session.setAttribute("CHECKIN_ID_ITINERARIO", upgradeDto.getIdItinerario());
                        return new RedirectView("/home/checkin.xhtml?upgradeSuccess=true");
                    } else {
                        return new RedirectView("/perfil/pasajero/index.xhtml?upgradeSuccess=true");
                    }
                } else {
                    return new RedirectView("/home/errorPago.xhtml?reason=UPGRADE_ERROR");
                }
            }

            // 2. Verificar si corresponde a Cambio de Asiento con Pago
            SolicitudCambioAsientoDTO cambioDto = (session != null)
                    ? (SolicitudCambioAsientoDTO) session.getAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE")
                    : null;

            if (cambioDto != null) {
                Logger.logInfo("🎯 [PAYPAL] Confirmando Cambio de Asientos para Reserva ID: " + cambioDto.getIdReserva());
                int idMetodoPagoPayPal = 2;
                com.SkyWay.modules.reservaasiento.presentation.dto.ResultadoCambioAsientoDTO resultado = cambioAsientoProcesadorService.procesarYConfirmarCambioAsiento(cambioDto, idMetodoPagoPayPal, "PayPal Express Checkout", request);
                session.removeAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE");

                if (resultado != null) {
                    session.setAttribute("RESULTADO_CAMBIO_ASIENTO", resultado);
                    session.setAttribute("CAMBIO_ASIENTO_STATUS", "SUCCESS");
                    session.setAttribute("CAMBIO_ASIENTO_ID_RESERVA", cambioDto.getIdReserva());
                    return new RedirectView("/home/cambio-asiento.xhtml");
                } else {
                    return new RedirectView("/home/errorPago.xhtml?reason=CAMBIO_ASIENTO_ERROR");
                }
            }

            // 3. Recuperar DTO guardado en la sesión para nueva reserva
            SolicitudReservaDTO dto = (session != null)
                    ? (SolicitudReservaDTO) session.getAttribute("SOLICITUD_RESERVA_PENDIENTE")
                    : null;

            if (dto == null) {
                Logger.logError("❌ No se encontraron los datos DTO de la reserva en la sesión.");
                return new RedirectView("/home/errorPago.xhtml?reason=EXPIRED_SESSION");
            }

            try {
                // ID_METODO_PAGO = 2 (PayPal según la tabla Metodo_Pago)
                int idMetodoPagoPayPal = 2;
                Logger.logInfo("💾 Persistiendo reserva con ID Método de Pago PAYPAL (" + idMetodoPagoPayPal + ")...");

                Integer idReserva = procesarReservaUseCase.ejecutarPersistenciaReserva(dto, idMetodoPagoPayPal);

                session = request.getSession(false);
                if (session != null) {
                    session.setAttribute("ULTIMO_ID_RESERVA", idReserva);
                }

                session.removeAttribute("SOLICITUD_RESERVA_PENDIENTE");
                Logger.logInfo("🎉 [PAYPAL-CONTROLLER] Reserva ID: " + idReserva + " procesada con éxito. Redirigiendo a pago-exitoso.xhtml");
                return new RedirectView("/home/pago-exitoso.xhtml");
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
    public RedirectView cancelarPayPal(HttpServletRequest request) {
        Logger.logInfo("⚠️ El usuario canceló la compra en la pasarela de PayPal.");
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("SOLICITUD_UPGRADE_PENDIENTE") != null) {
            SolicitudUpgradeDTO upg = (SolicitudUpgradeDTO) session.getAttribute("SOLICITUD_UPGRADE_PENDIENTE");
            session.removeAttribute("SOLICITUD_UPGRADE_PENDIENTE");
            if ("checkin".equalsIgnoreCase(upg.getOrigen())) {
                session.setAttribute("CHECKIN_ID_RESERVA", upg.getIdReserva());
                session.setAttribute("CHECKIN_ID_ITINERARIO", upg.getIdItinerario());
                return new RedirectView("/home/checkin.xhtml?upgradeCancelled=true");
            } else {
                return new RedirectView("/perfil/pasajero/index.xhtml?upgradeCancelled=true");
            }
        }
        if (session != null && session.getAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE") != null) {
            SolicitudCambioAsientoDTO cambioDto = (SolicitudCambioAsientoDTO) session.getAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE");
            session.removeAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE");
            session.setAttribute("CAMBIO_ASIENTO_STATUS", "CANCELLED");
            session.setAttribute("CAMBIO_ASIENTO_ID_RESERVA", cambioDto.getIdReserva());
            return new RedirectView("/home/cambio-asiento.xhtml");
        }
        return new RedirectView("/home/errorPago.xhtml?reason=CANCELLED");
    }
}