package com.SkyWay.modules.asiento.presentation.dto;



import java.io.Serializable;

public class AsientoEstado implements Serializable {
    private String codigoAsiento; // Ej: "12A"
    private Long idUsuario;       // ID del usuario que lo tiene bloqueado/seleccionado
    private String estado;        // "DISPONIBLE", "SELECCIONADO", "OCUPADO"

    public AsientoEstado() {}

    public AsientoEstado(String codigoAsiento, Long idUsuario, String estado) {
        this.codigoAsiento = codigoAsiento;
        this.idUsuario = idUsuario;
        this.estado = estado;
    }

    // Getters y Setters
    public String getCodigoAsiento() { return codigoAsiento; }
    public void setCodigoAsiento(String codigoAsiento) { this.codigoAsiento = codigoAsiento; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}