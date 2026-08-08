package com.SkyWay.modules.avion.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;


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

	@Column(name="fecha_proximo_mantenimiento")
	private Timestamp fechaProximoMantenimiento;

	@Column(name="numero_de_registro")
	private String numeroDeRegistro;

	//bi-directional many-to-one association to Asiento
	@OneToMany(mappedBy="avion")
	private List<Asiento> asientos;

	//bi-directional many-to-one association to ModeloAvion
	@ManyToOne
	@JoinColumn(name="id_modelo")
	private ModeloAvion modeloAvion;

	//bi-directional many-to-one association to CapacidadClase
	@OneToMany(mappedBy="avion1")
	private List<CapacidadClase> capacidadClases1;


	@JsonIgnore
	//bi-directional many-to-one association to Vuelo
	@OneToMany(mappedBy="avion")
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

	public Timestamp getFechaProximoMantenimiento() {
		return this.fechaProximoMantenimiento;
	}

	public void setFechaProximoMantenimiento(Timestamp fechaProximoMantenimiento) {
		this.fechaProximoMantenimiento = fechaProximoMantenimiento;
	}

	public String getNumeroDeRegistro() {
		return this.numeroDeRegistro;
	}

	public void setNumeroDeRegistro(String numeroDeRegistro) {
		this.numeroDeRegistro = numeroDeRegistro;
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

	public ModeloAvion getModeloAvion() {
		return this.modeloAvion;
	}

	public void setModeloAvion(ModeloAvion modeloAvion) {
		this.modeloAvion = modeloAvion;
	}

	public List<CapacidadClase> getCapacidadClases1() {
		return this.capacidadClases1;
	}

	public void setCapacidadClases1(List<CapacidadClase> capacidadClases1) {
		this.capacidadClases1 = capacidadClases1;
	}

	public CapacidadClase addCapacidadClases1(CapacidadClase capacidadClases1) {
		getCapacidadClases1().add(capacidadClases1);
		capacidadClases1.setAvion1(this);

		return capacidadClases1;
	}

	public CapacidadClase removeCapacidadClases1(CapacidadClase capacidadClases1) {
		getCapacidadClases1().remove(capacidadClases1);
		capacidadClases1.setAvion1(null);

		return capacidadClases1;
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

}