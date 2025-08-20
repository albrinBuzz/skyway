package com.SkyWay.modules.itinerario.presentation.dto;

import java.io.Serializable;

public class ItinerarioDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer itinerario;            // ID del itinerario
    private String origen;                 // Código IATA del aeropuerto de origen
    private String destino;                // Código IATA del aeropuerto de destino
    private String ciudadSalida;          // Ciudad y aeropuerto de salida
    private String ciudadLlegada;         // Ciudad y aeropuerto de llegada
    private String cantParadas;           // Cantidad de paradas
    private Integer precio;                // Precio del vuelo en formato CLP
    private String duracion;              // Duración del vuelo en formato "X h Y min"
    private String horaSalida24h;         // Hora de salida en formato 24 horas
    private String horaLlegada24h;        // Hora de llegada en formato 24 horas

    // Constructor vacío
    public ItinerarioDTO() {}

    // Constructor con parámetros
    public ItinerarioDTO(Integer itinerario, String origen, String destino, String ciudadSalida,
                         String ciudadLlegada, String cantParadas, Integer precio, String duracion,
                         String horaSalida24h, String horaLlegada24h) {
        this.itinerario = itinerario;
        this.origen = origen;
        this.destino = destino;
        this.ciudadSalida = ciudadSalida;
        this.ciudadLlegada = ciudadLlegada;
        this.cantParadas = cantParadas;
        this.precio = precio;
        this.duracion = duracion;
        this.horaSalida24h = horaSalida24h;
        this.horaLlegada24h = horaLlegada24h;
    }

    // Getters y Setters
    public Integer getItinerario() {
        return itinerario;
    }

    public void setItinerario(Integer itinerario) {
        this.itinerario = itinerario;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getCiudadSalida() {
        return ciudadSalida;
    }

    public void setCiudadSalida(String ciudadSalida) {
        this.ciudadSalida = ciudadSalida;
    }

    public String getCiudadLlegada() {
        return ciudadLlegada;
    }

    public void setCiudadLlegada(String ciudadLlegada) {
        this.ciudadLlegada = ciudadLlegada;
    }

    public String getCantParadas() {
        return cantParadas;
    }

    public void setCantParadas(String cantParadas) {
        this.cantParadas = cantParadas;
    }

    public void setPrecio(Integer precio) {
        this.precio = precio;
    }

    public Integer getPrecio() {
        return precio;
    }

    public String getDuracion() {
        return duracion;
    }

    public void setDuracion(String duracion) {
        this.duracion = duracion;
    }

    public String getHoraSalida24h() {
        return horaSalida24h;
    }

    public void setHoraSalida24h(String horaSalida24h) {
        this.horaSalida24h = horaSalida24h;
    }

    public String getHoraLlegada24h() {
        return horaLlegada24h;
    }

    public void setHoraLlegada24h(String horaLlegada24h) {
        this.horaLlegada24h = horaLlegada24h;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ItinerarioDTO{");
        sb.append("itinerario=").append(itinerario);
        sb.append(", origen='").append(origen).append('\'');
        sb.append(", destino='").append(destino).append('\'');
        sb.append(", ciudadSalida='").append(ciudadSalida).append('\'');
        sb.append(", ciudadLlegada='").append(ciudadLlegada).append('\'');
        sb.append(", cantParadas='").append(cantParadas).append('\'');
        sb.append(", precio='").append(precio).append('\'');
        sb.append(", duracion='").append(duracion).append('\'');
        sb.append(", horaSalida24h='").append(horaSalida24h).append('\'');
        sb.append(", horaLlegada24h='").append(horaLlegada24h).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
