package com.SkyWay.modules.reserva.presentation.bean;



import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        asientosSeleccionados.forEach((idVuelo, asiento) -> {
            System.out.println("Vuelo: " + idVuelo + ", Asiento: " + asiento.getNumeroAsiento());
        });

        // Redirigir a página de éxito
        return "reservaExitosa.xhtml?faces-redirect=true";
    }

    public List<Itinerario> getItinerarios() {
        return itinerarios;
    }

    public Integer getTotal() {
        return total;
    }

}
