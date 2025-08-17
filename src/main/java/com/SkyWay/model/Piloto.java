package com.SkyWay.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;


/**
 * The persistent class for the piloto database table.
 * 
 */
/*@Entity
@NamedQuery(name="Piloto.findAll", query="SELECT p FROM Piloto p")*/
public class Piloto extends Usuario implements Serializable {
	private static final long serialVersionUID = 1L;

	/*@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "piloto_seq")
	@SequenceGenerator(name = "piloto_seq",sequenceName = "piloto_seq",allocationSize = 1)
	@Column(name="rut")
	private String rutPiloto;

	private String apellido;

	private String contrasena;

	@Column(name="correo_electronico")
	private String correoElectronico;

	@Column(name="documento_identidad")
	private String documentoIdentidad;



	@Temporal(TemporalType.DATE)
	@Column(name="fecha_nacimiento")
	private Date fechaNacimiento;
	private String nombre;*/
	
	@Column(name="experiencia_anos")
	private Integer experienciaAnos;

	private String licencia;

	@Column(name="correo_electronico")
	private String correoElectronico;

	//bi-directional many-to-one association to Vuelo
	@OneToMany(mappedBy="piloto")
	@JsonIgnore
	private List<Vuelo> vuelos;

	public Piloto() {
	}
	

	public Piloto(String rutUsuario, String apellido, String contrasena, String correoElectronico,
			String documentoIdentidad, Date fechaNacimiento, String nombre, String rol, String telefono,
			Integer experienciaAnos, String licencia) {
		super(rutUsuario, apellido, contrasena, correoElectronico, documentoIdentidad, fechaNacimiento, nombre, null,
				telefono);
		this.experienciaAnos = experienciaAnos;
		this.licencia = licencia;
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
	
	public String getRutPiloto() {
		return rutPiloto;
	}



	public void setRutPiloto(String rutPiloto) {
		this.rutPiloto = rutPiloto;
	}*/
	
	
	
	


	public Integer getExperienciaAnos() {
		return this.experienciaAnos;
	}

	


	public void setExperienciaAnos(Integer experienciaAnos) {
		this.experienciaAnos = experienciaAnos;
	}




	public String getLicencia() {
		return this.licencia;
	}

	public void setLicencia(String licencia) {
		this.licencia = licencia;
	}




	public List<Vuelo> getVuelos() {
		return this.vuelos;
	}

	public void setVuelos(List<Vuelo> vuelos) {
		this.vuelos = vuelos;
	}

	public Vuelo addVuelo(Vuelo vuelo) {
		getVuelos().add(vuelo);
		vuelo.setPiloto(this);

		return vuelo;
	}

	public Vuelo removeVuelo(Vuelo vuelo) {
		getVuelos().remove(vuelo);
		vuelo.setPiloto(null);

		return vuelo;
	}

	@Override
	public String getNombre() {
		return super.getNombre();
	}


	public String getCorreoElectronico() {
		return this.correoElectronico;
	}

	public void setCorreoElectronico(String correoElectronico) {
		this.correoElectronico = correoElectronico;
	}


	@Override
	public String toString() {
		return "Piloto [experienciaAnos=" + experienciaAnos + ", licencia=" + licencia + "]";
	}





}