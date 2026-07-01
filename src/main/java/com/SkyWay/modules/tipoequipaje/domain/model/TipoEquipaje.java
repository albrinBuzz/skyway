package com.SkyWay.modules.tipoequipaje.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import jakarta.persistence.*;
import java.util.List;


/**
 * The persistent class for the tipo_equipaje database table.
 * 
 */
@Entity
@Table(name="tipo_equipaje")
@NamedQuery(name="TipoEquipaje.findAll", query="SELECT t FROM TipoEquipaje t")
public class TipoEquipaje implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "tipo_equipaje_seq")
	@SequenceGenerator(name = "tipo_equipaje_seq",sequenceName = "tipo_equipaje_seq",allocationSize = 1)
	@Column(name="id_tipo")
	private Integer idTipo;

	private String nombre;

	//bi-directional many-to-one association to Equipaje
	@OneToMany(mappedBy="tipoEquipaje")
	private List<Equipaje> equipajes;

	public TipoEquipaje() {
	}

	public Integer getIdTipo() {
		return this.idTipo;
	}

	public void setIdTipo(Integer idTipo) {
		this.idTipo = idTipo;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public List<Equipaje> getEquipajes() {
		return this.equipajes;
	}

	public void setEquipajes(List<Equipaje> equipajes) {
		this.equipajes = equipajes;
	}

	public Equipaje addEquipaje(Equipaje equipaje) {
		getEquipajes().add(equipaje);
		equipaje.setTipoEquipaje(this);

		return equipaje;
	}

	public Equipaje removeEquipaje(Equipaje equipaje) {
		getEquipajes().remove(equipaje);
		equipaje.setTipoEquipaje(null);

		return equipaje;
	}

}