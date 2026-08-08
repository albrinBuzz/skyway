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
                        @ColumnResult(name = "hora_salida", type = java.sql.Timestamp.class)
                }
        )
)

public class ItinerarioResumenDTO {

    private int idItinerario;
    private String codigoIataOrigen;
    private String codigoIataDestino;
    private Date horaSalida;
    private int idReserva;

    public ItinerarioResumenDTO(int idItinerario, String codigoIataOrigen, String codigoIataDestino, Date horaSalida, int idReserva) {
        this.idItinerario = idItinerario;
        this.codigoIataOrigen = codigoIataOrigen;
        this.codigoIataDestino = codigoIataDestino;
        this.horaSalida = horaSalida;
        this.idReserva = idReserva;
    }

    // Getters y setters


    // Getters y Setters


    public int getIdItinerario() {
        return idItinerario;
    }

    public void setIdItinerario(int idItinerario) {
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

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ItinerarioResumenDTO{");
        sb.append("idItinerario=").append(idItinerario);
        sb.append(", codigoIataOrigen='").append(codigoIataOrigen).append('\'');
        sb.append(", codigoIataDestino='").append(codigoIataDestino).append('\'');
        sb.append(", horaSalida=").append(horaSalida);
        sb.append('}');
        return sb.toString();
    }
}
