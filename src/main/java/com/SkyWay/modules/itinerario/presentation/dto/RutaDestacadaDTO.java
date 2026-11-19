package com.SkyWay.modules.itinerario.presentation.dto;

import java.io.Serializable;

public class RutaDestacadaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String origenIata;
    private final String origenNombre;
    private final String origenCiudad;
    private final String origenPais;
    private final String destinoIata;
    private final String destinoNombre;
    private final String destinoCiudad;
    private final String destinoPais;
    private final int precioMinimo;
    private final int escalasMinimas;
    private final long cantidadOpciones;
    private final String fechaUrl;
    private final String fechaLabel;

    public RutaDestacadaDTO(String origenIata, String origenNombre, String origenCiudad, String origenPais,
                            String destinoIata, String destinoNombre, String destinoCiudad, String destinoPais,
                            int precioMinimo, int escalasMinimas, long cantidadOpciones,
                            String fechaUrl, String fechaLabel) {
        this.origenIata = origenIata;
        this.origenNombre = origenNombre;
        this.origenCiudad = origenCiudad;
        this.origenPais = origenPais;
        this.destinoIata = destinoIata;
        this.destinoNombre = destinoNombre;
        this.destinoCiudad = destinoCiudad;
        this.destinoPais = destinoPais;
        this.precioMinimo = precioMinimo;
        this.escalasMinimas = escalasMinimas;
        this.cantidadOpciones = cantidadOpciones;
        this.fechaUrl = fechaUrl;
        this.fechaLabel = fechaLabel;
    }

    public String getOrigenIata() { return origenIata; }
    public String getOrigenNombre() { return origenNombre; }
    public String getOrigenCiudad() { return origenCiudad; }
    public String getOrigenPais() { return origenPais; }
    public String getDestinoIata() { return destinoIata; }
    public String getDestinoNombre() { return destinoNombre; }
    public String getDestinoCiudad() { return destinoCiudad; }
    public String getDestinoPais() { return destinoPais; }
    public int getPrecioMinimo() { return precioMinimo; }
    public int getEscalasMinimas() { return escalasMinimas; }
    public long getCantidadOpciones() { return cantidadOpciones; }
    public String getFechaUrl() { return fechaUrl; }
    public String getFechaLabel() { return fechaLabel; }
}
