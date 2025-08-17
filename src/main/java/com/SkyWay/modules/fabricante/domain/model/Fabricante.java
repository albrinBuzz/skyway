package com.SkyWay.modules.fabricante.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the fabricante database table.
 * 
 */
@Entity
@NamedQuery(name="Fabricante.findAll", query="SELECT f FROM Fabricante f")
public class Fabricante implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "fabricante_seq")
	@SequenceGenerator(name = "fabricante_seq",sequenceName = "fabricante_seq",allocationSize = 1)
	@Column(name="id_fabricante")
	private Integer idFabricante;

	private String nombre;

	//bi-directional many-to-one association to ModeloAvion
	@OneToMany(mappedBy="fabricante")
	private List<ModeloAvion> modeloAvions;

	public Fabricante() {
	}

	public Integer getIdFabricante() {
		return this.idFabricante;
	}

	public void setIdFabricante(Integer idFabricante) {
		this.idFabricante = idFabricante;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<ModeloAvion> getModeloAvions() {
		return this.modeloAvions;
	}

	public void setModeloAvions(List<ModeloAvion> modeloAvions) {
		this.modeloAvions = modeloAvions;
	}

	public ModeloAvion addModeloAvion(ModeloAvion modeloAvion) {
		getModeloAvions().add(modeloAvion);
		modeloAvion.setFabricante(this);

		return modeloAvion;
	}

	public ModeloAvion removeModeloAvion(ModeloAvion modeloAvion) {
		getModeloAvions().remove(modeloAvion);
		modeloAvion.setFabricante(null);

		return modeloAvion;
	}

}