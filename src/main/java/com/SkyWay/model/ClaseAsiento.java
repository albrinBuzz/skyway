package com.SkyWay.model;

import java.io.Serializable;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the clase_asiento database table.
 * 
 */
@Entity
@Table(name="clase_asiento")
@NamedQuery(name="ClaseAsiento.findAll", query="SELECT c FROM ClaseAsiento c")
public class ClaseAsiento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "clase_asiento_seq")
	@SequenceGenerator(name = "clase_asiento_seq",sequenceName = "clase_asiento_seq",allocationSize = 1)
	@Column(name="id_clase")
	private Integer idClase;

	private String descripcion;

	//bi-directional many-to-one association to Asiento
	@OneToMany(mappedBy="claseAsiento")
	private List<Asiento> asientos;

	public ClaseAsiento() {
	}

	public Integer getIdClase() {
		return this.idClase;
	}

	public void setIdClase(Integer idClase) {
		this.idClase = idClase;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public List<Asiento> getAsientos() {
		return this.asientos;
	}

	public void setAsientos(List<Asiento> asientos) {
		this.asientos = asientos;
	}

	public Asiento addAsiento(Asiento asiento) {
		getAsientos().add(asiento);
		asiento.setClaseAsiento(this);

		return asiento;
	}

	public Asiento removeAsiento(Asiento asiento) {
		getAsientos().remove(asiento);
		asiento.setClaseAsiento(null);

		return asiento;
	}

}