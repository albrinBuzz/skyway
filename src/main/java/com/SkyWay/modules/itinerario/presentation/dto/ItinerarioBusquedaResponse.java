package com.SkyWay.modules.itinerario.presentation.dto;



import java.util.List;

public class ItinerarioBusquedaResponse {
    private List<ItinerarioDTO> ida;
    private List<ItinerarioDTO> regreso;

    public ItinerarioBusquedaResponse(List<ItinerarioDTO> ida, List<ItinerarioDTO> regreso) {
        this.ida = ida;
        this.regreso = regreso;
    }

    public List<ItinerarioDTO> getIda() {
        return ida;
    }

    public void setIda(List<ItinerarioDTO> ida) {
        this.ida = ida;
    }

    public List<ItinerarioDTO> getRegreso() {
        return regreso;
    }

    public void setRegreso(List<ItinerarioDTO> regreso) {
        this.regreso = regreso;
    }
}
