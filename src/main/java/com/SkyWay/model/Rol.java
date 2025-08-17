package com.SkyWay.model;

import jakarta.persistence.*;

//@Entity
//@Table(name = "Roles")
public class Rol {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "rol_seq")
    @SequenceGenerator(name = "rol_seq",sequenceName = "rol_seq",allocationSize = 1)
    private Integer id_rol;
    private String nombre;
    private String descripcion;



    public Rol() {

    }

    public Rol(Integer id_rol, String nombre, String descripcion) {
        this.id_rol = id_rol;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getId_rol() {
        return id_rol;
    }

    public void setId_rol(Integer id_rol) {
        this.id_rol = id_rol;
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
}
