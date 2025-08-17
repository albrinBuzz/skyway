package com.SkyWay.modules.equipaje.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import jakarta.persistence.*;
import java.math.BigDecimal;


/**
 * The persistent class for the equipaje database table.
 * 
 */
@Entity
@NamedQuery(name="Equipaje.findAll", query="SELECT e FROM Equipaje e")
public class Equipaje implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "ciudad_seq")
	@SequenceGenerator(name = "ciudad_seq",sequenceName = "ciudad_seq",allocationSize = 1)
	@Column(name="id_equipaje")
	private Integer idEquipaje;

	private String dimensiones;

	private BigDecimal peso;

	private String tipo;

	//bi-directional many-to-one association to Pasajero
	@ManyToOne
	@JoinColumn(name="rut_pasajero")
	private Pasajero pasajero;

	//bi-directional many-to-one association to Reserva
	@ManyToOne
	@JoinColumn(name="id_reserva")
	private Reserva reserva;

	//bi-directional many-to-one association to TipoEquipaje
	@ManyToOne
	@JoinColumn(name="id_tipo")
	private TipoEquipaje tipoEquipaje;

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

	public Reserva getReserva() {
		return this.reserva;
	}

	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}

	public TipoEquipaje getTipoEquipaje() {
		return this.tipoEquipaje;
	}

	public void setTipoEquipaje(TipoEquipaje tipoEquipaje) {
		this.tipoEquipaje = tipoEquipaje;
	}

}