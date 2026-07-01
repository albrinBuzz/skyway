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
import org.apache.juli.logging.Log;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.ls.LSInput;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Named("reservaBean")
@RequestScoped
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
    private ItinerarioTarifaService itinerarioTarifaService;

    private Pasajero pasajero = new Pasajero();
    private HashMap<Integer,Integer>tarifasItinerarios;
    List<ReservaAsientoBean.Pasajero> pasajeros;
    List<Pasajero>pasajerosList;

    @Autowired
    private HttpSession session;
    Usuario usuario;


    // Simulamos una inyección de un servicio (puedes usar @Inject si usas CDI)
    // @Inject
    // private ReservaService reservaService;

    @PostConstruct
    public void init() {
        // En una app real podrías obtener esta logInfo desde sesión, o un paso previo
        //this.cliente = new ClienteDTO("Juan Pérez", "juan@example.com", "123456789");
        total=0;

        usuario = (Usuario) session.getAttribute("usuario");

        pasajero.setUsuario(new Usuario());
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        var idItinerarios = (List<Integer>) sessionMap.get("itinerarios");

        tarifasItinerarios= (HashMap<Integer, Integer>) sessionMap.get("tarifasItinerios");

        pasajeros= (List<ReservaAsientoBean.Pasajero>) sessionMap.get("pasajeros");

        pasajerosList=new ArrayList<>();

        if (usuario!=null){
            this.pasajero = pasajeroService.findById(usuario.getRut()).get();

            pasajerosList = new ArrayList<>();
            for (int i = 0; i < pasajeros.size(); i++) {
                if (i == 0) {
                    // El primer pasajero de la lista ES el autenticado
                    pasajerosList.add(this.pasajero);
                } else {
                    // Pasajeros adicionales: Solo inicializar si son nuevos
                    Pasajero p = new Pasajero();
                    p.setUsuario(new Usuario());
                    pasajerosList.add(p);
                }
            }
        }else {
            if (pasajeros!=null){
                for (ReservaAsientoBean.Pasajero pasajero1 : pasajeros) {
                    Pasajero p = new Pasajero();
                    p.setUsuario(new Usuario()); // OBLIGATORIO
                    pasajerosList.add(p);
                }
            }


        }

        if (idItinerarios==null){
            return;
        }

        this.asientosSeleccionados = (Map<Integer, List<InfoAsientoDTO>>) sessionMap.get("asientosSeleccionados");

        for (Integer id : idItinerarios) {
            //Logger.logInfo(String.valueOf(id));
            var itinerario=itinerarioService.findById(id);
            //Logger.logInfo(itinerario.toString());
            total+=itinerario.getPrecioBase();
            itinerarios.add(itinerario);
        }
        var asientos= asientosSeleccionados.values();


        asientosSeleccionados.forEach((integer, asientoDTO) -> asientoDTO.forEach(infoAsientoDTO -> total+=infoAsientoDTO.getPrecio()));




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

                        //Logger.logInfo(mensaje.get());


                    } catch (Exception ex) {
                        Logger.logInfo("Error inesperado: " + ex.getMessage());
                        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error inesperado", ex.getMessage());
                        //PrimeFaces.current().dialog().showMessageDynamic(message);

                        addMessage(FacesMessage.SEVERITY_ERROR, "Error En la reserva", ex.getMessage());
                    }
                });


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
}
