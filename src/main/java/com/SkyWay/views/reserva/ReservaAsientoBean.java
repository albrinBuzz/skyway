package com.SkyWay.views.reserva;

import com.SkyWay.modules.asiento.presentation.dto.BloqueAsientosDTO;
import com.SkyWay.modules.asiento.presentation.dto.FilaCabinaDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;

import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

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

    private List<FilaCabinaDTO> filasCabina = new ArrayList<>();


    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();

        // 🔒 Guard-Clause contra arranque prematuro de Spring
        if (fc == null || fc.getExternalContext() == null) {
            Logger.logInfo("[ReservaAsientoBean] Invocación de init() omitida: No hay contexto HTTP activo.");
            return;
        }

        ExternalContext externalContext = fc.getExternalContext();
        HttpSession httpSession = (HttpSession) externalContext.getSession(true);
        Map<String, String> params = externalContext.getRequestParameterMap();
        Map<String, Object> sessionMap = externalContext.getSessionMap();

        String rawSessionId = httpSession != null ? httpSession.getId() : "NULL";

        boolean isExpired = Boolean.parseBoolean(params.get("expired"));
        if (isExpired) {
            reiniciarEstadoReserva(sessionMap);

            // Obtener la URL de búsqueda limpia almacenada previamente
            String queryString = (String) sessionMap.get("ultimaBusquedaAsientosQuery");
            String targetUrl = "/home/seleccionAsientos.xhtml?" + (queryString != null ? queryString : "");

            try {
                // Redireccionar al usuario a la URL limpia sin 'expired=true'
                externalContext.redirect(targetUrl);
                return; // Detener ejecución del init()
            } catch (IOException e) {
                Logger.logError("Error al redirigir para limpiar URL expirada: " + e.getMessage());
            }
        }

        String adultosStr = params.getOrDefault("adultos", "1");
        cantAdultos = Integer.valueOf(adultosStr);
        tarifasItinerarios = new HashMap<>();

        // RASTREO DE SESIÓN
        if (sessionMap.containsKey("reservaSessionId")) {
            this.miSessionId = (String) sessionMap.get("reservaSessionId");
        } else {
            // Token opaco por reserva; nunca el JSESSIONID real.
            this.miSessionId = java.util.UUID.randomUUID().toString();
            sessionMap.put("reservaSessionId", this.miSessionId);
        }

        String idsParam = params.get("itinerarios");
        String idsTarifas = params.get("tarifas");

        // -------------------------------------------------------------------------
        // 🚦 VALIDACIÓN DE PARÁMETROS DE ENTRADA Y REDIRECCIÓN A LA RAIZ (ROOT)
        // -------------------------------------------------------------------------
        if (idsParam != null && !idsParam.trim().isEmpty() && idsTarifas != null && !idsTarifas.trim().isEmpty()) {

            String queryString = (String) sessionMap.get("ultimaBusquedaAsientosQuery");
            if (queryString != null && !queryString.isBlank()) {
                sessionMap.put("ultimaBusquedaAsientosQuery", queryString);
            } else {
                sessionMap.put("ultimaBusquedaAsientosQuery",
                        "tarifas=" + idsTarifas + "&adultos=" + cantAdultos + "&itinerarios=" + idsParam);
            }

            var idsIte = idsParam.split(",");
            var idsTar = idsTarifas.split(",");

            for (int i = 0; i < idsIte.length; i++) {
                try {
                    tarifasItinerarios.put(Integer.parseInt(idsIte[i].trim()), Integer.parseInt(idsTar[i].trim()));
                } catch (NumberFormatException e) {
                    Logger.logError("❌ Error de formato en itinerarios/tarifas: " + e.getMessage());
                }
            }

            idsItinerarios = Arrays.stream(idsParam.split(",")).map(String::trim).map(Integer::parseInt).toList();

            for (Integer id : idsItinerarios) {
                Itinerario it = itinerarioService.findById(id);
                if (it != null) {
                    itinerarios.add(it);
                }
            }

            // Si los itinerarios consultados no existen en BD, redirigir al Root
            if (itinerarios.isEmpty()) {
                Logger.logWarn("[ReservaAsientoBean] Los itinerarios indicados no existen en BD. Redirigiendo al Root (/)...");
                redirigirAlRoot(externalContext);
                return;
            }

            vuelos = new ArrayList<>();
            for (Itinerario itinerario : itinerarios) {
                for (ItinerarioVuelo itinerarioVuelo : itinerario.getItinerarioVuelos()) {
                    vuelos.add(itinerarioVuelo.getVuelo());
                }
            }

            this.cantVuelos = vuelos.size();

            if (this.cantVuelos == 0) {
                Logger.logWarn("[ReservaAsientoBean] No se encontraron vuelos asociados a los itinerarios. Redirigiendo al Root (/)...");
                redirigirAlRoot(externalContext);
                return;
            }

            // =========================================================================
            // 🔄 RESTAURACIÓN DE ESTADO Y PASAJEROS (F5 CLEAN RECOVERY)
            // =========================================================================
            if (sessionMap.containsKey("asientosSeleccionados")) {
                asientosSeleccionados = (HashMap<Integer, List<InfoAsientoDTO>>) sessionMap.get("asientosSeleccionados");
            } else {
                asientosSeleccionados = new HashMap<>();
            }

            // 1. Crear la lista base de Pasajeros
            pasajeros = new ArrayList<>(cantAdultos);
            for (int i = 0; i < cantAdultos; i++) {
                Pasajero p = new Pasajero("Pasajero " + (i + 1));
                // Inicializar la lista con elementos nulos del tamaño de cantVuelos
                for (int vIdx = 0; vIdx < cantVuelos; vIdx++) {
                    p.asientos.add(null);
                }
                pasajeros.add(p);
            }

            // 2. Determinar en qué tramo se quedó el usuario antes del F5
            int vueloIncompletoIndex = 0;
            for (int vIdx = 0; vIdx < vuelos.size(); vIdx++) {
                Vuelo v = vuelos.get(vIdx);
                List<InfoAsientoDTO> seleccionadosEnVuelo = asientosSeleccionados.getOrDefault(v.getIdVuelo(), Collections.emptyList());

                for (int pIdx = 0; pIdx < seleccionadosEnVuelo.size() && pIdx < cantAdultos; pIdx++) {
                    InfoAsientoDTO a = seleccionadosEnVuelo.get(pIdx);
                    pasajeros.get(pIdx).asientos.set(vIdx, new AsientoSeleccionado(
                            a.getIdAsiento(), a.getNumeroAsiento(), a.getClase(), v.getNumeroVuelo(), a.getPrecio()
                    ));
                }
            }

            this.idxVuelo = vueloIncompletoIndex;
            this.vuelo = vuelos.get(idxVuelo);

            // 3. Hidratar el Panel Izquierdo con UN solo bloque sin duplicados
            for (Vuelo v : vuelos) {
                List<InfoAsientoDTO> seleccionadosEnVuelo = asientosSeleccionados.getOrDefault(v.getIdVuelo(), Collections.emptyList());
                for (int pIdx = 0; pIdx < seleccionadosEnVuelo.size() && pIdx < cantAdultos; pIdx++) {
                    InfoAsientoDTO a = seleccionadosEnVuelo.get(pIdx);

                    // Evitar duplicar badges si ya existe para este vuelo y pasajero
                    boolean yaExiste = pasajeros.get(pIdx).asientos.stream()
                            .anyMatch(as -> as.getNumeroVuelo().equalsIgnoreCase(v.getNumeroVuelo()) && as.getIdAsiento() == a.getIdAsiento());

                    if (!yaExiste) {
                        pasajeros.get(pIdx).asientos.add(new AsientoSeleccionado(
                                a.getIdAsiento(), a.getNumeroAsiento(), a.getClase(), v.getNumeroVuelo(), a.getPrecio()
                        ));
                    }
                }
            }

            // 4. Cargar la matriz de asientos del tramo actual
            cargarDatosVueloActual();

            // 5. Ajustar el índice de selección para el tramo actual
            List<InfoAsientoDTO> elegidosTramoActual = asientosSeleccionados.getOrDefault(vuelo.getIdVuelo(), Collections.emptyList());
            this.idxAsientoSeleccion = Math.min(elegidosTramoActual.size(), cantAdultos);

            Logger.logInfo(String.format("[SessionTrack] F5 Restaurado - Tramo: %d/%d (%s), Asientos en tramo actual: %d/%d, Próximo pasajero idx: %d",
                    (idxVuelo + 1), cantVuelos, getRutaVueloActual(), elegidosTramoActual.size(), cantAdultos, idxAsientoSeleccion));

        } else {
            // 🏠 REDIRECCIÓN AL ROOT SI NO VIENEN LOS PARÁMETROS ITINERARIOS / TARIFAS
            Logger.logWarn("[ReservaAsientoBean] Acceso directo sin parámetros 'itinerarios' ni 'tarifas'. Redirigiendo al Root (/)...");
            redirigirAlRoot(externalContext);
        }
    }

    /**
     * Método auxiliar para redirigir limpia y directamente a la página principal / Home ("/")
     */
    private void redirigirAlRoot(ExternalContext externalContext) {
        try {
            externalContext.redirect("/");
        } catch (IOException e) {
            Logger.logError("[ReservaAsientoBean] Error al intentar redirigir al Root: " + e.getMessage());
        }
    }



    private void cargarDatosVueloActual() {
        if (this.vuelo == null) {
            Logger.logWarn("[cargarDatosVueloActual] El objeto vuelo es NULL. Se cancela la carga de asientos.");
            return;
        }


        this.asientos = asientoCacheService.getAsientosVuelo(vuelo.getIdVuelo());
        this.asientosSeleccionadosList = asientosSeleccionados.getOrDefault(vuelo.getIdVuelo(), new ArrayList<>());

        // Construir estructura dinámica de cabina
        construirFilasCabina();

    }

    private void construirFilasCabina() {
        this.filasCabina = new ArrayList<>();

        if (this.asientos == null || this.asientos.isEmpty()) {
            Logger.logWarn("[construirFilasCabina] La lista de asientos está vacía o es nula.");
            return;
        }

        Map<Integer, List<InfoAsientoDTO>> mapaFilas = new TreeMap<>();
        for (InfoAsientoDTO a : this.asientos) {
            int numFila = (a.getFila() != null && a.getFila() > 0) ? a.getFila() : extraerFilaDeNumero(a.getNumeroAsiento());
            if (a.getLetra() == null || a.getLetra().trim().isEmpty()) {
                a.setLetra(extraerLetraDeNumero(a.getNumeroAsiento()));
            }
            mapaFilas.computeIfAbsent(numFila, k -> new ArrayList<>()).add(a);
        }

        String ultimaClaseProcesada = "";

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : mapaFilas.entrySet()) {
            int numFila = entry.getKey();
            List<InfoAsientoDTO> asientosFila = entry.getValue();

            asientosFila.sort(Comparator.comparing(InfoAsientoDTO::getLetra));

            boolean esEmergencia = asientosFila.stream().anyMatch(a -> Boolean.TRUE.equals(a.getEsEmergencia()));
            String claseFila = asientosFila.get(0).getClase();

            FilaCabinaDTO filaDTO = new FilaCabinaDTO(numFila, claseFila, esEmergencia);

            // Marca el divisor solo si cambió la clase de asiento respecto a la fila anterior
            if (!claseFila.equalsIgnoreCase(ultimaClaseProcesada)) {
                filaDTO.setEsPrimerFilaDeClase(true);
                ultimaClaseProcesada = claseFila;
            }

            boolean usaPasilloBD = asientosFila.stream().anyMatch(a -> Boolean.TRUE.equals(a.getEsPasillo()));
            BloqueAsientosDTO bloqueActual = new BloqueAsientosDTO();
            int totalAsientosFila = asientosFila.size();

            for (int i = 0; i < totalAsientosFila; i++) {
                InfoAsientoDTO asiento = asientosFila.get(i);
                bloqueActual.getAsientos().add(asiento);

                boolean romperBloque = false;

                if (usaPasilloBD) {
                    // Respetar bandera de BD si existe
                    romperBloque = Boolean.TRUE.equals(asiento.getEsPasillo()) && i < totalAsientosFila - 1;
                } else {
                    // FALLBACK AUTOMÁTICO DE PASILLOS POR TAMAÑO DE FILA
                    if (totalAsientosFila == 10) {
                        // Configuración 3-4-3 (ej. Boeing 747) -> Corta tras el 3er y 7mo asiento (índices 2 y 6)
                        romperBloque = (i == 2 || i == 6) && i < totalAsientosFila - 1;
                    } else if (totalAsientosFila == 9) {
                        // 👈 ¡AGREGAR ESTE BLOQUE! Configuración 3-3-3 (Airbus A350 / B787)
                        // Corta tras el 3er y 6to asiento (índices 2 y 5) -> [A,B,C] PASILLO [D,E,F] PASILLO [J,K,L]
                        romperBloque = (i == 2 || i == 5) && i < totalAsientosFila - 1;
                    } else if (totalAsientosFila == 7 || totalAsientosFila == 8) {
                        // Configuración 2-3-2 o 2-4-2 (ej. A330) -> Corta tras el 2do y 5to/6to
                        romperBloque = (i == 1 || i == 4) && i < totalAsientosFila - 1;
                    } else if (totalAsientosFila == 6) {
                        // Configuración 3-3 (ej. A320) -> Corta tras el 3er asiento (índice 2)
                        romperBloque = (i == 2) && i < totalAsientosFila - 1;
                    } else if (totalAsientosFila == 4) {
                        // Configuración 2-2 -> Corta tras el 2do asiento (índice 1)
                        romperBloque = (i == 1) && i < totalAsientosFila - 1;
                    } else if (totalAsientosFila == 3) {
                        // Configuración 1-2 -> Corta tras el 1er asiento (índice 0)
                        romperBloque = (i == 0) && i < totalAsientosFila - 1;
                    }
                }

                if (romperBloque) {
                    filaDTO.getBloques().add(bloqueActual);
                    bloqueActual = new BloqueAsientosDTO();
                }
            }

            if (!bloqueActual.getAsientos().isEmpty()) {
                filaDTO.getBloques().add(bloqueActual);
            }

            this.filasCabina.add(filaDTO);
        }
    }
    // Método auxiliar para extraer la letra ("14A" -> "A")
    private String extraerLetraDeNumero(String numeroAsiento) {
        if (numeroAsiento == null) return "";
        String letra = numeroAsiento.replaceAll("[0-9]", "").trim();
        return letra.isEmpty() ? numeroAsiento : letra;
    }

    private int extraerFilaDeNumero(String numeroAsiento) {
        if (numeroAsiento == null) {
            Logger.logWarn("[extraerFilaDeNumero] Numero de asiento es NULL, asignando fila 1 por defecto.");
            return 1;
        }
        String digits = numeroAsiento.replaceAll("[^0-9]", "");
        int filaCalculada = digits.isEmpty() ? 1 : Integer.parseInt(digits);



        return filaCalculada;
    }




    // =========================================================================
