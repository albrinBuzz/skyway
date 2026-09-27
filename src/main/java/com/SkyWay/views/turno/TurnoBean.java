package com.SkyWay.views.turno;

import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.tripulacion.domain.service.TripulacionService;
import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turno.domain.service.TurnoService;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.service.TurnoTripulacionService;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Named("turnoBean")
@ViewScoped
public class TurnoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String vueloId;
    private Vuelo vuelo;
    private Turno turno;

    // Variables de cabecera formateadas
    private String origenIata = "N/A";
    private String destinoIata = "N/A";
    private String origenNombre = "";
    private String destinoNombre = "";
    private String numeroVueloDisplay = "";

    private List<Tripulacion> tripulantesDisponibles;
    private String selectedTripulanteRut;
    private List<TurnoTripulacion> turnosTripulacion;

    // Métricas para Dashboard de Cabina
    private long totalPilotos = 0;
    private long totalCopilotos = 0;
    private long totalCabina = 0;

    @Autowired private TurnoService turnoService;
    @Autowired private TurnoTripulacionService turnoTripulacionService;
    @Autowired private TripulacionService tripulacionService;
    @Autowired private VueloService vueloService;

    @PostConstruct
    public void init() {
        Logger.logInfo(">>> [TurnoBean] Iniciando inicialización @PostConstruct...");
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            this.vueloId = externalContext.getRequestParameterMap().get("vueloId");
            Logger.logInfo("[TurnoBean] Parámetro 'vueloId' recibido: " + this.vueloId);

            if (this.vueloId != null && !this.vueloId.trim().isEmpty()) {
                Integer idVuelo = Integer.parseInt(this.vueloId);
                Logger.logInfo("[TurnoBean] Procesando ID de Vuelo numérico: " + idVuelo);

                // 1. Obtener datos detallados del vuelo
                try {
                    Logger.logInfo("[TurnoBean] Buscando entidad Vuelo con ID: " + idVuelo);
                    Optional<Vuelo> optVuelo = vueloService.findById(idVuelo);

                    if (optVuelo.isPresent()) {
                        this.vuelo = optVuelo.get();
                        this.numeroVueloDisplay = (this.vuelo.getNumeroVuelo() != null)
                                ? this.vuelo.getNumeroVuelo()
                                : "SK-" + this.vuelo.getIdVuelo();
                        Logger.logInfo("[TurnoBean] Vuelo cargado correctamente: " + this.numeroVueloDisplay);

                        // Extraer Origen y Destino desde el primer Segmento de Vuelo
                        if (this.vuelo.getSegmentoVuelos() != null && !this.vuelo.getSegmentoVuelos().isEmpty()) {
                            SegmentoVuelo seg = this.vuelo.getSegmentoVuelos().get(0);

                            if (seg.getAeropuertoOrigen() != null) {
                                this.origenIata = seg.getAeropuertoOrigen().getCodigoIata();
                                this.origenNombre = seg.getAeropuertoOrigen().getNombreAeropuerto();
                                Logger.logInfo("[TurnoBean] Origen configurado: " + this.origenIata + " - " + this.origenNombre);
                            } else {
                                Logger.logInfo("[TurnoBean] Advertencia: Aeropuerto de origen es nulo en el segmento 0.");
                            }

                            if (seg.getAeropuertoDestino() != null) {
                                this.destinoIata = seg.getAeropuertoDestino().getCodigoIata();
                                this.destinoNombre = seg.getAeropuertoDestino().getNombreAeropuerto();
                                Logger.logInfo("[TurnoBean] Destino configurado: " + this.destinoIata + " - " + this.destinoNombre);
                            } else {
                                Logger.logInfo("[TurnoBean] Advertencia: Aeropuerto de destino es nulo en el segmento 0.");
                            }
                        } else {
                            Logger.logInfo("[TurnoBean] Advertencia: El vuelo no posee lista de segmentoVuelos o está vacía.");
                        }
                    } else {
                        Logger.logInfo("[TurnoBean] No se encontró ningún Vuelo en BD con ID: " + idVuelo);
                    }
                } catch (Exception e) {
                    Logger.logInfo("[TurnoBean] Excepción al intentar obtener detalles del vuelo: " + e.getMessage());
                }

                // 2. Cargar el Turno y los Tripulantes
                Logger.logInfo("[TurnoBean] Buscando turnos asociados al vuelo ID: " + idVuelo);
                List<Turno> turnos = turnoService.findByVueloId(idVuelo);

                if (turnos != null && !turnos.isEmpty()) {
                    this.turno = turnos.get(0);
                    this.turnosTripulacion = new ArrayList<>(this.turno.getTurnoTripulacions1());
                    Logger.logInfo("[TurnoBean] Turno cargado ID: " + this.turno.getIdTurno()
                            + " con " + this.turnosTripulacion.size() + " tripulantes asignados.");
                } else {
                    Logger.logInfo("[TurnoBean] No se encontraron turnos configurados para el vuelo ID: " + idVuelo);
                    this.turnosTripulacion = new ArrayList<>();
                }
            } else {
                Logger.logInfo("[TurnoBean] El parámetro 'vueloId' fue nulo o está vacío.");
                this.turnosTripulacion = new ArrayList<>();
            }

            Logger.logInfo("[TurnoBean] Ejecutando actualizarListasYMetricas()...");
            actualizarListasYMetricas();
            Logger.logInfo("[TurnoBean] >>> Inicialización de TurnoBean completada exitosamente.");

        } catch (Exception e) {
            Logger.logInfo("[TurnoBean] Excepción crítica durante la inicialización @PostConstruct: " + e.getMessage());
            this.turnosTripulacion = new ArrayList<>();
        }
    }

    public void actualizarListasYMetricas() {
        List<Tripulacion> todos = tripulacionService.findAll();

        // Extraer los RUTs ya asignados
        List<String> rutsAsignados = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null)
                .map(tt -> tt.getTripulacion1().getRut())
                .collect(Collectors.toList());

        // Filtrar solo los tripulantes que NO están asignados actualmente
        this.tripulantesDisponibles = todos.stream()
                .filter(t -> !rutsAsignados.contains(t.getRut()))
                .collect(Collectors.toList());

        // Calcular métricas
        this.totalPilotos = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null && tt.getTripulacion1().getCargo() != null && tt.getTripulacion1().getCargo().equalsIgnoreCase("Piloto"))
                .count();

        this.totalCopilotos = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null && tt.getTripulacion1().getCargo() != null && tt.getTripulacion1().getCargo().toLowerCase().contains("copiloto"))
                .count();

        this.totalCabina = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null && tt.getTripulacion1().getCargo() != null && !tt.getTripulacion1().getCargo().toLowerCase().contains("piloto"))
                .count();
    }

    public void asignarTripulanteAlTurno() {
        if (this.selectedTripulanteRut == null || this.selectedTripulanteRut.trim().isEmpty()) {
            addErrorMessage("Atención", "Debe seleccionar un tripulante válido de la lista.");
            return;
        }

        try {
            Optional<Tripulacion> tripulanteOpt = tripulacionService.findByRut(this.selectedTripulanteRut);
            if (tripulanteOpt.isPresent()) {
                Tripulacion tripulante = tripulanteOpt.get();

                TurnoTripulacion nuevoTurnoTrip = new TurnoTripulacion();
                nuevoTurnoTrip.setTripulacion1(tripulante);
                nuevoTurnoTrip.setTurno1(this.turno);

                TurnoTripulacion guardado = turnoTripulacionService.save(nuevoTurnoTrip);
                this.turnosTripulacion.add(guardado);

                this.selectedTripulanteRut = null;
                actualizarListasYMetricas();

                addInfoMessage("Asignación Exitosa", "Se integró a " + tripulante.getUsuario().getNombre() + " " + tripulante.getUsuario().getApellido() + " a la tripulación.");
            }
        } catch (Exception e) {
            addErrorMessage("Error de Asignación", "No se pudo vincular al tripulante: " + e.getMessage());
        }
    }

    public void eliminarTripulante(TurnoTripulacion tt) {
        try {
            if (tt != null && tt.getIdTurnoTripulacion() != null) {
                turnoTripulacionService.delete(tt.getIdTurnoTripulacion());
            }
            this.turnosTripulacion.remove(tt);
            actualizarListasYMetricas();

            addInfoMessage("Tripulante Retirado", "Se desvinculó al tripulante del turno operativo.");
        } catch (Exception e) {
            addErrorMessage("Error al retirar", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }

    private void addInfoMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void addErrorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }

    // Getters y Setters
    public String getVueloId() { return vueloId; }
    public Vuelo getVuelo() { return vuelo; }
    public Turno getTurno() { return turno; }

    public String getOrigenIata() { return origenIata; }
    public String getDestinoIata() { return destinoIata; }
    public String getOrigenNombre() { return origenNombre; }
    public String getDestinoNombre() { return destinoNombre; }
    public String getNumeroVueloDisplay() { return numeroVueloDisplay; }

    public List<Tripulacion> getTripulantesDisponibles() { return tripulantesDisponibles; }
    public String getSelectedTripulanteRut() { return selectedTripulanteRut; }
    public void setSelectedTripulanteRut(String selectedTripulanteRut) { this.selectedTripulanteRut = selectedTripulanteRut; }

    public List<TurnoTripulacion> getTurnosTripulacion() { return turnosTripulacion; }
    public void setTurnosTripulacion(List<TurnoTripulacion> turnosTripulacion) { this.turnosTripulacion = turnosTripulacion; }

    public long getTotalPilotos() { return totalPilotos; }
    public long getTotalCopilotos() { return totalCopilotos; }
    public long getTotalCabina() { return totalCabina; }
}