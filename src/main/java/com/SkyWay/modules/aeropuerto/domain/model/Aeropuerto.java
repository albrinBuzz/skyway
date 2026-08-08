package com.SkyWay.modules.aeropuerto.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.puertaembarque.domain.model.PuertaEmbarque;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;


/**
 * The persistent class for the aeropuerto database table.
 * 
 */
@Entity
@NamedQuery(name="Aeropuerto.findAll", query="SELECT a FROM Aeropuerto a")
public class Aeropuerto implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "aeropuerto_seq")
	@SequenceGenerator(name = "aeropuerto_seq",sequenceName = "aeropuerto_seq",allocationSize = 1)
	@Column(name="id_aeropuerto")
	private Integer idAeropuerto;

	@Column(name="codigo_iata")
	private String codigoIata;

	@Column(name="nombre_aeropuerto")
	private String nombreAeropuerto;

	//bi-directional many-to-one association to Ciudad
	@ManyToOne
	@JoinColumn(name="id_ciudad")
	private Ciudad ciudad;

	//bi-directional many-to-one association to Itinerario
	@OneToMany(mappedBy="aeropuertoDestino")
	private List<Itinerario> itinerarios1;

	//bi-directional many-to-one association to Itinerario
	@OneToMany(mappedBy="aeropuertoOrigen")
	private List<Itinerario> itinerarios2;

	//bi-directional many-to-one association to Itinerario
	/*@OneToMany(mappedBy="aeropuerto3")
	private List<Itinerario> itinerarios3;

	//bi-directional many-to-one association to Itinerario
	@OneToMany(mappedBy="aeropuerto4")
	private List<Itinerario> itinerarios4;*/

	//bi-directional many-to-one association to PuertaEmbarque
	@OneToMany(mappedBy="aeropuerto")
	private List<PuertaEmbarque> puertaEmbarques;

	//bi-directional many-to-one association to SegmentoVuelo
	@OneToMany(mappedBy="aeropuertoDestino")
	private List<SegmentoVuelo> segmentoVuelos1;

	//bi-directional many-to-one association to SegmentoVuelo
	@OneToMany(mappedBy="aeropuertoOrigen")
	private List<SegmentoVuelo> segmentoVuelos2;



	public Aeropuerto() {
	}

	public Integer getIdAeropuerto() {
		return this.idAeropuerto;
	}

	public void setIdAeropuerto(Integer idAeropuerto) {
		this.idAeropuerto = idAeropuerto;
	}

	public String getCodigoIata() {
		return this.codigoIata;
	}

	public void setCodigoIata(String codigoIata) {
		this.codigoIata = codigoIata;
	}

	public String getNombreAeropuerto() {
		return this.nombreAeropuerto;
	}

	public void setNombreAeropuerto(String nombreAeropuerto) {
		this.nombreAeropuerto = nombreAeropuerto;
	}

	public Ciudad getCiudad() {
		return this.ciudad;
	}

	public void setCiudad(Ciudad ciudad) {
		this.ciudad = ciudad;
	}

	public List<Itinerario> getItinerarios1() {
		return this.itinerarios1;
	}

	public void setItinerarios1(List<Itinerario> itinerarios1) {
		this.itinerarios1 = itinerarios1;
	}



	public List<Itinerario> getItinerarios2() {
		return this.itinerarios2;
	}

	public void setItinerarios2(List<Itinerario> itinerarios2) {
		this.itinerarios2 = itinerarios2;
	}

	/*public List<Itinerario> getItinerarios3() {
		return this.itinerarios3;
	}

	public void setItinerarios3(List<Itinerario> itinerarios3) {
		this.itinerarios3 = itinerarios3;
	}

	public Itinerario addItinerarios3(Itinerario itinerarios3) {
		getItinerarios3().add(itinerarios3);
		itinerarios3.setAeropuerto3(this);

		return itinerarios3;
	}

	public Itinerario removeItinerarios3(Itinerario itinerarios3) {
		getItinerarios3().remove(itinerarios3);
		itinerarios3.setAeropuerto3(null);

		return itinerarios3;
	}

	public List<Itinerario> getItinerarios4() {
		return this.itinerarios4;
	}

	public void setItinerarios4(List<Itinerario> itinerarios4) {
		this.itinerarios4 = itinerarios4;
	}

	public Itinerario addItinerarios4(Itinerario itinerarios4) {
		getItinerarios4().add(itinerarios4);
		itinerarios4.setAeropuerto4(this);

		return itinerarios4;
	}

	public Itinerario removeItinerarios4(Itinerario itinerarios4) {
		getItinerarios4().remove(itinerarios4);
		itinerarios4.setAeropuerto4(null);

		return itinerarios4;
	}*/

	public List<PuertaEmbarque> getPuertaEmbarques() {
		return this.puertaEmbarques;
	}

	public void setPuertaEmbarques(List<PuertaEmbarque> puertaEmbarques) {
		this.puertaEmbarques = puertaEmbarques;
	}

	public PuertaEmbarque addPuertaEmbarque(PuertaEmbarque puertaEmbarque) {
		getPuertaEmbarques().add(puertaEmbarque);
		puertaEmbarque.setAeropuerto(this);

		return puertaEmbarque;
	}

	public PuertaEmbarque removePuertaEmbarque(PuertaEmbarque puertaEmbarque) {
		getPuertaEmbarques().remove(puertaEmbarque);
		puertaEmbarque.setAeropuerto(null);

		return puertaEmbarque;
	}

	public List<SegmentoVuelo> getSegmentoVuelos1() {
		return this.segmentoVuelos1;
	}

	public void setSegmentoVuelos1(List<SegmentoVuelo> segmentoVuelos1) {
		this.segmentoVuelos1 = segmentoVuelos1;
	}

	public SegmentoVuelo addSegmentoVuelos1(SegmentoVuelo segmentoVuelos1) {
		getSegmentoVuelos1().add(segmentoVuelos1);
		segmentoVuelos1.setAeropuertoDestino(this);

		return segmentoVuelos1;
	}

	public SegmentoVuelo removeSegmentoVuelos1(SegmentoVuelo segmentoVuelos1) {
		getSegmentoVuelos1().remove(segmentoVuelos1);
		segmentoVuelos1.setAeropuertoDestino(null);

		return segmentoVuelos1;
	}

	public List<SegmentoVuelo> getSegmentoVuelos2() {
		return this.segmentoVuelos2;
	}

	public void setSegmentoVuelos2(List<SegmentoVuelo> segmentoVuelos2) {
		this.segmentoVuelos2 = segmentoVuelos2;
	}

	public SegmentoVuelo addSegmentoVuelos2(SegmentoVuelo segmentoVuelos2) {
		getSegmentoVuelos2().add(segmentoVuelos2);
		segmentoVuelos2.setAeropuertoOrigen(this);

		return segmentoVuelos2;
	}

	public SegmentoVuelo removeSegmentoVuelos2(SegmentoVuelo segmentoVuelos2) {
		getSegmentoVuelos2().remove(segmentoVuelos2);
		segmentoVuelos2.setAeropuertoOrigen(null);

		return segmentoVuelos2;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		Aeropuerto other = (Aeropuerto) obj;
		return Objects.equals(idAeropuerto, other.idAeropuerto);
	}

	@Override
	public int hashCode() {
		return Objects.hash(idAeropuerto);
	}


	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("Aeropuerto{");
		sb.append("idAeropuerto=").append(idAeropuerto);
		sb.append(", codigoIata='").append(codigoIata).append('\'');
		sb.append(", nombreAeropuerto='").append(nombreAeropuerto).append('\'');
		sb.append('}');
		return sb.toString();
	}
}