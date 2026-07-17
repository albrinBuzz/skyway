package com.SkyWay.modules.segmentovuelo.presentation.dto;

public interface PuntoRutaProjection {
    Integer getIdAeropuerto();
    String getCodigoIata();
    String getNombreAeropuerto();
    String getCiudad();
    Double getLatitud();
    Double getLongitud();
    Integer getOrdenGlobal();
    String getRol(); // ORIGEN | DESTINO
}