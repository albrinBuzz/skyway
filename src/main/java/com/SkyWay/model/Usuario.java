package com.SkyWay.model;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;


/**
 * The persistent class for the usuario database table.
 * 
 */
@Entity
@NamedQuery(name="Usuario.findAll", query="SELECT u FROM Usuario u")
@Inheritance(strategy = InheritanceType.JOINED)
public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "pasajero_seq")
	//@SequenceGenerator(name = "pasajero_seq",sequenceName = "pasajero_seq",allocationSize = 1)
	@Column(name="rut")
	private String rutUsuario;

	private String apellido;

	private String contrasena;

	@Column(name="correo_electronico",unique = true)
	private String correoElectronico;

	@Column(name="documento_identidad")
	private String documentoIdentidad;

	@Temporal(TemporalType.DATE)
	@Column(name="fecha_nacimiento")
	private Date fechaNacimiento;

	private String nombre;

	@ManyToOne
	@JoinColumn(name = "id_rol")
	private Rol rol;


	private String telefono;

	public Usuario() {
	}

	public Usuario(String rutUsuario, String apellido, String contrasena, String correoElectronico,
			String documentoIdentidad, Date fechaNacimiento, String nombre, Rol rol, String telefono) {
		super();
		this.rutUsuario = rutUsuario;
		this.apellido = apellido;
		this.contrasena = contrasena;
		this.correoElectronico = correoElectronico;
		this.documentoIdentidad = documentoIdentidad;
		this.fechaNacimiento = fechaNacimiento;
		this.nombre = nombre;
		this.rol = rol;
		this.telefono = telefono;
	}

	public Usuario(String rut, String apellido, String contrasena, String correoElectronico, String documentoIdentidad, Date fechaNacimiento, String nombre, String telefono) {
		this.rutUsuario = rut;
		this.apellido = apellido;
		this.contrasena = contrasena;
		this.correoElectronico = correoElectronico;
		this.documentoIdentidad = documentoIdentidad;
		this.fechaNacimiento = fechaNacimiento;
		this.nombre = nombre;
		this.telefono = telefono;

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

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Rol getRol() {
		return this.rol;
	}

	public void setRol(Rol rol) {
		this.rol = rol;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public String getRutUsuario() {
		return rutUsuario;
	}

	public void setRutUsuario(String rutUsuario) {
		this.rutUsuario = rutUsuario;
	}

	@Override
	public String toString() {
		return "Usuario [rutUsuario=" + rutUsuario + ", apellido=" + apellido + ", contrasena=" + contrasena
				+ ", correoElectronico=" + correoElectronico + ", documentoIdentidad=" + documentoIdentidad
				+ ", fechaNacimiento=" + fechaNacimiento + ", nombre=" + nombre + ", rol=" + rol + ", telefono="
				+ telefono + "]";
	}
	
	

}