package com.SkyWay.modules.vuelo.presentation.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resumen de impacto previo y resultado final de una cancelación operacional de vuelo.
 */
public class ResultadoCancelacionVueloDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean permitido;
    private boolean ejecutado;
    private Integer idVuelo;
    private String numeroVuelo;
    private String ruta;
    private String estadoActual;

    // Métricas de previsualización (simularCancelacionVuelo)
    private long reservasActivas;
    private long reservasMultiTramo;
    private long pasajerosAfectados;
    private long asientosVendidos;
    private long checkinsActivos;
    private long turnosActivos;

    // Métricas confirmadas desde BD (fn_cancelar_vuelo_operacional)
    private int reservasCanceladas;
    private int asientosLiberados;
    private int checkinsAnulados;
    private int turnosCancelados;
    private int pasajerosNotificados;

    private List<ItinerarioImpactoDTO> itinerariosImpactados = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private String mensajeResumen;

    public boolean isPermitido() { return permitido; }
    public void setPermitido(boolean permitido) { this.permitido = permitido; }

    public boolean isEjecutado() { return ejecutado; }
    public void setEjecutado(boolean ejecutado) { this.ejecutado = ejecutado; }

    public Integer getIdVuelo() { return idVuelo; }
    public void setIdVuelo(Integer idVuelo) { this.idVuelo = idVuelo; }

    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }

    public String getEstadoActual() { return estadoActual; }
    public void setEstadoActual(String estadoActual) { this.estadoActual = estadoActual; }

    public long getReservasActivas() { return reservasActivas; }
    public void setReservasActivas(long reservasActivas) { this.reservasActivas = reservasActivas; }

    public long getReservasMultiTramo() { return reservasMultiTramo; }
    public void setReservasMultiTramo(long reservasMultiTramo) { this.reservasMultiTramo = reservasMultiTramo; }

    public long getPasajerosAfectados() { return pasajerosAfectados; }
    public void setPasajerosAfectados(long pasajerosAfectados) { this.pasajerosAfectados = pasajerosAfectados; }

    public long getAsientosVendidos() { return asientosVendidos; }
    public void setAsientosVendidos(long asientosVendidos) { this.asientosVendidos = asientosVendidos; }

    public long getCheckinsActivos() { return checkinsActivos; }
    public void setCheckinsActivos(long checkinsActivos) { this.checkinsActivos = checkinsActivos; }

    public long getTurnosActivos() { return turnosActivos; }
    public void setTurnosActivos(long turnosActivos) { this.turnosActivos = turnosActivos; }

    public int getReservasCanceladas() { return reservasCanceladas; }
    public void setReservasCanceladas(int reservasCanceladas) { this.reservasCanceladas = reservasCanceladas; }

    public int getAsientosLiberados() { return asientosLiberados; }
    public void setAsientosLiberados(int asientosLiberados) { this.asientosLiberados = asientosLiberados; }

    public int getCheckinsAnulados() { return checkinsAnulados; }
    public void setCheckinsAnulados(int checkinsAnulados) { this.checkinsAnulados = checkinsAnulados; }

    public int getTurnosCancelados() { return turnosCancelados; }
    public void setTurnosCancelados(int turnosCancelados) { this.turnosCancelados = turnosCancelados; }

    public int getPasajerosNotificados() { return pasajerosNotificados; }
    public void setPasajerosNotificados(int pasajerosNotificados) { this.pasajerosNotificados = pasajerosNotificados; }

    public List<ItinerarioImpactoDTO> getItinerariosImpactados() { return itinerariosImpactados; }
    public void setItinerariosImpactados(List<ItinerarioImpactoDTO> itinerariosImpactados) {
        this.itinerariosImpactados = itinerariosImpactados != null ? itinerariosImpactados : new ArrayList<>();
    }

    public List<String> getAdvertencias() { return advertencias; }
    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias != null ? advertencias : new ArrayList<>();
    }

    public String getMensajeResumen() { return mensajeResumen; }
    public void setMensajeResumen(String mensajeResumen) { this.mensajeResumen = mensajeResumen; }
}
