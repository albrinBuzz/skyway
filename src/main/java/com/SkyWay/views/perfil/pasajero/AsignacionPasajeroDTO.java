package com.SkyWay.views.perfil.pasajero;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import java.io.Serializable;

/**
 * DTO que vincula a cada pasajero de la reserva con su vuelo, asiento asignado actual,
 * la nueva opción seleccionada, y el cálculo detallado de tarifa por ascenso de clase
 * más el cargo administrativo de gestión (15%).
 */
public class AsignacionPasajeroDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idVuelo;
    private String numeroVuelo;
    private String tramoVuelo;

    private Integer idAsientoOriginal;
    private String numeroAsientoOriginal;
    private String claseOriginal;
    private int precioOriginal;

    private String rutPasajero;
    private String nombrePasajero;
    private boolean esTitular;
    private boolean editable;

    private InfoAsientoDTO nuevoAsiento;
    private int diferenciaTarifa;
    private int cargoGestion;
    private int totalAdicional;
    private int porcentajeCargo = 15;

    public AsignacionPasajeroDTO() {}

    public AsignacionPasajeroDTO(Integer idVuelo, String numeroVuelo, String tramoVuelo,
                                Integer idAsientoOriginal, String numeroAsientoOriginal, String claseOriginal,
                                int precioOriginal, String rutPasajero, String nombrePasajero,
                                boolean esTitular, boolean editable) {
        this.idVuelo = idVuelo;
        this.numeroVuelo = numeroVuelo;
        this.tramoVuelo = tramoVuelo;
        this.idAsientoOriginal = idAsientoOriginal;
        this.numeroAsientoOriginal = numeroAsientoOriginal;
        this.claseOriginal = claseOriginal;
        this.precioOriginal = precioOriginal;
        this.rutPasajero = rutPasajero;
        this.nombrePasajero = nombrePasajero;
        this.esTitular = esTitular;
        this.editable = editable;
    }

    public void asignarNuevoAsiento(InfoAsientoDTO nuevo) {
        asignarNuevoAsiento(nuevo, this.porcentajeCargo);
    }

    public void asignarNuevoAsiento(InfoAsientoDTO nuevo, int porcentaje) {
        if (nuevo == null || (idAsientoOriginal != null && nuevo.getIdAsiento() == idAsientoOriginal)) {
            deseleccionar();
            return;
        }
        this.porcentajeCargo = porcentaje;
        this.nuevoAsiento = nuevo;
        this.diferenciaTarifa = Math.max(0, nuevo.getPrecio() - this.precioOriginal);
        this.cargoGestion = (this.diferenciaTarifa > 0 && this.porcentajeCargo > 0)
                ? Math.round(this.diferenciaTarifa * (this.porcentajeCargo / 100.0f)) : 0;
        this.totalAdicional = this.diferenciaTarifa + this.cargoGestion;
    }

    public void deseleccionar() {
        this.nuevoAsiento = null;
        this.diferenciaTarifa = 0;
        this.cargoGestion = 0;
        this.totalAdicional = 0;
    }

    public boolean tieneCambio() {
        return nuevoAsiento != null && (idAsientoOriginal == null || nuevoAsiento.getIdAsiento() != idAsientoOriginal);
    }

    public boolean esUpgrade() {
        return tieneCambio() && diferenciaTarifa > 0;
    }

    public String getNumeroAsientoNuevo() {
        return (nuevoAsiento != null) ? nuevoAsiento.getNumeroAsiento() : "---";
    }

    public String getClaseNueva() {
        return (nuevoAsiento != null) ? nuevoAsiento.getClase() : claseOriginal;
    }

    public int getPrecioNuevo() {
        return (nuevoAsiento != null) ? nuevoAsiento.getPrecio() : precioOriginal;
    }

    // Getters y Setters
    public Integer getIdVuelo() { return idVuelo; }
    public void setIdVuelo(Integer idVuelo) { this.idVuelo = idVuelo; }

    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

    public String getTramoVuelo() { return tramoVuelo; }
    public void setTramoVuelo(String tramoVuelo) { this.tramoVuelo = tramoVuelo; }

    public Integer getIdAsientoOriginal() { return idAsientoOriginal; }
    public void setIdAsientoOriginal(Integer idAsientoOriginal) { this.idAsientoOriginal = idAsientoOriginal; }

    public String getNumeroAsientoOriginal() { return numeroAsientoOriginal; }
    public void setNumeroAsientoOriginal(String numeroAsientoOriginal) { this.numeroAsientoOriginal = numeroAsientoOriginal; }

    public String getClaseOriginal() { return claseOriginal; }
    public void setClaseOriginal(String claseOriginal) { this.claseOriginal = claseOriginal; }

    public int getPrecioOriginal() { return precioOriginal; }
    public void setPrecioOriginal(int precioOriginal) { this.precioOriginal = precioOriginal; }

    public String getRutPasajero() { return rutPasajero; }
    public void setRutPasajero(String rutPasajero) { this.rutPasajero = rutPasajero; }

    public String getNombrePasajero() { return nombrePasajero; }
    public void setNombrePasajero(String nombrePasajero) { this.nombrePasajero = nombrePasajero; }

    public boolean isEsTitular() { return esTitular; }
    public void setEsTitular(boolean esTitular) { this.esTitular = esTitular; }

    public boolean isEditable() { return editable; }
    public void setEditable(boolean editable) { this.editable = editable; }

    public InfoAsientoDTO getNuevoAsiento() { return nuevoAsiento; }
    public void setNuevoAsiento(InfoAsientoDTO nuevoAsiento) { this.nuevoAsiento = nuevoAsiento; }

    public int getDiferenciaTarifa() { return diferenciaTarifa; }
    public void setDiferenciaTarifa(int diferenciaTarifa) { this.diferenciaTarifa = diferenciaTarifa; }

    public int getCargoGestion() { return cargoGestion; }
    public void setCargoGestion(int cargoGestion) { this.cargoGestion = cargoGestion; }

    public int getTotalAdicional() { return totalAdicional; }
    public void setTotalAdicional(int totalAdicional) { this.totalAdicional = totalAdicional; }

    public int getPorcentajeCargo() { return porcentajeCargo; }
    public void setPorcentajeCargo(int porcentajeCargo) { this.porcentajeCargo = porcentajeCargo; }
}
