package com.SkyWay.beans;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

@Named("asientoBean")
@SessionScoped
public class AsientoBean {

    // Variables para manejar los asientos
    private String asientoSeleccionado;
    private String asientoDestino;
    private boolean cambioConfirmado;

    // Lista de asientos ocupados (en este caso, pueden ser valores predeterminados)
    private final String[] asientosOcupados = {"1A", "2B", "3D", "4C"};

    // Getter y Setter
    public String getAsientoSeleccionado() {
        return asientoSeleccionado;
    }

    public void setAsientoSeleccionado(String asientoSeleccionado) {
        this.asientoSeleccionado = asientoSeleccionado;
    }

    public String getAsientoDestino() {
        return asientoDestino;
    }

    public void setAsientoDestino(String asientoDestino) {
        this.asientoDestino = asientoDestino;
    }

    public boolean isCambioConfirmado() {
        return cambioConfirmado;
    }

    public void setCambioConfirmado(boolean cambioConfirmado) {
        this.cambioConfirmado = cambioConfirmado;
    }

    // Métodos para manejar la selección de asiento
    public void seleccionarAsiento(String asiento) {
        // Si ya se seleccionó un asiento, asignamos al asiento destino
        if (asientoSeleccionado == null) {
            asientoSeleccionado = asiento;
        } else {
            asientoDestino = asiento;
        }
    }

    // Comprobar si un asiento está ocupado
    public boolean isAsientoOcupado(String asiento) {
        for (String ocupado : asientosOcupados) {
            if (ocupado.equals(asiento)) {
                return true;
            }
        }
        return false;
    }

    // Comprobar si un asiento ya fue seleccionado
    public boolean isAsientoSeleccionado(String asiento) {
        return asientoSeleccionado != null && asientoSeleccionado.equals(asiento);
    }

    // Método para confirmar el cambio de asiento
    public String confirmarCambio() {
        if (asientoSeleccionado != null && asientoDestino != null) {
            // Realizar el cambio de asiento
            cambioConfirmado = true;

            // Actualizamos la lista de ocupados (solo como ejemplo)
            for (int i = 0; i < asientosOcupados.length; i++) {
                if (asientosOcupados[i].equals(asientoSeleccionado)) {
                    asientosOcupados[i] = asientoDestino;
                    break;
                }
            }

            // Limpiar los asientos seleccionados
            asientoSeleccionado = null;
            asientoDestino = null;

            // Redirigir a la página de confirmación (en este caso, solo un texto de éxito)
            return "cambioConfirmado";
        }
        return null;
    }
}
