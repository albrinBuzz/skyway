package com.SkyWay.modules.reembolso.domain.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "reembolso")
public class Reembolso implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "reembolso_seq")
    @SequenceGenerator(name = "reembolso_seq", sequenceName = "reembolso_seq", allocationSize = 1)
    @Column(name = "id_reembolso")
    private Integer idReembolso;

    @Column(name = "id_cancelacion", nullable = false, unique = true)
    private Integer idCancelacion;

    @Column(name = "id_reserva", nullable = false)
    private Integer idReserva;

    @Column(name = "rut_solicitante", nullable = false, length = 12)
    private String rutSolicitante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cuenta_reembolso")
    private CuentaReembolso cuentaReembolso;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "estado", nullable = false, length = 40)
    private String estado = "SOLICITADO";

    @Column(name = "fecha_solicitud", nullable = false)
    private Timestamp fechaSolicitud;

    @Column(name = "fecha_datos_bancarios")
    private Timestamp fechaDatosBancarios;

    @Column(name = "fecha_confirmacion_admin")
    private Timestamp fechaConfirmacionAdmin;

    @Column(name = "rut_admin_confirma", length = 12)
    private String rutAdminConfirma;

    @Column(name = "fecha_ejecucion")
    private Timestamp fechaEjecucion;

    @Column(name = "rut_admin_ejecuta", length = 12)
    private String rutAdminEjecuta;

    @Column(name = "codigo_operacion", length = 80)
    private String codigoOperacion;

    @Column(name = "observacion_admin")
    private String observacionAdmin;

    public Integer getIdReembolso() { return idReembolso; }
    public void setIdReembolso(Integer idReembolso) { this.idReembolso = idReembolso; }

    public Integer getIdCancelacion() { return idCancelacion; }
    public void setIdCancelacion(Integer idCancelacion) { this.idCancelacion = idCancelacion; }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public String getRutSolicitante() { return rutSolicitante; }
    public void setRutSolicitante(String rutSolicitante) { this.rutSolicitante = rutSolicitante; }

    public CuentaReembolso getCuentaReembolso() { return cuentaReembolso; }
    public void setCuentaReembolso(CuentaReembolso cuentaReembolso) { this.cuentaReembolso = cuentaReembolso; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Timestamp getFechaSolicitud() { return fechaSolicitud; }
    public void setFechaSolicitud(Timestamp fechaSolicitud) { this.fechaSolicitud = fechaSolicitud; }

    public Timestamp getFechaDatosBancarios() { return fechaDatosBancarios; }
    public void setFechaDatosBancarios(Timestamp fechaDatosBancarios) { this.fechaDatosBancarios = fechaDatosBancarios; }

    public Timestamp getFechaConfirmacionAdmin() { return fechaConfirmacionAdmin; }
    public void setFechaConfirmacionAdmin(Timestamp fechaConfirmacionAdmin) { this.fechaConfirmacionAdmin = fechaConfirmacionAdmin; }

    public String getRutAdminConfirma() { return rutAdminConfirma; }
    public void setRutAdminConfirma(String rutAdminConfirma) { this.rutAdminConfirma = rutAdminConfirma; }

    public Timestamp getFechaEjecucion() { return fechaEjecucion; }
    public void setFechaEjecucion(Timestamp fechaEjecucion) { this.fechaEjecucion = fechaEjecucion; }

    public String getRutAdminEjecuta() { return rutAdminEjecuta; }
    public void setRutAdminEjecuta(String rutAdminEjecuta) { this.rutAdminEjecuta = rutAdminEjecuta; }

    public String getCodigoOperacion() { return codigoOperacion; }
    public void setCodigoOperacion(String codigoOperacion) { this.codigoOperacion = codigoOperacion; }

    public String getObservacionAdmin() { return observacionAdmin; }
    public void setObservacionAdmin(String observacionAdmin) { this.observacionAdmin = observacionAdmin; }
}
