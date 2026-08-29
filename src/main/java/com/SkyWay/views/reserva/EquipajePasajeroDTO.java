package com.SkyWay.views.reserva;



import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EquipajePasajeroDTO implements Serializable {

    private int pasajeroIndex;
    private String nombrePasajero;
    private String rutPasajero;
    private List<ItemEquipaje> maletas = new ArrayList<>();

    public EquipajePasajeroDTO(int pasajeroIndex, String nombrePasajero) {
        this.pasajeroIndex = pasajeroIndex;
        this.nombrePasajero = nombrePasajero;
    }

    public static class ItemEquipaje implements Serializable {
        private String tipo;        // "CABINA", "BODEGA_15KG", "BODEGA_23KG"
        private String descripcion; // Ej: "Maleta en cabina (10 kg)"
        private int precio;
        private String dimensiones;
        private BigDecimal peso;

        private int idTipo;

        public ItemEquipaje(String tipo, String descripcion, int precio) {
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.precio = precio;
        }

        public String getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
        public int getPrecio() { return precio; }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public void setPrecio(int precio) {
            this.precio = precio;
        }

        public String getDimensiones() {
            return dimensiones;
        }

        public void setDimensiones(String dimensiones) {
            this.dimensiones = dimensiones;
        }

        public BigDecimal getPeso() {
            return peso;
        }

        public void setPeso(BigDecimal peso) {
            this.peso = peso;
        }

        public int getIdTipo() {
            return idTipo;
        }

        public void setIdTipo(int idTipo) {
            this.idTipo = idTipo;
        }
    }

    public int getPasajeroIndex() { return pasajeroIndex; }
    public String getNombrePasajero() { return nombrePasajero; }
    public List<ItemEquipaje> getMaletas() { return maletas; }

    public String getRutPasajero() {
        return rutPasajero;
    }

    public void setRutPasajero(String rutPasajero) {
        this.rutPasajero = rutPasajero;
    }
}