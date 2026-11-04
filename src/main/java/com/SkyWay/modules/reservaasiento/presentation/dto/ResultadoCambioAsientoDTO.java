package com.SkyWay.modules.reservaasiento.presentation.dto;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * DTO que encapsula el resultado y feedback operacional detallado tras ejecutar
 * una reasignación atómica de asientos, informando al pasajero los tramos modificados,
 * butacas originales y nuevas, estado de tarjetas de embarque y comprobante de pago.
 */
public class ResultadoCambioAsientoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idReserva;
    private String codigoPnr;
    private String rutTitular;
    private int montoTotal;
    private String montoTotalFormateado;
    private String metodoPago;
    private String fechaHora;
    private String origen;
    private List<DetalleResultadoCambioDTO> items = new ArrayList<>();

    public ResultadoCambioAsientoDTO() {}

    public static String formatearClp(int valor) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("es", "CL"));
        symbols.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("$#,##0 CLP", symbols);
        return df.format(valor);
    }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getCodigoPnr() { return codigoPnr; }
    public void setCodigoPnr(String codigoPnr) { this.codigoPnr = codigoPnr; }

    public String getRutTitular() { return rutTitular; }
    public void setRutTitular(String rutTitular) { this.rutTitular = rutTitular; }

    public int getMontoTotal() { return montoTotal; }
    public void setMontoTotal(int montoTotal) {
        this.montoTotal = montoTotal;
        this.montoTotalFormateado = formatearClp(montoTotal);
    }

    public String getMontoTotalFormateado() { return montoTotalFormateado; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getFechaHora() { return fechaHora; }
    public void setFechaHora(String fechaHora) { this.fechaHora = fechaHora; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public List<DetalleResultadoCambioDTO> getItems() { return items; }
    public void setItems(List<DetalleResultadoCambioDTO> items) { this.items = items; }
    public void addItem(DetalleResultadoCambioDTO item) { this.items.add(item); }

    public static class DetalleResultadoCambioDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String numeroVuelo;
        private String tramoVuelo;
        private String nombrePasajero;
        private String rutPasajero;
        private String asientoOriginal;
        private String claseOriginal;
        private String asientoNuevo;
        private String claseNueva;
        private int costoAdicional;
        private String costoAdicionalFormateado;

        public DetalleResultadoCambioDTO() {}

        public String getNumeroVuelo() { return numeroVuelo; }
        public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

        public String getTramoVuelo() { return tramoVuelo; }
        public void setTramoVuelo(String tramoVuelo) { this.tramoVuelo = tramoVuelo; }

        public String getNombrePasajero() { return nombrePasajero; }
        public void setNombrePasajero(String nombrePasajero) { this.nombrePasajero = nombrePasajero; }

        public String getRutPasajero() { return rutPasajero; }
        public void setRutPasajero(String rutPasajero) { this.rutPasajero = rutPasajero; }

        public String getAsientoOriginal() { return asientoOriginal; }
        public void setAsientoOriginal(String asientoOriginal) { this.asientoOriginal = asientoOriginal; }

        public String getClaseOriginal() { return claseOriginal; }
        public void setClaseOriginal(String claseOriginal) { this.claseOriginal = claseOriginal; }

        public String getAsientoNuevo() { return asientoNuevo; }
        public void setAsientoNuevo(String asientoNuevo) { this.asientoNuevo = asientoNuevo; }

        public String getClaseNueva() { return claseNueva; }
        public void setClaseNueva(String claseNueva) { this.claseNueva = claseNueva; }

        public int getCostoAdicional() { return costoAdicional; }
        public void setCostoAdicional(int costoAdicional) {
            this.costoAdicional = costoAdicional;
            this.costoAdicionalFormateado = ResultadoCambioAsientoDTO.formatearClp(costoAdicional);
        }

        public String getCostoAdicionalFormateado() { return costoAdicionalFormateado; }
    }
}
