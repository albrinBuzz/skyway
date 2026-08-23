package com.SkyWay.views.reserva;



import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
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

    @Autowired
    private HttpSession session;
    Usuario usuario;
    String miSessionId;

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
        // Aquí guardas la reserva en BD o llamas al servicio
        //System.out.println("Reserva confirmada para: " + cliente.getNombre());

         miSessionId = (String) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("reservaSessionId");

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : asientosSeleccionados.entrySet()) {
            Integer idVuelo = entry.getKey();
            List<InfoAsientoDTO> asientos = entry.getValue();

            for (InfoAsientoDTO asiento : asientos) {
                boolean sigueValido = asientoCacheService.validarPertenenciaYSeleccion(idVuelo, asiento.getIdAsiento(), miSessionId);
                if (!sigueValido) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "Tiempo Agotado",
                            "Tu reserva temporal expiró. El asiento " + asiento.getNumeroAsiento() + " fue liberado.");

                    // Redirigir de vuelta a la selección de asientos
                    try {
                        FacesContext.getCurrentInstance().getExternalContext().redirect("/home/reservaAsiento.xhtml?error=expired");
                    } catch (IOException e) {
                        Logger.logInfo("Error al redirigir: " + e.getMessage());
                    }
                    return; // Cancelar confirmación y no tocar la BD
                }
            }
        }


        boolean resultadoDisp = false;
        List<String> erroresDisponibilidad = new ArrayList<>();

        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : asientosSeleccionados.entrySet()) {
            Integer idVuelo = entry.getKey();
            List<InfoAsientoDTO> asientos = entry.getValue();

            for (InfoAsientoDTO asiento : asientos) {
                Logger.logInfo("Vuelo: " + idVuelo + ", Asiento: " + asiento.getNumeroAsiento());

                Integer[] asientosIds = { asiento.getIdAsiento() };

                try {
                    Logger.logInfo(Arrays.toString(asientosIds));
                    // Llamada al servicio para verificar disponibilidad
                    var resultado = asientoService.verificarDisponibilidad(idVuelo, asientosIds);
                    Logger.logInfo(resultado);
                    if (resultado.isEmpty()||resultado.isBlank()) {
                        resultadoDisp = true;  // Marcar como disponible si no hay error
                        break;
                    }

                } catch (SQLException e) {
                    Logger.logInfo("Error SQL en la reserva. verificando disponibilidad: " + e.getMessage());
                    resultadoDisp = false;
                    break;
                    // Otros manejos de errores...
                } catch (Exception ex) {
                    Logger.logInfo("Error inesperado: " + ex.getClass().getName());
                    Logger.logInfo("Error inesperado: " + ex.getMessage());
                    erroresDisponibilidad.add("Asiento " + asiento.getNumeroAsiento() + ": " + ex.getMessage());
                    resultadoDisp = false;
                    break;
                    // Otros manejos de excepciones...
                }
            }
            if (resultadoDisp) break;
        }
        if (!erroresDisponibilidad.isEmpty()) {
            // Mostramos todos los errores acumulados en la interfaz
            for (String error : erroresDisponibilidad) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Disponibilidad", error));
            }
            return; // Detenemos la reserva
        }

        Logger.logInfo(String.valueOf(resultadoDisp));
        if (resultadoDisp) {

            Logger.logInfo("confirma la reserva");


            var reserva = new Reserva();
            if (usuario == null) {
                Logger.logInfo("No autenticado");
                for (int i = 0; i < pasajerosList.size(); i++) {
                    Pasajero pasajero = pasajerosList.get(i);


                    if (i == 0) { // pasajeros adicionales
                        Usuario usuario = pasajero.getUsuario();
                        // Asegurarse de que el rut esté asignado en Usuario
                        usuario.setRut(pasajero.getRut());

                        // Asignar usuario al pasajero
                        pasajero.setUsuario(usuario);

                        // Guardar el pasajero (cascade se encargará de guardar Usuario)
                        pasajeroService.save(pasajero);
                        reserva.setPasajero(pasajero);

                    }else{
                        Usuario usuario = pasajero.getUsuario();

                        // Asegurarse de que el rut esté asignado en Usuario
                        usuario.setRut(pasajero.getRut());

                        // Asignar usuario al pasajero
                        pasajero.setUsuario(usuario);

                        // Guardar el pasajero (cascade se encargará de guardar Usuario)
                        pasajeroService.save(pasajero);
                    }
                }
            } else {

                Logger.logInfo("Autenticado");
                /*this.pasajero = pasajeroService.findById(usuario.getRut())
                        .orElseThrow(() -> new RuntimeException("Pasajero no encontrado"));*/

                // 2. Asignamos la instancia oficial a la reserva
                reserva.setPasajero(this.pasajero);



                for (int i = 0; i < pasajerosList.size(); i++) {
                    Pasajero pLista = pasajerosList.get(i);

                    // Si es el pasajero autenticado, no hacemos nada, ya está en la DB
                    if (i == 0 || pLista.getRut().equals(this.pasajero.getRut())) {
                        continue;
                    } else {
                        // Para pasajeros adicionales, verifica si ya existen antes de salvar
                        // para evitar el error de Duplicate ID
                        Usuario userAdicional = pLista.getUsuario();
                        userAdicional.setRut(pLista.getRut());
                        pLista.setUsuario(userAdicional);

                        // IMPORTANTE: Solo guarda si estás seguro de que es nuevo
                        // o usa un método que haga merge en el service
                        if (!pasajeroService.findById(pLista.getRut()).isPresent()) {
                            pasajeroService.save(pLista);
                        }
                    }
                }

            }


            var estatus = estadoReservaService.findById(2).get();

            //var pasajero=pasajeroService.findById("12345678-9").get();

            reserva.setEstadoReservaBean(estatus);

            reserva.setTotal(new BigDecimal(total));
            reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

            var reservaGuardada = reservaService.save(reserva);
            AtomicReference<String> mensaje = new AtomicReference<>();

            asientosSeleccionados.forEach((idVuelo, asientos) -> {
                List<Integer> idsConfirmados = new ArrayList<>();
                asientos.forEach(asiento -> {
                    //Logger.logInfo("Vuelo: " + idVuelo + ", Asiento: " + asiento.getNumeroAsiento());

                    Integer[] asientosIds = {asiento.getIdAsiento()};


                    try {
                        //Logger.logInfo(reserva.toString());
                        ///Logger.logInfo(pasajero.getRut());
                        //Logger.logInfo(String.valueOf(asiento.getIdAsiento()));

                        var rut=getRutPasajero(asiento.getIdAsiento(),asiento.getNumeroAsiento());

                        Logger.logInfo(rut+"->"+asiento.getIdAsiento()+"->"+asiento.getNumeroAsiento());
                        //Logger.logInfo("ID de reserva antes de llamar al procedimiento: " + reservaGuardada.getIdReserva());
                        //String mensaje = reservaService.confirmarReserva(idVuelo, asientos, "12345678-9",reservaGuardada.getIdReserva());
                        mensaje.set(reservaService.confirmarReserva(idVuelo, asientosIds, rut, reservaGuardada.getIdReserva()));

                        idsConfirmados.add(asiento.getIdAsiento());

                        //Logger.logInfo(mensaje.get());


                    } catch (Exception ex) {
                        Logger.logInfo("Error inesperado: " + ex.getMessage());
                        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error inesperado", ex.getMessage());
                        //PrimeFaces.current().dialog().showMessageDynamic(message);

                        addMessage(FacesMessage.SEVERITY_ERROR, "Error En la reserva", ex.getMessage());
                    }
                });

                if (!idsConfirmados.isEmpty()) {
                    asientoCacheService.confirmarReservaDefinitiva(idVuelo, idsConfirmados);
                }

            });

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje.get());
            PrimeFaces.current().dialog().showMessageDynamic(message);

            addMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje.get());

            Logger.logInfo("arreglar la asignacion de la tarifa, solo se setea un vaalor preestablecido");


            tarifasItinerarios.forEach((idItinerario, idTarifa) -> {

                var itinerario = itinerarioService.findById(idItinerario);
                ReservaItinerario rersv = new ReservaItinerario();
                rersv.setReserva(reserva);
                rersv.setItinerario(itinerario);
                Logger.logInfo(idItinerario + "->" + idTarifa);
                var tarifaItinerario = itinerarioTarifaService.getByTarifaAndItinerario(idItinerario, idTarifa);
                if (tarifaItinerario == null) {
                    Logger.logInfo("No se encontró ItinerarioTarifa para itinerario " + idItinerario + " y tarifa " + idItinerario);
                    throw new IllegalStateException("No se encontró ItinerarioTarifa para itinerario " + idItinerario + " y tarifa " + idTarifa);
                }
                rersv.setItinerarioTarifa(tarifaItinerario);

                Logger.logInfo(tarifaItinerario.toString());


                reservaItinerarioService.save(rersv);
            });



        /*for (Itinerario itinerario : itinerarios) {

            ReservaItinerario rersv=new ReservaItinerario();
            rersv.setReserva(reservaGuardada);
            rersv.setItinerario(itinerario);

            //esto corregir
            var tarifas=itinerarioTarifaService.findByItinerario(itinerario.getIdItinerario());
            rersv.setItinerarioTarifa(tarifas.get(0));

            reservaItinerarioService.save(rersv);

        }*/

        }else {
            Logger.logInfo("asientos no disponibles");
            addMessage(FacesMessage.SEVERITY_WARN, "Aviso", "Uno o más asientos ya no están disponibles.");
        }


            // Redirigir a página de éxito
        //return "reservaExitosa.xhtml?faces-redirect=true";
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
}
