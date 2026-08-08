package com.SkyWay.modules.tipoturno.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.turno.domain.model.Turno;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the tipo_turno database table.
 * 
 */
@Entity
@Table(name="tipo_turno")
@NamedQuery(name="TipoTurno.findAll", query="SELECT t FROM TipoTurno t")
public class TipoTurno implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "tipo_turno_seq")
	@SequenceGenerator(name = "tipo_turno_seq",sequenceName = "tipo_turno_seq",allocationSize = 1)
	@Column(name="id_tipo")
	private Integer idTipo;

	private String nombre;

	//bi-directional many-to-one association to Turno
	@OneToMany(mappedBy="tipoTurno")
	private List<Turno> turnos;

	public TipoTurno() {
	}

	public Integer getIdTipo() {
		return this.idTipo;
	}

	public void setIdTipo(Integer idTipo) {
		this.idTipo = idTipo;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Turno> getTurnos() {
		return this.turnos;
	}

	public void setTurnos(List<Turno> turnos) {
		this.turnos = turnos;
	}

	public Turno addTurno(Turno turno) {
		getTurnos().add(turno);
		turno.setTipoTurno(this);

		return turno;
	}

	public Turno removeTurno(Turno turno) {
		getTurnos().remove(turno);
		turno.setTipoTurno(null);

		return turno;
	}

}