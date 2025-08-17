package com.SkyWay.modules.pago.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;


/**
 * The persistent class for the pago database table.
 * 
 */
@Entity
@NamedQuery(name="Pago.findAll", query="SELECT p FROM Pago p")
public class Pago implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "pago_seq")
	@SequenceGenerator(name = "pago_seq",sequenceName = "pago_seq",allocationSize = 1)
	@Column(name="id_pago")
	private Integer idPago;

	private Timestamp fecha;

	@Column(name="monto_pagado")
	private BigDecimal montoPagado;

	//bi-directional many-to-one association to MetodoPago
	@ManyToOne
	@JoinColumn(name="id_metodo_pago")
	private MetodoPago metodoPago;

	//bi-directional many-to-one association to Reserva
	@ManyToOne
	@JoinColumn(name="id_reserva")
	private Reserva reserva;

	public Pago() {
	}

	public Integer getIdPago() {
		return this.idPago;
	}

	public void setIdPago(Integer idPago) {
		this.idPago = idPago;
	}

	public Timestamp getFecha() {
		return this.fecha;
	}

	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}

	public BigDecimal getMontoPagado() {
		return this.montoPagado;
	}

	public void setMontoPagado(BigDecimal montoPagado) {
		this.montoPagado = montoPagado;
	}

	public MetodoPago getMetodoPago() {
		return this.metodoPago;
	}

	public void setMetodoPago(MetodoPago metodoPago) {
		this.metodoPago = metodoPago;
	}

	public Reserva getReserva() {
		return this.reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

}