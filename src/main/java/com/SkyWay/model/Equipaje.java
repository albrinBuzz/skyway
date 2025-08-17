package com.SkyWay.model;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.SequenceGenerator;


/**
 * The persistent class for the equipaje database table.
 * 
 */
/*@Entity
@NamedQuery(name="Equipaje.findAll", query="SELECT e FROM Equipaje e")*/
public class Equipaje implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "ciudad_seq")
	@SequenceGenerator(name = "ciudad_seq",sequenceName = "ciudad_seq",allocationSize = 1)
	@Column(name="id_equipaje")
	private Integer idEquipaje;

	private String dimensiones;

	@Column(name="id_reserva")
	private Integer idReserva;

	private BigDecimal peso;

	private String tipo;

	//bi-directional many-to-one association to Pasajero
	@ManyToOne
	@JoinColumn(name="RUT_PASAJERO")
	private Pasajero pasajero;

	public Equipaje() {
	}

	public Integer getIdEquipaje() {
		return this.idEquipaje;
	}

	public void setIdEquipaje(Integer idEquipaje) {
		this.idEquipaje = idEquipaje;
	}

	public String getDimensiones() {
		return this.dimensiones;
	}

	public void setDimensiones(String dimensiones) {
		this.dimensiones = dimensiones;
	}

	public Integer getIdReserva() {
		return this.idReserva;
	}

	public void setIdReserva(Integer idReserva) {
		this.idReserva = idReserva;
	}

	public BigDecimal getPeso() {
		return this.peso;
	}

	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}

	public String getTipo() {
		return this.tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Pasajero getPasajero() {
		return this.pasajero;
	}

	public void setPasajero(Pasajero pasajero) {
		this.pasajero = pasajero;
	}

}