// MÉTODOS DE ACCIÓN RESTRUCTURADOS
// =========================================================================
    public void setearAsiento(InfoAsientoDTO asiento) {
        if (asiento == null) return;

        var resultado = asientoCacheService.seleccionarOliberarAsiento(vuelo.getIdVuelo(), asiento, miSessionId);

        switch (resultado) {
            case FALLO_ASIENTO_NO_DISPONIBLE -> {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "El asiento ya no está disponible.");
                recargarAsientos();
                return;
            }
            case FALLO_BLOQUEADO_POR_OTRO -> {
                addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Otro pasajero está reservando este asiento ahora mismo.");
                recargarAsientos();
                return;
            }
        }

        boolean seLibero = (resultado == AsientoCacheService.ResultadoSeleccion.EXITO_LIBERADO);

        if (seLibero) {
            asientosSeleccionadosList.removeIf(a -> a.getIdAsiento() == asiento.getIdAsiento());

            if (idxAsientoSeleccion > 0) {
                int idxPasajeroAnterior = idxAsientoSeleccion - 1;
                // Liberar el slot del tramo actual (idxVuelo)
                pasajeros.get(idxPasajeroAnterior).asientos.set(idxVuelo, null);
                idxAsientoSeleccion--;
            }
        } else {
            // Regla de sustitución cuando el tramo se llena
            if (idxAsientoSeleccion >= cantAdultos && !asientosSeleccionadosList.isEmpty()) {
                InfoAsientoDTO asientoAnterior = asientosSeleccionadosList.remove(asientosSeleccionadosList.size() - 1);
                asientoCacheService.seleccionarOliberarAsiento(vuelo.getIdVuelo(), asientoAnterior, miSessionId);

                if (idxAsientoSeleccion > 0) {
                    idxAsientoSeleccion--;
                }
                pasajeros.get(idxAsientoSeleccion).asientos.set(idxVuelo, null);
            }

            asientosSeleccionadosList.add(asiento);

            AsientoSeleccionado asientoSeleccionado = new AsientoSeleccionado(
                    asiento.getIdAsiento(), asiento.getNumeroAsiento(),
                    asiento.getClase(), vuelo.getNumeroVuelo(), asiento.getPrecio()
            );

            if (idxAsientoSeleccion < cantAdultos) {
                // Guardar en la posición fija del tramo activo (idxVuelo)
                pasajeros.get(idxAsientoSeleccion).asientos.set(idxVuelo, asientoSeleccionado);
                idxAsientoSeleccion++;
            }
        }

        // Actualizar el mapa global en sesión
        asientosSeleccionados.put(vuelo.getIdVuelo(), new ArrayList<>(asientosSeleccionadosList));
        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("asientosSeleccionados", asientosSeleccionados);

        // Auto-avance de tramo si se completaron los asientos requeridos
        if (!seLibero && idxAsientoSeleccion >= cantAdultos) {
            if (idxVuelo + 1 >= cantVuelos) {
                finalizarReserva();
            } else {
                siguienteVuelo();
            }
        }

        long segundosRestantes = getTiempoRestanteSegundos();
        PrimeFaces.current().executeScript("iniciarCronometroUI(" + segundosRestantes + ");");
    }

    public int getSubtotalGeneral() {
        int total = 0;
        for (List<InfoAsientoDTO> lista : asientosSeleccionados.values()) {
            for (InfoAsientoDTO a : lista) total += a.getPrecio();
        }
        return total;
    }

    public int getImpuestos() {
        return (int) Math.round(getSubtotalGeneral() * 0.19);
    }

    public int getTotalGeneral() {
        return getSubtotalGeneral() + getImpuestos();
    }

    public List<ResumenTramo> getResumenPorTramo() {
        List<ResumenTramo> resumen = new ArrayList<>();
        for (Vuelo v : vuelos) {
            List<InfoAsientoDTO> deLTramo = asientosSeleccionados.getOrDefault(v.getIdVuelo(), Collections.emptyList());
            int subtotal = deLTramo.stream().mapToInt(InfoAsientoDTO::getPrecio).sum();
            resumen.add(new ResumenTramo(v.getNumeroVuelo(), deLTramo.size(), subtotal));
        }
        return resumen;
    }

    public int getAsientosCompletados() {
        return asientosSeleccionados.values().stream().mapToInt(List::size).sum();
    }

    public int getAsientosRequeridos() {
        return cantAdultos * cantVuelos;
    }

    public String getMiSessionId() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            HttpSession session = (HttpSession) fc.getExternalContext().getSession(false);
            if (session != null) {
                return session.getId();
            }
        }
        return null;
    }

    // Redirección limpia para la Opción 1 de JSF (Evita el IllegalStateException)
    public String irASeleccionEquipaje() {
        for (Vuelo v : vuelos) {
            List<InfoAsientoDTO> elegidos = asientosSeleccionados.getOrDefault(v.getIdVuelo(), Collections.emptyList());
            if (elegidos.size() < cantAdultos) {
                addMessage(FacesMessage.SEVERITY_WARN, "Incompleto", "Debe seleccionar asientos para todos los pasajeros en todos los tramos.");
                return null;
            }
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
        sessionMap.put("asientosSeleccionados", this.asientosSeleccionados);
        sessionMap.put("pasajeros", this.pasajeros);
        sessionMap.put("itinerarios", this.idsItinerarios);
        sessionMap.put("tarifasItinerios", this.tarifasItinerarios);

        return "/home/equipaje.xhtml?faces-redirect=true";
    }


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
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

    public void siguienteVuelo() {
        if (idxVuelo + 1 < cantVuelos) {
            idxVuelo++;
            this.vuelo = vuelos.get(idxVuelo);
            cargarDatosVueloActual();

            // Sincronizar el puntero con la cantidad asignada en este tramo
            List<InfoAsientoDTO> guardadosEnEsteTramo = asientosSeleccionados.getOrDefault(vuelo.getIdVuelo(), Collections.emptyList());
            this.idxAsientoSeleccion = Math.min(guardadosEnEsteTramo.size(), cantAdultos);

            addMessage(FacesMessage.SEVERITY_INFO, "Cambio de vuelo",
                    "Ahora seleccionas asientos para el Vuelo " + vuelo.getNumeroVuelo()
                            + " (" + getRutaVueloActual() + ") — Tramo " + (idxVuelo + 1) + " de " + cantVuelos);

            long segundosRestantes = getTiempoRestanteSegundos();

            // Se ejecuta el JS de cambio de canal y cronómetro.
            // NOTA: Se elimina PrimeFaces.current().ajax().update("seatForm") de aquí.
            PrimeFaces.current().executeScript(
                    "cambiarCanalWebSocket('" + vuelo.getIdVuelo() + "'); " +
                            "flashCambioVuelo(); " +
                            "iniciarCronometroUI(" + segundosRestantes + ");"
            );
        } else {
            finalizarReserva();
        }
    }

    public void vueloAnterior() {
        if (idxVuelo > 0) {
            idxVuelo--;
            this.vuelo = vuelos.get(idxVuelo);
            cargarDatosVueloActual();

            List<InfoAsientoDTO> guardadosEnEsteTramo = asientosSeleccionados.getOrDefault(vuelo.getIdVuelo(), Collections.emptyList());
            this.idxAsientoSeleccion = Math.min(guardadosEnEsteTramo.size(), cantAdultos);

            addMessage(FacesMessage.SEVERITY_INFO, "Cambio de vuelo",
                    "Volviste al Vuelo " + vuelo.getNumeroVuelo()
                            + " (" + getRutaVueloActual() + ") — Tramo " + (idxVuelo + 1) + " de " + cantVuelos);

            long segundosRestantes = getTiempoRestanteSegundos();

            PrimeFaces.current().executeScript(
                    "cambiarCanalWebSocket('" + vuelo.getIdVuelo() + "'); " +
                            "flashCambioVuelo(); " +
                            "iniciarCronometroUI(" + segundosRestantes + ");"
            );
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

            // Persistir la selección de asientos e itinerarios en la sesión JSF
            ec.getSessionMap().put("asientosSeleccionados", asientosSeleccionados);
            ec.getSessionMap().put("itinerarios", idsItinerarios);
            ec.getSessionMap().put("pasajeros", pasajeros);
            ec.getSessionMap().put("tarifasItinerios", tarifasItinerarios);


            // Redirección hacia la pantalla de selección de equipaje
            ec.redirect("/home/equipaje.xhtml");
        } catch (IOException e) {
            Logger.logError("Redirección fallida a equipaje.xhtml: " + e.getMessage());
        }
    }

    public void sincronizarLiberacionExterna(Integer idAsientoLiberado) {
        if (idAsientoLiberado == null) return;

        List<InfoAsientoDTO> seleccionadosEnVuelo = asientosSeleccionados.get(vuelo.getIdVuelo());
        if (seleccionadosEnVuelo != null) {
            boolean seElimino = seleccionadosEnVuelo.removeIf(a -> a.getIdAsiento() == idAsientoLiberado);
            if (seElimino) {
                this.asientosSeleccionadosList = seleccionadosEnVuelo;
                if (idxAsientoSeleccion > 0) {
                    idxAsientoSeleccion--;
                }
                recargarAsientos();
                addMessage(FacesMessage.SEVERITY_WARN, "Tiempo expirado",
                        "Tu reserva temporal de uno o más asientos expiró por inactividad.");
                PrimeFaces.current().ajax().update("seatForm");
            }
        }
    }

    public long getTiempoRestanteSegundos() {
        long restanteMs = asientoCacheService.getTiempoRestanteMsParaSesion(miSessionId);
        return restanteMs > 0 ? (restanteMs / 1000) : 0;
    }

    public void forzarExpiracionPorTimeout() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        Map<String, Object> sessionMap = ec.getSessionMap();

        Logger.logWarn(String.format("[forzarExpiracionPorTimeout] Expiración forzada por inactividad para SessionId: %s", miSessionId));

        reiniciarEstadoReserva(sessionMap);

        String query = (String) sessionMap.get("ultimaBusquedaAsientosQuery");
        String targetUrl = "/home/seleccionAsientos.xhtml?" + (query != null ? query : "") + "&expired=true";

        try {
            ec.redirect(targetUrl);
        } catch (IOException e) {
            Logger.logError("Error al redirigir por timeout de sesión: " + e.getMessage());
        }
    }
    public void evaluarExpiracion() {
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        String expired = ec.getRequestParameterMap().get("expired");

        if ("true".equalsIgnoreCase(expired)) {
            // 1. Purgar estado de sesión
            ec.getSessionMap().remove("asientosSeleccionados");
            ec.getSessionMap().remove("pasajeros");

            if (this.asientosSeleccionados != null) this.asientosSeleccionados.clear();
            if (this.asientosSeleccionadosList != null) this.asientosSeleccionadosList.clear();

            // 2. Notificar al usuario
            addMessage(FacesMessage.SEVERITY_WARN, "Sesión de asientos restablecida",
                    "Selecciona nuevamente tus asientos para continuar.");

            // 3. 🪄 LIMPIAR EL PARÁMETRO DE LA URL EN EL NAVEGADOR
            PrimeFaces.current().executeScript(
                    "if (window.history.replaceState) { " +
                            "    const url = new URL(window.location.href); " +
                            "    url.searchParams.delete('expired'); " +
                            "    window.history.replaceState({}, document.title, url.toString()); " +
                            "}"
            );
        }
    }

    private void reiniciarEstadoReserva(Map<String, Object> sessionMap) {
        Logger.logInfo(String.format("[reiniciarEstadoReserva] Limpiando datos de sesión JSF para SessionId: %s", miSessionId));
        sessionMap.remove("asientosSeleccionados");
        sessionMap.remove("itinerarios");
        sessionMap.remove("pasajeros");
        sessionMap.remove("tarifasItinerios");

        if (asientosSeleccionados != null) {
            asientosSeleccionados.clear();
        } else {
            asientosSeleccionados = new HashMap<>();
        }

        if (asientosSeleccionadosList != null) {
            asientosSeleccionadosList.clear();
        }

        this.idxVuelo = 0;
        this.idxAsientoSeleccion = 0;
    }

    public void recargarAsientos() {
        this.asientos = asientoCacheService.getAsientosVuelo(vuelo.getIdVuelo());
    }


    // =========================================================
