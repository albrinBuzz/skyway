package com.SkyWay.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "post_seq")
    @SequenceGenerator(name = "post_seq", sequenceName = "post_seq", allocationSize = 1)
    @Column(name = "id_post")
    private int idPost;

    @Column(name = "titulo", nullable = false, length = 255)
    private String titulo;

    @Column(name = "fecha", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date fecha;

    @Column(name = "contenido")
    private String contenido;
    
    //private String descripcion ;

 

    @ManyToOne
    @JoinColumn(name = "id_categoria_post", nullable = false)
    private CategoriaPost categoriaPost; // Relación con CategoriaPost

    // Constructor vacío
    public Post() {
    }

    // Constructor con parámetros
    public Post(int idPost, String titulo, Date fecha, String contenido, CategoriaPost categoriaPost) {
        this.idPost = idPost;
        this.titulo = titulo;
        this.fecha = fecha;
        this.contenido = contenido;

        this.categoriaPost = categoriaPost;
    }

    // Getters y Setters
    public int getIdPost() {
        return idPost;
    }

    public void setIdPost(int idPost) {
        this.idPost = idPost;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

 
    public CategoriaPost getCategoriaPost() {
        return categoriaPost;
    }

    public void setCategoriaPost(CategoriaPost categoriaPost) {
        this.categoriaPost = categoriaPost;
    }
    
   /* public String getDescripcion() {
		return descripcion;
	}
    
    public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}*/

    // toString()
    @Override
    public String toString() {
        return "Post [idPost=" + idPost + ", titulo=" + titulo + ", fecha=" + fecha + ", contenido=" + contenido 
            + " categoriaPost=" + categoriaPost + "]";
    }
}
