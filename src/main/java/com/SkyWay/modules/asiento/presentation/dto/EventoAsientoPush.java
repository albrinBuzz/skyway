package com.SkyWay.modules.asiento.presentation.dto;

import java.io.Serializable;

public class EventoAsientoPush implements Serializable {
    private Integer idAsiento;
    private Integer idVuelo;
    private String estado; // "Bloqueado", "Disponible", "Ocupado"
    private String sessionId; // Para diferenciar al usuario actual

    public EventoAsientoPush(Integer idAsiento, Integer idVuelo, String estado, String sessionId) {
        this.idAsiento = idAsiento;
        this.idVuelo = idVuelo;
        this.estado = estado;
        this.sessionId = sessionId;
    }

    // Getters y Setters
    public Integer getIdAsiento() { return idAsiento; }
    public Integer getIdVuelo() { return idVuelo; }
    public String getEstado() { return estado; }
    public String getSessionId() { return sessionId; }
}
