package com.SkyWay.modules.reserva.presentation.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Vista consolidada de un registro transaccional de reembolso para pasajero y administración.
 */
public class ReembolsoPasajeroDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idReembolso;
    private Integer idCancelacion;
    private Integer idReserva;
    private String codigoPnr;
    private Date fechaCancelacion;
    private String motivo;
    private String estadoReembolso;
    private BigDecimal montoReembolso;
    private String metodoPago;
    private String resumenVuelosAsientos;
    private boolean titular;
    private boolean cancelacionAerolinea;

    private String rutSolicitante;
    private String nombreSolicitante;
    private Integer idCuentaReembolso;
    private String banco;
    private String tipoCuenta;
    private String numeroCuenta;
    private String titularNombre;
    private String titularRut;
    private String correoConfirmacion;
    private Date fechaDatosBancarios;
    private Date fechaConfirmacionAdmin;
    private String rutAdminConfirma;
    private Date fechaEjecucion;
    private String rutAdminEjecuta;
    private String codigoOperacion;
    private String observacionAdmin;

    public Integer getIdReembolso() { return idReembolso; }
    public void setIdReembolso(Integer idReembolso) { this.idReembolso = idReembolso; }

    public Integer getIdCancelacion() { return idCancelacion; }
    public void setIdCancelacion(Integer idCancelacion) { this.idCancelacion = idCancelacion; }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getCodigoPnr() { return codigoPnr; }
    public void setCodigoPnr(String codigoPnr) { this.codigoPnr = codigoPnr; }

    public Date getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(Date fechaCancelacion) { this.fechaCancelacion = fechaCancelacion; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstadoReembolso() { return estadoReembolso; }
    public void setEstadoReembolso(String estadoReembolso) { this.estadoReembolso = estadoReembolso; }

    public BigDecimal getMontoReembolso() { return montoReembolso; }
    public void setMontoReembolso(BigDecimal montoReembolso) { this.montoReembolso = montoReembolso; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public String getResumenVuelosAsientos() { return resumenVuelosAsientos; }
    public void setResumenVuelosAsientos(String resumenVuelosAsientos) { this.resumenVuelosAsientos = resumenVuelosAsientos; }

    public boolean isTitular() { return titular; }
    public void setTitular(boolean titular) { this.titular = titular; }

    public boolean isCancelacionAerolinea() { return cancelacionAerolinea; }
    public void setCancelacionAerolinea(boolean cancelacionAerolinea) { this.cancelacionAerolinea = cancelacionAerolinea; }

    public String getRutSolicitante() { return rutSolicitante; }
    public void setRutSolicitante(String rutSolicitante) { this.rutSolicitante = rutSolicitante; }

    public String getNombreSolicitante() { return nombreSolicitante; }
    public void setNombreSolicitante(String nombreSolicitante) { this.nombreSolicitante = nombreSolicitante; }

    public Integer getIdCuentaReembolso() { return idCuentaReembolso; }
    public void setIdCuentaReembolso(Integer idCuentaReembolso) { this.idCuentaReembolso = idCuentaReembolso; }

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

    public Date getFechaDatosBancarios() { return fechaDatosBancarios; }
    public void setFechaDatosBancarios(Date fechaDatosBancarios) { this.fechaDatosBancarios = fechaDatosBancarios; }

    public Date getFechaConfirmacionAdmin() { return fechaConfirmacionAdmin; }
    public void setFechaConfirmacionAdmin(Date fechaConfirmacionAdmin) { this.fechaConfirmacionAdmin = fechaConfirmacionAdmin; }

    public String getRutAdminConfirma() { return rutAdminConfirma; }
    public void setRutAdminConfirma(String rutAdminConfirma) { this.rutAdminConfirma = rutAdminConfirma; }

    public Date getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(Date fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }

    public String getRutAdminEjecuta() { return rutAdminEjecuta; }
    public void setRutAdminEjecuta(String rutAdminEjecuta) { this.rutAdminEjecuta = rutAdminEjecuta; }

    public String getCodigoOperacion() { return codigoOperacion; }
    public void setCodigoOperacion(String codigoOperacion) { this.codigoOperacion = codigoOperacion; }

    public String getObservacionAdmin() { return observacionAdmin; }
    public void setObservacionAdmin(String observacionAdmin) { this.observacionAdmin = observacionAdmin; }
}
