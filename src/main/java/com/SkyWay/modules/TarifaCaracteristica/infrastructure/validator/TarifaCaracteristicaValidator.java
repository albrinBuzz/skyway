package com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.util.Logger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class TarifaCaracteristicaValidator {

    /**
     * Valida si una tarifa permite cambiar asiento según las características.
     * @param tarifaCaracteristicas lista de características de la tarifa con su valor
     * @return true si permite cambio, false si no
     */
    public boolean permiteCambio(List<TarifaCaracteristica> tarifaCaracteristicas) {
        for (TarifaCaracteristica carac : tarifaCaracteristicas) {
            if ("Permite Cambios Asiento".equalsIgnoreCase(carac.getCaracteristica().getNombre())) {
                Logger.logInfo(carac.getValorBool().toString());
                Logger.logInfo(carac.getCaracteristica().getNombre());
                return carac.getValorBool();
            }
        }
        return false; // Por defecto no permite cambio si no se encuentra la característica
    }

    /**
     * Valida si el cambio cumple con las horas mínimas requeridas antes del vuelo.
     * @param tarifaCaracteristicas lista de características de la tarifa con su valor
     * @param horasAntesDeVuelo horas restantes antes de la salida
     * @return true si cumple con las horas mínimas para cambio
     */
    public boolean cumpleHorasMinimasCambio(List<TarifaCaracteristica> tarifaCaracteristicas, int horasAntesDeVuelo) {
        for (TarifaCaracteristica carac : tarifaCaracteristicas) {
            if ("Horas Minimas Cambio".equalsIgnoreCase(carac.getCaracteristica().getNombre())) {
                int horasMinimas = Integer.parseInt(carac.getValor());
                return horasAntesDeVuelo >= horasMinimas;
            }
        }
        // Si no está configurado, asumimos que no hay restricción
        return true;
    }

    // Puedes agregar más métodos según las reglas de negocio para otras características
}
