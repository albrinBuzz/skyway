package com.SkyWay.modules.itinerario.presentation.dto;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;

import java.util.Date;

@SqlResultSetMapping(
        name = "ItinerarioResumenDTOMapping",
        classes = @ConstructorResult(
                targetClass = ItinerarioResumenDTO.class,
                columns = {
                        @ColumnResult(name = "id_itinerario", type = Integer.class),
                        @ColumnResult(name = "codigo_iata_origen", type = String.class),
                        @ColumnResult(name = "codigo_iata_destino", type = String.class),
                        @ColumnResult(name = "hora_salida", type = java.sql.Timestamp.class),
                        @ColumnResult(name = "id_reserva", type = Integer.class),
                        @ColumnResult(name = "hora_llegada", type = java.sql.Timestamp.class)
                }
        )
)
public class ItinerarioResumenDTO {

    private Integer idItinerario;
    private String codigoIataOrigen;
    private String codigoIataDestino;
    private Date horaSalida;
    private Integer idReserva;
    private Date horaLlegada;

    // Constructor completo
    public ItinerarioResumenDTO(Integer idItinerario, String codigoIataOrigen, String codigoIataDestino, Date horaSalida, Integer idReserva, Date horaLlegada) {
        this.idItinerario = idItinerario;
        this.codigoIataOrigen = codigoIataOrigen;
        this.codigoIataDestino = codigoIataDestino;
        this.horaSalida = horaSalida;
        this.idReserva = idReserva;
        this.horaLlegada = horaLlegada;
    }

    // Constructor alternativo (compatibilidad de 5 parámetros)
    public ItinerarioResumenDTO(Integer idItinerario, String codigoIataOrigen, String codigoIataDestino, Date horaSalida, Integer idReserva) {
        this(idItinerario, codigoIataOrigen, codigoIataDestino, horaSalida, idReserva, null);
    }

    // Getters y Setters
    public Integer getIdItinerario() {
        return idItinerario;
    }

    public void setIdItinerario(Integer idItinerario) {
        this.idItinerario = idItinerario;
    }

    public String getCodigoIataOrigen() {
        return codigoIataOrigen;
    }

    public void setCodigoIataOrigen(String codigoIataOrigen) {
        this.codigoIataOrigen = codigoIataOrigen;
    }

    public String getCodigoIataDestino() {
        return codigoIataDestino;
    }

    public void setCodigoIataDestino(String codigoIataDestino) {
        this.codigoIataDestino = codigoIataDestino;
    }

    public Date getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(Date horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Integer getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Integer idReserva) {
        this.idReserva = idReserva;
    }

    public Date getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(Date horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    @Override
    public String toString() {
        return "ItinerarioResumenDTO{" +
                "idItinerario=" + idItinerario +
                ", codigoIataOrigen='" + codigoIataOrigen + '\'' +
                ", codigoIataDestino='" + codigoIataDestino + '\'' +
                ", horaSalida=" + horaSalida +
                ", idReserva=" + idReserva +
                ", horaLlegada=" + horaLlegada +
                '}';
    }
}