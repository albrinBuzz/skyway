package com.SkyWay.modules.pago.domain.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Registro de cada captura hecha en una pasarela. UNIQUE(proveedor, id_externo) impide reutilizar
 * un mismo pago (replay) y sirve para reconciliar cobros sin reserva.
 * Estados: CAPTURADO -> PAGADA (reserva creada) | REEMBOLSADO | REEMBOLSO_PENDIENTE (requiere accion manual).
 */
@Entity
@Table(name = "pago_pasarela",
        uniqueConstraints = @UniqueConstraint(name = "uq_pago_pasarela_externo", columnNames = {"proveedor", "id_externo"}))
public class PagoPasarela implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String CAPTURADO = "CAPTURADO";
    public static final String PAGADA = "PAGADA";
    public static final String REEMBOLSADO = "REEMBOLSADO";
    public static final String REEMBOLSO_PENDIENTE = "REEMBOLSO_PENDIENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pago_pasarela_seq")
    @SequenceGenerator(name = "pago_pasarela_seq", sequenceName = "pago_pasarela_seq", allocationSize = 1)
    @Column(name = "id_pago_pasarela")
    private Integer id;

    @Column(name = "proveedor", nullable = false, length = 20)
    private String proveedor;

    @Column(name = "id_externo", nullable = false, length = 200)
    private String idExterno;

    @Column(name = "orden_compra", length = 64)
    private String ordenCompra;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", length = 3)
    private String moneda;

    @Column(name = "estado", nullable = false, length = 24)
    private String estado;

    @Column(name = "id_reserva")
    private Integer idReserva;

    @Column(name = "fecha_creacion", nullable = false)
    private Timestamp fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private Timestamp fechaActualizacion;

    public Integer getId() { return id; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public String getIdExterno() { return idExterno; }
    public void setIdExterno(String idExterno) { this.idExterno = idExterno; }
    public String getOrdenCompra() { return ordenCompra; }
    public void setOrdenCompra(String ordenCompra) { this.ordenCompra = ordenCompra; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }
    public Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public Timestamp getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(Timestamp fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }
}
