package com.SkyWay.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * The persistent class for the estado_vuelo database table.
 * 
 */
@Entity
@Table(name="estado_vuelo")
@NamedQuery(name="EstadoVuelo.findAll", query="SELECT e FROM EstadoVuelo e")
public class EstadoVuelo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "estado_vuelo_seq")
	@SequenceGenerator(name = "estado_vuelo_seq",sequenceName = "estado_vuelo_seq",allocationSize = 1)
	@Column(name="id_estado_vuelo")
	private Integer idEstadoVuelo;

	private String descripcion;

	private String estado;

	//bi-directional many-to-one association to Vuelo
	@OneToMany(mappedBy="estadoVuelo")
	@JsonIgnore
	private List<Vuelo> vuelos;

	public EstadoVuelo() {
	}

	public Integer getIdEstadoVuelo() {
		return this.idEstadoVuelo;
	}

	public void setIdEstadoVuelo(Integer idEstadoVuelo) {
		this.idEstadoVuelo = idEstadoVuelo;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getEstado() {
		return this.estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public List<Vuelo> getVuelos() {
		return this.vuelos;
	}

	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public Vuelo addVuelo(Vuelo vuelo) {
		getVuelos().add(vuelo);
		vuelo.setEstadoVuelo(this);

		return vuelo;
	}

	public Vuelo removeVuelo(Vuelo vuelo) {
		getVuelos().remove(vuelo);
		vuelo.setEstadoVuelo(null);

		return vuelo;
	}

}