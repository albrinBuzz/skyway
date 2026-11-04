package com.SkyWay.modules.reservaasiento.presentation.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SolicitudCambioAsientoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idReserva;
    private String rutUsuario;
    private int granTotalAdicional;
    private Integer idMetodoPago;
    private String origen;
    private List<ItemCambioAsientoDTO> items = new ArrayList<>();

    public SolicitudCambioAsientoDTO() {
    }

    public Integer getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Integer idReserva) {
        this.idReserva = idReserva;
    }

    public String getRutUsuario() {
        return rutUsuario;
    }

    public void setRutUsuario(String rutUsuario) {
        this.rutUsuario = rutUsuario;
    }

    public int getGranTotalAdicional() {
        return granTotalAdicional;
    }

    public void setGranTotalAdicional(int granTotalAdicional) {
        this.granTotalAdicional = granTotalAdicional;
    }

    public Integer getIdMetodoPago() {
        return idMetodoPago;
    }

    public void setIdMetodoPago(Integer idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public List<ItemCambioAsientoDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemCambioAsientoDTO> items) {
        this.items = items;
    }

    public void addItem(ItemCambioAsientoDTO item) {
        this.items.add(item);
    }

    public static class ItemCambioAsientoDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer idVuelo;
        private String numeroVuelo;
        private String tramoVuelo;
        private String rutPasajero;
        private String nombrePasajero;
        private Integer idAsientoOriginal;
        private String numeroAsientoOriginal;
        private String claseOriginal;
        private Integer idAsientoNuevo;
        private String numeroAsientoNuevo;
        private String claseNueva;
        private int montoAdicional;

        public ItemCambioAsientoDTO() {}

        public Integer getIdVuelo() { return idVuelo; }
        public void setIdVuelo(Integer idVuelo) { this.idVuelo = idVuelo; }

        public String getNumeroVuelo() { return numeroVuelo; }
        public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

        public String getTramoVuelo() { return tramoVuelo; }
        public void setTramoVuelo(String tramoVuelo) { this.tramoVuelo = tramoVuelo; }

        public String getRutPasajero() { return rutPasajero; }
        public void setRutPasajero(String rutPasajero) { this.rutPasajero = rutPasajero; }

        public String getNombrePasajero() { return nombrePasajero; }
        public void setNombrePasajero(String nombrePasajero) { this.nombrePasajero = nombrePasajero; }

        public Integer getIdAsientoOriginal() { return idAsientoOriginal; }
        public void setIdAsientoOriginal(Integer idAsientoOriginal) { this.idAsientoOriginal = idAsientoOriginal; }

        public String getNumeroAsientoOriginal() { return numeroAsientoOriginal; }
        public void setNumeroAsientoOriginal(String numeroAsientoOriginal) { this.numeroAsientoOriginal = numeroAsientoOriginal; }

        public String getClaseOriginal() { return claseOriginal; }
        public void setClaseOriginal(String claseOriginal) { this.claseOriginal = claseOriginal; }

        public Integer getIdAsientoNuevo() { return idAsientoNuevo; }
        public void setIdAsientoNuevo(Integer idAsientoNuevo) { this.idAsientoNuevo = idAsientoNuevo; }

        public String getNumeroAsientoNuevo() { return numeroAsientoNuevo; }
        public void setNumeroAsientoNuevo(String numeroAsientoNuevo) { this.numeroAsientoNuevo = numeroAsientoNuevo; }

        public String getClaseNueva() { return claseNueva; }
        public void setClaseNueva(String claseNueva) { this.claseNueva = claseNueva; }

        public int getMontoAdicional() { return montoAdicional; }
        public void setMontoAdicional(int montoAdicional) { this.montoAdicional = montoAdicional; }
    }
}
