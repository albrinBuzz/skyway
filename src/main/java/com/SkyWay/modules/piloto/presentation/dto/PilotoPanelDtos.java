package com.SkyWay.modules.piloto.presentation.dto;

import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.IntervaloServicioInfo;
import com.SkyWay.modules.turno.domain.service.FatigaDescansoHelper.ResumenFatigaDTO;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * DTOs inmutables y serializables compatibles con Jakarta EL (JSF 4 / Tomcat 10.1)
 * para el Panel Operativo del Piloto, respaldados por consultas reales en PostgreSQL V3.
 */
public final class PilotoPanelDtos {

    private PilotoPanelDtos() {
    }

    public static final class KpisPilotoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final long minutosCompletados;
        private final long minutosProgramados;
        private final String horasCompletadasTexto;
        private final String horasProgramadasTexto;
        private final long horasTotalesEnteras;
        private final int totalVuelos;
        private final int vuelosProximos;
        private final int vuelosCompletados;
        private final int vuelosCancelados;
        private final long totalPasajerosReservados;
        private final long totalPasajerosCheckin;
        private final long totalCapacidadOfertada;
        private final double factorOcupacionRealPct;
        private final double porcentajeCheckinReal;
        private final String modeloAvionPrincipal;
        private final int aeronavesDistintas;
        private final int aeropuertosVisitados;
        private final int alertasMantenimiento;

        public KpisPilotoDTO(
                long minutosCompletados, long minutosProgramados,
                String horasCompletadasTexto, String horasProgramadasTexto,
                long horasTotalesEnteras, int totalVuelos, int vuelosProximos,
                int vuelosCompletados, int vuelosCancelados, long totalPasajerosReservados,
                long totalPasajerosCheckin, long totalCapacidadOfertada,
                double factorOcupacionRealPct, double porcentajeCheckinReal,
                String modeloAvionPrincipal, int aeronavesDistintas,
                int aeropuertosVisitados, int alertasMantenimiento
        ) {
            this.minutosCompletados = minutosCompletados;
            this.minutosProgramados = minutosProgramados;
            this.horasCompletadasTexto = horasCompletadasTexto;
            this.horasProgramadasTexto = horasProgramadasTexto;
            this.horasTotalesEnteras = horasTotalesEnteras;
            this.totalVuelos = totalVuelos;
            this.vuelosProximos = vuelosProximos;
            this.vuelosCompletados = vuelosCompletados;
            this.vuelosCancelados = vuelosCancelados;
            this.totalPasajerosReservados = totalPasajerosReservados;
            this.totalPasajerosCheckin = totalPasajerosCheckin;
            this.totalCapacidadOfertada = totalCapacidadOfertada;
            this.factorOcupacionRealPct = factorOcupacionRealPct;
            this.porcentajeCheckinReal = porcentajeCheckinReal;
            this.modeloAvionPrincipal = modeloAvionPrincipal;
            this.aeronavesDistintas = aeronavesDistintas;
            this.aeropuertosVisitados = aeropuertosVisitados;
            this.alertasMantenimiento = alertasMantenimiento;
        }

