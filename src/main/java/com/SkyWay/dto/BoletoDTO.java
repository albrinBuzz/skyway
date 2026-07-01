package com.SkyWay.dto;

import java.math.BigDecimal;

public class BoletoDTO {

    // Información del pasajero
    private String nombreCompleto;
    private String documentoIdentidad;
    private String correoElectronico;
    
    // Información del vuelo
    private String numeroVuelo;
    private String fechaSalidaCompleta;
    private String horaSalida;
    
    // Información de los aeropuertos de salida y llegada
    private String aeropuertoSalida;
    private String aeropuertoLlegada;
    
    // Duración del vuelo
    private BigDecimal duracionVuelo;
    
    // Precio total del vuelo
    private Long precioTotal;

    // Getters and Setters
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public String getFechaSalidaCompleta() {
        return fechaSalidaCompleta;
    }

    public void setFechaSalidaCompleta(String fechaSalidaCompleta) {
        this.fechaSalidaCompleta = fechaSalidaCompleta;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getAeropuertoSalida() {
        return aeropuertoSalida;
    }

    public void setAeropuertoSalida(String aeropuertoSalida) {
        this.aeropuertoSalida = aeropuertoSalida;
    }

    public String getAeropuertoLlegada() {
        return aeropuertoLlegada;
    }

    public void setAeropuertoLlegada(String aeropuertoLlegada) {
        this.aeropuertoLlegada = aeropuertoLlegada;
    }

    public BigDecimal getDuracionVuelo() {
        return duracionVuelo;
    }

    public void setDuracionVuelo(BigDecimal duracionVuelo) {
        this.duracionVuelo = duracionVuelo;
    }

    public Long getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(Long precioTotal) {
        this.precioTotal = precioTotal;
    }
    
    @Override
    public String toString() {
        return "VueloDTO{" +
                "nombreCompleto='" + nombreCompleto + '\'' +
                ", documentoIdentidad='" + documentoIdentidad + '\'' +
                ", correoElectronico='" + correoElectronico + '\'' +
                ", numeroVuelo='" + numeroVuelo + '\'' +
                ", fechaSalidaCompleta='" + fechaSalidaCompleta + '\'' +
                ", horaSalida='" + horaSalida + '\'' +
                ", aeropuertoSalida='" + aeropuertoSalida + '\'' +
                ", aeropuertoLlegada='" + aeropuertoLlegada + '\'' +
                ", duracionVuelo=" + duracionVuelo +
                ", precioTotal=" + precioTotal +
                '}';
    }
}
