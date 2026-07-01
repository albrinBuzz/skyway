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
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

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

    // Indicador analítico de persistencia
    private boolean esModificacion = false;

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
        this.aeropuertosMap = new HashMap<>();
        this.precioTarifas = new HashMap<>();
        this.itinerariosAsignados = new ArrayList<>();
        this.vuelos = new ArrayList<>();

        cargarListaItinerarios();
        this.vuelos = vueloService.findAll();
        this.tarifas = tarifaService.listarTarifas();
        this.ciudades = ciudadService.getAllCiudades();
        this.aeropuertos = aeropuertoService.findAll();

        this.aeropuertos.forEach(a -> aeropuertosMap.put(a.getCodigoIata(), a));

        verificarParametroEdicion();
    }

    public void cargarListaItinerarios() {
        try {
            this.listaItinerarios = itinerarioService.findAll();
        } catch (Exception e) {
            this.listaItinerarios = new ArrayList<>();
            Logger.logInfo("Error al cargar la matriz de itinerarios: " + e.getMessage());
        }
    }

    // --- DETECTA Y PRECARGA SI ES EDICIÓN ---
    // --- DENTRO DE ItinerarioBean.java ---

    private void verificarParametroEdicion() {
        String idParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("id");
        if (idParam != null && !idParam.isEmpty()) {
            try {
                Integer id = Integer.parseInt(idParam);
                Optional<Itinerario> itinOpt = Optional.ofNullable(itinerarioService.findById(id));
                if (itinOpt.isPresent()) {
                    this.itinerario = itinOpt.get();
                    this.aeropuertoOrigen = this.itinerario.getAeropuertoOrigen().getCodigoIata();
                    this.aeropuertoDestino = this.itinerario.getAeropuertoDestino().getCodigoIata();
                    this.esModificacion = true;

                    // 1. Precargar tramos físicos
                    if (this.itinerario.getItinerarioVuelos() != null) {
                        this.itinerariosAsignados = new ArrayList<>(this.itinerario.getItinerarioVuelos());
                        this.itinerariosAsignados.sort(Comparator.comparingInt(ItinerarioVuelo::getOrden));
                    }

                    // 2. SOLUCIÓN: Usar el método del servicio en lugar de la entidad
                    List<ItinerarioTarifa> tarifasAsociadas = itinerarioTarifaService.findByItinerario(id);
                    if (tarifasAsociadas != null) {
                        for (ItinerarioTarifa it : tarifasAsociadas) {
                            this.precioTarifas.put(it.getTarifa().getIdTarifa(), it.getPrecio());
                        }
                    }

                    Logger.logInfo("Itinerario ID: " + id + " cargado con éxito. Tarifas mapeadas: " + this.precioTarifas.size());
                }
            } catch (NumberFormatException e) {
                Logger.logInfo("Formato de ID inválido en la URL de gestión.");
            }
        }
    }

    public void eliminarItinerario(Itinerario itin) {
        if (itin == null) return;
        try {
            itinerarioService.deleteById(itin.getIdItinerario());
            this.listaItinerarios.remove(itin);
            Logger.logInfo("Itinerario SKW-ITI-" + itin.getIdItinerario() + " purgado del sistema.");
            addMessage(FacesMessage.SEVERITY_INFO, "Registro Eliminado", "El itinerario fue removido correctamente.");
        } catch (Exception e) {
            Logger.logInfo("Error al intentar eliminar itinerario: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Restricción", "No se puede eliminar porque cuenta con operaciones o reservas activas.");
        }
    }

    // --- PASO 1: RUTA MAESTRA (Fijar o Actualizar) ---
    public void saveItinerario() {
        if (this.aeropuertoOrigen == null || this.aeropuertoDestino == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe especificar un origen y un destino comercial.");
            return;
        }

        Aeropuerto origen = aeropuertosMap.get(this.aeropuertoOrigen);
        Aeropuerto destino = aeropuertosMap.get(this.aeropuertoDestino);

        // Conservar fecha de creación si estamos en flujo de edición
        if (!esModificacion) {
            this.itinerario.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        }

        this.itinerario.setAeropuertoOrigen(origen);
        this.itinerario.setAeropuertoDestino(destino);

        Logger.logInfo("Ruta maestra modificada/fijada: " + origen.getCodigoIata() + " -> " + destino.getCodigoIata());
        addMessage(FacesMessage.SEVERITY_INFO, "Ruta Actualizada", "Ruta base establecida correctamente.");
    }

    public void buscarVuelos() {
        String fechaStr = (this.fechaBusqueda != null) ? this.fechaBusqueda.toString() : null;
        this.vuelos = vueloService.buscarVuelo(aeropuertoOrigenBusqueda, aeropuertoDestinoBusqueda, fechaStr);
        Logger.logInfo("Registros de vuelo indexados para selección: " + vuelos.size());
    }

    public void agregarVuelo(Vuelo vuelo) {
        if (vuelo == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Operación Inválida", "El vuelo seleccionado no es válido.");
            return;
        }

        boolean yaAsignado = itinerariosAsignados.stream()
                .anyMatch(iv -> iv.getVuelo().getIdVuelo().equals(vuelo.getIdVuelo()));

        if (yaAsignado) {
            addMessage(FacesMessage.SEVERITY_WARN, "Duplicidad", "Este tramo de vuelo ya se encuentra enlazado a la ruta.");
            return;
        }

        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(this.itinerario);
        iv.setVuelo(vuelo);
        iv.setOrden(this.itinerariosAsignados.size() + 1);

        this.itinerariosAsignados.add(iv);
        Logger.logInfo("Vuelo físico " + vuelo.getNumeroVuelo() + " acoplado al itinerario.");
        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Vinculado", "Vuelo " + vuelo.getNumeroVuelo() + " agregado al plan.");
    }

    // NUEVO: Permite quitar un tramo en caliente desde la vista si se equivocaron al armarlo
    public void removerVuelo(ItinerarioVuelo iv) {
        this.itinerariosAsignados.remove(iv);
        // Re-indexar el orden secuencial de los tramos restantes
        for (int i = 0; i < this.itinerariosAsignados.size(); i++) {
            this.itinerariosAsignados.get(i).setOrden(i + 1);
        }
        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Removido", "Se actualizó la secuencia operativa.");
    }

    public void actualizarPrecioTarifaIndividual(Tarifa tarifa, Object valorInput) {
        if (tarifa == null || valorInput == null || valorInput.toString().trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "Debe proporcionar un monto tarifario correcto.");
            return;
        }

        try {
            BigDecimal precioIngresado;
            if (valorInput instanceof Number) {
                precioIngresado = BigDecimal.valueOf(((Number) valorInput).doubleValue());
            } else {
                String limpio = valorInput.toString().replaceAll("[^0-9.]", "");
                precioIngresado = new BigDecimal(limpio);
            }

            this.precioTarifas.put(tarifa.getIdTarifa(), precioIngresado);
            addMessage(FacesMessage.SEVERITY_INFO, "Precio Modificado", "Categoría " + tarifa.getNombre() + ": $" + precioIngresado);
        } catch (NumberFormatException e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Formato", "El valor monetario no es válido.");
        }
    }

    // --- CONSOLIDACIÓN FINAL (CREATE OR UPDATE) ---
    public void guardarItinerario() {
        if (this.itinerario.getAeropuertoOrigen() == null || this.itinerario.getAeropuertoDestino() == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Error de Secuencia", "Debe fijar primero la ruta maestra (Paso 1).");
            return;
        }
        if (this.precioTarifas.isEmpty() || this.precioTarifas.size() < this.tarifas.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Estrategia Comercial", "Debe establecer los precios de todas las tarifas comerciales (Paso 2).");
            return;
        }
        if (this.itinerariosAsignados.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Plan Operativo Vacío", "Debe enlazar al menos un tramo operativo de vuelo (Paso 3).");
            return;
        }

        // --- REEMPLAZA ESTE BLOQUE DENTRO DE guardarItinerario() EN ItinerarioBean.java ---

        try {
            this.itinerario.setNumeroEscalas(Math.max(0, this.itinerariosAsignados.size() - 1));

            // 1. Guardar o actualizar la raíz del Itinerario
            Itinerario itinerarioGuardado = itinerarioService.save(this.itinerario);

            // 2. SOLUCIÓN COMPATIBLE: Limpieza en cascada manual para el modo Modificación
            if (esModificacion) {
                // Limpiar Tramos de Vuelo antiguos asociados a este itinerario
                List<ItinerarioVuelo> tramosViejos = itinVueloService.findByItinerarioId(itinerarioGuardado.getIdItinerario());
                if (tramosViejos != null) {
                    for (ItinerarioVuelo tv : tramosViejos) {
                        // Asumiendo que tu entidad ItinerarioVuelo tiene un método getIdItinerarioVuelo() o similar
                        // Ajusta el getter del ID según cómo se llame la Primary Key en tu entidad ItinerarioVuelo
                        itinVueloService.deleteById(tv.getIdItinerarioVuelo());
                    }
                }

                // Limpiar Tarifas antiguas asociadas a este itinerario
                List<ItinerarioTarifa> tarifasViejas = itinerarioTarifaService.findByItinerario(itinerarioGuardado.getIdItinerario());
                if (tarifasViejas != null) {
                    for (ItinerarioTarifa tv : tarifasViejas) {
                        // Ajusta el getter del ID según corresponda en tu entidad ItinerarioTarifa
                        itinerarioTarifaService.deleteById(tv.getIdItinerarioTarifa());
                    }
                }
            }

            // 3. Persistir los nuevos Tramos Físicos de Vuelo configurados en la UI
            for (ItinerarioVuelo iv : this.itinerariosAsignados) {
                // Importante: Blanqueamos el ID de la relación para que JPA lo maneje como una inserción nueva y limpia
                iv.setIdItinerarioVuelo(null); // Ajusta al nombre de tu setter de ID (ej: setId(), setIdItinerarioVuelo())
                iv.setItinerario(itinerarioGuardado);
                itinVueloService.save(iv);
            }

            // 4. Persistir la nueva matriz de precios por tarifa
            for (Map.Entry<Integer, BigDecimal> entry : this.precioTarifas.entrySet()) {
                Optional<Tarifa> tOpt = tarifaService.buscarPorId(entry.getKey());
                if (tOpt.isPresent()) {
                    ItinerarioTarifa itinerarioTarifa = new ItinerarioTarifa();
                    // itinerarioTarifa.setIdItinerarioTarifa(null); // Descomenta si tiene ID autoincremental simple
                    itinerarioTarifa.setTarifa(tOpt.get());
                    itinerarioTarifa.setItinerario(itinerarioGuardado);
                    itinerarioTarifa.setPrecio(entry.getValue());

                    itinerarioTarifaService.save(itinerarioTarifa);
                }
            }

            String msgExito = esModificacion ? "El itinerario ha sido modificado y actualizado con éxito." : "La propuesta comercial de itinerario ha sido guardada y publicada.";
            addMessage(FacesMessage.SEVERITY_INFO, "Operación Exitosa", msgExito);

            cargarListaItinerarios();
            resetForm();

        } catch (Exception e) {
            Logger.logInfo("Error crítico de persistencia en guardarItinerario: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Consolidación", "No se pudieron guardar los cambios: " + e.getMessage());
        }
    }

    private void resetForm() {
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
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // --- ACCESORES COMPATIBLES ---
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