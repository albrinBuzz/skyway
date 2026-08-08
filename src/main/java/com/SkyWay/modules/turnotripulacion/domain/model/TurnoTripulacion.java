package com.SkyWay.modules.turnotripulacion.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.turno.domain.model.Turno;
import jakarta.persistence.*;


/**
 * The persistent class for the turno_tripulacion database table.
 * 
 */
@Entity
@Table(name="turno_tripulacion")
@NamedQuery(name="TurnoTripulacion.findAll", query="SELECT t FROM TurnoTripulacion t")
public class TurnoTripulacion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "turno_tripulacion_seq")
	@SequenceGenerator(name = "turno_tripulacion_seq",sequenceName = "turno_tripulacion_seq",allocationSize = 1)
	@Column(name="id_turno_tripulacion")
	private Integer idTurnoTripulacion;

	//bi-directional many-to-one association to Tripulacion
	@ManyToOne
	@JoinColumn(name="rut_tripulacion")
	private Tripulacion tripulacion1;



	//bi-directional many-to-one association to Turno
	@ManyToOne
	@JoinColumn(name="id_turno")
	private Turno turno1;



	public TurnoTripulacion() {
	}

	public Integer getIdTurnoTripulacion() {
		return this.idTurnoTripulacion;
	}

	public void setIdTurnoTripulacion(Integer idTurnoTripulacion) {
		this.idTurnoTripulacion = idTurnoTripulacion;
	}

	public Tripulacion getTripulacion1() {
		return this.tripulacion1;
	}

	public void setTripulacion1(Tripulacion tripulacion1) {
		this.tripulacion1 = tripulacion1;
	}



	public Turno getTurno1() {
		return this.turno1;
	}

	public void setTurno1(Turno turno1) {
		this.turno1 = turno1;
	}



}