        public long getMinutosCompletados() { return minutosCompletados; }
        public long getMinutosProgramados() { return minutosProgramados; }
        public String getHorasCompletadasTexto() { return horasCompletadasTexto; }
        public String getHorasProgramadasTexto() { return horasProgramadasTexto; }
        public long getHorasTotalesEnteras() { return horasTotalesEnteras; }
        public int getTotalVuelos() { return totalVuelos; }
        public int getVuelosProximos() { return vuelosProximos; }
        public int getVuelosCompletados() { return vuelosCompletados; }
        public int getVuelosCancelados() { return vuelosCancelados; }
        public long getTotalPasajerosReservados() { return totalPasajerosReservados; }
        public long getTotalPasajerosCheckin() { return totalPasajerosCheckin; }
        public long getTotalCapacidadOfertada() { return totalCapacidadOfertada; }
        public double getFactorOcupacionRealPct() { return factorOcupacionRealPct; }
        public double getPorcentajeCheckinReal() { return porcentajeCheckinReal; }
        public String getModeloAvionPrincipal() { return modeloAvionPrincipal; }
        public int getAeronavesDistintas() { return aeronavesDistintas; }
        public int getAeropuertosVisitados() { return aeropuertosVisitados; }
        public int getAlertasMantenimiento() { return alertasMantenimiento; }
    }

    public static final class VueloResumenPilotoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Integer idVuelo;
        private final String numeroVuelo;
        private final Timestamp fechaHoraSalida;
        private final Timestamp fechaHoraLlegada;
        private final long minutosDuracion;
        private final String duracionTexto;
        private final String estadoDescripcion;
        private final String estadoCodigo;
        private final String estadoSeveridad;
        private final boolean esCancelado;
        private final boolean esFinalizado;
        private final boolean esProximo;
        private final String ventanaOperativa;
        private final String aerolineaNombre;
        private final String aerolineaCodigo;
        private final String origenIata;
        private final String origenCiudad;
        private final String origenAeropuerto;
        private final String origenPais;
        private final String destinoIata;
        private final String destinoCiudad;
        private final String destinoAeropuerto;
        private final String destinoPais;
        private final int cantidadTramos;
        private final double distanciaTotalKm;
        private final String puertaOrigenCodigo;
        private final String terminalOrigen;
        private final Integer idAvion;
        private final String avionMatricula;
        private final String avionFabricante;
        private final String avionModelo;
        private final Integer avionAnoFabricacion;
        private final int avionCapacidadPax;
        private final int avionCapacidadCargaKg;
        private final String avionEstadoMantenimiento;
        private final Timestamp avionFechaProximoMant;
        private final boolean alertaMantenimiento;
        private final int pasajerosReservados;
        private final int pasajerosCheckin;
        private final double ocupacionPct;
        private final double checkinPct;
        private final int piezasEquipaje;
        private final double pesoEquipajeKg;
        private final double cargaEquipajePct;
        private final int tripulacionTotal;
        private final int tripulacionConfirmada;
        private final int tripulacionPendiente;
        private final int tripulacionRechazada;
        private IntervaloServicioInfo intervaloPrevio;

        public VueloResumenPilotoDTO(
                Integer idVuelo, String numeroVuelo, Timestamp fechaHoraSalida, Timestamp fechaHoraLlegada,
                long minutosDuracion, String duracionTexto, String estadoDescripcion, String estadoCodigo,
                String estadoSeveridad, boolean esCancelado, boolean esFinalizado, boolean esProximo,
                String ventanaOperativa, String aerolineaNombre, String aerolineaCodigo,
                String origenIata, String origenCiudad, String origenAeropuerto, String origenPais,
                String destinoIata, String destinoCiudad, String destinoAeropuerto, String destinoPais,
                int cantidadTramos, double distanciaTotalKm, String puertaOrigenCodigo, String terminalOrigen,
                Integer idAvion, String avionMatricula, String avionFabricante, String avionModelo,
                Integer avionAnoFabricacion, int avionCapacidadPax, int avionCapacidadCargaKg,
                String avionEstadoMantenimiento, Timestamp avionFechaProximoMant, boolean alertaMantenimiento,
                int pasajerosReservados, int pasajerosCheckin, double ocupacionPct, double checkinPct,
                int piezasEquipaje, double pesoEquipajeKg, double cargaEquipajePct,
                int tripulacionTotal, int tripulacionConfirmada, int tripulacionPendiente, int tripulacionRechazada
        ) {
            this.idVuelo = idVuelo;
            this.numeroVuelo = numeroVuelo;
            this.fechaHoraSalida = fechaHoraSalida;
            this.fechaHoraLlegada = fechaHoraLlegada;
            this.minutosDuracion = minutosDuracion;
            this.duracionTexto = duracionTexto;
            this.estadoDescripcion = estadoDescripcion;
            this.estadoCodigo = estadoCodigo;
            this.estadoSeveridad = estadoSeveridad;
            this.esCancelado = esCancelado;
            this.esFinalizado = esFinalizado;
            this.esProximo = esProximo;
            this.ventanaOperativa = ventanaOperativa;
            this.aerolineaNombre = aerolineaNombre;
            this.aerolineaCodigo = aerolineaCodigo;
            this.origenIata = origenIata;
            this.origenCiudad = origenCiudad;
            this.origenAeropuerto = origenAeropuerto;
            this.origenPais = origenPais;
            this.destinoIata = destinoIata;
            this.destinoCiudad = destinoCiudad;
            this.destinoAeropuerto = destinoAeropuerto;
            this.destinoPais = destinoPais;
            this.cantidadTramos = cantidadTramos;
            this.distanciaTotalKm = distanciaTotalKm;
            this.puertaOrigenCodigo = puertaOrigenCodigo;
            this.terminalOrigen = terminalOrigen;
            this.idAvion = idAvion;
            this.avionMatricula = avionMatricula;
            this.avionFabricante = avionFabricante;
            this.avionModelo = avionModelo;
            this.avionAnoFabricacion = avionAnoFabricacion;
            this.avionCapacidadPax = avionCapacidadPax;
            this.avionCapacidadCargaKg = avionCapacidadCargaKg;
            this.avionEstadoMantenimiento = avionEstadoMantenimiento;
            this.avionFechaProximoMant = avionFechaProximoMant;
            this.alertaMantenimiento = alertaMantenimiento;
            this.pasajerosReservados = pasajerosReservados;
            this.pasajerosCheckin = pasajerosCheckin;
            this.ocupacionPct = ocupacionPct;
            this.checkinPct = checkinPct;
            this.piezasEquipaje = piezasEquipaje;
            this.pesoEquipajeKg = pesoEquipajeKg;
            this.cargaEquipajePct = cargaEquipajePct;
            this.tripulacionTotal = tripulacionTotal;
            this.tripulacionConfirmada = tripulacionConfirmada;
            this.tripulacionPendiente = tripulacionPendiente;
            this.tripulacionRechazada = tripulacionRechazada;
            this.intervaloPrevio = FatigaDescansoHelper.intervaloSinReferencia(minutosDuracion);
        }

        public IntervaloServicioInfo getIntervaloPrevio() { return intervaloPrevio; }
        public void setIntervaloPrevio(IntervaloServicioInfo intervaloPrevio) {
            if (intervaloPrevio != null) {
                this.intervaloPrevio = intervaloPrevio;
            }
        }

        public Integer getIdVuelo() { return idVuelo; }
        public String getNumeroVuelo() { return numeroVuelo; }
        public Timestamp getFechaHoraSalida() { return fechaHoraSalida; }
        public Timestamp getFechaHoraLlegada() { return fechaHoraLlegada; }
        public long getMinutosDuracion() { return minutosDuracion; }
        public String getDuracionTexto() { return duracionTexto; }
        public String getEstadoDescripcion() { return estadoDescripcion; }
        public String getEstadoCodigo() { return estadoCodigo; }
        public String getEstadoSeveridad() { return estadoSeveridad; }
        public boolean isEsCancelado() { return esCancelado; }
        public boolean isEsFinalizado() { return esFinalizado; }
        public boolean isEsProximo() { return esProximo; }
        public String getVentanaOperativa() { return ventanaOperativa; }
        public String getAerolineaNombre() { return aerolineaNombre; }
        public String getAerolineaCodigo() { return aerolineaCodigo; }
        public String getOrigenIata() { return origenIata; }
        public String getOrigenCiudad() { return origenCiudad; }
        public String getOrigenAeropuerto() { return origenAeropuerto; }
        public String getOrigenPais() { return origenPais; }
        public String getDestinoIata() { return destinoIata; }
        public String getDestinoCiudad() { return destinoCiudad; }
        public String getDestinoAeropuerto() { return destinoAeropuerto; }
        public String getDestinoPais() { return destinoPais; }
        public int getCantidadTramos() { return cantidadTramos; }
        public double getDistanciaTotalKm() { return distanciaTotalKm; }
        public String getPuertaOrigenCodigo() { return puertaOrigenCodigo; }
        public String getTerminalOrigen() { return terminalOrigen; }
        public Integer getIdAvion() { return idAvion; }
        public String getAvionMatricula() { return avionMatricula; }
        public String getAvionFabricante() { return avionFabricante; }
        public String getAvionModelo() { return avionModelo; }
        public Integer getAvionAnoFabricacion() { return avionAnoFabricacion; }
        public int getAvionCapacidadPax() { return avionCapacidadPax; }
        public int getAvionCapacidadCargaKg() { return avionCapacidadCargaKg; }
        public String getAvionEstadoMantenimiento() { return avionEstadoMantenimiento; }
        public Timestamp getAvionFechaProximoMant() { return avionFechaProximoMant; }
        public boolean isAlertaMantenimiento() { return alertaMantenimiento; }
        public int getPasajerosReservados() { return pasajerosReservados; }
        public int getPasajerosCheckin() { return pasajerosCheckin; }
        public double getOcupacionPct() { return ocupacionPct; }
        public double getCheckinPct() { return checkinPct; }
        public int getPiezasEquipaje() { return piezasEquipaje; }
        public double getPesoEquipajeKg() { return pesoEquipajeKg; }
        public double getCargaEquipajePct() { return cargaEquipajePct; }
        public int getTripulacionTotal() { return tripulacionTotal; }
        public int getTripulacionConfirmada() { return tripulacionConfirmada; }
        public int getTripulacionPendiente() { return tripulacionPendiente; }
        public int getTripulacionRechazada() { return tripulacionRechazada; }

        public int getEscalas() {
            return Math.max(0, cantidadTramos - 1);
        }

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

    public static final class SegmentoBriefingDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int ordenSegmento;
        private final String origenIata;
        private final String origenAeropuerto;
        private final String origenCiudad;
        private final String origenPais;
        private final String destinoIata;
        private final String destinoAeropuerto;
        private final String destinoCiudad;
        private final String destinoPais;
        private final Timestamp horaSalida;
        private final Timestamp horaLlegada;
        private final String duracionTexto;
        private final double distanciaKm;
        private final String codigoPuerta;
        private final String terminal;
        private final Timestamp horaAsignacionPuerta;

        public SegmentoBriefingDTO(
                int ordenSegmento, String origenIata, String origenAeropuerto, String origenCiudad, String origenPais,
                String destinoIata, String destinoAeropuerto, String destinoCiudad, String destinoPais,
                Timestamp horaSalida, Timestamp horaLlegada, String duracionTexto, double distanciaKm,
                String codigoPuerta, String terminal, Timestamp horaAsignacionPuerta
        ) {
            this.ordenSegmento = ordenSegmento;
            this.origenIata = origenIata;
            this.origenAeropuerto = origenAeropuerto;
            this.origenCiudad = origenCiudad;
            this.origenPais = origenPais;
            this.destinoIata = destinoIata;
            this.destinoAeropuerto = destinoAeropuerto;
            this.destinoCiudad = destinoCiudad;
            this.destinoPais = destinoPais;
            this.horaSalida = horaSalida;
            this.horaLlegada = horaLlegada;
            this.duracionTexto = duracionTexto;
            this.distanciaKm = distanciaKm;
            this.codigoPuerta = codigoPuerta;
            this.terminal = terminal;
            this.horaAsignacionPuerta = horaAsignacionPuerta;
        }

        public int getOrdenSegmento() { return ordenSegmento; }
        public String getOrigenIata() { return origenIata; }
        public String getOrigenAeropuerto() { return origenAeropuerto; }
        public String getOrigenCiudad() { return origenCiudad; }
        public String getOrigenPais() { return origenPais; }
        public String getDestinoIata() { return destinoIata; }
        public String getDestinoAeropuerto() { return destinoAeropuerto; }
        public String getDestinoCiudad() { return destinoCiudad; }
        public String getDestinoPais() { return destinoPais; }
        public Timestamp getHoraSalida() { return horaSalida; }
        public Timestamp getHoraLlegada() { return horaLlegada; }
        public String getDuracionTexto() { return duracionTexto; }
        public double getDistanciaKm() { return distanciaKm; }
        public String getCodigoPuerta() { return codigoPuerta; }
        public String getTerminal() { return terminal; }
        public Timestamp getHoraAsignacionPuerta() { return horaAsignacionPuerta; }
    }

    public static final class TripulanteBriefingDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String rut;
        private final String nombreCompleto;
        private final String cargo;
        private final String tipoTurno;
        private final Timestamp horaInicio;
        private final Timestamp horaFin;
        private final String estadoConfirmacion;
        private final Timestamp fechaRespuesta;
        private final String motivoRechazo;
        private final String telefono;
        private final String correoElectronico;

        public TripulanteBriefingDTO(
                String rut, String nombreCompleto, String cargo, String tipoTurno,
                Timestamp horaInicio, Timestamp horaFin, String estadoConfirmacion,
                Timestamp fechaRespuesta, String motivoRechazo, String telefono, String correoElectronico
        ) {
            this.rut = rut;
            this.nombreCompleto = nombreCompleto;
            this.cargo = cargo;
            this.tipoTurno = tipoTurno;
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.estadoConfirmacion = estadoConfirmacion;
            this.fechaRespuesta = fechaRespuesta;
            this.motivoRechazo = motivoRechazo;
            this.telefono = telefono;
            this.correoElectronico = correoElectronico;
        }

        public String getRut() { return rut; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getCargo() { return cargo; }
        public String getTipoTurno() { return tipoTurno; }
        public Timestamp getHoraInicio() { return horaInicio; }
        public Timestamp getHoraFin() { return horaFin; }
        public String getEstadoConfirmacion() { return estadoConfirmacion; }
        public Timestamp getFechaRespuesta() { return fechaRespuesta; }
        public String getMotivoRechazo() { return motivoRechazo; }
        public String getTelefono() { return telefono; }
        public String getCorreoElectronico() { return correoElectronico; }
    }

    public static final class OcupacionClaseDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String claseNombre;
        private final int capacidadClase;
        private final int asientosOcupados;
        private final int checkinsCompletados;
        private final double ocupacionPct;

        public OcupacionClaseDTO(
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

    public static final class EquipajeResumenDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String tipoNombre;
        private final int piezas;
        private final double pesoTotalKg;

        public EquipajeResumenDTO(String tipoNombre, int piezas, double pesoTotalKg) {
            this.tipoNombre = tipoNombre;
            this.piezas = piezas;
            this.pesoTotalKg = pesoTotalKg;
        }

        public String getTipoNombre() { return tipoNombre; }
        public int getPiezas() { return piezas; }
        public double getPesoTotalKg() { return pesoTotalKg; }
    }

    public static final class ManifiestoPasajeroDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String rut;
        private final String nombreCompleto;
        private final String codigoReserva;
        private final String estadoReserva;
        private final String numeroAsiento;
        private final String claseAsiento;
        private final String checkinEstado;
        private final String checkinMetodo;
        private final String codigoBoardingPass;
        private final int piezasEquipaje;
        private final double pesoEquipajeKg;

        public ManifiestoPasajeroDTO(
                String rut, String nombreCompleto, String codigoReserva, String estadoReserva,
                String numeroAsiento, String claseAsiento, String checkinEstado,
                String checkinMetodo, String codigoBoardingPass, int piezasEquipaje, double pesoEquipajeKg
        ) {
            this.rut = rut;
            this.nombreCompleto = nombreCompleto;
            this.codigoReserva = codigoReserva;
            this.estadoReserva = estadoReserva;
            this.numeroAsiento = numeroAsiento;
            this.claseAsiento = claseAsiento;
            this.checkinEstado = checkinEstado;
            this.checkinMetodo = checkinMetodo;
            this.codigoBoardingPass = codigoBoardingPass;
            this.piezasEquipaje = piezasEquipaje;
            this.pesoEquipajeKg = pesoEquipajeKg;
        }

        public String getRut() { return rut; }
        public String getNombreCompleto() { return nombreCompleto; }
        public String getCodigoReserva() { return codigoReserva; }
        public String getEstadoReserva() { return estadoReserva; }
        public String getNumeroAsiento() { return numeroAsiento; }
        public String getClaseAsiento() { return claseAsiento; }
        public String getCheckinEstado() { return checkinEstado; }
        public String getCheckinMetodo() { return checkinMetodo; }
        public String getCodigoBoardingPass() { return codigoBoardingPass; }
        public int getPiezasEquipaje() { return piezasEquipaje; }
        public double getPesoEquipajeKg() { return pesoEquipajeKg; }
    }

    public static final class FlotaPilotoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Integer idAvion;
        private final String matricula;
        private final String fabricante;
        private final String modelo;
        private final Integer anoFabricacion;
        private final int capacidadPax;
        private final int capacidadCargaKg;
        private final String estadoMantenimiento;
        private final Timestamp fechaProximoMantenimiento;
        private final boolean alertaMantenimiento;
        private final int totalVuelos;
        private final String horasAsignadasTexto;

        public FlotaPilotoDTO(
                Integer idAvion, String matricula, String fabricante, String modelo,
                Integer anoFabricacion, int capacidadPax, int capacidadCargaKg,
                String estadoMantenimiento, Timestamp fechaProximoMantenimiento,
                boolean alertaMantenimiento, int totalVuelos, String horasAsignadasTexto
        ) {
            this.idAvion = idAvion;
            this.matricula = matricula;
            this.fabricante = fabricante;
            this.modelo = modelo;
            this.anoFabricacion = anoFabricacion;
            this.capacidadPax = capacidadPax;
            this.capacidadCargaKg = capacidadCargaKg;
            this.estadoMantenimiento = estadoMantenimiento;
            this.fechaProximoMantenimiento = fechaProximoMantenimiento;
            this.alertaMantenimiento = alertaMantenimiento;
            this.totalVuelos = totalVuelos;
            this.horasAsignadasTexto = horasAsignadasTexto;
        }

        public Integer getIdAvion() { return idAvion; }
        public String getMatricula() { return matricula; }
        public String getFabricante() { return fabricante; }
        public String getModelo() { return modelo; }
        public Integer getAnoFabricacion() { return anoFabricacion; }
        public int getCapacidadPax() { return capacidadPax; }
        public int getCapacidadCargaKg() { return capacidadCargaKg; }
        public String getEstadoMantenimiento() { return estadoMantenimiento; }
        public Timestamp getFechaProximoMantenimiento() { return fechaProximoMantenimiento; }
        public boolean isAlertaMantenimiento() { return alertaMantenimiento; }
        public int getTotalVuelos() { return totalVuelos; }
        public String getHorasAsignadasTexto() { return horasAsignadasTexto; }

        public String getModeloCompleto() {
            if (fabricante != null && !fabricante.isBlank()
                    && modelo != null && !modelo.toLowerCase().startsWith(fabricante.toLowerCase())) {
                return fabricante + " " + modelo;
            }
            return modelo != null ? modelo : "Por asignar";
        }
    }

    public static final class NotificacionPilotoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Integer idNotificacion;
        private final String titulo;
        private final String mensaje;
        private final Timestamp fecha;
        private final boolean leido;
        private final String canal;
        private final boolean enviada;

        public NotificacionPilotoDTO(
                Integer idNotificacion, String titulo, String mensaje,
                Timestamp fecha, boolean leido, String canal, boolean enviada
        ) {
            this.idNotificacion = idNotificacion;
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.fecha = fecha;
            this.leido = leido;
            this.canal = canal;
            this.enviada = enviada;
        }

        public Integer getIdNotificacion() { return idNotificacion; }
        public String getTitulo() { return titulo; }
        public String getMensaje() { return mensaje; }
        public Timestamp getFecha() { return fecha; }
        public boolean isLeido() { return leido; }
        public String getCanal() { return canal; }
        public boolean isEnviada() { return enviada; }
    }

    public static final class BriefingVueloDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final VueloResumenPilotoDTO resumen;
        private final List<SegmentoBriefingDTO> tramos;
        private final List<OcupacionClaseDTO> ocupacionPorClase;
        private final List<EquipajeResumenDTO> equipajePorTipo;
        private final List<TripulanteBriefingDTO> tripulacion;
        private final List<ManifiestoPasajeroDTO> manifiestoPasajeros;

        public BriefingVueloDTO(
                VueloResumenPilotoDTO resumen, List<SegmentoBriefingDTO> tramos,
                List<OcupacionClaseDTO> ocupacionPorClase, List<EquipajeResumenDTO> equipajePorTipo,
                List<TripulanteBriefingDTO> tripulacion, List<ManifiestoPasajeroDTO> manifiestoPasajeros
        ) {
            this.resumen = resumen;
            this.tramos = tramos;
            this.ocupacionPorClase = ocupacionPorClase;
            this.equipajePorTipo = equipajePorTipo;
            this.tripulacion = tripulacion;
            this.manifiestoPasajeros = manifiestoPasajeros;
        }

        public VueloResumenPilotoDTO getResumen() { return resumen; }
        public List<SegmentoBriefingDTO> getTramos() { return tramos; }
        public List<OcupacionClaseDTO> getOcupacionPorClase() { return ocupacionPorClase; }
        public List<EquipajeResumenDTO> getEquipajePorTipo() { return equipajePorTipo; }
        public List<TripulanteBriefingDTO> getTripulacion() { return tripulacion; }
        public List<ManifiestoPasajeroDTO> getManifiestoPasajeros() { return manifiestoPasajeros; }
    }

    public static final class PanelPilotoDataDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final KpisPilotoDTO kpis;
        private final VueloResumenPilotoDTO proximoVuelo;
        private final List<VueloResumenPilotoDTO> vuelos;
        private final List<FlotaPilotoDTO> flota;
        private final List<NotificacionPilotoDTO> notificaciones;
        private final ResumenFatigaDTO fatiga;

        public PanelPilotoDataDTO(
                KpisPilotoDTO kpis, VueloResumenPilotoDTO proximoVuelo,
                List<VueloResumenPilotoDTO> vuelos, List<FlotaPilotoDTO> flota,
                List<NotificacionPilotoDTO> notificaciones, ResumenFatigaDTO fatiga
        ) {
            this.kpis = kpis;
            this.proximoVuelo = proximoVuelo;
            this.vuelos = vuelos;
            this.flota = flota;
            this.notificaciones = notificaciones;
            this.fatiga = fatiga;
        }

        public KpisPilotoDTO getKpis() { return kpis; }
        public VueloResumenPilotoDTO getProximoVuelo() { return proximoVuelo; }
        public List<VueloResumenPilotoDTO> getVuelos() { return vuelos; }
        public List<FlotaPilotoDTO> getFlota() { return flota; }
        public List<NotificacionPilotoDTO> getNotificaciones() { return notificaciones; }
        public ResumenFatigaDTO getFatiga() { return fatiga; }
    }

    public static final class DiaAgendaPilotoDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String isoDate;
        private final String diaCorto;
        private final String fechaCorta;
        private final boolean esHoy;
        private final int cantidadVuelos;
        private final String horasTexto;
        private final List<VueloResumenPilotoDTO> vuelos;

        public DiaAgendaPilotoDTO(
                String isoDate, String diaCorto, String fechaCorta,
                boolean esHoy, int cantidadVuelos, String horasTexto,
                List<VueloResumenPilotoDTO> vuelos
        ) {
            this.isoDate = isoDate;
            this.diaCorto = diaCorto;
            this.fechaCorta = fechaCorta;
            this.esHoy = esHoy;
            this.cantidadVuelos = cantidadVuelos;
            this.horasTexto = horasTexto;
            this.vuelos = vuelos;
        }

        public String getIsoDate() { return isoDate; }
        public String getDiaCorto() { return diaCorto; }
        public String getFechaCorta() { return fechaCorta; }
        public boolean isEsHoy() { return esHoy; }
        public int getCantidadVuelos() { return cantidadVuelos; }
        public String getHorasTexto() { return horasTexto; }
        public List<VueloResumenPilotoDTO> getVuelos() { return vuelos; }
    }
}
