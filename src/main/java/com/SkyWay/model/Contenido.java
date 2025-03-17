package com.SkyWay.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class Contenido {

    @Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "contenido_seq")
	@SequenceGenerator(name = "contenido_seq",sequenceName = "contenido_seq",allocationSize = 1)
    private Integer id;

    @Column(length = 255)
    private String tipo;

    @Column(length = 255)
    private String titulo;

    @Column(length = 255)
    private String seccion;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSeccion() {
        return seccion;
    }

    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
}