package com.SkyWay.modules.aeropuerto.presentation.dto;

public interface AeropuertoMapaProjection {
    Integer getIdAeropuerto();
    String getCodigoIata();
    String getNombreAeropuerto();
    String getCiudad();
    String getPais();
    String getContinente();
    Double getLatitud();
    Double getLongitud();
    Boolean getActivo();
}