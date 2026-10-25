package com.SkyWay.views.Itinerario;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.itinerariovuelo.domain.service.ItinerarioVueloService;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
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
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO;

@Component
@Named("itinerarioBean")
@ViewScoped
public class ItinerarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- SERVICIOS CORE ---
    private final ItinerarioService itinerarioService;
    private final CiudadService ciudadService;
    private final AeropuertoService aeropuertoService;
    private final VueloService vueloService;
    private final ItinerarioVueloService itinVueloService;
    private final TarifaService tarifaService;
    private final ItinerarioTarifaService itinerarioTarifaService;

    // --- ESTADO COMERCIAL Y LOGÍSTICO ---
    private String itinerarioArmadoMapaJson = "[]";
    private Itinerario itinerario = new Itinerario();
    private List<Itinerario> listaItinerarios;
    private List<Tarifa> tarifas;
    private List<Aeropuerto> aeropuertos;
    private List<Ciudad> ciudades;
    private List<Vuelo> vuelos;
    private List<ItinerarioVuelo> itinerariosAsignados;

    // --- MAPAS DE INDEXACIÓN RÁPIDA ---
    private Map<String, Aeropuerto> aeropuertosMap;
    private Map<Integer, BigDecimal> precioTarifas;

    // --- ENLACES DE ENTRADA DESDE LA VISTA ---
    private String aeropuertoOrigen;
    private String aeropuertoDestino;
    private String aeropuertoOrigenBusqueda;
    private String aeropuertoDestinoBusqueda;
    private LocalDate fechaBusqueda;
    private String aeropuertosMapaJson;
    private String vuelosMapaJson = "[]";

    private boolean esModificacion = false;

    // --- CLAVE DE RESPALDO EN SESIÓN ---
    private static final String SESSION_DRAFT_KEY = "SKW_ITINERARIO_DRAFT_";

    @Autowired
    public ItinerarioBean(ItinerarioService itinerarioService, CiudadService ciudadService,
                          AeropuertoService aeropuertoService, VueloService vueloService,
                          ItinerarioVueloService itinVueloService, TarifaService tarifaService,
                          ItinerarioTarifaService itinerarioTarifaService) {
        this.itinerarioService = itinerarioService;
        this.ciudadService = ciudadService;
        this.aeropuertoService = aeropuertoService;
        this.vueloService = vueloService;
        this.itinVueloService = itinVueloService;
        this.tarifaService = tarifaService;
        this.itinerarioTarifaService = itinerarioTarifaService;
    }

    @PostConstruct
    public void init() {
        long inicioTotal = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Inicio de ItinerarioBean.init()");

        this.aeropuertosMap = new HashMap<>();
        this.precioTarifas = new HashMap<>();
        this.itinerariosAsignados = new ArrayList<>();
        this.vuelos = new ArrayList<>();

        // 1. Cargar matriz de itinerarios y catálogo
        cargarListaItinerarios();
        this.tarifas = tarifaService.findAll();
        this.aeropuertos = aeropuertoService.findAll();
        if (this.aeropuertos != null) {
            this.aeropuertos.forEach(a -> aeropuertosMap.put(a.getCodigoIata(), a));
        }

        // 2. Mapeo JSON de aeropuertos para Leaflet
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.aeropuertosMapaJson = mapper.writeValueAsString(aeropuertoService.findAllParaMapa());
        } catch (Exception e) {
            Logger.logInfo("Error serializando aeropuertos para mapa: " + e.getMessage());
            this.aeropuertosMapaJson = "[]";
        }

        // 3. Evaluar contexto: ¿Edición por URL o Restauración por F5?
        boolean editando = verificarParametroEdicion();
        if (!editando) {
            restaurarBorradorSesion();
        }

        Logger.logInfo(">>> [PERF-ITINERARIO] Tiempo TOTAL init(): " + (System.currentTimeMillis() - inicioTotal) + " ms");
    }

    // ==========================================
    // MANEJO DE BORRADOR DE SESIÓN (PERSISTENCIA F5)
    // ==========================================

    public void guardarBorradorSesion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
            sessionMap.put(SESSION_DRAFT_KEY + "itinerario", this.itinerario);
            sessionMap.put(SESSION_DRAFT_KEY + "origen", this.aeropuertoOrigen);
            sessionMap.put(SESSION_DRAFT_KEY + "destino", this.aeropuertoDestino);
            sessionMap.put(SESSION_DRAFT_KEY + "tarifas", this.precioTarifas);
            sessionMap.put(SESSION_DRAFT_KEY + "asignados", this.itinerariosAsignados);
            sessionMap.put(SESSION_DRAFT_KEY + "esModif", this.esModificacion);
            Logger.logInfo("Borrador de itinerario respaldado en sesión HTTP.");
        }
    }

    @SuppressWarnings("unchecked")
    public void restaurarBorradorSesion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;

        Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
        if (sessionMap.containsKey(SESSION_DRAFT_KEY + "origen")) {
            this.itinerario = (Itinerario) sessionMap.get(SESSION_DRAFT_KEY + "itinerario");
            this.aeropuertoOrigen = (String) sessionMap.get(SESSION_DRAFT_KEY + "origen");
            this.aeropuertoDestino = (String) sessionMap.get(SESSION_DRAFT_KEY + "destino");
            this.esModificacion = Boolean.TRUE.equals(sessionMap.get(SESSION_DRAFT_KEY + "esModif"));

            Map<Integer, BigDecimal> tarifasDraft = (Map<Integer, BigDecimal>) sessionMap.get(SESSION_DRAFT_KEY + "tarifas");
            if (tarifasDraft != null) {
                this.precioTarifas.putAll(tarifasDraft);
            }

            List<ItinerarioVuelo> asignadosDraft = (List<ItinerarioVuelo>) sessionMap.get(SESSION_DRAFT_KEY + "asignados");
            if (asignadosDraft != null) {
                this.itinerariosAsignados = new ArrayList<>(asignadosDraft);
            }

            actualizarItinerarioArmadoMapa();
            Logger.logInfo("Borrador de itinerario restaurado con éxito desde la sesión HTTP.");
        }
    }

    public void limpiarBorradorSesion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            Map<String, Object> sessionMap = fc.getExternalContext().getSessionMap();
            sessionMap.remove(SESSION_DRAFT_KEY + "itinerario");
            sessionMap.remove(SESSION_DRAFT_KEY + "origen");
            sessionMap.remove(SESSION_DRAFT_KEY + "destino");
            sessionMap.remove(SESSION_DRAFT_KEY + "tarifas");
            sessionMap.remove(SESSION_DRAFT_KEY + "asignados");
            sessionMap.remove(SESSION_DRAFT_KEY + "esModif");
        }
    }

    // ==========================================
    // LÓGICA DE NEGOCIO Y PERSISTENCIA
    // ==========================================

    public void cargarListaItinerarios() {
        try {
            this.listaItinerarios = itinerarioService.findAll();
        } catch (Exception e) {
            this.listaItinerarios = new ArrayList<>();
            Logger.logInfo("Error al cargar la matriz de itinerarios: " + e.getMessage());
        }
    }

    private boolean verificarParametroEdicion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return false;

        String idParam = fc.getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idParam);
                Optional<Itinerario> itinOpt = Optional.ofNullable(itinerarioService.findById(id));
                if (itinOpt.isPresent()) {
                    this.itinerario = itinOpt.get();
                    this.aeropuertoOrigen = this.itinerario.getAeropuertoOrigen().getCodigoIata();
                    this.aeropuertoDestino = this.itinerario.getAeropuertoDestino().getCodigoIata();
                    this.esModificacion = true;

                    if (this.itinerario.getItinerarioVuelos() != null) {
                        this.itinerariosAsignados = new ArrayList<>(this.itinerario.getItinerarioVuelos());
                        this.itinerariosAsignados.sort(Comparator.comparingInt(ItinerarioVuelo::getOrden));
                    }

                    List<ItinerarioTarifa> tarifasAsociadas = itinerarioTarifaService.findByItinerario(id);
                    if (tarifasAsociadas != null) {
                        for (ItinerarioTarifa it : tarifasAsociadas) {
                            this.precioTarifas.put(it.getTarifa().getIdTarifa(), it.getPrecio());
                        }
                    }

                    Logger.logInfo("Itinerario ID: " + id + " cargado para edición.");
                    actualizarItinerarioArmadoMapa();
                    return true;
                }
            } catch (NumberFormatException e) {
                Logger.logInfo("Formato de ID inválido en la URL de gestión.");
            }
        }
        return false;
    }

    public void saveItinerario() {
        if (this.aeropuertoOrigen == null || this.aeropuertoDestino == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe especificar un origen y un destino comercial.");
            return;
        }

        Aeropuerto origen = aeropuertosMap.get(this.aeropuertoOrigen);
        Aeropuerto destino = aeropuertosMap.get(this.aeropuertoDestino);

        if (!esModificacion) {
            this.itinerario.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        }

        this.itinerario.setAeropuertoOrigen(origen);
        this.itinerario.setAeropuertoDestino(destino);

        this.aeropuertoOrigenBusqueda = this.aeropuertoOrigen;
        this.aeropuertoDestinoBusqueda = this.aeropuertoDestino;
        this.vuelosMapaJson = "[]";

        guardarBorradorSesion();
        Logger.logInfo("Ruta maestra fijada: " + origen.getCodigoIata() + " -> " + destino.getCodigoIata());
        addMessage(FacesMessage.SEVERITY_INFO, "Ruta Actualizada", "Ruta base establecida correctamente.");
    }

    public void guardarTodasLasTarifas() {
        if (this.tarifas == null || this.tarifas.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "No hay catálogo de tarifas cargado.");
            return;
        }

        BigDecimal montoMinimoComercial = new BigDecimal("10000");
        int asignadas = 0;

        for (Tarifa tarifa : this.tarifas) {
            Object rawValue = this.precioTarifas.get(tarifa.getIdTarifa());
            BigDecimal monto = parseToBigDecimal(rawValue);

            if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Tarifa Incompleta",
                        String.format("Debe ingresar un monto válido para la categoría '%s'.", tarifa.getNombre()));
                return;
            }

            if (monto.compareTo(montoMinimoComercial) < 0) {
                addMessage(FacesMessage.SEVERITY_WARN, "Valor Mínimo No Alcanzado",
                        String.format("La tarifa '%s' ($%s) no puede ser inferior al mínimo comercial de $10.000 CLP.",
                                tarifa.getNombre(), monto.toPlainString()));
                return;
            }

            this.precioTarifas.put(tarifa.getIdTarifa(), monto);
            asignadas++;
        }

        if (asignadas < this.tarifas.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Estrategia Comercial Incompleta",
                    String.format("Ha configurado %d de %d tarifas. Debe ingresar un valor para todas las categorías antes de continuar.",
                            asignadas, this.tarifas.size()));
            return;
        }

        String errorJerarquia = validarCoherenciaJerarquica();
        if (errorJerarquia != null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Incoherencia Comercial de Tarifas", errorJerarquia);
            return;
        }

        guardarBorradorSesion();
        Logger.logInfo("Precios de tarifas consolidados y validados correctamente.");
        addMessage(FacesMessage.SEVERITY_INFO, "Configuración Guardada", "Se han fijado y validado exitosamente los precios de todas las tarifas.");
    }

    private BigDecimal parseToBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            String strVal = value.toString().replaceAll("[^0-9.]", "").trim();
            return strVal.isEmpty() ? null : new BigDecimal(strVal);
        } catch (Exception e) {
            return null;
        }
    }

    private String validarCoherenciaJerarquica() {
        BigDecimal precioBasica = null;
        BigDecimal precioIntermedia = null;
        BigDecimal precioPremium = null;

        String nombreBasica = "", nombreIntermedia = "", nombrePremium = "";

        for (Tarifa t : this.tarifas) {
            String nombreLower = t.getNombre().toLowerCase().trim();
            BigDecimal precio = parseToBigDecimal(this.precioTarifas.get(t.getIdTarifa()));

            if (nombreLower.contains("basic") || nombreLower.contains("básic") || nombreLower.contains("promo") || nombreLower.contains("light")) {
                precioBasica = precio;
                nombreBasica = t.getNombre();
            } else if (nombreLower.contains("flex") || nombreLower.contains("med") || nombreLower.contains("standar") || nombreLower.contains("estándar")) {
                precioIntermedia = precio;
                nombreIntermedia = t.getNombre();
            } else if (nombreLower.contains("premium") || nombreLower.contains("execut") || nombreLower.contains("ejecut") || nombreLower.contains("top") || nombreLower.contains("first")) {
                precioPremium = precio;
                nombrePremium = t.getNombre();
            }
        }

        if (precioBasica != null && precioPremium != null && precioBasica.compareTo(precioPremium) >= 0) {
            return String.format("La tarifa '%s' ($%s) no puede ser mayor o igual a la tarifa '%s' ($%s).",
                    nombreBasica, precioBasica.toPlainString(), nombrePremium, precioPremium.toPlainString());
        }

        if (precioBasica != null && precioIntermedia != null && precioBasica.compareTo(precioIntermedia) >= 0) {
            return String.format("La tarifa '%s' ($%s) no puede ser mayor o igual a la tarifa '%s' ($%s).",
                    nombreBasica, precioBasica.toPlainString(), nombreIntermedia, precioIntermedia.toPlainString());
        }

        if (precioIntermedia != null && precioPremium != null && precioIntermedia.compareTo(precioPremium) >= 0) {
            return String.format("La tarifa '%s' ($%s) no puede ser mayor o igual a la tarifa '%s' ($%s).",
                    nombreIntermedia, precioIntermedia.toPlainString(), nombrePremium, precioPremium.toPlainString());
        }

        return null;
    }

    public void agregarVuelo(Vuelo vuelo) {
        if (vuelo == null) return;

        if (estaVinculado(vuelo)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Vuelo Ya Vinculado",
                    String.format("El vuelo %s ya forma parte de este itinerario.", vuelo.getNumeroVuelo()));
            return;
        }

        if (!esFechaCorrecta(vuelo)) {
            Vuelo ultimo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
            addMessage(FacesMessage.SEVERITY_WARN, "Incoherencia Horaria",
                    String.format("El vuelo %s sale (%s) antes de que aterrice el vuelo anterior %s (%s).",
                            vuelo.getNumeroVuelo(), vuelo.getFechaHoraSalida(), ultimo.getNumeroVuelo(), ultimo.getFechaHoraLlegada()));
            return;
        }

        if (!ultimoAeropuertoCorrecto(vuelo)) {
            Vuelo ultimo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
            var origenNuevo = vuelo.getSegmentoVuelos().get(0).getAeropuertoOrigen();
            var destinoUltimo = ultimo.getSegmentoVuelos().get(ultimo.getSegmentoVuelos().size() - 1).getAeropuertoDestino();

            addMessage(FacesMessage.SEVERITY_WARN, "Desconexión Geográfica",
                    String.format("El origen de este vuelo (%s) no coincide con el destino del vuelo anterior (%s).",
                            origenNuevo.getCodigoIata(), destinoUltimo.getCodigoIata()));
            return;
        }

        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(this.itinerario);
        iv.setVuelo(vuelo);
        iv.setOrden(this.itinerariosAsignados.size() + 1);

        this.itinerariosAsignados.add(iv);
        actualizarItinerarioArmadoMapa();
        guardarBorradorSesion();

        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Vinculado",
                String.format("Vuelo %s agregado en posición #%d.", vuelo.getNumeroVuelo(), iv.getOrden()));
    }

    public void removerVuelo(ItinerarioVuelo iv) {
        this.itinerariosAsignados.remove(iv);
        for (int i = 0; i < this.itinerariosAsignados.size(); i++) {
            this.itinerariosAsignados.get(i).setOrden(i + 1);
        }
        actualizarItinerarioArmadoMapa();
        guardarBorradorSesion();
        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Removido", "Se actualizó la secuencia operativa.");
    }

    public void guardarItinerario() {
        if (this.itinerario.getAeropuertoOrigen() == null || this.itinerario.getAeropuertoDestino() == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Error de Secuencia", "Debe fijar primero la ruta maestra (Paso 1).");
            return;
        }
        if (this.precioTarifas.isEmpty() || this.precioTarifas.size() < this.tarifas.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Estrategia Comercial", "Debe establecer los precios de todas las tarifas (Paso 2).");
            return;
        }
        if (this.itinerariosAsignados.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Plan Operativo Vacío", "Debe enlazar al menos un tramo operativo (Paso 3).");
            return;
        }

        String resumenSecuencia = getResumenSecuencia();
        if (!"OK".equals(resumenSecuencia)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Incoherencia en Plan Operativo", resumenSecuencia);
            return;
        }

        try {
            this.itinerariosAsignados.sort(Comparator.comparingInt(ItinerarioVuelo::getOrden));

            Vuelo primerVuelo = this.itinerariosAsignados.get(0).getVuelo();
            Vuelo ultimoVuelo = this.itinerariosAsignados.get(this.itinerariosAsignados.size() - 1).getVuelo();

            this.itinerario.setHoraSalida(primerVuelo.getFechaHoraSalida());
            this.itinerario.setHoraLlegada(ultimoVuelo.getFechaHoraLlegada());
            this.itinerario.setNumeroEscalas(Math.max(0, this.itinerariosAsignados.size() - 1));

            Itinerario itinerarioGuardado = itinerarioService.save(this.itinerario);

            if (esModificacion) {
                List<ItinerarioVuelo> tramosViejos = itinVueloService.findByItinerarioId(itinerarioGuardado.getIdItinerario());
                if (tramosViejos != null) {
                    for (ItinerarioVuelo tv : tramosViejos) {
                        itinVueloService.deleteById(tv.getIdItinerarioVuelo());
                    }
                }

                List<ItinerarioTarifa> tarifasViejas = itinerarioTarifaService.findByItinerario(itinerarioGuardado.getIdItinerario());
                if (tarifasViejas != null) {
                    for (ItinerarioTarifa tv : tarifasViejas) {
                        itinerarioTarifaService.deleteById(tv.getIdItinerarioTarifa());
                    }
                }
            }

            for (ItinerarioVuelo iv : this.itinerariosAsignados) {
                iv.setIdItinerarioVuelo(null);
                iv.setItinerario(itinerarioGuardado);
                itinVueloService.save(iv);
            }

            for (Map.Entry<Integer, BigDecimal> entry : this.precioTarifas.entrySet()) {
                Optional<Tarifa> tOpt = tarifaService.findById(entry.getKey());
                if (tOpt.isPresent()) {
                    ItinerarioTarifa itinerarioTarifa = new ItinerarioTarifa();
                    itinerarioTarifa.setTarifa(tOpt.get());
                    itinerarioTarifa.setItinerario(itinerarioGuardado);
                    itinerarioTarifa.setPrecio(entry.getValue());
                    itinerarioTarifaService.save(itinerarioTarifa);
                }
            }

            String msgExito = esModificacion ? "El itinerario ha sido actualizado con éxito." : "El nuevo itinerario fue publicado exitosamente.";
            addMessage(FacesMessage.SEVERITY_INFO, "Operación Exitosa", msgExito);

            limpiarBorradorSesion();
            cargarListaItinerarios();
            resetForm();

        } catch (Exception e) {
            Logger.logInfo("Error de persistencia en guardarItinerario: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Consolidación", "No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    public void resetForm() {
        this.itinerario = new Itinerario();
        this.itinerariosAsignados.clear();
        this.precioTarifas.clear();
        this.vuelos.clear();
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        this.aeropuertoOrigenBusqueda = null;
        this.aeropuertoDestinoBusqueda = null;
        this.fechaBusqueda = null;
        this.esModificacion = false;
        this.itinerarioArmadoMapaJson = "[]";
        limpiarBorradorSesion();
    }

    // ==========================================
    // MÉTODOS AUXILIARES Y ACCESORES
    // ==========================================

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

    public void seleccionarOrigenMapa() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;
        String iata = fc.getExternalContext().getRequestParameterMap().get("iata");

        if (iata != null && aeropuertosMap.containsKey(iata) && !iata.equals(this.aeropuertoDestino)) {
            this.aeropuertoOrigen = iata;
            guardarBorradorSesion();
        }
    }

    public void seleccionarDestinoMapa() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;
        String iata = fc.getExternalContext().getRequestParameterMap().get("iata");

        if (iata != null && aeropuertosMap.containsKey(iata) && !iata.equals(this.aeropuertoOrigen)) {
            this.aeropuertoDestino = iata;
            guardarBorradorSesion();
        }
    }

    public void reiniciarSeleccionRutaMaestra() {
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
        guardarBorradorSesion();
    }

    public void buscarVuelos() {
        String fechaStr = (this.fechaBusqueda != null) ? this.fechaBusqueda.toString() : null;
        this.vuelos = vueloService.buscarVuelo(aeropuertoOrigenBusqueda, aeropuertoDestinoBusqueda, fechaStr);
    }

    public boolean estaVinculado(Vuelo vuelo) {
        if (vuelo == null) return false;
        return itinerariosAsignados.stream().anyMatch(iv -> iv.getVuelo().getIdVuelo().equals(vuelo.getIdVuelo()));
    }

    public boolean esFechaCorrecta(Vuelo vuelo) {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) return true;
        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        return !vuelo.getFechaHoraSalida().before(ultimoVuelo.getFechaHoraLlegada());
    }

    public boolean ultimoAeropuertoCorrecto(Vuelo vuelo) {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) return true;
        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        var aeropuertoDestinoUltimo = ultimoVuelo.getSegmentoVuelos()
                .get(ultimoVuelo.getSegmentoVuelos().size() - 1)
                .getAeropuertoDestino().getIdAeropuerto();

        var aeropuertoOrigenNuevo = vuelo.getSegmentoVuelos().get(0).getAeropuertoOrigen().getIdAeropuerto();
        return aeropuertoDestinoUltimo.equals(aeropuertoOrigenNuevo);
    }

    public String getResumenSecuencia() {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) {
            return "El itinerario no tiene vuelos asignados.";
        }

        String origenPrimero = itinerariosAsignados.get(0).getVuelo()
                .getSegmentoVuelos().get(0).getAeropuertoOrigen().getCodigoIata();

        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        String destinoUltimo = ultimoVuelo.getSegmentoVuelos()
                .get(ultimoVuelo.getSegmentoVuelos().size() - 1)
                .getAeropuertoDestino().getCodigoIata();

        boolean coincideOrigen = origenPrimero.equalsIgnoreCase(this.aeropuertoOrigen);
        boolean coincideDestino = destinoUltimo.equalsIgnoreCase(this.aeropuertoDestino);

        if (coincideOrigen && coincideDestino) return "OK";

        if (!coincideOrigen && !coincideDestino) {
            return String.format("Incoherencia total: Inicia en %s y termina en %s (Se requiere %s → %s).",
                    origenPrimero, destinoUltimo, this.aeropuertoOrigen, this.aeropuertoDestino);
        } else if (!coincideOrigen) {
            return String.format("Origen incoherente: Inicia en %s, pero el origen planificado es %s.",
                    origenPrimero, this.aeropuertoOrigen);
        } else {
            return String.format("Destino incompleto: Finaliza en %s, pero el destino planificado es %s.",
                    destinoUltimo, this.aeropuertoDestino);
        }
    }

    private void actualizarItinerarioArmadoMapa() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Vuelo> vuelosEnOrden = itinerariosAsignados.stream()
                    .sorted(Comparator.comparingInt(ItinerarioVuelo::getOrden))
                    .map(ItinerarioVuelo::getVuelo)
                    .toList();

            List<VueloMapaDTO> geometrias = vueloService.construirGeometriaVuelos(vuelosEnOrden);
            this.itinerarioArmadoMapaJson = mapper.writeValueAsString(geometrias);

            if (PrimeFaces.current().isAjaxRequest()) {
                String script = String.format("renderItinerarioArmado(%s);", mapper.writeValueAsString(this.itinerarioArmadoMapaJson));
                PrimeFaces.current().executeScript(script);
            }
        } catch (Exception e) {
            this.itinerarioArmadoMapaJson = "[]";
        }
    }

    public int getNumeroEscalasCalculado() {
        return Math.max(0, itinerariosAsignados.size() - 1);
    }

    // --- GETTERS Y SETTERS ---
    public String getItinerarioArmadoMapaJson() { return itinerarioArmadoMapaJson; }
    public String getAeropuertosMapaJson() { return aeropuertosMapaJson; }
    public String getVuelosMapaJson() { return vuelosMapaJson; }
    public Itinerario getItinerario() { return itinerario; }
    public void setItinerario(Itinerario itinerario) { this.itinerario = itinerario; }
    public List<Itinerario> getListaItinerarios() { return listaItinerarios; }
    public List<Tarifa> getTarifas() { return tarifas; }
    public void setTarifas(List<Tarifa> tarifas) { this.tarifas = tarifas; }
    public List<Aeropuerto> getAeropuertos() { return aeropuertos; }
    public List<Ciudad> getCiudads() { return ciudades; }
    public List<Vuelo> getVuelos() { return vuelos; }
    public List<ItinerarioVuelo> getItinerariosAsignados() { return itinerariosAsignados; }
    public Map<Integer, BigDecimal> getPrecioTarifas() { return precioTarifas; }
    public Map<String, Aeropuerto> getAeropuertosMap() { return aeropuertosMap; }
    public String getAeropuertoOrigen() { return aeropuertoOrigen; }
    public void setAeropuertoOrigen(String aeropuertoOrigen) { this.aeropuertoOrigen = aeropuertoOrigen; }
    public String getAeropuertoDestino() { return aeropuertoDestino; }
    public void setAeropuertoDestino(String aeropuertoDestino) { this.aeropuertoDestino = aeropuertoDestino; }
    public String getAeropuertoOrigenBusqueda() { return aeropuertoOrigenBusqueda; }
    public void setAeropuertoOrigenBusqueda(String origenBusqueda) { this.aeropuertoOrigenBusqueda = origenBusqueda; }
    public String getAeropuertoDestinoBusqueda() { return aeropuertoDestinoBusqueda; }
    public void setAeropuertoDestinoBusqueda(String destinoBusqueda) { this.aeropuertoDestinoBusqueda = destinoBusqueda; }
    public LocalDate getFechaBusqueda() { return fechaBusqueda; }
    public void setFechaBusqueda(LocalDate fechaBusqueda) { this.fechaBusqueda = fechaBusqueda; }
    public boolean isEsModificacion() { return esModificacion; }
}