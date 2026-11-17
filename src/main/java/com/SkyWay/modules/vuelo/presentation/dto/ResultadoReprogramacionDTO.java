package com.SkyWay.modules.vuelo.presentation.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado de validación o ejecución de una reprogramación horaria controlada de vuelo.
 */
public class ResultadoReprogramacionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean valido;
    private boolean ejecutado;
    private long asientosVendidos;
    private long reservasActivas;
    private long checkinsCompletados;
    private int itinerariosEvaluados;
    private int turnosEvaluados;
    private List<ItinerarioImpactoDTO> itinerariosImpactados = new ArrayList<>();
    private List<String> errores = new ArrayList<>();
    private List<String> advertencias = new ArrayList<>();
    private String resumenMensaje;

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public boolean isEjecutado() {
        return ejecutado;
    }

    public void setEjecutado(boolean ejecutado) {
        this.ejecutado = ejecutado;
    }

    public long getAsientosVendidos() {
        return asientosVendidos;
    }

    public void setAsientosVendidos(long asientosVendidos) {
        this.asientosVendidos = asientosVendidos;
    }

    public long getReservasActivas() {
        return reservasActivas;
    }

    public void setReservasActivas(long reservasActivas) {
        this.reservasActivas = reservasActivas;
    }

    public long getCheckinsCompletados() {
        return checkinsCompletados;
    }

    public void setCheckinsCompletados(long checkinsCompletados) {
        this.checkinsCompletados = checkinsCompletados;
    }

    public List<ItinerarioImpactoDTO> getItinerariosImpactados() {
        return itinerariosImpactados;
    }

    public void setItinerariosImpactados(List<ItinerarioImpactoDTO> itinerariosImpactados) {
        this.itinerariosImpactados = itinerariosImpactados != null ? itinerariosImpactados : new ArrayList<>();
    }

    public int getItinerariosEvaluados() {
        return itinerariosEvaluados;
    }

    public void setItinerariosEvaluados(int itinerariosEvaluados) {
        this.itinerariosEvaluados = itinerariosEvaluados;
    }

    public int getTurnosEvaluados() {
        return turnosEvaluados;
    }

    public void setTurnosEvaluados(int turnosEvaluados) {
        this.turnosEvaluados = turnosEvaluados;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores != null ? errores : new ArrayList<>();
    }

    public List<String> getAdvertencias() {
        return advertencias;
    }

    public void setAdvertencias(List<String> advertencias) {
        this.advertencias = advertencias != null ? advertencias : new ArrayList<>();
    }

    public String getResumenMensaje() {
        return resumenMensaje;
    }

    public void setResumenMensaje(String resumenMensaje) {
        this.resumenMensaje = resumenMensaje;
    }
}
