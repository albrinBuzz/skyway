package com.SkyWay.modules.asiento.presentation.dto;

import java.io.Serializable;

public class EventoAsientoPush implements Serializable {
    private Integer idAsiento;
    private Integer idVuelo;
    private String estado; // "Bloqueado", "Disponible", "Ocupado"

    public EventoAsientoPush(Integer idAsiento, Integer idVuelo, String estado, String ignorado) {
        this.idAsiento = idAsiento;
        this.idVuelo = idVuelo;
        this.estado = estado;
        // El sessionId/token del dueno NUNCA se emite por el canal publico (evita robo de sesion/suplantacion).
    }

    // Getters y Setters
    public Integer getIdAsiento() { return idAsiento; }
    public Integer getIdVuelo() { return idVuelo; }
    public String getEstado() { return estado; }
}
