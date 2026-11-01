package com.SkyWay.views.turno;

import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import java.sql.Timestamp;
import com.SkyWay.modules.tipoturno.domain.service.TipoTurnoService;
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
    private String selectedPilotoRut;
    private String selectedCabinaRut;
    private List<Tripulacion> pilotosDisponibles = new ArrayList<>();
    private List<Tripulacion> cabinaDisponibles = new ArrayList<>();
    private List<TurnoTripulacion> turnosTripulacion;

    // Métricas para Dashboard de Cabina
    private long totalPilotos = 0;
    private long totalCopilotos = 0;
    private long totalCabina = 0;

    @Autowired private TurnoService turnoService;
    @Autowired private TurnoTripulacionService turnoTripulacionService;
    @Autowired private TripulacionService tripulacionService;
    @Autowired private PiloService piloService;

    private boolean alertaDotacionInsuficiente;
    private boolean exencionDotacionAprobada = false;
    private String mensajeDotacionRequerida;
    @Autowired private VueloService vueloService;
    @Autowired private TipoTurnoService tipoTurnoService;

    // Gestión del Tipo de Turno
    private Integer selectedTipoTurnoId;
    private List<TipoTurno> tiposTurnoDisponibles;

    // Métricas y Alertas Operacionales
    private long totalConfirmados = 0;
    private long totalPendientes = 0;
    private long totalRechazados = 0;
    private boolean alertaRechazados = false;

    @PostConstruct
    public void init() {
        Logger.logInfo(">>> [TurnoBean] Iniciando inicialización @PostConstruct...");
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            this.vueloId = externalContext.getRequestParameterMap().get("vueloId");
            Logger.logInfo("[TurnoBean] Parámetro 'vueloId' recibido: " + this.vueloId);

            this.tiposTurnoDisponibles = tipoTurnoService.findAll();

            if (this.vueloId == null || this.vueloId.trim().isEmpty()) {
                List<Vuelo> vuelos = vueloService.findAll();
                if (vuelos != null && !vuelos.isEmpty()) {
                    this.vueloId = String.valueOf(vuelos.get(0).getIdVuelo());
                    Logger.logInfo("[TurnoBean] vueloId no provisto. Seleccionado primer vuelo ID: " + this.vueloId);
                }
            }

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
                } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
                    Logger.logInfo("[TurnoBean] Excepción al intentar obtener detalles del vuelo: " + e.getMessage());
                }

                // 2. Cargar el Turno y los Tripulantes
                Logger.logInfo("[TurnoBean] Buscando turnos asociados al vuelo ID: " + idVuelo);
                List<Turno> turnos = (this.vuelo != null)
                        ? List.of(turnoService.obtenerOCrearTurnoParaVuelo(this.vuelo))
                        : turnoService.findByVueloId(idVuelo);

                if (turnos != null && !turnos.isEmpty()) {
                    this.turno = turnos.get(0);
                    this.turnosTripulacion = (this.turno.getTurnoTripulacions1() != null)
                            ? new ArrayList<>(this.turno.getTurnoTripulacions1())
                            : new ArrayList<>();
                    if (this.turno.getTipoTurno() != null) {
                        this.selectedTipoTurnoId = this.turno.getTipoTurno().getIdTipo();
                    }
                    Logger.logInfo("[TurnoBean] Turno cargado ID: " + this.turno.getIdTurno()
                            + " con " + this.turnosTripulacion.size() + " tripulantes asignados.");

                    // Sincronizar e integrar automticamente al Comandante asignado al vuelo
                    try {
                        if (this.vuelo != null && this.vuelo.getPiloto() != null) {
                            Piloto pVuelo = this.vuelo.getPiloto();
                            String rutComandante = obtenerRutPiloto(pVuelo);

                            if (rutComandante != null && !rutComandante.trim().isEmpty()) {
                                tripulacionService.upsertTripulacion(rutComandante, "Comandante");
                                Tripulacion tripCom = tripulacionService.findByRut(rutComandante).orElse(null);

                                if (tripCom != null) {
                                    boolean yaEnTurno = this.turnosTripulacion.stream()
                                            .anyMatch(tt -> tt.getTripulacion1() != null &&
                                                    rutComandante.equalsIgnoreCase(obtenerRutTripulacion(tt.getTripulacion1())));

                                    if (!yaEnTurno) {
                                        List<TurnoTripulacion> enBd = turnoTripulacionService.findByTurnoId(this.turno.getIdTurno());
                                        TurnoTripulacion ttCom = null;
                                        if (enBd != null) {
                                            for (TurnoTripulacion tt : enBd) {
                                                if (tt.getTripulacion1() != null &&
                                                        rutComandante.equalsIgnoreCase(obtenerRutTripulacion(tt.getTripulacion1()))) {
                                                    ttCom = tt;
                                                    break;
                                                }
                                            }
                                        }
                                        if (ttCom != null) {
                                            this.turnosTripulacion.add(0, ttCom);
                                        } else {
                                            TurnoTripulacion nuevoTT = new TurnoTripulacion();
                                            nuevoTT.setTurno1(this.turno);
                                            nuevoTT.setTripulacion1(tripCom);
                                            nuevoTT.setEstado("CONFIRMADO");
                                            nuevoTT.setFechaRespuesta(new Timestamp(System.currentTimeMillis()));
                                            TurnoTripulacion guardado = turnoTripulacionService.save(nuevoTT);
                                            this.turnosTripulacion.add(0, guardado);
                                            Logger.logInfo("[TurnoBean] Comandante del vuelo (" + rutComandante + ") auto-asignado al turno.");
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Logger.logInfo("[TurnoBean] Error no bloqueante al auto-asignar Comandante: " + ex.getMessage());
                    }
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

        } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
            Logger.logInfo("[TurnoBean] Excepción crítica durante la inicialización @PostConstruct: " + e.getMessage());
            this.turnosTripulacion = new ArrayList<>();
        }
    }

    public void actualizarListasYMetricas() {
        // 1. Sincronizar pilotos a Tripulacion para asegurar que aparezcan como personal asignable
        try {
            if (piloService != null) {
                List<Piloto> pilotos = piloService.findAll();
                if (pilotos != null) {
                    String rutComandante = (this.vuelo != null && this.vuelo.getPiloto() != null)
                            ? obtenerRutPiloto(this.vuelo.getPiloto()) : null;
                    for (Piloto p : pilotos) {
                        String rutP = obtenerRutPiloto(p);
                        if (rutP != null && !rutP.trim().isEmpty()) {
                            boolean esComandanteVuelo = (rutComandante != null && rutP.equalsIgnoreCase(rutComandante));
                            String cargo = esComandanteVuelo ? "Comandante" :
                                    ((p.getExperienciaAnos() <= 8) ? "Copiloto" : "Piloto");
                            tripulacionService.upsertTripulacion(rutP, cargo);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logInfo("[TurnoBean] Advertencia al sincronizar pilotos a tripulacion: " + e.getMessage());
        }

        List<Tripulacion> todos = tripulacionService.findAll();

        // Extraer los RUTs ya asignados
        List<String> rutsAsignados = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null)
                .map(tt -> obtenerRutTripulacion(tt.getTripulacion1()))
                .filter(r -> r != null && !r.trim().isEmpty())
                .collect(Collectors.toList());

        // Filtrar solo los tripulantes que NO están asignados actualmente y que están disponibles para la fecha y horario operativo del vuelo
        this.tripulantesDisponibles = (todos != null)
                ? todos.stream()
                        .filter(t -> {
                            String rutT = obtenerRutTripulacion(t);
                            if (rutT == null || rutsAsignados.contains(rutT)) {
                                return false;
                            }
                            if (this.turno != null && this.turno.getHoraInicio() != null && this.turno.getHoraFin() != null) {
                                return turnoTripulacionService.estaDisponible(t, this.turno);
                            }
                            return true;
                        })
                        .collect(Collectors.toList())
                : new ArrayList<>();

        Logger.logInfo("[TurnoBean] Tripulantes disponibles calculados para fecha/horario: " + this.tripulantesDisponibles.size());

        // Separar disponibles en tripulación técnica (pilotos/copilotos) y de cabina (sobrecargos/azafatas)
        this.pilotosDisponibles = this.tripulantesDisponibles.stream()
                .filter(t -> t.getCargo() != null &&
                        (t.getCargo().toLowerCase().contains("piloto") ||
                         t.getCargo().toLowerCase().contains("copiloto") ||
                         t.getCargo().toLowerCase().contains("comandante") ||
                         t.getCargo().toLowerCase().contains("primer oficial")))
                .collect(Collectors.toList());

        this.cabinaDisponibles = this.tripulantesDisponibles.stream()
                .filter(t -> t.getCargo() == null ||
                        (!t.getCargo().toLowerCase().contains("piloto") &&
                         !t.getCargo().toLowerCase().contains("copiloto") &&
                         !t.getCargo().toLowerCase().contains("comandante") &&
                         !t.getCargo().toLowerCase().contains("primer oficial")))
                .collect(Collectors.toList());

        // Calcular métricas
        this.totalPilotos = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null && tt.getTripulacion1().getCargo() != null &&
                        (tt.getTripulacion1().getCargo().equalsIgnoreCase("Piloto") || tt.getTripulacion1().getCargo().toLowerCase().contains("comandante")))
                .count();

        this.totalCopilotos = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null && tt.getTripulacion1().getCargo() != null &&
                        (tt.getTripulacion1().getCargo().toLowerCase().contains("copiloto") || tt.getTripulacion1().getCargo().toLowerCase().contains("primer oficial")))
                .count();

        this.totalConfirmados = this.turnosTripulacion.stream()
                .filter(tt -> "CONFIRMADO".equalsIgnoreCase(tt.getEstado()))
                .count();

        this.totalPendientes = this.turnosTripulacion.stream()
                .filter(tt -> tt.getEstado() == null || "PENDIENTE".equalsIgnoreCase(tt.getEstado()))
                .count();

        this.totalRechazados = this.turnosTripulacion.stream()
                .filter(tt -> "RECHAZADO".equalsIgnoreCase(tt.getEstado()))
                .count();

        this.alertaRechazados = (this.totalRechazados > 0);

        this.totalCabina = this.turnosTripulacion.stream()
                .filter(tt -> tt.getTripulacion1() != null && tt.getTripulacion1().getCargo() != null &&
                        !tt.getTripulacion1().getCargo().toLowerCase().contains("piloto") &&
                        !tt.getTripulacion1().getCargo().toLowerCase().contains("comandante"))
                .count();

        // Evaluar requerimientos de dotación operacional según duración del vuelo
        if (this.exencionDotacionAprobada) {
            this.alertaDotacionInsuficiente = false;
            this.mensajeDotacionRequerida = null;
            return;
        }

        double duracion = (this.turno != null) ? this.turno.getDuracionHoras() : 0.0;
        long totalPilotosMando = this.totalPilotos + this.totalCopilotos;

        if (duracion > 18.0) {
            // Vuelo ULH (>18h): Exige al menos 3 pilotos de mando (Comandantes + Copilotos de relevo)
            if (this.totalPilotos == 0) {
                this.alertaDotacionInsuficiente = true;
                this.mensajeDotacionRequerida = String.format(java.util.Locale.US,
                        "Vuelo de Ultra Largo Alcance (%.1f hrs): Requiere al menos 1 Piloto Comandante y pilotos de relevo a bordo. Actualmente no hay Comandantes asignados.", duracion);
            } else if (totalPilotosMando < 3) {
                this.alertaDotacionInsuficiente = true;
                String nombreCom = (this.vuelo != null && this.vuelo.getPiloto() != null)
                        ? obtenerNombrePiloto(this.vuelo.getPiloto())
                        : "Comandante titular";
                long faltantes = 3 - totalPilotosMando;
                this.mensajeDotacionRequerida = String.format(java.util.Locale.US,
                        "Vuelo de Ultra Largo Alcance (%.1f hrs): La normativa ULH exige al menos 3 pilotos técnicos a bordo (Comandante: %s). Actualmente asignados: %d de 3. Incorpore %d piloto o copiloto adicional para habilitar el despacho.",
                        duracion, nombreCom, totalPilotosMando, faltantes);
            } else {
                this.alertaDotacionInsuficiente = false;
                this.mensajeDotacionRequerida = null;
            }
        } else if (duracion > 12.0) {
            // Vuelo Largo Alcance (12h - 18h): Requiere al menos 2 pilotos técnicos de mando
            if (totalPilotosMando < 2) {
                this.alertaDotacionInsuficiente = true;
                long faltantes = 2 - totalPilotosMando;
                this.mensajeDotacionRequerida = String.format(java.util.Locale.US,
                        "Vuelo de Largo Alcance (%.1f hrs): Requiere tripulación de relevo (mínimo 2 pilotos de mando a bordo). Actualmente asignados: %d de 2 (falta %d piloto o copiloto).",
                        duracion, totalPilotosMando, faltantes);
            } else {
                this.alertaDotacionInsuficiente = false;
                this.mensajeDotacionRequerida = null;
            }
        } else {
            // Vuelo Estándar (<=12h): Requiere al menos 1 piloto
            if (totalPilotosMando < 1) {
                this.alertaDotacionInsuficiente = true;
                this.mensajeDotacionRequerida = "Vuelo comercial sin tripulación técnica asignada. Incorpore al menos un piloto al roster.";
            } else {
                this.alertaDotacionInsuficiente = false;
                this.mensajeDotacionRequerida = null;
            }
        }
    }

    public void actualizarTipoTurno() {
        if (this.turno == null) {
            addErrorMessage("Error", "No existe un turno operacional asociado a este vuelo.");
            return;
        }
        if (this.selectedTipoTurnoId == null) {
            addErrorMessage("Error", "Debe seleccionar un tipo de turno válido.");
            return;
        }
        try {
            Optional<TipoTurno> opt = tipoTurnoService.findById(this.selectedTipoTurnoId);
            if (opt.isPresent()) {
                this.turno.setTipoTurno(opt.get());
                this.turno = turnoService.save(this.turno);
                addInfoMessage("Tipo de Turno Actualizado", "El servicio ha sido configurado como: " + opt.get().getNombre());
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
            addErrorMessage("Error al actualizar", "No se pudo actualizar el tipo de turno: " + e.getMessage());
        }
    }

    public void aprobarRechazoYReasignar(TurnoTripulacion tt) {
        try {
            if (tt != null && tt.getIdTurnoTripulacion() != null) {
                turnoTripulacionService.delete(tt.getIdTurnoTripulacion());
            }
            this.turnosTripulacion.remove(tt);
            actualizarListasYMetricas();
            addInfoMessage("Vacante Liberada para Reasignación",
                    "Se ha retirado al tripulante que rechazó el turno. La vacante está lista para ser cubierta.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
            addErrorMessage("Error", "No se pudo procesar la vacante: " + e.getMessage());
        }
    }

    public void aprobarTurnoManual(TurnoTripulacion tt) {
        try {
            if (tt != null && tt.getIdTurnoTripulacion() != null) {
                tt.setEstado("CONFIRMADO");
                tt.setFechaRespuesta(new java.sql.Timestamp(System.currentTimeMillis()));
                turnoTripulacionService.save(tt);
                actualizarListasYMetricas();
                addInfoMessage("Aprobación Administrativa", "El turno #" + tt.getIdTurnoTripulacion() + " ha sido confirmado por la administración.");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
            addErrorMessage("Error", "No se pudo confirmar el turno: " + e.getMessage());
        }
    }

    public void asignarPilotoAlTurno() {
        this.selectedTripulanteRut = this.selectedPilotoRut;
        asignarTripulanteAlTurno();
        this.selectedPilotoRut = null;
    }

    public void asignarCabinaAlTurno() {
        this.selectedTripulanteRut = this.selectedCabinaRut;
        asignarTripulanteAlTurno();
        this.selectedCabinaRut = null;
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
                if (this.turno == null) {
                    addErrorMessage("Error de Turno", "No se encontró un turno activo asociado para este vuelo.");
                    return;
                }

                // Validar horas legales, solapamiento y descanso mínimo reglamentario
                turnoTripulacionService.validarAsignacion(tripulante, this.turno);

                nuevoTurnoTrip.setTurno1(this.turno);
                nuevoTurnoTrip.setEstado("PENDIENTE");

                TurnoTripulacion guardado = turnoTripulacionService.save(nuevoTurnoTrip);
                this.turnosTripulacion.add(guardado);

                this.selectedTripulanteRut = null;
                actualizarListasYMetricas();

                addInfoMessage("Asignación Exitosa", "Se integró a " + tripulante.getUsuario().getNombre() + " " + tripulante.getUsuario().getApellido() + " a la tripulación.");
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
            addErrorMessage("Error de Asignación", "No se pudo vincular al tripulante: " + e.getMessage());
        }
    }

    public void eliminarTripulante(TurnoTripulacion tt) {
        try {
            if (tt != null && tt.getTripulacion1() != null && this.vuelo != null && this.vuelo.getPiloto() != null) {
                String rutCom = obtenerRutPiloto(this.vuelo.getPiloto());
                String rutTrip = obtenerRutTripulacion(tt.getTripulacion1());
                if (rutCom != null && rutTrip != null && rutCom.equalsIgnoreCase(rutTrip)) {
                    addErrorMessage("Comandante de Cabina",
                            "No se puede retirar al Comandante designado al vuelo desde aqu. Para cambiar el Comandante al mando, actualice las especificaciones en Ajustes de Vuelo.");
                    return;
                }
            }
            if (tt != null && tt.getIdTurnoTripulacion() != null) {
                turnoTripulacionService.delete(tt.getIdTurnoTripulacion());
            }
            this.turnosTripulacion.remove(tt);
            actualizarListasYMetricas();

            addInfoMessage("Tripulante Retirado", "Se desvinculó al tripulante del turno operativo.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            addErrorMessage("Restricción Operacional", e.getMessage());
        } catch (Exception e) {
            addErrorMessage("Error al retirar", "No se pudo eliminar el registro: " + e.getMessage());
        }
    }

    private void addInfoMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void addWarnMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, summary, detail));
    }

    private void addErrorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }

    // Getters y Setters
    private String obtenerRutPiloto(Piloto p) {
        if (p == null) return null;
        if (p.getUsuario() != null && p.getUsuario().getRut() != null && !p.getUsuario().getRut().trim().isEmpty()) {
            return p.getUsuario().getRut();
        }
        return p.getRut();
    }

    private String obtenerNombrePiloto(Piloto p) {
        if (p == null) return "Piloto";
        if (p.getUsuario() != null) {
            return p.getUsuario().getNombre() + " " + p.getUsuario().getApellido();
        }
        return (p.getRut() != null) ? p.getRut() : "Piloto";
    }

    private String obtenerRutTripulacion(Tripulacion t) {
        if (t == null) return null;
        if (t.getRut() != null && !t.getRut().trim().isEmpty()) {
            return t.getRut();
        }
        return (t.getUsuario() != null) ? t.getUsuario().getRut() : null;
    }

    public String getSelectedPilotoRut() { return selectedPilotoRut; }
    public void setSelectedPilotoRut(String selectedPilotoRut) { this.selectedPilotoRut = selectedPilotoRut; }

    public String getSelectedCabinaRut() { return selectedCabinaRut; }
    public void setSelectedCabinaRut(String selectedCabinaRut) { this.selectedCabinaRut = selectedCabinaRut; }

    public List<Tripulacion> getPilotosDisponibles() { return pilotosDisponibles; }
    public List<Tripulacion> getCabinaDisponibles() { return cabinaDisponibles; }

    public int getPilotosRequeridos() {
        double duracion = (this.turno != null) ? this.turno.getDuracionHoras() : 0.0;
        if (duracion > 18.0) return 3;
        if (duracion > 12.0) return 2;
        return 1;
    }

    public boolean isExencionDotacionAprobada() {
        return exencionDotacionAprobada;
    }

    public void aprobarExencionDotacion() {
        this.exencionDotacionAprobada = true;
        this.alertaDotacionInsuficiente = false;
        addInfoMessage("Exención Operativa Aprobada", "Se ha autorizado el despacho con la dotación actual bajo supervisión de operaciones.");
    }

    public void revocarExencionDotacion() {
        this.exencionDotacionAprobada = false;
        actualizarListasYMetricas();
        addInfoMessage("Control Reglamentario Reactivado", "Se restableció la validación estricta de relevos a bordo.");
    }


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

    public Integer getSelectedTipoTurnoId() { return selectedTipoTurnoId; }
    public void setSelectedTipoTurnoId(Integer selectedTipoTurnoId) { this.selectedTipoTurnoId = selectedTipoTurnoId; }

    public List<TipoTurno> getTiposTurnoDisponibles() { return tiposTurnoDisponibles; }
    public void setTiposTurnoDisponibles(List<TipoTurno> tiposTurnoDisponibles) { this.tiposTurnoDisponibles = tiposTurnoDisponibles; }

    public long getTotalConfirmados() { return totalConfirmados; }
    public long getTotalPendientes() { return totalPendientes; }
    public long getTotalRechazados() { return totalRechazados; }
    public boolean isAlertaRechazados() { return alertaRechazados; }
    public boolean isAlertaDotacionInsuficiente() { return alertaDotacionInsuficiente; }
    public String getMensajeDotacionRequerida() { return mensajeDotacionRequerida; }
}