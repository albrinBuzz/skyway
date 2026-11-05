package com.SkyWay.views.aeropuerto;

import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.pai.domain.model.Pai;
import com.SkyWay.modules.pai.domain.service.PaiService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("gestionAeropuertosBean")
@ViewScoped
public class GestionAeropuertosBean implements Serializable {

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private PaiService paiService;

    @Autowired
    private CiudadService ciudadService;

    // Listas Administrativas (Traen TODO: Activos e Inactivos para la tabla de gestión)
    private List<AeropuertoMapaProjection> listaAeropuertosAdmin;

    // Listas Operativas / Activas (Traen SOLO Activos para dropdowns, selects y mapa)
    private List<AeropuertoMapaProjection> listaAeropuertosActivos;
    private List<Pai> paisesActivos;
    private List<Ciudad> ciudadesFiltradasActivas = new ArrayList<>();

    private Integer idAeropuerto;
    private String nombreAeropuerto;
    private String codigoIata;
    private Integer idPaisSeleccionado;
    private Integer idCiudad;
    private Double latitud;
    private Double longitud;

    private boolean modoEdicion = false;

    private String nuevoPaisNombre;
    private String nuevaCiudadNombre;

    @PostConstruct
    public void init() {
        cargarListas();
    }

    public void cargarListas() {
        // 1. Lista administrativa completa para gestión (con activos e inactivos)
        this.listaAeropuertosAdmin = aeropuertoService.findAllConCoordenadasAdmin();

        // 2. Listas operativas activas para dropdowns y mapa
        this.listaAeropuertosActivos = aeropuertoService.findAllConCoordenadas();
        this.paisesActivos = paiService.findAllOrdenados();
    }

