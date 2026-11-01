package com.SkyWay.views.turno;

import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.tripulacion.domain.service.TripulacionService;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.service.TurnoTripulacionService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
//import org.omnifaces.cdi.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@Named("misTurnosBean")
@ViewScoped
public class MisTurnosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private TurnoTripulacionService turnoTripulacionService;

    @Autowired
    private TripulacionService tripulacionService;

    @Autowired
    private HttpSession session;

    private List<TurnoTripulacion> turnosTripulante;
    private TurnoTripulacion selectedTurno;
    private Tripulacion tripulacion;
    private Usuario usuario;
    private String motivoRechazoForm;
    private TurnoTripulacion turnoARechazar;

    @PostConstruct
    public void init() {
        //usuario = (Usuario) session.getAttribute("usuario");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            if (session != null) {
                usuario = (Usuario) session.getAttribute("usuario");
            }
        }

        if (usuario != null) {
            // Cargar datos específicos del tripulante
            tripulacionService.findByRut(usuario.getRut()).ifPresent(t -> this.tripulacion = t);

            // Cargar turnos asignados
            cargarTurnos();
        }
    }

    public void cargarTurnos() {
        if (usuario != null) {
            this.turnosTripulante = turnoTripulacionService.findByTripulacionRut(usuario.getRut());
        }
    }

    public void verDetallesTurno(TurnoTripulacion turnoTripulacion) {
        this.selectedTurno = turnoTripulacion;
    }

    public void prepararRechazo(TurnoTripulacion turno) {
        this.turnoARechazar = turno;
        this.motivoRechazoForm = "";
    }

    public void ejecutarRechazo() {
        if (this.turnoARechazar == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se ha seleccionado ningún turno para rechazar."));
            return;
        }

        if (this.motivoRechazoForm == null || this.motivoRechazoForm.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Motivo Requerido",
                            "Debe ingresar el motivo justificado de rechazo del servicio."));
            return;
        }

        try {
            turnoTripulacionService.rechazarTurno(this.turnoARechazar.getIdTurnoTripulacion(), this.motivoRechazoForm.trim());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Turno Rechazado",
                            "Has registrado el rechazo del turno #" + this.turnoARechazar.getIdTurnoTripulacion() +
                                    ". Operaciones ha sido notificado para la reasignación."));
            cargarTurnos();
            this.turnoARechazar = null;
            this.motivoRechazoForm = "";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al Rechazar", e.getMessage()));
            Logger.logInfo(e.getMessage());
        }
    }

    public void confirmarAsistencia(TurnoTripulacion turno) {
        try {
            if (turno != null && turno.getIdTurnoTripulacion() != null) {
                turnoTripulacionService.confirmarTurno(turno.getIdTurnoTripulacion());
            }
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Turno Confirmado",
                            "Has confirmado tu asistencia para el turno #" + turno.getIdTurnoTripulacion()));
            cargarTurnos();
        } catch (Exception e) {
            Logger.logInfo(e.getMessage());
        }
    }

    public void rechazarTurno(TurnoTripulacion turno) {
        prepararRechazo(turno);
    }

    // Getters y Setters
    public List<TurnoTripulacion> getTurnosTripulante() { return turnosTripulante; }
    public void setTurnosTripulante(List<TurnoTripulacion> turnosTripulante) { this.turnosTripulante = turnosTripulante; }

    public TurnoTripulacion getSelectedTurno() { return selectedTurno; }
    public void setSelectedTurno(TurnoTripulacion selectedTurno) { this.selectedTurno = selectedTurno; }

    public Tripulacion getTripulacion() { return tripulacion; }
    public void setTripulacion(Tripulacion tripulacion) { this.tripulacion = tripulacion; }

    public Usuario getUsuario() { return usuario; }

    public String getMotivoRechazoForm() { return motivoRechazoForm; }
    public void setMotivoRechazoForm(String motivoRechazoForm) { this.motivoRechazoForm = motivoRechazoForm; }

    public TurnoTripulacion getTurnoARechazar() { return turnoARechazar; }
    public void setTurnoARechazar(TurnoTripulacion turnoARechazar) { this.turnoARechazar = turnoARechazar; }
}