package com.SkyWay.modules.asiento.presentation.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FilaCabinaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int numeroFila;
    private String clase;
    private boolean esEmergencia;
    private List<BloqueAsientosDTO> bloques = new ArrayList<>();

    public FilaCabinaDTO() {}

    public FilaCabinaDTO(int numeroFila, String clase, boolean esEmergencia) {
        this.numeroFila = numeroFila;
        this.clase = clase;
        this.esEmergencia = esEmergencia;
    }

    public int getNumeroFila() { return numeroFila; }
    public void setNumeroFila(int numeroFila) { this.numeroFila = numeroFila; }

    public String getClase() { return clase; }
    public void setClase(String clase) { this.clase = clase; }

    public boolean isEsEmergencia() { return esEmergencia; }
    public void setEsEmergencia(boolean esEmergencia) { this.esEmergencia = esEmergencia; }

    public List<BloqueAsientosDTO> getBloques() { return bloques; }
    public void setBloques(List<BloqueAsientosDTO> bloques) { this.bloques = bloques; }
}