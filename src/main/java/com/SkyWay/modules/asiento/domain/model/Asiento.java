package com.SkyWay.modules.asiento.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the asiento database table.
 * 
 */
@Entity
@NamedQuery(name="Asiento.findAll", query="SELECT a FROM Asiento a")
public class Asiento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "asiento_seq")
	@SequenceGenerator(name = "asiento_seq",sequenceName = "asiento_seq",allocationSize = 1)
	@Column(name="id_asiento")
	private Integer idAsiento;

	@Column(name="numero_asiento")
	private String numeroAsiento;

	//bi-directional many-to-one association to Avion
	@ManyToOne
	@JoinColumn(name="id_avion")
	private Avion avion;

	//bi-directional many-to-one association to ClaseAsiento
	@ManyToOne
	@JoinColumn(name="id_clase")
	private ClaseAsiento claseAsiento;

	//bi-directional many-to-one association to ReservaAsiento
	@OneToMany(mappedBy="asiento")
	private List<ReservaAsiento> reservaAsientos;

	public Asiento() {
	}

	public Integer getIdAsiento() {
		return this.idAsiento;
	}

	public void setIdAsiento(Integer idAsiento) {
		this.idAsiento = idAsiento;
	}

	public String getNumeroAsiento() {
		return this.numeroAsiento;
	}

	public void setNumeroAsiento(String numeroAsiento) {
		this.numeroAsiento = numeroAsiento;
	}

	public Avion getAvion() {
		return this.avion;
	}

	public void setAvion(Avion avion) {
		this.avion = avion;
	}

	public ClaseAsiento getClaseAsiento() {
		return this.claseAsiento;
	}

	public void setClaseAsiento(ClaseAsiento claseAsiento) {
		this.claseAsiento = claseAsiento;
	}

	public List<ReservaAsiento> getReservaAsientos() {
		return this.reservaAsientos;
	}

	public void setReservaAsientos(List<ReservaAsiento> reservaAsientos) {
		this.reservaAsientos = reservaAsientos;
	}

	public ReservaAsiento addReservaAsiento(ReservaAsiento reservaAsiento) {
		getReservaAsientos().add(reservaAsiento);
		reservaAsiento.setAsiento(this);

		return reservaAsiento;
	}

	public ReservaAsiento removeReservaAsiento(ReservaAsiento reservaAsiento) {
		getReservaAsientos().remove(reservaAsiento);
		reservaAsiento.setAsiento(null);

		return reservaAsiento;
	}

}