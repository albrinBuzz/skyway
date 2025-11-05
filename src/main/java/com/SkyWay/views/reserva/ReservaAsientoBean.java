package com.SkyWay.views.reserva;


import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.dto.InfoVueloDTO;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named("seleccionAsientosBean")
@ViewScoped
public class ReservaAsientoBean implements Serializable {

    private List<InfoAsientoDTO>asientos;
    @Autowired
    private AsientoService asientoService;
    @Autowired
    private VueloService vueloService;
    @Autowired
    private ItinerarioService itinerarioService;
    private InfoVueloDTO vueloSeleccionado;
    private int idxVuelo;
    private int cantVuelos;
    List<Integer>idVuelos;
    private Vuelo vuelo;
    private HashMap<Integer,List<InfoAsientoDTO>>asientosSeleccionados;
    private HashMap<Integer,List<Integer>> itinerariosAsientos=new HashMap<>();

    private List<InfoAsientoDTO> asientosSeleccionadosList = new ArrayList<>();
    private Integer cantAdultos;
    List<Itinerario>itinerarios=new ArrayList<>();
    List<Integer> idsItinerarios;
    private HashMap<Integer,Integer>tarifasItinerarios;

    private Integer idxAsientoSeleccion;
    @PostConstruct
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> params = externalContext.getRequestParameterMap();

        String adultosStr = params.getOrDefault("adultos", "1");
        cantAdultos= Integer.valueOf(adultosStr);
        tarifasItinerarios=new HashMap<>();


        String idsParam = externalContext.getRequestParameterMap().get("itinerarios");

        String idsTarifas = externalContext.getRequestParameterMap().get("tarifas");
        Logger.logInfo(idsTarifas);

        if (idsParam != null && !idsParam.isEmpty() && idsTarifas != null && !idsTarifas.isEmpty()) {
            var idsIte = idsParam.split(",");
            var idsTar=idsTarifas.split(",");

            if (idsIte.length != idsTar.length) {
                Logger.logInfo("⚠️ La cantidad de itinerarios y tarifas no coincide.");
                return;
            }

            Logger.logInfo("esto puede generar error en la association de las tarifas y los itinerarios");
            for (int i = 0; i < idsIte.length; i++) {
                try {
                    int idItinerario = Integer.parseInt(idsIte[i].trim());
                    int idTarifa = Integer.parseInt(idsTar[i].trim());
                    tarifasItinerarios.put(idItinerario, idTarifa);
                } catch (NumberFormatException e) {
                    System.err.println("❌ Error al convertir a entero: " + idsIte[i] + " o " + idsTar[i]);
                }
            }

            idsItinerarios = Arrays.stream(idsParam.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .toList();




            for (Integer id : idsItinerarios) {
                //Logger.logInfo(String.valueOf(id));
                itinerarios.add(itinerarioService.findById(id));
            }

            idVuelos=new ArrayList<>();
            asientosSeleccionados=new HashMap<>();

            for (Itinerario itinerario : itinerarios) {
                var itinerariosVuelos=  itinerario.getItinerarioVuelos();


                for (ItinerarioVuelo itinerarioVuelo : itinerario.getItinerarioVuelos()) {


                    idVuelos.add(itinerarioVuelo.getVuelo().getIdVuelo());

                }

            }







            this.cantVuelos=idVuelos.size();
            this.idxVuelo=0;
            idxAsientoSeleccion=0;
            //this.asientos = generarAsientosMock();
            this.vuelo = vueloService.findById(idVuelos.get(idxVuelo)).get();
            Logger.logInfo(vuelo.toString());

            this.asientos = asientoService.getAsientosDisponibles(vuelo.getIdVuelo());
            //Logger.logInfo(asientos.toString());

            idxVuelo++;
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






    public void setearAsiento(InfoAsientoDTO asiento) {

        if ("Ocupado".equalsIgnoreCase(asiento.getEstado())) {
            return; // No permitir seleccionar
        }

        // Si ya está seleccionado, lo quitamos
        if (asientosSeleccionadosList.contains(asiento)) {
            asientosSeleccionadosList.remove(asiento);
            asiento.setEstado("Disponible");
        } else {
            asientosSeleccionadosList.add(asiento);
            asiento.setEstado("Seleccionado");
        }


        // Guardar selección en el mapa (clave = id del vuelo, valor = asiento)
        //asientosSeleccionados.put(vuelo.getIdVuelo(), );
        if (asientosSeleccionados.containsKey(vuelo.getIdVuelo())){
            asientosSeleccionados.get(vuelo.getIdVuelo()).add(asiento);
        }else{
            List<InfoAsientoDTO>list=new ArrayList<>();
            list.add(asiento);
            asientosSeleccionados.put(vuelo.getIdVuelo(),list);
        }

        //asientosSeleccionados.computeIfAbsent(vuelo.getIdVuelo(), k -> new ArrayList<>()).add(asiento);

        idxAsientoSeleccion++;



        if (idxVuelo >= cantVuelos) {
            // Redireccionar a la página de reserva
            try {

                FacesContext.getCurrentInstance().getExternalContext()
                        .getSessionMap().put("asientosSeleccionados", asientosSeleccionados);

                FacesContext.getCurrentInstance().getExternalContext()
                        .getSessionMap().put("itinerarios", idsItinerarios);

                FacesContext.getCurrentInstance().getExternalContext()
                        .getSessionMap().put("tarifasItinerios", tarifasItinerarios);

                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect("/home/reserva.xhtml");

            } catch (IOException e) {
                Logger.logInfo("Redirección fallida a reserva.xhtml" +e.getMessage());
            }
            return;
        }

        if (idxAsientoSeleccion>=cantAdultos){
            // Si aún hay vuelos, cargar el siguiente
            int vueloId = idVuelos.get(idxVuelo);
            Logger.logInfo("Siguiente vuelo -> " + vueloId);
            this.asientos = asientoService.getAsientosDisponibles(vueloId);
            this.vuelo = vueloService.findById(vueloId).orElse(null);
            Logger.logInfo("Siguiente vuelo -> " + vuelo.toString());
            idxVuelo++;
            idxAsientoSeleccion=0;
        }

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

    public Vuelo getVuelo() {
        return vuelo;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public HashMap<Integer, List<InfoAsientoDTO>> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }

    public List<InfoAsientoDTO> getAsientosSeleccionadosList() {
        return asientosSeleccionadosList;
    }
}
