package com.SkyWay.modules.tarifaItinerario.domain.model;


import java.io.Serializable;
import java.math.BigDecimal;

import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import jakarta.persistence.*;

@Entity
@Table(name = "itinerario_tarifa", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_itinerario", "id_tarifa"})
})
@NamedQuery(name = "ItinerarioTarifa.findAll", query = "SELECT it FROM ItinerarioTarifa it")
public class ItinerarioTarifa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "itinerario_tarifa_seq")
    @SequenceGenerator(name = "itinerario_tarifa_seq", sequenceName = "itinerario_tarifa_seq", allocationSize = 1)
    @Column(name = "id_itinerario_tarifa")
    private Integer idItinerarioTarifa;

    @ManyToOne
    @JoinColumn(name = "id_itinerario", nullable = false)
    private Itinerario itinerario;

    @ManyToOne
    @JoinColumn(name = "id_tarifa", nullable = false)
    private Tarifa tarifa;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    public ItinerarioTarifa() {
    }

    // Getters y Setters

    public Integer getIdItinerarioTarifa() {
        return idItinerarioTarifa;
    }

    public void setIdItinerarioTarifa(Integer idItinerarioTarifa) {
        this.idItinerarioTarifa = idItinerarioTarifa;
    }

    public Itinerario getItinerario() {
        return itinerario;
    }

    public void setItinerario(Itinerario itinerario) {
        this.itinerario = itinerario;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ItinerarioTarifa{");
        sb.append("idItinerarioTarifa=").append(idItinerarioTarifa);
        sb.append(", itinerario=").append(itinerario.getIdItinerario());
        sb.append(", tarifa=").append(tarifa.getIdTarifa());
        sb.append(", precio=").append(precio);
        sb.append('}');
        return sb.toString();
    }
}
