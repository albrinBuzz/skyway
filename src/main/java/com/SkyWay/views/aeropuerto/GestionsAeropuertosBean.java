package com.SkyWay.views.aeropuerto;

import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.continente.domain.model.Continente;
import com.SkyWay.modules.continente.domain.service.ContinenteService;
import com.SkyWay.modules.pai.domain.model.Pai;
import com.SkyWay.modules.pai.domain.service.PaiService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.*;

@Named("gestionAeropuertosBean")
@ViewScoped
public class GestionsAeropuertosBean implements Serializable {

    @Autowired
    private AeropuertoService aeropuertoService;

    @Autowired
    private PaiService paiService;

    @Autowired
    private CiudadService ciudadService;

    @Autowired(required = false)
    private ContinenteService continenteService;

    // Control de pestaña activa (0: Aeropuertos, 1: Continentes, 2: Países, 3: Ciudades)
    private int tabActiva = 0;

    // Listas Principales
    private List<AeropuertoMapaProjection> listaAeropuertos;
    private List<Pai> paisesDisponibles;
    private List<Ciudad> ciudadesFiltradas = new ArrayList<>();
    private List<Ciudad> todasLasCiudades = new ArrayList<>();
    private List<Continente> listaContinentes = new ArrayList<>();

    // Variables de Formulario Aeropuerto
    private Integer idAeropuerto;
    private String nombreAeropuerto;
    private String codigoIata;
    private Integer idPaisSeleccionado;
    private Integer idCiudad;
    private Double latitud;
    private Double longitud;

    private boolean modoEdicion = false;

    // Form Modales Auxiliares
    private String nuevoPaisNombre;
    private Integer idContinenteParaPais;
    private String nuevaCiudadNombre;

    // Form Modal Continente
    private Integer idContinenteSel;
    private String nombreContinenteSel;

    private String sugerenciaCiudadDetectada;
    private String sugerenciaPaisDetectado;

    @PostConstruct
    public void init() {
        cargarListas();
    }

    public void cargarListas() {
        this.listaAeropuertos = aeropuertoService.findAllConCoordenadas();
        this.paisesDisponibles = paiService.findAllOrdenados();
        this.todasLasCiudades = ciudadService.getAllCiudades();
        if (continenteService != null) {
            this.listaContinentes = continenteService.findAll();
        }
    }

