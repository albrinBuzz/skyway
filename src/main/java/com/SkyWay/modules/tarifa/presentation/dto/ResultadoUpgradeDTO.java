package com.SkyWay.modules.tarifa.presentation.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DTO que encapsula el resultado y retroalimentación detallada tras completar
 * con éxito un ascenso de tarifa (Upgrade), para informar al pasajero
 * a qué reserva, vuelo e itinerario afectó, junto a sus nuevos beneficios.
 */
public class ResultadoUpgradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idReserva;
    private String codigoPnr;
    private String nombrePasajero;
    private Integer idItinerario;
    private String numeroVuelo;
    private String origenRuta;
    private String destinoRuta;
    private String rutaCompleta;
    private String fechaHoraSalida;
    private String tarifaAnterior;
    private String tarifaNueva;
    private BigDecimal montoDiferencia;
    private String montoDiferenciaFormateado;
    private String metodoPago;
    private boolean permiteCambioAsiento;
    private List<String> beneficiosNuevos = new ArrayList<>();
    private String origen; // "checkin" o "perfil"

    public ResultadoUpgradeDTO() {}

    public static String formatearClp(BigDecimal valor) {
        if (valor == null) return "$0 CLP";
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CL"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("$#,##0 CLP", symbols);
        return df.format(valor);
    }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getCodigoPnr() { return codigoPnr; }
    public void setCodigoPnr(String codigoPnr) { this.codigoPnr = codigoPnr; }

    public String getNombrePasajero() { return nombrePasajero; }
    public void setNombrePasajero(String nombrePasajero) { this.nombrePasajero = nombrePasajero; }

    public Integer getIdItinerario() { return idItinerario; }
    public void setIdItinerario(Integer idItinerario) { this.idItinerario = idItinerario; }

    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

    public String getOrigenRuta() { return origenRuta; }
    public void setOrigenRuta(String origenRuta) { this.origenRuta = origenRuta; }

    public String getDestinoRuta() { return destinoRuta; }
    public void setDestinoRuta(String destinoRuta) { this.destinoRuta = destinoRuta; }

    public String getRutaCompleta() { return rutaCompleta; }
    public void setRutaCompleta(String rutaCompleta) { this.rutaCompleta = rutaCompleta; }

    public String getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(String fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    public String getTarifaAnterior() { return tarifaAnterior; }
    public void setTarifaAnterior(String tarifaAnterior) { this.tarifaAnterior = tarifaAnterior; }

    public String getTarifaNueva() { return tarifaNueva; }
    public void setTarifaNueva(String tarifaNueva) { this.tarifaNueva = tarifaNueva; }

    public BigDecimal getMontoDiferencia() { return montoDiferencia; }
    public void setMontoDiferencia(BigDecimal montoDiferencia) {
        this.montoDiferencia = montoDiferencia;
        this.montoDiferenciaFormateado = formatearClp(montoDiferencia);
    }

    public String getMontoDiferenciaFormateado() {
        if (montoDiferenciaFormateado == null && montoDiferencia != null) {
            return formatearClp(montoDiferencia);
        }
        return montoDiferenciaFormateado != null ? montoDiferenciaFormateado : "$0 CLP";
    }
    public void setMontoDiferenciaFormateado(String montoDiferenciaFormateado) { this.montoDiferenciaFormateado = montoDiferenciaFormateado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public boolean isPermiteCambioAsiento() { return permiteCambioAsiento; }
    public void setPermiteCambioAsiento(boolean permiteCambioAsiento) { this.permiteCambioAsiento = permiteCambioAsiento; }

    public List<String> getBeneficiosNuevos() { return beneficiosNuevos; }
    public void setBeneficiosNuevos(List<String> beneficiosNuevos) { this.beneficiosNuevos = beneficiosNuevos; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
}
