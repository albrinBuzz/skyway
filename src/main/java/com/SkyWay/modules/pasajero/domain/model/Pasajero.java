package com.SkyWay.modules.pasajero.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.persistence.*;
import java.util.Date;
import java.util.List;


/**
 * The persistent class for the pasajero database table.
 * 
 */
@Entity
@NamedQuery(name="Pasajero.findAll", query="SELECT p FROM Pasajero p")
public class Pasajero implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	//@GeneratedValue(strategy=GenerationType.AUTO)
	private String rut;

	@Temporal(TemporalType.DATE)
	@Column(name="fecha_nacimiento")
	private Date fechaNacimiento;

	private String nacionalidad;

	@Column(name="numero_documento")
	private String numeroDocumento;

	@Column(name="tipo_documento")
	private String tipoDocumento;

	//bi-directional many-to-one association to Equipaje
	@OneToMany(mappedBy="pasajero")
	private List<Equipaje> equipajes;

	//bi-directional one-to-one association to Usuario
	@OneToOne(cascade = CascadeType.ALL)
	@MapsId
	@JoinColumn(name="rut")
	private Usuario usuario;


	//bi-directional many-to-one association to Reserva
	@OneToMany(mappedBy="pasajero",fetch = FetchType.LAZY)
	private List<Reserva> reservas;

	public Pasajero() {
	}

	public String getRut() {
		return this.rut;
	}

	public void setRut(String rut) {
		this.rut = rut;
	}

	public Date getFechaNacimiento() {
		return this.fechaNacimiento;
	}

	public void setFechaNacimiento(Date fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getNacionalidad() {
		return this.nacionalidad;
	}

	public void setNacionalidad(String nacionalidad) {
		this.nacionalidad = nacionalidad;
	}

	public String getNumeroDocumento() {
		return this.numeroDocumento;
	}

	public void setNumeroDocumento(String numeroDocumento) {
		this.numeroDocumento = numeroDocumento;
	}

	public String getTipoDocumento() {
		return this.tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

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

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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
		final StringBuffer sb = new StringBuffer("Pasajero{");
		sb.append("rut='").append(rut).append('\'');
		sb.append(", fechaNacimiento=").append(fechaNacimiento);
		sb.append(", nacionalidad='").append(nacionalidad).append('\'');
		sb.append(", numeroDocumento='").append(numeroDocumento).append('\'');
		sb.append(", tipoDocumento='").append(tipoDocumento).append('\'');
		sb.append(", usuario=").append(usuario.toString());
		sb.append('}');
		return sb.toString();
	}
}