package com.SkyWay.views.turno;


import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.service.TurnoTripulacionService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.annotation.PostConstruct;
//import jakarta.faces.view.ViewScoped;

import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.omnifaces.cdi.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class MisTurnosBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Injectamos el repositorio de Turno (el cual interactúa con la base de datos)
    @Autowired
    private TurnoTripulacionService turnoTripulacionService;
    @Autowired
    private HttpSession session;
    // Lista de turnos asignados al tripulante
    private List<TurnoTripulacion> turnosTripulante;

    // Turno seleccionado para mostrar detalles
    private TurnoTripulacion selectedTurno;

    // El tripulante actualmente logueado
    private Tripulacion tripulacion;
    Usuario usuario;

    // Método que carga los turnos al iniciar la página
    @PostConstruct
    public void init() {
        usuario = (Usuario) session.getAttribute("usuario");
        turnosTripulante=turnoTripulacionService.findByTripulacionRut(usuario.getRut());

        //cargarTurnos();
    }

    // Método para cargar los turnos asignados al tripulante
    public void cargarTurnos() {
        // Aquí asumimos que el tripulante ya está autenticado y tenemos acceso a su información
        // Debes reemplazar con la lógica para obtener el tripulante autenticado
        this.tripulacion = obtenerTripulanteAutenticado();

        // Recuperamos los turnos asignados a este tripulante desde el repositorio
        //this.turnosTripulante = turnoRepository.findByTripulacionRut(tripulacion.getRut());

    }

    // Método para obtener un tripulante autenticado (esto puede depender de tu sistema de autenticación)
    private Tripulacion obtenerTripulanteAutenticado() {
        // Aquí deberías agregar la lógica para obtener el tripulante autenticado
        // Por ejemplo, si usas una sesión o un contexto de seguridad
        return new Tripulacion(); // Este es un ejemplo, debes implementar la lógica real
    }

    // Getters y Setters
    public List<TurnoTripulacion> getTurnosTripulante() {
        return turnosTripulante;
    }

    public void setTurnosTripulante(List<TurnoTripulacion> turnosTripulante) {
        this.turnosTripulante = turnosTripulante;
    }

    public TurnoTripulacion getSelectedTurno() {
        return selectedTurno;
    }

    public void setSelectedTurno(TurnoTripulacion selectedTurno) {
        this.selectedTurno = selectedTurno;
    }

    public Tripulacion getTripulacion() {
        return tripulacion;
    }

    public void setTripulacion(Tripulacion tripulacion) {
        this.tripulacion = tripulacion;
    }

    // Método que se llama cuando el tripulante selecciona un turno para ver detalles
    public void verDetallesTurno(TurnoTripulacion turnoTripulacion) {
        this.selectedTurno = turnoTripulacion;
    }
}