    public String getAeropuertosJson() {
        if (listaAeropuertos == null || listaAeropuertos.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < listaAeropuertos.size(); i++) {
            AeropuertoMapaProjection a = listaAeropuertos.get(i);
            sb.append("{")
                    .append("\"id\":").append(a.getIdAeropuerto()).append(",")
                    .append("\"nombre\":\"").append(a.getNombreAeropuerto().replace("\"", "\\\"")).append("\",")
                    .append("\"iata\":\"").append(a.getCodigoIata()).append("\",")
                    .append("\"lat\":").append(a.getLatitud()).append(",")
                    .append("\"lng\":").append(a.getLongitud())
                    .append("}");
            if (i < listaAeropuertos.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public void onPaisChange() {
        if (idPaisSeleccionado != null) {
            this.ciudadesFiltradas = ciudadService.obtenerCiudadesPorPais(idPaisSeleccionado);
        } else {
            this.ciudadesFiltradas = new ArrayList<>();
        }
    }

    // 1. Detección flexible de País/Ciudad desde el Mapa
    public void seleccionarUbicacionDesdeMapa() {
        FacesContext context = FacesContext.getCurrentInstance();
        String paisDetectado = context.getExternalContext().getRequestParameterMap().get("paisDetectado");
        String ciudadDetectada = context.getExternalContext().getRequestParameterMap().get("ciudadDetectada");

        this.sugerenciaCiudadDetectada = null;
        this.sugerenciaPaisDetectado = null;

        if (paisDetectado != null && !paisDetectado.isBlank()) {
            String paisClean = paisDetectado.trim();

            // Búsqueda inteligente / parcial del país en el repositorio
            Optional<Pai> paisOpt = paiService.findAllOrdenados().stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(paisClean)
                            || paisClean.toLowerCase().contains(p.getNombre().toLowerCase())
                            || p.getNombre().toLowerCase().contains(paisClean.toLowerCase()))
                    .findFirst();

            if (paisOpt.isPresent()) {
                Pai pFound = paisOpt.get();
                this.idPaisSeleccionado = pFound.getIdPais();
                this.sugerenciaPaisDetectado = pFound.getNombre();

                if (ciudadDetectada != null && !ciudadDetectada.isBlank()) {
                    String ciudadClean = ciudadDetectada.trim();
                    List<Ciudad> ciudadesPais = ciudadService.obtenerCiudadesPorPais(pFound.getIdPais());

                    Optional<Ciudad> ciudadOpt = ciudadesPais.stream()
                            .filter(c -> c.getNombre().equalsIgnoreCase(ciudadClean))
                            .findFirst();

                    if (ciudadOpt.isPresent()) {
                        Ciudad c = ciudadOpt.get();
                        this.idCiudad = c.getIdCiudad();
                        this.lugarSeleccionado = new LugarItem(c.getNombre(), "ciudad", c.getIdCiudad());
                    } else {
                        this.sugerenciaCiudadDetectada = ciudadClean;
                    }
                }
            } else {
                this.sugerenciaPaisDetectado = paisClean;
                this.sugerenciaCiudadDetectada = ciudadDetectada;
            }
        }
    }

    public void autoCrearCiudadSugerida() {
        Logger.logInfo(">>> [START] Ejecutando autoCrearCiudadSugerida()");
        Logger.logInfo(">>> Ciudad detectada: " + this.sugerenciaCiudadDetectada);
        Logger.logInfo(">>> País detectado: " + this.sugerenciaPaisDetectado);

        try {
            // 1. Búsqueda inteligente de País para evitar duplicados ("Estados Unidos" vs "Estados Unidos de América")
            if (this.idPaisSeleccionado == null && this.sugerenciaPaisDetectado != null && !this.sugerenciaPaisDetectado.isBlank()) {
                String paisClean = limpiarTexto(this.sugerenciaPaisDetectado);
                List<Pai> listaPaises = paiService.findAllOrdenados();

                Optional<Pai> paisOpt = listaPaises.stream()
                        .filter(p -> {
                            String pBD = limpiarTexto(p.getNombre());
                            // Comprobación de coincidencia exacta o subcadena bidireccional
                            return pBD.equalsIgnoreCase(paisClean)
                                    || paisClean.contains(pBD)
                                    || pBD.contains(paisClean);
                        })
                        .findFirst();

                if (paisOpt.isPresent()) {
                    this.idPaisSeleccionado = paisOpt.get().getIdPais();
                    this.sugerenciaPaisDetectado = paisOpt.get().getNombre();
                    Logger.logInfo(">>> ¡Coincidencia encontrada! Reutilizando País ID: " + this.idPaisSeleccionado + " (" + this.sugerenciaPaisDetectado + ")");
                } else {
                    Logger.logInfo(">>> Creando país nuevo en BD: " + this.sugerenciaPaisDetectado);
                    Pai nuevoP = paiService.guardarPais(this.sugerenciaPaisDetectado.trim());
                    this.idPaisSeleccionado = nuevoP.getIdPais();
                }
            }

            // 2. Registrar la ciudad y seleccionarla
            if (this.idPaisSeleccionado != null && this.sugerenciaCiudadDetectada != null && !this.sugerenciaCiudadDetectada.isBlank()) {
                Ciudad c = ciudadService.registrarCiudadAutoDetectada(this.idPaisSeleccionado, this.sugerenciaCiudadDetectada.trim());

                this.idCiudad = c.getIdCiudad();
                // Asignamos el objeto DTO directamente
                this.lugarSeleccionado = new LugarItem(c.getNombre(), "ciudad", c.getIdCiudad());

                // Refrescar combos y el árbol del CascadeSelect
                cargarListas();
                cargarCascadingUbicaciones();

                String msj = "Ciudad '" + c.getNombre() + "' (" + this.sugerenciaPaisDetectado + ") creada e integrada correctamente.";
                this.sugerenciaCiudadDetectada = null;
                this.sugerenciaPaisDetectado = null;

                addMessage(FacesMessage.SEVERITY_INFO, "Éxito", msj);
            }

        } catch (Exception e) {
            Logger.logError(">>> ERROR en autoCrearCiudadSugerida: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error al crear ciudad", e.getMessage());
        }
    }

    // --- MÉTODOS AUXILIARES DE COINCIDENCIA LÉXICA UNIVERSAL ---

    /**
     * Limpia tildes, convierte a minúsculas y remueve caracteres especiales.
     */
    private String limpiarTexto(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .toLowerCase()
                .trim();
    }

    /**
     * Calcula la similitud combinando coincidencia de palabras (Jaccard) y Distancia Levenshtein.
     */
    private double calcularSimilitudLexica(String str1, String str2) {
        if (str1.equals(str2)) return 1.0;
        if (str1.contains(str2) || str2.contains(str1)) return 0.85;

        // Palabras vacías ignoradas (artículos, preposiciones)
        Set<String> stopwords = new HashSet<>(Arrays.asList("de", "del", "la", "las", "los", "y", "e", "the", "of", "and"));

        Set<String> palabras1 = new HashSet<>(Arrays.asList(str1.split("\\s+")));
        Set<String> palabras2 = new HashSet<>(Arrays.asList(str2.split("\\s+")));

        palabras1.removeAll(stopwords);
        palabras2.removeAll(stopwords);

        // Coincidencia de tokens (Intersección de palabras clave)
        Set<String> interseccion = new HashSet<>(palabras1);
        interseccion.retainAll(palabras2);

        if (!interseccion.isEmpty()) {
            double mayorComun = Math.max(palabras1.size(), palabras2.size());
            return (double) interseccion.size() / mayorComun;
        }

        return 0.0;
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
        this.ciudadesFiltradas = new ArrayList<>();

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
            if (cFound.getPai() != null) {
                this.idPaisSeleccionado = cFound.getPai().getIdPais();
                onPaisChange();
            }
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
            cargarListas();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Nueva Ciudad creada.");
            PrimeFaces.current().executeScript("PF('dlgNuevaCiudad').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void eliminar(Integer id) {
        try {
            aeropuertoService.delete(id);
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeropuerto eliminado.");
            cargarListas();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar: el aeropuerto tiene operaciones vinculadas.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }


    // 1. DTO LugarItem con ID e Implementación de Equals/HashCode para JSF
    public static class LugarItem implements Serializable {
        private String nombre;
        private String tipo; // "continente", "pais", "ciudad"
        private Integer id;   // Contiene el ID de la entidad

        public LugarItem() {}

        public LugarItem(String nombre, String tipo, Integer id) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.id = id;
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LugarItem lugarItem = (LugarItem) o;
            if (id != null ? !id.equals(lugarItem.id) : lugarItem.id != null) return false;
            return tipo != null ? tipo.equals(lugarItem.tipo) : lugarItem.tipo == null;
        }

        @Override
        public int hashCode() {
            int result = tipo != null ? tipo.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }

        // Este método se usa explícitamente para pintar la etiqueta limpia en PrimeFaces
        @Override
        public String toString() {
            return nombre != null ? nombre : "";
        }

        // Identificador para el Converter JSF
        public String toKey() {
            return tipo + ":" + id + ":" + nombre;
        }
    }

    // 2. Propiedad seleccionada en el Bean (de tipo LugarItem)
    private LugarItem lugarSeleccionado;
    private List<SelectItem> cascadingUbicaciones;

    // 3. Generación de los nodos jerárquicos
    public void cargarCascadingUbicaciones() {
        this.cascadingUbicaciones = new ArrayList<>();
        List<Continente> continentes = (continenteService != null) ? continenteService.findAll() : new ArrayList<>();

        for (Continente cont : continentes) {
            LugarItem itemCont = new LugarItem(cont.getNombre(), "continente", cont.getIdContinente());
            SelectItemGroup grupoCont = new SelectItemGroup(cont.getNombre());
            grupoCont.setValue(itemCont);

            List<SelectItem> paisesItems = new ArrayList<>();
            if (cont.getPaises() != null) {
                for (Pai p : cont.getPaises()) {
                    LugarItem itemPais = new LugarItem(p.getNombre(), "pais", p.getIdPais());
                    SelectItemGroup grupoPais = new SelectItemGroup(p.getNombre());
                    grupoPais.setValue(itemPais);

                    List<SelectItem> ciudadesItems = new ArrayList<>();
                    List<Ciudad> ciudades = ciudadService.obtenerCiudadesPorPais(p.getIdPais());

                    for (Ciudad c : ciudades) {
                        LugarItem itemCiudad = new LugarItem(c.getNombre(), "ciudad", c.getIdCiudad());
                        // Pasamos itemCiudad como valor y c.getNombre() como label
                        SelectItem optCiudad = new SelectItem(itemCiudad, c.getNombre());
                        ciudadesItems.add(optCiudad);
                    }

                    grupoPais.setSelectItems(ciudadesItems.toArray(new SelectItem[0]));
                    paisesItems.add(grupoPais);
                }
            }

            grupoCont.setSelectItems(paisesItems.toArray(new SelectItem[0]));
            cascadingUbicaciones.add(grupoCont);
        }
    }

    // 4. Listener AJAX al seleccionar un ítem
    public void onCascadeCiudadSelect(SelectEvent<Object> event) {
        LugarItem item = null;
        if (event.getObject() instanceof LugarItem) {
            item = (LugarItem) event.getObject();
        } else if (this.lugarSeleccionado != null) {
            item = this.lugarSeleccionado;
        }

        if (item != null && "ciudad".equals(item.getTipo())) {
            this.idCiudad = item.getId();

            ciudadService.getCiudadById(this.idCiudad).ifPresent(c -> {
                String ciudadNombre = c.getNombre();
                String paisNombre = (c.getPai() != null) ? c.getPai().getNombre() : "";
                String queryBusqueda = (paisNombre.isBlank()) ? ciudadNombre : ciudadNombre + ", " + paisNombre;

                PrimeFaces.current().executeScript(
                        String.format("window.centrarMapaEnUbicacion('%s');", queryBusqueda.replace("'", "\\'"))
                );
            });
        }
    }

    // 5. Converter de JSF embebido para deserializar LugarItem
    @FacesConverter(value = "lugarItemConverter", managed = true)
    public static class LugarItemConverter implements Converter<LugarItem> {

        @Override
        public LugarItem getAsObject(FacesContext context, UIComponent component, String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            String[] parts = value.split(":", 3);
            if (parts.length < 3) return null;

            String tipo = parts[0];
            Integer id = "null".equals(parts[1]) ? null : Integer.parseInt(parts[1]);
            String nombre = parts[2];

            return new LugarItem(nombre, tipo, id);
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, LugarItem value) {
            if (value == null) {
                return "";
            }
            return value.toKey();
        }
    }

    // Getters y Setters
    public LugarItem getLugarSeleccionado() { return lugarSeleccionado; }
    public void setLugarSeleccionado(LugarItem lugarSeleccionado) { this.lugarSeleccionado = lugarSeleccionado; }

    public List<SelectItem> getCascadingUbicaciones() {
        if (cascadingUbicaciones == null || cascadingUbicaciones.isEmpty()) {
            cargarCascadingUbicaciones();
        }
        return cascadingUbicaciones;
    }

    private String valorCascadeSeleccionado;

    // Getters & Setters para el TabControl y Tablas
    public int getTabActiva() { return tabActiva; }
    public void setTabActiva(int tabActiva) { this.tabActiva = tabActiva; }
    public String getSugerenciaCiudadDetectada() { return sugerenciaCiudadDetectada; }
    public String getSugerenciaPaisDetectado() { return sugerenciaPaisDetectado; }

    public List<AeropuertoMapaProjection> getListaAeropuertos() { return listaAeropuertos; }
    public List<Pai> getPaisesDisponibles() { return paisesDisponibles; }
    public List<Ciudad> getCiudadesFiltradas() { return ciudadesFiltradas; }
    public List<Ciudad> getTodasLasCiudades() { return todasLasCiudades; }
    public List<Continente> getListaContinentes() { return listaContinentes; }
    public String getValorCascadeSeleccionado() { return valorCascadeSeleccionado; }
    public void setValorCascadeSeleccionado(String valorCascadeSeleccionado) { this.valorCascadeSeleccionado = valorCascadeSeleccionado; }

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
    public Integer getIdContinenteParaPais() { return idContinenteParaPais; }
    public void setIdContinenteParaPais(Integer idContinenteParaPais) { this.idContinenteParaPais = idContinenteParaPais; }
}