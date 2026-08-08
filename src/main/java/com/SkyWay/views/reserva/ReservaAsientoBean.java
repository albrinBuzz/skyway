package com.SkyWay.views.reserva;


import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;


import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Component
@Named("seleccionAsientosBean")
@ViewScoped
public class ReservaAsientoBean implements Serializable {

    @Autowired
    private AsientoCacheService asientoCacheService;

    @Autowired
    private ItinerarioService itinerarioService;

    private List<InfoAsientoDTO> asientos;
    private int idxVuelo;
    private int cantVuelos;
    private List<Vuelo> vuelos;
    private Vuelo vuelo;

    // Guardar selección por cada ID de Vuelo
    private HashMap<Integer, List<InfoAsientoDTO>> asientosSeleccionados;
    private List<InfoAsientoDTO> asientosSeleccionadosList = new ArrayList<>();

    private Integer cantAdultos;
    private List<Itinerario> itinerarios = new ArrayList<>();
    private List<Integer> idsItinerarios;
    private HashMap<Integer, Integer> tarifasItinerarios;

    private Integer idxAsientoSeleccion;
    private List<Pasajero> pasajeros;
    private String miSessionId;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, String> params = externalContext.getRequestParameterMap();
        //Map<String, Object> sessionMap = externalContext.getSessionMap();

        String adultosStr = params.getOrDefault("adultos", "1");
        cantAdultos = Integer.valueOf(adultosStr);
        tarifasItinerarios = new HashMap<>();

        Map<String, Object> sessionMap = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap();
        if (sessionMap.containsKey("reservaSessionId")) {
            this.miSessionId = (String) sessionMap.get("reservaSessionId");
        } else {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            //this.miSessionId = UUID.randomUUID().toString();
            this.miSessionId=session.getId();
            sessionMap.put("reservaSessionId", this.miSessionId);
        }

        String idsParam = params.get("itinerarios");
        String idsTarifas = params.get("tarifas");

