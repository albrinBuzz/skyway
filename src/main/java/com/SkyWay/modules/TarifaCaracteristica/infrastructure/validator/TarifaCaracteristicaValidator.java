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
        if (tarifaCaracteristicas == null) return false;
        for (TarifaCaracteristica carac : tarifaCaracteristicas) {
            if (carac != null && carac.getCaracteristica() != null && carac.getCaracteristica().getNombre() != null) {
                String nom = carac.getCaracteristica().getNombre().toLowerCase(Locale.ROOT);
                if (nom.contains("cambio") && nom.contains("asiento")) {
                    return Boolean.TRUE.equals(carac.getValorBool());
                }
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
        Integer horasMinimas = obtenerHorasMinimasCambio(tarifaCaracteristicas);
        if (horasMinimas != null && horasMinimas > 0) {
            return horasAntesDeVuelo >= horasMinimas;
        }
        return true;
    }

    /**
     * Obtiene el plazo límite en horas mínimas previas al vuelo para cambio de asiento.
     */
    public Integer obtenerHorasMinimasCambio(List<TarifaCaracteristica> tarifaCaracteristicas) {
        if (tarifaCaracteristicas == null) return null;
        for (TarifaCaracteristica carac : tarifaCaracteristicas) {
            if (carac != null && carac.getCaracteristica() != null && carac.getCaracteristica().getNombre() != null) {
                String nom = carac.getCaracteristica().getNombre().toLowerCase(Locale.ROOT);
                if (nom.contains("plazo") || (nom.contains("horas") && nom.contains("cambio"))) {
                    if (carac.getValorInt() != null) {
                        return carac.getValorInt();
                    }
                    if (carac.getValor() != null && !carac.getValor().isBlank()) {
                        try {
                            String soloDigitos = carac.getValor().replaceAll("[^0-9]", "");
                            if (!soloDigitos.isEmpty()) {
                                return Integer.parseInt(soloDigitos);
                            }
                        } catch (NumberFormatException e) {
                            Logger.logError("Error al interpretar plazo en texto: " + carac.getValor());
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Obtiene el porcentaje de recargo administrativo por cambio de asiento desde la BD.
     * Retorna el valor entero configurado (ej. 15 para 15%, 0 para 0%).
     */
    public int obtenerPorcentajeCargoCambio(List<TarifaCaracteristica> tarifaCaracteristicas) {
        if (tarifaCaracteristicas != null) {
            for (TarifaCaracteristica carac : tarifaCaracteristicas) {
                if (carac != null && carac.getCaracteristica() != null && carac.getCaracteristica().getNombre() != null) {
                    String nom = carac.getCaracteristica().getNombre().toLowerCase(Locale.ROOT);
                    if (nom.contains("porcentaje") || nom.contains("cargo") || nom.contains("recargo")) {
                        if (carac.getValorInt() != null) {
                            return carac.getValorInt();
                        }
                        if (carac.getValor() != null && !carac.getValor().isBlank()) {
                            try {
                                String soloDigitos = carac.getValor().replaceAll("[^0-9]", "");
                                if (!soloDigitos.isEmpty()) {
                                    return Integer.parseInt(soloDigitos);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        }
        return 15; // Fallback corporativo estándar de SkyWay (15%) si no está explícito en la base de datos
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
