package com.SkyWay.modules.vuelo.presentation.dto;

import java.util.List;

public class VueloMapaDTO {
    private Integer idVuelo;
    private String numeroVuelo;
    private List<PuntoSegmentoDTO> puntos;
    private boolean vinculado; // NUEVO

    public VueloMapaDTO(Integer idVuelo, String numeroVuelo, List<PuntoSegmentoDTO> puntos) {
        this.idVuelo = idVuelo;
        this.numeroVuelo = numeroVuelo;
        this.puntos = puntos;
        this.vinculado = false;
    }
    public Integer getIdVuelo() { return idVuelo; }
    public String getNumeroVuelo() { return numeroVuelo; }
    public List<PuntoSegmentoDTO> getPuntos() { return puntos; }
    public boolean isVinculado() { return vinculado; }
    public void setVinculado(boolean vinculado) { this.vinculado = vinculado; }

    public static class PuntoSegmentoDTO {
        private String iata;
        private String nombre;
        private double lat;
        private double lng;
        private String tipo; // ORIGEN, ESCALA, DESTINO

        public PuntoSegmentoDTO(String iata, String nombre, double lat, double lng, String tipo) {
            this.iata = iata; this.nombre = nombre; this.lat = lat; this.lng = lng; this.tipo = tipo;
        }
        public String getIata() { return iata; }
        public String getNombre() { return nombre; }
        public double getLat() { return lat; }
        public double getLng() { return lng; }
        public String getTipo() { return tipo; }
    }
}