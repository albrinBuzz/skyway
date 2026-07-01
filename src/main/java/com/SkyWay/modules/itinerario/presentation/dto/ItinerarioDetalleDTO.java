package com.SkyWay.modules.itinerario.presentation.dto;

public class ItinerarioDetalleDTO {
    private Integer idItinerario;
    private String aeropuertoOrigen;
    private String aeropuertoDestino;
    private String duracionTotal;
    private Integer numeroEscalas;
    private Integer orden;
    private String salida;
    private String aeropuertoSalida;
    private String ciudadSalida;
    private String llegada;
    private String aeropuertoLlegada;
    private String ciudadLlegada;
    private String duracion;
    private String tiempoEspera;
    private String modeloAvion;
    private String aerolinea;
    private String vuelo;
    private String descripcionVuelo;

    public ItinerarioDetalleDTO(Integer idItinerario, String aeropuertoOrigen, String aeropuertoDestino, String duracionTotal, Integer numeroEscalas, Integer orden, String salida, String aeropuertoSalida, String ciudadSalida, String llegada, String aeropuertoLlegada, String ciudadLlegada, String duracion, String tiempoEspera, String modeloAvion, String aerolinea, String vuelo, String descripcionVuelo) {
        this.idItinerario = idItinerario;
        this.aeropuertoOrigen = aeropuertoOrigen;
        this.aeropuertoDestino = aeropuertoDestino;
        this.duracionTotal = duracionTotal;
        this.numeroEscalas = numeroEscalas;
        this.orden = orden;
        this.salida = salida;
        this.aeropuertoSalida = aeropuertoSalida;
        this.ciudadSalida = ciudadSalida;
        this.llegada = llegada;
        this.aeropuertoLlegada = aeropuertoLlegada;
        this.ciudadLlegada = ciudadLlegada;
        this.duracion = duracion;
        this.tiempoEspera = tiempoEspera;
        this.modeloAvion = modeloAvion;
        this.aerolinea = aerolinea;
        this.vuelo = vuelo;
        this.descripcionVuelo = descripcionVuelo;
    }

    public ItinerarioDetalleDTO() {
    }

    public Integer getIdItinerario() {
        return idItinerario;
    }

    public void setIdItinerario(Integer idItinerario) {
        this.idItinerario = idItinerario;
    }

    public String getAeropuertoOrigen() {
        return aeropuertoOrigen;
    }

    public void setAeropuertoOrigen(String aeropuertoOrigen) {
        this.aeropuertoOrigen = aeropuertoOrigen;
    }

    public String getAeropuertoDestino() {
        return aeropuertoDestino;
    }

    public void setAeropuertoDestino(String aeropuertoDestino) {
        this.aeropuertoDestino = aeropuertoDestino;
    }

    public String getDuracionTotal() {
        return duracionTotal;
    }

    public void setDuracionTotal(String duracionTotal) {
        this.duracionTotal = duracionTotal;
    }

    public Integer getNumeroEscalas() {
        return numeroEscalas;
    }

    public void setNumeroEscalas(Integer numeroEscalas) {
        this.numeroEscalas = numeroEscalas;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public String getAeropuertoSalida() {
        return aeropuertoSalida;
    }

    public void setAeropuertoSalida(String aeropuertoSalida) {
        this.aeropuertoSalida = aeropuertoSalida;
    }

    public String getCiudadSalida() {
        return ciudadSalida;
    }

    public void setCiudadSalida(String ciudadSalida) {
        this.ciudadSalida = ciudadSalida;
    }

    public String getLlegada() {
        return llegada;
    }

    public void setLlegada(String llegada) {
        this.llegada = llegada;
    }

    public String getAeropuertoLlegada() {
        return aeropuertoLlegada;
    }

    public void setAeropuertoLlegada(String aeropuertoLlegada) {
        this.aeropuertoLlegada = aeropuertoLlegada;
    }

    public String getCiudadLlegada() {
        return ciudadLlegada;
    }

    public void setCiudadLlegada(String ciudadLlegada) {
        this.ciudadLlegada = ciudadLlegada;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getTiempoEspera() {
        return tiempoEspera;
    }

    public void setTiempoEspera(String tiempoEspera) {
        this.tiempoEspera = tiempoEspera;
    }

    public String getModeloAvion() {
        return modeloAvion;
    }

    public void setModeloAvion(String modeloAvion) {
        this.modeloAvion = modeloAvion;
    }

    public String getAerolinea() {
        return aerolinea;
    }

    public void setAerolinea(String aerolinea) {
        this.aerolinea = aerolinea;
    }

    public String getVuelo() {
        return vuelo;
    }

    public void setVuelo(String vuelo) {
        this.vuelo = vuelo;
    }

    public String getDescripcionVuelo() {
        return descripcionVuelo;
    }

    public void setDescripcionVuelo(String descripcionVuelo) {
        this.descripcionVuelo = descripcionVuelo;
    }

// Getters and setters
}
