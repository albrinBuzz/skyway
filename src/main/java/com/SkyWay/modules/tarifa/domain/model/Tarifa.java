package com.SkyWay.modules.tarifa.domain.model;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@NamedQuery(name = "Tarifa.findAll", query = "SELECT t FROM Tarifa t")
@Table(name = "Tarifa")
public class Tarifa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tarifa_seq")
    @SequenceGenerator(name = "tarifa_seq", sequenceName = "tarifa_seq", allocationSize = 1)
    @Column(name = "ID_TARIFA")
    private Integer idTarifa;

    @Column(name = "Nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    // bi-directional one-to-many association to TarifaCaracteristica
    @OneToMany(mappedBy = "tarifa", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<TarifaCaracteristica> tarifaCaracteristicas;

    public Tarifa() {}

    // Getters y setters
    public Integer getIdTarifa() {
        return idTarifa;
    }

    public void setIdTarifa(Integer idTarifa) {
        this.idTarifa = idTarifa;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<TarifaCaracteristica> getTarifaCaracteristicas() {
        return tarifaCaracteristicas;
    }

    public void setTarifaCaracteristicas(List<TarifaCaracteristica> tarifaCaracteristicas) {
        this.tarifaCaracteristicas = tarifaCaracteristicas;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Tarifa{");
        sb.append("nombre='").append(nombre).append('\'');
        sb.append(", idTarifa=").append(idTarifa);
        sb.append('}');
        return sb.toString();
    }
}
