package com.SkyWay.views.reserva;



import com.SkyWay.config.pago.MetodoPagoEnum;
import com.SkyWay.config.pago.PagoFactoryService;
import com.SkyWay.config.pago.PasarelaPagoStrategy;
import com.SkyWay.config.pago.SolicitudPagoDTO;
import com.SkyWay.config.webPay.WebPayService;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.estadoreserva.domain.service.EstadoReservaService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import com.SkyWay.modules.tarifa.domain.service.TarifaService;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Named("reservaBean")
//@RequestScoped
@ViewScoped
public class ReservaBean implements Serializable {

    //private ClienteDTO cliente;
    private Map<Integer, List<InfoAsientoDTO>> asientosSeleccionados;
    List<Itinerario> itinerarios=new ArrayList<>();
    private HashMap<Integer,List<Integer>> itinerariosAsientos=new HashMap<>();
    private Integer total;

    @Autowired
    private ItinerarioService itinerarioService;
    @Autowired
    private VueloService vueloService;
    @Autowired
    private ReservaService reservaService;
    @Autowired
    private ReservaItinerarioService reservaItinerarioService;
    @Autowired
    private EstadoReservaService estadoReservaService;
    @Autowired
    private PasajeroService pasajeroService;
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TarifaService tarifaService;
    @Autowired
    private AsientoService asientoService;

    @Autowired
    private AsientoCacheService asientoCacheService;

    @Autowired
    private ItinerarioTarifaService itinerarioTarifaService;

    private Pasajero pasajero = new Pasajero();
    private HashMap<Integer,Integer>tarifasItinerarios;
    List<ReservaAsientoBean.Pasajero> pasajeros;
    List<Pasajero>pasajerosList;

    private List<EquipajePasajeroDTO> equipajePorPasajero;
    private int totalEquipaje = 0;

    @Autowired
    private HttpSession session;
    Usuario usuario;
    String miSessionId;
    @Autowired
    private EquipajeService equipajeService;

    @Value("${webpay.return-path:/webpay/commit}")
    private String returnPath;

    @Autowired
    private WebPayService webPayService;

    @Autowired
    private PagoFactoryService pagoFactoryService;

    private MetodoPagoEnum metodoPagoSeleccionado = MetodoPagoEnum.WEBPAY;
    private List<OpcionPagoView> opcionesPago;

