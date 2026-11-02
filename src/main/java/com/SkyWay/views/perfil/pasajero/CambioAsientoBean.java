package com.SkyWay.views.perfil.pasajero;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator.TarifaCaracteristicaValidator;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.BloqueAsientosDTO;
import com.SkyWay.modules.asiento.presentation.dto.FilaCabinaDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.*;

@Named("cambioBean")
@ViewScoped
public class CambioAsientoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired private VueloService vueloService;
    @Autowired private AsientoService asientoService;
    @Autowired private ReservaService reservaService;
    @Autowired private ReservaAsientoService reservaAsientoService;
    @Autowired private TarifaCaracteristicaValidator tarifaValidator;

    private String idVuelo;
    private String idReserva;
    private Integer idItinerario;
    private String origenRetorno = "CHECKIN";

    private boolean permiteCambio = true;
    private String mensajeReglaTarifa;
    private String nombreTarifa;

    private Vuelo vuelo;
    private InfoAsientoDTO asientoActual;

    private List<Vuelo> vuelosReserva = new ArrayList<>();
    private List<InfoAsientoDTO> asientos = new ArrayList<>();
    private List<InfoAsientoDTO> asientoSeleccionados = new ArrayList<>();
    private HashMap<Integer, InfoAsientoDTO> asientosCambio = new HashMap<>();

    // Estructura de Cabina para renderizado realista
    private List<FilaCabinaDTO> filasCabina = new ArrayList<>();

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

            // 1. Prioridad: Inyección segura por sesión (previene manipulación de URL)
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ID_RESERVA")) {
                Object r = sessionMap.get("CAMBIO_ASIENTO_ID_RESERVA");
                if (r != null) this.idReserva = r.toString();
            }
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ID_VUELO")) {
                Object v = sessionMap.get("CAMBIO_ASIENTO_ID_VUELO");
                if (v != null) this.idVuelo = v.toString();
            }
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ID_ITINERARIO")) {
                Object it = sessionMap.get("CAMBIO_ASIENTO_ID_ITINERARIO");
                if (it instanceof Integer) {
                    this.idItinerario = (Integer) it;
                } else if (it != null) {
                    try { this.idItinerario = Integer.parseInt(it.toString()); } catch (NumberFormatException ignored) {}
                }
            }
            if (sessionMap.containsKey("CAMBIO_ASIENTO_ORIGEN")) {
                Object orig = sessionMap.get("CAMBIO_ASIENTO_ORIGEN");
                if (orig != null) this.origenRetorno = orig.toString();
            }

            // 2. Si no viene idReserva por sesión, revisar parámetros por URL (fallback)
            if (this.idReserva == null || this.idReserva.isBlank()) {
                Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
                if (params.containsKey("idReserva")) this.idReserva = params.get("idReserva");
                if (params.containsKey("idVuelo")) this.idVuelo = params.get("idVuelo");
            }

            // 3. Si no hay idVuelo pero sí idReserva, resolver vuelo automáticamente
            if ((this.idVuelo == null || this.idVuelo.isBlank()) && this.idReserva != null && !this.idReserva.isBlank()) {
                resolverVueloDeReserva();
            }

            // 4. Validar reglas de la tarifa contratada
            validarTarifaYReglas();

            if (this.permiteCambio) {
                cargarDatosVueloYAsientos();
            }
        }
    }

    private void resolverVueloDeReserva() {
        try {
            int parsedIdReserva = Integer.parseInt(idReserva);
            Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
            if (optRes.isPresent()) {
                Reserva res = optRes.get();
                if (res.getReservaItinerarios() != null) {
                    for (ReservaItinerario ri : res.getReservaItinerarios()) {
                        if (ri.getItinerario() != null && ri.getItinerario().getItinerarioVuelos() != null) {
                            for (ItinerarioVuelo iv : ri.getItinerario().getItinerarioVuelos()) {
                                if (iv.getVuelo() != null) {
                                    this.idVuelo = String.valueOf(iv.getVuelo().getIdVuelo());
                                    if (this.idItinerario == null && ri.getItinerario() != null) {
                                        this.idItinerario = ri.getItinerario().getIdItinerario();
                                    }
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logError("Error al resolver vuelo de reserva: " + e.getMessage());
        }
    }

    public void validarTarifaYReglas() {
        if (idReserva == null || idReserva.isBlank()) {
            this.permiteCambio = false;
            this.mensajeReglaTarifa = "No se ha proporcionado una reserva para cambio de asiento.";
            return;
        }

        try {
            int parsedIdReserva = Integer.parseInt(idReserva);
            Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
            if (optRes.isEmpty()) {
                this.permiteCambio = false;
                this.mensajeReglaTarifa = "No se encontró información de la reserva #" + idReserva;
                return;
            }

            Reserva res = optRes.get();

            if (res.getEstadoReservaBean() != null && res.getEstadoReservaBean().getDescripcion() != null
                    && res.getEstadoReservaBean().getDescripcion().toLowerCase().contains("cancelad")) {
                this.permiteCambio = false;
                this.mensajeReglaTarifa = "La reserva se encuentra cancelada. No es posible cambiar asientos.";
                return;
            }

            if (res.getReservaItinerarios() != null && !res.getReservaItinerarios().isEmpty()) {
                for (ReservaItinerario ri : res.getReservaItinerarios()) {
                    if (ri.getItinerarioTarifa() != null && ri.getItinerarioTarifa().getTarifa() != null) {
                        Tarifa t = ri.getItinerarioTarifa().getTarifa();
                        this.nombreTarifa = t.getNombre();
                        List<TarifaCaracteristica> caracs = t.getTarifaCaracteristicas();

                        if (!tarifaValidator.permiteCambio(caracs)) {
                            this.permiteCambio = false;
                            this.mensajeReglaTarifa = "Tu tarifa contratada (" + this.nombreTarifa + ") no autoriza modificaciones de asiento.";
                            return;
                        }

                        // Validar anticipación requerida por la tarifa
                        Date fSalida = null;
                        if (ri.getItinerario() != null && ri.getItinerario().getHoraSalida() != null) {
                            fSalida = new Date(ri.getItinerario().getHoraSalida().getTime());
                        }

                        if (fSalida != null) {
                            long diffMillis = fSalida.getTime() - System.currentTimeMillis();
                            int horasRestantes = (int) (diffMillis / (3600 * 1000));
                            if (!tarifaValidator.cumpleHorasMinimasCambio(caracs, horasRestantes)) {
                                this.permiteCambio = false;
                                this.mensajeReglaTarifa = "El plazo límite estipulado por tu tarifa (" + this.nombreTarifa + ") para cambiar asientos ha finalizado.";
                                return;
                            }
                        }

                        this.permiteCambio = true;
                        return;
                    }
                }
            }

            this.permiteCambio = true;
        } catch (Exception e) {
            Logger.logError("Error al verificar tarifa en CambioAsientoBean: " + e.getMessage());
            this.permiteCambio = false;
            this.mensajeReglaTarifa = "Ocurrió un error al verificar las restricciones de tu tarifa: " + e.getMessage();
        }
    }

    public void cargarDatosVueloYAsientos() {
        if (idReserva != null && !idReserva.isEmpty()) {
            try {
                int parsedIdReserva = Integer.parseInt(idReserva);

                // Cargar lista de vuelos de la reserva si aún no se cargaron
                if (vuelosReserva.isEmpty()) {
                    Optional<Reserva> optRes = reservaService.findById(parsedIdReserva);
                    if (optRes.isPresent() && optRes.get().getReservaItinerarios() != null) {
                        Set<Integer> vuelosIds = new LinkedHashSet<>();
                        for (ReservaItinerario ri : optRes.get().getReservaItinerarios()) {
                            if (ri.getItinerario() != null && ri.getItinerario().getItinerarioVuelos() != null) {
                                for (ItinerarioVuelo iv : ri.getItinerario().getItinerarioVuelos()) {
                                    if (iv.getVuelo() != null && vuelosIds.add(iv.getVuelo().getIdVuelo())) {
                                        vuelosReserva.add(iv.getVuelo());
                                    }
                                }
                            }
                        }
                    }
                }

                if ((idVuelo == null || idVuelo.isEmpty()) && !vuelosReserva.isEmpty()) {
                    idVuelo = String.valueOf(vuelosReserva.get(0).getIdVuelo());
                }

                if (idVuelo != null && !idVuelo.isEmpty()) {
                    int parsedIdVuelo = Integer.parseInt(idVuelo);
                    vuelo = vueloService.findById(parsedIdVuelo).orElse(null);

                    if (vuelo != null) {
                        asientos = asientoService.getAsientosVuelo(parsedIdReserva, parsedIdVuelo);

                        if (asientos != null) {
                            asientoSeleccionados = asientos.stream()
                                    .filter(asiento -> "seleccionado".equalsIgnoreCase(asiento.getEstado()))
                                    .toList();

                            if (!asientoSeleccionados.isEmpty() && (asientoActual == null || !asientoSeleccionados.contains(asientoActual))) {
                                asientoActual = asientoSeleccionados.get(0);
                            }

                            construirFilasCabina();
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error cargando vuelo/asientos en CambioAsientoBean: " + e.getMessage());
            }
        }
    }

    public void seleccionarVuelo(Vuelo v) {
        if (v != null) {
            this.idVuelo = String.valueOf(v.getIdVuelo());
            this.asientosCambio.clear();
            this.asientoActual = null;
            cargarDatosVueloYAsientos();
        }
    }

    public void volver() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getExternalContext() != null) {
                if ("PERFIL".equalsIgnoreCase(this.origenRetorno)) {
                    context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/perfil/pasajero/index.xhtml");
                } else {
                    Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
                    if (this.idReserva != null) {
                        try {
                            sessionMap.put("CHECKIN_ID_RESERVA", Integer.parseInt(this.idReserva));
                        } catch (NumberFormatException ignored) {}
                    }
                    if (this.idItinerario != null) {
                        sessionMap.put("CHECKIN_ID_ITINERARIO", this.idItinerario);
                    }
                    context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/home/checkin.xhtml");
                }
            }
        } catch (Exception e) {
            Logger.logError("Error al volver desde cambio de asiento: " + e.getMessage());
        }
    }

    private void construirFilasCabina() {
        this.filasCabina = new ArrayList<>();
        if (this.asientos == null || this.asientos.isEmpty()) return;

        Map<Integer, List<InfoAsientoDTO>> mapaFilas = new TreeMap<>();
        for (InfoAsientoDTO a : this.asientos) {
            int numFila = (a.getFila() != null && a.getFila() > 0) ? a.getFila() : extraerFila(a.getNumeroAsiento());
            if (a.getLetra() == null || a.getLetra().trim().isEmpty()) {
                a.setLetra(extraerLetra(a.getNumeroAsiento()));
            }
            mapaFilas.computeIfAbsent(numFila, k -> new ArrayList<>()).add(a);
        }

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : mapaFilas.entrySet()) {
            int numFila = entry.getKey();
            List<InfoAsientoDTO> asientosFila = entry.getValue();
            asientosFila.sort(Comparator.comparing(InfoAsientoDTO::getLetra));

            boolean esEmergencia = asientosFila.stream().anyMatch(a -> Boolean.TRUE.equals(a.getEsEmergencia()));
            String claseFila = asientosFila.get(0).getClase();

            FilaCabinaDTO filaDTO = new FilaCabinaDTO(numFila, claseFila, esEmergencia);
            BloqueAsientosDTO bloqueActual = new BloqueAsientosDTO();
            int totalFila = asientosFila.size();

            for (int i = 0; i < totalFila; i++) {
                InfoAsientoDTO asiento = asientosFila.get(i);
                bloqueActual.getAsientos().add(asiento);

                boolean romper = (totalFila == 6 && i == 2) || (totalFila == 4 && i == 1) || (totalFila == 10 && (i == 2 || i == 6));
                if (romper) {
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

    private String extraerLetra(String num) {
        if (num == null) return "";
        String l = num.replaceAll("[0-9]", "").trim();
        return l.isEmpty() ? num : l;
    }

    private int extraerFila(String num) {
        if (num == null) return 1;
        String d = num.replaceAll("[^0-9]", "");
        return d.isEmpty() ? 1 : Integer.parseInt(d);
    }

    public void seleccionarAsientoAEditar(InfoAsientoDTO asiento) {
        this.asientoActual = asiento;
    }

    public void setearAsiento(InfoAsientoDTO asientoNuevo) {
        if (asientoActual != null && asientoNuevo != null && !"ocupado".equalsIgnoreCase(asientoNuevo.getEstado())) {
            // Validar restricción de cabina: no permitir seleccionar asientos de distinta clase
            if (asientoActual.getClase() != null && asientoNuevo.getClase() != null
                    && !asientoActual.getClase().trim().equalsIgnoreCase(asientoNuevo.getClase().trim())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Clase no coincidente",
                                "Tu asiento actual corresponde a cabina '" + asientoActual.getClase() + "'. Solo puedes seleccionar asientos dentro de esta misma clase."));
                return;
            }
            asientosCambio.put(asientoActual.getIdAsiento(), asientoNuevo);
        }
    }

    public void confirmar(String s) {
        if (asientosCambio.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin Cambios", "No has seleccionado un nuevo asiento del mapa."));
            return;
        }

        try {
            int parsedIdReserva = Integer.parseInt(idReserva);

            for (Map.Entry<Integer, InfoAsientoDTO> entry : asientosCambio.entrySet()) {
                Integer idOriginal = entry.getKey();
                InfoAsientoDTO nuevo = entry.getValue();

                reservaAsientoService.cambiarAsiento(nuevo.getIdAsiento(), parsedIdReserva, idOriginal);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Cambio Exitoso", "Tus asientos han sido actualizados con éxito."));

            asientosCambio.clear();
            cargarDatosVueloYAsientos();

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo procesar el cambio: " + e.getMessage()));
        }
    }

    public String getCambio(Integer idAsiento) {
        Optional<InfoAsientoDTO> opt = Optional.ofNullable(asientosCambio.get(idAsiento));
        return opt.map(InfoAsientoDTO::getNumeroAsiento).orElse("");
    }

    public String getEstadoVisualAsiento(InfoAsientoDTO a) {
        if (a == null) return "disponible";

        // 1. Si el asiento está marcado como ocupado en la BD
        if ("ocupado".equalsIgnoreCase(a.getEstado())) {
            return "ocupado";
        }

        // 2. Si el usuario seleccionó este asiento como NUEVA opción (color Naranjo/Ámbar)
        if (asientoActual != null) {
            String asientoElegido = getCambio(asientoActual.getIdAsiento());
            if (asientoElegido != null && asientoElegido.equalsIgnoreCase(a.getNumeroAsiento())) {
                return "seleccionado";
            }
        }

        // 3. Si este asiento es el ORIGINAL que actualmente tiene el pasajero (ej: 2F)
        if (asientoSeleccionados != null && asientoSeleccionados.stream()
                .anyMatch(orig -> orig.getIdAsiento() == a.getIdAsiento() ||
                        orig.getNumeroAsiento().equalsIgnoreCase(a.getNumeroAsiento()))) {
            return "en_proceso"; // Se destaca como el asiento origen
        }

        return "disponible";
    }

    // Getters y Setters
    public Vuelo getVuelo() { return vuelo; }
    public void setVuelo(Vuelo vuelo) { this.vuelo = vuelo; }
    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }
    public String getIdVuelo() { return idVuelo; }
    public void setIdVuelo(String idVuelo) { this.idVuelo = idVuelo; }
    public Integer getIdItinerario() { return idItinerario; }
    public void setIdItinerario(Integer idItinerario) { this.idItinerario = idItinerario; }
    public String getOrigenRetorno() { return origenRetorno; }
    public void setOrigenRetorno(String origenRetorno) { this.origenRetorno = origenRetorno; }
    public boolean isPermiteCambio() { return permiteCambio; }
    public void setPermiteCambio(boolean permiteCambio) { this.permiteCambio = permiteCambio; }
    public String getMensajeReglaTarifa() { return mensajeReglaTarifa; }
    public void setMensajeReglaTarifa(String mensajeReglaTarifa) { this.mensajeReglaTarifa = mensajeReglaTarifa; }
    public String getNombreTarifa() { return nombreTarifa; }
    public void setNombreTarifa(String nombreTarifa) { this.nombreTarifa = nombreTarifa; }
    public List<Vuelo> getVuelosReserva() { return vuelosReserva; }
    public List<InfoAsientoDTO> getAsientos() { return asientos; }
    public List<InfoAsientoDTO> getAsientoSeleccionados() { return asientoSeleccionados; }
    public InfoAsientoDTO getAsientoActual() { return asientoActual; }
    public void setAsientoActual(InfoAsientoDTO asientoActual) { this.asientoActual = asientoActual; }
    public List<FilaCabinaDTO> getFilasCabina() { return filasCabina; }
}