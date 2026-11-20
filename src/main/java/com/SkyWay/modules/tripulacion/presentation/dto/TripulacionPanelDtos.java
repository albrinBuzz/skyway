package com.SkyWay.modules.tripulacion.presentation.dto;

import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.IntervaloServicioInfo;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.ResumenFatigaDTO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * DTOs inmutables y serializables compatibles con Jakarta EL para el Portal de Tripulación de Cabina,
 * respaldados íntegramente por consultas reales en PostgreSQL V3.
 */
public final class TripulacionPanelDtos {

    private TripulacionPanelDtos() {
    }

    public static final class KpisTripulacionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int totalTurnos;
        private final int turnosPendientes;
        private final int turnosConfirmados;
        private final int turnosRechazados;
        private final int turnosProximos;
        private final int turnosCompletados;
        private final String horasConfirmadasTexto;
        private final String horasPendientesTexto;
        private final String horasTotalesTexto;
        private final int vuelosDistintos;
        private final int aeropuertosDistintos;
        private final long totalPasajerosAtendidos;
        private final boolean descansoReglamentarioOk;
        private final String detalleDescansoTexto;

        public KpisTripulacionDTO(
                int totalTurnos, int turnosPendientes, int turnosConfirmados, int turnosRechazados,
                int turnosProximos, int turnosCompletados, String horasConfirmadasTexto,
                String horasPendientesTexto, String horasTotalesTexto, int vuelosDistintos,
                int aeropuertosDistintos, long totalPasajerosAtendidos,
                boolean descansoReglamentarioOk, String detalleDescansoTexto
        ) {
            this.totalTurnos = totalTurnos;
            this.turnosPendientes = turnosPendientes;
            this.turnosConfirmados = turnosConfirmados;
            this.turnosRechazados = turnosRechazados;
            this.turnosProximos = turnosProximos;
            this.turnosCompletados = turnosCompletados;
            this.horasConfirmadasTexto = horasConfirmadasTexto;
            this.horasPendientesTexto = horasPendientesTexto;
            this.horasTotalesTexto = horasTotalesTexto;
            this.vuelosDistintos = vuelosDistintos;
            this.aeropuertosDistintos = aeropuertosDistintos;
            this.totalPasajerosAtendidos = totalPasajerosAtendidos;
            this.descansoReglamentarioOk = descansoReglamentarioOk;
            this.detalleDescansoTexto = detalleDescansoTexto;
        }

