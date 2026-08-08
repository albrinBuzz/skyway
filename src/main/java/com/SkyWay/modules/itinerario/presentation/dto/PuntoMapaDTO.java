package com.SkyWay.modules.itinerario.presentation.dto;

public class PuntoMapaDTO {
    private String codigoIata;
    private String nombreAeropuerto;
    private String ciudad;
    private double lat;
    private double lng;
    private String tipo; // ORIGEN, ESCALA, DESTINO

    // constructor, getters, setters
    public PuntoMapaDTO(String codigoIata, String nombreAeropuerto, String ciudad,
                        double lat, double lng, String tipo) {
        this.codigoIata = codigoIata;
        this.nombreAeropuerto = nombreAeropuerto;
        this.ciudad = ciudad;
        this.lat = lat;
        this.lng = lng;
        this.tipo = tipo;
    }
    public String getCodigoIata() { return codigoIata; }
    public String getNombreAeropuerto() { return nombreAeropuerto; }
    public String getCiudad() { return ciudad; }
    public double getLat() { return lat; }
    public double getLng() { return lng; }
    public String getTipo() { return tipo; }
}