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
            if ("Permite cambio de asiento".equalsIgnoreCase(carac.getCaracteristica().getNombre())) {
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
            if (carac.getCaracteristica() != null &&
                    "Plazo límite para cambio de asiento".equalsIgnoreCase(carac.getCaracteristica().getNombre())) {

                Integer horasMinimas = null;

                // 1. Intentar leer prioritariamente de la columna Valor_Int
                if (carac.getValorInt() != null) {
                    horasMinimas = carac.getValorInt();
                }
                // 2. Fallback de seguridad: Si viene en el campo texto 'Valor', extraer únicamente los números
                else if (carac.getValor() != null && !carac.getValor().isBlank()) {
                    try {
                        String soloDigitos = carac.getValor().replaceAll("[^0-9]", "");
                        if (!soloDigitos.isEmpty()) {
                            horasMinimas = Integer.parseInt(soloDigitos);
                        }
                    } catch (NumberFormatException e) {
                        Logger.logError("Error al interpretar valor en texto: " + carac.getValor());
                    }
                }

                // 3. Evaluar la condición de horas
                if (horasMinimas != null) {
                    return horasAntesDeVuelo >= horasMinimas;
                }
            }
        }

        // Si la característica no está configurada para la tarifa, no hay restricción
        return true;
    }

    /**
     * Valida si una tarifa permite la cancelación o anulación de la reserva.
     *
     * @param tarifaCaracteristicas lista de características de la tarifa con su valor
     * @return true si permite cancelación/devolución, false si no
     */
    public boolean permiteCancelacion(List<TarifaCaracteristica> tarifaCaracteristicas) {
        if (tarifaCaracteristicas == null) return false;

        for (TarifaCaracteristica carac : tarifaCaracteristicas) {
            if (carac.getCaracteristica() != null &&
                    ("Permite Cancelacion".equalsIgnoreCase(carac.getCaracteristica().getNombre()) ||
                            "Permite Anulacion".equalsIgnoreCase(carac.getCaracteristica().getNombre()))) {

                if (carac.getValorBool() != null) {
                    return carac.getValorBool();
                }
            }
        }
        return false; // Por defecto no permite cancelación si no está configurada la regla
    }
    // Puedes agregar más métodos según las reglas de negocio para otras características
}
