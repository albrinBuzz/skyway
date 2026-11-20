package com.SkyWay.views.perfil.Piloto;

import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.piloto.domain.service.PilotoPanelService;
import com.SkyWay.modules.piloto.presentation.dto.PilotoPanelDtos.*;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.ResumenFatigaDTO;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
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

@Named("perfilPilotoBean")
@ViewScoped
public class PerfilBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final ZoneId ZONA_OPS = ZoneId.of("America/Santiago");
    private static final Locale LOCALE_ES = new Locale("es", "CL");
    private static final DateTimeFormatter FMT_DIA_CORTO = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter FMT_FECHA_COMPLETA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final String TAB_VUELOS = "VUELOS";
    public static final String TAB_FLOTA = "FLOTA";
    public static final String TAB_ALERTAS = "ALERTAS";
    public static final String TAB_EXPEDIENTE = "EXPEDIENTE";

    public static final String SCOPE_HOY = "HOY";
    public static final String SCOPE_SEMANA = "SEMANA";
    public static final String SCOPE_PROXIMOS = "PROXIMOS";
    public static final String SCOPE_HISTORIAL = "HISTORIAL";
    public static final String SCOPE_CANCELADOS = "CANCELADOS";
    public static final String SCOPE_TODOS = "TODOS";

    @Autowired
    private transient PiloService piloService;

    @Autowired
    private transient PilotoPanelService pilotoPanelService;

    @Autowired
    private transient VueloService vueloService;

    private List<Vuelo> vuelos;
    private Vuelo selectedVuelo;
    private Usuario usuario;
    private Piloto piloto;

    private KpisPilotoDTO kpis;
    private VueloResumenPilotoDTO proximoVuelo;
    private List<VueloResumenPilotoDTO> vuelosResumen = List.of();
    private List<VueloResumenPilotoDTO> vuelosFiltrados = List.of();
    private List<FlotaPilotoDTO> flotaAsignada = List.of();
    private List<NotificacionPilotoDTO> notificaciones = List.of();
    private List<DiaAgendaPilotoDTO> diasAgenda7Dias = List.of();
    private List<VueloResumenPilotoDTO> vuelosEstaSemana = List.of();
    private int vuelosHoyCount;
    private int vuelosSemanaCount;
    private String horasSemanaTexto = "0h 00m";
    private int semanaOffset = 0;
    private String rangoSemanaTexto = "";
    private ScheduleModel scheduleModel = new DefaultScheduleModel();
    private LocalDate fechaInicialSchedule = LocalDate.now(ZONA_OPS);
    private ResumenFatigaDTO resumenFatiga;
    private BriefingVueloDTO briefingSeleccionado;

    private String tabActiva = TAB_VUELOS;
    private String briefingTabActiva = "RESUMEN";
    private String filtroScope = SCOPE_PROXIMOS;
    private String filtroTexto = "";
    private String fechaDiaFiltro = "";

    private long totalHorasVuelo;
    private int totalVuelosCompletados;
    private String modeloAvionPrincipal = "N/A";
    private double factorOcupacionPromedio;

    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                usuario = (Usuario) session.getAttribute("usuario");
            }
        }

        if (usuario != null) {
            piloService.findByRut(usuario.getRut()).ifPresent(p -> {
                this.piloto = p;
                cargarDatosPanel(true);
            });
        }

        long endTime = System.currentTimeMillis();
        Logger.logInfo("⏱ Tiempo total en init() PerfilBean Piloto: " + (endTime - startTime) + " ms");
    }

    public void recargarPanel() {
        if (piloto == null) {
            return;
        }
        cargarDatosPanel(false);
        agregarMensaje(FacesMessage.SEVERITY_INFO, "Panel actualizado", "Datos sincronizados desde la base de datos.");
    }

    private void cargarDatosPanel(boolean permitirAjusteScopeInicial) {
        PanelPilotoDataDTO data = pilotoPanelService.cargarPanelPiloto(piloto.getRut());
        this.kpis = data.getKpis();
        this.proximoVuelo = data.getProximoVuelo();
        this.vuelosResumen = data.getVuelos();
        this.flotaAsignada = data.getFlota();
        this.notificaciones = data.getNotificaciones();
        this.resumenFatiga = data.getFatiga();

        this.totalHorasVuelo = kpis.getHorasTotalesEnteras();
        this.totalVuelosCompletados = kpis.getTotalVuelos();
        this.modeloAvionPrincipal = kpis.getModeloAvionPrincipal();
        this.factorOcupacionPromedio = kpis.getFactorOcupacionRealPct();

        construirAgendaSemanal();
        construirScheduleModel();

        if (permitirAjusteScopeInicial && kpis.getVuelosProximos() == 0 && kpis.getTotalVuelos() > 0) {
            this.filtroScope = SCOPE_TODOS;
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

        Map<LocalDate, List<VueloResumenPilotoDTO>> porDia = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            porDia.put(lunesSemana.plusDays(i), new ArrayList<>());
        }

        List<VueloResumenPilotoDTO> semanaList = new ArrayList<>();
        int hoyCnt = 0;
        long minutosSemana = 0L;

        for (VueloResumenPilotoDTO v : vuelosResumen) {
            if (v.getFechaHoraSalida() == null) {
                continue;
            }
            LocalDate diaSalida = toLocalDate(v.getFechaHoraSalida());
            if (diaSalida == null) {
                continue;
            }
            if (!v.isEsCancelado() && diaSalida.equals(hoy)) {
                hoyCnt++;
            }
            if (!v.isEsCancelado() && !diaSalida.isBefore(lunesSemana) && !diaSalida.isAfter(domingoSemana)) {
                semanaList.add(v);
                minutosSemana += v.getMinutosDuracion();
                List<VueloResumenPilotoDTO> listaDia = porDia.get(diaSalida);
                if (listaDia != null) {
                    listaDia.add(v);
                }
            }
        }

        semanaList.sort(Comparator.comparing(VueloResumenPilotoDTO::getFechaHoraSalida));
        this.vuelosEstaSemana = semanaList;
        this.vuelosHoyCount = hoyCnt;
        this.vuelosSemanaCount = semanaList.size();
        this.horasSemanaTexto = formatearDuracion(minutosSemana);

        List<DiaAgendaPilotoDTO> dias = new ArrayList<>(7);
        for (Map.Entry<LocalDate, List<VueloResumenPilotoDTO>> entry : porDia.entrySet()) {
            LocalDate fecha = entry.getKey();
            List<VueloResumenPilotoDTO> vuelosDia = entry.getValue();
            vuelosDia.sort(Comparator.comparing(VueloResumenPilotoDTO::getFechaHoraSalida));

            long minDia = vuelosDia.stream().mapToLong(VueloResumenPilotoDTO::getMinutosDuracion).sum();
            boolean esHoy = fecha.equals(hoy);
            String dow = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, LOCALE_ES).toUpperCase(LOCALE_ES).replace(".", "");
            String diaCorto = esHoy ? ("HOY · " + dow) : dow;

            dias.add(new DiaAgendaPilotoDTO(
                    fecha.toString(),
                    diaCorto,
                    fecha.format(FMT_DIA_CORTO),
                    esHoy,
                    vuelosDia.size(),
                    formatearDuracion(minDia),
                    vuelosDia
            ));
        }
        this.diasAgenda7Dias = dias;
    }

    private void construirScheduleModel() {
        DefaultScheduleModel model = new DefaultScheduleModel();
        if (vuelosResumen == null || vuelosResumen.isEmpty()) {
            this.scheduleModel = model;
            this.fechaInicialSchedule = LocalDate.now(ZONA_OPS);
            return;
        }

        LocalDate hoy = LocalDate.now(ZONA_OPS);
        LocalDate primeraFechaFutura = null;
        LocalDate primeraFechaGeneral = null;

        for (VueloResumenPilotoDTO v : vuelosResumen) {
            if (v.getFechaHoraSalida() == null) {
                continue;
            }
            LocalDateTime start = v.getFechaHoraSalida().toInstant().atZone(ZONA_OPS).toLocalDateTime();
            LocalDateTime end = v.getFechaHoraLlegada() != null
                    ? v.getFechaHoraLlegada().toInstant().atZone(ZONA_OPS).toLocalDateTime()
                    : start.plusHours(2);

            if (primeraFechaGeneral == null) {
                primeraFechaGeneral = start.toLocalDate();
            }
            if (!v.isEsCancelado() && !start.toLocalDate().isBefore(hoy)) {
                if (primeraFechaFutura == null || start.toLocalDate().isBefore(primeraFechaFutura)) {
                    primeraFechaFutura = start.toLocalDate();
                }
            }

            String bg = "#2563eb";
            String border = "#4c7cf3";
            if (v.isEsCancelado()) {
                bg = "#475569";
                border = "#64748b";
            } else if ("success".equals(v.getEstadoSeveridad())) {
                bg = "#059669";
                border = "#10b981";
            } else if ("warning".equals(v.getEstadoSeveridad())) {
                bg = "#d97706";
                border = "#f59e0b";
            } else if (v.isEsFinalizado()) {
                bg = "#1e293b";
                border = "#334155";
            }

            String prefijo = v.isEsCancelado() ? "✕ " : "✈ ";
            String titulo = prefijo + v.getNumeroVuelo() + " (" + v.getOrigenIata() + "➔" + v.getDestinoIata() + ")";
            String descripcion = v.getOrigenCiudad() + " → " + v.getDestinoCiudad()
                    + " | " + v.getPuertaTerminalTexto()
                    + " | " + v.getAvionMatricula() + " (" + v.getModeloCompleto() + ")"
                    + " | " + v.getEstadoDescripcion();

            ScheduleEvent<?> event = DefaultScheduleEvent.builder()
                    .title(titulo)
                    .startDate(start)
                    .endDate(end)
                    .data(v.getIdVuelo())
                    .description(descripcion)
                    .backgroundColor(bg)
                    .borderColor(border)
                    .textColor("#ffffff")
                    .build();
            model.addEvent(event);
        }

        this.scheduleModel = model;
        this.fechaInicialSchedule = primeraFechaFutura != null
                ? primeraFechaFutura
                : (primeraFechaGeneral != null ? primeraFechaGeneral : hoy);
    }

    public void onScheduleEventSelect(SelectEvent<ScheduleEvent<?>> selectEvent) {
        if (selectEvent == null || selectEvent.getObject() == null || piloto == null) {
            return;
        }
        Object data = selectEvent.getObject().getData();
        if (data instanceof Integer idVuelo) {
            this.briefingTabActiva = "RESUMEN";
            this.briefingSeleccionado = pilotoPanelService.cargarBriefingVuelo(piloto.getRut(), idVuelo);
            sincronizarIntervaloEnBriefing(idVuelo);
            if (this.briefingSeleccionado == null) {
                agregarMensaje(FacesMessage.SEVERITY_WARN, "No disponible", "No se pudo cargar el briefing operacional del vuelo.");
            }
        }
    }

    private void sincronizarIntervaloEnBriefing(Integer idVuelo) {
        if (this.briefingSeleccionado == null || this.briefingSeleccionado.getResumen() == null || idVuelo == null) {
            return;
        }
        vuelosResumen.stream()
                .filter(v -> idVuelo.equals(v.getIdVuelo()))
                .findFirst()
                .ifPresent(v -> this.briefingSeleccionado.getResumen().setIntervaloPrevio(v.getIntervaloPrevio()));
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
            this.tabActiva = TAB_VUELOS;
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
        if (vuelosResumen == null || vuelosResumen.isEmpty()) {
            this.vuelosFiltrados = List.of();
            return;
        }

        LocalDate hoy = LocalDate.now(ZONA_OPS);
        LocalDate lunesSemana = hoy.plusWeeks(this.semanaOffset)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate domingoSemana = lunesSemana.plusDays(6);
        LocalDate fechaExacta = parsearFechaFiltro(this.fechaDiaFiltro);
        String q = filtroTexto != null ? filtroTexto.trim().toLowerCase(Locale.ROOT) : "";
        List<VueloResumenPilotoDTO> filtrados = new ArrayList<>();

        for (VueloResumenPilotoDTO v : vuelosResumen) {
            if (fechaExacta != null) {
                LocalDate diaSalida = toLocalDate(v.getFechaHoraSalida());
                if (diaSalida == null || !diaSalida.equals(fechaExacta)) {
                    continue;
                }
            } else {
                boolean pasaScope = switch (filtroScope) {
                    case SCOPE_HOY -> !v.isEsCancelado() && hoy.equals(toLocalDate(v.getFechaHoraSalida()));
                    case SCOPE_SEMANA -> !v.isEsCancelado() && estaEnRango(toLocalDate(v.getFechaHoraSalida()), lunesSemana, domingoSemana);
                    case SCOPE_PROXIMOS -> v.isEsProximo();
                    case SCOPE_HISTORIAL -> v.isEsFinalizado() && !v.isEsCancelado();
                    case SCOPE_CANCELADOS -> v.isEsCancelado();
                    default -> true;
                };
                if (!pasaScope) {
                    continue;
                }
            }
            if (!q.isEmpty() && !coincideBusqueda(v, q)) {
                continue;
            }
            filtrados.add(v);
        }

        if (SCOPE_PROXIMOS.equals(filtroScope) || SCOPE_SEMANA.equals(filtroScope) || SCOPE_HOY.equals(filtroScope) || fechaExacta != null) {
            filtrados.sort(Comparator.comparing(VueloResumenPilotoDTO::getFechaHoraSalida,
                    Comparator.nullsLast(Comparator.naturalOrder())));
        } else if (SCOPE_HISTORIAL.equals(filtroScope) || SCOPE_CANCELADOS.equals(filtroScope)) {
            filtrados.sort(Comparator.comparing(VueloResumenPilotoDTO::getFechaHoraSalida,
                    Comparator.nullsLast(Comparator.reverseOrder())));
        } else {
            filtrados.sort(Comparator
                    .comparing((VueloResumenPilotoDTO v) -> v.isEsProximo() ? 0 : (v.isEsCancelado() ? 2 : 1))
                    .thenComparing((VueloResumenPilotoDTO v) -> {
                        if (v.isEsProximo()) {
                            return v.getFechaHoraSalida() != null ? v.getFechaHoraSalida().getTime() : Long.MAX_VALUE;
                        }
                        return v.getFechaHoraSalida() != null ? -v.getFechaHoraSalida().getTime() : Long.MAX_VALUE;
                    }));
        }

        this.vuelosFiltrados = filtrados;
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

    private boolean coincideBusqueda(VueloResumenPilotoDTO v, String q) {
        return contiene(v.getNumeroVuelo(), q)
                || contiene(v.getOrigenIata(), q)
                || contiene(v.getOrigenCiudad(), q)
                || contiene(v.getDestinoIata(), q)
                || contiene(v.getDestinoCiudad(), q)
                || contiene(v.getAvionMatricula(), q)
                || contiene(v.getAvionModelo(), q)
                || contiene(v.getEstadoDescripcion(), q);
    }

    private boolean contiene(String valor, String q) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(q);
    }

    public void abrirBriefing(VueloResumenPilotoDTO vueloResumen) {
        if (piloto == null || vueloResumen == null) {
            return;
        }
        this.briefingTabActiva = "RESUMEN";
        this.briefingSeleccionado = pilotoPanelService.cargarBriefingVuelo(piloto.getRut(), vueloResumen.getIdVuelo());
        sincronizarIntervaloEnBriefing(vueloResumen.getIdVuelo());
        if (this.briefingSeleccionado == null) {
            agregarMensaje(FacesMessage.SEVERITY_WARN, "No disponible", "No se pudo cargar el briefing operacional del vuelo.");
        }
    }

    public void marcarNotificacionLeida(Integer idNotificacion) {
        if (piloto == null || idNotificacion == null) {
            return;
        }
        if (pilotoPanelService.marcarNotificacionLeida(piloto.getRut(), idNotificacion)) {
            cargarDatosPanel(false);
        }
    }

    public void marcarTodasLeidas() {
        if (piloto == null) {
            return;
        }
        int count = pilotoPanelService.marcarTodasNotificacionesLeidas(piloto.getRut());
        if (count > 0) {
            cargarDatosPanel(false);
            agregarMensaje(FacesMessage.SEVERITY_INFO, "Alertas al día", count + " notificación(es) marcadas como leídas.");
        }
    }

    public void verDetallesVuelo(Vuelo vuelo) {
        this.selectedVuelo = vuelo;
        if (vuelo != null && piloto != null) {
            this.briefingTabActiva = "RESUMEN";
            this.briefingSeleccionado = pilotoPanelService.cargarBriefingVuelo(piloto.getRut(), vuelo.getIdVuelo());
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

    // Getters y setters
    public long getTotalHorasVuelo() { return totalHorasVuelo; }
    public int getTotalVuelosCompletados() { return totalVuelosCompletados; }
    public String getModeloAvionPrincipal() { return modeloAvionPrincipal; }
    public double getFactorOcupacionPromedio() { return factorOcupacionPromedio; }
    public KpisPilotoDTO getKpis() { return kpis; }
    public VueloResumenPilotoDTO getProximoVuelo() { return proximoVuelo; }
    public List<VueloResumenPilotoDTO> getVuelosResumen() { return vuelosResumen; }
    public List<VueloResumenPilotoDTO> getVuelosFiltrados() { return vuelosFiltrados; }
    public List<FlotaPilotoDTO> getFlotaAsignada() { return flotaAsignada; }
    public List<NotificacionPilotoDTO> getNotificaciones() { return notificaciones; }
    public List<DiaAgendaPilotoDTO> getDiasAgenda7Dias() { return diasAgenda7Dias; }
    public List<VueloResumenPilotoDTO> getVuelosEstaSemana() { return vuelosEstaSemana; }
    public int getVuelosHoyCount() { return vuelosHoyCount; }
    public int getVuelosSemanaCount() { return vuelosSemanaCount; }
    public String getHorasSemanaTexto() { return horasSemanaTexto; }
    public int getSemanaOffset() { return semanaOffset; }
    public String getRangoSemanaTexto() { return rangoSemanaTexto; }
    public ScheduleModel getScheduleModel() { return scheduleModel; }
    public LocalDate getFechaInicialSchedule() { return fechaInicialSchedule; }
    public ResumenFatigaDTO getResumenFatiga() { return resumenFatiga; }
    public String getFechaDiaFiltro() { return fechaDiaFiltro; }
    public void setFechaDiaFiltro(String fechaDiaFiltro) { this.fechaDiaFiltro = fechaDiaFiltro; }
    public BriefingVueloDTO getBriefingSeleccionado() { return briefingSeleccionado; }
    public String getTabActiva() { return tabActiva; }
    public void setTabActiva(String tabActiva) { this.tabActiva = tabActiva; }
    public String getBriefingTabActiva() { return briefingTabActiva; }
    public void setBriefingTabActiva(String briefingTabActiva) { this.briefingTabActiva = briefingTabActiva; }
    public String getFiltroScope() { return filtroScope; }
    public void setFiltroScope(String filtroScope) { this.filtroScope = filtroScope; }
    public String getFiltroTexto() { return filtroTexto; }
    public void setFiltroTexto(String filtroTexto) { this.filtroTexto = filtroTexto; }

    public Piloto getPiloto() { return piloto; }
    public void setPiloto(Piloto piloto) { this.piloto = piloto; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public List<Vuelo> getVuelos() {
        if (vuelos == null && piloto != null) {
            vuelos = vueloService.findByPiloto(piloto);
        }
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) { this.vuelos = vuelos; }
    public Vuelo getSelectedVuelo() { return selectedVuelo; }
    public void setSelectedVuelo(Vuelo selectedVuelo) { this.selectedVuelo = selectedVuelo; }

    public String getLicencia() {
        return (piloto != null) ? piloto.getLicencia() : "N/A";
    }

    public int getExperienciaAnos() {
        return (piloto != null) ? piloto.getExperienciaAnos() : 0;
    }

    public String getEspecializaciones() {
        return (piloto != null && piloto.getEspecializaciones() != null)
                ? piloto.getEspecializaciones()
                : "Sin especializaciones registradas";
    }
}