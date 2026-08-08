package com.SkyWay.modules.turno.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import jakarta.persistence.*;
import java.util.Date;
import java.sql.Timestamp;
import java.util.List;


/**
 * The persistent class for the turno database table.
 * 
 */
@Entity
@NamedQuery(name="Turno.findAll", query="SELECT t FROM Turno t")
public class Turno implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "turno_seq")
	@SequenceGenerator(name = "turno_seq",sequenceName = "turno_seq",allocationSize = 1)
	@Column(name="id_turno")
	private Integer idTurno;

	@Temporal(TemporalType.DATE)
	private Date fecha;

	@Column(name="hora_fin")
	private Timestamp horaFin;

	@Column(name="hora_inicio")
	private Timestamp horaInicio;

	//bi-directional many-to-one association to TipoTurno
	@ManyToOne
	@JoinColumn(name="id_tipo_turno")
	private TipoTurno tipoTurno;

	//bi-directional many-to-one association to Vuelo
	@ManyToOne
	@JoinColumn(name="id_vuelo")
	private Vuelo vuelo;

	//bi-directional many-to-one association to TurnoTripulacion
	@OneToMany(mappedBy="turno1")
	private List<TurnoTripulacion> turnoTripulacions1;


	public Turno() {
	}

	public Integer getIdTurno() {
		return this.idTurno;
	}

	public void setIdTurno(Integer idTurno) {
		this.idTurno = idTurno;
	}

	public Date getFecha() {
		return this.fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Timestamp getHoraFin() {
		return this.horaFin;
	}

	public void setHoraFin(Timestamp horaFin) {
		this.horaFin = horaFin;
	}

	public Timestamp getHoraInicio() {
		return this.horaInicio;
	}

	public void setHoraInicio(Timestamp horaInicio) {
		this.horaInicio = horaInicio;
	}

	public TipoTurno getTipoTurno() {
		return this.tipoTurno;
	}

	public void setTipoTurno(TipoTurno tipoTurno) {
		this.tipoTurno = tipoTurno;
	}

	public Vuelo getVuelo() {
		return this.vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}

	public List<TurnoTripulacion> getTurnoTripulacions1() {
		return this.turnoTripulacions1;
	}

	public void setTurnoTripulacions1(List<TurnoTripulacion> turnoTripulacions1) {
		this.turnoTripulacions1 = turnoTripulacions1;
	}

	public TurnoTripulacion addTurnoTripulacions1(TurnoTripulacion turnoTripulacions1) {
		getTurnoTripulacions1().add(turnoTripulacions1);
		turnoTripulacions1.setTurno1(this);

		return turnoTripulacions1;
	}

	public TurnoTripulacion removeTurnoTripulacions1(TurnoTripulacion turnoTripulacions1) {
		getTurnoTripulacions1().remove(turnoTripulacions1);
		turnoTripulacions1.setTurno1(null);

		return turnoTripulacions1;
	}


	@Override
	public String toString() {
		return "Turno{" +
				"idTurno=" + idTurno +
				", fecha=" + fecha +
				", horaFin=" + horaFin +
				", horaInicio=" + horaInicio +
				", tipoTurno=" + tipoTurno +
				", vuelo=" + vuelo +
				", turnoTripulacions1=" + turnoTripulacions1 +
				'}';
	}
}