        public int getTotalTurnos() { return totalTurnos; }
        public int getTurnosPendientes() { return turnosPendientes; }
        public int getTurnosConfirmados() { return turnosConfirmados; }
        public int getTurnosRechazados() { return turnosRechazados; }
        public int getTurnosProximos() { return turnosProximos; }
        public int getTurnosCompletados() { return turnosCompletados; }
        public String getHorasConfirmadasTexto() { return horasConfirmadasTexto; }
        public String getHorasPendientesTexto() { return horasPendientesTexto; }
        public String getHorasTotalesTexto() { return horasTotalesTexto; }
        public int getVuelosDistintos() { return vuelosDistintos; }
        public int getAeropuertosDistintos() { return aeropuertosDistintos; }
        public long getTotalPasajerosAtendidos() { return totalPasajerosAtendidos; }
        public boolean isDescansoReglamentarioOk() { return descansoReglamentarioOk; }
        public String getDetalleDescansoTexto() { return detalleDescansoTexto; }
    }

    public static final class TurnoResumenTripulacionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Integer idTurnoTripulacion;
        private final Integer idTurno;
        private final String tipoServicio;
        private final Timestamp horaInicio;
        private final Timestamp horaFin;
        private final long minutosDuracion;
        private final String duracionTexto;
        private final String estadoRespuesta;
        private final String estadoRespuestaSeveridad;
        private final Timestamp fechaRespuesta;
        private final String motivoRechazo;
        private final boolean esPendiente;
        private final boolean esConfirmado;
        private final boolean esRechazado;
        private final boolean esFinalizado;
        private final boolean esProximo;
        private final String ventanaOperativa;

        private final Integer idVuelo;
        private final String numeroVuelo;
        private final Timestamp vueloSalida;
        private final Timestamp vueloLlegada;
        private final String vueloEstadoDesc;
        private final String vueloEstadoSeveridad;
        private final boolean vueloCancelado;
        private final String aerolineaNombre;

        private final String origenIata;
        private final String origenCiudad;
        private final String origenAeropuerto;
        private final String destinoIata;
        private final String destinoCiudad;
        private final String destinoAeropuerto;
        private final int cantidadTramos;
        private final String puertaOrigenCodigo;
        private final String terminalOrigen;

        private final String pilotoNombre;
        private final String pilotoLicencia;
        private final String pilotoTelefono;

        private final Integer idAvion;
        private final String avionMatricula;
        private final String avionFabricante;
        private final String avionModelo;
        private final int avionCapacidadPax;
        private final String avionEstadoMantenimiento;

        private final int pasajerosReservados;
        private final int pasajerosCheckin;
        private final int pasajerosSalidaEmergencia;
        private final double ocupacionPct;
        private final double checkinPct;
        private final int piezasEquipaje;
        private final double pesoEquipajeKg;
        private final int tripulacionTotal;
        private final int tripulacionConfirmada;
        private IntervaloServicioInfo intervaloPrevio;

        public TurnoResumenTripulacionDTO(
                Integer idTurnoTripulacion, Integer idTurno, String tipoServicio,
                Timestamp horaInicio, Timestamp horaFin, long minutosDuracion, String duracionTexto,
                String estadoRespuesta, String estadoRespuestaSeveridad, Timestamp fechaRespuesta,
                String motivoRechazo, boolean esPendiente, boolean esConfirmado, boolean esRechazado,
                boolean esFinalizado, boolean esProximo, String ventanaOperativa,
                Integer idVuelo, String numeroVuelo, Timestamp vueloSalida, Timestamp vueloLlegada,
                String vueloEstadoDesc, String vueloEstadoSeveridad, boolean vueloCancelado,
                String aerolineaNombre, String origenIata, String origenCiudad, String origenAeropuerto,
                String destinoIata, String destinoCiudad, String destinoAeropuerto, int cantidadTramos,
                String puertaOrigenCodigo, String terminalOrigen,
                String pilotoNombre, String pilotoLicencia, String pilotoTelefono,
                Integer idAvion, String avionMatricula, String avionFabricante, String avionModelo,
                int avionCapacidadPax, String avionEstadoMantenimiento,
                int pasajerosReservados, int pasajerosCheckin, int pasajerosSalidaEmergencia,
                double ocupacionPct, double checkinPct, int piezasEquipaje, double pesoEquipajeKg,
                int tripulacionTotal, int tripulacionConfirmada
        ) {
            this.idTurnoTripulacion = idTurnoTripulacion;
            this.idTurno = idTurno;
            this.tipoServicio = tipoServicio;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.minutosDuracion = minutosDuracion;
            this.duracionTexto = duracionTexto;
            this.estadoRespuesta = estadoRespuesta;
            this.estadoRespuestaSeveridad = estadoRespuestaSeveridad;
            this.fechaRespuesta = fechaRespuesta;
            this.motivoRechazo = motivoRechazo;
            this.esPendiente = esPendiente;
            this.esConfirmado = esConfirmado;
            this.esRechazado = esRechazado;
            this.esFinalizado = esFinalizado;
            this.esProximo = esProximo;
            this.ventanaOperativa = ventanaOperativa;
            this.idVuelo = idVuelo;
            this.numeroVuelo = numeroVuelo;
            this.vueloSalida = vueloSalida;
            this.vueloLlegada = vueloLlegada;
            this.vueloEstadoDesc = vueloEstadoDesc;
            this.vueloEstadoSeveridad = vueloEstadoSeveridad;
            this.vueloCancelado = vueloCancelado;
            this.aerolineaNombre = aerolineaNombre;
            this.origenIata = origenIata;
            this.origenCiudad = origenCiudad;
            this.origenAeropuerto = origenAeropuerto;
            this.destinoIata = destinoIata;
            this.destinoCiudad = destinoCiudad;
            this.destinoAeropuerto = destinoAeropuerto;
            this.cantidadTramos = cantidadTramos;
            this.puertaOrigenCodigo = puertaOrigenCodigo;
            this.terminalOrigen = terminalOrigen;
            this.pilotoNombre = pilotoNombre;
            this.pilotoLicencia = pilotoLicencia;
            this.pilotoTelefono = pilotoTelefono;
            this.idAvion = idAvion;
            this.avionMatricula = avionMatricula;
            this.avionFabricante = avionFabricante;
            this.avionModelo = avionModelo;
            this.avionCapacidadPax = avionCapacidadPax;
            this.avionEstadoMantenimiento = avionEstadoMantenimiento;
            this.pasajerosReservados = pasajerosReservados;
            this.pasajerosCheckin = pasajerosCheckin;
            this.pasajerosSalidaEmergencia = pasajerosSalidaEmergencia;
            this.ocupacionPct = ocupacionPct;
            this.checkinPct = checkinPct;
            this.piezasEquipaje = piezasEquipaje;
            this.pesoEquipajeKg = pesoEquipajeKg;
            this.tripulacionTotal = tripulacionTotal;
            this.tripulacionConfirmada = tripulacionConfirmada;
            this.intervaloPrevio = FatigaDescansoHelper.intervaloSinReferencia(minutosDuracion);
        }

        public IntervaloServicioInfo getIntervaloPrevio() { return intervaloPrevio; }
        public void setIntervaloPrevio(IntervaloServicioInfo intervaloPrevio) {
            if (intervaloPrevio != null) {
                this.intervaloPrevio = intervaloPrevio;
            }
        }

        public Integer getIdTurnoTripulacion() { return idTurnoTripulacion; }
        public Integer getIdTurno() { return idTurno; }
        public String getTipoServicio() { return tipoServicio; }
        public Timestamp getHoraInicio() { return horaInicio; }
        public Timestamp getHoraFin() { return horaFin; }
        public long getMinutosDuracion() { return minutosDuracion; }
        public String getDuracionTexto() { return duracionTexto; }
        public String getEstadoRespuesta() { return estadoRespuesta; }
        public String getEstadoRespuestaSeveridad() { return estadoRespuestaSeveridad; }
        public Timestamp getFechaRespuesta() { return fechaRespuesta; }
        public String getMotivoRechazo() { return motivoRechazo; }
        public boolean isEsPendiente() { return esPendiente; }
        public boolean isEsConfirmado() { return esConfirmado; }
        public boolean isEsRechazado() { return esRechazado; }
        public boolean isEsFinalizado() { return esFinalizado; }
        public boolean isEsProximo() { return esProximo; }
        public String getVentanaOperativa() { return ventanaOperativa; }
        public Integer getIdVuelo() { return idVuelo; }
        public String getNumeroVuelo() { return numeroVuelo; }
        public Timestamp getVueloSalida() { return vueloSalida; }
        public Timestamp getVueloLlegada() { return vueloLlegada; }
        public String getVueloEstadoDesc() { return vueloEstadoDesc; }
        public String getVueloEstadoSeveridad() { return vueloEstadoSeveridad; }
        public boolean isVueloCancelado() { return vueloCancelado; }
        public String getAerolineaNombre() { return aerolineaNombre; }
        public String getOrigenIata() { return origenIata; }
        public String getOrigenCiudad() { return origenCiudad; }
        public String getOrigenAeropuerto() { return origenAeropuerto; }
        public String getDestinoIata() { return destinoIata; }
        public String getDestinoCiudad() { return destinoCiudad; }
        public String getDestinoAeropuerto() { return destinoAeropuerto; }
        public int getCantidadTramos() { return cantidadTramos; }
        public String getPuertaOrigenCodigo() { return puertaOrigenCodigo; }
        public String getTerminalOrigen() { return terminalOrigen; }
        public String getPilotoNombre() { return pilotoNombre; }
        public String getPilotoLicencia() { return pilotoLicencia; }
        public String getPilotoTelefono() { return pilotoTelefono; }
        public Integer getIdAvion() { return idAvion; }
        public String getAvionMatricula() { return avionMatricula; }
        public String getAvionFabricante() { return avionFabricante; }
        public String getAvionModelo() { return avionModelo; }
        public int getAvionCapacidadPax() { return avionCapacidadPax; }
        public String getAvionEstadoMantenimiento() { return avionEstadoMantenimiento; }
        public int getPasajerosReservados() { return pasajerosReservados; }
        public int getPasajerosCheckin() { return pasajerosCheckin; }
        public int getPasajerosSalidaEmergencia() { return pasajerosSalidaEmergencia; }
        public double getOcupacionPct() { return ocupacionPct; }
        public double getCheckinPct() { return checkinPct; }
        public int getPiezasEquipaje() { return piezasEquipaje; }
        public double getPesoEquipajeKg() { return pesoEquipajeKg; }
        public int getTripulacionTotal() { return tripulacionTotal; }
        public int getTripulacionConfirmada() { return tripulacionConfirmada; }

        public String getModeloCompleto() {
            if (avionFabricante != null && !avionFabricante.isBlank()
                    && avionModelo != null && !avionModelo.toLowerCase().startsWith(avionFabricante.toLowerCase())) {
                return avionFabricante + " " + avionModelo;
            }
            return avionModelo != null ? avionModelo : "Por asignar";
        }

        public String getPuertaTerminalTexto() {
            if (puertaOrigenCodigo == null || puertaOrigenCodigo.isBlank()) {
                return "Sin asignar";
            }
            if (terminalOrigen != null && !terminalOrigen.isBlank()) {
                return "Puerta " + puertaOrigenCodigo + " (" + terminalOrigen + ")";
            }
            return "Puerta " + puertaOrigenCodigo;
        }
    }

    public static final class SegmentoTripulacionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int ordenSegmento;
        private final String origenIata;
        private final String origenAeropuerto;
        private final String origenCiudad;
        private final String destinoIata;
        private final String destinoAeropuerto;
        private final String destinoCiudad;
        private final Timestamp horaSalida;
        private final Timestamp horaLlegada;
        private final String duracionTexto;
        private final String codigoPuerta;
        private final String terminal;

        public SegmentoTripulacionDTO(
                int ordenSegmento, String origenIata, String origenAeropuerto, String origenCiudad,
                String destinoIata, String destinoAeropuerto, String destinoCiudad,
                Timestamp horaSalida, Timestamp horaLlegada, String duracionTexto,
                String codigoPuerta, String terminal
        ) {
            this.ordenSegmento = ordenSegmento;
            this.origenIata = origenIata;
            this.origenAeropuerto = origenAeropuerto;
            this.origenCiudad = origenCiudad;
            this.destinoIata = destinoIata;
            this.destinoAeropuerto = destinoAeropuerto;
            this.destinoCiudad = destinoCiudad;
            this.horaSalida = horaSalida;
            this.horaLlegada = horaLlegada;
            this.duracionTexto = duracionTexto;
            this.codigoPuerta = codigoPuerta;
            this.terminal = terminal;
        }

        public int getOrdenSegmento() { return ordenSegmento; }
        public String getOrigenIata() { return origenIata; }
        public String getOrigenAeropuerto() { return origenAeropuerto; }
        public String getOrigenCiudad() { return origenCiudad; }
        public String getDestinoIata() { return destinoIata; }
        public String getDestinoAeropuerto() { return destinoAeropuerto; }
        public String getDestinoCiudad() { return destinoCiudad; }
        public Timestamp getHoraSalida() { return horaSalida; }
        public Timestamp getHoraLlegada() { return horaLlegada; }
        public String getDuracionTexto() { return duracionTexto; }
        public String getCodigoPuerta() { return codigoPuerta; }
        public String getTerminal() { return terminal; }
    }

    public static final class CompaneroTripulacionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String rut;
        private final String nombreCompleto;
        private final String cargo;
        private final String tipoTurno;
        private final String estadoConfirmacion;
        private final String telefono;
        private final String correoElectronico;
        private final boolean esUsuarioActual;

        public CompaneroTripulacionDTO(
                String rut, String nombreCompleto, String cargo, String tipoTurno,
                String estadoConfirmacion, String telefono, String correoElectronico, boolean esUsuarioActual
        ) {
            this.rut = rut;
            this.nombreCompleto = nombreCompleto;
            this.cargo = cargo;
            this.tipoTurno = tipoTurno;
            this.estadoConfirmacion = estadoConfirmacion;
            this.telefono = telefono;
            this.correoElectronico = correoElectronico;
            this.esUsuarioActual = esUsuarioActual;
        }

        public String getRut() { return rut; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getCargo() { return cargo; }
        public String getTipoTurno() { return tipoTurno; }
        public String getEstadoConfirmacion() { return estadoConfirmacion; }
        public String getTelefono() { return telefono; }
        public String getCorreoElectronico() { return correoElectronico; }
        public boolean isEsUsuarioActual() { return esUsuarioActual; }
    }

    public static final class CabinaClaseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String claseNombre;
        private final int capacidadClase;
        private final int asientosOcupados;
        private final int checkinsCompletados;
        private final double ocupacionPct;

        public CabinaClaseDTO(
                String claseNombre, int capacidadClase, int asientosOcupados,
                int checkinsCompletados, double ocupacionPct
        ) {
            this.claseNombre = claseNombre;
            this.capacidadClase = capacidadClase;
            this.asientosOcupados = asientosOcupados;
            this.checkinsCompletados = checkinsCompletados;
            this.ocupacionPct = ocupacionPct;
        }

        public String getClaseNombre() { return claseNombre; }
        public int getCapacidadClase() { return capacidadClase; }
        public int getAsientosOcupados() { return asientosOcupados; }
        public int getCheckinsCompletados() { return checkinsCompletados; }
        public double getOcupacionPct() { return ocupacionPct; }
    }

    public static final class EquipajeCabinaDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String tipoNombre;
        private final int piezas;
        private final double pesoTotalKg;

        public EquipajeCabinaDTO(String tipoNombre, int piezas, double pesoTotalKg) {
            this.tipoNombre = tipoNombre;
            this.piezas = piezas;
            this.pesoTotalKg = pesoTotalKg;
        }

        public String getTipoNombre() { return tipoNombre; }
        public int getPiezas() { return piezas; }
        public double getPesoTotalKg() { return pesoTotalKg; }
    }

    public static final class ManifiestoCabinaDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String nombreCompleto;
        private final String rut;
        private final String numeroAsiento;
        private final String claseAsiento;
        private final boolean salidaEmergencia;
        private final String codigoReserva;
        private final String checkinEstado;
        private final String checkinMetodo;
        private final int piezasEquipaje;
        private final double pesoEquipajeKg;

        public ManifiestoCabinaDTO(
                String nombreCompleto, String rut, String numeroAsiento, String claseAsiento,
                boolean salidaEmergencia, String codigoReserva, String checkinEstado,
                String checkinMetodo, int piezasEquipaje, double pesoEquipajeKg
        ) {
            this.nombreCompleto = nombreCompleto;
            this.rut = rut;
            this.numeroAsiento = numeroAsiento;
            this.claseAsiento = claseAsiento;
            this.salidaEmergencia = salidaEmergencia;
            this.codigoReserva = codigoReserva;
            this.checkinEstado = checkinEstado;
            this.checkinMetodo = checkinMetodo;
            this.piezasEquipaje = piezasEquipaje;
            this.pesoEquipajeKg = pesoEquipajeKg;
        }

        public String getNombreCompleto() { return nombreCompleto; }
        public String getRut() { return rut; }
        public String getNumeroAsiento() { return numeroAsiento; }
        public String getClaseAsiento() { return claseAsiento; }
        public boolean isSalidaEmergencia() { return salidaEmergencia; }
        public String getCodigoReserva() { return codigoReserva; }
        public String getCheckinEstado() { return checkinEstado; }
        public String getCheckinMetodo() { return checkinMetodo; }
        public int getPiezasEquipaje() { return piezasEquipaje; }
        public double getPesoEquipajeKg() { return pesoEquipajeKg; }
    }

    public static final class NotificacionTripulacionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Integer idNotificacion;
        private final String titulo;
        private final String mensaje;
        private final Timestamp fecha;
        private final boolean leido;
        private final String canal;

        public NotificacionTripulacionDTO(
                Integer idNotificacion, String titulo, String mensaje,
                Timestamp fecha, boolean leido, String canal
        ) {
            this.idNotificacion = idNotificacion;
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.fecha = fecha;
            this.leido = leido;
            this.canal = canal;
        }

        public Integer getIdNotificacion() { return idNotificacion; }
        public String getTitulo() { return titulo; }
        public String getMensaje() { return mensaje; }
        public Timestamp getFecha() { return fecha; }
        public boolean isLeido() { return leido; }
        public String getCanal() { return canal; }
    }

    public static final class DiaAgendaTripulacionDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String isoDate;
        private final String diaCorto;
        private final String fechaCorta;
        private final boolean esHoy;
        private final int cantidadTurnos;
        private final String horasTexto;
        private final List<TurnoResumenTripulacionDTO> turnos;

        public DiaAgendaTripulacionDTO(
                String isoDate, String diaCorto, String fechaCorta,
                boolean esHoy, int cantidadTurnos, String horasTexto,
                List<TurnoResumenTripulacionDTO> turnos
        ) {
            this.isoDate = isoDate;
            this.diaCorto = diaCorto;
            this.fechaCorta = fechaCorta;
            this.esHoy = esHoy;
            this.cantidadTurnos = cantidadTurnos;
            this.horasTexto = horasTexto;
            this.turnos = turnos;
        }

        public String getIsoDate() { return isoDate; }
        public String getDiaCorto() { return diaCorto; }
        public String getFechaCorta() { return fechaCorta; }
        public boolean isEsHoy() { return esHoy; }
        public int getCantidadTurnos() { return cantidadTurnos; }
        public String getHorasTexto() { return horasTexto; }
        public List<TurnoResumenTripulacionDTO> getTurnos() { return turnos; }
    }

    public static final class BriefingTurnoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final TurnoResumenTripulacionDTO resumen;
        private final List<SegmentoTripulacionDTO> tramos;
        private final List<CabinaClaseDTO> cabinaPorClase;
        private final List<EquipajeCabinaDTO> equipajePorTipo;
        private final List<CompaneroTripulacionDTO> companeros;
        private final List<ManifiestoCabinaDTO> manifiestoCabina;

        public BriefingTurnoDTO(
                TurnoResumenTripulacionDTO resumen, List<SegmentoTripulacionDTO> tramos,
                List<CabinaClaseDTO> cabinaPorClase, List<EquipajeCabinaDTO> equipajePorTipo,
                List<CompaneroTripulacionDTO> companeros, List<ManifiestoCabinaDTO> manifiestoCabina
        ) {
            this.resumen = resumen;
            this.tramos = tramos;
            this.cabinaPorClase = cabinaPorClase;
            this.equipajePorTipo = equipajePorTipo;
            this.companeros = companeros;
            this.manifiestoCabina = manifiestoCabina;
        }

        public TurnoResumenTripulacionDTO getResumen() { return resumen; }
        public List<SegmentoTripulacionDTO> getTramos() { return tramos; }
        public List<CabinaClaseDTO> getCabinaPorClase() { return cabinaPorClase; }
        public List<EquipajeCabinaDTO> getEquipajePorTipo() { return equipajePorTipo; }
        public List<CompaneroTripulacionDTO> getCompaneros() { return companeros; }
        public List<ManifiestoCabinaDTO> getManifiestoCabina() { return manifiestoCabina; }
    }

    public static final class PanelTripulacionDataDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final KpisTripulacionDTO kpis;
        private final TurnoResumenTripulacionDTO proximoTurno;
        private final List<TurnoResumenTripulacionDTO> turnos;
        private final List<NotificacionTripulacionDTO> notificaciones;
        private final ResumenFatigaDTO fatiga;

        public PanelTripulacionDataDTO(
                KpisTripulacionDTO kpis, TurnoResumenTripulacionDTO proximoTurno,
                List<TurnoResumenTripulacionDTO> turnos, List<NotificacionTripulacionDTO> notificaciones,
                ResumenFatigaDTO fatiga
        ) {
            this.kpis = kpis;
            this.proximoTurno = proximoTurno;
            this.turnos = turnos;
            this.notificaciones = notificaciones;
            this.fatiga = fatiga;
        }

        public KpisTripulacionDTO getKpis() { return kpis; }
        public TurnoResumenTripulacionDTO getProximoTurno() { return proximoTurno; }
        public List<TurnoResumenTripulacionDTO> getTurnos() { return turnos; }
        public List<NotificacionTripulacionDTO> getNotificaciones() { return notificaciones; }
        public ResumenFatigaDTO getFatiga() { return fatiga; }
    }
}
