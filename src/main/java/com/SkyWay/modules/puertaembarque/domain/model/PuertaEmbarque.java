package com.SkyWay.modules.puertaembarque.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the puerta_embarque database table.
 * 
 */
@Entity
@Table(name="puerta_embarque")
@org.hibernate.annotations.SQLDelete(sql = "UPDATE puerta_embarque SET activo = false, fecha_baja = NOW() WHERE id_puerta = ?")
@org.hibernate.annotations.SQLRestriction("activo = true")
@NamedQuery(name="PuertaEmbarque.findAll", query="SELECT p FROM PuertaEmbarque p")
public class PuertaEmbarque implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "puerta_seq")
	@SequenceGenerator(name = "puerta_seq",sequenceName = "puerta_seq",allocationSize = 1)
	@Column(name="id_puerta")
	private Integer idPuerta;

	@Column(name="codigo_puerta")
	private String codigoPuerta;

	private String terminal;

	@Column(name="activo", nullable=false)
	private Boolean activo = true;

	@Column(name="fecha_baja")
	private java.sql.Timestamp fechaBaja;

	public Boolean getActivo() { return this.activo; }
	public void setActivo(Boolean activo) { this.activo = activo; }
	public java.sql.Timestamp getFechaBaja() { return this.fechaBaja; }
	public void setFechaBaja(java.sql.Timestamp fechaBaja) { this.fechaBaja = fechaBaja; }

	//bi-directional many-to-one association to AsignacionPuerta
	@OneToMany(mappedBy="puertaEmbarque")
	@JsonIgnore
	private List<AsignacionPuerta> asignacionPuertas;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="id_aeropuerto")
	@org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
	@JsonIgnore
	private Aeropuerto aeropuerto;

	public PuertaEmbarque() {
	}

	public Integer getIdPuerta() {
		return this.idPuerta;
	}

	public void setIdPuerta(Integer idPuerta) {
		this.idPuerta = idPuerta;
	}

	public String getCodigoPuerta() {
		return this.codigoPuerta;
	}

	public void setCodigoPuerta(String codigoPuerta) {
		this.codigoPuerta = codigoPuerta;
	}

	public String getTerminal() {
		return this.terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	public List<AsignacionPuerta> getAsignacionPuertas() {
		return this.asignacionPuertas;
	}

	public void setAsignacionPuertas(List<AsignacionPuerta> asignacionPuertas) {
		this.asignacionPuertas = asignacionPuertas;
	}

	public AsignacionPuerta addAsignacionPuerta(AsignacionPuerta asignacionPuerta) {
		getAsignacionPuertas().add(asignacionPuerta);
		asignacionPuerta.setPuertaEmbarque(this);

		return asignacionPuerta;
	}

	public AsignacionPuerta removeAsignacionPuerta(AsignacionPuerta asignacionPuerta) {
		getAsignacionPuertas().remove(asignacionPuerta);
		asignacionPuerta.setPuertaEmbarque(null);

		return asignacionPuerta;
	}

	public Aeropuerto getAeropuerto() {
		return this.aeropuerto;
	}

	public void setAeropuerto(Aeropuerto aeropuerto) {
		this.aeropuerto = aeropuerto;
	}

}