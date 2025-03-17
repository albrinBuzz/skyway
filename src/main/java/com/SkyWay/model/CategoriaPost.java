package com.SkyWay.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table( name ="categoriapost")
public class CategoriaPost {
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "ciudad_seq")
	@SequenceGenerator(name = "ciudad_seq",sequenceName = "ciudad_seq",allocationSize = 1)
	@Column( name =  "id_categoria_post")
    private int idCategoriaPost;
    private String nombre;
    private String descripcion;

    // Constructor
    public CategoriaPost() {
		// TODO Auto-generated constructor stub
	}
    
    public CategoriaPost(int idCategoriaPost, String nombre, String descripcion) {
        this.idCategoriaPost = idCategoriaPost;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public int getIdCategoriaPost() {
        return idCategoriaPost;
    }

    public void setIdCategoriaPost(int idCategoriaPost) {
        this.idCategoriaPost = idCategoriaPost;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // toString()
    @Override
    public String toString() {
        return "CategoriaPost [idCategoriaPost=" + idCategoriaPost + ", nombre=" + nombre + ", descripcion=" + descripcion + "]";
    }
}
