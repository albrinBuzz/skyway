package com.SkyWay.dto;
import java.time.LocalDateTime;
import java.util.HashMap;

public class VueloDTO {
    private Integer idVuelo;
    private LocalDateTime fechaHoraLlegada;
    private LocalDateTime fechaHoraSalida;
    private String numeroVuelo;
    private Integer precio;  // Precio general del vuelo (puede ser un precio base)
    private Integer aeropuerto1Id;   // ID del aeropuerto de llegada
    private Integer aeropuerto2Id;   // ID del aeropuerto de salida
    private Integer avionId;         // ID del avión
    private Integer estadoVueloId;   // ID del estado del vuelo
    private String pilotoRut;        // RUT del piloto
    private HashMap<Integer, Integer> preciosClases; // Mapa de precios de las clases (ID de clase -> precio)

    // Constructor vacío
    public VueloDTO() {}

    // Constructor con todos los parámetros
    public VueloDTO(Integer idVuelo, LocalDateTime fechaHoraLlegada, LocalDateTime fechaHoraSalida, 
                    String numeroVuelo, Integer precio, Integer aeropuerto1Id, Integer aeropuerto2Id, 
                    Integer avionId, Integer estadoVueloId, String pilotoRut, 
                    HashMap<Integer, Integer> preciosClases) {
        this.idVuelo = idVuelo;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.numeroVuelo = numeroVuelo;
        this.precio = precio;
        this.aeropuerto1Id = aeropuerto1Id;
        this.aeropuerto2Id = aeropuerto2Id;
        this.avionId = avionId;
        this.estadoVueloId = estadoVueloId;
        this.pilotoRut = pilotoRut;
        this.preciosClases = preciosClases;
    }

    // Getters and Setters
    public Integer getIdVuelo() {
        return idVuelo;
    }

    public void setIdVuelo(Integer idVuelo) {
        this.idVuelo = idVuelo;
    }

    public LocalDateTime getFechaHoraLlegada() {
        return fechaHoraLlegada;
    }

    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) {
        this.fechaHoraLlegada = fechaHoraLlegada;
    }

    public LocalDateTime getFechaHoraSalida() {
        return fechaHoraSalida;
    }

    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) {
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public Integer getPrecio() {
        return precio;
    }

    public void setPrecio(Integer precio) {
        this.precio = precio;
    }

    public Integer getAeropuerto1Id() {
        return aeropuerto1Id;
    }

    public void setAeropuerto1Id(Integer aeropuerto1Id) {
        this.aeropuerto1Id = aeropuerto1Id;
    }

    public Integer getAeropuerto2Id() {
        return aeropuerto2Id;
    }

    public void setAeropuerto2Id(Integer aeropuerto2Id) {
        this.aeropuerto2Id = aeropuerto2Id;
    }

    public Integer getAvionId() {
        return avionId;
    }

    public void setAvionId(Integer avionId) {
        this.avionId = avionId;
    }

    public Integer getEstadoVueloId() {
        return estadoVueloId;
    }

    public void setEstadoVueloId(Integer estadoVueloId) {
        this.estadoVueloId = estadoVueloId;
    }

    public String getPilotoRut() {
        return pilotoRut;
    }

    public void setPilotoRut(String pilotoRut) {
        this.pilotoRut = pilotoRut;
    }

    public HashMap<Integer, Integer> getPreciosClases() {
        return preciosClases;
    }

    public void setPreciosClases(HashMap<Integer, Integer> preciosClases) {
        this.preciosClases = preciosClases;
    }

    @Override
    public String toString() {
        return "VueloDTO{" +
                "idVuelo=" + idVuelo +
                ", fechaHoraLlegada=" + fechaHoraLlegada +
                ", fechaHoraSalida=" + fechaHoraSalida +
                ", numeroVuelo='" + numeroVuelo + '\'' +
                ", precio=" + precio +
                ", aeropuerto1Id=" + aeropuerto1Id +
                ", aeropuerto2Id=" + aeropuerto2Id +
                ", avionId=" + avionId +
                ", estadoVueloId=" + estadoVueloId +
                ", pilotoRut='" + pilotoRut + '\'' +
                ", preciosClases=" + preciosClases +
                '}';
    }
}
