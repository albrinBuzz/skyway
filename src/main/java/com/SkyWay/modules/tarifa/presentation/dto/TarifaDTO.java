package com.SkyWay.modules.tarifa.presentation.dto;

import java.math.BigDecimal;
import java.util.List;

public class TarifaDTO {
    private Integer idTarifa;
    private String nombre;
    private BigDecimal precio;
    private List<CaracteristicaDTO> caracteristicas;
    // getters y setters


    public Integer getIdTarifa() {
        return idTarifa;
    }

    public void setIdTarifa(Integer idTarifa) {
        this.idTarifa = idTarifa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public List<CaracteristicaDTO> getCaracteristicas() {
        return caracteristicas;
    }

    public void setCaracteristicas(List<CaracteristicaDTO> caracteristicas) {
        this.caracteristicas = caracteristicas;
    }
}

