package com.SkyWay.modules.reserva.presentation.dto;

import java.io.Serializable;
import java.sql.Timestamp;

public class TicketInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String codigoPnr;
    private String numeroVuelo;
    private Timestamp horaSalida;
    private Timestamp horaLlegada;
    private String codigoPuerta;
    private String terminal;
    private String numeroAsiento;
    private String claseAsiento;
    private String nombre;
    private String codigoQr;

    public TicketInfo() {}

    // Constructor que invoca automáticamente Hibernate por reflexión
    public TicketInfo(String codigoPnr, String numeroVuelo, Timestamp horaSalida, Timestamp horaLlegada,
                      String codigoPuerta, String terminal, String numeroAsiento,
                      String claseAsiento, String nombre, String codigoQr) {
        this.codigoPnr = codigoPnr;
        this.numeroVuelo = numeroVuelo;
        this.horaSalida = horaSalida;
        this.horaLlegada = horaLlegada;
        this.codigoPuerta = codigoPuerta;
        this.terminal = terminal;
        this.numeroAsiento = numeroAsiento;
        this.claseAsiento = claseAsiento;
        this.nombre = nombre;
        this.codigoQr = codigoQr;
    }

    public String getPuertaTerminal() {
        return (codigoPuerta != null ? codigoPuerta : "TBD") + " / " + (terminal != null ? terminal : "TBD");
    }

    // Getters y Setters
    public String getCodigoPnr() { return codigoPnr; }
    public void setCodigoPnr(String codigoPnr) { this.codigoPnr = codigoPnr; }

    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

    public Timestamp getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Timestamp horaSalida) { this.horaSalida = horaSalida; }

    public Timestamp getHoraLlegada() { return horaLlegada; }
    public void setHoraLlegada(Timestamp horaLlegada) { this.horaLlegada = horaLlegada; }

    public String getCodigoPuerta() { return codigoPuerta; }
    public void setCodigoPuerta(String codigoPuerta) { this.codigoPuerta = codigoPuerta; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public String getNumeroAsiento() { return numeroAsiento; }
    public void setNumeroAsiento(String numeroAsiento) { this.numeroAsiento = numeroAsiento; }

    public String getClaseAsiento() { return claseAsiento; }
    public void setClaseAsiento(String claseAsiento) { this.claseAsiento = claseAsiento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
}