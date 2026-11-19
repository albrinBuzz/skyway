package com.SkyWay.modules.vuelo.presentation.dto;

import java.io.Serializable;

public class VueloProgramadoPortalDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String numeroVuelo;
    private final String origIata;
    private final String origCiudad;
    private final String destIata;
    private final String destCiudad;
    private final String salidaTxt;
    private final String llegadaTxt;
    private final String modelo;
    private final String estado;

    public VueloProgramadoPortalDTO(String numeroVuelo, String origIata, String origCiudad, String destIata,
                                    String destCiudad, String salidaTxt, String llegadaTxt, String modelo, String estado) {
        this.numeroVuelo = numeroVuelo;
        this.origIata = origIata;
        this.origCiudad = origCiudad;
        this.destIata = destIata;
        this.destCiudad = destCiudad;
        this.salidaTxt = salidaTxt;
        this.llegadaTxt = llegadaTxt;
        this.modelo = modelo;
        this.estado = estado;
    }

    public String getNumeroVuelo() { return numeroVuelo; }
    public String getOrigIata() { return origIata; }
    public String getOrigCiudad() { return origCiudad; }
    public String getDestIata() { return destIata; }
    public String getDestCiudad() { return destCiudad; }
    public String getSalidaTxt() { return salidaTxt; }
    public String getLlegadaTxt() { return llegadaTxt; }
    public String getModelo() { return modelo; }
    public String getEstado() { return estado; }
}
