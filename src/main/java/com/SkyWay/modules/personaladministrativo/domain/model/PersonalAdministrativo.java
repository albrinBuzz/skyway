package com.SkyWay.modules.personaladministrativo.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the personal_administrativo database table.
 * 
 */
@Entity
@Table(name="personal_administrativo")
@NamedQuery(name="PersonalAdministrativo.findAll", query="SELECT p FROM PersonalAdministrativo p")
public class PersonalAdministrativo implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	private String rut;

	private String cargo;

	private String departamento;

	@Column(name="experiencia_anios")
	private Integer experienciaAnios;

	@Column(name="fecha_contratacion")
	private Timestamp fechaContratacion;

	@Column(name="nivel_acceso")
	private String nivelAcceso;

	@Column(name="titulo_profesional")
	private String tituloProfesional;

	//bi-directional one-to-one association to Usuario
	@OneToOne
	@JoinColumn(name="rut")
	private Usuario usuario;

	public PersonalAdministrativo() {
	}

	public String getRut() {
		return this.rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public String getCargo() {
		return this.cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getDepartamento() {
		return this.departamento;
	}

	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}

	public Integer getExperienciaAnios() {
		return this.experienciaAnios;
	}

	public void setExperienciaAnios(Integer experienciaAnios) {
		this.experienciaAnios = experienciaAnios;
	}

	public Timestamp getFechaContratacion() {
		return this.fechaContratacion;
	}

	public void setFechaContratacion(Timestamp fechaContratacion) {
		this.fechaContratacion = fechaContratacion;
	}

	public String getNivelAcceso() {
		return this.nivelAcceso;
	}

	public void setNivelAcceso(String nivelAcceso) {
		this.nivelAcceso = nivelAcceso;
	}

	public String getTituloProfesional() {
		return this.tituloProfesional;
	}

	public void setTituloProfesional(String tituloProfesional) {
		this.tituloProfesional = tituloProfesional;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}