package com.SkyWay.modules.precioasiento.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import jakarta.persistence.*;


/**
 * The persistent class for the precio_asiento database table.
 * 
 */
@Entity
@Table(name="precio_asiento")
@NamedQuery(name="PrecioAsiento.findAll", query="SELECT p FROM PrecioAsiento p")
public class PrecioAsiento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "precio_asiento_seq")
	@SequenceGenerator(name = "precio_asiento_seq",sequenceName = "precio_asiento_seq",allocationSize = 1)
	@Column(name="id_precio_asiento")
	private Integer idPrecioAsiento;

	private Integer precio;

	//bi-directional many-to-one association to ClaseAsiento
	@ManyToOne
	@JoinColumn(name="id_clase")
	private ClaseAsiento claseAsiento;

	//bi-directional many-to-one association to Vuelo
	@ManyToOne
	@JoinColumn(name="id_vuelo")
	private Vuelo vuelo;

	public PrecioAsiento() {
	}

	public Integer getIdPrecioAsiento() {
		return this.idPrecioAsiento;
	}

	public void setIdPrecioAsiento(Integer idPrecioAsiento) {
		this.idPrecioAsiento = idPrecioAsiento;
	}

	public Integer getPrecio() {
		return this.precio;
	}

	public void setPrecio(Integer precio) {
		this.precio = precio;
	}

	public ClaseAsiento getClaseAsiento() {
		return this.claseAsiento;
	}

	public void setClaseAsiento(ClaseAsiento claseAsiento) {
		this.claseAsiento = claseAsiento;
	}

	public Vuelo getVuelo() {
		return this.vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}

}