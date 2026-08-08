package com.SkyWay.modules.aeropuerto.presentation.dto;


public class AeropuertoMapaDTO {
    private String codigoIata;
    private String nombreAeropuerto;
    private String ciudad;
    private double lat;
    private double lng;

    public AeropuertoMapaDTO(String codigoIata, String nombreAeropuerto, String ciudad, double lat, double lng) {
        this.codigoIata = codigoIata;
        this.nombreAeropuerto = nombreAeropuerto;
        this.ciudad = ciudad;
        this.lat = lat;
        this.lng = lng;
    }
    public String getCodigoIata() { return codigoIata; }
    public String getNombreAeropuerto() { return nombreAeropuerto; }
    public String getCiudad() { return ciudad; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
}