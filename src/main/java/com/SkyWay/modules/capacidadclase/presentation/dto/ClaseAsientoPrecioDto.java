package com.SkyWay.modules.capacidadclase.presentation.dto;

public class ClaseAsientoPrecioDto {

    int precio;

    int claseAsiento;

    int cantidad;

    String clase;

    public ClaseAsientoPrecioDto(int precio, int claseAsiento) {
        this.precio = precio;
        this.claseAsiento = claseAsiento;
    }

    public ClaseAsientoPrecioDto() {
    }

    public void setClase(String clase) {
        this.clase = clase;
    }

    public String getClase() {
        return clase;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setClaseAsiento(int claseAsiento) {
        this.claseAsiento = claseAsiento;
    }

    public int getClaseAsiento() {
        return claseAsiento;
    }

    public void setPrecio(int precio) {
        this.precio = precio;
    }

    public int getPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ClaseAsientoPrecioDto{");
        sb.append("precio=").append(precio);
        sb.append(", claseAsiento=").append(claseAsiento);
        sb.append(", cantidad=").append(cantidad);
        sb.append(", clase='").append(clase).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
