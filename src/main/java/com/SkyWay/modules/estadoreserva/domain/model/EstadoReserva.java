package com.SkyWay.modules.estadoreserva.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the estado_reserva database table.
 * 
 */
@Entity
@Table(name="estado_reserva")
@NamedQuery(name="EstadoReserva.findAll", query="SELECT e FROM EstadoReserva e")
public class EstadoReserva implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "estado_reserva_seq")
	@SequenceGenerator(name = "estado_reserva_seq",sequenceName = "estado_reserva_seq",allocationSize = 1)
	@Column(name="id_estado_reserva")
	private Integer idEstadoReserva;

	private String descripcion;

	//bi-directional many-to-one association to Reserva
	@OneToMany(mappedBy="estadoReservaBean")
	private List<Reserva> reservas;

	public EstadoReserva() {
	}

	public Integer getIdEstadoReserva() {
		return this.idEstadoReserva;
	}

	public void setIdEstadoReserva(Integer idEstadoReserva) {
		this.idEstadoReserva = idEstadoReserva;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<Reserva> getReservas() {
		return this.reservas;
	}

	public void setReservas(List<Reserva> reservas) {
		this.reservas = reservas;
	}

	public Reserva addReserva(Reserva reserva) {
		getReservas().add(reserva);
		reserva.setEstadoReservaBean(this);

		return reserva;
	}

	public Reserva removeReserva(Reserva reserva) {
		getReservas().remove(reserva);
		reserva.setEstadoReservaBean(null);

		return reserva;
	}

}