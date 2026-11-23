package com.SkyWay.views.avion;

import com.SkyWay.modules.asiento.domain.model.Asiento;
import com.SkyWay.modules.asiento.domain.repository.AsientoRepository;
import com.SkyWay.modules.asiento.presentation.dto.BloqueAsientosDTO;
import com.SkyWay.modules.asiento.presentation.dto.FilaCabinaDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.avion.domain.exception.AvionValidationException;
import com.SkyWay.modules.avion.domain.model.Avion;
import com.SkyWay.modules.avion.domain.model.ConfiguracionCabina;
import com.SkyWay.modules.avion.domain.repository.ConfiguracionCabinaRepository;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.capacidadclase.domain.model.CapacidadClase;
import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;
import com.SkyWay.modules.fabricante.domain.model.Fabricante;
import com.SkyWay.modules.fabricante.domain.service.FabricanteService;
import com.SkyWay.modules.modeloavion.domain.model.ModeloAvion;
import com.SkyWay.modules.modeloavion.domain.service.ModeloAvionService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Named("gestionFlotaIntegradaBean")
@ViewScoped
public class GestionFlotaIntegradaBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private AvionService avionService;

    @Autowired
    private ModeloAvionService modeloAvionService;

    @Autowired
    private FabricanteService fabricanteService;

    @Autowired
    private ClaseAsientoRepository claseAsientoRepository;

    @Autowired
    private AsientoRepository asientoRepository;

    @Autowired
    private ConfiguracionCabinaRepository configuracionCabinaRepository;

    // Listas Administrativas (Traen TODO: Activos e Inactivos para las tablas de gestión)
    private List<Avion> listaAvionesAdmin;
    private List<ModeloAvion> listaModelosAdmin;
    private List<Fabricante> listaFabricantesAdmin;
    private List<ClaseAsiento> listaClasesAdmin;

    // Listas Operativas / Activas (Traen SOLO Activos para selects, dropdowns y modales)
    private List<Avion> listaAvionesActivos;
    private List<ModeloAvion> listaModelosActivos;
    private List<Fabricante> listaFabricantesActivos;
    private List<ClaseAsiento> listaClasesActivas;

    // Entidades para formularios
    private Avion avionSeleccionado;
    private ModeloAvion modeloSeleccionado;
    private Fabricante fabricanteSeleccionado;

    // Variables Auxiliares
    private Integer idModeloParaAvion;
    private Integer idFabricanteParaModelo;
    private Date fechaMantenimientoAux;
    private boolean modoEdicionAvion;
    private int totalAsientosCalculados;

    private Map<Integer, Integer> capacidadesClaseMap = new HashMap<>();
    private Map<Integer, List<Asiento>> asientosPorFilaMap = new TreeMap<>();
    private List<FilaCabinaDTO> filasCabinaSimulador = new ArrayList<>();
    private boolean avionDoblePasilloSimulador;
    private int totalAsientosSimulador;
    private String distribucionResumenSimulador;
    private int tabActiva = 0;

    @PostConstruct
    public void init() {
        cargarTodo();
    }

    public void cargarTodo() {
        // 1. Listas administrativas completas (para las dataTables del admin)
        this.listaAvionesAdmin = avionService.findAllAdmin();
        this.listaModelosAdmin = modeloAvionService.findAllAdmin();
        this.listaFabricantesAdmin = fabricanteService.findAllAdmin();
        this.listaClasesAdmin = claseAsientoRepository.findAllAdmin();

        // 2. Listas de entidades activas (para dropdowns de asignación y modales)
        this.listaAvionesActivos = avionService.findAll();
        this.listaFabricantesActivos = fabricanteService.findAll();
        this.listaModelosActivos = modeloAvionService.findAll().stream()
                .filter(m -> m.getFabricante() == null || Boolean.TRUE.equals(m.getFabricante().getActivo()))
                .collect(Collectors.toList());
        this.listaClasesActivas = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();
    }

    // ==========================================
    // 1. GESTIÓN DE FABRICANTES
    // ==========================================
    public void prepararNuevoFabricante() {
        this.fabricanteSeleccionado = new Fabricante();
        PrimeFaces.current().executeScript("PF('dlgFabricante').show();");
    }

    public void guardarFabricante() {
        try {
            fabricanteService.guardar(fabricanteSeleccionado);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Fabricante registrado correctamente.");
            PrimeFaces.current().executeScript("PF('dlgFabricante').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void eliminarFabricante(Integer id) {
        try {
            fabricanteService.eliminar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Fabricante dado de baja exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar un fabricante con modelos asociados.");
        }
    }

    public void reactivarFabricante(Integer id) {

        try {
            fabricanteService.reactivar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Fabricante reactivado exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar el fabricante: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. GESTIÓN DE MODELOS DE AVIÓN
    // ==========================================
    public void prepararNuevoModelo() {
        this.modeloSeleccionado = new ModeloAvion();
        this.idFabricanteParaModelo = null;
        this.listaFabricantesActivos = fabricanteService.findAll();
        PrimeFaces.current().executeScript("PF('dlgModelo').show();");
    }

    public void guardarModelo() {
        try {
            if (idFabricanteParaModelo != null) {
                Fabricante fab = listaFabricantesActivos != null ? listaFabricantesActivos.stream()
                        .filter(f -> f.getIdFabricante().equals(idFabricanteParaModelo))
                        .findFirst().orElse(null) : null;
                if (fab == null && listaFabricantesAdmin != null) {
                    fab = listaFabricantesAdmin.stream()
                            .filter(f -> f.getIdFabricante().equals(idFabricanteParaModelo))
                            .findFirst().orElse(null);
                }
                modeloSeleccionado.setFabricante(fab);
            }
            modeloAvionService.guardar(modeloSeleccionado);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Modelo de avión guardado.");
            PrimeFaces.current().executeScript("PF('dlgModelo').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
        }
    }

    public void eliminarModelo(Integer id) {
        try {
            modeloAvionService.eliminar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Modelo dado de baja exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar un modelo con aeronaves asignadas.");
        }
    }

    public void reactivarModelo(Integer id) {
        try {
            modeloAvionService.reactivar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Modelo reactivado exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar el modelo: " + e.getMessage());
        }
    }

    // ==========================================
    // 3. GESTIÓN DE AERONAVES (AVIÓN)
    // ==========================================
    public void prepararNuevoAvion() {
        this.avionSeleccionado = new Avion();
        this.avionSeleccionado.setEstadoDeMantenimiento("Operativo");
        this.avionSeleccionado.setCapacidadDeCarga(15000);
        this.avionSeleccionado.setAnoDeFabricacion(LocalDate.now().getYear());
        this.idModeloParaAvion = null;
        this.modoEdicionAvion = false;
        this.totalAsientosCalculados = 0;

        this.listaModelosActivos = modeloAvionService.findAll().stream()
                .filter(m -> m.getFabricante() == null || Boolean.TRUE.equals(m.getFabricante().getActivo()))
                .collect(Collectors.toList());
        this.listaClasesActivas = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();

        LocalDate seisMeses = LocalDate.now().plusMonths(6);
        this.fechaMantenimientoAux = Date.from(seisMeses.atStartOfDay(ZoneId.systemDefault()).toInstant());

        this.capacidadesClaseMap.clear();
        if (listaClasesActivas != null) {
            for (ClaseAsiento c : listaClasesActivas) {
                capacidadesClaseMap.put(c.getIdClase(), 0);
            }
        }

        PrimeFaces.current().executeScript("PF('dlgAvion').show();");
    }

    public void prepararEdicionAvion(Avion a) {
        if (a == null) {
            return;
        }

        Logger.logInfo(a.toString());
        this.avionSeleccionado = a;
        this.modoEdicionAvion = true;
        this.idModeloParaAvion = (a.getModeloAvion() != null) ? a.getModeloAvion().getIdModelo() : null;

        this.listaModelosActivos = modeloAvionService.findAll().stream()
                .filter(m -> m.getFabricante() == null || Boolean.TRUE.equals(m.getFabricante().getActivo()))
                .collect(Collectors.toList());
        this.listaClasesActivas = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();

        // Carga de la fecha de próximo mantenimiento
        if (a.getFechaProximoMantenimiento() != null) {
            this.fechaMantenimientoAux = new Date(a.getFechaProximoMantenimiento().getTime());
        } else {
            this.fechaMantenimientoAux = null;
        }

        // Inicializar mapa de capacidades
        this.capacidadesClaseMap.clear();
        if (listaClasesActivas != null) {
            for (ClaseAsiento c : listaClasesActivas) {
                capacidadesClaseMap.put(c.getIdClase(), 0);
            }
        }

        // Mapear capacidades registradas actualmente en el avión
        if (a.getCapacidadClases1() != null && !a.getCapacidadClases1().isEmpty()) {
            for (CapacidadClase cc : a.getCapacidadClases1()) {
                if (cc.getClaseAsiento1() != null && cc.getCantidad() != null) {
                    capacidadesClaseMap.put(cc.getClaseAsiento1().getIdClase(), cc.getCantidad());
                }
            }
        }

        recalcularTotalAsientos();
    }

    public void recalcularTotalAsientos() {
        normalizarCapacidadesMap();
        this.totalAsientosCalculados = capacidadesClaseMap.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void guardarAvion() {
        try {
            if (idModeloParaAvion != null) {
                ModeloAvion mod = listaModelosActivos != null ? listaModelosActivos.stream()
                        .filter(m -> m.getIdModelo().equals(idModeloParaAvion))
                        .findFirst().orElse(null) : null;
                if (mod == null && listaModelosAdmin != null) {
                    mod = listaModelosAdmin.stream()
                            .filter(m -> m.getIdModelo().equals(idModeloParaAvion))
                            .findFirst().orElse(null);
                }
                avionSeleccionado.setModeloAvion(mod);
            } else {
                avionSeleccionado.setModeloAvion(null);
            }

            if (fechaMantenimientoAux != null) {
                avionSeleccionado.setFechaProximoMantenimiento(new Timestamp(fechaMantenimientoAux.getTime()));
            }

            normalizarCapacidadesMap();
            avionService.saveConCapacidades(avionSeleccionado, capacidadesClaseMap);

            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave guardada y asientos configurados correctamente.");
            PrimeFaces.current().executeScript("PF('dlgAvion').hide();");
        } catch (AvionValidationException ave) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", ave.getMessage());
        } catch (Exception e) {
            Logger.logError("Error al guardar aeronave: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar la aeronave. Verifica los datos ingresados.");
        }
    }

    private void normalizarCapacidadesMap() {
        Map<Integer, Integer> normalizado = new HashMap<>();
        if (capacidadesClaseMap != null) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) capacidadesClaseMap).entrySet()) {
                Integer idClase = parseEntero(entry.getKey());
                Integer cantidad = parseEntero(entry.getValue());
                if (idClase != null) {
                    normalizado.put(idClase, cantidad != null ? cantidad : 0);
                }
            }
        }
        this.capacidadesClaseMap = normalizado;
    }

    private Integer parseEntero(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof Number num) {
            return num.intValue();
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return 0;
        }
        return Integer.valueOf(texto);
    }

    public void verMapaAsientos(Avion a) {
        this.avionSeleccionado = a;
        this.filasCabinaSimulador = new ArrayList<>();
        this.avionDoblePasilloSimulador = false;
        this.totalAsientosSimulador = 0;
        this.distribucionResumenSimulador = "";

        if (a == null || a.getIdAvion() == null) {
            this.asientosPorFilaMap.clear();
            return;
        }

        List<Asiento> asientos = asientoRepository.findByAvion_IdAvionOrderByFilaAscLetraAsc(a.getIdAvion());
        List<ConfiguracionCabina> configs = (a.getModeloAvion() != null && a.getModeloAvion().getIdModelo() != null)
                ? configuracionCabinaRepository.findByModeloAvionIdModelo(a.getModeloAvion().getIdModelo())
                : List.of();

        this.distribucionResumenSimulador = construirResumenConfiguracion(configs);

        if (asientos != null && !asientos.isEmpty()) {
            this.totalAsientosSimulador = asientos.size();
            this.asientosPorFilaMap = asientos.stream()
                    .filter(s -> s.getFila() != null)
                    .collect(Collectors.groupingBy(Asiento::getFila, TreeMap::new, Collectors.toList()));
            construirCabinaSimulador(this.asientosPorFilaMap, configs);
        } else {
            this.asientosPorFilaMap.clear();
        }

        PrimeFaces.current().executeScript("PF('dlgMapaAsientos').show();");
    }

    private String construirResumenConfiguracion(List<ConfiguracionCabina> configs) {
        if (configs == null || configs.isEmpty()) {
            return "Configuración estándar por fila";
        }
        Map<String, String> porClase = new LinkedHashMap<>();
        for (ConfiguracionCabina cc : configs) {
            String clase = (cc.getClaseAsiento() != null && cc.getClaseAsiento().getDescripcion() != null)
                    ? cc.getClaseAsiento().getDescripcion()
                    : "Cabina";
            String dist = cc.getDistribucionColumnas() != null ? cc.getDistribucionColumnas().trim() : "";
            if (!dist.isEmpty()) {
                porClase.putIfAbsent(clase, dist);
            }
        }
        return porClase.entrySet().stream()
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(" • "));
    }

    private void construirCabinaSimulador(Map<Integer, List<Asiento>> mapaFilas, List<ConfiguracionCabina> configs) {
        String ultimaClase = "";
        for (Map.Entry<Integer, List<Asiento>> entry : mapaFilas.entrySet()) {
            int numFila = entry.getKey();
            List<Asiento> asientosFila = new ArrayList<>(entry.getValue());
            ConfiguracionCabina config = buscarConfiguracionParaFila(numFila, configs);

            ordenarAsientosSegunConfig(asientosFila, config);

            String claseFila = !asientosFila.isEmpty() && asientosFila.get(0).getClaseAsiento() != null
                    ? asientosFila.get(0).getClaseAsiento().getDescripcion()
                    : (config != null && config.getClaseAsiento() != null ? config.getClaseAsiento().getDescripcion() : "Económica");
            boolean esEmergencia = (config != null && Boolean.TRUE.equals(config.getEsSalidaEmergencia()))
                    || asientosFila.stream().anyMatch(s -> Boolean.TRUE.equals(s.getEsEmergencia()));

            FilaCabinaDTO filaDTO = new FilaCabinaDTO(numFila, claseFila, esEmergencia);
            if (!claseFila.equalsIgnoreCase(ultimaClase)) {
                filaDTO.setEsPrimerFilaDeClase(true);
                ultimaClase = claseFila;
            }

            List<BloqueAsientosDTO> bloques = dividirEnBloques(asientosFila, config);
            filaDTO.setBloques(bloques);
            if (bloques.size() >= 3 || asientosFila.size() >= 7) {
                this.avionDoblePasilloSimulador = true;
            }
            this.filasCabinaSimulador.add(filaDTO);
        }
    }

    private ConfiguracionCabina buscarConfiguracionParaFila(int numFila, List<ConfiguracionCabina> configs) {
        if (configs == null) {
            return null;
        }
        for (ConfiguracionCabina cc : configs) {
            if (cc.getFilaInicio() != null && cc.getFilaFin() != null
                    && numFila >= cc.getFilaInicio() && numFila <= cc.getFilaFin()) {
                return cc;
            }
        }
        return null;
    }

    private void ordenarAsientosSegunConfig(List<Asiento> asientosFila, ConfiguracionCabina config) {
        if (config != null && config.getLetrasColumnas() != null && !config.getLetrasColumnas().isBlank()) {
            List<String> orden = Arrays.stream(config.getLetrasColumnas().split(","))
                    .map(s -> s.trim().toUpperCase())
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            asientosFila.sort(Comparator
                    .comparingInt((Asiento s) -> {
                        int idx = s.getLetra() != null ? orden.indexOf(s.getLetra().trim().toUpperCase()) : -1;
                        return idx >= 0 ? idx : Integer.MAX_VALUE;
                    })
                    .thenComparing(s -> s.getLetra() != null ? s.getLetra() : ""));
            return;
        }
        asientosFila.sort(Comparator.comparing(s -> s.getLetra() != null ? s.getLetra() : ""));
    }

    private List<BloqueAsientosDTO> dividirEnBloques(List<Asiento> asientosFila, ConfiguracionCabina config) {
        List<Integer> tamanosBloque = parsearDistribucion(config != null ? config.getDistribucionColumnas() : null);
        List<String> letrasConfig = (config != null && config.getLetrasColumnas() != null)
                ? Arrays.stream(config.getLetrasColumnas().split(","))
                        .map(s -> s.trim().toUpperCase())
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList())
                : List.of();

        // Mapear cada letra configurada a su índice de bloque (pasillos según configuración de cabina)
        if (!tamanosBloque.isEmpty() && !letrasConfig.isEmpty()) {
            Map<String, Integer> bloquePorLetra = new HashMap<>();
            int cursorLetra = 0;
            for (int b = 0; b < tamanosBloque.size(); b++) {
                int cap = tamanosBloque.get(b);
                for (int k = 0; k < cap && cursorLetra < letrasConfig.size(); k++) {
                    bloquePorLetra.put(letrasConfig.get(cursorLetra++), b);
                }
            }
            List<BloqueAsientosDTO> resultado = new ArrayList<>();
            for (int b = 0; b < tamanosBloque.size(); b++) {
                resultado.add(new BloqueAsientosDTO());
            }
            for (Asiento s : asientosFila) {
                String letra = s.getLetra() != null ? s.getLetra().trim().toUpperCase() : "";
                Integer idxBloque = bloquePorLetra.get(letra);
                if (idxBloque != null && idxBloque < resultado.size()) {
                    resultado.get(idxBloque).getAsientos().add(convertirAInfoAsiento(s));
                }
            }
            resultado.removeIf(b -> b.getAsientos().isEmpty());
            if (!resultado.isEmpty()) {
                return resultado;
            }
        }

        // Respaldo si no hay configuración de letras coincidente
        List<BloqueAsientosDTO> bloques = new ArrayList<>();
        BloqueAsientosDTO bloqueActual = new BloqueAsientosDTO();
        int total = asientosFila.size();
        for (int i = 0; i < total; i++) {
            Asiento s = asientosFila.get(i);
            bloqueActual.getAsientos().add(convertirAInfoAsiento(s));
            boolean cortar = false;
            if (!tamanosBloque.isEmpty()) {
                int acumulado = 0;
                for (int b = 0; b < tamanosBloque.size() - 1; b++) {
                    acumulado += tamanosBloque.get(b);
                    if (i == acumulado - 1 && i < total - 1) {
                        cortar = true;
                        break;
                    }
                }
            } else if (total == 10) {
                cortar = (i == 2 || i == 6) && i < total - 1;
            } else if (total == 9) {
                cortar = (i == 2 || i == 5) && i < total - 1;
            } else if (total == 7 || total == 8) {
                cortar = (i == 1 || i == (total == 8 ? 5 : 4)) && i < total - 1;
            } else if (total == 6) {
                cortar = (i == 2) && i < total - 1;
            } else if (total == 4) {
                cortar = (i == 1) && i < total - 1;
            } else if (total == 3) {
                cortar = (i == 0) && i < total - 1;
            }
            if (cortar) {
                bloques.add(bloqueActual);
                bloqueActual = new BloqueAsientosDTO();
            }
        }
        if (!bloqueActual.getAsientos().isEmpty()) {
            bloques.add(bloqueActual);
        }
        return bloques;
    }

    private List<Integer> parsearDistribucion(String distribucion) {
        List<Integer> resultado = new ArrayList<>();
        if (distribucion == null || distribucion.isBlank()) {
            return resultado;
        }
        for (String parte : distribucion.split("-")) {
            try {
                int n = Integer.parseInt(parte.trim());
                if (n > 0) {
                    resultado.add(n);
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return resultado;
    }

    private InfoAsientoDTO convertirAInfoAsiento(Asiento s) {
        String clase = (s.getClaseAsiento() != null && s.getClaseAsiento().getDescripcion() != null)
                ? s.getClaseAsiento().getDescripcion()
                : "Económica";
        return new InfoAsientoDTO(
                s.getIdAsiento() != null ? s.getIdAsiento() : 0,
                s.getNumeroAsiento(),
                "libre",
                0,
                clase,
                s.getFila(),
                s.getLetra(),
                Boolean.TRUE.equals(s.getEsVentana()),
                Boolean.TRUE.equals(s.getEsPasillo()),
                Boolean.TRUE.equals(s.getEsEmergencia())
        );
    }

    public void eliminarAvion(Integer id) {
        try {
            avionService.deleteById(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave dada de baja.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se puede eliminar la aeronave. Posee itinerarios asociados.");
        }
    }

    public void reactivarAvion(Integer id) {
        try {
            avionService.reactivar(id);
            cargarTodo();
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Aeronave reactivada exitosamente.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo reactivar la aeronave: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public int getTabActiva() { return tabActiva; }
    public void setTabActiva(int tabActiva) { this.tabActiva = tabActiva; }
    // Getters Listas Administrativas
    public List<Avion> getListaAvionesAdmin() { return listaAvionesAdmin; }
    public List<ModeloAvion> getListaModelosAdmin() { return listaModelosAdmin; }
    public List<Fabricante> getListaFabricantesAdmin() { return listaFabricantesAdmin; }
    public List<ClaseAsiento> getListaClasesAdmin() { return listaClasesAdmin; }

    // Getters Listas Activas
    public List<Avion> getListaAvionesActivos() { return listaAvionesActivos; }
    public List<ModeloAvion> getListaModelosActivos() { return listaModelosActivos; }
    public List<Fabricante> getListaFabricantesActivos() { return listaFabricantesActivos; }
    public List<ClaseAsiento> getListaClasesActivas() { return listaClasesActivas; }

    // Compatibilidad retroactiva
    public List<Avion> getListaAviones() { return listaAvionesAdmin; }
    public List<ModeloAvion> getListaModelos() { return listaModelosAdmin; }
    public List<Fabricante> getListaFabricantes() { return listaFabricantesAdmin; }
    public List<ClaseAsiento> getListaClases() { return listaClasesAdmin; }
    public List<Fabricante> getFabricantesActivos() { return listaFabricantesActivos; }
    public List<ModeloAvion> getModelosActivos() { return listaModelosActivos; }
    public List<ClaseAsiento> getClasesActivas() { return listaClasesActivas; }
    public Avion getAvionSeleccionado() { return avionSeleccionado; }
    public void setAvionSeleccionado(Avion avionSeleccionado) { this.avionSeleccionado = avionSeleccionado; }
    public ModeloAvion getModeloSeleccionado() { return modeloSeleccionado; }
    public void setModeloSeleccionado(ModeloAvion modeloSeleccionado) { this.modeloSeleccionado = modeloSeleccionado; }
    public Fabricante getFabricanteSeleccionado() { return fabricanteSeleccionado; }
    public void setFabricanteSeleccionado(Fabricante fabricanteSeleccionado) { this.fabricanteSeleccionado = fabricanteSeleccionado; }
    public Integer getIdModeloParaAvion() { return idModeloParaAvion; }
    public void setIdModeloParaAvion(Integer idModeloParaAvion) { this.idModeloParaAvion = idModeloParaAvion; }
    public Integer getIdFabricanteParaModelo() { return idFabricanteParaModelo; }
    public void setIdFabricanteParaModelo(Integer idFabricanteParaModelo) { this.idFabricanteParaModelo = idFabricanteParaModelo; }
    public Date getFechaMantenimientoAux() { return fechaMantenimientoAux; }
    public void setFechaMantenimientoAux(Date fechaMantenimientoAux) { this.fechaMantenimientoAux = fechaMantenimientoAux; }
    public boolean isModoEdicionAvion() { return modoEdicionAvion; }
    public Map<Integer, Integer> getCapacidadesClaseMap() { return capacidadesClaseMap; }
    public Map<Integer, List<Asiento>> getAsientosPorFilaMap() { return asientosPorFilaMap; }
    public int getTotalAsientosCalculados() { return totalAsientosCalculados; }
    public int getAnoActual() { return LocalDate.now().getYear(); }
    public List<FilaCabinaDTO> getFilasCabinaSimulador() { return filasCabinaSimulador; }
    public boolean isAvionDoblePasilloSimulador() { return avionDoblePasilloSimulador; }
    public int getTotalAsientosSimulador() { return totalAsientosSimulador; }
    public String getDistribucionResumenSimulador() { return distribucionResumenSimulador; }
}