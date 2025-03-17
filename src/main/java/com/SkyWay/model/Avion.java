package com.SkyWay.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;


/**
 * The persistent class for the avion database table.
 * 
 */
@Entity
@NamedQuery(name="Avion.findAll", query="SELECT a FROM Avion a")
public class Avion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "avion_seq")
	@SequenceGenerator(name = "avion_seq",sequenceName = "avion_seq",allocationSize = 1)
	@Column(name="id_avion")
	private Integer idAvion;

	@Column(name="ano_de_fabricacion")
	private Integer anoDeFabricacion;

	@Column(name="capacidad_de_carga")
	private Integer capacidadDeCarga;

	@Column(name="capacidad_de_pasajeros")
	private Integer capacidadDePasajeros;

	@Column(name="estado_de_mantenimiento")
	private String estadoDeMantenimiento;

	private String fabricante;

	private String modelo;
	
	private Integer cap_economica;
	
	private Integer cap_ejecutiva;
	
	private Integer cap_primera;

	@Column(name="numero_de_registro")
	private String numeroDeRegistro;

	//bi-directional many-to-one association to Asiento
	@OneToMany(mappedBy="avion",cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Asiento> asientos;

	//bi-directional many-to-one association to Vuelo
	@JsonIgnore
	@OneToMany(mappedBy="avion",cascade = CascadeType.ALL)
	private List<Vuelo> vuelos;

	public Avion() {

	}

	public Integer getIdAvion() {
		return this.idAvion;
	}

	public void setIdAvion(Integer idAvion) {
		this.idAvion = idAvion;
	}

	public Integer getAnoDeFabricacion() {
		return this.anoDeFabricacion;
	}

	public void setAnoDeFabricacion(Integer anoDeFabricacion) {
		this.anoDeFabricacion = anoDeFabricacion;
	}

	public Integer getCapacidadDeCarga() {
		return this.capacidadDeCarga;
	}

	public void setCapacidadDeCarga(Integer capacidadDeCarga) {
		this.capacidadDeCarga = capacidadDeCarga;
	}

	public Integer getCapacidadDePasajeros() {
		return this.capacidadDePasajeros;
	}

	public void setCapacidadDePasajeros(Integer capacidadDePasajeros) {
		this.capacidadDePasajeros = capacidadDePasajeros;
	}

	public String getEstadoDeMantenimiento() {
		return this.estadoDeMantenimiento;
	}

	public void setEstadoDeMantenimiento(String estadoDeMantenimiento) {
		this.estadoDeMantenimiento = estadoDeMantenimiento;
	}

	public String getFabricante() {
		return this.fabricante;
	}

	public void setFabricante(String fabricante) {
		this.fabricante = fabricante;
	}

	public String getModelo() {
		return this.modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public String getNumeroDeRegistro() {
		return this.numeroDeRegistro;
	}

	
	
	public void setNumeroDeRegistro(String numeroDeRegistro) {
		this.numeroDeRegistro = numeroDeRegistro;
	}

	public Integer getCap_economica() {
		return cap_economica;
	}

	public void setCap_economica(Integer cap_economica) {
		this.cap_economica = cap_economica;
	}

	public Integer getCap_ejecutiva() {
		return cap_ejecutiva;
	}

	public void setCap_ejecutiva(Integer cap_ejecutiva) {
		this.cap_ejecutiva = cap_ejecutiva;
	}

	public Integer getCap_primera() {
		return cap_primera;
	}

	public void setCap_primera(Integer cap_primera) {
		this.cap_primera = cap_primera;
	}
	
	
	
	public List<Asiento> getAsientos() {
		return this.asientos;
	}

	public void setAsientos(List<Asiento> asientos) {
		this.asientos = asientos;
	}

	public Asiento addAsiento(Asiento asiento) {
		getAsientos().add(asiento);
		asiento.setAvion(this);

		return asiento;
	}

	public Asiento removeAsiento(Asiento asiento) {
		getAsientos().remove(asiento);
		asiento.setAvion(null);

		return asiento;
	}

	public List<Vuelo> getVuelos() {
		return this.vuelos;
	}

	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public Vuelo addVuelo(Vuelo vuelo) {
		getVuelos().add(vuelo);
		vuelo.setAvion(this);

		return vuelo;
	}

	public Vuelo removeVuelo(Vuelo vuelo) {
		getVuelos().remove(vuelo);
		vuelo.setAvion(null);

		return vuelo;
	}

	@Override
	public String toString() {
		return "Avion [idAvion=" + idAvion + ", anoDeFabricacion=" + anoDeFabricacion + ", capacidadDeCarga="
				+ capacidadDeCarga + ", capacidadDePasajeros=" + capacidadDePasajeros + ", estadoDeMantenimiento="
				+ estadoDeMantenimiento + ", fabricante=" + fabricante + ", modelo=" + modelo + ", cap_economica="
				+ cap_economica + ", cap_ejecutiva=" + cap_ejecutiva + ", cap_primera=" + cap_primera
				+ ", numeroDeRegistro=" + numeroDeRegistro + "]";
	}
	
	



	
	

}