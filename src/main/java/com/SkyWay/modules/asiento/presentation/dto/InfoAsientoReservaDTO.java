package com.SkyWay.modules.asiento.presentation.dto;

public class InfoAsientoReservaDTO {

    private int idAsiento;
    private String numeroAsiento;
    private String claseAsiento;
    private int idVuelo;
    private int idReserva;

    public InfoAsientoReservaDTO(int idAsiento, String numeroAsiento, String claseAsiento, int idVuelo,int idReserva) {
        this.idAsiento = idAsiento;
        this.numeroAsiento = numeroAsiento;
        this.claseAsiento = claseAsiento;
        this.idVuelo = idVuelo;
        this.idReserva=idReserva;
    }

    public int getIdAsiento() {

        return idAsiento;
    }

    public void setIdAsiento(int idAsiento) {
        this.idAsiento = idAsiento;
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

    public int getIdVuelo() {
        return idVuelo;
    }

    public void setIdVuelo(int idVuelo) {
        this.idVuelo = idVuelo;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("InfoAsientoReservaDTO{");
        sb.append("idAsiento=").append(idAsiento);
        sb.append(", numeroAsiento='").append(numeroAsiento).append('\'');
        sb.append(", claseAsiento='").append(claseAsiento).append('\'');
        sb.append(", idVuelo=").append(idVuelo);
        sb.append(", idReserva=").append(idReserva);
        sb.append('}');
        return sb.toString();
    }
}
