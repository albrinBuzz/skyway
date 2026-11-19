package com.SkyWay.views.home;

import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;
import com.SkyWay.modules.aerolinea.domain.service.AerolineaService;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.aeropuerto.presentation.dto.PaisDestinosDTO;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.avion.presentation.dto.FlotaResumenDTO;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.RutaDestacadaDTO;
import com.SkyWay.modules.itinerario.presentation.dto.RutasPortalResumenDTO;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.metodopago.domain.service.MetodoPagoService;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import com.SkyWay.modules.tipoequipaje.domain.service.TipoEquipajeService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.modules.vuelo.presentation.dto.VueloProgramadoPortalDTO;
import com.SkyWay.util.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

@Service
public class HomePortalFacadeService {

    private static final long CACHE_TTL_MS = 5L * 60L * 1000L;
    private static final int LIMITE_RUTAS_DESTACADAS = 6;
    private static final int LIMITE_PROXIMOS_VUELOS = 6;

    private final AerolineaService aerolineaService;
    private final AeropuertoService aeropuertoService;
    private final AvionService avionService;
    private final ItinerarioService itinerarioService;
    private final VueloService vueloService;
    private final TarifaService tarifaService;
    private final MetodoPagoService metodoPagoService;
    private final TipoEquipajeService tipoEquipajeService;

    private volatile PortalSnapshot cachedSnapshot;
    private volatile long cacheTimestamp = 0L;
    private final Object cacheLock = new Object();

    @Autowired
    public HomePortalFacadeService(AerolineaService aerolineaService,
                                   AeropuertoService aeropuertoService,
                                   AvionService avionService,
                                   ItinerarioService itinerarioService,
                                   VueloService vueloService,
                                   TarifaService tarifaService,
                                   MetodoPagoService metodoPagoService,
                                   TipoEquipajeService tipoEquipajeService) {
        this.aerolineaService = aerolineaService;
        this.aeropuertoService = aeropuertoService;
        this.avionService = avionService;
        this.itinerarioService = itinerarioService;
        this.vueloService = vueloService;
        this.tarifaService = tarifaService;
        this.metodoPagoService = metodoPagoService;
        this.tipoEquipajeService = tipoEquipajeService;
    }

    public PortalSnapshot obtenerSnapshot() {
        long ahora = System.currentTimeMillis();
        PortalSnapshot actual = this.cachedSnapshot;
        if (actual != null && (ahora - this.cacheTimestamp) < CACHE_TTL_MS) {
            return actual;
        }
        synchronized (this.cacheLock) {
            ahora = System.currentTimeMillis();
            if (this.cachedSnapshot != null && (ahora - this.cacheTimestamp) < CACHE_TTL_MS) {
                return this.cachedSnapshot;
            }
            PortalSnapshot nuevo = construirSnapshotDesdeServicios();
            this.cachedSnapshot = nuevo;
            this.cacheTimestamp = System.currentTimeMillis();
            return nuevo;
        }
    }

    private PortalSnapshot construirSnapshotDesdeServicios() {
        PortalSnapshot snap = new PortalSnapshot();

        try {
            List<Aerolinea> aerolineas = aerolineaService.findAll();
            if (aerolineas != null && !aerolineas.isEmpty()) {
                Aerolinea principal = aerolineas.get(0);
                if (principal.getNombre() != null && !principal.getNombre().isBlank()) {
                    snap.nombreAerolinea = principal.getNombre();
                }
                if (principal.getCodigo() != null && !principal.getCodigo().isBlank()) {
                    snap.codigoAerolinea = principal.getCodigo();
                }
            }
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.aerolinea: " + e.getMessage());
        }

        try {
            snap.totalAeropuertos = aeropuertoService.contarAeropuertosActivos();
            snap.totalPaises = aeropuertoService.contarPaisesActivos();
            snap.destinosPorPais = aeropuertoService.obtenerDestinosAgrupadosPorPais();
            List<AeropuertoMapaDTO> mapa = aeropuertoService.findAllParaMapa();
            snap.aeropuertosMapaJson = new ObjectMapper().writeValueAsString(mapa != null ? mapa : List.of());
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.aeropuerto: " + e.getMessage());
        }

        try {
            snap.totalAviones = avionService.contarAvionesActivos();
            snap.flotaResumen = avionService.obtenerResumenFlotaPortal();
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.avion: " + e.getMessage());
        }

        try {
            snap.totalItinerarios = itinerarioService.obtenerTotalItinerariosEstimado();
            RutasPortalResumenDTO rutasRes = itinerarioService.obtenerRutasDestacadasPortal(LIMITE_RUTAS_DESTACADAS);
            snap.rutasDestacadas = rutasRes.getRutas();
            snap.itinerariosHistoricos = rutasRes.isHistoricas();
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.itinerario: " + e.getMessage());
        }

        try {
            snap.proximosVuelos = vueloService.obtenerProximosVuelosPortal(LIMITE_PROXIMOS_VUELOS);
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.vuelo: " + e.getMessage());
        }

        try {
            List<Tarifa> t = tarifaService.findAll();
            snap.tarifas = t != null ? t : Collections.emptyList();
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.tarifa: " + e.getMessage());
        }

        try {
            List<MetodoPago> mp = metodoPagoService.findAll();
            snap.metodosPago = mp != null ? mp : Collections.emptyList();
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.metodoPago: " + e.getMessage());
        }

        try {
            List<TipoEquipaje> eq = tipoEquipajeService.listarTodos();
            snap.tiposEquipaje = eq != null ? eq : Collections.emptyList();
        } catch (Exception e) {
            Logger.logInfo("HomePortalFacadeService.tipoEquipaje: " + e.getMessage());
        }

        return snap;
    }

    public static class PortalSnapshot implements Serializable {
        private static final long serialVersionUID = 1L;
        String nombreAerolinea = "SkyWay";
        String codigoAerolinea = "SW";
        long totalAeropuertos = 0L;
        long totalPaises = 0L;
        long totalAviones = 0L;
        long totalItinerarios = 0L;
        boolean itinerariosHistoricos = false;
        List<RutaDestacadaDTO> rutasDestacadas = Collections.emptyList();
        List<FlotaResumenDTO> flotaResumen = Collections.emptyList();
        List<VueloProgramadoPortalDTO> proximosVuelos = Collections.emptyList();
        List<PaisDestinosDTO> destinosPorPais = Collections.emptyList();
        List<Tarifa> tarifas = Collections.emptyList();
        List<MetodoPago> metodosPago = Collections.emptyList();
        List<TipoEquipaje> tiposEquipaje = Collections.emptyList();
        String aeropuertosMapaJson = "[]";
    }
}
