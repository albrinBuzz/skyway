package com.SkyWay.views.reserva;



import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.estadoreserva.domain.service.EstadoReservaService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Named("reservaBean")
//@RequestScoped
@ViewScoped
public class ReservaBean implements Serializable {

    //private ClienteDTO cliente;
    private Map<Integer, List<InfoAsientoDTO>> asientosSeleccionados;
    List<Itinerario> itinerarios=new ArrayList<>();
    private HashMap<Integer,List<Integer>> itinerariosAsientos=new HashMap<>();
    private Integer total;

    @Autowired
    private ItinerarioService itinerarioService;
    @Autowired
    private VueloService vueloService;
    @Autowired
    private ReservaService reservaService;
    @Autowired
    private ReservaItinerarioService reservaItinerarioService;
    @Autowired
    private EstadoReservaService estadoReservaService;
    @Autowired
    private PasajeroService pasajeroService;
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TarifaService tarifaService;
    @Autowired
    private AsientoService asientoService;

    @Autowired
    private AsientoCacheService asientoCacheService;

    @Autowired
    private ItinerarioTarifaService itinerarioTarifaService;

    private Pasajero pasajero = new Pasajero();
    private HashMap<Integer,Integer>tarifasItinerarios;
    List<ReservaAsientoBean.Pasajero> pasajeros;
    List<Pasajero>pasajerosList;

    private List<EquipajePasajeroDTO> equipajePorPasajero;
    private int totalEquipaje = 0;

    @Autowired
    private HttpSession session;
    Usuario usuario;
    String miSessionId;
    @Autowired
    private EquipajeService equipajeService;

