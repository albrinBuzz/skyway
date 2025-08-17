package com.SkyWay.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * The persistent class for the reserva_asiento database table.
 * 
 */
/*@Entity
@Table(name="reserva_asiento")
@NamedQuery(name="ReservaAsiento.findAll", query="SELECT r FROM ReservaAsiento r")*/
public class ReservaAsiento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "reserva_asiento_seq")
	@SequenceGenerator(name = "reserva_asiento_seq",sequenceName = "reserva_asiento_seq",allocationSize = 1)
	@Column(name="id_reserva_asiento")
	private Integer idReservaAsiento;

	//bi-directional many-to-one association to Asiento
	@ManyToOne
	@JoinColumn(name="id_asiento")
	private Asiento asiento;

	//bi-directional many-to-one association to Reserva
	@ManyToOne
	@JoinColumn(name="id_reserva")
	private Reserva reserva;

	public ReservaAsiento() {
	}

	public Integer getIdReservaAsiento() {
		return this.idReservaAsiento;
	}

	public void setIdReservaAsiento(Integer idReservaAsiento) {
		this.idReservaAsiento = idReservaAsiento;
	}

	public Asiento getAsiento() {
		return this.asiento;
	}

	public void setAsiento(Asiento asiento) {
		this.asiento = asiento;
	}

	public Reserva getReserva() {
		return this.reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	@Override
	public String toString() {
		return "ReservaAsiento [" + asiento + "]";
	}

	
	
}