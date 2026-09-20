package com.SkyWay.modules.asiento.presentation.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BloqueAsientosDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<InfoAsientoDTO> asientos = new ArrayList<>();

    public BloqueAsientosDTO() {}

    public List<InfoAsientoDTO> getAsientos() { return asientos; }
    public void setAsientos(List<InfoAsientoDTO> asientos) { this.asientos = asientos; }
}