package com.SkyWay.modules.itinerario.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.io.Serializable;
import java.time.Duration;
import java.sql.Timestamp;
import java.util.List;



/**
 * The persistent class for the itinerario database table.
 *
 */
@Entity
@NamedQuery(name="Itinerario.findAll", query="SELECT i FROM Itinerario i")
public class Itinerario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "itinerario_seq")
	@SequenceGenerator(name = "itinerario_seq",sequenceName = "itinerario_seq",allocationSize = 1)
	@Column(name="id_itinerario")
	private Integer idItinerario;

	/*@Column(name="duracion_total",columnDefinition = "interval")
	private Duration duracionTotal;  // Usamos Duration en lugar de Object*/

	@Column(name="fecha_creacion")
	private Timestamp fechaCreacion;

	@Column(name="hora_llegada")
	private Timestamp horaLlegada;

	@Column(name="hora_salida")
	private Timestamp horaSalida;

	@Column(name="numero_escalas")
	private Integer numeroEscalas;

	@Column(name="precio_base")
	private Integer precioBase;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="destino_aeropuerto")
	private Aeropuerto aeropuertoDestino;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="origen_aeropuerto")
	private Aeropuerto aeropuertoOrigen;



	//bi-directional many-to-one association to ItinerarioVuelo
	@OneToMany(mappedBy="itinerario",fetch = FetchType.EAGER)
	private List<ItinerarioVuelo> itinerarioVuelos;

	//bi-directional many-to-one association to ReservaItinerario
	@OneToMany(mappedBy="itinerario")
	private List<ReservaItinerario> reservaItinerarios;

	public Itinerario() {
	}

	public Integer getIdItinerario() {
		return this.idItinerario;
	}

	public void setIdItinerario(Integer idItinerario) {
		this.idItinerario = idItinerario;
	}

	/*public void setDuracionTotal(Duration duracionTotal) {
		this.duracionTotal = duracionTotal;
	}

	public Duration getDuracionTotal() {
		return duracionTotal;
	}*/

	public Timestamp getFechaCreacion() {
		return this.fechaCreacion;
	}

	public void setFechaCreacion(Timestamp fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Timestamp getHoraLlegada() {
		return this.horaLlegada;
	}

	public void setHoraLlegada(Timestamp horaLlegada) {
		this.horaLlegada = horaLlegada;
	}

	public Timestamp getHoraSalida() {
		return this.horaSalida;
	}

	public void setHoraSalida(Timestamp horaSalida) {
		this.horaSalida = horaSalida;
	}

	public Integer getNumeroEscalas() {
		return this.numeroEscalas;
	}

	public void setNumeroEscalas(Integer numeroEscalas) {
		this.numeroEscalas = numeroEscalas;
	}

	public Integer getPrecioBase() {
		return this.precioBase;
	}

	public void setPrecioBase(Integer precioBase) {
		this.precioBase = precioBase;
	}

	public Aeropuerto getAeropuertoDestino() {
		return aeropuertoDestino;
	}

	public void setAeropuertoDestino(Aeropuerto aeropuertoDestino) {
		this.aeropuertoDestino = aeropuertoDestino;
	}

	public Aeropuerto getAeropuertoOrigen() {
		return aeropuertoOrigen;
	}

	public void setAeropuertoOrigen(Aeropuerto aeropuertoOrigen) {
		this.aeropuertoOrigen = aeropuertoOrigen;
	}

	public List<ItinerarioVuelo> getItinerarioVuelos() {
		return this.itinerarioVuelos;
	}

	public void setItinerarioVuelos(List<ItinerarioVuelo> itinerarioVuelos) {
		this.itinerarioVuelos = itinerarioVuelos;
	}

	public ItinerarioVuelo addItinerarioVuelo(ItinerarioVuelo itinerarioVuelo) {
		getItinerarioVuelos().add(itinerarioVuelo);
		itinerarioVuelo.setItinerario(this);

		return itinerarioVuelo;
	}

	public ItinerarioVuelo removeItinerarioVuelo(ItinerarioVuelo itinerarioVuelo) {
		getItinerarioVuelos().remove(itinerarioVuelo);
		itinerarioVuelo.setItinerario(null);

		return itinerarioVuelo;
	}

	public List<ReservaItinerario> getReservaItinerarios() {
		return this.reservaItinerarios;
	}

	public void setReservaItinerarios(List<ReservaItinerario> reservaItinerarios) {
		this.reservaItinerarios = reservaItinerarios;
	}

	public ReservaItinerario addReservaItinerario(ReservaItinerario reservaItinerario) {
		getReservaItinerarios().add(reservaItinerario);
		reservaItinerario.setItinerario(this);

		return reservaItinerario;
	}

	public ReservaItinerario removeReservaItinerario(ReservaItinerario reservaItinerario) {
		getReservaItinerarios().remove(reservaItinerario);
		reservaItinerario.setItinerario(null);

		return reservaItinerario;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Itinerario{");
		sb.append("idItinerario=").append(idItinerario);
		sb.append(", fechaCreacion=").append(fechaCreacion);
		sb.append(", horaLlegada=").append(horaLlegada);
		sb.append(", horaSalida=").append(horaSalida);
		sb.append(", numeroEscalas=").append(numeroEscalas);
		sb.append(", precioBase=").append(precioBase);
		sb.append('}');
		return sb.toString();
	}
}