package com.SkyWay.modules.vuelo.presentation.dto;

import java.sql.Timestamp;

public interface VueloEstadoProjection {
    Integer getIdVuelo();
    String getNumeroVuelo();
    String getOrigenCiudad();
    String getOrigenIata();
    String getDestinoCiudad();
    String getDestinoIata();
    Timestamp getFechaHoraSalida();
    String getEstadoNombre();
    String getCodigoPuerta();
}