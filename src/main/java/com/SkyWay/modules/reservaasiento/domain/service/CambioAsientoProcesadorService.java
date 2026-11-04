package com.SkyWay.modules.reservaasiento.domain.service;

import com.SkyWay.modules.auditoria.application.LogAuditoriaService;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.presentation.dto.ResultadoCambioAsientoDTO;
import com.SkyWay.modules.reservaasiento.presentation.dto.SolicitudCambioAsientoDTO;
import com.SkyWay.util.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class CambioAsientoProcesadorService {

    @Autowired
    private ReservaAsientoService reservaAsientoService;

    @Autowired
    private AsientoCacheService asientoCacheService;

    @Autowired
    private LogAuditoriaService logAuditoriaService;

    @PersistenceContext
    private EntityManager em;

    /**
     * Ejecuta la operación transaccional atómica de cambio de asientos con confirmación de pago en pasarela.
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultadoCambioAsientoDTO procesarYConfirmarCambioAsiento(SolicitudCambioAsientoDTO dto, Integer idMetodoPago, String pasarelaNombre, HttpServletRequest request) {
        if (dto == null || dto.getIdReserva() == null || dto.getItems() == null || dto.getItems().isEmpty()) {
            Logger.logError("❌ Parámetros insuficientes en SolicitudCambioAsientoDTO para procesar confirmación.");
            return null;
        }

        try {
            List<String> resumenLog = new ArrayList<>();
            ResultadoCambioAsientoDTO resultado = new ResultadoCambioAsientoDTO();
            resultado.setIdReserva(dto.getIdReserva());
            resultado.setRutTitular(dto.getRutUsuario());
            resultado.setMontoTotal(dto.getGranTotalAdicional());
            resultado.setMetodoPago(pasarelaNombre != null ? pasarelaNombre : "Pasarela");
            resultado.setOrigen(dto.getOrigen() != null ? dto.getOrigen() : "RESERVA");
            resultado.setFechaHora(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));

            // 1. Ejecutar procedimiento atómico sp_cambiarAsiento para cada asiento solicitado
            for (SolicitudCambioAsientoDTO.ItemCambioAsientoDTO item : dto.getItems()) {
                boolean exito = reservaAsientoService.cambiarAsiento(item.getIdAsientoNuevo(), dto.getIdReserva(), item.getIdAsientoOriginal());
                if (!exito) {
                    throw new IllegalStateException("Error al reasignar asiento en base de datos: original " 
                            + item.getIdAsientoOriginal() + " -> nuevo " + item.getIdAsientoNuevo());
                }

                // Sincronizar cache de memoria y WebSockets
                if (item.getIdVuelo() != null) {
                    asientoCacheService.actualizarCambioAsiento(item.getIdVuelo(), item.getIdAsientoOriginal(), item.getIdAsientoNuevo());
                }

                // Armar detalle de feedback para el pasajero
                ResultadoCambioAsientoDTO.DetalleResultadoCambioDTO det = new ResultadoCambioAsientoDTO.DetalleResultadoCambioDTO();
                det.setNumeroVuelo(item.getNumeroVuelo());
                det.setTramoVuelo(item.getTramoVuelo());
                det.setNombrePasajero(item.getNombrePasajero());
                det.setRutPasajero(item.getRutPasajero());
                det.setAsientoOriginal(item.getNumeroAsientoOriginal());
                det.setClaseOriginal(item.getClaseOriginal());
                det.setAsientoNuevo(item.getNumeroAsientoNuevo());
                det.setClaseNueva(item.getClaseNueva());
                det.setCostoAdicional(item.getMontoAdicional());
                resultado.addItem(det);

                resumenLog.add(String.format("Vuelo %s (%s) | %s: Asiento %s (%s) -> %s (%s)",
                        item.getNumeroVuelo(), item.getTramoVuelo(), item.getNombrePasajero(),
                        item.getNumeroAsientoOriginal(), item.getClaseOriginal(),
                        item.getNumeroAsientoNuevo(), item.getClaseNueva()));
            }

            // 2. Obtener reserva para PNR y registrar pago si aplica
            Reserva reserva = em.find(Reserva.class, dto.getIdReserva());
            if (reserva != null) {
                resultado.setCodigoPnr(reserva.getCodigoReserva() != null ? reserva.getCodigoReserva().toUpperCase() : "RES-" + dto.getIdReserva());

                if (dto.getGranTotalAdicional() > 0) {
                    Pago pago = new Pago();
                    pago.setFecha(new Timestamp(System.currentTimeMillis()));
                    pago.setMontoPagado(BigDecimal.valueOf(dto.getGranTotalAdicional()));
                    pago.setReserva(reserva);

                    int metodoId = (idMetodoPago != null && idMetodoPago > 0) ? idMetodoPago : 1;
                    MetodoPago mp = em.find(MetodoPago.class, metodoId);
                    pago.setMetodoPago(mp);

                    em.persist(pago);
                    Logger.logInfo("💳 [PAGO-CAMBIO-ASIENTO] Registrado pago de $" + dto.getGranTotalAdicional() + " CLP con método ID: " + metodoId + " para Reserva #" + dto.getIdReserva());
                }
            } else {
                resultado.setCodigoPnr("RES-" + dto.getIdReserva());
            }

            // 3. Auditoría transversal corporativa
            logAuditoriaService.registrarLog(
                    dto.getRutUsuario() != null ? dto.getRutUsuario() : "SISTEMA",
                    "CAMBIO_ASIENTO_TRANSACCIONAL",
                    "reserva_asiento",
                    dto.getIdReserva(),
                    String.format("Reasignación atómica completada vía %s | Modificaciones: [%s] | Total abonado: $%,d CLP",
                            pasarelaNombre != null ? pasarelaNombre : "Pasarela",
                            String.join("; ", resumenLog),
                            dto.getGranTotalAdicional()),
                    request
            );

            Logger.logInfo("🎉 [CAMBIO-ASIENTO-EXITOSO] Cambio multi-tramo procesado atómicamente para reserva #" + dto.getIdReserva());
            return resultado;

        } catch (Exception e) {
            Logger.logError("💥 Error transaccional en procesarYConfirmarCambioAsiento: " + e.getMessage());
            throw new RuntimeException("Error al procesar atómicamente el cambio de asiento: " + e.getMessage(), e);
        }
    }

    /**
     * Ejecuta el cambio directo atómico en caso de no existir cargo adicional ($0 CLP, misma cabina).
     */
    @Transactional(rollbackFor = Exception.class)
    public ResultadoCambioAsientoDTO procesarCambioAsientoDirecto(SolicitudCambioAsientoDTO dto, HttpServletRequest request) {
        return procesarYConfirmarCambioAsiento(dto, 1, "Sin Costo (Misma Cabina)", request);
    }
}
