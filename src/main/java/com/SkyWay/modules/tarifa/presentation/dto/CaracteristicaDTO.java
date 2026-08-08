package com.SkyWay.modules.tarifa.presentation.dto;

public class CaracteristicaDTO {
    private String nombre;
    private String valor;
    private String tipoDato;
    private Boolean valorBool;
    private Integer valorInt;
    // getters y setters


    public CaracteristicaDTO(String nombre, String valor, String tipoDato, Boolean valorBool, Integer valorInt) {
        this.nombre = nombre;
        this.valor = valor;
        this.tipoDato = tipoDato;
        this.valorBool = valorBool;
        this.valorInt = valorInt;
    }

    public CaracteristicaDTO(String nombre, String valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public void setValorBool(Boolean valorBool) {
        this.valorBool = valorBool;
    }

    public void setValorInt(Integer valorInt) {
        this.valorInt = valorInt;
    }

    public Boolean getValorBool() {
        return valorBool;
    }

    public Integer getValorInt() {
        return valorInt;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }
}