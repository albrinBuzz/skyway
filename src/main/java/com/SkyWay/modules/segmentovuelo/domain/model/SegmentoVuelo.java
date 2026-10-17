package com.SkyWay.modules.segmentovuelo.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.asignacionpuerta.domain.model.AsignacionPuerta;
import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
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

	// En SegmentoVuelo.java o el Bean que usa la vista:
	public LocalDateTime getHoraSalidaAsLocalDateTime() {
		return horaSalida != null ? horaSalida.toLocalDateTime() : null;
	}

	public LocalDateTime getHoraLlegadaAsLocalDateTime() {
		return horaLlegada != null ? horaLlegada.toLocalDateTime() : null;
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

	public String getPuertasEmbarque() {
		Logger.logInfo("================ 🚪 EVALUANDO PUERTAS DE EMBARQUE ================");

		if (vuelo == null) {
			Logger.logWarn("⚠️ [getPuertasEmbarque] Objeto 'vuelo' es NULL.");
			return "Por asignar";
		}

		Logger.logInfo("✈️ [getPuertasEmbarque] Vuelo ID: " + vuelo.getIdVuelo() + " | Número Vuelo: " + vuelo.getNumeroVuelo());

		List<SegmentoVuelo> segmentos = vuelo.getSegmentoVuelos();
		if (segmentos == null || segmentos.isEmpty()) {
			Logger.logWarn("⚠️ [getPuertasEmbarque] La lista 'segmentoVuelos' es NULL o está vacía.");
			return "Por asignar";
		}

		Logger.logInfo("📦 [getPuertasEmbarque] Cantidad de segmentos encontrados: " + segmentos.size());

		SegmentoVuelo primerSegmento = segmentos.get(0);
		if (primerSegmento == null) {
			Logger.logWarn("⚠️ [getPuertasEmbarque] El primer segmento (índice 0) es NULL.");
			return "Por asignar";
		}

		Logger.logInfo("📍 [getPuertasEmbarque] Segmento ID: " + primerSegmento.getIdSegmento()
				+ " | Origen: " + (primerSegmento.getAeropuertoOrigen() != null ? primerSegmento.getAeropuertoOrigen().getCodigoIata() : "NULL")
				+ " -> Destino: " + (primerSegmento.getAeropuertoDestino() != null ? primerSegmento.getAeropuertoDestino().getCodigoIata() : "NULL"));

		List<AsignacionPuerta> asignaciones = primerSegmento.getAsignacionPuertas();
		if (asignaciones == null) {
			Logger.logWarn("⚠️ [getPuertasEmbarque] La lista 'asignacionPuertas' en el primer segmento es NULL.");
			return "Por asignar";
		}

		if (asignaciones.isEmpty()) {
			Logger.logWarn("⚠️ [getPuertasEmbarque] La lista 'asignacionPuertas' en el primer segmento está VACÍA (size = 0).");
			return "Por asignar";
		}

		Logger.logInfo("🔑 [getPuertasEmbarque] Asignaciones de puerta encontradas: " + asignaciones.size());

		StringBuilder infoPuertas = new StringBuilder();

		for (int i = 0; i < asignaciones.size(); i++) {
			AsignacionPuerta ap = asignaciones.get(i);
			if (ap == null) {
				Logger.logWarn("⚠️ [getPuertasEmbarque] Elemento AsignacionPuerta índice " + i + " es NULL.");
				continue;
			}

			PuertaEmbarque puerta = ap.getPuertaEmbarque();
			if (puerta == null) {
				Logger.logWarn("⚠️ [getPuertasEmbarque] AsignacionPuerta ID " + ap.getIdAsignacion() + " no tiene asociada una 'PuertaEmbarque' (es NULL).");
				continue;
			}

			String terminal = (puerta.getTerminal() != null && !puerta.getTerminal().isBlank())
					? "T" + puerta.getTerminal().trim() + " - "
					: "";
			String codigo = (puerta.getCodigoPuerta() != null)
					? puerta.getCodigoPuerta().trim()
					: "";

			Logger.logInfo("✨ [getPuertasEmbarque] Puerta hallada -> ID: " + puerta.getIdPuerta()
					+ " | Código: '" + codigo + "' | Terminal: '" + puerta.getTerminal() + "'");

			if (!codigo.isBlank()) {
				if (infoPuertas.length() > 0) {
					infoPuertas.append(", ");
				}
				infoPuertas.append(terminal).append("Puerta ").append(codigo);
			} else {
				Logger.logWarn("⚠️ [getPuertasEmbarque] Puerta ID " + puerta.getIdPuerta() + " tiene 'codigoPuerta' blanco o nulo.");
			}
		}

		String resultado = infoPuertas.length() > 0 ? infoPuertas.toString() : "Por asignar";
		Logger.logInfo("✅ [getPuertasEmbarque] Resultado final a retornar: '" + resultado + "'");
		Logger.logInfo("====================================================================");

		return resultado;
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