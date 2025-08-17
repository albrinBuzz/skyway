package com.SkyWay.modules.estadovuelo.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import jakarta.persistence.*;
import java.util.List;


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