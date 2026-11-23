package com.SkyWay.modules.reembolso.application.serviceImpl;

import com.SkyWay.modules.reembolso.domain.model.CuentaReembolso;
import com.SkyWay.modules.reembolso.domain.model.Reembolso;
import com.SkyWay.modules.reembolso.domain.repository.CuentaReembolsoRepository;
import com.SkyWay.modules.reembolso.domain.repository.ReembolsoRepository;
import com.SkyWay.modules.reembolso.domain.service.PasarelaReembolsoGateway;
import com.SkyWay.modules.reembolso.domain.service.ReembolsoService;
import com.SkyWay.modules.reembolso.presentation.dto.CuentaBancariaSolicitudDTO;
import com.SkyWay.modules.reembolso.presentation.dto.PayoutResponseDTO;
import com.SkyWay.modules.reserva.presentation.dto.ReembolsoPasajeroDTO;
import com.SkyWay.util.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReembolsoServiceImpl implements ReembolsoService {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ReembolsoRepository reembolsoRepository;

    @Autowired
    private CuentaReembolsoRepository cuentaReembolsoRepository;

    @Autowired
    private PasarelaReembolsoGateway pasarelaReembolsoGateway;

    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<CuentaReembolso> listarCuentasPorPasajero(String rutPasajero) {
        if (rutPasajero == null || rutPasajero.isBlank()) {
            return Collections.emptyList();
        }
        return cuentaReembolsoRepository.findByRutPasajeroIgnoreCaseAndActivoTrueOrderByFechaRegistroDesc(rutPasajero.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReembolsoPasajeroDTO> listarReembolsosPorPasajero(String rutPasajero) {
        if (rutPasajero == null || rutPasajero.isBlank()) {
            return Collections.emptyList();
        }
        return consultarReembolsos(rutPasajero.trim().toUpperCase(), false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReembolsoPasajeroDTO> listarTodosReembolsosAdmin() {
        return consultarReembolsos(null, true);
    }

    @Override
    @Transactional
    public void registrarDatosBancariosReembolso(Integer idReembolso, String rutPasajero, CuentaBancariaSolicitudDTO datosCuenta) {
        if (idReembolso == null) {
            throw new IllegalArgumentException("Debe seleccionar un reembolso válido.");
        }
        if (rutPasajero == null || rutPasajero.isBlank()) {
            throw new SecurityException("Sesión de pasajero inválida.");
        }
        if (datosCuenta == null) {
            throw new IllegalArgumentException("Debe ingresar los datos de la cuenta bancaria.");
        }

        Reembolso reembolso = reembolsoRepository.findById(idReembolso)
                .orElseThrow(() -> new IllegalArgumentException("Reembolso #" + idReembolso + " no encontrado."));
        validarElegibilidadCancelacionAerolinea(reembolso.getIdCancelacion());

        if (!usuarioPerteneceAReserva(rutPasajero.trim(), reembolso.getIdReserva())) {
            throw new SecurityException("No tiene autorización para registrar datos bancarios en este reembolso.");
        }

        String estActual = reembolso.getEstado() != null ? reembolso.getEstado().toUpperCase() : "SOLICITADO";
        if ("DATOS_CONFIRMADOS".equals(estActual) || "REEMBOLSADO".equals(estActual)) {
            throw new IllegalStateException("Este reembolso ya fue confirmado o ejecutado y no permite cambiar la cuenta bancaria.");
        }

        CuentaReembolso cuenta;
        if (datosCuenta.getIdCuentaExistente() != null && datosCuenta.getIdCuentaExistente() > 0) {
            cuenta = cuentaReembolsoRepository.findById(datosCuenta.getIdCuentaExistente())
                    .orElseThrow(() -> new IllegalArgumentException("La cuenta bancaria seleccionada no existe."));
            if (!rutPasajero.trim().equalsIgnoreCase(cuenta.getRutPasajero())) {
                throw new SecurityException("La cuenta bancaria seleccionada no pertenece a su perfil.");
            }
        } else {
            validarCamposCuenta(datosCuenta);
            cuenta = new CuentaReembolso();
            cuenta.setRutPasajero(rutPasajero.trim().toUpperCase());
            cuenta.setBanco(datosCuenta.getBanco().trim());
            cuenta.setTipoCuenta(datosCuenta.getTipoCuenta().trim());
            cuenta.setNumeroCuenta(datosCuenta.getNumeroCuenta().trim());
            cuenta.setTitularNombre(datosCuenta.getTitularNombre().trim());
            cuenta.setTitularRut(datosCuenta.getTitularRut().trim().toUpperCase());
            cuenta.setCorreoConfirmacion(datosCuenta.getCorreoConfirmacion() != null ? datosCuenta.getCorreoConfirmacion().trim() : null);
            cuenta.setActivo(true);
            cuenta.setFechaRegistro(new Timestamp(System.currentTimeMillis()));
            cuenta = cuentaReembolsoRepository.saveAndFlush(cuenta);
        }

        String estadoAnterior = reembolso.getEstado();
        reembolso.setCuentaReembolso(cuenta);
        reembolso.setEstado("DATOS_BANCARIOS_ENVIADOS");
        reembolso.setFechaDatosBancarios(new Timestamp(System.currentTimeMillis()));
        if ("RECHAZADO".equals(estActual)) {
            reembolso.setObservacionAdmin(null);
        }
        reembolsoRepository.saveAndFlush(reembolso);

        registrarAuditoria(
                rutPasajero.trim(),
                "REEMBOLSO_DATOS_BANCARIOS_ENVIADOS",
                "INFO",
                idReembolso,
                Map.of("estado", String.valueOf(estadoAnterior)),
                Map.of("estado", "DATOS_BANCARIOS_ENVIADOS", "banco", cuenta.getBanco(), "tipo_cuenta", cuenta.getTipoCuenta(), "id_cuenta", cuenta.getIdCuentaReembolso()),
                "Pasajero registró cuenta bancaria " + cuenta.getBanco() + " (" + cuenta.getTipoCuenta() + ") para Reembolso #" + idReembolso
        );
    }

    @Override
    @Transactional
    public void confirmarDatosBancariosAdmin(Integer idReembolso, String rutAdmin, String observacion) {
        Reembolso reembolso = reembolsoRepository.findById(idReembolso)
                .orElseThrow(() -> new IllegalArgumentException("Reembolso #" + idReembolso + " no encontrado."));
        validarElegibilidadCancelacionAerolinea(reembolso.getIdCancelacion());

        if (!"DATOS_BANCARIOS_ENVIADOS".equalsIgnoreCase(reembolso.getEstado())) {
            throw new IllegalStateException("Solo se pueden confirmar datos bancarios cuando el reembolso está en estado DATOS_BANCARIOS_ENVIADOS.");
        }
        if (reembolso.getCuentaReembolso() == null) {
            throw new IllegalStateException("El reembolso no tiene una cuenta bancaria asociada.");
        }

        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        reembolso.setEstado("DATOS_CONFIRMADOS");
        reembolso.setFechaConfirmacionAdmin(ahora);
        reembolso.setRutAdminConfirma(normalizarRutUsuarioValido(rutAdmin));
        reembolso.setObservacionAdmin(observacion != null && !observacion.isBlank() ? observacion.trim() : "Datos bancarios verificados y aprobados.");
        reembolsoRepository.saveAndFlush(reembolso);

        em.createNativeQuery("UPDATE cancelacion SET estado = 'Procesado' WHERE id_cancelacion = :idCanc")
                .setParameter("idCanc", reembolso.getIdCancelacion())
                .executeUpdate();

        CuentaReembolso c = reembolso.getCuentaReembolso();
        notificarPasajero(
                reembolso.getRutSolicitante(),
                "Reembolso #" + idReembolso + ": Cuenta Validada",
                "Estimado/a pasajero/a,<br/><br/>Los datos de su cuenta bancaria (<strong>" + c.getBanco() + " · " + c.getTipoCuenta()
                        + " N° " + enmascararCuenta(c.getNumeroCuenta()) + "</strong>) han sido <strong>confirmados</strong> por nuestro equipo.<br/>"
                        + "Monto aprobado: <strong>$" + reembolso.getMonto() + " CLP</strong>. El pago se encuentra listo para ejecución."
        );

        registrarAuditoria(
                rutAdmin,
                "REEMBOLSO_DATOS_CONFIRMADOS",
                "INFO",
                idReembolso,
                Map.of("estado", "DATOS_BANCARIOS_ENVIADOS"),
                Map.of("estado", "DATOS_CONFIRMADOS", "id_cuenta", c.getIdCuentaReembolso()),
                "Ejecutivo confirmó datos bancarios de Reembolso #" + idReembolso
        );
    }

    @Override
    @Transactional
    public void rechazarDatosBancariosAdmin(Integer idReembolso, String rutAdmin, String motivoRechazo) {
        if (motivoRechazo == null || motivoRechazo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el motivo por el cual se observan/rechazan los datos bancarios.");
        }
        Reembolso reembolso = reembolsoRepository.findById(idReembolso)
                .orElseThrow(() -> new IllegalArgumentException("Reembolso #" + idReembolso + " no encontrado."));
        validarElegibilidadCancelacionAerolinea(reembolso.getIdCancelacion());

        String est = reembolso.getEstado() != null ? reembolso.getEstado().toUpperCase() : "";
        if (!"DATOS_BANCARIOS_ENVIADOS".equals(est) && !"DATOS_CONFIRMADOS".equals(est)) {
            throw new IllegalStateException("No es posible observar los datos bancarios en el estado actual (" + est + ").");
        }

        reembolso.setEstado("RECHAZADO");
        reembolso.setObservacionAdmin(motivoRechazo.trim());
        reembolso.setRutAdminConfirma(normalizarRutUsuarioValido(rutAdmin));
        reembolsoRepository.saveAndFlush(reembolso);

        notificarPasajero(
                reembolso.getRutSolicitante(),
                "Reembolso #" + idReembolso + ": Corregir Cuenta Bancaria",
                "Estimado/a pasajero/a,<br/><br/>Su solicitud de reembolso #<strong>" + idReembolso
                        + "</strong> requiere corregir los datos bancarios ingresados.<br/>"
                        + "<strong>Observación:</strong> " + motivoRechazo.trim() + "<br/>"
                        + "Por favor ingrese a su perfil en SkyWay > pestaña <strong>Reembolsos</strong> para actualizar su cuenta."
        );

        registrarAuditoria(
                rutAdmin,
                "REEMBOLSO_DATOS_OBSERVADOS",
                "WARN",
                idReembolso,
                Map.of("estado", est),
                Map.of("estado", "RECHAZADO", "observacion", motivoRechazo.trim()),
                "Ejecutivo observó datos bancarios de Reembolso #" + idReembolso + ": " + motivoRechazo.trim()
        );
    }

    @Override
    @Transactional
    public PayoutResponseDTO ejecutarReembolsoSimuladoAdmin(Integer idReembolso, String rutAdmin, String observacion, String escenarioSandbox) {
        Reembolso reembolso = reembolsoRepository.findById(idReembolso)
                .orElseThrow(() -> new IllegalArgumentException("Reembolso #" + idReembolso + " no encontrado."));
        validarElegibilidadCancelacionAerolinea(reembolso.getIdCancelacion());

        if (!"DATOS_CONFIRMADOS".equalsIgnoreCase(reembolso.getEstado())) {
            throw new IllegalStateException("Solo se puede ejecutar el reembolso cuando los datos bancarios estén en estado DATOS_CONFIRMADOS.");
        }
        if (reembolso.getCodigoOperacion() != null && !reembolso.getCodigoOperacion().isBlank()) {
            throw new IllegalStateException("Este reembolso ya posee un comprobante de transferencia emitido (" + reembolso.getCodigoOperacion() + ").");
        }
        if (reembolso.getCuentaReembolso() == null) {
            throw new IllegalStateException("El reembolso no posee cuenta bancaria confirmada.");
        }

        CuentaReembolso c = reembolso.getCuentaReembolso();
        String idempotencyKey = "SKY-REEM-" + idReembolso + "-RES-" + reembolso.getIdReserva();
        PayoutResponseDTO resp = pasarelaReembolsoGateway.ejecutarTransferencia(reembolso, c, idempotencyKey, escenarioSandbox);

        if (!resp.isAprobado()) {
            String detalleFallo = String.format("[%s | RC-%s | %dms] %s",
                    resp.getCanalCompensacion(), resp.getCodigoRespuestaBanco(), resp.getLatenciaMs(), resp.getMensajeBanco());

            if ("14".equals(resp.getCodigoRespuestaBanco())) {
                // Rechazo definitivo de cuenta destino: se devuelve a RECHAZADO para que el pasajero corrija su cuenta
                reembolso.setEstado("RECHAZADO");
                reembolso.setObservacionAdmin(detalleFallo);
                reembolsoRepository.saveAndFlush(reembolso);

                notificarPasajero(
                        reembolso.getRutSolicitante(),
                        "Reembolso #" + idReembolso + ": Transferencia Rechazada por Banco",
                        "Estimado/a pasajero/a,<br/><br/>Al intentar realizar el abono a su cuenta bancaria ("
                                + c.getBanco() + " N° " + enmascararCuenta(c.getNumeroCuenta())
                                + "), el banco respondió con rechazo:<br/><strong>" + resp.getMensajeBanco()
                                + "</strong><br/>Por favor ingrese a su perfil > pestaña <strong>Reembolsos</strong> para corregir los datos."
                );

                registrarAuditoria(
                        rutAdmin,
                        "REEMBOLSO_RECHAZO_BANCARIO",
                        "WARN",
                        idReembolso,
                        Map.of("estado", "DATOS_CONFIRMADOS"),
                        Map.of("estado", "RECHAZADO", "codigo_respuesta_banco", resp.getCodigoRespuestaBanco(), "idempotency_key", idempotencyKey),
                        "Banco rechazó TEF Reembolso #" + idReembolso + ": " + detalleFallo
                );
            } else {
                // Timeout u otro fallo transitorio (ej. RC-91): mantiene DATOS_CONFIRMADOS para reintento idempotente
                reembolso.setObservacionAdmin(detalleFallo);
                reembolsoRepository.saveAndFlush(reembolso);

                registrarAuditoria(
                        rutAdmin,
                        "REEMBOLSO_TIMEOUT_BANCARIO",
                        "WARN",
                        idReembolso,
                        Map.of("estado", "DATOS_CONFIRMADOS"),
                        Map.of("estado", "DATOS_CONFIRMADOS", "codigo_respuesta_banco", resp.getCodigoRespuestaBanco(), "idempotency_key", idempotencyKey),
                        "Timeout/error transitorio en TEF Reembolso #" + idReembolso + ": " + detalleFallo
                );
            }
            return resp;
        }

        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        String codigoOp = resp.getCodigoOperacion();
        String notaBanco = String.format("[%s | SBIF %s | RC-%s | %dms | Key: %s] %s",
                resp.getCanalCompensacion(), resp.getCodigoBancoSbif(), resp.getCodigoRespuestaBanco(),
                resp.getLatenciaMs(), resp.getIdempotencyKey(),
                (observacion != null && !observacion.isBlank()) ? observacion.trim() : "Abono confirmado.");

        reembolso.setEstado("REEMBOLSADO");
        reembolso.setFechaEjecucion(ahora);
        reembolso.setRutAdminEjecuta(normalizarRutUsuarioValido(rutAdmin));
        reembolso.setCodigoOperacion(codigoOp);
        reembolso.setObservacionAdmin(notaBanco);
        reembolsoRepository.saveAndFlush(reembolso);

        em.createNativeQuery("UPDATE cancelacion SET estado = 'Reembolsado' WHERE id_cancelacion = :idCanc")
                .setParameter("idCanc", reembolso.getIdCancelacion())
                .executeUpdate();

        notificarPasajero(
                reembolso.getRutSolicitante(),
                "Reembolso Acreditado #" + idReembolso + " (" + codigoOp + ")",
                "Estimado/a pasajero/a,<br/><br/>Hemos ejecutado la transferencia electrónica de su reembolso:<br/>"
                        + "<ul style=\"margin-top:8px; padding-left:20px; line-height:1.6;\">"
                        + "<li>N° Comprobante TEF: <strong>" + codigoOp + "</strong></li>"
                        + "<li>Respuesta Bancaria: <strong>" + resp.getMensajeBanco() + "</strong></li>"
                        + "<li>Monto transferido: <strong>$" + reembolso.getMonto() + " CLP</strong></li>"
                        + "<li>Banco destino: <strong>" + c.getBanco() + " (SBIF " + resp.getCodigoBancoSbif() + ") · " + c.getTipoCuenta() + " N° " + enmascararCuenta(c.getNumeroCuenta()) + "</strong></li>"
                        + "<li>Titular: <strong>" + c.getTitularNombre() + " (" + c.getTitularRut() + ")</strong></li>"
                        + "</ul>"
        );

        registrarAuditoria(
                rutAdmin,
                "REEMBOLSO_EJECUTADO_SIMULADO",
                "INFO",
                idReembolso,
                Map.of("estado", "DATOS_CONFIRMADOS"),
                Map.of(
                        "estado", "REEMBOLSADO",
                        "codigo_operacion", codigoOp,
                        "codigo_banco_sbif", resp.getCodigoBancoSbif(),
                        "codigo_respuesta_banco", resp.getCodigoRespuestaBanco(),
                        "idempotency_key", idempotencyKey,
                        "latencia_ms", resp.getLatenciaMs(),
                        "monto", reembolso.getMonto()
                ),
                "TEF ejecutada Reembolso #" + idReembolso + " | Comprobante: " + codigoOp + " | Monto: $" + reembolso.getMonto()
        );

        return resp;
    }

    private List<ReembolsoPasajeroDTO> consultarReembolsos(String rutFiltro, boolean esAdmin) {
        List<ReembolsoPasajeroDTO> lista = new ArrayList<>();
        try {
            String whereClause = (esAdmin
                    ? "WHERE 1=1 "
                    : "WHERE (UPPER(COALESCE(r.rut_pasajero, '')) = :rut "
                    + "   OR UPPER(COALESCE(rem.rut_solicitante, '')) = :rut "
                    + "   OR EXISTS (SELECT 1 FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva AND UPPER(pr.rut) = :rut) "
                    + "   OR EXISTS (SELECT 1 FROM cancelacion_asiento ca2 WHERE ca2.id_cancelacion = c.id_cancelacion AND UPPER(ca2.rut_pasajero) = :rut)) ")
                    + "AND UPPER(COALESCE(c.estado, '')) <> 'SIN REEMBOLSO' "
                    + "AND COALESCE(c.monto_reembolso, 0) > 0 "
                    + "AND (LOWER(COALESCE(c.motivo, '')) LIKE '%cancelación operacional%' "
                    + "     OR LOWER(COALESCE(c.motivo, '')) LIKE '%cancelacion operacional%') ";

            String sql = "SELECT DISTINCT " +
                    "   rem.id_reembolso, " +
                    "   c.id_cancelacion, " +
                    "   r.id_reserva, " +
                    "   COALESCE(r.codigo_reserva, 'RES-' || r.id_reserva) AS codigo_pnr, " +
                    "   COALESCE(rem.fecha_solicitud, c.fecha) AS fecha_sol, " +
                    "   COALESCE(c.motivo, 'Cancelación registrada') AS motivo, " +
                    "   COALESCE(rem.estado, 'SOLICITADO') AS estado_flujo, " +
                    "   COALESCE(rem.monto, c.monto_reembolso, r.total, 0) AS monto, " +
                    "   COALESCE((" +
                    "       SELECT mp.descripcion FROM pago pg " +
                    "       JOIN metodo_pago mp ON mp.id_metodo_pago = pg.id_metodo_pago " +
                    "       WHERE pg.id_reserva = r.id_reserva " +
                    "       ORDER BY pg.fecha DESC LIMIT 1" +
                    "   ), 'Medio de pago original') AS metodo_pago, " +
                    "   COALESCE((" +
                    "       SELECT STRING_AGG(DISTINCT COALESCE(NULLIF(ca.numero_vuelo, ''), 'Vuelo #' || ca.id_vuelo) || ' · Asiento ' || COALESCE(ca.numero_asiento, '-') || COALESCE(' (' || ca.clase_asiento || ')', ''), ' | ') " +
                    "       FROM cancelacion_asiento ca " +
                    "       WHERE ca.id_cancelacion = c.id_cancelacion" +
                    "   ), 'Sin desglose de asientos registrado') AS detalle_asientos, " +
                    (esAdmin ? " TRUE AS es_titular, " : " (UPPER(COALESCE(r.rut_pasajero, '')) = :rut) AS es_titular, ") +
                    "   COALESCE(rem.rut_solicitante, r.rut_pasajero) AS rut_sol, " +
                    "   COALESCE(u_sol.nombre || ' ' || u_sol.apellido, r.rut_pasajero) AS nombre_sol, " +
                    "   cr.id_cuenta_reembolso, " +
                    "   cr.banco, " +
                    "   cr.tipo_cuenta, " +
                    "   cr.numero_cuenta, " +
                    "   cr.titular_nombre, " +
                    "   cr.titular_rut, " +
                    "   cr.correo_confirmacion, " +
                    "   rem.fecha_datos_bancarios, " +
                    "   rem.fecha_confirmacion_admin, " +
                    "   rem.rut_admin_confirma, " +
                    "   rem.fecha_ejecucion, " +
                    "   rem.rut_admin_ejecuta, " +
                    "   rem.codigo_operacion, " +
                    "   rem.observacion_admin " +
                    "FROM reembolso rem " +
                    "JOIN cancelacion c ON c.id_cancelacion = rem.id_cancelacion " +
                    "JOIN reserva r ON r.id_reserva = rem.id_reserva " +
                    "LEFT JOIN usuario u_sol ON u_sol.rut = COALESCE(rem.rut_solicitante, r.rut_pasajero) " +
                    "LEFT JOIN cuenta_reembolso cr ON cr.id_cuenta_reembolso = rem.id_cuenta_reembolso " +
                    whereClause +
                    "ORDER BY COALESCE(rem.fecha_solicitud, c.fecha) DESC, rem.id_reembolso DESC";

            var query = em.createNativeQuery(sql);
            if (!esAdmin) {
                query.setParameter("rut", rutFiltro);
            }

            @SuppressWarnings("unchecked")
            List<Object[]> filas = query.getResultList();
            for (Object[] row : filas) {
                ReembolsoPasajeroDTO dto = new ReembolsoPasajeroDTO();
                dto.setIdReembolso(row[0] != null ? ((Number) row[0]).intValue() : null);
                dto.setIdCancelacion(row[1] != null ? ((Number) row[1]).intValue() : null);
                dto.setIdReserva(row[2] != null ? ((Number) row[2]).intValue() : null);
                dto.setCodigoPnr(row[3] != null ? row[3].toString() : "");
                dto.setFechaCancelacion(toDate(row[4]));
                String motivoStr = row[5] != null ? row[5].toString() : "";
                dto.setMotivo(motivoStr);
                dto.setEstadoReembolso(row[6] != null ? row[6].toString() : "SOLICITADO");
                dto.setMontoReembolso(row[7] != null ? new BigDecimal(row[7].toString()) : BigDecimal.ZERO);
                dto.setMetodoPago(row[8] != null ? row[8].toString() : "Medio de pago original");
                dto.setResumenVuelosAsientos(row[9] != null ? row[9].toString() : "");
                dto.setTitular(Boolean.TRUE.equals(row[10]));
                dto.setCancelacionAerolinea(motivoStr.toLowerCase().contains("cancelación operacional"));
                dto.setRutSolicitante(row[11] != null ? row[11].toString() : null);
                dto.setNombreSolicitante(row[12] != null ? row[12].toString() : null);
                dto.setIdCuentaReembolso(row[13] != null ? ((Number) row[13]).intValue() : null);
                dto.setBanco(row[14] != null ? row[14].toString() : null);
                dto.setTipoCuenta(row[15] != null ? row[15].toString() : null);
                dto.setNumeroCuenta(row[16] != null ? row[16].toString() : null);
                dto.setTitularNombre(row[17] != null ? row[17].toString() : null);
                dto.setTitularRut(row[18] != null ? row[18].toString() : null);
                dto.setCorreoConfirmacion(row[19] != null ? row[19].toString() : null);
                dto.setFechaDatosBancarios(toDate(row[20]));
                dto.setFechaConfirmacionAdmin(toDate(row[21]));
                dto.setRutAdminConfirma(row[22] != null ? row[22].toString() : null);
                dto.setFechaEjecucion(toDate(row[23]));
                dto.setRutAdminEjecuta(row[24] != null ? row[24].toString() : null);
                dto.setCodigoOperacion(row[25] != null ? row[25].toString() : null);
                dto.setObservacionAdmin(row[26] != null ? row[26].toString() : null);
                lista.add(dto);
            }
        } catch (Exception e) {
            Logger.logError("Error consultando reembolsos transaccionales: " + e.getMessage());
        }
        return lista;
    }

    private void validarElegibilidadCancelacionAerolinea(Integer idCancelacion) {
        if (idCancelacion == null) {
            throw new IllegalStateException("El reembolso aplica únicamente ante cancelación operacional de vuelo por parte de la aerolínea.");
        }
        List<?> res = em.createNativeQuery(
                "SELECT motivo, estado, COALESCE(monto_reembolso, 0) FROM cancelacion WHERE id_cancelacion = :idCanc LIMIT 1"
        ).setParameter("idCanc", idCancelacion).getResultList();
        if (res.isEmpty()) {
            throw new IllegalStateException("Solo se consideran reembolsos cuando un vuelo es cancelado operacionalmente por parte de la aerolínea.");
        }
        Object[] row = (Object[]) res.get(0);
        String motivo = row[0] == null ? "" : row[0].toString().toLowerCase();
        String estado = row[1] == null ? "" : row[1].toString().toUpperCase();
        BigDecimal monto = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
        if ("SIN REEMBOLSO".equals(estado) || monto.compareTo(BigDecimal.ZERO) <= 0
                || (!motivo.contains("cancelación operacional") && !motivo.contains("cancelacion operacional"))) {
            throw new IllegalStateException("Solo se consideran reembolsos cuando un vuelo es cancelado operacionalmente por parte de la aerolínea.");
        }
    }

    private boolean usuarioPerteneceAReserva(String rut, Integer idReserva) {
        Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM reserva r " +
                "WHERE r.id_reserva = :idRes " +
                "AND (UPPER(COALESCE(r.rut_pasajero, '')) = :rut " +
                "     OR EXISTS (SELECT 1 FROM pasajero_reserva pr WHERE pr.id_reserva = r.id_reserva AND UPPER(pr.rut) = :rut))"
        )
                .setParameter("idRes", idReserva)
                .setParameter("rut", rut.toUpperCase())
                .getSingleResult();
        return count != null && count.intValue() > 0;
    }

    private void validarCamposCuenta(CuentaBancariaSolicitudDTO d) {
        if (d.getBanco() == null || d.getBanco().isBlank()) {
            throw new IllegalArgumentException("Debe indicar el banco.");
        }
        if (d.getTipoCuenta() == null || d.getTipoCuenta().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar el tipo de cuenta.");
        }
        if (d.getNumeroCuenta() == null || d.getNumeroCuenta().trim().length() < 4) {
            throw new IllegalArgumentException("Debe ingresar un número de cuenta válido.");
        }
        if (d.getTitularNombre() == null || d.getTitularNombre().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el nombre del titular de la cuenta.");
        }
        if (d.getTitularRut() == null || d.getTitularRut().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el RUT del titular de la cuenta.");
        }
    }

    private String normalizarRutUsuarioValido(String rut) {
        if (rut == null || rut.isBlank()) {
            return null;
        }
        List<?> res = em.createNativeQuery("SELECT rut FROM usuario WHERE rut = :rut LIMIT 1")
                .setParameter("rut", rut.trim())
                .getResultList();
        return res.isEmpty() ? null : String.valueOf(res.get(0));
    }

    private void notificarPasajero(String rutDestinatario, String titulo, String mensajeHtml) {
        if (rutDestinatario == null || rutDestinatario.isBlank()) {
            return;
        }
        String tituloFinal = titulo.length() > 100 ? titulo.substring(0, 100) : titulo;
        em.createNativeQuery(
                "INSERT INTO notificacion (rut_destinatario, titulo, mensaje, fecha, leido, enviada, canal) " +
                "VALUES (:rut, :titulo, :msg, NOW(), FALSE, FALSE, 'Email')"
        )
                .setParameter("rut", rutDestinatario.trim())
                .setParameter("titulo", tituloFinal)
                .setParameter("msg", mensajeHtml)
                .executeUpdate();
    }

    private void registrarAuditoria(String rutUsuario,
                                    String accion,
                                    String nivel,
                                    Integer idReembolso,
                                    Map<String, Object> ant,
                                    Map<String, Object> nue,
                                    String detalle) {
        try {
            String rutValido = normalizarRutUsuarioValido(rutUsuario);
            String jsonAnt = jsonMapper.writeValueAsString(ant);
            String jsonNue = jsonMapper.writeValueAsString(nue);
            em.createNativeQuery(
                    "INSERT INTO log_auditoria (rut_usuario, accion, nivel, tabla_afectada, id_registro_afectado, datos_anteriores, datos_nuevos, detalle) " +
                    "VALUES (:rut, :accion, CAST(:nivel AS tipo_nivel_log), 'reembolso', :idReg, CAST(:ant AS jsonb), CAST(:nue AS jsonb), :det)"
            )
                    .setParameter("rut", rutValido)
                    .setParameter("accion", accion)
                    .setParameter("nivel", nivel)
                    .setParameter("idReg", idReembolso)
                    .setParameter("ant", jsonAnt)
                    .setParameter("nue", jsonNue)
                    .setParameter("det", detalle)
                    .executeUpdate();
        } catch (Exception ex) {
            Logger.logWarn("No se pudo registrar auditoría de reembolso #" + idReembolso + ": " + ex.getMessage());
        }
    }

    private String enmascararCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.length() <= 4) {
            return numeroCuenta != null ? numeroCuenta : "";
        }
        return "****" + numeroCuenta.substring(numeroCuenta.length() - 4);
    }

    private Date toDate(Object val) {
        if (val instanceof Timestamp ts) return new Date(ts.getTime());
        if (val instanceof Date d) return d;
        return null;
    }
}
