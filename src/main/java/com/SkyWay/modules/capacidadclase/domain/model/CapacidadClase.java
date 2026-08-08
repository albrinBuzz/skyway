package com.SkyWay.modules.capacidadclase.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


/**
 * The persistent class for the capacidad_clase database table.
 * 
 */
@Entity
@Table(name="capacidad_clase")
@NamedQuery(name="CapacidadClase.findAll", query="SELECT c FROM CapacidadClase c")
public class CapacidadClase implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "capacidad_clase_seq")
	@SequenceGenerator(name = "capacidad_clase_seq",sequenceName = "capacidad_clase_seq",allocationSize = 1)
	@Column(name="id_capacidad_clase")
	private Integer idCapacidadClase;

	private Integer cantidad;

	//bi-directional many-to-one association to Avion
	@ManyToOne
	@JoinColumn(name="id_avion")
	@JsonIgnore
	private Avion avion1;

	//bi-directional many-to-one association to Avion


	//bi-directional many-to-one association to ClaseAsiento
	@ManyToOne
	@JoinColumn(name="id_clase")
	private ClaseAsiento claseAsiento1;

	//bi-directional many-to-one association to ClaseAsiento


	public CapacidadClase() {
	}

	public Integer getIdCapacidadClase() {
		return this.idCapacidadClase;
	}

	public void setIdCapacidadClase(Integer idCapacidadClase) {
		this.idCapacidadClase = idCapacidadClase;
	}

	public Integer getCantidad() {
		return this.cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Avion getAvion1() {
		return this.avion1;
	}

	public void setAvion1(Avion avion1) {
		this.avion1 = avion1;
	}


	public ClaseAsiento getClaseAsiento1() {
		return this.claseAsiento1;
	}

	public void setClaseAsiento1(ClaseAsiento claseAsiento1) {
		this.claseAsiento1 = claseAsiento1;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("CapacidadClase{");
		sb.append("claseAsiento1=").append(claseAsiento1.getDescripcion());
		sb.append(", cantidad=").append(cantidad);
		sb.append('}');
		return sb.toString();
	}
}