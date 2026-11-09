package com.SkyWay.controller;

import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.webPay.WebPayService;

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
public class WebPayController {

    @Autowired private WebPayService webPayService;
    @Autowired private ReservaProcesadorService procesarReservaUseCase;
    @Autowired private com.SkyWay.modules.reserva.domain.service.ReservaWalService reservaWalService;
    @Autowired private UpgradeTarifaService upgradeTarifaService;
    @Autowired private com.SkyWay.modules.reservaasiento.domain.service.CambioAsientoProcesadorService cambioAsientoProcesadorService;

    private final PagoFactoryService pagoFactoryService;

    public WebPayController(PagoFactoryService pagoFactoryService) {
        this.pagoFactoryService = pagoFactoryService;
    }

    @GetMapping("/webpay/commit")
    public RedirectView responderWebPay(
            @RequestParam(value = "token_ws", required = false) Optional<String> tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) Optional<String> tbkToken,
            HttpServletRequest request) {

        Logger.logInfo("================================================================================");
        Logger.logInfo("🌐 [WEBPAY-CONTROLLER] Iniciando confirmación de retorno desde Transbank / WebPay");
        Logger.logInfo("================================================================================");

        // El usuario canceló la compra en la pasarela
        if (tbkToken.isPresent()) {
            Logger.logWarn("⚠️ El usuario canceló la compra en la pasarela WebPay (TBK_TOKEN presente)");
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

        if (tokenWs.isPresent() && !tokenWs.get().isBlank()) {

            String token = tokenWs.get();
            Logger.logInfo("🔑 Token de WebPay recibido: " + token);
            boolean pagoAutorizado = webPayService.confirmarPagoWebPay(token);

            if (!pagoAutorizado) {
                Logger.logError("❌ WebPay no autorizó la transacción para el token: " + token);
                return new RedirectView("/home/errorPago.xhtml?reason=REJECTED");
            }

            Logger.logInfo("✅ [WEBPAY] Pago APROBADO en Transbank");

            HttpSession session = request.getSession(false);

            // 1. Verificar si corresponde a un Ascenso de Tarifa (Upgrade)
            SolicitudUpgradeDTO upgradeDto = (session != null)
                    ? (SolicitudUpgradeDTO) session.getAttribute("SOLICITUD_UPGRADE_PENDIENTE")
                    : null;

            if (upgradeDto != null) {
                Logger.logInfo("🎯 [WEBPAY] Confirmando Ascenso de Tarifa para Reserva ID: " + upgradeDto.getIdReserva());
                int idMetodoPagoWebPay = 1;
                ResultadoUpgradeDTO resultadoUpgrade = upgradeTarifaService.procesarYConfirmarUpgrade(
                        upgradeDto.getIdReserva(),
                        upgradeDto.getIdItinerario(),
                        upgradeDto.getIdItinerarioTarifaNuevo(),
                        idMetodoPagoWebPay,
                        "Transbank WebPay Plus"
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

            // 2. Verificar si corresponde a un Cambio de Asiento con Pago
            SolicitudCambioAsientoDTO cambioDto = (session != null)
                    ? (SolicitudCambioAsientoDTO) session.getAttribute("SOLICITUD_CAMBIO_ASIENTO_PENDIENTE")
                    : null;

            if (cambioDto != null) {
                Logger.logInfo("🎯 [WEBPAY] Confirmando Cambio de Asientos para Reserva ID: " + cambioDto.getIdReserva());
                int idMetodoPagoWebPay = 1;
                com.SkyWay.modules.reservaasiento.presentation.dto.ResultadoCambioAsientoDTO resultado = cambioAsientoProcesadorService.procesarYConfirmarCambioAsiento(cambioDto, idMetodoPagoWebPay, "Transbank WebPay Plus", request);
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

            // 3. Flujo regular de nueva reserva
            SolicitudReservaDTO dto = (session != null) ? (SolicitudReservaDTO) session.getAttribute("SOLICITUD_RESERVA_PENDIENTE") : null;

            if (dto == null) {
                Logger.logError("❌ No se encontraron los datos DTO de la reserva en la sesión.");
                return new RedirectView("/home/errorPago.xhtml?reason=EXPIRED_SESSION");
            }

            int idMetodoPagoWebPay = 1;
            Long idWal = null;
            try {
                idWal = reservaWalService.registrarInicioPago(dto, idMetodoPagoWebPay, token, request.getRemoteAddr(), request.getHeader("User-Agent"));
            } catch (Exception exWal) {
                Logger.logWarn("⚠️ Error registrando WAL preliminar: " + exWal.getMessage());
            }

            try {
                Logger.logInfo("💾 Persistiendo reserva con ID Método de Pago WEBPAY (" + idMetodoPagoWebPay + ")...");

                Integer idReserva = procesarReservaUseCase.ejecutarPersistenciaReserva(dto, idMetodoPagoWebPay);

                if (idWal != null) {
                    reservaWalService.marcarWalCompletado(idWal, idReserva);
                }

                session = request.getSession(false);
                if (session != null) {
                    session.setAttribute("ULTIMO_ID_RESERVA", idReserva);
                    session.removeAttribute("SOLICITUD_RESERVA_PENDIENTE");
                }

                Logger.logInfo("🎉 [WEBPAY-CONTROLLER] Reserva ID: " + idReserva + " procesada con éxito. Redirigiendo a pago-exitoso.xhtml");
                return new RedirectView("/home/pago-exitoso.xhtml");
            } catch (Exception e) {
                Logger.logError("💥 Error al escribir la reserva tras pago aprobado: " + e.getMessage());
                if (idWal != null) {
                    reservaWalService.marcarWalFallido(idWal, e.getMessage(), e);
                }
                session = request.getSession(false);
                if (session != null) {
                    session.setAttribute("WAL_FALLO_ID", idWal);
                    session.setAttribute("WAL_FALLO_MOTIVO", e.getMessage());
                    session.setAttribute("WAL_FALLO_MONTO", dto.montoTotal());
                }
                return new RedirectView("/home/errorPago.xhtml?reason=PERSISTENCE_ERROR" + (idWal != null ? "&walId=" + idWal : ""));
            }
        }

        Logger.logError("❌ Token de WebPay inválido o ausente en la petición");
        return new RedirectView("/home/errorPago.xhtml?reason=INVALID_TOKEN");
    }
}