    // Expresiones Regulares para Validaciones Rigurosas
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PASAPORTE_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,15}$");
    private static final Pattern CEDULA_PATTERN = Pattern.compile("^[0-9]{7,8}-[0-9Kk]{1}$");

    // FLAG DE MODOS: Se activa/desactiva mediante application.properties o switch manual en UI
    @Value("${app.modo-desarrollo:true}")
    private boolean modoDesarrollo;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        total = 0;

        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) {
            Logger.logInfo("❌ [ReservaBean] FacesContext o ExternalContext son nulos. Abortando init.");
            return;
        }

        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        // 1. Obtener usuario e identificador de sesión
        this.usuario = (Usuario) sessionMap.get("usuario");


        if (sessionMap.containsKey("reservaSessionId")) {
            this.miSessionId = (String) sessionMap.get("reservaSessionId");
        } else {
            HttpSession httpSession = (HttpSession) context.getExternalContext().getSession(false);
            if (httpSession != null) {
                this.miSessionId = httpSession.getId();
                sessionMap.put("reservaSessionId", this.miSessionId);
            }
        }


        // 2. Verificar itinerarios en sesión
        var idItinerarios = (List<Integer>) sessionMap.get("itinerarios");

        if (idItinerarios == null || idItinerarios.isEmpty()) {
            try {
                Logger.logInfo("⚠️ [ReservaBean] No se encontraron itinerarios en sesión al cargar reserva.xhtml. Redirigiendo a /home/index.xhtml...");
                context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/home/index.xhtml");
            } catch (Exception e) {
                Logger.logInfo("❌ [ReservaBean] Error al intentar redirigir: " + e.getMessage());
            }
            return;
        }

        // 3. Cargar Objetos de Sesión
        this.tarifasItinerarios = (HashMap<Integer, Integer>) sessionMap.get("tarifasItinerios");
        this.pasajeros = (List<ReservaAsientoBean.Pasajero>) sessionMap.get("pasajeros");
        this.asientosSeleccionados = (Map<Integer, List<InfoAsientoDTO>>) sessionMap.get("asientosSeleccionados");



        // 4. Reconstrucción de la lista de Pasajeros de la Reserva
        // 4. Reconstrucción de la lista de Pasajeros de la Reserva
        pasajerosList = new ArrayList<>();
        if (usuario != null) {
            this.pasajero = pasajeroService.findById(usuario.getRut()).orElse(new Pasajero());

            if (this.pasajero.getUsuario() == null) {
                this.pasajero.setUsuario(usuario);
            }

            if (this.pasajero.getTipoDocumento() == null || this.pasajero.getTipoDocumento().isBlank()) {
                this.pasajero.setTipoDocumento("CEDULA");
            }
            if (this.pasajero.getNumeroDocumento() == null || this.pasajero.getNumeroDocumento().isBlank()) {
                this.pasajero.setNumeroDocumento(usuario.getDocumentoIdentidad() != null ? usuario.getDocumentoIdentidad() : usuario.getRut());
            }

            if (pasajeros != null) {
                for (int i = 0; i < pasajeros.size(); i++) {
                    if (i == 0) {
                        pasajerosList.add(this.pasajero);
                    } else {
                        Pasajero p = new Pasajero();
                        p.setUsuario(new Usuario()); // 👈 Usuario explícito e independiente
                        pasajerosList.add(p);
                    }
                }
            }
        } else if (pasajeros != null) {
            for (ReservaAsientoBean.Pasajero p1 : pasajeros) {
                Pasajero p = new Pasajero();
                p.setUsuario(new Usuario()); // 👈 Usuario explícito e independiente
                pasajerosList.add(p);
            }
        }


        // 5. Cálculo y Acumulación de Totales (Itinerarios + Asientos + Equipaje)
        for (Integer id : idItinerarios) {
            var itinerario = itinerarioService.findById(id);
            if (itinerario != null) {
                total += itinerario.getPrecioBase();
                itinerarios.add(itinerario);
            } else {
                Logger.logInfo("⚠️ [ReservaBean] No se encontró la entidad Itinerario para ID #" + id);
            }
        }

        if (asientosSeleccionados != null) {
            asientosSeleccionados.forEach((idVuelo, listaAsientos) -> {
                if (listaAsientos != null) {
                    listaAsientos.forEach(a -> {
                        total += a.getPrecio();
                    });
                } else {
                    Logger.logInfo("   ⚠️ Lista de asientos nula para Vuelo #" + idVuelo);
                }
            });
        } else {
            Logger.logInfo("⚠️ [ReservaBean] No hay asientos seleccionados en la sesión.");
        }

        // 6. Equipaje
        this.equipajePorPasajero = (List<EquipajePasajeroDTO>) sessionMap.get("equipajePorPasajero");
        if (sessionMap.containsKey("totalEquipaje")) {
            this.totalEquipaje = (Integer) sessionMap.get("totalEquipaje");
            this.total += this.totalEquipaje;
        }


        // 7. Cargar Métodos de Pago
        opcionesPago = new ArrayList<>();
        opcionesPago.add(new OpcionPagoView(MetodoPagoEnum.WEBPAY, "icons/logo_webpay.png", "Webpay Plus", "Webpay"));
        opcionesPago.add(new OpcionPagoView(MetodoPagoEnum.PAYPAL, "icons/logo_paypal.png", "PayPal (USD)", "PayPal"));

    }




    public Map<Integer, List<InfoAsientoDTO>> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }


    public void mostrarPasajeros(){
        for (Pasajero pasajero1 : pasajerosList) {
            Logger.logInfo(pasajero1.toString());
        }
    }

    // Acción del botón
    public void confirmarReserva() {
        miSessionId = (String) FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().get("reservaSessionId");


        if (getTiempoRestanteSegundos() <= 0) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Tiempo Agotado",
                    "Tu tiempo de retención temporal de asientos ha expirado. Selecciona tus asientos nuevamente.");

            // Limpiar sesión y redirigir tras un breve retraso
            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getSessionMap().remove("asientosSeleccionados");

            PrimeFaces.current().executeScript("setTimeout(function(){ window.location.href='"
                    + context.getExternalContext().getRequestContextPath() + getUrlRetornoAsientos() + "'; }, 2000);");
            return;
        }


        Logger.logInfo("============ 🔍 DETALLE DE PASAJEROS EN SUBMIT ============");
        Logger.logInfo("Total Pasajeros en Lista: " + (pasajerosList != null ? pasajerosList.size() : "NULL"));

        if (pasajerosList != null) {
            for (int i = 0; i < pasajerosList.size(); i++) {
                Pasajero p = pasajerosList.get(i);
                Usuario u = p != null ? p.getUsuario() : null;

                Logger.logInfo(String.format("📋 [Pasajero %d]", i + 1));
                Logger.logInfo(String.format("   - RUT Pasajero: '%s'", p != null ? p.getRut() : "NULL"));
                Logger.logInfo(String.format("   - Tipo Doc: '%s' | N° Doc: '%s' | Nacionalidad: '%s'",
                        p != null ? p.getTipoDocumento() : "NULL",
                        p != null ? p.getNumeroDocumento() : "NULL",
                        p != null ? p.getNacionalidad() : "NULL"));

                if (u != null) {
                    Logger.logInfo(String.format("   - Usuario -> Nombre: '%s' | Apellido: '%s' | Correo: '%s' | Tel: '%s' | DocId: '%s' | F.Nac: '%s'",
                            u.getNombre(), u.getApellido(), u.getCorreoElectronico(), u.getTelefono(), u.getDocumentoIdentidad(), u.getFechaNacimiento()));
                } else {
                    Logger.logInfo("   - Usuario: NULL");
                }
            }
        }
        Logger.logInfo("==========================================================");

        // 1. VALIDACIÓN EN CACHÉ (TTL)
        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : asientosSeleccionados.entrySet()) {
            Integer idVuelo = entry.getKey();
            for (InfoAsientoDTO asiento : entry.getValue()) {
                boolean sigueValido = asientoCacheService.validarPertenenciaYSeleccion(idVuelo, asiento.getIdAsiento(), miSessionId);
                if (!sigueValido) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "Tiempo Agotado",
                            String.format("Tu reserva del asiento %s en el vuelo #%d expiró.", asiento.getNumeroAsiento(), idVuelo));
                    return;
                }
            }
        }

        // 2. VALIDACIÓN DE CAMPOS DE PASAJEROS
        if (!modoDesarrollo) {
            if (!validarPasajerosProfesional()) {
                return; // Se detiene el proceso si falla alguna validación estricta
            }
        } else {
            addMessage(FacesMessage.SEVERITY_INFO, "Modo Desarrollo Activo", "Validaciones estrictas de pasajeros omitidas para agilizar pruebas.");
        }

        // 3. VERIFICACIÓN DE DISPONIBILIDAD EN BD
        for (Map.Entry<Integer, List<InfoAsientoDTO>> entry : asientosSeleccionados.entrySet()) {
            Integer idVuelo = entry.getKey();
            for (InfoAsientoDTO asiento : entry.getValue()) {
                Integer[] asientosIds = { asiento.getIdAsiento() };
                try {
                    var resultado = asientoService.verificarDisponibilidad(idVuelo, asientosIds);
                    if (resultado != null && !resultado.isBlank()) {
                        addMessage(FacesMessage.SEVERITY_WARN, "Asiento Ocupado",
                                String.format("El asiento %s del vuelo #%d ya fue reservado por otro pasajero.", asiento.getNumeroAsiento(), idVuelo));
                        return;
                    }
                } catch (Exception e) {
                    addMessage(FacesMessage.SEVERITY_ERROR, "Error de Verificación", "Ocurrió un problema al consultar el estado de las plazas.");
                    return;
                }
            }
        }

        // 4. PERSISTIR EN SESIÓN Y PAGAR
        SolicitudReservaDTO dto = new SolicitudReservaDTO(
                this.usuario != null ? this.usuario.getRut() : null,
                this.pasajerosList,
                this.asientosSeleccionados,
                this.tarifasItinerarios,
                this.equipajePorPasajero,
                new BigDecimal(this.total),
                this.miSessionId
        );

        FacesContext.getCurrentInstance().getExternalContext()
                .getSessionMap().put("SOLICITUD_RESERVA_PENDIENTE", dto);

        pagar();
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().
                addMessage(null, new FacesMessage(severity, summary, detail));
    }


    public void cancelarPorExpiracion() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) return;

        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        // Comprobar si el tiempo expiró o si la reserva ya no tiene datos válidos
        if (getTiempoRestanteSegundos() <= 0) {
            // Verificar si tenía una búsqueda previa activa antes de limpiar
            String savedQuery = (String) sessionMap.get("ultimaBusquedaAsientosQuery");
            boolean teniaBusquedaActiva = (savedQuery != null && !savedQuery.isBlank());

            // 1. Limpiar objetos de sesión obsoletos
            sessionMap.remove("asientosSeleccionados");
            sessionMap.remove("itinerarios");
            sessionMap.remove("tarifasItinerios");
            sessionMap.remove("pasajeros");
            sessionMap.remove("equipajePorPasajero");
            sessionMap.remove("SOLICITUD_RESERVA_PENDIENTE");
            sessionMap.remove("ultimaBusquedaAsientosQuery");

            Logger.logWarn("⚠️ [ReservaBean] Sesión/asientos expirados o reserva inexistente. Redirigiendo...");

            try {
                String destino;

                // LÓGICA CLAVE: Si conservaba el query de búsqueda, fue un timeout de pago -> Volver a Asientos.
                // Si ya no existe búsqueda (reserva pagada y limpiada), volver al Home.
                if (teniaBusquedaActiva) {
                    destino = context.getExternalContext().getRequestContextPath()
                            + "/home/seleccionAsientos.xhtml?" + savedQuery + "&expired=true";
                } else {
                    destino = context.getExternalContext().getRequestContextPath() + "/home/index.xhtml";
                }

                // 2. Redirección limpia y cancelación del ciclo de vida de JSF
                context.getExternalContext().redirect(destino);
                context.responseComplete();
            } catch (Exception e) {
                Logger.logError("❌ Error al redirigir tras expiración: " + e.getMessage());
            }
        }
    }

    public String getAsientoPasajero(int idAsiento,String numeroAsiento){
        for (ReservaAsientoBean.Pasajero pasajero1 : pasajeros) {

            for (ReservaAsientoBean.AsientoSeleccionado asiento : pasajero1.getAsientos()) {

                if (asiento.getIdAsiento()==idAsiento&&asiento.getNumeroAsiento().equals(numeroAsiento)){
                    return pasajero1.getNombre();
                }
            }

        }
        return "";
    }


    /**
     * Validador estricto y profesional para la lista de pasajeros
     */
    private boolean validarPasajerosProfesional() {
        for (int i = 0; i < pasajerosList.size(); i++) {
            Pasajero p = pasajerosList.get(i);
            int num = i + 1;
            Usuario u = p.getUsuario();

            if (p.getRut() == null || !validarRutChileno(p.getRut())) {
                addMessage(FacesMessage.SEVERITY_WARN, "RUT Inválido", "Pasajero " + num + ": El RUT ingresado no es válido.");
                return false;
            }

            if (u == null || u.getNombre() == null || u.getNombre().trim().length() < 2) {
                addMessage(FacesMessage.SEVERITY_WARN, "Nombre Inválido", "Pasajero " + num + ": Ingrese un nombre válido.");
                return false;
            }

            if (u.getApellido() == null || u.getApellido().trim().length() < 2) {
                addMessage(FacesMessage.SEVERITY_WARN, "Apellido Inválido", "Pasajero " + num + ": Ingrese un apellido válido.");
                return false;
            }

            if (u.getCorreoElectronico() == null || !EMAIL_PATTERN.matcher(u.getCorreoElectronico()).matches()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Correo Inválido", "Pasajero " + num + ": Ingrese un correo electrónico válido.");
                return false;
            }

            if (p.getTipoDocumento() == null || p.getTipoDocumento().isBlank()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Documento Requerido", "Pasajero " + num + ": Debe seleccionar tipo de documento.");
                return false;
            }

            if (p.getNumeroDocumento() == null || p.getNumeroDocumento().isBlank()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Documento Requerido", "Pasajero " + num + ": Ingrese el número de documento.");
                return false;
            }

            if ("PASAPORTE".equalsIgnoreCase(p.getTipoDocumento()) && !PASAPORTE_PATTERN.matcher(p.getNumeroDocumento()).matches()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Pasaporte Inválido", "Pasajero " + num + ": Formato de pasaporte incorrecto (6-15 caracteres alfanuméricos).");
                return false;
            }
        }
        return true;
    }

    /**
     * Algoritmo Módulo 11 para la verificación del RUT Chileno
     */
    private boolean validarRutChileno(String rut) {
        if (rut == null) return false;
        String rutLimpio = rut.replace(".", "").replace("-", "").toUpperCase().trim();
        if (rutLimpio.length() < 8) return false;

        try {
            char dv = rutLimpio.charAt(rutLimpio.length() - 1);
            int numRut = Integer.parseInt(rutLimpio.substring(0, rutLimpio.length() - 1));

            int m = 0, s = 1;
            for (; numRut != 0; numRut /= 10) {
                s = (s + numRut % 10 * (9 - m++ % 6)) % 11;
            }
            char dvEsperado = (char) (s != 0 ? s + 47 : 75);
            return dv == dvEsperado;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void pagar() {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

            String scheme = request.getScheme();
            String serverName = request.getServerName();
            int serverPort = request.getServerPort();
            String contextPath = request.getContextPath();

            String portSegment = ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
                    ? "" : ":" + serverPort;
            String baseUrl = String.format("%s://%s%s%s", scheme, serverName, portSegment, contextPath);

            // Generar DTO Unificado
            SolicitudPagoDTO solicitud = new SolicitudPagoDTO();
            solicitud.setOrdenCompra("ORD-" + System.currentTimeMillis() % 1000000000L);
            solicitud.setSessionId(this.miSessionId != null ? this.miSessionId : request.getSession().getId());
            solicitud.setMonto(BigDecimal.valueOf(this.total));
            solicitud.setDescripcion("Reserva de vuelo SkyWay");

            // Ajustar moneda si es PayPal (ejemplo: conversión a USD si tu base es CLP)
            if (metodoPagoSeleccionado == MetodoPagoEnum.PAYPAL) {
                solicitud.setMoneda("USD");
                solicitud.setMonto(BigDecimal.valueOf(this.total / 950.0).setScale(2, RoundingMode.HALF_UP)); // Conversión de CLP a USD
                solicitud.setReturnUrl(baseUrl + "/paypal/commit");
                solicitud.setCancelUrl(baseUrl + "/paypal/cancel");
            } else {
                solicitud.setMoneda("CLP");
                solicitud.setReturnUrl(baseUrl + "/webpay/commit");
                solicitud.setCancelUrl(baseUrl + "/reserva.xhtml");
            }

            // Obtener la estrategia adecuada (Webpay, MercadoPago o PayPal)
            PasarelaPagoStrategy pasarela = pagoFactoryService.obtenerEstrategia(metodoPagoSeleccionado);

            // Iniciar transacción y redirigir a la URL correspondiente
            String redirectUrl = pasarela.iniciarTransaccion(solicitud);
            externalContext.redirect(redirectUrl);

        } catch (Exception e) {
            Logger.logInfo(e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Pago", "No fue posible procesar la solicitud con el medio de pago seleccionado.");
        }
    }

    public long getTiempoRestanteSegundos() {
        if (miSessionId == null || miSessionId.isBlank()) {
            return 0;
        }
        long restanteMs = asientoCacheService.getTiempoRestanteMsParaSesion(miSessionId);
        return restanteMs > 0 ? (restanteMs / 1000) : 0;
    }

    /**
     * Genera la URL dinámica para retornar a la selección de asientos preservando los parámetros de búsqueda.
     */
    public String getUrlRetornoAsientos() {
        FacesContext context = FacesContext.getCurrentInstance();
        String query = "";

        if (context != null && context.getExternalContext() != null) {
            String savedQuery = (String) context.getExternalContext().getSessionMap().get("ultimaBusquedaAsientosQuery");
            if (savedQuery != null && !savedQuery.isBlank()) {
                query = "?" + savedQuery + "&expired=true";
            } else {
                query = "?expired=true";
            }
        }

        return "/home/seleccionAsientos.xhtml" + query;
    }

    public String getUrlModificarAsientosVoluntario() {
        FacesContext context = FacesContext.getCurrentInstance();
        String savedQuery = (String) context.getExternalContext().getSessionMap().get("ultimaBusquedaAsientosQuery");
        return "/home/seleccionAsientos.xhtml" + (savedQuery != null && !savedQuery.isBlank() ? "?" + savedQuery : "");
    }



    /**
     * Retorna la suma total de asientos seleccionados de todos los vuelos de forma segura.
     */
    public int getTolerantTotalAsientos() {
        if (asientosSeleccionados == null || asientosSeleccionados.isEmpty()) {
            return 0;
        }
        int totalCount = 0;
        for (List<InfoAsientoDTO> lista : asientosSeleccionados.values()) {
            if (lista != null) {
                totalCount += lista.size();
            }
        }
        return totalCount;
    }

    public String getRutPasajero(int idAsiento,String numeroAsiento){

        for (int i = 0; i < pasajeros.size(); i++) {

            for (int i1 = 0; i1 < pasajeros.get(i).asientos.size(); i1++) {

                if (pasajeros.get(i).asientos.get(i1).idAsiento==idAsiento&&pasajeros.get(i).asientos.get(i1).numeroAsiento.equals(numeroAsiento)){

                   return pasajerosList.get(i).getRut();
                }
            }
        }

        return "";
    }

    public String getVuelo(Integer idVuelo){
        return vueloService.findById(idVuelo).get().getNumeroVuelo();
    }

    public List<Itinerario> getItinerarios() {
        return itinerarios;
    }

    public Integer getTotal() {
        return total;
    }

    public Pasajero getPasajero() {
        return pasajero;
    }

    public void setPasajero(Pasajero pasajero) {
        this.pasajero = pasajero;
    }

    public List<ReservaAsientoBean.Pasajero> getPasajeros() {
        return pasajeros;
    }

    public List<Pasajero> getPasajerosList() {
        return pasajerosList;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getMiSessionId() {
        return miSessionId;
    }

    public void setMiSessionId(String miSessionId) {
        this.miSessionId = miSessionId;
    }

    public List<EquipajePasajeroDTO> getEquipajePorPasajero() {
        return equipajePorPasajero;
    }

    public MetodoPagoEnum getMetodoPagoSeleccionado() {
        return metodoPagoSeleccionado;
    }

    public void setMetodoPagoSeleccionado(MetodoPagoEnum metodoPagoSeleccionado) {
        this.metodoPagoSeleccionado = metodoPagoSeleccionado;
    }



    // DTO interno para el renderizado en vista
    public static class OpcionPagoView {
        private MetodoPagoEnum metodo;
        private String imageName;
        private String label;
        private String alt;

        public OpcionPagoView(MetodoPagoEnum metodo, String imageName, String label, String alt) {
            this.metodo = metodo;
            this.imageName = imageName;
            this.label = label;
            this.alt = alt;
        }

        public MetodoPagoEnum getMetodo() { return metodo; }
        public String getImageName() { return imageName; }
        public String getLabel() { return label; }
        public String getAlt() { return alt; }
    }

    public List<OpcionPagoView> getOpcionesPago() { return opcionesPago; }

    public int getTotalEquipaje() {
        return totalEquipaje;
    }

    public Date getFechaMaxNacimiento() {
        return java.sql.Date.valueOf(LocalDate.now());
    }

    // O si trabajas con LocalDate directamente:
    public LocalDate getFechaMaxNacimientoLocalDate() {
        return LocalDate.now();
    }

    /**
     * Retorna la fecha máxima permitida para cumplir 18 años hoy.
     * Ejemplo: Si hoy es 25/10/2026, la fecha máxima será 25/10/2008.
     */
    public Date getFechaMaxMayorEdad() {
        LocalDate limiteMayorEdad = LocalDate.now().minusYears(18);
        return Date.from(limiteMayorEdad.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public boolean esMayorDeEdad(Date fechaNacimiento) {
        if (fechaNacimiento == null) return false;

        LocalDate nacimiento = fechaNacimiento.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate hoy = LocalDate.now();

        return Period.between(nacimiento, hoy).getYears() >= 18;
    }

    /**
     * Retorna verdadero si al menos un pasajero tiene equipaje asignado
     */
    public boolean isTieneEquipajeAsignado() {
        if (equipajePorPasajero == null || equipajePorPasajero.isEmpty()) {
            return false;
        }
        return equipajePorPasajero.stream()
                .anyMatch(dto -> dto.getMaletas() != null && !dto.getMaletas().isEmpty());
    }


}
