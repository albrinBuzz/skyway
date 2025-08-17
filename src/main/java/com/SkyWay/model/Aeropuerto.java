package com.SkyWay.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;


/**
 * The persistent class for the aeropuerto database table.
 * 
 */
//@Entity
//@NamedQuery(name="Aeropuerto.findAll", query="SELECT a FROM Aeropuerto a")
//@Table(name = "aeropuerto")
public class Aeropuerto implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "aeropuerto_seq")
	@SequenceGenerator(name = "aeropuerto_seq",sequenceName = "aeropuerto_seq",allocationSize = 1)
	@Column(name="id_aeropuerto")
	private Integer idAeropuerto;

	@Column(name="codigo_iata")
	private String codigoIata;

	@Column(name="nombre_aeropuerto")
	private String nombreAeropuerto;

	//bi-directional many-to-one association to Ciudad
	@ManyToOne
	@JoinColumn(name="ciudad")
	@JsonIgnore
	private Ciudad ciudadBean;

	//bi-directional many-to-one association to Vuelo
	@OneToMany(mappedBy="aeropuerto1")
	@JsonIgnore
	private List<Vuelo> vuelos1;

	//bi-directional many-to-one association to Vuelo
	@OneToMany(mappedBy="aeropuerto2")
	@JsonIgnore
	private List<Vuelo> vuelos2;

	public Aeropuerto() {
	}

	public Integer getIdAeropuerto() {
		return this.idAeropuerto;
	}

	public void setIdAeropuerto(Integer idAeropuerto) {
		this.idAeropuerto = idAeropuerto;
	}

	public String getCodigoIata() {
		return this.codigoIata;
	}

	public void setCodigoIata(String codigoIata) {
		this.codigoIata = codigoIata;
	}

	public String getNombreAeropuerto() {
		return this.nombreAeropuerto;
	}

	public void setNombreAeropuerto(String nombreAeropuerto) {
		this.nombreAeropuerto = nombreAeropuerto;
	}

	public Ciudad getCiudadBean() {
		return this.ciudadBean;
	}

	public void setCiudadBean(Ciudad ciudadBean) {
		this.ciudadBean = ciudadBean;
	}

	public List<Vuelo> getVuelos1() {
		return this.vuelos1;
	}

	public void setVuelos1(List<Vuelo> vuelos1) {
		this.vuelos1 = vuelos1;
	}

	public Vuelo addVuelos1(Vuelo vuelos1) {
		getVuelos1().add(vuelos1);
		vuelos1.setAeropuerto1(this);

		return vuelos1;
	}

	public Vuelo removeVuelos1(Vuelo vuelos1) {
		getVuelos1().remove(vuelos1);
		vuelos1.setAeropuerto1(null);

		return vuelos1;
	}

	public List<Vuelo> getVuelos2() {
		return this.vuelos2;
	}

	public void setVuelos2(List<Vuelo> vuelos2) {
		this.vuelos2 = vuelos2;
	}

	public Vuelo addVuelos2(Vuelo vuelos2) {
		getVuelos2().add(vuelos2);
		vuelos2.setAeropuerto2(this);

		return vuelos2;
	}

	public Vuelo removeVuelos2(Vuelo vuelos2) {
		getVuelos2().remove(vuelos2);
		vuelos2.setAeropuerto2(null);

		return vuelos2;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Aeropuerto that = (Aeropuerto) o;
		return Objects.equals(idAeropuerto, that.idAeropuerto) && Objects.equals(codigoIata, that.codigoIata) && Objects.equals(nombreAeropuerto, that.nombreAeropuerto);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idAeropuerto, codigoIata, nombreAeropuerto);
	}

	@Override
	public String toString() {
		return "Aeropuerto [codigoIata=" + codigoIata + ", nombreAeropuerto=" + nombreAeropuerto + "]";
	}
	
	

}