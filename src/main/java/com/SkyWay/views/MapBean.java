package com.SkyWay.views;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;

@Named("mapBean")
@ViewScoped
public class MapBean implements Serializable {

    private String ciudadSeleccionada;
    private String aeropuertoSeleccionado;

    public String getAeropuertoSeleccionado() {
        return aeropuertoSeleccionado;
    }

    public void setAeropuertoSeleccionado(String aeropuertoSeleccionado) {
        this.aeropuertoSeleccionado = aeropuertoSeleccionado;
    }

    public String getCiudadSeleccionada() {
        return ciudadSeleccionada;
    }

    public void setCiudadSeleccionada(String ciudadSeleccionada) {
        this.ciudadSeleccionada = ciudadSeleccionada;
    }
}
