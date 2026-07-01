package com.SkyWay.modules.claseasiento.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.precioasiento.domain.model.PrecioAsiento;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
	@JsonIgnore
	private List<Asiento> asientos;

	//bi-directional many-to-one association to CapacidadClase
	@OneToMany(mappedBy="claseAsiento1")
	@JsonIgnore
	private List<CapacidadClase> capacidadClases1;

	//bi-directional many-to-one association to CapacidadClase


	//bi-directional many-to-one association to PrecioAsiento
	@OneToMany(mappedBy="claseAsiento")
	@JsonIgnore
	private List<PrecioAsiento> precioAsientos;

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

	public List<CapacidadClase> getCapacidadClases1() {
		return this.capacidadClases1;
	}

	public void setCapacidadClases1(List<CapacidadClase> capacidadClases1) {
		this.capacidadClases1 = capacidadClases1;
	}

	public CapacidadClase addCapacidadClases1(CapacidadClase capacidadClases1) {
		getCapacidadClases1().add(capacidadClases1);
		capacidadClases1.setClaseAsiento1(this);

		return capacidadClases1;
	}

	public CapacidadClase removeCapacidadClases1(CapacidadClase capacidadClases1) {
		getCapacidadClases1().remove(capacidadClases1);
		capacidadClases1.setClaseAsiento1(null);

		return capacidadClases1;
	}



	public List<PrecioAsiento> getPrecioAsientos() {
		return this.precioAsientos;
	}

	public void setPrecioAsientos(List<PrecioAsiento> precioAsientos) {
		this.precioAsientos = precioAsientos;
	}

	public PrecioAsiento addPrecioAsiento(PrecioAsiento precioAsiento) {
		getPrecioAsientos().add(precioAsiento);
		precioAsiento.setClaseAsiento(this);

		return precioAsiento;
	}

	public PrecioAsiento removePrecioAsiento(PrecioAsiento precioAsiento) {
		getPrecioAsientos().remove(precioAsiento);
		precioAsiento.setClaseAsiento(null);

		return precioAsiento;
	}

}