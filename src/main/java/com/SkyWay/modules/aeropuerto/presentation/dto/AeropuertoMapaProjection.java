package com.SkyWay.modules.aeropuerto.presentation.dto;

public interface AeropuertoMapaProjection {
    Integer getIdAeropuerto();
    String getCodigoIata();
    String getNombreAeropuerto();
    String getCiudad();
    Double getLatitud();
    Double getLongitud();
}