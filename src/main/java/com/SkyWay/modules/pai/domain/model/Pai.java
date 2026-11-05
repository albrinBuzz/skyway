package com.SkyWay.modules.pai.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.continente.domain.model.Continente;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the pais database table.
 * 
 */
@Entity
@org.hibernate.annotations.SQLDelete(sql = "UPDATE pais SET activo = false, fecha_baja = NOW() WHERE id_pais = ?")
@org.hibernate.annotations.SQLRestriction("activo = true")
@Table(name="pais")
@NamedQuery(name="Pai.findAll", query="SELECT p FROM Pai p")
public class Pai implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "pais_seq")
	@SequenceGenerator(name = "pais_seq",sequenceName = "pais_seq",allocationSize = 1)
	@Column(name="id_pais")
	private Integer idPais;

	private String nombre;

	@Column(name = "activo", nullable = false)
	private Boolean activo = true;

	@Column(name = "fecha_baja")
	private java.sql.Timestamp fechaBaja;

	public Boolean getActivo() { return this.activo; }
	public void setActivo(Boolean activo) { this.activo = activo; }
	public java.sql.Timestamp getFechaBaja() { return this.fechaBaja; }
	public void setFechaBaja(java.sql.Timestamp fechaBaja) { this.fechaBaja = fechaBaja; }

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_continente")
	@org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
	private Continente continente;

	//bi-directional many-to-one association to Ciudad
	@OneToMany(mappedBy="pai")
	private List<Ciudad> ciudads;



	public Pai() {
	}

	public Integer getIdPais() {
		return this.idPais;
	}

	public void setIdPais(Integer idPais) {
		this.idPais = idPais;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Ciudad> getCiudads() {
		return this.ciudads;
	}

	public void setCiudads(List<Ciudad> ciudads) {
		this.ciudads = ciudads;
	}

	public Continente getContinente() {
		return continente;
	}

	public void setContinente(Continente continente) {
		this.continente = continente;
	}

	public Ciudad addCiudad(Ciudad ciudad) {
		getCiudads().add(ciudad);
		ciudad.setPai(this);

		return ciudad;
	}

	public Ciudad removeCiudad(Ciudad ciudad) {
		getCiudads().remove(ciudad);
		ciudad.setPai(null);

		return ciudad;
	}

}