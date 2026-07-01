package com.SkyWay.modules.asignacionpuerta.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the asignacion_puerta database table.
 * 
 */
@Entity
@Table(name="asignacion_puerta")
@NamedQuery(name="AsignacionPuerta.findAll", query="SELECT a FROM AsignacionPuerta a")
public class AsignacionPuerta implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "asignacion_puerta_seq")
	@SequenceGenerator(name = "asignacion_puerta_seq",sequenceName = "asignacion_puerta_seq",allocationSize = 1)
	@Column(name="id_asignacion")
	private Integer idAsignacion;

	@Column(name="hora_asignacion")
	private Timestamp horaAsignacion;

	//bi-directional many-to-one association to PuertaEmbarque
	@ManyToOne
	@JoinColumn(name="id_puerta")
	private PuertaEmbarque puertaEmbarque;

	//bi-directional many-to-one association to SegmentoVuelo
	@ManyToOne
	@JoinColumn(name="id_segmento")
	private SegmentoVuelo segmentoVuelo;

	public AsignacionPuerta() {
	}

	public Integer getIdAsignacion() {
		return this.idAsignacion;
	}

	public void setIdAsignacion(Integer idAsignacion) {
		this.idAsignacion = idAsignacion;
	}

	public Timestamp getHoraAsignacion() {
		return this.horaAsignacion;
	}

	public void setHoraAsignacion(Timestamp horaAsignacion) {
		this.horaAsignacion = horaAsignacion;
	}

	public PuertaEmbarque getPuertaEmbarque() {
		return this.puertaEmbarque;
	}

	public void setPuertaEmbarque(PuertaEmbarque puertaEmbarque) {
		this.puertaEmbarque = puertaEmbarque;
	}

	public SegmentoVuelo getSegmentoVuelo() {
		return this.segmentoVuelo;
	}

	public void setSegmentoVuelo(SegmentoVuelo segmentoVuelo) {
		this.segmentoVuelo = segmentoVuelo;
	}

}