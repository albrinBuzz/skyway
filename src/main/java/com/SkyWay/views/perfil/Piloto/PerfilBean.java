package com.SkyWay.views.perfil.Piloto;


import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioResumenDTO;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
//import jakarta.faces.view.ViewScoped;
import org.omnifaces.cdi.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Named("perfilPilotoBean")
@ViewScoped
public class PerfilBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ItinerarioService itinerarioService;

    @Autowired
    private PiloService piloService;
    @Autowired
    private ReservaService reservaService;

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private HttpSession session;

    private List<ItinerarioResumenDTO> listaItinerarios;             // Todos los itinerarios
    private List<ItinerarioResumenDTO> listaItinerariosFiltrados;    // Itinerarios filtrados
    private Itinerario itinerarioDetalle;
    private List<InfoAsientoReservaDTO> listaAsientosReservados;

    private LocalDate fechaInicioFiltro;
    private LocalDate fechaFinFiltro;
    private List<TicketInfo>ticketInfos;


    @Autowired
    private VueloService vueloService;

    private List<Vuelo> vuelos;
    private Vuelo selectedVuelo;
    Usuario usuario;
    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();


        usuario = (Usuario) session.getAttribute("usuario");



        vuelos=vueloService.findByPiloto(piloService.findByRut(usuario.getRut()).get());


                //reservaService.obtenerReservasPorRut(usuario.getRut());

                // Obtener resumen optimizado
                listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);

                long subEnd = System.currentTimeMillis();
                //Logger.logInfo("⏱ Tiempo en obtener los itinerarios: " + (subEnd - subStart) + " ms");

                // Filtro inicial
                //filtrarVuelosFuturos();
                ///filtrarVuelosHoy();


        long endTime = System.currentTimeMillis();
        Logger.logInfo("⏱ Tiempo total en init(): " + (endTime - startTime) + " ms");
    }

    public List<Vuelo> getVuelos() {
        return vuelos;
    }

    public void setVuelos(List<Vuelo> vuelos) {
        this.vuelos = vuelos;
    }

    public Vuelo getSelectedVuelo() {
        return selectedVuelo;
    }

    public void setSelectedVuelo(Vuelo selectedVuelo) {
        this.selectedVuelo = selectedVuelo;
    }
}
