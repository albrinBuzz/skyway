package com.SkyWay.modules.TarifaCaracteristica.domain.model;


import com.SkyWay.modules.CaracteristicaTarifa.domain.model.CaracteristicaTarifa;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "Tarifa_Caracteristica")
public class TarifaCaracteristica implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tarifa_caracteristica_seq")
    @SequenceGenerator(name = "tarifa_caracteristica_seq", sequenceName = "tarifa_caracteristica_seq", allocationSize = 1)
    @Column(name = "ID_TARIFA_CARACTERISTICA")
    private Integer idTarifaCaracteristica;

    @ManyToOne
    @JoinColumn(name = "ID_TARIFA", nullable = false)
    private Tarifa tarifa;

    @ManyToOne
    @JoinColumn(name = "ID_CARACTERISTICA", nullable = false)
    private CaracteristicaTarifa caracteristica;

    @Column(name = "Valor", length = 100)
    private String valor;

    @Column(name = "Valor_Bool")
    private Boolean valorBool;

    @Column(name = "Valor_Int")
    private Integer valorInt;

    public TarifaCaracteristica() {}

    // Getters y setters
    public Integer getIdTarifaCaracteristica() {
        return idTarifaCaracteristica;
    }

    public void setIdTarifaCaracteristica(Integer idTarifaCaracteristica) {
        this.idTarifaCaracteristica = idTarifaCaracteristica;
    }

    public Tarifa getTarifa() {
        return tarifa;
    }

    public void setTarifa(Tarifa tarifa) {
        this.tarifa = tarifa;
    }

    public CaracteristicaTarifa getCaracteristica() {
        return caracteristica;
    }

    public void setCaracteristica(CaracteristicaTarifa caracteristica) {
        this.caracteristica = caracteristica;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Boolean getValorBool() {
        return valorBool;
    }

    public Integer getValorInt() {
        return valorInt;
    }

    public void setValorBool(Boolean valorBool) {
        this.valorBool = valorBool;
    }

    public void setValorInt(Integer valorInt) {
        this.valorInt = valorInt;
    }
}
