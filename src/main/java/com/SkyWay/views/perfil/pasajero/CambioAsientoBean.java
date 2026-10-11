package com.SkyWay.views.perfil.pasajero;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.BloqueAsientosDTO;
import com.SkyWay.modules.asiento.presentation.dto.FilaCabinaDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

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

    private String idVuelo;
    private String idReserva;

    private Vuelo vuelo;
    private InfoAsientoDTO asientoActual;

    private List<InfoAsientoDTO> asientos = new ArrayList<>();
    private List<InfoAsientoDTO> asientoSeleccionados = new ArrayList<>();
    private HashMap<Integer, InfoAsientoDTO> asientosCambio = new HashMap<>();

    // Estructura de Cabina para renderizado realista
    private List<FilaCabinaDTO> filasCabina = new ArrayList<>();

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
            idVuelo = params.get("idVuelo");
            idReserva = params.get("idReserva");

            cargarDatosVueloYAsientos();
        }
    }

    public void cargarDatosVueloYAsientos() {
        if (idVuelo != null && !idVuelo.isEmpty() && idReserva != null && !idReserva.isEmpty()) {
            try {
                int parsedIdVuelo = Integer.parseInt(idVuelo);
                int parsedIdReserva = Integer.parseInt(idReserva);

                vuelo = vueloService.findById(parsedIdVuelo).orElse(null);

                if (vuelo != null) {
                    asientos = asientoService.getAsientosVuelo(parsedIdReserva, parsedIdVuelo);

                    if (asientos != null) {
                        asientoSeleccionados = asientos.stream()
                                .filter(asiento -> "seleccionado".equalsIgnoreCase(asiento.getEstado()))
                                .toList();

                        // Establecer por defecto el primer asiento a cambiar
                        if (!asientoSeleccionados.isEmpty() && asientoActual == null) {
                            asientoActual = asientoSeleccionados.get(0);
                        }

                        construirFilasCabina();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error cargando vuelo/asientos en CambioAsientoBean: " + e.getMessage());
            }
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
    public List<InfoAsientoDTO> getAsientos() { return asientos; }
    public List<InfoAsientoDTO> getAsientoSeleccionados() { return asientoSeleccionados; }
    public InfoAsientoDTO getAsientoActual() { return asientoActual; }
    public void setAsientoActual(InfoAsientoDTO asientoActual) { this.asientoActual = asientoActual; }
    public List<FilaCabinaDTO> getFilasCabina() { return filasCabina; }
}