package com.SkyWay.modules.reserva.presentation.dto;

import java.sql.Timestamp;

public class TicketInfo {
    private String numeroVuelo;
    private Timestamp horaSalida;
    private Timestamp horaLlegada;
    private String codigoPuerta;
    private String terminal;
    private String numeroAsiento;
    private String claseAsiento;
    private String nombre;

    public TicketInfo() {
    }

    public TicketInfo(String numeroVuelo, Timestamp horaSalida, Timestamp horaLlegada, String codigoPuerta, String terminal, String numeroAsiento, String claseAsiento, String nombre) {
        this.numeroVuelo = numeroVuelo;
        this.horaSalida = horaSalida;
        this.horaLlegada = horaLlegada;
        this.codigoPuerta = codigoPuerta;
        this.terminal = terminal;
        this.numeroAsiento = numeroAsiento;
        this.claseAsiento = claseAsiento;
        this.nombre = nombre;
    }

    public String getNumeroVuelo() {
        return numeroVuelo;
    }

    public void setNumeroVuelo(String numeroVuelo) {
        this.numeroVuelo = numeroVuelo;
    }

    public Timestamp getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(Timestamp horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Timestamp getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(Timestamp horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public String getCodigoPuerta() {
        return codigoPuerta;
    }

    public void setCodigoPuerta(String codigoPuerta) {
        this.codigoPuerta = codigoPuerta;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getNumeroAsiento() {
        return numeroAsiento;
    }

    public void setNumeroAsiento(String numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
    }

    public String getClaseAsiento() {
        return claseAsiento;
    }

    public void setClaseAsiento(String claseAsiento) {
        this.claseAsiento = claseAsiento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("TicketInfo{");
        sb.append("numeroVuelo='").append(numeroVuelo).append('\'');
        sb.append(", horaSalida=").append(horaSalida);
        sb.append(", horaLlegada=").append(horaLlegada);
        sb.append(", codigoPuerta='").append(codigoPuerta).append('\'');
        sb.append(", terminal='").append(terminal).append('\'');
        sb.append(", numeroAsiento='").append(numeroAsiento).append('\'');
        sb.append(", claseAsiento='").append(claseAsiento).append('\'');
        sb.append('}');
        return sb.toString();
    }

    // Getters y setters
}
