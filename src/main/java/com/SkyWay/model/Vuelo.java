package com.SkyWay.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.FutureOrPresent;


/**
 * The persistent class for the vuelo database table.
 * 
 */
@Entity
@NamedQuery(name="Vuelo.findAll", query="SELECT v FROM Vuelo v")
public class Vuelo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "vuelo_seq")
	@SequenceGenerator(name = "vuelo_seq",sequenceName = "vuelo_seq",allocationSize = 1)
	@Column(name="id_vuelo")
	private Integer idVuelo;

	@Column(name="fecha_hora_llegada")
	@FutureOrPresent(message = "La fecha de llegada debe ser hoy o en el futuro.")
	private LocalDateTime fechaHoraLlegada;

	@Column(name="fecha_hora_salida")
	  @FutureOrPresent(message = "La fecha de salida debe ser hoy o en el futuro.")
	private LocalDateTime fechaHoraSalida;

	@Column(name="numero_vuelo")
	private String numeroVuelo;

	private Integer precio;

	//bi-directional many-to-one association to Reserva
	@OneToMany(mappedBy="vuelo",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Reserva> reservas;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="id_aeropuerto_llegada")
	private Aeropuerto aeropuerto1;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="id_aeropuerto_salida")
	private Aeropuerto aeropuerto2;

	//bi-directional many-to-one association to Avion
	@ManyToOne()
	@JoinColumn(name="id_avion")
	private Avion avion;

	//bi-directional many-to-one association to EstadoVuelo
	@ManyToOne()
	@JoinColumn(name="id_estado_vuelo")
	private EstadoVuelo estadoVuelo;

	//bi-directional many-to-one association to Piloto
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="RUT_PILOTO")

	private Piloto piloto;

	@JsonIgnore
	@OneToMany(mappedBy="vuelo",fetch = FetchType.LAZY,cascade = CascadeType.ALL)
	private List<PrecioAsiento> precios;

	public Vuelo() {
	}

	public Integer getIdVuelo() {
		return this.idVuelo;
	}

	public void setIdVuelo(Integer idVuelo) {
		this.idVuelo = idVuelo;
	}

	public LocalDateTime getFechaHoraLlegada() {
		return this.fechaHoraLlegada;
	}

	public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) {
		this.fechaHoraLlegada = fechaHoraLlegada;
	}

	public LocalDateTime getFechaHoraSalida() {
		return this.fechaHoraSalida;
	}

	public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
		this.fechaHoraSalida = fechaHoraSalida;
	}

	public String getNumeroVuelo() {
		return this.numeroVuelo;
	}

	public void setNumeroVuelo(String numeroVuelo) {
		this.numeroVuelo = numeroVuelo;
	}

	public Integer getPrecio() {
		return this.precio;
	}

	public void setPrecio(Integer precio) {
		this.precio = precio;
	}

	public int cantidadPasajeros() {
	return	reservas.size();
	}
	
	public List<Reserva> getReservas() {
		return this.reservas;
	}

	public void setReservas(List<Reserva> reservas) {
		this.reservas = reservas;
	}

	public Reserva addReserva(Reserva reserva) {
		getReservas().add(reserva);
		reserva.setVuelo(this);

		return reserva;
	}

	public Reserva removeReserva(Reserva reserva) {
		getReservas().remove(reserva);
		reserva.setVuelo(null);

		return reserva;
	}

	public Aeropuerto getAeropuerto1() {
		return this.aeropuerto1;
	}

	public void setAeropuerto1(Aeropuerto aeropuerto1) {
		this.aeropuerto1 = aeropuerto1;
	}

	public Aeropuerto getAeropuerto2() {
		return this.aeropuerto2;
	}

	public void setAeropuerto2(Aeropuerto aeropuerto2) {
		this.aeropuerto2 = aeropuerto2;
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

	public List<PrecioAsiento> getPrecios() {
		return precios;
	}


	public void setPrecios(List<PrecioAsiento> precios) {
		this.precios = precios;
	}

	@Override
	public String toString() {
		return "Vuelo [idVuelo=" + idVuelo + ", fechaHoraLlegada=" + fechaHoraLlegada + ", fechaHoraSalida="
				+ fechaHoraSalida + ", numeroVuelo=" + numeroVuelo + ", precio=" + precio + ", reservas=" + reservas
				+ ", aeropuerto1=" + aeropuerto1 + ", aeropuerto2=" + aeropuerto2 + ", avion=" + avion
				+ ", estadoVuelo=" + estadoVuelo + ", piloto=" + piloto + "]";
	}


	
	

	
	
}