package com.SkyWay.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;


/**
 * The persistent class for the reserva database table.
 * 
 */
/*@Entity
@NamedQuery(name="Reserva.findAll", query="SELECT r FROM Reserva r")*/
public class Reserva implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "reserva_seq")
	@SequenceGenerator(name = "reserva_seq",sequenceName = "reserva_seq",allocationSize = 1)
	@Column(name="id_reserva")
	private Integer idReserva;

	@Column(name="fecha_reserva")
	private Timestamp fechaReserva;

	//bi-directional many-to-one association to Pago
	@OneToMany(mappedBy="reserva",cascade = CascadeType.ALL)
	private List<Pago> pagos;

	//bi-directional many-to-one association to EstadoReserva
	@ManyToOne
	@JoinColumn(name="estado_reserva")
	private EstadoReserva estadoReservaBean;

	//bi-directional many-to-one association to Pasajero
	@ManyToOne
	@JoinColumn(name="RUT_PASAJERO")
	private Pasajero pasajero;

	//bi-directional many-to-one association to Vuelo
	@ManyToOne
	@JoinColumn(name="id_vuelo")
	@JsonBackReference
	private Vuelo vuelo;

	//bi-directional many-to-one association to ReservaAsiento
	@OneToMany(mappedBy="reserva",cascade = CascadeType.ALL)
	private List<ReservaAsiento> reservaAsientos;

	public Reserva() {
	}

	public Integer getIdReserva() {
		return this.idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public Timestamp getFechaReserva() {
		return this.fechaReserva;
	}

	public void setFechaReserva(Timestamp fechaReserva) {
		this.fechaReserva = fechaReserva;
	}

	public List<Pago> getPagos() {
		return this.pagos;
	}

	public void setPagos(List<Pago> pagos) {
		this.pagos = pagos;
	}

	public Pago addPago(Pago pago) {
		getPagos().add(pago);
		pago.setReserva(this);

		return pago;
	}

	public Pago removePago(Pago pago) {
		getPagos().remove(pago);
		pago.setReserva(null);

		return pago;
	}

	public EstadoReserva getEstadoReservaBean() {
		return this.estadoReservaBean;
	}

	public void setEstadoReservaBean(EstadoReserva estadoReservaBean) {
		this.estadoReservaBean = estadoReservaBean;
	}

	public Pasajero getPasajero() {
		return this.pasajero;
	}

	public void setPasajero(Pasajero pasajero) {
		this.pasajero = pasajero;
	}

	public Vuelo getVuelo() {
		return this.vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}

	public List<ReservaAsiento> getReservaAsientos() {
		return this.reservaAsientos;
	}

	public void setReservaAsientos(List<ReservaAsiento> reservaAsientos) {
		this.reservaAsientos = reservaAsientos;
	}

	public ReservaAsiento addReservaAsiento(ReservaAsiento reservaAsiento) {
		getReservaAsientos().add(reservaAsiento);
		reservaAsiento.setReserva(this);

		return reservaAsiento;
	}

	public ReservaAsiento removeReservaAsiento(ReservaAsiento reservaAsiento) {
		getReservaAsientos().remove(reservaAsiento);
		reservaAsiento.setReserva(null);

		return reservaAsiento;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Reserva{");
		sb.append("idReserva=").append(idReserva);
		sb.append(", fechaReserva=").append(fechaReserva);
		sb.append(", pasajero=").append(pasajero.toString());
		sb.append('}');
		return sb.toString();
	}
}