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
@Table(name="Ciudad")
@org.hibernate.annotations.SQLDelete(sql = "UPDATE Ciudad SET activo = false, fecha_baja = NOW() WHERE id_ciudad = ?")
@org.hibernate.annotations.SQLRestriction("activo = true")
@NamedQuery(name="Ciudad.findAll", query="SELECT c FROM Ciudad c")
public class Ciudad implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "ciudad_seq")
	@SequenceGenerator(name = "ciudad_seq",sequenceName = "ciudad_seq",allocationSize = 1)
	@Column(name="id_ciudad")
	private Integer idCiudad;

	private String nombre;

	@Column(name="activo", nullable=false)
	private Boolean activo = true;

	@Column(name="fecha_baja")
	private java.sql.Timestamp fechaBaja;

	public Boolean getActivo() { return this.activo; }
	public void setActivo(Boolean activo) { this.activo = activo; }
	public java.sql.Timestamp getFechaBaja() { return this.fechaBaja; }
	public void setFechaBaja(java.sql.Timestamp fechaBaja) { this.fechaBaja = fechaBaja; }

	//bi-directional many-to-one association to Aeropuerto
	@OneToMany(mappedBy="ciudad")
	private List<Aeropuerto> aeropuertos;

	//bi-directional many-to-one association to Pai
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="id_pais")
	@org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
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