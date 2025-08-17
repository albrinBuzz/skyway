package com.SkyWay.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;


/**
 * The persistent class for the pasajero database table.
 * 
 */
/*@Entity
@NamedQuery(name="Pasajero.findAll", query="SELECT p FROM Pasajero p")*/
public class Pasajero extends Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	
	

	
	/*@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "pasajero_seq")
	@SequenceGenerator(name = "pasajero_seq",sequenceName = "pasajero_seq",allocationSize = 1)
	@Column(name="rut")
	private String rutPasajero;

	private String apellido;

	private String contrasena;

	@Column(name="correo_electronico")
	private String correoElectronico;

	@Column(name="documento_identidad")
	private String documentoIdentidad;

	@Temporal(TemporalType.DATE)
	@Column(name="fecha_nacimiento")
	private Date fechaNacimiento;

	

	private String nombre;

	private String rol;

	private String telefono;*/

	//bi-directional many-to-one association to Equipaje
	@OneToMany(mappedBy="pasajero")
	private List<Equipaje> equipajes;

	//bi-directional many-to-one association to Reserva
	@OneToMany(mappedBy="pasajero")
	private List<Reserva> reservas;

	public Pasajero() {
	}
	
	
	public Pasajero(String rutUsuario, String apellido, String contrasena, String correoElectronico,
			String documentoIdentidad, Date fechaNacimiento, String nombre, String rol, String telefono) {
		super(rutUsuario, apellido, contrasena, correoElectronico, documentoIdentidad, fechaNacimiento, nombre, null,
				telefono);
	}

	

	/*public String getApellido() {
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

	public String getRol() {
		return this.rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getTelefono() {
		return this.telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	
	public String getRutPasajero() {
		return  rutPasajero;
	}



	public void setRutPasajero(String rutPasajero) {
		this.rutPasajero = rutPasajero;
	}*/




	public List<Equipaje> getEquipajes() {
		return this.equipajes;
	}

	public void setEquipajes(List<Equipaje> equipajes) {
		this.equipajes = equipajes;
	}

	public Equipaje addEquipaje(Equipaje equipaje) {
		getEquipajes().add(equipaje);
		equipaje.setPasajero(this);

		return equipaje;
	}

	public Equipaje removeEquipaje(Equipaje equipaje) {
		getEquipajes().remove(equipaje);
		equipaje.setPasajero(null);

		return equipaje;
	}

	public List<Reserva> getReservas() {
		return this.reservas;
	}

	public void setReservas(List<Reserva> reservas) {
		this.reservas = reservas;
	}

	public Reserva addReserva(Reserva reserva) {
		getReservas().add(reserva);
		reserva.setPasajero(this);

		return reserva;
	}

	public Reserva removeReserva(Reserva reserva) {
		getReservas().remove(reserva);
		reserva.setPasajero(null);

		return reserva;
	}

	
	@Override
	public String toString() {
		return "Pasajero [apellido=" + super.getApellido() + ", correoElectronico=" + super.getCorreoElectronico() + ", fechaNacimiento="
				+ super.getFechaNacimiento() + ", nombre=" + super.getNombre() + "]";
	}




	
	

}