package com.SkyWay.modules.reserva.presentation.bean;



import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import com.SkyWay.modules.estadoreserva.domain.service.EstadoReservaService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Named("reservaBean")
@ViewScoped
public class ReservaBean implements Serializable {

    //private ClienteDTO cliente;
    private Pasajero pasajero;
    private Map<Integer, InfoAsientoDTO> asientosSeleccionados;
    List<Itinerario> itinerarios=new ArrayList<>();
    private Integer total;

    @Autowired
    private ItinerarioService itinerarioService;
    @Autowired
    private VueloService vueloService;
    @Autowired
    private ReservaService reservaService;
    @Autowired
    private EstadoReservaService estadoReservaService;
    @Autowired
    private PasajeroService pasajeroService;


    // Simulamos una inyección de un servicio (puedes usar @Inject si usas CDI)
    // @Inject
    // private ReservaService reservaService;

    @PostConstruct
    public void init() {
        // En una app real podrías obtener esta info desde sesión, o un paso previo
        //this.cliente = new ClienteDTO("Juan Pérez", "juan@example.com", "123456789");
        total=0;
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        var idItinerarios = (List<Integer>) sessionMap.get("itinerarios");

        this.asientosSeleccionados = (Map<Integer, InfoAsientoDTO>) sessionMap.get("asientosSeleccionados");

        for (Integer id : idItinerarios) {
            //Logger.logInfo(String.valueOf(id));
            var itinerario=itinerarioService.findById(id);
            Logger.logInfo(itinerario.toString());
            total+=itinerario.getPrecioBase();
            itinerarios.add(itinerario);
        }
        var asientos= asientosSeleccionados.values();

        for (InfoAsientoDTO asiento : asientos) {
            Logger.logInfo(asiento.toString());
        }

        asientosSeleccionados.forEach((integer, asientoDTO) -> total+=asientoDTO.getPrecio());

        // Simulación de asientos seleccionados
        /*InfoAsientoDTO asiento1 = new InfoAsientoDTO("12A", "Económica", "Seleccionado");
        InfoAsientoDTO asiento2 = new InfoAsientoDTO("1C", "Primera Clase", "Seleccionado");

        asientosSeleccionados.put(101, asiento1); // 101 -> ID del vuelo 1
        asientosSeleccionados.put(102, asiento2); // 102 -> ID del vuelo 2*/
    }


    public Map<Integer, InfoAsientoDTO> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }

    // Acción del botón
    public String confirmarReserva() {
        // Aquí guardas la reserva en BD o llamas al servicio
        //System.out.println("Reserva confirmada para: " + cliente.getNombre());


        var estatus=estadoReservaService.findById(2).get();
        var pasajero=pasajeroService.findById("12345678-9").get();
        var reserva=new Reserva();
        reserva.setEstadoReservaBean(estatus);
        reserva.setPasajero(pasajero);
        reserva.setTotal(new BigDecimal(total));
        reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

        var reservaGuardada= reservaService.save(reserva);

        asientosSeleccionados.forEach((idVuelo, asiento) -> {
            Logger.logInfo("Vuelo: " + idVuelo + ", Asiento: " + asiento.getNumeroAsiento());

            Integer[]asientos={asiento.getIdAsiento()};


            try {
                //Logger.logInfo(reservaGuardada.toString());

                //Logger.logInfo("ID de reserva antes de llamar al procedimiento: " + reservaGuardada.getIdReserva());
                String mensaje = reservaService.confirmarReserva(idVuelo, asientos, "12345678-9",reservaGuardada.getIdReserva());
                Logger.logInfo(mensaje);

                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje);
                PrimeFaces.current().dialog().showMessageDynamic(message);

                addMessage(FacesMessage.SEVERITY_INFO, "Reserva confirmada", mensaje);

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

}
