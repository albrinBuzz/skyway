package com.SkyWay.modules.vuelo.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.turno.domain.model.Turno;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the vuelo database table.
 * 
 */
@Entity
@NamedQuery(name="Vuelo.findAll", query="SELECT v FROM Vuelo v")
public class Vuelo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vuelo_seq_gen")
	@SequenceGenerator(name = "vuelo_seq_gen", sequenceName = "vuelo_seq", allocationSize = 1)
	@Column(name = "id_vuelo")
	private Integer idVuelo;


	@Column(name="fecha_hora_llegada")
	private Timestamp fechaHoraLlegada;

	@Column(name="fecha_hora_salida")
	private Timestamp fechaHoraSalida;

	@Column(name="numero_vuelo")
	private String numeroVuelo;

	//bi-directional many-to-one association to ItinerarioVuelo
	@JsonIgnore
	@OneToMany(mappedBy="vuelo")
	private List<ItinerarioVuelo> itinerarioVuelos;

	//bi-directional many-to-one association to PrecioAsiento
	@JsonIgnore
	@OneToMany(mappedBy="vuelo")
	private List<PrecioAsiento> precioAsientos;

	//bi-directional many-to-one association to ReservaAsiento
	@JsonIgnore
	@OneToMany(mappedBy="vuelo")
	private List<ReservaAsiento> reservaAsientos;

	@JsonIgnore
	//bi-directional many-to-one association to SegmentoVuelo
	@OneToMany(mappedBy="vuelo", fetch = FetchType.EAGER)
	private List<SegmentoVuelo> segmentoVuelos;

	//bi-directional many-to-one association to Turno
	@JsonIgnore
	@OneToMany(mappedBy="vuelo")
	private List<Turno> turnos;

	//bi-directional many-to-one association to Aerolinea
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="id_aerolinea")
	private Aerolinea aerolinea;

	//bi-directional many-to-one association to Avion
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="id_avion")
	private Avion avion;

	//bi-directional many-to-one association to EstadoVuelo
	@ManyToOne
	@JoinColumn(name="id_estado_vuelo")
	private EstadoVuelo estadoVuelo;

	//bi-directional many-to-one association to Piloto
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name="rut_piloto")
	private Piloto piloto;

	public Vuelo() {
	}

	public Vuelo(String number, Date date, String aer1, String aer2) {

	}


	public Integer getIdVuelo() {
		return this.idVuelo;
	}

	public void setIdVuelo(Integer idVuelo) {
		this.idVuelo = idVuelo;
	}

	public Timestamp getFechaHoraLlegada() {
		return this.fechaHoraLlegada;
	}

	public void setFechaHoraLlegada(Timestamp fechaHoraLlegada) {
		this.fechaHoraLlegada = fechaHoraLlegada;
	}

	public Timestamp getFechaHoraSalida() {
		return this.fechaHoraSalida;
	}

	public void setFechaHoraSalida(Timestamp fechaHoraSalida) {
		this.fechaHoraSalida = fechaHoraSalida;
	}

	public String getNumeroVuelo() {
		return this.numeroVuelo;
	}

	public void setNumeroVuelo(String numeroVuelo) {
		this.numeroVuelo = numeroVuelo;
	}

	public List<ItinerarioVuelo> getItinerarioVuelos() {
		return this.itinerarioVuelos;
	}

	public void setItinerarioVuelos(List<ItinerarioVuelo> itinerarioVuelos) {
		this.itinerarioVuelos = itinerarioVuelos;
	}

	public ItinerarioVuelo addItinerarioVuelo(ItinerarioVuelo itinerarioVuelo) {
		getItinerarioVuelos().add(itinerarioVuelo);
		itinerarioVuelo.setVuelo(this);

		return itinerarioVuelo;
	}

	public ItinerarioVuelo removeItinerarioVuelo(ItinerarioVuelo itinerarioVuelo) {
		getItinerarioVuelos().remove(itinerarioVuelo);
		itinerarioVuelo.setVuelo(null);

		return itinerarioVuelo;
	}

	public List<PrecioAsiento> getPrecioAsientos() {
		return this.precioAsientos;
	}

	public void setPrecioAsientos(List<PrecioAsiento> precioAsientos) {
		this.precioAsientos = precioAsientos;
	}

	public PrecioAsiento addPrecioAsiento(PrecioAsiento precioAsiento) {
		getPrecioAsientos().add(precioAsiento);
		precioAsiento.setVuelo(this);

		return precioAsiento;
	}

	public PrecioAsiento removePrecioAsiento(PrecioAsiento precioAsiento) {
		getPrecioAsientos().remove(precioAsiento);
		precioAsiento.setVuelo(null);

		return precioAsiento;
	}

	public List<ReservaAsiento> getReservaAsientos() {
		return this.reservaAsientos;
	}

	public void setReservaAsientos(List<ReservaAsiento> reservaAsientos) {
		this.reservaAsientos = reservaAsientos;
	}

	public ReservaAsiento addReservaAsiento(ReservaAsiento reservaAsiento) {
		getReservaAsientos().add(reservaAsiento);
		reservaAsiento.setVuelo(this);

		return reservaAsiento;
	}

	public ReservaAsiento removeReservaAsiento(ReservaAsiento reservaAsiento) {
		getReservaAsientos().remove(reservaAsiento);
		reservaAsiento.setVuelo(null);

		return reservaAsiento;
	}

	public List<SegmentoVuelo> getSegmentoVuelos() {
		return this.segmentoVuelos;
	}

	public void setSegmentoVuelos(List<SegmentoVuelo> segmentoVuelos) {
		this.segmentoVuelos = segmentoVuelos;
	}

	public SegmentoVuelo addSegmentoVuelo(SegmentoVuelo segmentoVuelo) {
		getSegmentoVuelos().add(segmentoVuelo);
		segmentoVuelo.setVuelo(this);

		return segmentoVuelo;
	}

	public SegmentoVuelo removeSegmentoVuelo(SegmentoVuelo segmentoVuelo) {
		getSegmentoVuelos().remove(segmentoVuelo);
		segmentoVuelo.setVuelo(null);

		return segmentoVuelo;
	}

	public List<Turno> getTurnos() {
		return this.turnos;
	}

	public void setTurnos(List<Turno> turnos) {
		this.turnos = turnos;
	}

	public Turno addTurno(Turno turno) {
		getTurnos().add(turno);
		turno.setVuelo(this);

		return turno;
	}

	public Turno removeTurno(Turno turno) {
		getTurnos().remove(turno);
		turno.setVuelo(null);

		return turno;
	}

	public Aerolinea getAerolinea() {
		return this.aerolinea;
	}

	public void setAerolinea(Aerolinea aerolinea) {
		this.aerolinea = aerolinea;
	}

	public Avion getAvion() {
		return this.avion;
	}

	public void setAvion(Avion avion) {
		this.avion = avion;
	}

	public EstadoVuelo getEstadoVuelo() {
		return this.estadoVuelo;
	}

	public void setEstadoVuelo(EstadoVuelo estadoVuelo) {
		this.estadoVuelo = estadoVuelo;
	}

	public Piloto getPiloto() {
		return this.piloto;
	}

	public void setPiloto(Piloto piloto) {
		this.piloto = piloto;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Vuelo{");
		sb.append("idVuelo=").append(idVuelo);
		sb.append(", numeroVuelo='").append(numeroVuelo).append('\'');
		sb.append(", avion=").append(avion);
		sb.append(", piloto=").append(piloto);
		sb.append('}');
		return sb.toString();
	}
}