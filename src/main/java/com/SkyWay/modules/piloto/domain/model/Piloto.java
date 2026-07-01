package com.SkyWay.modules.piloto.domain.model;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Piloto")
public class Piloto implements Serializable {

    @Id
    @Column(name = "RUT", nullable = false, length = 12)
    private String rut;  // Referencia a Usuario

    @Column(name = "Licencia", nullable = false, length = 20)
    private String licencia;

    @Column(name = "Experiencia_anos", nullable = false)
    private int experienciaAnos;

    @Column(name = "Especializaciones", columnDefinition = "TEXT")
    private String especializaciones;

    // Relación con la tabla Usuario (herencia por RUT)
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "RUT", referencedColumnName = "RUT")
    private Usuario usuario;

    // Getters y Setters
    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getLicencia() {
        return licencia;
    }

    public void setLicencia(String licencia) {
        this.licencia = licencia;
    }

    public int getExperienciaAnos() {
        return experienciaAnos;
    }

    public void setExperienciaAnos(int experienciaAnos) {
        this.experienciaAnos = experienciaAnos;
    }

    public String getEspecializaciones() {
        return especializaciones;
    }

    public void setEspecializaciones(String especializaciones) {
        this.especializaciones = especializaciones;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