        if (idsParam != null && !idsParam.isEmpty() && idsTarifas != null && !idsTarifas.isEmpty()) {
            var idsIte = idsParam.split(",");
            var idsTar = idsTarifas.split(",");

            for (int i = 0; i < idsIte.length; i++) {
                try {
                    tarifasItinerarios.put(Integer.parseInt(idsIte[i].trim()), Integer.parseInt(idsTar[i].trim()));
                } catch (NumberFormatException e) {
                    System.err.println("❌ Error de formato en itinerarios/tarifas");
                }
            }

            idsItinerarios = Arrays.stream(idsParam.split(",")).map(String::trim).map(Integer::parseInt).toList();

            for (Integer id : idsItinerarios) {
                itinerarios.add(itinerarioService.findById(id));
            }

            vuelos = new ArrayList<>();

            // Recuperar estado de la sesión en caso de refresco (F5)
            if (sessionMap.containsKey("asientosSeleccionados")) {
                asientosSeleccionados = (HashMap<Integer, List<InfoAsientoDTO>>) sessionMap.get("asientosSeleccionados");
            } else {
                asientosSeleccionados = new HashMap<>();
            }

            for (Itinerario itinerario : itinerarios) {
                for (ItinerarioVuelo itinerarioVuelo : itinerario.getItinerarioVuelos()) {
                    vuelos.add(itinerarioVuelo.getVuelo());
                }
            }

            this.cantVuelos = vuelos.size();
            this.idxVuelo = 0;
            this.idxAsientoSeleccion = 0;
            this.vuelo = vuelos.get(idxVuelo);

            pasajeros = new ArrayList<>(cantAdultos);
            for (int i = 0; i < cantAdultos; i++) {
                pasajeros.add(new Pasajero("Pasajero " + (i + 1)));
            }

            // Cargar asientos e hidratar selección previa si existe
            cargarDatosVueloActual();

        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se recibieron itinerarios."));
        }
    }

    private void cargarDatosVueloActual() {
        this.asientos = asientoCacheService.getAsientosVuelo(vuelo.getIdVuelo());
        this.asientosSeleccionadosList = asientosSeleccionados.getOrDefault(vuelo.getIdVuelo(), new ArrayList<>());
    }

    public void setearAsiento(InfoAsientoDTO asiento) {


        if (asiento == null) {
            //Logger.logWarn("[setearAsiento] Se intentó procesar un asiento nulo.");
            return;
        }

        if ("OCUPADO".equalsIgnoreCase(asiento.getEstado())) {

            //return;
        }



        var resultado = asientoCacheService.seleccionarOliberarAsiento(vuelo.getIdVuelo(), asiento, miSessionId);


        switch (resultado) {
            case FALLO_ASIENTO_NO_DISPONIBLE -> {

                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "El asiento ya no está disponible.");
                recargarAsientos();
                return;
            }
            case FALLO_BLOQUEADO_POR_OTRO -> {

                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Otro pasajero está reservando este asiento ahora mismo. Prueba otro.");
                recargarAsientos();
                return;
            }
            default -> {

            }
        }

        boolean seLibero = (resultado == AsientoCacheService.ResultadoSeleccion.EXITO_LIBERADO);

        if (seLibero) {


            asientosSeleccionadosList.removeIf(a -> a.getIdAsiento() == asiento.getIdAsiento());

            if (idxAsientoSeleccion > 0) {
                int idxPasajeroAnterior = idxAsientoSeleccion - 1;
                boolean removido = pasajeros.get(idxPasajeroAnterior).asientos
                        .removeIf(a -> a.getIdAsiento() == asiento.getIdAsiento());

            }
        } else {


            asientosSeleccionadosList.add(asiento);

            AsientoSeleccionado asientoSeleccionado = new AsientoSeleccionado(
                    asiento.getIdAsiento(), asiento.getNumeroAsiento(),
                    asiento.getClase(), vuelo.getNumeroVuelo(), asiento.getPrecio()
            );

            if (idxAsientoSeleccion < cantAdultos) {
                pasajeros.get(idxAsientoSeleccion).asientos.add(asientoSeleccionado);
                //Logger.logInfo(String.format(

                idxAsientoSeleccion++;

            } else {

            }
        }

        // Actualización de mapa y sesión JSF
        asientosSeleccionados.put(vuelo.getIdVuelo(), new ArrayList<>(asientosSeleccionadosList));
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("asientosSeleccionados", asientosSeleccionados);


        // Evaluación de avance/transición
        if (!seLibero && idxAsientoSeleccion >= cantAdultos) {


            if (idxVuelo + 1 >= cantVuelos) {
                //Logger.logInfo("[setearAsiento] Último vuelo alcanzado. Invocando finalizarReserva().");
                finalizarReserva();
            } else {

                siguienteVuelo();
            }
        }
    }

    public int getSubtotalGeneral() {
        int total = 0;
        for (List<InfoAsientoDTO> lista : asientosSeleccionados.values()) {
            for (InfoAsientoDTO a : lista) total += a.getPrecio();
        }
        return total;
    }

    /** IVA Chile 19%, redondeado. Ajusta la tasa si tu operación es internacional/exenta. */
    public int getImpuestos() {
        return (int) Math.round(getSubtotalGeneral() * 0.19);
    }

    public int getTotalGeneral() {
        return getSubtotalGeneral() + getImpuestos();
    }

    /** Desglose por tramo/vuelo para mostrar en el checkout. */
    public List<ResumenTramo> getResumenPorTramo() {
        List<ResumenTramo> resumen = new ArrayList<>();
        for (Vuelo v : vuelos) {
            List<InfoAsientoDTO> deLTramo = asientosSeleccionados.getOrDefault(v.getIdVuelo(), Collections.emptyList());
            int subtotal = deLTramo.stream().mapToInt(InfoAsientoDTO::getPrecio).sum();
            resumen.add(new ResumenTramo(v.getNumeroVuelo(), deLTramo.size(), subtotal));
        }
        return resumen;
    }

    /** Cuántos asientos totales lleva seleccionados sobre el total requerido (todos los tramos). */
    public int getAsientosCompletados() {
        return asientosSeleccionados.values().stream().mapToInt(List::size).sum();
    }

    public int getAsientosRequeridos() {
        return cantAdultos * cantVuelos;
    }

    public String getMiSessionId() { return miSessionId; }

    public static class ResumenTramo implements Serializable {
        private String numeroVuelo;
        private int cantidadAsientos;
        private int subtotal;
        public ResumenTramo(String numeroVuelo, int cantidadAsientos, int subtotal) {
            this.numeroVuelo = numeroVuelo; this.cantidadAsientos = cantidadAsientos; this.subtotal = subtotal;
        }
        public String getNumeroVuelo() { return numeroVuelo; }
        public int getCantidadAsientos() { return cantidadAsientos; }
        public int getSubtotal() { return subtotal; }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    /**
     * Avanza al siguiente vuelo (Forward)
     */
    public void siguienteVuelo() {
        if (idxVuelo + 1 < cantVuelos) {
            idxVuelo++;
            idxAsientoSeleccion = 0;
            this.vuelo = vuelos.get(idxVuelo);
            cargarDatosVueloActual();

            addMessage(FacesMessage.SEVERITY_INFO, "Cambio de vuelo",
                    "Ahora seleccionas asientos para el Vuelo " + vuelo.getNumeroVuelo()
                            + " (" + getRutaVueloActual() + ") — Tramo " + (idxVuelo + 1) + " de " + cantVuelos);

            PrimeFaces.current().executeScript("cambiarCanalWebSocket('" + vuelo.getIdVuelo() + "'); flashCambioVuelo();");
            PrimeFaces.current().ajax().update("seatForm");
        } else {
            finalizarReserva();
        }
    }

    /**
     * Retrocede al vuelo anterior (Back)
     */
    public void vueloAnterior() {
        if (idxVuelo > 0) {
            idxVuelo--;
            idxAsientoSeleccion = 0;
            this.vuelo = vuelos.get(idxVuelo);
            cargarDatosVueloActual();

            addMessage(FacesMessage.SEVERITY_INFO, "Cambio de vuelo",
                    "Volviste al Vuelo " + vuelo.getNumeroVuelo()
                            + " (" + getRutaVueloActual() + ") — Tramo " + (idxVuelo + 1) + " de " + cantVuelos);

            PrimeFaces.current().executeScript("cambiarCanalWebSocket('" + vuelo.getIdVuelo() + "'); flashCambioVuelo();");
            PrimeFaces.current().ajax().update("seatForm");
        }
    }

    public String getRutaVueloActual() {
        if (vuelo == null || vuelo.getSegmentoVuelos() == null || vuelo.getSegmentoVuelos().isEmpty()) return "";
        var segmentos = vuelo.getSegmentoVuelos();
        String origen = segmentos.get(0).getAeropuertoOrigen().getCodigoIata();
        String destino = segmentos.get(segmentos.size() - 1).getAeropuertoDestino().getCodigoIata();
        return origen + " → " + destino;
    }

    public String getRutaDeVuelo(Vuelo v) {
        if (v == null || v.getSegmentoVuelos() == null || v.getSegmentoVuelos().isEmpty()) return "";
        var segmentos = v.getSegmentoVuelos();
        String origen = segmentos.get(0).getAeropuertoOrigen().getCodigoIata();
        String destino = segmentos.get(segmentos.size() - 1).getAeropuertoDestino().getCodigoIata();
        return origen + " → " + destino;
    }

    private void finalizarReserva() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.getSessionMap().put("asientosSeleccionados", asientosSeleccionados);
            ec.getSessionMap().put("itinerarios", idsItinerarios);
            ec.getSessionMap().put("pasajeros", pasajeros);
            ec.getSessionMap().put("tarifasItinerios", tarifasItinerarios);

            ec.redirect("/home/reserva.xhtml");
        } catch (IOException e) {
            //Logger.logInfo("Redirección fallida a reserva.xhtml: " + e.getMessage());
        }
    }

    public void recargarAsientos() {
        this.asientos = asientoCacheService.getAsientosVuelo(vuelo.getIdVuelo());
    }

    // --- Getters y Setters ---
    public List<InfoAsientoDTO> getAsientos() { return asientos; }
    public Vuelo getVuelo() { return vuelo; }
    public List<Vuelo> getVuelos() { return vuelos; }
    public int getIdxVuelo() { return idxVuelo; }
    public int getCantVuelos() { return cantVuelos; }
    public List<InfoAsientoDTO> getAsientosSeleccionadosList() { return asientosSeleccionadosList; }
    public List<Pasajero> getPasajeros() { return pasajeros; }
    public Integer getIdxAsientoSeleccion() { return idxAsientoSeleccion; }

    public Integer getCantAdultos() {
        return cantAdultos;
    }

    public static class Pasajero implements Serializable {
        String nombre;
        List<AsientoSeleccionado> asientos = new ArrayList<>();

        public Pasajero() {}
        public Pasajero(String nombre) { this.nombre = nombre; }
        public List<AsientoSeleccionado> getAsientos() { return asientos; }
        public String getNombre() { return nombre; }
    }

    public static class AsientoSeleccionado implements Serializable {
        int idAsiento;
        String numeroAsiento;
        String clase;
        String numeroVuelo;
        int precio;

        public AsientoSeleccionado() {}
        public AsientoSeleccionado(int idAsiento, String numeroAsiento, String clase, String numeroVuelo, int precio) {
            this.idAsiento = idAsiento;
            this.numeroAsiento = numeroAsiento;
            this.clase = clase;
            this.numeroVuelo = numeroVuelo;
            this.precio = precio;
        }

        public String getNumeroAsiento() { return numeroAsiento; }
        public String getClase() { return clase; }
        public int getPrecio() { return precio; }
        public String getNumeroVuelo() { return numeroVuelo; }
        public int getIdAsiento() { return idAsiento; }
    }
}