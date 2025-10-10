package com.SkyWay.modules.tarifa.domain.model;

import java.io.Serializable;

import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import jakarta.persistence.*;

@Entity
@NamedQuery(name = "Tarifa.findAll", query = "SELECT t FROM Tarifa t")
public class Tarifa implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tarifa_seq")
    @SequenceGenerator(name = "tarifa_seq", sequenceName = "tarifa_seq", allocationSize = 1)
    @Column(name = "id_tarifa")
    private Integer idTarifa;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "permite_cambios", nullable = false)
    private Boolean permiteCambios;

    @Column(name = "horas_minimas_cambio")
    private Integer horasMinimasCambio;

    @Column(name = "permite_cancelacion", nullable = false)
    private Boolean permiteCancelacion;

    @Column(name = "reembolso_permitido", nullable = false)
    private Boolean reembolsoPermitido;

    // Bi-directional one-to-many to ItinerarioTarifa
    @OneToMany(mappedBy = "tarifa", fetch = FetchType.LAZY)
    private java.util.List<ItinerarioTarifa> itinerarioTarifas;

    public Tarifa() {
    }

    // Getters y Setters
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

    public Boolean getPermiteCambios() {
        return permiteCambios;
    }

    public void setPermiteCambios(Boolean permiteCambios) {
        this.permiteCambios = permiteCambios;
    }

    public Integer getHorasMinimasCambio() {
        return horasMinimasCambio;
    }

    public void setHorasMinimasCambio(Integer horasMinimasCambio) {
        this.horasMinimasCambio = horasMinimasCambio;
    }

    public Boolean getPermiteCancelacion() {
        return permiteCancelacion;
    }

    public void setPermiteCancelacion(Boolean permiteCancelacion) {
        this.permiteCancelacion = permiteCancelacion;
    }

    public Boolean getReembolsoPermitido() {
        return reembolsoPermitido;
    }

    public void setReembolsoPermitido(Boolean reembolsoPermitido) {
        this.reembolsoPermitido = reembolsoPermitido;
    }

    public java.util.List<ItinerarioTarifa> getItinerarioTarifas() {
        return itinerarioTarifas;
    }

    public void setItinerarioTarifas(java.util.List<ItinerarioTarifa> itinerarioTarifas) {
        this.itinerarioTarifas = itinerarioTarifas;
    }
}
