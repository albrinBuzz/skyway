package com.SkyWay.modules.segmentovuelo.presentation.dto;

public interface SegmentoMapaProjection {
    Integer getIdSegmento();
    Integer getIdVuelo();
    Integer getOrdenSegmento();
    String getIataOrigen();
    String getNombreOrigen();
    Double getLatOrigen();
    Double getLngOrigen();
    String getIataDestino();
    String getNombreDestino();
    Double getLatDestino();
    Double getLngDestino();
}