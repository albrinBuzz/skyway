package com.SkyWay.modules.itinerario.presentation.dto;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class RutasPortalResumenDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final List<RutaDestacadaDTO> rutas;
    private final boolean historicas;

    public RutasPortalResumenDTO(List<RutaDestacadaDTO> rutas, boolean historicas) {
        this.rutas = rutas != null ? rutas : Collections.emptyList();
        this.historicas = historicas;
    }

    public List<RutaDestacadaDTO> getRutas() {
        return rutas;
    }

    public boolean isHistoricas() {
        return historicas;
    }
}
