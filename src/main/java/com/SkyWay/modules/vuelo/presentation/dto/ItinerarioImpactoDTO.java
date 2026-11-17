package com.SkyWay.modules.vuelo.presentation.dto;

import java.io.Serializable;

/**
 * Representa el impacto de reprogramar un vuelo sobre un itinerario comercial concreto y sus escalas.
 */
public class ItinerarioImpactoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idItinerario;
    private String rutaComercial;
    private int numeroEscalas;
    private String secuenciaTramos;
    private String estadoConexion;
    private long reservasActivas;
    private String detalleConexion;

    public Integer getIdItinerario() {
        return idItinerario;
    }

    public void setIdItinerario(Integer idItinerario) {
        this.idItinerario = idItinerario;
    }

    public String getRutaComercial() {
        return rutaComercial;
    }

    public void setRutaComercial(String rutaComercial) {
        this.rutaComercial = rutaComercial;
    }

    public int getNumeroEscalas() {
        return numeroEscalas;
    }

    public void setNumeroEscalas(int numeroEscalas) {
        this.numeroEscalas = numeroEscalas;
    }

    public String getSecuenciaTramos() {
        return secuenciaTramos;
    }

    public void setSecuenciaTramos(String secuenciaTramos) {
        this.secuenciaTramos = secuenciaTramos;
    }

    public String getEstadoConexion() {
        return estadoConexion;
    }

    public void setEstadoConexion(String estadoConexion) {
        this.estadoConexion = estadoConexion;
    }

    public long getReservasActivas() {
        return reservasActivas;
    }

    public void setReservasActivas(long reservasActivas) {
        this.reservasActivas = reservasActivas;
    }

    public String getDetalleConexion() {
        return detalleConexion;
    }

    public void setDetalleConexion(String detalleConexion) {
        this.detalleConexion = detalleConexion;
    }
}
