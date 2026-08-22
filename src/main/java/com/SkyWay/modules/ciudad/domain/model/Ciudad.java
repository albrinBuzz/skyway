package com.SkyWay.modules.ciudad.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.pai.domain.model.Pai;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the ciudad database table.
 * 
 */
@Entity
@NamedQuery(name="Ciudad.findAll", query="SELECT c FROM Ciudad c")
public class Ciudad implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "ciudad_seq")
	@SequenceGenerator(name = "ciudad_seq",sequenceName = "ciudad_seq",allocationSize = 1)
	@Column(name="id_ciudad")
	private Integer idCiudad;

	private String nombre;

	//bi-directional many-to-one association to Aeropuerto
	@OneToMany(mappedBy="ciudad")
	private List<Aeropuerto> aeropuertos;

	//bi-directional many-to-one association to Pai
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="id_pais")
	private Pai pai;

	public Ciudad() {
	}

	public Integer getIdCiudad() {
		return this.idCiudad;
	}

	public void setIdCiudad(Integer idCiudad) {
		this.idCiudad = idCiudad;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Aeropuerto> getAeropuertos() {
		return this.aeropuertos;
	}

	public void setAeropuertos(List<Aeropuerto> aeropuertos) {
		this.aeropuertos = aeropuertos;
	}

	public Aeropuerto addAeropuerto(Aeropuerto aeropuerto) {
		getAeropuertos().add(aeropuerto);
		aeropuerto.setCiudad(this);

		return aeropuerto;
	}

	public Aeropuerto removeAeropuerto(Aeropuerto aeropuerto) {
		getAeropuertos().remove(aeropuerto);
		aeropuerto.setCiudad(null);

		return aeropuerto;
	}

	public Pai getPai() {
		return this.pai;
	}

	public void setPai(Pai pai) {
		this.pai = pai;
	}

}