// MÉTODOS DE CONTEXTO DETALLADO DEL VUELO
// =========================================================

    public String getOrigenDetallado() {
        if (vuelo == null || vuelo.getSegmentoVuelos() == null || vuelo.getSegmentoVuelos().isEmpty()) return "";
        var origen = vuelo.getSegmentoVuelos().get(0).getAeropuertoOrigen();
        if (origen == null) return "";
        String ciudad = (origen.getCiudad() != null) ? origen.getCiudad().getNombre() : "";
        String pais = (origen.getCiudad() != null && origen.getCiudad().getPai() != null) ? origen.getCiudad().getPai().getNombre() : "";
        return String.format("%s (%s)", ciudad, pais);
    }

    public String getDestinoDetallado() {
        if (vuelo == null || vuelo.getSegmentoVuelos() == null || vuelo.getSegmentoVuelos().isEmpty()) return "";
        var segmentos = vuelo.getSegmentoVuelos();
        var destino = segmentos.get(segmentos.size() - 1).getAeropuertoDestino();
        if (destino == null) return "";
        String ciudad = (destino.getCiudad() != null) ? destino.getCiudad().getNombre() : "";
        String pais = (destino.getCiudad() != null && destino.getCiudad().getPai() != null) ? destino.getCiudad().getPai().getNombre() : "";
        return String.format("%s (%s)", ciudad, pais);
    }

    public String getModeloAvion() {
        if (vuelo == null || vuelo.getAvion() == null) return "Aeronave Comercial";
        return vuelo.getAvion().getModeloAvion() != null ? vuelo.getAvion().getModeloAvion().getNombre() : "Aeronave Comercial";
    }

    public String getDuracionEstimada() {
        if (vuelo == null || vuelo.getFechaHoraSalida() == null || vuelo.getFechaHoraLlegada() == null) return "N/A";
        long diffMs = vuelo.getFechaHoraLlegada().getTime() - vuelo.getFechaHoraSalida().getTime();
        if (diffMs <= 0) return "N/A";
        long horas = diffMs / (1000 * 60 * 60);
        long minutos = (diffMs / (1000 * 60)) % 60;
        return String.format("%dh %02dmin", horas, minutos);
    }

    public String getPuertasEmbarque() {
        if (vuelo == null || vuelo.getSegmentoVuelos() == null || vuelo.getSegmentoVuelos().isEmpty()) return "Por asignar";
        String puertas = vuelo.getSegmentoVuelos().get(0).getPuertasEmbarque();
        return (puertas != null && !puertas.trim().isEmpty()) ? puertas.trim() : "Por asignar";
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
    public Integer getCantAdultos() { return cantAdultos; }

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

    public List<FilaCabinaDTO> getFilasCabina() {
        return filasCabina;
    }
}