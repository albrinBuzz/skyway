package com.SkyWay.modules.usuario.domain.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.rolusuario.domain.model.Rolusuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;


/**
 * The persistent class for the usuario database table.
 * 
 */
@Entity
@NamedQuery(name="Usuario.findAll", query="SELECT u FROM Usuario u")
public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "RUT", nullable = false, length = 12)
	private String rut;

	@Column(name = "Nombre", nullable = false, length = 255)
	private String nombre;

	@Column(name = "Apellido", nullable = false, length = 255)
	private String apellido;

	@Column(name = "Correo_Electronico", nullable = false, length = 100, unique = true)
	private String correoElectronico;

	@Column(name = "Telefono", nullable = false, length = 255)
	private String telefono;

	@Column(name = "Documento_Identidad", nullable = false, length = 20, unique = true)
	private String documentoIdentidad;

	@Column(name = "Fecha_Nacimiento", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date fechaNacimiento;

	@Column(name = "Contrasena", nullable = false, length = 100)
	private String contrasena;

	@Column(name = "Fecha_Registro", nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Timestamp fechaRegistro;

	//bi-directional many-to-one association to Rolusuario
	@OneToMany(mappedBy="usuario")
	private List<Rolusuario> rolusuarios;

	//bi-directional many-to-many association to Role
	@ManyToMany
	@JoinTable(
			name="rolusuario",
			joinColumns={
					@JoinColumn(name="rut_usuario", referencedColumnName="RUT") // Hacemos referencia a la columna correcta
			},
			inverseJoinColumns={
					@JoinColumn(name="id_rol")
			}
	)
	private List<Role> roles;


	public Usuario() {
	}

	public String getRut() {
		return this.rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public String getApellido() {
		return this.apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getContrasena() {
		return this.contrasena;
	}

	public void setContrasena(String contrasena) {
		this.contrasena = contrasena;
	}

	public String getCorreoElectronico() {
		return this.correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}

	public String getDocumentoIdentidad() {
		return this.documentoIdentidad;
	}

	public void setDocumentoIdentidad(String documentoIdentidad) {
		this.documentoIdentidad = documentoIdentidad;
	}

	public Date getFechaNacimiento() {
		return this.fechaNacimiento;
	}

	public void setFechaNacimiento(Date fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public Timestamp getFechaRegistro() {
		return this.fechaRegistro;
	}

	public void setFechaRegistro(Timestamp fechaRegistro) {
		this.fechaRegistro = fechaRegistro;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public List<Rolusuario> getRolusuarios() {
		return this.rolusuarios;
	}

	public void setRolusuarios(List<Rolusuario> rolusuarios) {
		this.rolusuarios = rolusuarios;
	}

	public Rolusuario addRolusuario(Rolusuario rolusuario) {
		getRolusuarios().add(rolusuario);
		rolusuario.setUsuario(this);

		return rolusuario;
	}

	public Rolusuario removeRolusuario(Rolusuario rolusuario) {
		getRolusuarios().remove(rolusuario);
		rolusuario.setUsuario(null);

		return rolusuario;
	}

	public List<Role> getRoles() {
		return this.roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

}