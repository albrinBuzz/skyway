package com.SkyWay.modules.reservaitinerario.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import jakarta.persistence.*;


/**
 * The persistent class for the reserva_itinerario database table.
 * 
 */
@Entity
@Table(name="reserva_itinerario")
@NamedQuery(name="ReservaItinerario.findAll", query="SELECT r FROM ReservaItinerario r")
public class ReservaItinerario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "reserva_itinerario_seq")
	@SequenceGenerator(name = "reserva_itinerario_seq",sequenceName = "reserva_itinerario_seq",allocationSize = 1)
	@Column(name="id_reserva_itinerario")
	private Integer idReservaItinerario;

	//bi-directional many-to-one association to Itinerario
	@ManyToOne
	@JoinColumn(name="id_itinerario")
	private Itinerario itinerario;

	//bi-directional many-to-one association to Reserva
	@ManyToOne
	@JoinColumn(name="id_reserva")
	private Reserva reserva;

	// bi-directional many-to-one association to Tarifa
	@ManyToOne
	@JoinColumn(name = "id_itinerario_tarifa", nullable = false)
	private ItinerarioTarifa itinerarioTarifa;

	public ReservaItinerario() {
	}

	public Integer getIdReservaItinerario() {
		return this.idReservaItinerario;
	}

	public void setIdReservaItinerario(Integer idReservaItinerario) {
		this.idReservaItinerario = idReservaItinerario;
	}

	public Itinerario getItinerario() {
		return this.itinerario;
	}

	public void setItinerario(Itinerario itinerario) {
		this.itinerario = itinerario;
	}

	public Reserva getReserva() {
		return this.reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	public ItinerarioTarifa getItinerarioTarifa() {
		return itinerarioTarifa;
	}

	public void setItinerarioTarifa(ItinerarioTarifa itinerarioTarifa) {
		this.itinerarioTarifa = itinerarioTarifa;
	}
}