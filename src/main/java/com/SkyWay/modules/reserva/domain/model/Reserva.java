package com.SkyWay.modules.reserva.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.checkin.domain.model.Checkin;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the reserva database table.
 * 
 */
@Entity
@NamedQuery(name="Reserva.findAll", query="SELECT r FROM Reserva r")
public class Reserva implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "reserva_seq")
	@SequenceGenerator(name = "reserva_seq",sequenceName = "reserva_seq",allocationSize = 1)
	@Column(name="id_reserva")
	private Integer idReserva;

	@Column(name="fecha_reserva")
	private Timestamp fechaReserva;

	private BigDecimal total;

	//bi-directional many-to-one association to Checkin
	@OneToMany(mappedBy="reserva",fetch = FetchType.LAZY)
	private List<Checkin> checkins;

	//bi-directional many-to-one association to Equipaje
	@OneToMany(mappedBy="reserva",fetch = FetchType.LAZY)
	private List<Equipaje> equipajes;

	//bi-directional many-to-one association to Pago
	@OneToMany(mappedBy="reserva",fetch = FetchType.LAZY)
	private List<Pago> pagos;

	//bi-directional many-to-one association to EstadoReserva
	@ManyToOne
	@JoinColumn(name="estado_reserva")
	private EstadoReserva estadoReservaBean;

	//bi-directional many-to-one association to Pasajero
	@ManyToOne
	@JoinColumn(name="rut_pasajero")
	private Pasajero pasajero;

	//bi-directional many-to-one association to ReservaAsiento
	@OneToMany(mappedBy="reserva",fetch = FetchType.LAZY)
	private List<ReservaAsiento> reservaAsientos;

	//bi-directional many-to-one association to ReservaItinerario
	@OneToMany(mappedBy="reserva",fetch = FetchType.LAZY)
	private List<ReservaItinerario> reservaItinerarios;

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

	public BigDecimal getTotal() {
		return this.total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public List<Checkin> getCheckins() {
		return this.checkins;
	}

	public void setCheckins(List<Checkin> checkins) {
		this.checkins = checkins;
	}

	public Checkin addCheckin(Checkin checkin) {
		getCheckins().add(checkin);
		checkin.setReserva(this);

		return checkin;
	}

	public Checkin removeCheckin(Checkin checkin) {
		getCheckins().remove(checkin);
		checkin.setReserva(null);

		return checkin;
	}

	public List<Equipaje> getEquipajes() {
		return this.equipajes;
	}

	public void setEquipajes(List<Equipaje> equipajes) {
		this.equipajes = equipajes;
	}

	public Equipaje addEquipaje(Equipaje equipaje) {
		getEquipajes().add(equipaje);
		equipaje.setReserva(this);

		return equipaje;
	}

	public Equipaje removeEquipaje(Equipaje equipaje) {
		getEquipajes().remove(equipaje);
		equipaje.setReserva(null);

		return equipaje;
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

	public List<ReservaItinerario> getReservaItinerarios() {
		return this.reservaItinerarios;
	}

	public void setReservaItinerarios(List<ReservaItinerario> reservaItinerarios) {
		this.reservaItinerarios = reservaItinerarios;
	}

	public ReservaItinerario addReservaItinerario(ReservaItinerario reservaItinerario) {
		getReservaItinerarios().add(reservaItinerario);
		reservaItinerario.setReserva(this);

		return reservaItinerario;
	}

	public ReservaItinerario removeReservaItinerario(ReservaItinerario reservaItinerario) {
		getReservaItinerarios().remove(reservaItinerario);
		reservaItinerario.setReserva(null);

		return reservaItinerario;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Reserva{");
		sb.append("idReserva=").append(idReserva);
		sb.append(", fechaReserva=").append(fechaReserva);
		sb.append(", total=").append(total);
		sb.append('}');
		return sb.toString();
	}
}