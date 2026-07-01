package com.SkyWay.modules.tripulacion.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the tripulacion database table.
 * 
 */
@Entity
@NamedQuery(name="Tripulacion.findAll", query="SELECT t FROM Tripulacion t")
public class Tripulacion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	private String rut;

	private String cargo;

	@Column(name="fecha_ingreso")
	private Timestamp fechaIngreso;

	//bi-directional one-to-one association to Usuario
	@OneToOne
	@JoinColumn(name="rut")
	private Usuario usuario;

	//bi-directional many-to-one association to TurnoTripulacion
	@OneToMany(mappedBy="tripulacion1")
	private List<TurnoTripulacion> turnoTripulacions1;



	public Tripulacion() {
	}

	public String getRut() {
		return this.rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public String getCargo() {
		return this.cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public Timestamp getFechaIngreso() {
		return this.fechaIngreso;
	}

	public void setFechaIngreso(Timestamp fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<TurnoTripulacion> getTurnoTripulacions1() {
		return this.turnoTripulacions1;
	}

	public void setTurnoTripulacions1(List<TurnoTripulacion> turnoTripulacions1) {
		this.turnoTripulacions1 = turnoTripulacions1;
	}

	public TurnoTripulacion addTurnoTripulacions1(TurnoTripulacion turnoTripulacions1) {
		getTurnoTripulacions1().add(turnoTripulacions1);
		turnoTripulacions1.setTripulacion1(this);

		return turnoTripulacions1;
	}

	public TurnoTripulacion removeTurnoTripulacions1(TurnoTripulacion turnoTripulacions1) {
		getTurnoTripulacions1().remove(turnoTripulacions1);
		turnoTripulacions1.setTripulacion1(null);

		return turnoTripulacions1;
	}




}