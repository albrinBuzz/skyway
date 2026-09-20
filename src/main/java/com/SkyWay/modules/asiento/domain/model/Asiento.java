package com.SkyWay.modules.asiento.domain.model;

import java.io.Serializable;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.reservaasiento.domain.model.ReservaAsiento;
import jakarta.persistence.*;
import java.util.List;

@Entity
@NamedQuery(name="Asiento.findAll", query="SELECT a FROM Asiento a")
public class Asiento implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "asiento_seq")
	@SequenceGenerator(name = "asiento_seq", sequenceName = "asiento_seq", allocationSize = 1)
	@Column(name="id_asiento")
	private Integer idAsiento;

	@Column(name="numero_asiento")
	private String numeroAsiento;

	// NUEVOS CAMPOS ESPACIALES
	@Column(name="fila")
	private Integer fila;

	@Column(name="letra")
	private String letra;

	@Column(name="es_ventana")
	private Boolean esVentana;

	@Column(name="es_pasillo")
	private Boolean esPasillo;

	@Column(name="es_emergencia")
	private Boolean esEmergencia;

	// Relaciones JPA existentes intactas
	@ManyToOne
	@JoinColumn(name="id_avion")
	private Avion avion;

	@ManyToOne
	@JoinColumn(name="id_clase")
	private ClaseAsiento claseAsiento;

	@OneToMany(mappedBy="asiento")
	private List<ReservaAsiento> reservaAsientos;

	public Asiento() {
	}

	// Getters y Setters existentes
	public Integer getIdAsiento() { return this.idAsiento; }
	public void setIdAsiento(Integer idAsiento) { this.idAsiento = idAsiento; }

	public String getNumeroAsiento() { return this.numeroAsiento; }
	public void setNumeroAsiento(String numeroAsiento) { this.numeroAsiento = numeroAsiento; }

	public Avion getAvion() { return this.avion; }
	public void setAvion(Avion avion) { this.avion = avion; }

	public ClaseAsiento getClaseAsiento() { return this.claseAsiento; }
	public void setClaseAsiento(ClaseAsiento claseAsiento) { this.claseAsiento = claseAsiento; }

	public List<ReservaAsiento> getReservaAsientos() { return this.reservaAsientos; }
	public void setReservaAsientos(List<ReservaAsiento> reservaAsientos) { this.reservaAsientos = reservaAsientos; }

	// Getters y Setters de los Nuevos Campos
	public Integer getFila() { return fila; }
	public void setFila(Integer fila) { this.fila = fila; }

	public String getLetra() { return letra; }
	public void setLetra(String letra) { this.letra = letra; }

	public Boolean getEsVentana() { return esVentana; }
	public void setEsVentana(Boolean esVentana) { this.esVentana = esVentana; }

	public Boolean getEsPasillo() { return esPasillo; }
	public void setEsPasillo(Boolean esPasillo) { this.esPasillo = esPasillo; }

	public Boolean getEsEmergencia() { return esEmergencia; }
	public void setEsEmergencia(Boolean esEmergencia) { this.esEmergencia = esEmergencia; }

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