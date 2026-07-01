package com.SkyWay.modules.PasajeroReserva.domain.model;




import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(
        name = "pasajero_reserva",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_reserva", "rut"})
)
public class PasajeroReserva implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator = "reserva_pasajero_seq")
    @SequenceGenerator(name = "reserva_pasajero_seq",sequenceName = "reserva_pasajero_seq",allocationSize = 1)
    @Column(name = "id_pasajero_reserva")
    private Integer idPasajeroReserva;

    // Relación con la reserva
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_reserva",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_pasajero_reserva_reserva")
    )
    private Reserva reserva;

    // Relación con el pasajero
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "rut",
            nullable = false,
            referencedColumnName = "rut",
            foreignKey = @ForeignKey(name = "fk_pasajero_reserva_pasajero")
    )
    private Pasajero pasajero;

    // Getters y Setters

    public Integer getId() {
        return idPasajeroReserva;
    }

    public void setId(Integer id) {
        this.idPasajeroReserva = id;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public void setPasajero(Pasajero pasajero) {
        this.pasajero = pasajero;
    }

    // toString
    @Override
    public String toString() {
        return "PasajeroReserva{" +
                "id=" + idPasajeroReserva +
                ", reserva=" + (reserva != null ? reserva.getIdReserva() : null) +
                ", pasajero=" + (pasajero != null ? pasajero.getRut() : null) +
                '}';
    }
}