    public String getAeropuertosJson() {
        List<AeropuertoMapaProjection> listaMapa = (listaAeropuertosActivos != null) ? listaAeropuertosActivos : listaAeropuertosAdmin;
        if (listaMapa == null || listaMapa.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < listaMapa.size(); i++) {
            AeropuertoMapaProjection a = listaMapa.get(i);
            sb.append("{")
                    .append("\"id\":").append(a.getIdAeropuerto()).append(",")
                    .append("\"nombre\":\"").append(a.getNombreAeropuerto().replace("\"", "\\\"")).append("\",")
                    .append("\"iata\":\"").append(a.getCodigoIata()).append("\",")
                    .append("\"lat\":").append(a.getLatitud()).append(",")
                    .append("\"lng\":").append(a.getLongitud())
                    .append("}");
            if (i < listaMapa.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public void onPaisChange() {
        if (idPaisSeleccionado != null) {
            this.ciudadesFiltradasActivas = ciudadService.obtenerCiudadesPorPais(idPaisSeleccionado);
        } else {
            this.ciudadesFiltradasActivas = new ArrayList<>();
        }
    }

    public void seleccionarUbicacionDesdeMapa() {
        FacesContext context = FacesContext.getCurrentInstance();
        String paisDetectado = context.getExternalContext().getRequestParameterMap().get("paisDetectado");
        String ciudadDetectada = context.getExternalContext().getRequestParameterMap().get("ciudadDetectada");

        if (paisDetectado != null && !paisDetectado.isBlank()) {
            paiService.buscarPorNombre(paisDetectado)
                    .ifPresentOrElse(pFound -> {
                        this.idPaisSeleccionado = pFound.getIdPais();
                        onPaisChange();

                        if (ciudadDetectada != null && !ciudadDetectada.isBlank()) {
                            ciudadesFiltradasActivas.stream()
                                    .filter(c -> c.getNombre().equalsIgnoreCase(ciudadDetectada.trim()))
                                    .findFirst()
                                    .ifPresentOrElse(
                                            cFound -> this.idCiudad = cFound.getIdCiudad(),
                                            () -> this.nuevaCiudadNombre = ciudadDetectada
                                    );
                        }
                    }, () -> this.nuevoPaisNombre = paisDetectado);
        }
    }

    public void registrarCiudadAutoDetectada() {
        FacesContext context = FacesContext.getCurrentInstance();
        String nombreCiudad = context.getExternalContext().getRequestParameterMap().get("ciudadAuto");

        try {
            Ciudad c = ciudadService.registrarCiudadAutoDetectada(idPaisSeleccionado, nombreCiudad);
            onPaisChange();
            this.idCiudad = c.getIdCiudad();
            this.nuevaCiudadNombre = null;
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Ciudad '" + c.getNombre() + "' integrada.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void nuevoAeropuerto() {
        this.idAeropuerto = null;
        this.nombreAeropuerto = "";
        this.codigoIata = "";
        this.idPaisSeleccionado = null;
        this.idCiudad = null;
        this.latitud = null;
        this.longitud = null;
        this.modoEdicion = false;
        this.ciudadesFiltradasActivas = new ArrayList<>();
        this.paisesActivos = paiService.findAllOrdenados();

        PrimeFaces.current().executeScript("PF('dlgAeropuerto').show(); window.resetearMapa();");
    }

    public void prepararEdicion(AeropuertoMapaProjection item) {
        this.idAeropuerto = item.getIdAeropuerto();
        this.nombreAeropuerto = item.getNombreAeropuerto();
        this.codigoIata = item.getCodigoIata();
        this.latitud = item.getLatitud();
        this.longitud = item.getLongitud();
        this.modoEdicion = true;

        ciudadService.buscarCiudadConPaisPorNombre(item.getCiudad()).ifPresent(cFound -> {
            this.idPaisSeleccionado = cFound.getPai().getIdPais();
            onPaisChange();
            this.idCiudad = cFound.getIdCiudad();
        });

        PrimeFaces.current().executeScript(
                String.format("PF('dlgAeropuerto').show(); window.cargarUbicacionExistente(%f, %f);", latitud, longitud)
        );
    }

    public void guardar() {
        try {
            if (modoEdicion) {
                aeropuertoService.actualizarAeropuerto(idAeropuerto, nombreAeropuerto, codigoIata, idCiudad, latitud, longitud);
                addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeropuerto actualizado.");
            } else {
                aeropuertoService.registrarAeropuerto(nombreAeropuerto, codigoIata, idCiudad, latitud, longitud);
                addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeropuerto registrado.");
            }

            cargarListas();
            PrimeFaces.current().executeScript("PF('dlgAeropuerto').hide();");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage());
        }
    }

    public void guardarNuevoPais() {
        try {
            Pai p = paiService.guardarPais(nuevoPaisNombre);
            this.nuevoPaisNombre = "";
            cargarListas();
            this.idPaisSeleccionado = p.getIdPais();
            onPaisChange();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Nuevo País creado.");
            PrimeFaces.current().executeScript("PF('dlgNuevoPais').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void guardarNuevaCiudad() {
        try {
            Ciudad c = ciudadService.guardarCiudadParaPais(idPaisSeleccionado, nuevaCiudadNombre);
            this.nuevaCiudadNombre = "";
            onPaisChange();
            this.idCiudad = c.getIdCiudad();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Nueva Ciudad creada.");
            PrimeFaces.current().executeScript("PF('dlgNuevaCiudad').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void eliminar(Integer id) {
        try {
            aeropuertoService.delete(id);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeropuerto dado de baja exitosamente.");
            cargarListas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar: el aeropuerto tiene operaciones vinculadas.");
        }
    }

    public void reactivar(Integer id) {
        try {
            aeropuertoService.reactivar(id);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeropuerto reactivado exitosamente.");
            cargarListas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar el aeropuerto: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<AeropuertoMapaProjection> getListaAeropuertosAdmin() { return listaAeropuertosAdmin; }
    public List<AeropuertoMapaProjection> getListaAeropuertosActivos() { return listaAeropuertosActivos; }
    public List<Pai> getPaisesActivos() { return paisesActivos; }
    public List<Ciudad> getCiudadesFiltradasActivas() { return ciudadesFiltradasActivas; }

    // Compatibilidad
    public List<AeropuertoMapaProjection> getListaAeropuertos() { return listaAeropuertosAdmin; }
    public List<Pai> getPaisesDisponibles() { return paisesActivos; }
    public List<Ciudad> getCiudadesFiltradas() { return ciudadesFiltradasActivas; }
    public Integer getIdAeropuerto() { return idAeropuerto; }
    public void setIdAeropuerto(Integer idAeropuerto) { this.idAeropuerto = idAeropuerto; }
    public String getNombreAeropuerto() { return nombreAeropuerto; }
    public void setNombreAeropuerto(String nombreAeropuerto) { this.nombreAeropuerto = nombreAeropuerto; }
    public String getCodigoIata() { return codigoIata; }
    public void setCodigoIata(String codigoIata) { this.codigoIata = codigoIata; }
    public Integer getIdPaisSeleccionado() { return idPaisSeleccionado; }
    public void setIdPaisSeleccionado(Integer idPaisSeleccionado) { this.idPaisSeleccionado = idPaisSeleccionado; }
    public Integer getIdCiudad() { return idCiudad; }
    public void setIdCiudad(Integer idCiudad) { this.idCiudad = idCiudad; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
    public boolean isModoEdicion() { return modoEdicion; }
    public String getNuevoPaisNombre() { return nuevoPaisNombre; }
    public void setNuevoPaisNombre(String nuevoPaisNombre) { this.nuevoPaisNombre = nuevoPaisNombre; }
    public String getNuevaCiudadNombre() { return nuevaCiudadNombre; }
    public void setNuevaCiudadNombre(String nuevaCiudadNombre) { this.nuevaCiudadNombre = nuevaCiudadNombre; }
}