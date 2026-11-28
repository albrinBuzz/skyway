package com.SkyWay.views.aeropuerto;

import com.SkyWay.modules.aeropuerto.application.serviceImpl.GeoNombreResolver;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.pai.domain.model.Pai;
import com.SkyWay.modules.pai.domain.service.PaiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

@Named("gestionAeropuertosBean")
@ViewScoped
public class GestionAeropuertosBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private PaiService paiService;

    @Autowired
    private CiudadService ciudadService;

    // Listas Administrativas (Traen TODO: Activos e Inactivos para la tabla de gestión)
    private List<AeropuertoMapaProjection> listaAeropuertosAdmin;
    private List<AeropuertoMapaProjection> listaAeropuertosFiltrados = new ArrayList<>();

    // Filtros jerárquicos de tabla (Continente -> País -> Ciudad + texto)
    private String filtroContinente = "";
    private String filtroPais = "";
    private String filtroCiudad = "";
    private String filtroTexto = "";
    private List<SelectItem> opcionesContinentesFiltro = new ArrayList<>();
    private List<SelectItem> opcionesPaisesFiltro = new ArrayList<>();
    private List<SelectItem> opcionesCiudadesFiltro = new ArrayList<>();

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
    private Integer cantidadPuertas = 4;
    private String terminalInicial = "Terminal Principal";
    private long puertasAeropuertoActual = 0L;

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

        // 3. Reconstruir filtros jerárquicos y resultados filtrados
        reconstruirOpcionesFiltroYResultados(false);
    }

    public void onFiltroContinenteChange() {
        this.filtroPais = "";
        this.filtroCiudad = "";
        reconstruirOpcionesFiltroYResultados(true);
    }

    public void onFiltroPaisChange() {
        this.filtroCiudad = "";
        reconstruirOpcionesFiltroYResultados(true);
    }

    public void aplicarFiltrosTabla() {
        reconstruirOpcionesFiltroYResultados(true);
    }

    public void limpiarFiltrosTabla() {
        this.filtroContinente = "";
        this.filtroPais = "";
        this.filtroCiudad = "";
        this.filtroTexto = "";
        reconstruirOpcionesFiltroYResultados(true);
    }

    private void reconstruirOpcionesFiltroYResultados(boolean resetPagina) {
        List<AeropuertoMapaProjection> base = (listaAeropuertosAdmin != null) ? listaAeropuertosAdmin : List.of();
        Collator collatorEs = Collator.getInstance(Locale.forLanguageTag("es"));
        collatorEs.setStrength(Collator.PRIMARY);

        Map<String, Integer> conteoCont = new TreeMap<>(collatorEs);
        Map<String, Integer> conteoPais = new TreeMap<>(collatorEs);
        Map<String, Integer> conteoCiudad = new TreeMap<>(collatorEs);

        String contSel = filtroContinente != null ? filtroContinente.trim() : "";
        String paisSel = filtroPais != null ? filtroPais.trim() : "";
        String ciudadSel = filtroCiudad != null ? filtroCiudad.trim() : "";
        String textoNorm = GeoNombreResolver.normalizarTexto(filtroTexto);

        List<AeropuertoMapaProjection> filtrados = new ArrayList<>();

        for (AeropuertoMapaProjection a : base) {
            if (a == null) continue;
            String cont = a.getContinente() != null ? a.getContinente() : "Sin continente";
            String pais = a.getPais() != null ? a.getPais() : "";
            String ciudad = a.getCiudad() != null ? a.getCiudad() : "";

            conteoCont.merge(cont, 1, Integer::sum);

            boolean matchCont = contSel.isEmpty() || cont.equalsIgnoreCase(contSel);
            if (matchCont && !pais.isEmpty()) {
                conteoPais.merge(pais, 1, Integer::sum);
            }

            boolean matchPais = paisSel.isEmpty() || pais.equalsIgnoreCase(paisSel);
            if (matchCont && matchPais && !ciudad.isEmpty()) {
                conteoCiudad.merge(ciudad, 1, Integer::sum);
            }

            boolean matchCiudad = ciudadSel.isEmpty() || ciudad.equalsIgnoreCase(ciudadSel);
            boolean matchTexto = textoNorm.isEmpty()
                    || GeoNombreResolver.normalizarTexto(a.getCodigoIata()).contains(textoNorm)
                    || GeoNombreResolver.normalizarTexto(a.getNombreAeropuerto()).contains(textoNorm)
                    || GeoNombreResolver.normalizarTexto(ciudad).contains(textoNorm)
                    || GeoNombreResolver.normalizarTexto(pais).contains(textoNorm)
                    || GeoNombreResolver.normalizarTexto(cont).contains(textoNorm);

            if (matchCont && matchPais && matchCiudad && matchTexto) {
                filtrados.add(a);
            }
        }

        this.opcionesContinentesFiltro = construirSelectItemsConConteo(conteoCont);
        this.opcionesPaisesFiltro = construirSelectItemsConConteo(conteoPais);
        this.opcionesCiudadesFiltro = construirSelectItemsConConteo(conteoCiudad);
        this.listaAeropuertosFiltrados = filtrados;

        if (resetPagina) {
            reiniciarPaginadorTabla();
        }
    }

    private List<SelectItem> construirSelectItemsConConteo(Map<String, Integer> conteos) {
        List<SelectItem> items = new ArrayList<>(conteos.size());
        for (Map.Entry<String, Integer> e : conteos.entrySet()) {
            items.add(new SelectItem(e.getKey(), e.getKey() + " (" + e.getValue() + ")"));
        }
        return items;
    }

    private void reiniciarPaginadorTabla() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getViewRoot() == null) return;
        UIComponent comp = fc.getViewRoot().findComponent("formTabla:dt-aeropuertos");
        if (comp instanceof DataTable dt) {
            dt.setFirst(0);
        }
    }

    public String getAeropuertosJson() {
        List<AeropuertoMapaProjection> listaMapa = (listaAeropuertosActivos != null) ? listaAeropuertosActivos : listaAeropuertosAdmin;
        if (listaMapa == null || listaMapa.isEmpty()) {
            return "[]";
        }
        try {
            List<Map<String, Object>> payload = new ArrayList<>(listaMapa.size());
            for (AeropuertoMapaProjection a : listaMapa) {
                if (a == null || a.getLatitud() == null || a.getLongitud() == null) continue;
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", a.getIdAeropuerto());
                item.put("nombre", a.getNombreAeropuerto() != null ? a.getNombreAeropuerto() : "");
                item.put("iata", a.getCodigoIata() != null ? a.getCodigoIata() : "");
                item.put("lat", a.getLatitud());
                item.put("lng", a.getLongitud());
                payload.add(item);
            }
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (Exception e) {
            return "[]";
        }
    }

    public int getTotalAeropuertosActivos() {
        return listaAeropuertosActivos != null ? listaAeropuertosActivos.size() : 0;
    }

    public void onPaisChange() {
        this.idCiudad = null;
        this.nuevaCiudadNombre = null;
        if (idPaisSeleccionado != null) {
            this.ciudadesFiltradasActivas = ciudadService.obtenerCiudadesPorPais(idPaisSeleccionado);
        } else {
            this.ciudadesFiltradasActivas = new ArrayList<>();
        }
    }

    public void seleccionarUbicacionDesdeMapa() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) return;
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String paisDetectado = params.get("paisDetectado");
        String paisCodigoIso = params.get("paisCodigoIso");
        String ciudadDetectada = params.get("ciudadDetectada");
        String ciudadesCandidatasRaw = params.get("ciudadesCandidatas");
        String aeropuertoDetectado = params.get("aeropuertoDetectado");

        this.nuevaCiudadNombre = null;
        if ((paisDetectado == null || paisDetectado.isBlank()) && (paisCodigoIso == null || paisCodigoIso.isBlank())) {
            return;
        }

        if (this.paisesActivos == null || this.paisesActivos.isEmpty()) {
            this.paisesActivos = paiService.findAllOrdenados();
        }

        Pai paisEncontrado = buscarPaisDinamico(paisDetectado, paisCodigoIso);
        if (paisEncontrado != null) {
            this.idPaisSeleccionado = paisEncontrado.getIdPais();
            this.ciudadesFiltradasActivas = ciudadService.obtenerCiudadesPorPais(this.idPaisSeleccionado);
            this.idCiudad = null;
            this.nuevoPaisNombre = null;

            Ciudad ciudadEncontrada = resolverCiudadGlobalDesdeCandidatos(
                    ciudadDetectada, ciudadesCandidatasRaw, aeropuertoDetectado, this.ciudadesFiltradasActivas);

            if (ciudadEncontrada != null) {
                this.idCiudad = ciudadEncontrada.getIdCiudad();
            } else if (ciudadDetectada != null && !ciudadDetectada.isBlank()) {
                this.nuevaCiudadNombre = ciudadDetectada.trim();
            }
        } else if (paisDetectado != null && !paisDetectado.isBlank()) {
            this.nuevoPaisNombre = paisDetectado.trim();
        }
    }

    private Ciudad resolverCiudadGlobalDesdeCandidatos(String ciudadPrincipal, String ciudadesCandidatasRaw,
                                                       String aeropuertoDetectado, List<Ciudad> ciudadesPais) {
        if (ciudadesPais == null || ciudadesPais.isEmpty()) {
            return null;
        }

        // 1. Probar en orden jerárquico todos los candidatos (ciudad principal -> partido/municipio/provincia -> localidad)
        List<String> candidatos = new ArrayList<>();
        if (ciudadPrincipal != null && !ciudadPrincipal.isBlank()) {
            candidatos.add(ciudadPrincipal.trim());
        }
        if (ciudadesCandidatasRaw != null && !ciudadesCandidatasRaw.isBlank()) {
            for (String parte : ciudadesCandidatasRaw.split("\\|")) {
                String limpio = parte.trim();
                if (!limpio.isEmpty() && !candidatos.contains(limpio)) {
                    candidatos.add(limpio);
                }
            }
        }
        for (String cand : candidatos) {
            Ciudad match = buscarCiudadDinamica(cand, ciudadesPais);
            if (match != null) {
                return match;
            }
        }

        // 2. Si el nombre del aeropuerto menciona una ciudad principal ya registrada en este país (ej. "Aeropuerto de París-Charles de Gaulle")
        if (aeropuertoDetectado != null && !aeropuertoDetectado.isBlank()) {
            String normAero = " " + GeoNombreResolver.normalizarTexto(aeropuertoDetectado) + " ";
            Ciudad bestMatch = null;
            int bestLen = 0;
            for (Ciudad c : ciudadesPais) {
                if (c == null || c.getNombre() == null) continue;
                String normCiudad = GeoNombreResolver.normalizarTexto(c.getNombre());
                if (normCiudad.length() >= 4 && normAero.contains(" " + normCiudad + " ") && normCiudad.length() > bestLen) {
                    bestMatch = c;
                    bestLen = normCiudad.length();
                }
            }
            return bestMatch;
        }

        return null;
    }

    private Pai buscarPaisDinamico(String paisDetectado, String paisCodigoIso) {
        return GeoNombreResolver.buscarMejorPais(this.paisesActivos, Pai::getNombre, paisDetectado, paisCodigoIso);
    }

    private Ciudad buscarCiudadDinamica(String ciudadDetectada, List<Ciudad> ciudades) {
        return GeoNombreResolver.buscarMejorCiudad(ciudades, Ciudad::getNombre, ciudadDetectada);
    }

    public void registrarCiudadAutoDetectada() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) return;
        String nombreCiudad = context.getExternalContext().getRequestParameterMap().get("ciudadAuto");

        try {
            if (idPaisSeleccionado == null) {
                throw new IllegalArgumentException("Primero seleccione un país válido.");
            }
            Ciudad c = ciudadService.registrarCiudadAutoDetectada(idPaisSeleccionado, nombreCiudad);
            this.ciudadesFiltradasActivas = ciudadService.obtenerCiudadesPorPais(idPaisSeleccionado);
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
        this.cantidadPuertas = 4;
        this.terminalInicial = "Terminal Principal";
        this.puertasAeropuertoActual = 0L;
        this.nuevaCiudadNombre = null;
        this.nuevoPaisNombre = null;
        this.modoEdicion = false;
        this.ciudadesFiltradasActivas = new ArrayList<>();
        this.paisesActivos = paiService.findAllOrdenados();
    }

    public void prepararEdicion(AeropuertoMapaProjection item) {
        this.idAeropuerto = item.getIdAeropuerto();
        this.nombreAeropuerto = item.getNombreAeropuerto();
        this.codigoIata = item.getCodigoIata();
        this.latitud = item.getLatitud();
        this.longitud = item.getLongitud();
        this.nuevaCiudadNombre = null;
        this.nuevoPaisNombre = null;
        this.modoEdicion = true;
        this.puertasAeropuertoActual = aeropuertoService.contarPuertasActivasPorAeropuerto(item.getIdAeropuerto());

        this.idPaisSeleccionado = null;
        this.idCiudad = null;
        this.ciudadesFiltradasActivas = new ArrayList<>();
        ciudadService.buscarCiudadConPaisPorNombre(item.getCiudad()).ifPresent(cFound -> {
            this.idPaisSeleccionado = cFound.getPai().getIdPais();
            this.ciudadesFiltradasActivas = ciudadService.obtenerCiudadesPorPais(this.idPaisSeleccionado);
            this.idCiudad = cFound.getIdCiudad();
        });
    }

    public void guardar() {
        try {
            if (modoEdicion) {
                aeropuertoService.actualizarAeropuerto(idAeropuerto, nombreAeropuerto, codigoIata, idCiudad, latitud, longitud);
                addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeropuerto actualizado correctamente.");
            } else {
                int puertas = (cantidadPuertas != null) ? cantidadPuertas : 4;
                aeropuertoService.registrarAeropuerto(nombreAeropuerto, codigoIata, idCiudad, latitud, longitud, puertas, terminalInicial);
                addMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                        "Aeropuerto registrado con " + puertas + " puerta(s) de embarque generadas automáticamente.");
            }

            cargarListas();
            PrimeFaces.current().executeScript("PF('dlgAeropuerto').hide();");

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar", e.getMessage());
            marcarValidacionFallida();
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
            marcarValidacionFallida();
        }
    }

    public void guardarNuevaCiudad() {
        try {
            if (idPaisSeleccionado == null) {
                throw new IllegalArgumentException("Primero seleccione un país antes de crear una ciudad.");
            }
            if (nuevaCiudadNombre == null || nuevaCiudadNombre.isBlank()) {
                throw new IllegalArgumentException("El nombre de la ciudad es obligatorio.");
            }
            Ciudad c = ciudadService.registrarCiudadAutoDetectada(idPaisSeleccionado, nuevaCiudadNombre);
            this.nuevaCiudadNombre = "";
            this.ciudadesFiltradasActivas = ciudadService.obtenerCiudadesPorPais(idPaisSeleccionado);
            this.idCiudad = c.getIdCiudad();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Nueva Ciudad creada.");
            PrimeFaces.current().executeScript("PF('dlgNuevaCiudad').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            marcarValidacionFallida();
        }
    }

    private void marcarValidacionFallida() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.validationFailed();
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
    public List<AeropuertoMapaProjection> getListaAeropuertosFiltrados() { return listaAeropuertosFiltrados; }
    public List<AeropuertoMapaProjection> getListaAeropuertosActivos() { return listaAeropuertosActivos; }
    public List<Pai> getPaisesActivos() { return paisesActivos; }
    public List<Ciudad> getCiudadesFiltradasActivas() { return ciudadesFiltradasActivas; }
    public String getFiltroContinente() { return filtroContinente; }
    public void setFiltroContinente(String filtroContinente) { this.filtroContinente = filtroContinente; }
    public String getFiltroPais() { return filtroPais; }
    public void setFiltroPais(String filtroPais) { this.filtroPais = filtroPais; }
    public String getFiltroCiudad() { return filtroCiudad; }
    public void setFiltroCiudad(String filtroCiudad) { this.filtroCiudad = filtroCiudad; }
    public String getFiltroTexto() { return filtroTexto; }
    public void setFiltroTexto(String filtroTexto) { this.filtroTexto = filtroTexto; }
    public List<SelectItem> getOpcionesContinentesFiltro() { return opcionesContinentesFiltro; }
    public List<SelectItem> getOpcionesPaisesFiltro() { return opcionesPaisesFiltro; }
    public List<SelectItem> getOpcionesCiudadesFiltro() { return opcionesCiudadesFiltro; }
    public boolean isHayFiltrosActivos() {
        return (filtroContinente != null && !filtroContinente.isBlank())
                || (filtroPais != null && !filtroPais.isBlank())
                || (filtroCiudad != null && !filtroCiudad.isBlank())
                || (filtroTexto != null && !filtroTexto.isBlank());
    }

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
    public Integer getCantidadPuertas() { return cantidadPuertas; }
    public void setCantidadPuertas(Integer cantidadPuertas) { this.cantidadPuertas = cantidadPuertas; }
    public String getTerminalInicial() { return terminalInicial; }
    public void setTerminalInicial(String terminalInicial) { this.terminalInicial = terminalInicial; }
    public long getPuertasAeropuertoActual() { return puertasAeropuertoActual; }
}