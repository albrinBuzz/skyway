package com.SkyWay.config.pago;

import com.SkyWay.modules.pago.domain.model.PagoPasarela;
import com.SkyWay.modules.pago.domain.service.PagoPasarelaService;
import com.SkyWay.modules.reserva.domain.service.ReservaProcesadorService;
import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.util.Logger;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Logica unica del retorno de pago (WebPay, PayPal, MercadoPago):
 * PENDIENTE (sesion) -> CAPTURADO (BD, anti-replay) -> PAGADA (reserva creada) o REEMBOLSADO.
 */
@Service
public class PagoRetornoService {

    public static final String ATTR_PAGO_PENDIENTE = "PAGO_PENDIENTE";
    public static final String ATTR_SOLICITUD = "SOLICITUD_RESERVA_PENDIENTE";

    @Autowired private PagoFactoryService pagoFactoryService;
    @Autowired private PagoPasarelaService pagoPasarelaService;
    @Autowired private ReservaProcesadorService procesadorService;

    private static int idMetodoPago(MetodoPagoEnum m) {
        return switch (m) {
            case WEBPAY -> 1;
            case PAYPAL -> 2;
            case MERCADOPAGO -> 3; // requiere fila 'MercadoPago' en Metodo_Pago (ver dataBase/V3/03_datos)
        };
    }

    /** @return URL de redireccion para el usuario. */
    public String procesar(MetodoPagoEnum metodo, String idExterno, HttpSession session) {
        if (idExterno == null || idExterno.isBlank()) {
            return "/home/errorPago.xhtml?reason=INVALID_TOKEN";
        }
        if (session == null) {
            return "/home/errorPago.xhtml?reason=EXPIRED_SESSION";
        }

        PagoPendiente pendiente = (PagoPendiente) session.getAttribute(ATTR_PAGO_PENDIENTE);
        SolicitudReservaDTO dto = (SolicitudReservaDTO) session.getAttribute(ATTR_SOLICITUD);
        if (pendiente == null || dto == null) {
            Logger.logError("Retorno de pago sin pago/reserva pendiente en la sesion.");
            return "/home/errorPago.xhtml?reason=EXPIRED_SESSION";
        }
        if (pendiente.getProveedor() != metodo
                || dto.montoTotal() == null
                || dto.montoTotal().compareTo(pendiente.getMontoReserva()) != 0) {
            Logger.logError("Retorno de pago inconsistente con el pago pendiente.");
            return "/home/errorPago.xhtml?reason=REJECTED";
        }

        // Consumir el pendiente de inmediato: un segundo retorno concurrente en la misma sesion no pasa.
        synchronized (session.getId().intern()) {
            if (session.getAttribute(ATTR_PAGO_PENDIENTE) == null) {
                return "/home/errorPago.xhtml?reason=REJECTED";
            }
            session.removeAttribute(ATTR_PAGO_PENDIENTE);
        }

        // Anti-replay (pre-chequeo barato; la garantia real es el UNIQUE en BD).
        if (pagoPasarelaService.yaConsumido(metodo.name(), idExterno)) {
            Logger.logError("Pago ya consumido: " + metodo + " " + idExterno);
            return "/home/errorPago.xhtml?reason=REJECTED";
        }

        PasarelaPagoStrategy estrategia = pagoFactoryService.obtenerEstrategia(metodo);
        PagoConfirmado confirmado = estrategia.confirmarPago(idExterno, pendiente);

        if (!confirmado.isCapturado()) {
            return "/home/errorPago.xhtml?reason=REJECTED";
        }

        Integer idRegistro = pagoPasarelaService.registrarCaptura(metodo.name(), idExterno,
                pendiente.getOrdenCompra(), confirmado.getMonto(), confirmado.getMoneda());
        if (idRegistro == null) {
            // Replay: ese pago ya se uso. No se reembolsa (pertenece a otra reserva).
            Logger.logError("Replay de pago detectado: " + metodo + " " + idExterno);
            return "/home/errorPago.xhtml?reason=REJECTED";
        }

        if (!confirmado.isAprobado()) {
            Logger.logError("Pago capturado no coincide con lo esperado; se reembolsa.");
            reembolsar(estrategia, confirmado, idRegistro);
            return "/home/errorPago.xhtml?reason=REJECTED";
        }

        Integer idReserva;
        try {
            idReserva = procesadorService.ejecutarPersistenciaReserva(dto, idMetodoPago(metodo));
        } catch (Exception e) {
            Logger.logError("Pago capturado pero fallo la reserva (" + e.getMessage() + "); se reembolsa.");
            reembolsar(estrategia, confirmado, idRegistro);
            return "/home/errorPago.xhtml?reason=PERSISTENCE_ERROR";
        }

        try {
            pagoPasarelaService.cambiarEstado(idRegistro, PagoPasarela.PAGADA, idReserva);
        } catch (Exception e) {
            Logger.logError("Reserva " + idReserva + " creada pero no se pudo marcar PAGADA el registro " + idRegistro);
        }

        session.removeAttribute(ATTR_SOLICITUD);
        session.setAttribute("ULTIMO_ID_RESERVA", idReserva);
        return "/home/pago-exitoso.xhtml";
    }

    private void reembolsar(PasarelaPagoStrategy estrategia, PagoConfirmado confirmado, Integer idRegistro) {
        boolean ok = false;
        try {
            ok = estrategia.reembolsar(confirmado);
        } catch (Exception e) {
            Logger.logError("Excepcion al reembolsar: " + e.getMessage());
        }
        try {
            pagoPasarelaService.cambiarEstado(idRegistro,
                    ok ? PagoPasarela.REEMBOLSADO : PagoPasarela.REEMBOLSO_PENDIENTE, null);
        } catch (Exception e) {
            Logger.logError("No se pudo actualizar estado de reembolso del registro " + idRegistro);
        }
        if (!ok) {
            Logger.logError("REEMBOLSO PENDIENTE (accion manual) registro=" + idRegistro);
        }
    }
}
