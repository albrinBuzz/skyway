package com.SkyWay.modules.tarifa.presentation.dto;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DTO que representa una opción disponible para Ascenso de Tarifa (Fare Upsell / Cabin Upgrade),
 * detallando el precio de la nueva tarifa, el precio ya pagado, la diferencia exacta a pagar
 * y las características y beneficios adicionales que adquiere el pasajero.
 */
public class OpcionUpgradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idItinerarioTarifaNuevo;
    private Integer idTarifa;
    private String nombreTarifa;
    private String nombreTarifaActual;
    private BigDecimal precioTotalNuevo;
    private BigDecimal precioTarifaActual;
    private BigDecimal diferenciaAPagar;
    private boolean permiteCambioAsiento;
    private Integer horasMinimasCambioAsiento;
    private boolean permiteCancelacion;
    private List<TarifaCaracteristica> caracteristicas = new ArrayList<>();

    public OpcionUpgradeDTO() {}

    public OpcionUpgradeDTO(Integer idItinerarioTarifaNuevo, Integer idTarifa, String nombreTarifa,
                            BigDecimal precioTotalNuevo, BigDecimal precioTarifaActual, BigDecimal diferenciaAPagar) {
        this.idItinerarioTarifaNuevo = idItinerarioTarifaNuevo;
        this.idTarifa = idTarifa;
        this.nombreTarifa = nombreTarifa;
        this.precioTotalNuevo = precioTotalNuevo;
        this.precioTarifaActual = precioTarifaActual;
        this.diferenciaAPagar = diferenciaAPagar;
    }

    public String getDiferenciaFormateada() {
        if (diferenciaAPagar == null) return "$0 CLP";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CL"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("$#,##0 CLP", symbols);
        return df.format(diferenciaAPagar);
    }

    public String getPrecioTotalFormateado() {
        if (precioTotalNuevo == null) return "$0 CLP";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CL"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("$#,##0 CLP", symbols);
        return df.format(precioTotalNuevo);
    }

    public String getPrecioTarifaActualFormateada() {
        if (precioTarifaActual == null) return "$0 CLP";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CL"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("$#,##0 CLP", symbols);
        return df.format(precioTarifaActual);
    }

    public String getDescripcionPlazo() {
        if (horasMinimasCambioAsiento != null && horasMinimasCambioAsiento > 0) {
            return "Hasta " + horasMinimasCambioAsiento + " horas previas a la salida del vuelo";
        }
        return "Hasta 24 horas previas a la salida del vuelo";
    }

    public String getDetalleEquipaje() {
        if (nombreTarifa == null) return "Equipaje estándar";
        String n = nombreTarifa.toLowerCase();
        if (n.contains("premium")) return "Equipaje de mano (10 kg) + Embarque Prioritario";
        if (n.contains("flex")) return "Equipaje de mano (10 kg) incluido";
        return "Equipaje según condiciones de tarifa";
    }

    public String getBadgeClass() {
        if (nombreTarifa == null) return "badge-tier-corporate";
        String n = nombreTarifa.toLowerCase();
        if (n.contains("premium")) return "badge-tier-premium";
        if (n.contains("flex")) return "badge-tier-flexible";
        return "badge-tier-corporate";
    }

    public String getBadgeClase() {
        return getBadgeClass();
    }

    public String getPrecioNuevoFormateado() {
        return getPrecioTotalFormateado();
    }

    public String getIcono() {
        if (nombreTarifa == null) return "pi-tag";
        String n = nombreTarifa.toLowerCase();
        if (n.contains("premium")) return "pi-briefcase";
        if (n.contains("flex")) return "pi-sliders-h";
        return "pi-tag";
    }

    public String getNombreTarifaActual() { return nombreTarifaActual; }
    public void setNombreTarifaActual(String nombreTarifaActual) { this.nombreTarifaActual = nombreTarifaActual; }

    // Getters y Setters
    public Integer getIdItinerarioTarifaNuevo() { return idItinerarioTarifaNuevo; }
    public void setIdItinerarioTarifaNuevo(Integer idItinerarioTarifaNuevo) { this.idItinerarioTarifaNuevo = idItinerarioTarifaNuevo; }

    public Integer getIdTarifa() { return idTarifa; }
    public void setIdTarifa(Integer idTarifa) { this.idTarifa = idTarifa; }

    public String getNombreTarifa() { return nombreTarifa; }
    public void setNombreTarifa(String nombreTarifa) { this.nombreTarifa = nombreTarifa; }

    public BigDecimal getPrecioTotalNuevo() { return precioTotalNuevo; }
    public void setPrecioTotalNuevo(BigDecimal precioTotalNuevo) { this.precioTotalNuevo = precioTotalNuevo; }

    public BigDecimal getPrecioTarifaActual() { return precioTarifaActual; }
    public void setPrecioTarifaActual(BigDecimal precioTarifaActual) { this.precioTarifaActual = precioTarifaActual; }

    public BigDecimal getDiferenciaAPagar() { return diferenciaAPagar; }
    public void setDiferenciaAPagar(BigDecimal diferenciaAPagar) { this.diferenciaAPagar = diferenciaAPagar; }

    public boolean isPermiteCambioAsiento() { return permiteCambioAsiento; }
    public void setPermiteCambioAsiento(boolean permiteCambioAsiento) { this.permiteCambioAsiento = permiteCambioAsiento; }

    public Integer getHorasMinimasCambioAsiento() { return horasMinimasCambioAsiento; }
    public void setHorasMinimasCambioAsiento(Integer horasMinimasCambioAsiento) { this.horasMinimasCambioAsiento = horasMinimasCambioAsiento; }

    public boolean isPermiteCancelacion() { return permiteCancelacion; }
    public void setPermiteCancelacion(boolean permiteCancelacion) { this.permiteCancelacion = permiteCancelacion; }

    public List<TarifaCaracteristica> getCaracteristicas() { return caracteristicas; }
    public void setCaracteristicas(List<TarifaCaracteristica> caracteristicas) { this.caracteristicas = caracteristicas; }
}
