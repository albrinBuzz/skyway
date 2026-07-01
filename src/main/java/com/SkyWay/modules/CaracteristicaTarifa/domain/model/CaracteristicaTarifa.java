package com.SkyWay.modules.CaracteristicaTarifa.domain.model;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NamedQuery(name = "CaracteristicaTarifa.findAll", query = "SELECT c FROM CaracteristicaTarifa c")
@Table(name = "Caracteristica_Tarifa")
public class CaracteristicaTarifa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "caracteristica_tarifa_seq")
    @SequenceGenerator(name = "caracteristica_tarifa_seq", sequenceName = "caracteristica_tarifa_seq", allocationSize = 1)
    @Column(name = "ID_CARACTERISTICA")
    private Integer idCaracteristica;

    @Column(name = "Nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "Descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "Tipo_Dato", nullable = false, length = 20)
    private String tipoDato;

    // bi-directional one-to-many association to TarifaCaracteristica
    @OneToMany(mappedBy = "caracteristica", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TarifaCaracteristica> tarifaCaracteristicas;

    public CaracteristicaTarifa() {}

    // Getters y setters
    public Integer getIdCaracteristica() {
        return idCaracteristica;
    }

    public void setIdCaracteristica(Integer idCaracteristica) {
        this.idCaracteristica = idCaracteristica;
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

    public String getTipoDato() {
        return tipoDato;
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato = tipoDato;
    }

    public List<TarifaCaracteristica> getTarifaCaracteristicas() {
        return tarifaCaracteristicas;
    }

    public void setTarifaCaracteristicas(List<TarifaCaracteristica> tarifaCaracteristicas) {
        this.tarifaCaracteristicas = tarifaCaracteristicas;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CaracteristicaTarifa{");
        sb.append("tipoDato='").append(tipoDato).append('\'');
        sb.append(", descripcion='").append(descripcion).append('\'');
        sb.append(", nombre='").append(nombre).append('\'');
        sb.append(", idCaracteristica=").append(idCaracteristica);
        sb.append('}');
        return sb.toString();
    }
}
