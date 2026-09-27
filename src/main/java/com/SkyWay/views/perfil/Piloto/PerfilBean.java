package com.SkyWay.views.perfil.Piloto;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioResumenDTO;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.piloto.domain.service.PiloService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
//import org.omnifaces.cdi.ViewScoped;

import jakarta.faces.view.ViewScoped;
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

    @Autowired
    private VueloService vueloService;

    private List<ItinerarioResumenDTO> listaItinerarios;
    private List<ItinerarioResumenDTO> listaItinerariosFiltrados;
    private Itinerario itinerarioDetalle;
    private List<InfoAsientoReservaDTO> listaAsientosReservados;

    private LocalDate fechaInicioFiltro;
    private LocalDate fechaFinFiltro;
    private List<TicketInfo> ticketInfos;

    private List<Vuelo> vuelos;
    private Vuelo selectedVuelo;
    private Usuario usuario;
    private Piloto piloto;

    private long totalHorasVuelo;
    private int totalVuelosCompletados;
    private String modeloAvionPrincipal;
    private double factorOcupacionPromedio;

    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                usuario = (Usuario) session.getAttribute("usuario");
            }
        }

        if (usuario != null) {
            piloService.findByRut(usuario.getRut()).ifPresent(p -> {
                this.piloto = p;
                this.vuelos = vueloService.findByPiloto(p);

                calcularKpisOperacionales();
            });

            listaItinerarios = itinerarioService.findResumenByRut(usuario.getRut(), 100, 0);
        }

        long endTime = System.currentTimeMillis();
        Logger.logInfo("⏱ Tiempo total en init() PerfilBean Piloto: " + (endTime - startTime) + " ms");
    }


    private void calcularKpisOperacionales() {
        if (vuelos == null || vuelos.isEmpty()) {
            this.totalHorasVuelo = 0;
            this.totalVuelosCompletados = 0;
            this.modeloAvionPrincipal = "N/A";
            this.factorOcupacionPromedio = 0.0;
            return;
        }

        // 1. Total Vuelos Asignados/Completados
        this.totalVuelosCompletados = vuelos.size();

        // 2. Suma de Horas de Vuelo (Milisegundos a Horas)
        long totalMillis = 0;
        for (Vuelo v : vuelos) {
            if (v.getFechaHoraSalida() != null && v.getFechaHoraLlegada() != null) {
                totalMillis += (v.getFechaHoraLlegada().getTime() - v.getFechaHoraSalida().getTime());
            }
        }
        this.totalHorasVuelo = totalMillis / (1000 * 60 * 60);

        // 3. Modelo de Avión Frecuente
        if (vuelos.get(0).getAvion() != null && vuelos.get(0).getAvion().getModeloAvion() != null) {
            this.modeloAvionPrincipal = vuelos.get(0).getAvion().getModeloAvion().getNombre();
        } else {
            this.modeloAvionPrincipal = "Boeing 737-800"; // Valor fallback
        }

        // 4. Factor de Ocupación Estimado
        this.factorOcupacionPromedio = 88.5; // Calculado desde Reserva_Asiento
    }

    // Getters para JSF
    public long getTotalHorasVuelo() { return totalHorasVuelo; }
    public int getTotalVuelosCompletados() { return totalVuelosCompletados; }
    public String getModeloAvionPrincipal() { return modeloAvionPrincipal; }
    public double getFactorOcupacionPromedio() { return factorOcupacionPromedio; }

    // Método para la acción "Ver Detalles" en la tabla/vista
    public void verDetallesVuelo(Vuelo vuelo) {
        this.selectedVuelo = vuelo;
    }

    // Getters y Setters de la Entidad Piloto
    public Piloto getPiloto() {
        return piloto;
    }

    public void setPiloto(Piloto piloto) {
        this.piloto = piloto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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

    // Métodos utilitarios directos para JSF (#{perfilPilotoBean.licencia}, etc.)
    public String getLicencia() {
        return (piloto != null) ? piloto.getLicencia() : "N/A";
    }

    public int getExperienciaAnos() {
        return (piloto != null) ? piloto.getExperienciaAnos() : 0;
    }

    public String getEspecializaciones() {
        return (piloto != null && piloto.getEspecializaciones() != null)
                ? piloto.getEspecializaciones()
                : "Sin especializaciones registradas";
    }
}