    // Simulamos una inyección de un servicio (puedes usar @Inject si usas CDI)
    // @Inject
    // private ReservaService reservaService;
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        total = 0;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) return;

        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();


        // Obtener usuario autenticado o ID de sesión de manera SEGURA
        this.usuario = (Usuario) sessionMap.get("usuario");

        if (sessionMap.containsKey("reservaSessionId")) {
            this.miSessionId = (String) sessionMap.get("reservaSessionId");
        } else {
            HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(false);
            if (httpSession != null) {
                this.miSessionId = httpSession.getId();
                sessionMap.put("reservaSessionId", this.miSessionId);
            }
        }



        var idItinerarios = (List<Integer>) sessionMap.get("itinerarios");
        this.tarifasItinerarios = (HashMap<Integer, Integer>) sessionMap.get("tarifasItinerios");
        this.pasajeros = (List<ReservaAsientoBean.Pasajero>) sessionMap.get("pasajeros");

        pasajerosList = new ArrayList<>();

        if (usuario != null) {
            this.pasajero = pasajeroService.findById(usuario.getRut()).orElse(new Pasajero());
            if (pasajeros != null) {
                for (int i = 0; i < pasajeros.size(); i++) {
                    if (i == 0) {
                        pasajerosList.add(this.pasajero);
                    } else {
                        Pasajero p = new Pasajero();
                        p.setUsuario(new Usuario());
                        pasajerosList.add(p);
                    }
                }
            }
        } else if (pasajeros != null) {
            for (ReservaAsientoBean.Pasajero p1 : pasajeros) {
                Pasajero p = new Pasajero();
                p.setUsuario(new Usuario());
                pasajerosList.add(p);
            }
        }

        if (idItinerarios == null) return;

        this.asientosSeleccionados = (Map<Integer, List<InfoAsientoDTO>>) sessionMap.get("asientosSeleccionados");

        for (Integer id : idItinerarios) {
            var itinerario = itinerarioService.findById(id);
            if (itinerario != null) {
                total += itinerario.getPrecioBase();
                itinerarios.add(itinerario);
            }
        }

        if (asientosSeleccionados != null) {
            asientosSeleccionados.forEach((idVuelo, listaAsientos) -> {
                if (listaAsientos != null) {
                    listaAsientos.forEach(a -> total += a.getPrecio());
                }
            });
        }

        this.equipajePorPasajero = (List<EquipajePasajeroDTO>) sessionMap.get("equipajePorPasajero");

        if (sessionMap.containsKey("totalEquipaje")) {
            this.totalEquipaje = (Integer) sessionMap.get("totalEquipaje");
            this.total += this.totalEquipaje; // Acumular al costo final del vuelo
        }

    }


    public Map<Integer, List<InfoAsientoDTO>> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }


    public void mostrarPasajeros(){
        for (Pasajero pasajero1 : pasajerosList) {
            Logger.logInfo(pasajero1.toString());
        }
    }

    // Acción del botón
    public void confirmarReserva() {

        miSessionId = (String) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("reservaSessionId");


        // 1. VALIDACIÓN EN CACHÉ (TTL)

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : asientosSeleccionados.entrySet()) {
            Integer idVuelo = entry.getKey();
            List<InfoAsientoDTO> asientos = entry.getValue();

            for (InfoAsientoDTO asiento : asientos) {
                boolean sigueValido = asientoCacheService.validarPertenenciaYSeleccion(idVuelo, asiento.getIdAsiento(), miSessionId);


                if (!sigueValido) {

                    addMessage(FacesMessage.SEVERITY_ERROR, "Tiempo Agotado",
                            "Tu reserva temporal expiró. El asiento " + asiento.getNumeroAsiento() + " fue liberado.");

                    try {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("/home/reservaAsiento.xhtml?error=expired");
                    } catch (IOException e) {
                        Logger.logInfo("Error al redirigir: " + e.getMessage());
                    }
                    return; // Cancelar confirmación
                }
            }
        }

        // 2. VERIFICACIÓN DE DISPONIBILIDAD EN BD

        boolean resultadoDisp = false;
        List<String> erroresDisponibilidad = new ArrayList<>();

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : asientosSeleccionados.entrySet()) {
            Integer idVuelo = entry.getKey();
            List<InfoAsientoDTO> asientos = entry.getValue();

            for (InfoAsientoDTO asiento : asientos) {


                Integer[] asientosIds = { asiento.getIdAsiento() };

                try {

                    var resultado = asientoService.verificarDisponibilidad(idVuelo, asientosIds);

                    if (resultado == null || resultado.isEmpty() || resultado.isBlank()) {
                        resultadoDisp = true;
                        break;
                    }

                } catch (SQLException e) {
                    Logger.logInfo("❌ [DISPONIBILIDAD] Error SQL: " + e.getMessage());
                    resultadoDisp = false;
                    break;
                } catch (Exception ex) {
                    Logger.logInfo("❌ [DISPONIBILIDAD] Error inesperado (" + ex.getClass().getName() + "): " + ex.getMessage());
                    erroresDisponibilidad.add("Asiento " + asiento.getNumeroAsiento() + ": " + ex.getMessage());
                    resultadoDisp = false;
                    break;
                }
            }
            if (resultadoDisp) break;
        }

        if (!erroresDisponibilidad.isEmpty()) {
            for (String error : erroresDisponibilidad) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Disponibilidad", error));
            }
            return;
        }


        // 3. CONFIRMACIÓN Y PERSISTENCIA
        if (resultadoDisp) {

            var reserva = new Reserva();
            if (usuario == null) {
                for (int i = 0; i < pasajerosList.size(); i++) {
                    Pasajero pasajero = pasajerosList.get(i);

                    if (equipajePorPasajero != null && i < equipajePorPasajero.size()) {
                        equipajePorPasajero.get(i).setRutPasajero(pasajero.getRut());
                    }

                    Usuario usr = pasajero.getUsuario();
                    if (usr == null) {
                        usr = new Usuario();
                    }
                    usr.setRut(pasajero.getRut());
                    pasajero.setUsuario(usr);

                    pasajeroService.save(pasajero);

                    if (i == 0) {
                        reserva.setPasajero(pasajero);
                    }
                }
            } else {
                reserva.setPasajero(this.pasajero);

                for (int i = 0; i < pasajerosList.size(); i++) {
                    Pasajero pLista = pasajerosList.get(i);
                    if (equipajePorPasajero != null && i < equipajePorPasajero.size()) {
                        equipajePorPasajero.get(i).setRutPasajero(pLista.getRut());
                    }

                    if (i == 0 || pLista.getRut().equals(this.pasajero.getRut())) {
                        continue;
                    } else {
                        Usuario userAdicional = pLista.getUsuario();
                        if (userAdicional == null) {
                            userAdicional = new Usuario();
                        }
                        userAdicional.setRut(pLista.getRut());
                        pLista.setUsuario(userAdicional);

                        if (!pasajeroService.findById(pLista.getRut()).isPresent()) {
                            pasajeroService.save(pLista);
                        }
                    }
                }
            }

            var estatus = estadoReservaService.findById(2).get();
            reserva.setEstadoReservaBean(estatus);
            reserva.setTotal(new BigDecimal(total));
            reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

            var reservaGuardada = reservaService.save(reserva);

            AtomicReference<String> mensaje = new AtomicReference<>("Reserva registrada correctamente.");

            // 4. CONFIRMAR ASIENTOS
            asientosSeleccionados.forEach((idVuelo, asientos) -> {
                List<Integer> idsConfirmados = new ArrayList<>();
                asientos.forEach(asiento -> {
                    Integer[] asientosIds = {asiento.getIdAsiento()};
                    try {
                        var rut = getRutPasajero(asiento.getIdAsiento(), asiento.getNumeroAsiento());


                        String resMensaje = reservaService.confirmarReserva(idVuelo, asientosIds, rut, reservaGuardada.getIdReserva());
                        mensaje.set(resMensaje);
                        idsConfirmados.add(asiento.getIdAsiento());


                    } catch (Exception ex) {
                        Logger.logInfo("❌ [ASIENTOS] Error al confirmar asiento ID " + asiento.getIdAsiento() + ": " + ex.getMessage());
                        addMessage(FacesMessage.SEVERITY_ERROR, "Error En la reserva", ex.getMessage());
                    }
                });

                if (!idsConfirmados.isEmpty()) {
                    asientoCacheService.confirmarReservaDefinitiva(idVuelo, idsConfirmados);
                }
            });

            // 5. ITINERARIOS Y TARIFAS

            tarifasItinerarios.forEach((idItinerario, idTarifa) -> {
                var itinerario = itinerarioService.findById(idItinerario);
                ReservaItinerario rersv = new ReservaItinerario();
                rersv.setReserva(reservaGuardada);
                rersv.setItinerario(itinerario);


                var tarifaItinerario = itinerarioTarifaService.getByTarifaAndItinerario(idItinerario, idTarifa);
                if (tarifaItinerario == null) {

                    throw new IllegalStateException("No se encontró ItinerarioTarifa para itinerario " + idItinerario + " y tarifa " + idTarifa);
                }
                rersv.setItinerarioTarifa(tarifaItinerario);
                reservaItinerarioService.save(rersv);

            });

            // 6. PERSISTENCIA DE EQUIPAJE
            if (equipajePorPasajero != null && !equipajePorPasajero.isEmpty()) {
                int totalEquipajesGuardados = 0;

                for (EquipajePasajeroDTO dto : equipajePorPasajero) {
                    if (dto.getMaletas() != null) {
                        for (EquipajePasajeroDTO.ItemEquipaje item : dto.getMaletas()) {
                            Equipaje equipaje = new Equipaje();
                            equipaje.setPeso(item.getPeso());
                            equipaje.setDimensiones(item.getDimensiones());
                            equipaje.setTipo(item.getTipo());


                            equipajeService.guardar(equipaje, reservaGuardada.getIdReserva(), dto.getRutPasajero(), item.getIdTipo());
                            totalEquipajesGuardados++;
                        }
                    }
                }
            }

            // 7. FINALIZACIÓN Y NOTIFICACIÓN AL USUARIO
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje.get());
            PrimeFaces.current().dialog().showMessageDynamic(message);

            addMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje.get());


        } else {
            Logger.logInfo("⚠️ [CONFIRMAR_RESERVA] La confirmación fue rechazada: Asientos no disponibles.");
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Uno o más asientos ya no están disponibles.");
        }
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().
                addMessage(null, new FacesMessage(severity, summary, detail));
    }


    public void cancelarPorExpiracion() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        // Limpiar selección previa
        sessionMap.remove("asientosSeleccionados");

        Logger.logInfo("⚠️ Reserva cancelada automáticamente en checkout por expiración de TTL.");
    }

    public String getAsientoPasajero(int idAsiento,String numeroAsiento){
        for (ReservaAsientoBean.Pasajero pasajero1 : pasajeros) {

            for (ReservaAsientoBean.AsientoSeleccionado asiento : pasajero1.getAsientos()) {

                if (asiento.getIdAsiento()==idAsiento&&asiento.getNumeroAsiento().equals(numeroAsiento)){
                    return pasajero1.getNombre();
                }
            }

        }
        return "";
    }



    public long getTiempoRestanteSegundos() {
        if (miSessionId == null || miSessionId.isBlank()) {
            return 0;
        }
        long restanteMs = asientoCacheService.getTiempoRestanteMsParaSesion(miSessionId);
        return restanteMs > 0 ? (restanteMs / 1000) : 0;
    }

    /**
     * Genera la URL dinámica para retornar a la selección de asientos preservando los parámetros de búsqueda.
     */
    public String getUrlRetornoAsientos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String query = "";

        if (context != null && context.getExternalContext() != null) {
            String savedQuery = (String) context.getExternalContext().getSessionMap().get("ultimaBusquedaAsientosQuery");
            if (savedQuery != null && !savedQuery.isBlank()) {
                query = "?" + savedQuery + "&expired=true";
            } else {
                query = "?expired=true";
            }
        }

        return "/home/seleccionAsientos.xhtml" + query;
    }


    /**
     * Retorna la suma total de asientos seleccionados de todos los vuelos de forma segura.
     */
    public int getTolerantTotalAsientos() {
        if (asientosSeleccionados == null || asientosSeleccionados.isEmpty()) {
            return 0;
        }
        int totalCount = 0;
        for (List<InfoAsientoDTO> lista : asientosSeleccionados.values()) {
            if (lista != null) {
                totalCount += lista.size();
            }
        }
        return totalCount;
    }

    public String getRutPasajero(int idAsiento,String numeroAsiento){

        for (int i = 0; i < pasajeros.size(); i++) {

            for (int i1 = 0; i1 < pasajeros.get(i).asientos.size(); i1++) {

                if (pasajeros.get(i).asientos.get(i1).idAsiento==idAsiento&&pasajeros.get(i).asientos.get(i1).numeroAsiento.equals(numeroAsiento)){

                   return pasajerosList.get(i).getRut();
                }
            }
        }

        return "";
    }

    public String getVuelo(Integer idVuelo){
        return vueloService.findById(idVuelo).get().getNumeroVuelo();
    }

    public List<Itinerario> getItinerarios() {
        return itinerarios;
    }

    public Integer getTotal() {
        return total;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public void setPasajero(Pasajero pasajero) {
        this.pasajero = pasajero;
    }

    public List<ReservaAsientoBean.Pasajero> getPasajeros() {
        return pasajeros;
    }

    public List<Pasajero> getPasajerosList() {
        return pasajerosList;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getMiSessionId() {
        return miSessionId;
    }

    public void setMiSessionId(String miSessionId) {
        this.miSessionId = miSessionId;
    }

    public List<EquipajePasajeroDTO> getEquipajePorPasajero() {
        return equipajePorPasajero;
    }

    public int getTotalEquipaje() {
        return totalEquipaje;
    }

    /**
     * Retorna verdadero si al menos un pasajero tiene equipaje asignado
     */
    public boolean isTieneEquipajeAsignado() {
        if (equipajePorPasajero == null || equipajePorPasajero.isEmpty()) {
            return false;
        }
        return equipajePorPasajero.stream()
                .anyMatch(dto -> dto.getMaletas() != null && !dto.getMaletas().isEmpty());
    }


}
