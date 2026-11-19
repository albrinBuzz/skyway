package com.SkyWay.views.home;

import com.SkyWay.modules.aeropuerto.presentation.dto.PaisDestinosDTO;
import com.SkyWay.modules.avion.presentation.dto.FlotaResumenDTO;
import com.SkyWay.modules.itinerario.presentation.dto.RutaDestacadaDTO;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import com.SkyWay.modules.vuelo.presentation.dto.VueloProgramadoPortalDTO;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Named("homePortalBean")
@ViewScoped
public class HomePortalBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HomePortalFacadeService homePortalFacadeService;
    private HomePortalFacadeService.PortalSnapshot snapshot;
    private String articuloActivo = "red-rutas";

    @Autowired
    public HomePortalBean(HomePortalFacadeService homePortalFacadeService) {
        this.homePortalFacadeService = homePortalFacadeService;
    }

    @PostConstruct
    public void init() {
        cargarArticuloDesdeUrl();
        this.snapshot = homePortalFacadeService.obtenerSnapshot();
    }

    private void cargarArticuloDesdeUrl() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && fc.getExternalContext() != null) {
            String param = fc.getExternalContext().getRequestParameterMap().get("articulo");
            if (param != null && !param.isBlank()) {
                this.articuloActivo = normalizarArticulo(param);
            }
        }
    }

    public void seleccionarArticulo(String slug) {
        this.articuloActivo = normalizarArticulo(slug);
    }

    private String normalizarArticulo(String slug) {
        if ("tarifas-equipaje".equals(slug) || "flota-cabinas".equals(slug) || "checkin-embarque".equals(slug)) {
            return slug;
        }
        return "red-rutas";
    }

    public String getLogoMetodoPago(MetodoPago mp) {
        if (mp == null || mp.getDescripcion() == null) return "";
        String d = mp.getDescripcion().toLowerCase();
        if (d.contains("webpay") || d.contains("transbank")) return "assets/icons/logo_webpay.png";
        if (d.contains("paypal")) return "assets/icons/logo_paypal.png";
        return "";
    }

    public String getNombreAerolinea() { return snapshot.nombreAerolinea; }
    public String getCodigoAerolinea() { return snapshot.codigoAerolinea; }
    public long getTotalAeropuertos() { return snapshot.totalAeropuertos; }
    public long getTotalPaises() { return snapshot.totalPaises; }
    public long getTotalAviones() { return snapshot.totalAviones; }
    public long getTotalItinerarios() { return snapshot.totalItinerarios; }
    public boolean isItinerariosHistoricos() { return snapshot.itinerariosHistoricos; }
    public List<RutaDestacadaDTO> getRutasDestacadas() { return snapshot.rutasDestacadas; }
    public List<FlotaResumenDTO> getFlotaResumen() { return snapshot.flotaResumen; }
    public List<VueloProgramadoPortalDTO> getProximosVuelos() { return snapshot.proximosVuelos; }
    public List<PaisDestinosDTO> getDestinosPorPais() { return snapshot.destinosPorPais; }
    public List<Tarifa> getTarifas() { return snapshot.tarifas; }
    public List<MetodoPago> getMetodosPago() { return snapshot.metodosPago; }
    public List<TipoEquipaje> getTiposEquipaje() { return snapshot.tiposEquipaje; }
    public String getAeropuertosMapaJson() { return snapshot.aeropuertosMapaJson; }
    public String getArticuloActivo() { return articuloActivo; }
}
