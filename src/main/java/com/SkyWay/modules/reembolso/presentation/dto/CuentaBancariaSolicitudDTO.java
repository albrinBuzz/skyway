package com.SkyWay.modules.reembolso.presentation.dto;

import java.io.Serializable;

/**
 * Datos de cuenta bancaria ingresados o seleccionados por el pasajero para recibir un reembolso.
 */
public class CuentaBancariaSolicitudDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idCuentaExistente;
    private String banco;
    private String tipoCuenta;
    private String numeroCuenta;
    private String titularNombre;
    private String titularRut;
    private String correoConfirmacion;

    public Integer getIdCuentaExistente() { return idCuentaExistente; }
    public void setIdCuentaExistente(Integer idCuentaExistente) { this.idCuentaExistente = idCuentaExistente; }

    public String getBanco() { return banco; }
    public void setBanco(String banco) { this.banco = banco; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public String getTitularNombre() { return titularNombre; }
    public void setTitularNombre(String titularNombre) { this.titularNombre = titularNombre; }

    public String getTitularRut() { return titularRut; }
    public void setTitularRut(String titularRut) { this.titularRut = titularRut; }

    public String getCorreoConfirmacion() { return correoConfirmacion; }
    public void setCorreoConfirmacion(String correoConfirmacion) { this.correoConfirmacion = correoConfirmacion; }
}
