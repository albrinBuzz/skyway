package com.SkyWay.modules.notificacion.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the notificacion database table.
 * 
 */
@Entity
@NamedQuery(name="Notificacion.findAll", query="SELECT n FROM Notificacion n")
public class Notificacion implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificacion_seq")
	@SequenceGenerator(name = "notificacion_seq", sequenceName = "notificacion_seq", allocationSize = 1)
	@Column(name="id_notificacion")
	private Integer idNotificacion;

	private Timestamp fecha;

	private Boolean leido;

	private String mensaje;

	private String titulo;

	//bi-directional many-to-one association to Usuario
	@ManyToOne
	@JoinColumn(name="rut_destinatario")
	private Usuario usuario;

	public Notificacion() {
	}

	public Integer getIdNotificacion() {
		return this.idNotificacion;
	}

	public void setIdNotificacion(Integer idNotificacion) {
		this.idNotificacion = idNotificacion;
	}

	public Timestamp getFecha() {
		return this.fecha;
	}

	public void setFecha(Timestamp fecha) {
		this.fecha = fecha;
	}

	public Boolean getLeido() {
		return this.leido;
	}

	public void setLeido(Boolean leido) {
		this.leido = leido;
	}

	public String getMensaje() {
		return this.mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

	public String getTitulo() {
		return this.titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public Usuario getUsuario() {
		return this.usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}