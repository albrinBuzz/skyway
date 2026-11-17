package com.SkyWay.modules.reembolso.application.serviceImpl;

import com.SkyWay.modules.reembolso.domain.model.CuentaReembolso;
import com.SkyWay.modules.reembolso.domain.model.Reembolso;
import com.SkyWay.modules.reembolso.domain.service.PasarelaReembolsoGateway;
import com.SkyWay.modules.reembolso.presentation.dto.PayoutResponseDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulador realista de Transferencia Electrónica de Fondos (TEF / CCA Sandbox) para entorno académico.
 * Replica códigos SBIF chilenos, idempotencia, latencia de compensación y códigos de respuesta ISO.
 */
@Component
public class SimuladorBancarioTEFGateway implements PasarelaReembolsoGateway {

    @Override
    public PayoutResponseDTO ejecutarTransferencia(Reembolso reembolso,
                                                   CuentaReembolso cuenta,
                                                   String idempotencyKey,
                                                   String escenarioSandbox) {
        long inicio = System.currentTimeMillis();
        simularLatenciaRedBancaria();

        String codigoSbif = resolverCodigoSbif(cuenta.getBanco());
        String escenario = escenarioSandbox != null ? escenarioSandbox.trim().toUpperCase() : "APROBADO";

        PayoutResponseDTO resp = new PayoutResponseDTO();
        resp.setCodigoBancoSbif(codigoSbif);
        resp.setCanalCompensacion("TEF-CCA-SANDBOX");
        resp.setIdempotencyKey(idempotencyKey);
        resp.setMontoTransferido(reembolso.getMonto());
        resp.setFechaProceso(new Date());

        // Gatillo automático si el usuario prueba una cuenta terminada en 0000 o selecciona rechazo en sandbox
        boolean cuentaMarcadaError = cuenta.getNumeroCuenta() != null && cuenta.getNumeroCuenta().trim().endsWith("0000");

        if ("TIMEOUT_BANCO".equals(escenario)) {
            resp.setAprobado(false);
            resp.setCodigoRespuestaBanco("91");
            resp.setMensajeBanco("No hubo respuesta oportuna de " + cuenta.getBanco()
                    + ". No se realizó ningún cargo ni abono; puedes volver a intentar la transferencia de forma segura.");
        } else if ("RECHAZO_CUENTA".equals(escenario) || cuentaMarcadaError) {
            resp.setAprobado(false);
            resp.setCodigoRespuestaBanco("14");
            resp.setMensajeBanco(cuenta.getBanco() + " rechazó el depósito porque la cuenta no existe, está cerrada o el RUT ingresado no corresponde al titular.");
        } else {
            String fechaStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            int trace = ThreadLocalRandom.current().nextInt(1000, 9999);
            String codigoOp = String.format("TEF-%s-%s-%d-%04d", codigoSbif, fechaStr, reembolso.getIdReembolso(), trace);

            resp.setAprobado(true);
            resp.setCodigoOperacion(codigoOp);
            resp.setCodigoRespuestaBanco("00");
            resp.setMensajeBanco("Transferencia acreditada correctamente en " + cuenta.getBanco() + ".");
        }

        resp.setLatenciaMs(System.currentTimeMillis() - inicio);
        return resp;
    }

    private void simularLatenciaRedBancaria() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(220L, 480L);
            Thread.sleep(delay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private String resolverCodigoSbif(String banco) {
        if (banco == null) return "999";
        String b = banco.trim().toLowerCase();
        if (b.contains("chile") || b.contains("edwards")) return "001";
        if (b.contains("estado")) return "012";
        if (b.contains("scotiabank")) return "014";
        if (b.contains("bci") || b.contains("crédito") || b.contains("credito")) return "016";
        if (b.contains("bice")) return "028";
        if (b.contains("santander")) return "037";
        if (b.contains("itaú") || b.contains("itau")) return "039";
        if (b.contains("security")) return "049";
        if (b.contains("falabella")) return "051";
        if (b.contains("ripley")) return "053";
        if (b.contains("consorcio")) return "055";
        return "099";
    }
}
