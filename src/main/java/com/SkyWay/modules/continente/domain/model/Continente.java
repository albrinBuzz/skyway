package com.SkyWay.modules.continente.domain.model;

import com.SkyWay.modules.pai.domain.model.Pai;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Entidad JPA para la tabla 'continente'
 */
@Entity
@org.hibernate.annotations.SQLDelete(sql = "UPDATE continente SET activo = false, fecha_baja = NOW() WHERE id_continente = ?")
@org.hibernate.annotations.SQLRestriction("activo = true")
@Table(name = "continente", schema = "public")
@NamedQuery(name = "Continente.findAll", query = "SELECT c FROM Continente c")
public class Continente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id_continente")
    private Integer idContinente;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "fecha_baja")
    private java.sql.Timestamp fechaBaja;

    public Boolean getActivo() { return this.activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public java.sql.Timestamp getFechaBaja() { return this.fechaBaja; }
    public void setFechaBaja(java.sql.Timestamp fechaBaja) { this.fechaBaja = fechaBaja; }

    // Relación bidireccional OneToMany con País (un continente tiene muchos países)
    @OneToMany(mappedBy = "continente", fetch = FetchType.LAZY)
    private List<Pai> paises;

    public Continente() {
    }

    public Continente(Integer idContinente, String nombre) {
        this.idContinente = idContinente;
        this.nombre = nombre;
    }

    // Getters y Setters
    public Integer getIdContinente() {
        return idContinente;
    }

    public void setIdContinente(Integer idContinente) {
        this.idContinente = idContinente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Pai> getPaises() {
        return paises;
    }

    public void setPaises(List<Pai> paises) {
        this.paises = paises;
    }
}