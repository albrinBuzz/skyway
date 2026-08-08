package com.SkyWay.modules.checkin.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the checkin database table.
 * 
 */
@Entity
@NamedQuery(name="Checkin.findAll", query="SELECT c FROM Checkin c")
public class Checkin implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "checkin_seq")
	@SequenceGenerator(name = "checkin_seq",sequenceName = "checkin_seq",allocationSize = 1)
	@Column(name="id_checkin")
	private Integer idCheckin;

	@Column(name="fecha_hora")
	private Timestamp fechaHora;

	private String metodo;

	//bi-directional many-to-one association to Reserva
	@ManyToOne
	@JoinColumn(name="id_reserva")
	private Reserva reserva;

	public Checkin() {
	}

	public Integer getIdCheckin() {
		return this.idCheckin;
	}

	public void setIdCheckin(Integer idCheckin) {
		this.idCheckin = idCheckin;
	}

	public Timestamp getFechaHora() {
		return this.fechaHora;
	}

	public void setFechaHora(Timestamp fechaHora) {
		this.fechaHora = fechaHora;
	}

	public String getMetodo() {
		return this.metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public Reserva getReserva() {
		return this.reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

}