package com.SkyWay.views.turno;

import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.tripulacion.domain.service.TripulacionPanelService;
import com.SkyWay.modules.tripulacion.domain.service.TripulacionService;
import com.SkyWay.modules.tripulacion.presentation.dto.TripulacionPanelDtos.*;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.ResumenFatigaDTO;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.service.TurnoTripulacionService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Named("misTurnosBean")
@ViewScoped
public class MisTurnosBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ZoneId ZONA_OPS = ZoneId.of("America/Santiago");
    private static final Locale LOCALE_ES = new Locale("es", "CL");
    private static final DateTimeFormatter FMT_DIA_CORTO = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_FECHA_COMPLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final String TAB_TURNOS = "TURNOS";
    public static final String TAB_ALERTAS = "ALERTAS";
    public static final String TAB_EXPEDIENTE = "EXPEDIENTE";

    public static final String SCOPE_PENDIENTES = "PENDIENTES";
    public static final String SCOPE_HOY = "HOY";
    public static final String SCOPE_SEMANA = "SEMANA";
    public static final String SCOPE_CONFIRMADOS = "CONFIRMADOS";
    public static final String SCOPE_RECHAZADOS = "RECHAZADOS";
    public static final String SCOPE_HISTORIAL = "HISTORIAL";
    public static final String SCOPE_TODOS = "TODOS";

    @Autowired
    private transient TurnoTripulacionService turnoTripulacionService;

    @Autowired
    private transient TripulacionService tripulacionService;

    @Autowired
    private transient TripulacionPanelService tripulacionPanelService;

    private List<TurnoTripulacion> turnosTripulante;
    private TurnoTripulacion selectedTurno;
    private Tripulacion tripulacion;
    private Usuario usuario;
    private String motivoRechazoForm;
    private TurnoTripulacion turnoARechazar;

    private KpisTripulacionDTO kpis;
    private TurnoResumenTripulacionDTO proximoTurno;
    private List<TurnoResumenTripulacionDTO> turnosResumen = List.of();
    private List<TurnoResumenTripulacionDTO> turnosFiltrados = List.of();
    private List<TurnoResumenTripulacionDTO> turnosEstaSemana = List.of();
    private List<DiaAgendaTripulacionDTO> diasAgendaSemana = List.of();
    private List<NotificacionTripulacionDTO> notificaciones = List.of();
    private BriefingTurnoDTO briefingSeleccionado;
    private TurnoResumenTripulacionDTO resumenARechazar;

    private int turnosHoyCount;
    private int turnosSemanaCount;
    private String horasSemanaTexto = "0h 00m";
    private int semanaOffset = 0;
    private String rangoSemanaTexto = "";
    private ScheduleModel scheduleModel = new DefaultScheduleModel();
    private LocalDate fechaInicialSchedule = LocalDate.now(ZONA_OPS);
    private ResumenFatigaDTO resumenFatiga;

    private String tabActiva = TAB_TURNOS;
    private String briefingTabActiva = "RESUMEN";
    private String filtroScope = SCOPE_TODOS;
    private String filtroTexto = "";
    private String fechaDiaFiltro = "";

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                usuario = (Usuario) session.getAttribute("usuario");
            }
        }

        if (usuario != null) {
            tripulacionService.findByRut(usuario.getRut()).ifPresent(t -> this.tripulacion = t);
            cargarDatosPanel(true);
        }
    }

    public void cargarTurnos() {
        if (usuario != null) {
            this.turnosTripulante = turnoTripulacionService.findByTripulacionRut(usuario.getRut());
        }
    }

    public void recargarPanel() {
        if (usuario == null) {
            return;
        }
        cargarDatosPanel(false);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "Panel actualizado", "Turnos y datos operacionales sincronizados desde la base de datos.");
    }

    private void cargarDatosPanel(boolean ajusteInicial) {
        cargarTurnos();
        PanelTripulacionDataDTO data = tripulacionPanelService.cargarPanelTripulacion(usuario.getRut());
        this.kpis = data.getKpis();
        this.proximoTurno = data.getProximoTurno();
        this.turnosResumen = data.getTurnos();
        this.notificaciones = data.getNotificaciones();
        this.resumenFatiga = data.getFatiga();

        construirAgendaSemanal();
        construirScheduleModel();

        if (ajusteInicial && this.kpis != null) {
            this.filtroScope = this.kpis.getTurnosPendientes() > 0 ? SCOPE_PENDIENTES : SCOPE_TODOS;
        }
        aplicarFiltros();
    }

    public void semanaAnterior() {
        this.semanaOffset--;
        construirAgendaSemanal();
        if (SCOPE_SEMANA.equals(this.filtroScope)) {
            aplicarFiltros();
        }
    }

    public void semanaActual() {
        this.semanaOffset = 0;
        construirAgendaSemanal();
        if (SCOPE_SEMANA.equals(this.filtroScope)) {
            aplicarFiltros();
        }
    }

    public void semanaSiguiente() {
        this.semanaOffset++;
        construirAgendaSemanal();
        if (SCOPE_SEMANA.equals(this.filtroScope)) {
            aplicarFiltros();
        }
    }

    private void construirAgendaSemanal() {
        LocalDate hoy = LocalDate.now(ZONA_OPS);
        LocalDate lunesSemana = hoy.plusWeeks(this.semanaOffset)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingoSemana = lunesSemana.plusDays(6);

        this.rangoSemanaTexto = "Lun " + lunesSemana.format(FMT_FECHA_COMPLETA)
                + " — Dom " + domingoSemana.format(FMT_FECHA_COMPLETA);

        Map<LocalDate, List<TurnoResumenTripulacionDTO>> porDia = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            porDia.put(lunesSemana.plusDays(i), new ArrayList<>());
        }

        List<TurnoResumenTripulacionDTO> semanaList = new ArrayList<>();
        int hoyCnt = 0;
        long minSemana = 0L;

        for (TurnoResumenTripulacionDTO t : turnosResumen) {
            if (t.getHoraInicio() == null) {
                continue;
            }
            LocalDate dia = toLocalDate(t.getHoraInicio());
            if (dia == null) {
                continue;
            }
            if (!t.isEsRechazado() && !t.isVueloCancelado() && dia.equals(hoy)) {
                hoyCnt++;
            }
            if (!t.isEsRechazado() && !t.isVueloCancelado() && !dia.isBefore(lunesSemana) && !dia.isAfter(domingoSemana)) {
                semanaList.add(t);
                minSemana += t.getMinutosDuracion();
                List<TurnoResumenTripulacionDTO> diaList = porDia.get(dia);
                if (diaList != null) {
                    diaList.add(t);
                }
            }
        }

        semanaList.sort(Comparator.comparing(TurnoResumenTripulacionDTO::getHoraInicio));
        this.turnosEstaSemana = semanaList;
        this.turnosHoyCount = hoyCnt;
        this.turnosSemanaCount = semanaList.size();
        this.horasSemanaTexto = formatearDuracion(minSemana);

        List<DiaAgendaTripulacionDTO> dias = new ArrayList<>(7);
        for (Map.Entry<LocalDate, List<TurnoResumenTripulacionDTO>> entry : porDia.entrySet()) {
            LocalDate fecha = entry.getKey();
            List<TurnoResumenTripulacionDTO> turnosDia = entry.getValue();
            turnosDia.sort(Comparator.comparing(TurnoResumenTripulacionDTO::getHoraInicio));

            long minDia = turnosDia.stream().mapToLong(TurnoResumenTripulacionDTO::getMinutosDuracion).sum();
            boolean esHoy = fecha.equals(hoy);
            String dow = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES).toUpperCase(LOCALE_ES).replace(".", "");
            String diaCorto = esHoy ? ("HOY · " + dow) : dow;

            dias.add(new DiaAgendaTripulacionDTO(
                    fecha.toString(),
                    diaCorto,
                    fecha.format(FMT_DIA_CORTO),
                    esHoy,
                    turnosDia.size(),
                    formatearDuracion(minDia),
                    turnosDia
            ));
        }
        this.diasAgendaSemana = dias;
    }

    private void construirScheduleModel() {
        DefaultScheduleModel model = new DefaultScheduleModel();
        if (turnosResumen == null || turnosResumen.isEmpty()) {
            this.scheduleModel = model;
            this.fechaInicialSchedule = LocalDate.now(ZONA_OPS);
            return;
        }

        LocalDate hoy = LocalDate.now(ZONA_OPS);
        LocalDate primeraFutura = null;
        LocalDate primeraGeneral = null;

        for (TurnoResumenTripulacionDTO t : turnosResumen) {
            if (t.getHoraInicio() == null) {
                continue;
            }
            LocalDateTime start = t.getHoraInicio().toInstant().atZone(ZONA_OPS).toLocalDateTime();
            LocalDateTime end = t.getHoraFin() != null
                    ? t.getHoraFin().toInstant().atZone(ZONA_OPS).toLocalDateTime()
                    : start.plusHours(4);

            if (primeraGeneral == null) {
                primeraGeneral = start.toLocalDate();
            }
            if (!t.isEsRechazado() && !start.toLocalDate().isBefore(hoy)) {
                if (primeraFutura == null || start.toLocalDate().isBefore(primeraFutura)) {
                    primeraFutura = start.toLocalDate();
                }
            }

            String bg = "#059669";
            String border = "#10b981";
            if (t.isEsRechazado()) {
                bg = "#991b1b";
                border = "#ef4444";
            } else if (t.isEsPendiente()) {
                bg = "#d97706";
                border = "#f59e0b";
            } else if (t.isEsFinalizado()) {
                bg = "#1e293b";
                border = "#475569";
            }

            String ruta = t.getIdVuelo() != null
                    ? t.getNumeroVuelo() + " (" + t.getOrigenIata() + "➔" + t.getDestinoIata() + ")"
                    : t.getTipoServicio();
            String titulo = (t.isEsPendiente() ? "⏳ " : (t.isEsRechazado() ? "✕ " : "✓ ")) + ruta;
            String desc = "#" + t.getIdTurnoTripulacion()
                    + " · " + t.getTipoServicio()
                    + " | Estado: " + t.getEstadoRespuesta()
                    + (t.getIdVuelo() != null ? " | " + t.getPuertaTerminalTexto() + " | Cmdte: " + t.getPilotoNombre() : "");

            ScheduleEvent<?> event = DefaultScheduleEvent.builder()
                    .title(titulo)
                    .startDate(start)
                    .endDate(end)
                    .data(t.getIdTurnoTripulacion())
                    .description(desc)
                    .backgroundColor(bg)
                    .borderColor(border)
                    .textColor("#ffffff")
                    .build();
            model.addEvent(event);
        }

        this.scheduleModel = model;
        this.fechaInicialSchedule = primeraFutura != null
                ? primeraFutura
                : (primeraGeneral != null ? primeraGeneral : hoy);
    }

    public void onScheduleEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent) {
        if (selectEvent == null || selectEvent.getObject() == null || usuario == null) {
            return;
        }
        Object data = selectEvent.getObject().getData();
        if (data instanceof Integer idTurnoTrip) {
            this.briefingTabActiva = "RESUMEN";
            this.briefingSeleccionado = tripulacionPanelService.cargarBriefingTurno(usuario.getRut(), idTurnoTrip);
            sincronizarIntervaloEnBriefing(idTurnoTrip);
            if (this.briefingSeleccionado == null) {
                agregarMensaje(FacesMessage.SEVERITY_WARN, "No disponible", "No se pudo cargar el briefing operacional del turno.");
            }
        }
    }

    public void abrirBriefing(TurnoResumenTripulacionDTO resumen) {
        if (usuario == null || resumen == null) {
            return;
        }
        this.briefingTabActiva = "RESUMEN";
        this.briefingSeleccionado = tripulacionPanelService.cargarBriefingTurno(usuario.getRut(), resumen.getIdTurnoTripulacion());
        sincronizarIntervaloEnBriefing(resumen.getIdTurnoTripulacion());
        if (this.briefingSeleccionado == null) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "No disponible", "No se pudo cargar el detalle del servicio.");
        }
    }

    private void sincronizarIntervaloEnBriefing(Integer idTurnoTripulacion) {
        if (this.briefingSeleccionado == null || this.briefingSeleccionado.getResumen() == null || idTurnoTripulacion == null) {
            return;
        }
        turnosResumen.stream()
                .filter(t -> idTurnoTripulacion.equals(t.getIdTurnoTripulacion()))
                .findFirst()
                .ifPresent(t -> this.briefingSeleccionado.getResumen().setIntervaloPrevio(t.getIntervaloPrevio()));
    }

    public void seleccionarDiaAgenda(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) {
            return;
        }
        if (isoDate.equals(this.fechaDiaFiltro)) {
            this.fechaDiaFiltro = "";
        } else {
            this.fechaDiaFiltro = isoDate;
            this.filtroScope = SCOPE_TODOS;
            this.tabActiva = TAB_TURNOS;
        }
        aplicarFiltros();
    }

    public void limpiarDiaFiltro() {
        this.fechaDiaFiltro = "";
        aplicarFiltros();
    }

    public void cambiarTab(String tab) {
        if (tab != null && !tab.isBlank()) {
            this.tabActiva = tab;
        }
    }

    public void cambiarBriefingTab(String tab) {
        if (tab != null && !tab.isBlank()) {
            this.briefingTabActiva = tab;
        }
    }

    public void cambiarScope(String scope) {
        if (scope != null && !scope.isBlank()) {
            this.filtroScope = scope;
            if (!SCOPE_TODOS.equals(scope)) {
                this.fechaDiaFiltro = "";
            }
            aplicarFiltros();
        }
    }

    public void aplicarFiltros() {
        if (turnosResumen == null || turnosResumen.isEmpty()) {
            this.turnosFiltrados = List.of();
            return;
        }

        LocalDate hoy = LocalDate.now(ZONA_OPS);
        LocalDate lunesSemana = hoy.plusWeeks(this.semanaOffset)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingoSemana = lunesSemana.plusDays(6);
        LocalDate fechaExacta = parsearFechaFiltro(this.fechaDiaFiltro);
        String q = filtroTexto != null ? filtroTexto.trim().toLowerCase(Locale.ROOT) : "";
        List<TurnoResumenTripulacionDTO> filtrados = new ArrayList<>();

        for (TurnoResumenTripulacionDTO t : turnosResumen) {
            if (fechaExacta != null) {
                LocalDate dia = toLocalDate(t.getHoraInicio());
                if (dia == null || !dia.equals(fechaExacta)) {
                    continue;
                }
            } else {
                boolean pasa = switch (filtroScope) {
                    case SCOPE_PENDIENTES -> t.isEsPendiente();
                    case SCOPE_HOY -> !t.isEsRechazado() && hoy.equals(toLocalDate(t.getHoraInicio()));
                    case SCOPE_SEMANA -> !t.isEsRechazado() && estaEnRango(toLocalDate(t.getHoraInicio()), lunesSemana, domingoSemana);
                    case SCOPE_CONFIRMADOS -> t.isEsConfirmado();
                    case SCOPE_RECHAZADOS -> t.isEsRechazado();
                    case SCOPE_HISTORIAL -> t.isEsFinalizado();
                    default -> true;
                };
                if (!pasa) {
                    continue;
                }
            }

            if (!q.isEmpty() && !coincideBusqueda(t, q)) {
                continue;
            }
            filtrados.add(t);
        }

        if (SCOPE_HISTORIAL.equals(filtroScope) || SCOPE_RECHAZADOS.equals(filtroScope)) {
            filtrados.sort(Comparator.comparing(TurnoResumenTripulacionDTO::getHoraInicio,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            filtrados.sort(Comparator.comparing(TurnoResumenTripulacionDTO::getHoraInicio,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        }

        this.turnosFiltrados = filtrados;
    }

    private boolean coincideBusqueda(TurnoResumenTripulacionDTO t, String q) {
        return contiene("#" + t.getIdTurnoTripulacion(), q)
                || contiene(t.getTipoServicio(), q)
                || contiene(t.getNumeroVuelo(), q)
                || contiene(t.getOrigenIata(), q)
                || contiene(t.getOrigenCiudad(), q)
                || contiene(t.getDestinoIata(), q)
                || contiene(t.getDestinoCiudad(), q)
                || contiene(t.getAvionMatricula(), q)
                || contiene(t.getPilotoNombre(), q);
    }

    private boolean contiene(String val, String q) {
        return val != null && val.toLowerCase(Locale.ROOT).contains(q);
    }

    public void verDetallesTurno(TurnoTripulacion turnoTripulacion) {
        this.selectedTurno = turnoTripulacion;
        if (turnoTripulacion != null && usuario != null) {
            this.briefingTabActiva = "RESUMEN";
            this.briefingSeleccionado = tripulacionPanelService.cargarBriefingTurno(
                    usuario.getRut(), turnoTripulacion.getIdTurnoTripulacion());
        }
    }

    public void prepararRechazo(TurnoTripulacion turno) {
        this.turnoARechazar = turno;
        this.motivoRechazoForm = "";
    }

    public void prepararRechazoPorResumen(TurnoResumenTripulacionDTO resumen) {
        if (resumen == null) {
            return;
        }
        this.resumenARechazar = resumen;
        this.turnoARechazar = turnoTripulacionService.findById(resumen.getIdTurnoTripulacion()).orElse(null);
        this.motivoRechazoForm = "";
    }

    public void ejecutarRechazo() {
        Integer idTarget = this.turnoARechazar != null
                ? this.turnoARechazar.getIdTurnoTripulacion()
                : (this.resumenARechazar != null ? this.resumenARechazar.getIdTurnoTripulacion() : null);

        if (idTarget == null) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Error", "No se ha seleccionado ningún turno para rechazar.");
            return;
        }
        if (usuario == null || !tripulacionPanelService.verificarPropiedadTurno(usuario.getRut(), idTarget)) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "No autorizado", "El turno seleccionado no pertenece a tu cuenta.");
            return;
        }
        if (this.motivoRechazoForm == null || this.motivoRechazoForm.trim().isEmpty()) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Motivo Requerido", "Debe ingresar el motivo justificado de rechazo del servicio.");
            return;
        }

        try {
            turnoTripulacionService.rechazarTurno(idTarget, this.motivoRechazoForm.trim());
            agregarMensaje(FacesMessage.SEVERITY_WARN, "Turno Rechazado",
                    "Has registrado el rechazo del turno #" + idTarget + ". Operaciones ha sido notificado para la reasignación.");
            this.turnoARechazar = null;
            this.resumenARechazar = null;
            this.motivoRechazoForm = "";
            cargarDatosPanel(false);
        } catch (Exception e) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Error al Rechazar", e.getMessage());
            Logger.logInfo(e.getMessage());
        }
    }

    public void confirmarAsistencia(TurnoTripulacion turno) {
        if (turno != null) {
            confirmarAsistenciaPorId(turno.getIdTurnoTripulacion());
        }
    }

    public void confirmarAsistenciaPorId(Integer idTurnoTripulacion) {
        if (idTurnoTripulacion == null || usuario == null) {
            return;
        }
        if (!tripulacionPanelService.verificarPropiedadTurno(usuario.getRut(), idTurnoTripulacion)) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "No autorizado", "El turno seleccionado no pertenece a tu cuenta.");
            return;
        }
        try {
            turnoTripulacionService.confirmarTurno(idTurnoTripulacion);
            agregarMensaje(FacesMessage.SEVERITY_INFO, "Turno Confirmado",
                    "Has confirmado tu asistencia para el turno #" + idTurnoTripulacion + ".");
            cargarDatosPanel(false);
        } catch (Exception e) {
            agregarMensaje(FacesMessage.SEVERITY_ERROR, "Error al Confirmar", e.getMessage());
            Logger.logInfo(e.getMessage());
        }
    }

    public void rechazarTurno(TurnoTripulacion turno) {
        prepararRechazo(turno);
    }

    public void marcarNotificacionLeida(Integer idNotificacion) {
        if (usuario == null || idNotificacion == null) {
            return;
        }
        if (tripulacionPanelService.marcarNotificacionLeida(usuario.getRut(), idNotificacion)) {
            cargarDatosPanel(false);
        }
    }

    public void marcarTodasLeidas() {
        if (usuario == null) {
            return;
        }
        int count = tripulacionPanelService.marcarTodasNotificacionesLeidas(usuario.getRut());
        if (count > 0) {
            cargarDatosPanel(false);
            agregarMensaje(FacesMessage.SEVERITY_INFO, "Alertas al día", count + " notificación(es) marcadas como leídas.");
        }
    }

    public long getNotificacionesNoLeidas() {
        if (notificaciones == null || notificaciones.isEmpty()) {
            return 0L;
        }
        return notificaciones.stream().filter(n -> !n.isLeido()).count();
    }

    private void agregarMensaje(FacesMessage.Severity severidad, String resumen, String detalle) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severidad, resumen, detalle));
        }
    }

    private LocalDate toLocalDate(Timestamp ts) {
        return ts != null ? Instant.ofEpochMilli(ts.getTime()).atZone(ZONA_OPS).toLocalDate() : null;
    }

    private boolean estaEnRango(LocalDate fecha, LocalDate inicio, LocalDate fin) {
        return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(fin);
    }

    private LocalDate parsearFechaFiltro(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private String formatearDuracion(long minutos) {
        long h = Math.max(0L, minutos) / 60L;
        long m = Math.max(0L, minutos) % 60L;
        return h + "h " + (m < 10 ? "0" : "") + m + "m";
    }

    // Getters y Setters
    public List<TurnoTripulacion> getTurnosTripulante() { return turnosTripulante; }
    public void setTurnosTripulante(List<TurnoTripulacion> turnosTripulante) { this.turnosTripulante = turnosTripulante; }
    public TurnoTripulacion getSelectedTurno() { return selectedTurno; }
    public void setSelectedTurno(TurnoTripulacion selectedTurno) { this.selectedTurno = selectedTurno; }
    public Tripulacion getTripulacion() { return tripulacion; }
    public void setTripulacion(Tripulacion tripulacion) { this.tripulacion = tripulacion; }
    public Usuario getUsuario() { return usuario; }
    public String getMotivoRechazoForm() { return motivoRechazoForm; }
    public void setMotivoRechazoForm(String motivoRechazoForm) { this.motivoRechazoForm = motivoRechazoForm; }
    public TurnoTripulacion getTurnoARechazar() { return turnoARechazar; }
    public void setTurnoARechazar(TurnoTripulacion turnoARechazar) { this.turnoARechazar = turnoARechazar; }

    public KpisTripulacionDTO getKpis() { return kpis; }
    public TurnoResumenTripulacionDTO getProximoTurno() { return proximoTurno; }
    public List<TurnoResumenTripulacionDTO> getTurnosResumen() { return turnosResumen; }
    public List<TurnoResumenTripulacionDTO> getTurnosFiltrados() { return turnosFiltrados; }
    public List<TurnoResumenTripulacionDTO> getTurnosEstaSemana() { return turnosEstaSemana; }
    public List<DiaAgendaTripulacionDTO> getDiasAgendaSemana() { return diasAgendaSemana; }
    public List<NotificacionTripulacionDTO> getNotificaciones() { return notificaciones; }
    public BriefingTurnoDTO getBriefingSeleccionado() { return briefingSeleccionado; }
    public TurnoResumenTripulacionDTO getResumenARechazar() { return resumenARechazar; }

    public int getTurnosHoyCount() { return turnosHoyCount; }
    public int getTurnosSemanaCount() { return turnosSemanaCount; }
    public String getHorasSemanaTexto() { return horasSemanaTexto; }
    public int getSemanaOffset() { return semanaOffset; }
    public String getRangoSemanaTexto() { return rangoSemanaTexto; }
    public ScheduleModel getScheduleModel() { return scheduleModel; }
    public LocalDate getFechaInicialSchedule() { return fechaInicialSchedule; }
    public ResumenFatigaDTO getResumenFatiga() { return resumenFatiga; }

    public String getTabActiva() { return tabActiva; }
    public void setTabActiva(String tabActiva) { this.tabActiva = tabActiva; }
    public String getBriefingTabActiva() { return briefingTabActiva; }
    public void setBriefingTabActiva(String briefingTabActiva) { this.briefingTabActiva = briefingTabActiva; }
    public String getFiltroScope() { return filtroScope; }
    public void setFiltroScope(String filtroScope) { this.filtroScope = filtroScope; }
    public String getFiltroTexto() { return filtroTexto; }
    public void setFiltroTexto(String filtroTexto) { this.filtroTexto = filtroTexto; }
    public String getFechaDiaFiltro() { return fechaDiaFiltro; }
    public void setFechaDiaFiltro(String fechaDiaFiltro) { this.fechaDiaFiltro = fechaDiaFiltro; }
}