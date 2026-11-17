package com.SkyWay.modules.reembolso.domain.service;

import com.SkyWay.modules.reembolso.domain.model.CuentaReembolso;
import com.SkyWay.modules.reembolso.domain.model.Reembolso;
import com.SkyWay.modules.reembolso.presentation.dto.PayoutResponseDTO;

/**
 * Puerto de salida para ejecución de transferencias bancarias / reembolsos (Sandbox o Banco/API real).
 */
public interface PasarelaReembolsoGateway {

    /**
     * Ejecuta una transferencia hacia la cuenta del pasajero con clave de idempotencia.
     *
     * @param reembolso        Entidad transaccional del reembolso confirmado
     * @param cuenta           Cuenta bancaria verificada del destinatario
     * @param idempotencyKey   Clave única anti-duplicidad para la orden bancaria
     * @param escenarioSandbox Código de escenario de prueba ('APROBADO', 'RECHAZO_CUENTA', 'TIMEOUT_BANCO')
     * @return Respuesta normalizada del proveedor financiero
     */
    PayoutResponseDTO ejecutarTransferencia(
            Reembolso reembolso,
            CuentaReembolso cuenta,
            String idempotencyKey,
            String escenarioSandbox
    );
}
