package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.util.Logger;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

public class SesionReservaUtil {

    /**
     * Elimina todos los objetos de la sesión relacionados con el flujo de compra/reserva.
     * Permite que el usuario inicie una nueva búsqueda limpia sin arrastrar datos previos.
     */
    public static void limpiarObjetosSesionReserva() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) {
            return;
        }

        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        // Lista de todas las claves utilizadas durante el flujo de búsqueda, selección y pago
        String[] clavesAEliminar = {
                "itinerarios",
                "tarifasItinerios",
                "asientosSeleccionados",
                "pasajeros",
                "equipajePorPasajero",
                "totalEquipaje",
                "SOLICITUD_RESERVA_PENDIENTE",
                "PAGO_PENDIENTE",
                "ultimaBusquedaAsientosQuery",
                "reservaSessionId" // Genera una nueva id para la siguiente reserva
        };

        for (String clave : clavesAEliminar) {
            sessionMap.remove(clave);
        }

        Logger.logInfo("🧹 [SESIÓN] Todos los objetos del flujo de reserva han sido eliminados con éxito de la sesión.");
    }
}