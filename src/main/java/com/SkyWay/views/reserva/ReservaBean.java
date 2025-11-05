package com.SkyWay.views.reserva;



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
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Named("reservaBean")
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
    private ItinerarioTarifaService itinerarioTarifaService;

    private Pasajero pasajero = new Pasajero();
    private HashMap<Integer,Integer>tarifasItinerarios;


    @Autowired
    private HttpSession session;



    // Simulamos una inyección de un servicio (puedes usar @Inject si usas CDI)
    // @Inject
    // private ReservaService reservaService;

    @PostConstruct
    public void init() {
        // En una app real podrías obtener esta info desde sesión, o un paso previo
        //this.cliente = new ClienteDTO("Juan Pérez", "juan@example.com", "123456789");
        total=0;

        pasajero.setUsuario(new Usuario());
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        var idItinerarios = (List<Integer>) sessionMap.get("itinerarios");

        tarifasItinerarios= (HashMap<Integer, Integer>) sessionMap.get("tarifasItinerios");

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

    // Acción del botón
    public String confirmarReserva() {
        // Aquí guardas la reserva en BD o llamas al servicio
        //System.out.println("Reserva confirmada para: " + cliente.getNombre());

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        var reserva=new Reserva();
        if (usuario==null){
            Logger.logInfo("no logeado");
            var usuarioGuardado= usuarioService.save(pasajero
                    .getUsuario());

            pasajero.setUsuario(usuarioGuardado);

            var pasajero= pasajeroService.save(this.pasajero);
            reserva.setPasajero(pasajero);
        }else {
            Logger.logInfo("logeado");
            this.pasajero= pasajeroService.findById(usuario.getRut()).get();
            reserva.setPasajero(pasajero);
        }


        var estatus=estadoReservaService.findById(2).get();

        //var pasajero=pasajeroService.findById("12345678-9").get();

        reserva.setEstadoReservaBean(estatus);

        reserva.setTotal(new BigDecimal(total));
        reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

        var reservaGuardada= reservaService.save(reserva);
        AtomicReference<String> mensaje = new AtomicReference<>();
        asientosSeleccionados.forEach((idVuelo, asientos) -> {
            asientos.forEach(asiento -> {
                Logger.logInfo("Vuelo: " + idVuelo + ", Asiento: " + asiento.getNumeroAsiento());

                Integer[]asientosIds={asiento.getIdAsiento()};


                try {
                    Logger.logInfo(reservaGuardada.toString());
                    Logger.logInfo(pasajero.getRut());
                    Logger.logInfo(Arrays.toString(asientosIds));
                    //Logger.logInfo("ID de reserva antes de llamar al procedimiento: " + reservaGuardada.getIdReserva());
                    //String mensaje = reservaService.confirmarReserva(idVuelo, asientos, "12345678-9",reservaGuardada.getIdReserva());
                     mensaje.set(reservaService.confirmarReserva(idVuelo, asientosIds, pasajero.getRut(), reservaGuardada.getIdReserva()));
                    Logger.logInfo(mensaje.get());



                } catch (SQLException e) {
                    Logger.logInfo("Error SQL en la reserva: " + e.getMessage());

                    FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error en la reserva", e.getMessage());
                    PrimeFaces.current().dialog().showMessageDynamic(message);
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

           var  itinerario=  itinerarioService.findById(idItinerario);
            ReservaItinerario rersv=new ReservaItinerario();
            rersv.setReserva(reservaGuardada);
            rersv.setItinerario(itinerario);
            Logger.logInfo(idItinerario+"->"+idTarifa);
            var tarifaItinerario = itinerarioTarifaService.getByTarifaAndItinerario(idItinerario,idTarifa);
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



            // Redirigir a página de éxito
        return "reservaExitosa.xhtml?faces-redirect=true";
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().
                addMessage(null, new FacesMessage(severity, summary, detail));
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
}
