package com.SkyWay.modules.aerolinea.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the aerolinea database table.
 * 
 */
@Entity
@NamedQuery(name="Aerolinea.findAll", query="SELECT a FROM Aerolinea a")
public class Aerolinea implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "aerolinea_seq")
	@SequenceGenerator(name = "aerolinea_seq",sequenceName = "aerolinea_seq",allocationSize = 1)
	@Column(name="id_aerolinea")
	private Integer idAerolinea;

	private String codigo;

	private String nombre;

	//bi-directional many-to-one association to Vuelo
	@OneToMany(mappedBy="aerolinea")
	private List<Vuelo> vuelos;

	public Aerolinea() {
	}

	public Integer getIdAerolinea() {
		return this.idAerolinea;
	}

	public void setIdAerolinea(Integer idAerolinea) {
		this.idAerolinea = idAerolinea;
	}

	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Vuelo> getVuelos() {
		return this.vuelos;
	}

	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public Vuelo addVuelo(Vuelo vuelo) {
		getVuelos().add(vuelo);
		vuelo.setAerolinea(this);

		return vuelo;
	}

	public Vuelo removeVuelo(Vuelo vuelo) {
		getVuelos().remove(vuelo);
		vuelo.setAerolinea(null);

		return vuelo;
	}

}