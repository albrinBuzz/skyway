package com.SkyWay.modules.avion.presentation.dto;

import java.io.Serializable;

public class FlotaResumenDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String fabricante;
    private final String modelo;
    private final long unidades;
    private final int capMin;
    private final int capMax;
    private final int ultimoAnio;

    public FlotaResumenDTO(String fabricante, String modelo, long unidades, int capMin, int capMax, int ultimoAnio) {
        this.fabricante = fabricante;
        this.modelo = modelo;
        this.unidades = unidades;
        this.capMin = capMin;
        this.capMax = capMax;
        this.ultimoAnio = ultimoAnio;
    }

    public String getNombreMostrar() {
        if (modelo != null && fabricante != null && !fabricante.isBlank()
                && modelo.toLowerCase().contains(fabricante.toLowerCase())) {
            return modelo;
        }
        if (fabricante == null || fabricante.isBlank()) {
            return modelo != null ? modelo : "";
        }
        return fabricante + " " + (modelo != null ? modelo : "");
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getModelo() {
        return modelo;
    }

    public long getUnidades() {
        return unidades;
    }

    public int getCapMin() {
        return capMin;
    }

    public int getCapMax() {
        return capMax;
    }

    public int getUltimoAnio() {
        return ultimoAnio;
    }
}
