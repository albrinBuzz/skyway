package com.SkyWay.modules.reembolso.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "cuenta_reembolso")
public class CuentaReembolso implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cuenta_reembolso_seq")
    @SequenceGenerator(name = "cuenta_reembolso_seq", sequenceName = "cuenta_reembolso_seq", allocationSize = 1)
    @Column(name = "id_cuenta_reembolso")
    private Integer idCuentaReembolso;

    @Column(name = "rut_pasajero", nullable = false, length = 12)
    private String rutPasajero;

    @Column(name = "banco", nullable = false, length = 100)
    private String banco;

    @Column(name = "tipo_cuenta", nullable = false, length = 50)
    private String tipoCuenta;

    @Column(name = "numero_cuenta", nullable = false, length = 50)
    private String numeroCuenta;

    @Column(name = "titular_nombre", nullable = false, length = 150)
    private String titularNombre;

    @Column(name = "titular_rut", nullable = false, length = 20)
    private String titularRut;

    @Column(name = "correo_confirmacion", length = 150)
    private String correoConfirmacion;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private Timestamp fechaRegistro;

    public Integer getIdCuentaReembolso() { return idCuentaReembolso; }
    public void setIdCuentaReembolso(Integer idCuentaReembolso) { this.idCuentaReembolso = idCuentaReembolso; }

    public String getRutPasajero() { return rutPasajero; }
    public void setRutPasajero(String rutPasajero) { this.rutPasajero = rutPasajero; }

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

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
