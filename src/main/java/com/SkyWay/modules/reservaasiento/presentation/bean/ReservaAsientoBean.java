package com.SkyWay.modules.reservaasiento.presentation.bean;


import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.dto.InfoVueloDTO;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Named("seleccionAsientosBean")
@ViewScoped
public class ReservaAsientoBean {

    private List<InfoAsientoDTO>asientos;
    private ArrayList<InfoAsientoDTO>asientosSeleccionados;
    @Autowired
    private AsientoService asientoService;

    private InfoVueloDTO vueloSeleccionado;

    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        String idsParam = externalContext.getRequestParameterMap().get("itinerarios");

        if (idsParam != null && !idsParam.isEmpty()) {
            List<Integer> ids = Arrays.stream(idsParam.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();

            for (Integer id : ids) {
                Logger.logInfo(String.valueOf(id));
            }
            this.asientos = generarAsientosMock();
            //asientosSeleccionados=new ArrayList<InfoAsientoDTO>();
            //asientos = asientoService.getAsientosDisponibles(vueloSeleccionado.getIdAvion(), vueloSeleccionado.getIdVuelo());

            // Ahora ya tienes los IDs como lista
            // Puedes cargarlos desde la base de datos o servicio
            //this.itinerariosSeleccionados = itinerarioService.obtenerDetallesPorIds(ids);
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se recibieron itinerarios."));
        }
    }


    /**
     * Genera una lista de asientos de prueba con clases y estados variados
     */
    private List<InfoAsientoDTO> generarAsientosMock() {
        List<InfoAsientoDTO> mockAsientos = new ArrayList<>();

        // Primera Clase (6 asientos)
        for (int i = 1; i <= 6; i++) {
            mockAsientos.add(new InfoAsientoDTO(
                    i,
                    "1A" + i,
                    i % 2 == 0 ? "ocupado" : "libre", // alternar estado
                    500,
                    "Primera Clase"
            ));
        }

        // Ejecutiva (8 asientos)
        for (int i = 7; i <= 14; i++) {
            mockAsientos.add(new InfoAsientoDTO(
                    i,
                    "2B" + (i - 6),
                    i % 3 == 0 ? "ocupado" : "libre",
                    300,
                    "Ejecutiva"
            ));
        }

        // Económica (20 asientos)
        for (int i = 15; i <= 34; i++) {
            mockAsientos.add(new InfoAsientoDTO(
                    i,
                    "3C" + (i - 14),
                    i % 4 == 0 ? "ocupado" : "libre",
                    150,
                    "Económica"
            ));
        }

        return mockAsientos;
    }




    public List<List<List<InfoAsientoDTO>>> getFilasDistribuidas(String clase) {
        List<InfoAsientoDTO> filtrados = asientos.stream()
                .filter(a -> a.getClase().equalsIgnoreCase(clase))
                .collect(Collectors.toList());

        // Agrupar por "fila" (simulada a partir del índice del asiento)
        // Supongamos que cada fila tiene 4 asientos en Primera, 7 en Ejecutiva, 6 en Económica
        int bloquesPorFila = 0;
        int[] bloques = new int[0];

        switch (clase) {
            case "Primera Clase":
                bloquesPorFila = 4;
                bloques = new int[]{2, 2}; // 2-2
                break;
            case "Ejecutiva":
                bloquesPorFila = 7;
                bloques = new int[]{3, 2, 3}; // 2-3-2
                break;
            case "Económica":
                bloquesPorFila = 6;
                bloques = new int[]{3, 3}; // 3-3
                break;
        }

        List<List<List<InfoAsientoDTO>>> resultado = new ArrayList<>();

        for (int i = 0; i < filtrados.size(); i += bloquesPorFila) {
            List<InfoAsientoDTO> fila = filtrados.subList(i, Math.min(i + bloquesPorFila, filtrados.size()));
            List<List<InfoAsientoDTO>> filaConBloques = new ArrayList<>();

            int index = 0;
            for (int b : bloques) {
                if (index + b <= fila.size()) {
                    filaConBloques.add(fila.subList(index, index + b));
                    index += b;
                }
            }

            resultado.add(filaConBloques);
        }

        return resultado;
    }


    public List<InfoAsientoDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<InfoAsientoDTO> asientos) {
        this.asientos = asientos;
    }
}
