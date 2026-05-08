package com.SkyWay.views.turno;


import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.tripulacion.domain.service.TripulacionService;
import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turno.domain.service.TurnoService;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import com.SkyWay.modules.turnotripulacion.domain.service.TurnoTripulacionService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.apache.juli.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class TurnoBean {

    private String vueloId;
    private Vuelo vuelo;
    private List<Tripulacion> tripulantes;
    private String selectedTripulante;

    private List<TurnoTripulacion> turnosTripulacion;  // Lista de turnos de tripulantes asignados
    private Turno turno;


    @Autowired
    private TurnoService turnoService;
    @Autowired
    private TurnoTripulacionService turnoTripulacionService;

    @Autowired
    private TripulacionService tripulacionService;



    @PostConstruct
    public void init(){
        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        vueloId= externalContext.getRequestParameterMap().get("vueloId");

        //turnosTripulacion=new ArrayList<>();
        for (Turno turno1 : turnoService.findByVueloId(Integer.valueOf(vueloId))) {
            Logger.logInfo(turno1.toString());
        }
        turno=turnoService.findByVueloId(Integer.valueOf(vueloId)).get(0);
        turnosTripulacion= turno.getTurnoTripulacions1();
        tripulantes=tripulacionService.findAll();


    }

    public void asignarTripulanteAlTurno(){

        Logger.logInfo("asignado el turno");
        TurnoTripulacion turnoTripulacion=new TurnoTripulacion();

        var tripulante= tripulacionService.findByRut(selectedTripulante).get();
        turnoTripulacion.setTripulacion1(tripulante);

        turnoTripulacion.setTurno1(turno);
        turnoTripulacionService.save(turnoTripulacion);

        turnosTripulacion.add(turnoTripulacion);




    }
    public List<TurnoTripulacion> getTurnosTripulacion() {
        return turnosTripulacion;
    }

    public void setTurnosTripulacion(List<TurnoTripulacion> turnosTripulacion) {
        this.turnosTripulacion = turnosTripulacion;
    }



    public List<Tripulacion> getTripulantes() {
        return tripulantes;
    }

    public String getSelectedTripulante() {
        return selectedTripulante;
    }

    public void setTripulantes(List<Tripulacion> tripulantes) {
        this.tripulantes = tripulantes;
    }

    public void setSelectedTripulante(String selectedTripulante) {
        this.selectedTripulante = selectedTripulante;
    }

    public Turno getTurno() {
        return turno;
    }

    public void setTurno(Turno turno) {
        this.turno = turno;
    }
}
