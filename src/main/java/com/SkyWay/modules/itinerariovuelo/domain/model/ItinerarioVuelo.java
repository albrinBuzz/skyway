package com.SkyWay.modules.itinerariovuelo.domain.model;

import java.io.Serializable;
import java.time.Duration;

import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import jakarta.persistence.*;


/**
 * The persistent class for the itinerario_vuelo database table.
 * 
 */
@Entity
@Table(name="itinerario_vuelo")
@NamedQuery(name="ItinerarioVuelo.findAll", query="SELECT i FROM ItinerarioVuelo i")
public class ItinerarioVuelo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "itinerario_vuelo_seq")
	@SequenceGenerator(name = "itinerario_vuelo_seq",sequenceName = "itinerario_vuelo_seq",allocationSize = 1)
	@Column(name="id_itinerario_vuelo")
	private Integer idItinerarioVuelo;

	private Integer orden;

	/*@Column(name="tiempo_espera",columnDefinition = "interval")
	private Duration tiempoEspera;*/

	@Column(name="tipo_conexion")
	private String tipoConexion;

	//bi-directional many-to-one association to Itinerario
	@ManyToOne
	@JoinColumn(name="id_itinerario")
	private Itinerario itinerario;

	//bi-directional many-to-one association to Vuelo
	@ManyToOne
	@JoinColumn(name="id_vuelo")
	private Vuelo vuelo;

	public ItinerarioVuelo() {
	}

	public Integer getIdItinerarioVuelo() {
		return this.idItinerarioVuelo;
	}

	public void setIdItinerarioVuelo(Integer idItinerarioVuelo) {
		this.idItinerarioVuelo = idItinerarioVuelo;
	}

	public Integer getOrden() {
		return this.orden;
	}

	public void setOrden(Integer orden) {
		this.orden = orden;
	}

	/*public Duration getTiempoEspera() {
		return tiempoEspera;
	}

	public void setTiempoEspera(Duration tiempoEspera) {
		this.tiempoEspera = tiempoEspera;
	}*/

	public String getTipoConexion() {
		return this.tipoConexion;
	}

	public void setTipoConexion(String tipoConexion) {
		this.tipoConexion = tipoConexion;
	}

	public Itinerario getItinerario() {
		return this.itinerario;
	}

	public void setItinerario(Itinerario itinerario) {
		this.itinerario = itinerario;
	}

	public Vuelo getVuelo() {
		return this.vuelo;
	}

	public void setVuelo(Vuelo vuelo) {
		this.vuelo = vuelo;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("ItinerarioVuelo{");
		sb.append("orden=").append(orden);
		sb.append(", idItinerarioVuelo=").append(idItinerarioVuelo);
		sb.append(", vuelo=").append(vuelo.getNumeroVuelo());
		sb.append('}');
		return sb.toString();
	}
}