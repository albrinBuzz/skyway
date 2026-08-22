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

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.vuelo.presentation.dto.VueloMapaDTO;
import jakarta.faces.context.ExternalContext;
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
    private String itinerarioArmadoMapaJson = "[]";

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
    private String aeropuertosMapaJson;
    private String vuelosMapaJson = "[]";

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
        long inicioTotal = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Inicio de ItinerarioBean.init()");

        this.aeropuertosMap = new HashMap<>();
        this.precioTarifas = new HashMap<>();
        this.itinerariosAsignados = new ArrayList<>();
        this.vuelos = new ArrayList<>(); // Inicializar lista vacía para la búsqueda

        // 1. Cargar la matriz de itinerarios
        long t1 = System.currentTimeMillis();
        cargarListaItinerarios();
        long t2 = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Cargar lista itinerarios: " + (t2 - t1) + " ms");

        // 2. Cargar tarifas de catálogo
        long t3 = System.currentTimeMillis();
        this.tarifas = tarifaService.listarTarifas();
        long t4 = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Cargar tarifas: " + (t4 - t3) + " ms");

        // 3. Cargar aeropuertos
        long t5 = System.currentTimeMillis();
        this.aeropuertos = aeropuertoService.findAll();
        if (this.aeropuertos != null) {
            this.aeropuertos.forEach(a -> aeropuertosMap.put(a.getCodigoIata(), a));
        }
        long t6 = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Cargar aeropuertos (" + (aeropuertos != null ? aeropuertos.size() : 0) + "): " + (t6 - t5) + " ms");

        // 4. Mapeo JSON para el mapa interactivo
        long t7 = System.currentTimeMillis();
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.aeropuertosMapaJson = mapper.writeValueAsString(aeropuertoService.findAllParaMapa());
        } catch (Exception e) {
            Logger.logInfo("Error serializando aeropuertos para mapa: " + e.getMessage());
            this.aeropuertosMapaJson = "[]";
        }
        long t8 = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Serialización JSON mapa: " + (t8 - t7) + " ms");

        // 5. Verificar si viene ID en la URL para edición
        long t9 = System.currentTimeMillis();
        verificarParametroEdicion();
        long t10 = System.currentTimeMillis();
        Logger.logInfo(">>> [PERF-ITINERARIO] Verificación edición: " + (t10 - t9) + " ms");

        Logger.logInfo(">>> [PERF-ITINERARIO] Tiempo TOTAL init(): " + (System.currentTimeMillis() - inicioTotal) + " ms");
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
    public String getItinerarioArmadoMapaJson() { return itinerarioArmadoMapaJson; }
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
                    actualizarItinerarioArmadoMapa();
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

        if (!esModificacion) {
            this.itinerario.setFechaCreacion(new Timestamp(System.currentTimeMillis()));
        }

        this.itinerario.setAeropuertoOrigen(origen);
        this.itinerario.setAeropuertoDestino(destino);

        // Precargar el buscador del paso 3 con la ruta recién fijada, como ayuda al operador
        this.aeropuertoOrigenBusqueda = this.aeropuertoOrigen;
        this.aeropuertoDestinoBusqueda = this.aeropuertoDestino;
        this.vuelosMapaJson = "[]"; // limpia geometría vieja hasta la próxima búsqueda

        Logger.logInfo("Ruta maestra modificada/fijada: " + origen.getCodigoIata() + " -> " + destino.getCodigoIata());
        addMessage(FacesMessage.SEVERITY_INFO, "Ruta Actualizada", "Ruta base establecida correctamente.");
    }


    public void agregarVuelo(Vuelo vuelo) {
        if (vuelo == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Selección Inválida",
                    "No se ha seleccionado ningún vuelo para agregar.");
            return;
        }

        if (estaVinculado(vuelo)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Vuelo Ya Vinculado",
                    String.format("El vuelo %s ya se encuentra formando parte de este itinerario.", vuelo.getNumeroVuelo()));
            return;
        }

        if (!esFechaCorrecta(vuelo)) {
            Vuelo ultimo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
            addMessage(FacesMessage.SEVERITY_WARN, "Incoherencia Horaria",
                    String.format("El vuelo %s sale (%s) antes de que aterrice el vuelo anterior %s (%s).",
                            vuelo.getNumeroVuelo(),
                            vuelo.getFechaHoraSalida(),
                            ultimo.getNumeroVuelo(),
                            ultimo.getFechaHoraLlegada()));
            return;
        }

        if (!ultimoAeropuertoCorrecto(vuelo)) {
            Vuelo ultimo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();

            var origenNuevo = vuelo.getSegmentoVuelos().get(0).getAeropuertoOrigen();
            var destinoUltimo = ultimo.getSegmentoVuelos().get(ultimo.getSegmentoVuelos().size() - 1).getAeropuertoDestino();

            addMessage(FacesMessage.SEVERITY_WARN, "Desconexión Geográfica",
                    String.format("El origen de este vuelo (%s - %s) no coincide con el destino del vuelo anterior (%s - %s).",
                            origenNuevo.getCodigoIata(),
                            origenNuevo.getNombreAeropuerto(),
                            destinoUltimo.getCodigoIata(),
                            destinoUltimo.getNombreAeropuerto()));
            return;
        }

        // Si pasa todas las validaciones:
        ItinerarioVuelo iv = new ItinerarioVuelo();
        iv.setItinerario(this.itinerario);
        iv.setVuelo(vuelo);
        iv.setOrden(this.itinerariosAsignados.size() + 1);

        this.itinerariosAsignados.add(iv);
        actualizarItinerarioArmadoMapa(); // Refresca el mapa

        Logger.logInfo("Vuelo " + vuelo.getNumeroVuelo() + " acoplado exitosamente al itinerario.");
        addMessage(FacesMessage.SEVERITY_INFO, "Tramo Vinculado",
                String.format("El vuelo %s ha sido agregado correctamente en la posición #%d.",
                        vuelo.getNumeroVuelo(),
                        iv.getOrden()));
    }

    public boolean esFechaCorrecta(Vuelo vuelo) {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) {
            return true;
        }

        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();

        // Válido solo si la salida del nuevo vuelo es IGUAL o POSTERIOR a la llegada del anterior
        return !vuelo.getFechaHoraSalida().before(ultimoVuelo.getFechaHoraLlegada());
    }

    public boolean ultimoAeropuertoCorrecto(Vuelo vuelo) {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) {
            return true;
        }

        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();

        // Destino del ÚLTIMO segmento del ÚLTIMO vuelo asignado
        var aeropuertoDestinoUltimo = ultimoVuelo.getSegmentoVuelos()
                .get(ultimoVuelo.getSegmentoVuelos().size() - 1)
                .getAeropuertoDestino()
                .getIdAeropuerto();

        // Origen del PRIMER segmento del NUEVO vuelo (revisar get(0))
        var aeropuertoOrigenNuevo = vuelo.getSegmentoVuelos()
                .get(0)
                .getAeropuertoOrigen()
                .getIdAeropuerto();

        return aeropuertoDestinoUltimo.equals(aeropuertoOrigenNuevo);
    }

    public void prepararMapaVuelo(Vuelo vuelo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<VueloMapaDTO> geometrias = vueloService.construirGeometriaVuelos(List.of(vuelo));

            if (!geometrias.isEmpty()) {
                String jsonVuelo = mapper.writeValueAsString(geometrias.get(0));
                // Pasa los datos del vuelo formateados directamente a la callback de PrimeFaces
                PrimeFaces.current().ajax().addCallbackParam("vueloJson", jsonVuelo);
            }
        } catch (Exception e) {
            Logger.logInfo("Error preparando vista previa de vuelo: " + e.getMessage());
        }
    }


    public void removerVuelo(ItinerarioVuelo iv) {
        this.itinerariosAsignados.remove(iv);
        for (int i = 0; i < this.itinerariosAsignados.size(); i++) {
            this.itinerariosAsignados.get(i).setOrden(i + 1);
        }
        actualizarItinerarioArmadoMapa(); // 👈 refresca el mapa de la derecha
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
        boolean ok=true;
        if (this.itinerario.getAeropuertoOrigen() == null || this.itinerario.getAeropuertoDestino() == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Error de Secuencia", "Debe fijar primero la ruta maestra (Paso 1).");
          ok    =false;
        }
        if (this.precioTarifas.isEmpty() || this.precioTarifas.size() < this.tarifas.size()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Estrategia Comercial", "Debe establecer los precios de todas las tarifas comerciales (Paso 2).");
            ok    =false;
        }
        if (this.itinerariosAsignados.isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Plan Operativo Vacío", "Debe enlazar al menos un tramo operativo de vuelo (Paso 3).");
            ok    =false;
        }

        String resumenSecuencia = getResumenSecuencia();
        if (!"OK".equals(resumenSecuencia)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Incoherencia en Plan Operativo",
                    resumenSecuencia + " Por favor, ajuste los tramos para conectar " + this.aeropuertoOrigen + " con " + this.aeropuertoDestino + ".");
            return;
        }

        // --- REEMPLAZA ESTE BLOQUE DENTRO DE guardarItinerario() EN ItinerarioBean.java ---

        if (ok) {

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
        this.itinerarioArmadoMapaJson = "[]";
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public void seleccionarOrigenMapa() {
        String iata = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("iata");

        if (iata == null || !aeropuertosMap.containsKey(iata)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Selección inválida", "Aeropuerto no reconocido.");
            return;
        }

        if (iata.equals(this.aeropuertoDestino)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Selección inválida", "El origen no puede ser igual al destino.");
            return;
        }

        this.aeropuertoOrigen = iata;
        Logger.logInfo("Origen de ruta maestra fijado desde mapa: " + iata);
    }

    public void seleccionarDestinoMapa() {
        String iata = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("iata");

        if (iata == null || !aeropuertosMap.containsKey(iata)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Selección inválida", "Aeropuerto no reconocido.");
            return;
        }

        if (iata.equals(this.aeropuertoOrigen)) {
            addMessage(FacesMessage.SEVERITY_WARN, "Selección inválida", "El destino no puede ser igual al origen.");
            return;
        }

        this.aeropuertoDestino = iata;
        Logger.logInfo("Destino de ruta maestra fijado desde mapa: " + iata);
    }

    public void reiniciarSeleccionRutaMaestra() {
        this.aeropuertoOrigen = null;
        this.aeropuertoDestino = null;
    }

    public void buscarVuelos() {
        String fechaStr = (this.fechaBusqueda != null) ? this.fechaBusqueda.toString() : null;
        this.vuelos = vueloService.buscarVuelo(aeropuertoOrigenBusqueda, aeropuertoDestinoBusqueda, fechaStr);
        Logger.logInfo("Registros de vuelo indexados para selección: " + vuelos.size());
        //actualizarVuelosMapaJson();
    }

    public Map<String, Aeropuerto> getAeropuertosMap() {
        return aeropuertosMap;
    }


    private void actualizarVuelosMapaJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<VueloMapaDTO> geometrias = vueloService.construirGeometriaVuelos(this.vuelos);
            // Marca en el JSON cuáles ya están vinculados, para pintarlos distinto en el mapa
            for (VueloMapaDTO g : geometrias) {
                boolean yaVinculado = itinerariosAsignados.stream()
                        .anyMatch(iv -> iv.getVuelo().getIdVuelo().equals(g.getIdVuelo()));
                g.setVinculado(yaVinculado);
            }
            this.vuelosMapaJson = mapper.writeValueAsString(geometrias);
            PrimeFaces.current().executeScript("renderVuelosMapa(" + this.vuelosMapaJson + ")");
        } catch (Exception e) {
            Logger.logInfo("Error serializando vuelos para mapa: " + e.getMessage());
            this.vuelosMapaJson = "[]";
        }
    }

    public void vincularVueloDesdeMapa() {
        String idVueloStr = FacesContext.getCurrentInstance().getExternalContext()
                .getRequestParameterMap().get("idVuelo");

        if (idVueloStr == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Operación Inválida", "No se pudo identificar el vuelo seleccionado.");
            return;
        }

        Integer idVuelo;
        try {
            idVuelo = Integer.valueOf(idVueloStr);
        } catch (NumberFormatException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Operación Inválida", "Identificador de vuelo no válido.");
            return;
        }

        Vuelo vuelo = this.vuelos.stream()
                .filter(v -> v.getIdVuelo().equals(idVuelo))
                .findFirst()
                .orElse(null);

        if (vuelo == null) {
            addMessage(FacesMessage.SEVERITY_WARN, "Operación Inválida", "El vuelo ya no está disponible en el listado actual.");
            return;
        }

        agregarVuelo(vuelo);
    }


    public boolean estaVinculado(Vuelo vuelo) {
        if (vuelo == null) return false;
        return itinerariosAsignados.stream()
                .anyMatch(iv -> iv.getVuelo().getIdVuelo().equals(vuelo.getIdVuelo()));
    }

    public String getResumenSecuencia() {
        if (itinerariosAsignados == null || itinerariosAsignados.isEmpty()) {
            return "El itinerario no tiene vuelos asignados.";
        }

        // Origen del primer segmento del primer vuelo
        String origenPrimero = itinerariosAsignados.get(0).getVuelo()
                .getSegmentoVuelos().get(0).getAeropuertoOrigen().getCodigoIata();

        // Destino del último segmento del último vuelo
        var ultimoVuelo = itinerariosAsignados.get(itinerariosAsignados.size() - 1).getVuelo();
        String destinoUltimo = ultimoVuelo.getSegmentoVuelos()
                .get(ultimoVuelo.getSegmentoVuelos().size() - 1)
                .getAeropuertoDestino().getCodigoIata();

        boolean coincideOrigen = origenPrimero.equalsIgnoreCase(this.aeropuertoOrigen);
        boolean coincideDestino = destinoUltimo.equalsIgnoreCase(this.aeropuertoDestino);

        if (coincideOrigen && coincideDestino) {
            return "OK";
        }

        if (!coincideOrigen && !coincideDestino) {
            return String.format("Incoherencia total: La secuencia enlazada inicia en %s y termina en %s, pero la ruta maestra requiere %s → %s.",
                    origenPrimero, destinoUltimo, this.aeropuertoOrigen, this.aeropuertoDestino);
        } else if (!coincideOrigen) {
            return String.format("Origen incoherente: El primer tramo inicia en %s, pero el origen planificado en la ruta maestra es %s.",
                    origenPrimero, this.aeropuertoOrigen);
        } else {
            return String.format("Destino incompleto: El último tramo finaliza en %s, pero el destino final planificado es %s.",
                    destinoUltimo, this.aeropuertoDestino);
        }
    }

    public int getNumeroEscalasCalculado() {
        return Math.max(0, itinerariosAsignados.size() - 1);
    }


    // Reconstruye y empuja al cliente la geometría del itinerario tal como va quedando armado
    private void actualizarItinerarioArmadoMapa() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Vuelo> vuelosEnOrden = itinerariosAsignados.stream()
                    .sorted(Comparator.comparingInt(ItinerarioVuelo::getOrden))
                    .map(ItinerarioVuelo::getVuelo)
                    .toList();

            List<VueloMapaDTO> geometrias = vueloService.construirGeometriaVuelos(vuelosEnOrden);
            this.itinerarioArmadoMapaJson = mapper.writeValueAsString(geometrias);

            Logger.logInfo(itinerarioArmadoMapaJson);

            // Si es una petición AJAX (ej. vincular un vuelo), ejecutamos el script dinámicamente:
            if (PrimeFaces.current().isAjaxRequest()) {
                String script = String.format("renderItinerarioArmado(%s);", mapper.writeValueAsString(this.itinerarioArmadoMapaJson));
                PrimeFaces.current().executeScript(script);
            }
        } catch (Exception e) {
            Logger.logInfo("Error serializando itinerario armado para mapa: " + e.getMessage());
            this.itinerarioArmadoMapaJson = "[]";
        }
    }

    public String getAeropuertosMapaJson() { return aeropuertosMapaJson; }
    public String getVuelosMapaJson() { return vuelosMapaJson; }

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