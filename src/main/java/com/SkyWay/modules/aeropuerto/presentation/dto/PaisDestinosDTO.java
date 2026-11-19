package com.SkyWay.modules.aeropuerto.presentation.dto;

import java.io.Serializable;
import java.util.List;

public class PaisDestinosDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String pais;
    private final List<AeropuertoItemDTO> aeropuertos;

    public PaisDestinosDTO(String pais, List<AeropuertoItemDTO> aeropuertos) {
        this.pais = pais;
        this.aeropuertos = aeropuertos;
    }

    public String getPais() {
        return pais;
    }

    public List<AeropuertoItemDTO> getAeropuertos() {
        return aeropuertos;
    }

    public static class AeropuertoItemDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String codigoIata;
        private final String nombreAeropuerto;
        private final String ciudad;

        public AeropuertoItemDTO(String codigoIata, String nombreAeropuerto, String ciudad) {
            this.codigoIata = codigoIata;
            this.nombreAeropuerto = nombreAeropuerto;
            this.ciudad = ciudad;
        }

        public String getCodigoIata() {
            return codigoIata;
        }

        public String getNombreAeropuerto() {
            return nombreAeropuerto;
        }

        public String getCiudad() {
            return ciudad;
        }
    }
}
