package com.SkyWay.modules.modeloavion.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.fabricante.domain.model.Fabricante;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the modelo_avion database table.
 * 
 */
@Entity
@Table(name="modelo_avion")
@NamedQuery(name="ModeloAvion.findAll", query="SELECT m FROM ModeloAvion m")
public class ModeloAvion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "modelo_avion_seq")
	@SequenceGenerator(name = "modelo_avion_seq",sequenceName = "modelo_avion_seq",allocationSize = 1)
	@Column(name="id_modelo")
	private Integer idModelo;

	private String nombre;

	//bi-directional many-to-one association to Avion
	@OneToMany(mappedBy="modeloAvion")
	private List<Avion> avions;

	//bi-directional many-to-one association to Fabricante
	@ManyToOne
	@JoinColumn(name="id_fabricante")
	private Fabricante fabricante;

	public ModeloAvion() {
	}

	public Integer getIdModelo() {
		return this.idModelo;
	}

	public void setIdModelo(Integer idModelo) {
		this.idModelo = idModelo;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Avion> getAvions() {
		return this.avions;
	}

	public void setAvions(List<Avion> avions) {
		this.avions = avions;
	}

	public Avion addAvion(Avion avion) {
		getAvions().add(avion);
		avion.setModeloAvion(this);

		return avion;
	}

	public Avion removeAvion(Avion avion) {
		getAvions().remove(avion);
		avion.setModeloAvion(null);

		return avion;
	}

	public Fabricante getFabricante() {
		return this.fabricante;
	}

	public void setFabricante(Fabricante fabricante) {
		this.fabricante = fabricante;
	}

}