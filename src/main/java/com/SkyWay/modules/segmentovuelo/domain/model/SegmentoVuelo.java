package com.SkyWay.modules.segmentovuelo.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.segmentovuelo.infrastructure.validator.DurationConverter;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;
import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the segmento_vuelo database table.
 * 
 */
@Entity
@Table(name="segmento_vuelo")
@NamedQuery(name="SegmentoVuelo.findAll", query="SELECT s FROM SegmentoVuelo s")
public class SegmentoVuelo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "segmento_vuelo_seq")
	@SequenceGenerator(name = "segmento_vuelo_seq",sequenceName = "segmento_vuelo_seq",allocationSize = 1)
	@Column(name="id_segmento")
	private Integer idSegmento;

	/*@Convert(converter = DurationConverter.class)
	@Column(name="duracion_estimada",columnDefinition = "interval")
	private Duration duracionEstimada;*/

	@Column(name="hora_llegada")
	@FutureOrPresent(message = "La fecha de llegada debe ser hoy o en el futuro.")
	private Timestamp horaLlegada;

	@Column(name="hora_salida")
	@FutureOrPresent(message = "La fecha de salida debe ser hoy o en el futuro.")
	private Timestamp horaSalida;

	@Column(name="orden_segmento")
	private Integer ordenSegmento;

	//bi-directional many-to-one association to AsignacionPuerta
	@OneToMany(mappedBy="segmentoVuelo",fetch = FetchType.EAGER)
	private List<AsignacionPuerta> asignacionPuertas;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="id_aeropuerto_destino")
	private Aeropuerto aeropuertoDestino;

	//bi-directional many-to-one association to Aeropuerto
	@ManyToOne
	@JoinColumn(name="id_aeropuerto_origen")
	private Aeropuerto aeropuertoOrigen;


	//bi-directional many-to-one association to Vuelo
	@ManyToOne
	@JoinColumn(name="id_vuelo")
	private Vuelo vuelo;

	public SegmentoVuelo() {
	}

	public Integer getIdSegmento() {
		return this.idSegmento;
	}

	public void setIdSegmento(Integer idSegmento) {
		this.idSegmento = idSegmento;
	}

	/*public Duration getDuracionEstimada() {
		return duracionEstimada;
	}

	public void setDuracionEstimada(Duration duracionEstimada) {
		this.duracionEstimada = duracionEstimada;
	}*/

	public void setHoraLlegada(@FutureOrPresent(message = "La fecha de llegada debe ser hoy o en el futuro.") Timestamp horaLlegada) {
		this.horaLlegada = horaLlegada;
	}

	public @FutureOrPresent(message = "La fecha de llegada debe ser hoy o en el futuro.") Timestamp getHoraLlegada() {
		return horaLlegada;
	}

	public void setHoraSalida(@FutureOrPresent(message = "La fecha de salida debe ser hoy o en el futuro.") Timestamp horaSalida) {
		this.horaSalida = horaSalida;
	}

	public @FutureOrPresent(message = "La fecha de salida debe ser hoy o en el futuro.") Timestamp getHoraSalida() {
		return horaSalida;
	}

	public Date getHoraSalidaDate() {
		return (horaSalida != null) ? new Date(horaSalida.getTime()) : null;
	}

	public void setHoraSalidaDate(Date date) {
		this.horaSalida = (date != null) ? new Timestamp(date.getTime()) : null;
	}

	public Date getHoraLlegadaDate() {
		return (horaLlegada != null) ? new Date(horaLlegada.getTime()) : null;
	}

	public void setHoraLlegadaDate(Date date) {
		this.horaLlegada = (date != null) ? new Timestamp(date.getTime()) : null;
	}

	public Integer getOrdenSegmento() {
		return this.ordenSegmento;
	}

	public void setOrdenSegmento(Integer ordenSegmento) {
		this.ordenSegmento = ordenSegmento;
	}

	public List<AsignacionPuerta> getAsignacionPuertas() {
		return this.asignacionPuertas;
	}

	public void setAsignacionPuertas(List<AsignacionPuerta> asignacionPuertas) {
		this.asignacionPuertas = asignacionPuertas;
	}

	public AsignacionPuerta addAsignacionPuerta(AsignacionPuerta asignacionPuerta) {
		getAsignacionPuertas().add(asignacionPuerta);
		asignacionPuerta.setSegmentoVuelo(this);

		return asignacionPuerta;
	}

	public AsignacionPuerta removeAsignacionPuerta(AsignacionPuerta asignacionPuerta) {
		getAsignacionPuertas().remove(asignacionPuerta);
		asignacionPuerta.setSegmentoVuelo(null);

		return asignacionPuerta;
	}


	public Aeropuerto getAeropuertoDestino() {
		return aeropuertoDestino;
	}

	public void setAeropuertoDestino(Aeropuerto aeropuertoDestino) {
		this.aeropuertoDestino = aeropuertoDestino;
	}

	public Aeropuerto getAeropuertoOrigen() {
		return aeropuertoOrigen;
	}

	public void setAeropuertoOrigen(Aeropuerto aeropuertoOrigen) {
		this.aeropuertoOrigen = aeropuertoOrigen;
	}

	public Vuelo getVuelo() {
		return this.vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}

	public String getPuertas(){
		StringBuilder puertas=new StringBuilder();

		Logger.logInfo("puertas "+asignacionPuertas);

		asignacionPuertas.forEach(asignacionPuerta -> {
			Logger.logInfo(asignacionPuerta.getPuertaEmbarque().getCodigoPuerta());

			String codigoPuerta = asignacionPuerta.getPuertaEmbarque().getCodigoPuerta();
			puertas.append(codigoPuerta).append(" ");

		});
		return puertas.toString();
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("SegmentoVuelo{");
		sb.append("idSegmento=").append(idSegmento);
		sb.append(", horaLlegada=").append(horaLlegada);
		sb.append(", horaSalida=").append(horaSalida);
		sb.append(", ordenSegmento=").append(ordenSegmento);
		sb.append(", aeropuertoDestino=").append(aeropuertoDestino.getNombreAeropuerto());
		sb.append(", aeropuertoOrigen=").append(aeropuertoOrigen.getNombreAeropuerto());
		sb.append('}');
		return sb.toString();
	}
}