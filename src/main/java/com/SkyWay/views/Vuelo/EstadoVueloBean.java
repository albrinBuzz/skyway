package com.SkyWay.views.Vuelo;

import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.modules.vuelo.presentation.dto.VueloEstadoProjection;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.List;

@Named("estadoVueloBean")
@ViewScoped
public class EstadoVueloBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private VueloService vueloService;

    private String filtroNumeroVuelo;
    private String filtroRuta;

    private List<VueloEstadoProjection> listaVuelos;

    @PostConstruct
    public void init() {
        buscarVuelos();
    }

    public void buscarVuelos() {
        this.listaVuelos = vueloService.obtenerEstadoVuelosEnVivo(filtroNumeroVuelo, filtroRuta);
    }

    public void limpiarFiltros() {
        this.filtroNumeroVuelo = null;
        this.filtroRuta = null;
        buscarVuelos();
    }

    public String formatearHora(java.sql.Timestamp fechaHora) {
        if (fechaHora == null) return "--:-- Hrs";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(fechaHora) + " Hrs";
    }

    public String obtenerEstiloEstado(String estado) {
        if (estado == null) return "status-ontime";
        String estadoLower = estado.toLowerCase();

        if (estadoLower.contains("tiempo") || estadoLower.contains("programado") || estadoLower.contains("confirmado") || estadoLower.contains("a tiempo")) {
            return "status-ontime";
        } else if (estadoLower.contains("demorad") || estadoLower.contains("retrasad")) {
            return "status-delayed";
        } else if (estadoLower.contains("cancel")) {
            return "status-cancelled";
        }
        return "status-ontime";
    }

    public String getFiltroNumeroVuelo() { return filtroNumeroVuelo; }
    public void setFiltroNumeroVuelo(String filtroNumeroVuelo) { this.filtroNumeroVuelo = filtroNumeroVuelo; }

    public String getFiltroRuta() { return filtroRuta; }
    public void setFiltroRuta(String filtroRuta) { this.filtroRuta = filtroRuta; }

    public List<VueloEstadoProjection> getListaVuelos() { return listaVuelos; }
}