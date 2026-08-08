package com.SkyWay.views;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.SkyWay.dto.InfoVueloDTO;
import com.SkyWay.modules.vuelo.domain.service.VueloService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;


@Named("flightBean")
@RequestScoped
@Component
public class FlightBean {

	@Autowired
    private VueloService vueloService;
	
    private  List<InfoVueloDTO> vuelosProx;
    private InfoVueloDTO selectedVuelo;

    // Constructor - Inicializa los vuelos (puedes cargar estos datos desde una base de datos)
    public FlightBean(VueloService vueloService) {
    	//vueloService=new VueloServiceImpl();
    	this.vueloService=vueloService;
    	//vuelosProx=vueloService.vuelosProximos();
    }

    // Métodos para manejar la reserva y ver detalles de los vuelos
    public String bookFlight(int vueloId) {
        // Simulamos la reserva (en una aplicación real, aquí iría la lógica para guardar la reserva)
        for (InfoVueloDTO vuelo : vuelosProx) {
            if (vuelo.getIdVuelo() == vueloId) {
                selectedVuelo = vuelo;
                // Aquí podrías redirigir a una página de confirmación o similar
                return "confirmation"; // O algún otro nombre de vista
            }
        }
        return null;
    }

    public String viewFlightDetails(int vueloId) {
        // Simulamos la visualización de detalles (aquí puedes agregar lógica para mostrar detalles)
        for (InfoVueloDTO vuelo : vuelosProx) {
            if (vuelo.getIdVuelo() == vueloId) {
                selectedVuelo = vuelo;
                // Podrías redirigir a una página de detalles
                return "flightDetails"; // Página de detalles del vuelo
            }
        }
        return null;
    }

    public void reserve(Integer idVuelo) {
    	System.out.println("ID de vuelo "+idVuelo);
    	
    }

    // Getters and Setters
    public List<InfoVueloDTO> getVuelosProx() {
        return vuelosProx;
    }

    public void setVuelosProx(List<InfoVueloDTO> vuelosProx) {
        this.vuelosProx = vuelosProx;
    }

    public InfoVueloDTO getSelectedVuelo() {
        return selectedVuelo;
    }

    public void setSelectedVuelo(InfoVueloDTO selectedVuelo) {
        this.selectedVuelo = selectedVuelo;
    }
}
