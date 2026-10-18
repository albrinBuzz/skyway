package com.SkyWay.modules.reservaasiento.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import jakarta.persistence.*;

@Entity
@Table(name="reserva_asiento")
@NamedQuery(name="ReservaAsiento.findAll", query="SELECT r FROM ReservaAsiento r")
public class ReservaAsiento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="reserva_asiento_seq")
	@SequenceGenerator(name="reserva_asiento_seq", sequenceName="reserva_asiento_seq", allocationSize=1)
	@Column(name="id_reserva_asiento")
	private Integer idReservaAsiento;

	@Column(name="rut", length=12, nullable=false)
	private String rut;

	// Relación ManyToOne con Pasajero
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="rut", insertable=false, updatable=false)
	private Pasajero pasajero;

	// Relación ManyToOne con Asiento
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="id_asiento")
	private Asiento asiento;

	// Relación ManyToOne con Reserva
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_reserva")
	private Reserva reserva;

	// Relación ManyToOne con Vuelo
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="id_vuelo")
	private Vuelo vuelo;

	public ReservaAsiento() {
	}

	public Integer getIdReservaAsiento() {
		return this.idReservaAsiento;
	}

	public void setIdReservaAsiento(Integer idReservaAsiento) {
		this.idReservaAsiento = idReservaAsiento;
	}

	public String getRut() {
		return this.rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public Pasajero getPasajero() {
		return this.pasajero;
	}

	public void setPasajero(Pasajero pasajero) {
		this.pasajero = pasajero;
		if (pasajero != null) {
			this.rut = pasajero.getRut();
		}
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

	public Vuelo getVuelo() {
		return this.vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}
}