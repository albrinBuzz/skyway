package com.SkyWay.modules.itinerario.presentation.dto;

import com.SkyWay.modules.vuelo.domain.model.Vuelo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Permutación válida de vuelos operables entre origen y destino comercial
 * con métricas de escalas, tiempos de conexión y ocupación para decisión financiera.
 */
public class CombinacionRutaSugeridaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private int numeroTramos;
    private int numeroEscalas;
    private List<Vuelo> vuelos = new ArrayList<>();
    private String secuenciaCodigos;
    private String rutaEscalas;
    private String horarioSalidaTexto;
    private String horarioLlegadaTexto;
    private String duracionTotalTexto;
    private long duracionTotalMinutos;
    private int capacidadMinima;
    private int asientosVendidosTotal;
    private int asientosLibresMinimos;
    private int ocupacionPromedioPct;
    private int ocupacionMaximaPct;
    private String etiquetaEstrategia;
    private String nivelEstrategia; // OPORTUNIDAD | BALANCEADA | CRITICA

    public int getNumeroTramos() { return numeroTramos; }
    public void setNumeroTramos(int numeroTramos) { this.numeroTramos = numeroTramos; }

    public int getNumeroEscalas() { return numeroEscalas; }
    public void setNumeroEscalas(int numeroEscalas) { this.numeroEscalas = numeroEscalas; }

    public List<Vuelo> getVuelos() { return vuelos; }
    public void setVuelos(List<Vuelo> vuelos) { this.vuelos = vuelos != null ? vuelos : new ArrayList<>(); }

    public String getSecuenciaCodigos() { return secuenciaCodigos; }
    public void setSecuenciaCodigos(String secuenciaCodigos) { this.secuenciaCodigos = secuenciaCodigos; }

    public String getRutaEscalas() { return rutaEscalas; }
    public void setRutaEscalas(String rutaEscalas) { this.rutaEscalas = rutaEscalas; }

    public String getHorarioSalidaTexto() { return horarioSalidaTexto; }
    public void setHorarioSalidaTexto(String horarioSalidaTexto) { this.horarioSalidaTexto = horarioSalidaTexto; }

    public String getHorarioLlegadaTexto() { return horarioLlegadaTexto; }
    public void setHorarioLlegadaTexto(String horarioLlegadaTexto) { this.horarioLlegadaTexto = horarioLlegadaTexto; }

    public String getDuracionTotalTexto() { return duracionTotalTexto; }
    public void setDuracionTotalTexto(String duracionTotalTexto) { this.duracionTotalTexto = duracionTotalTexto; }

    public long getDuracionTotalMinutos() { return duracionTotalMinutos; }
    public void setDuracionTotalMinutos(long duracionTotalMinutos) { this.duracionTotalMinutos = duracionTotalMinutos; }

    public int getCapacidadMinima() { return capacidadMinima; }
    public void setCapacidadMinima(int capacidadMinima) { this.capacidadMinima = capacidadMinima; }

    public int getAsientosVendidosTotal() { return asientosVendidosTotal; }
    public void setAsientosVendidosTotal(int asientosVendidosTotal) { this.asientosVendidosTotal = asientosVendidosTotal; }

    public int getAsientosLibresMinimos() { return asientosLibresMinimos; }
    public void setAsientosLibresMinimos(int asientosLibresMinimos) { this.asientosLibresMinimos = asientosLibresMinimos; }

    public int getOcupacionPromedioPct() { return ocupacionPromedioPct; }
    public void setOcupacionPromedioPct(int ocupacionPromedioPct) { this.ocupacionPromedioPct = ocupacionPromedioPct; }

    public int getOcupacionMaximaPct() { return ocupacionMaximaPct; }
    public void setOcupacionMaximaPct(int ocupacionMaximaPct) { this.ocupacionMaximaPct = ocupacionMaximaPct; }

    public String getEtiquetaEstrategia() { return etiquetaEstrategia; }
    public void setEtiquetaEstrategia(String etiquetaEstrategia) { this.etiquetaEstrategia = etiquetaEstrategia; }

    public String getNivelEstrategia() { return nivelEstrategia; }
    public void setNivelEstrategia(String nivelEstrategia) { this.nivelEstrategia = nivelEstrategia; }
}
