package com.SkyWay.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


//@Entity
//@Table(name = "notificacion")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notifica_seq")
    @SequenceGenerator(name = "notifica_seq", sequenceName = "notifica_seq", allocationSize = 1)
    @Column(name = "id_notificacion")
    private Integer idNotificacion;

    @Column(name = "RUT", length = 12)
    private String rut;

    @Column(name = "titulo", length = 55, nullable = false)
    private String titulo;

    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "leida", nullable = false)
    private Boolean leida;

    
    public Notificacion() {
		// TODO Auto-generated constructor stub
	}
    
    public Notificacion(Integer idNotificacion, String rut, String titulo, String mensaje, LocalDateTime fecha,
			Boolean leida) {
		super();
		this.idNotificacion = idNotificacion;
		this.rut = rut;
		this.titulo = titulo;
		this.mensaje = mensaje;
		this.fecha = fecha;
		this.leida = leida;
	}

	// Getters y setters

    public Integer getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Integer idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Boolean getLeida() {
        return leida;
    }

    public void setLeida(Boolean leida) {
        this.leida = leida;
    }

    @Override
    public String toString() {
        return "Notificacion{" +
                "idNotificacion=" + idNotificacion +
                ", rut='" + rut + '\'' +
                ", titulo='" + titulo + '\'' +
                ", mensaje='" + mensaje + '\'' +
                ", fecha=" + fecha +
                ", leida=" + leida +
                '}';
    }
}