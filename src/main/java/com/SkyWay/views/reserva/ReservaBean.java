package com.SkyWay.views.reserva;



import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;
import com.SkyWay.config.webPay.WebPayService;
import com.SkyWay.config.webPay.entity.WebPayTransactionRequest;
import com.SkyWay.config.webPay.entity.WebPayTransactionResponse;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.estadoreserva.domain.service.EstadoReservaService;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.model.Reserva;
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
import jakarta.faces.application.FacesMessage;
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
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

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

    // Simulamos una inyección de un servicio (puedes usar @Inject si usas CDI)
    // @Inject
    // private ReservaService reservaService;
    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        total = 0;
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) return;

        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();


        // Obtener usuario autenticado o ID de sesión de manera SEGURA
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



        var idItinerarios = (List<Integer>) sessionMap.get("itinerarios");
        this.tarifasItinerarios = (HashMap<Integer, Integer>) sessionMap.get("tarifasItinerios");
        this.pasajeros = (List<ReservaAsientoBean.Pasajero>) sessionMap.get("pasajeros");

        pasajerosList = new ArrayList<>();

        if (usuario != null) {
            this.pasajero = pasajeroService.findById(usuario.getRut()).orElse(new Pasajero());
            if (pasajeros != null) {
                for (int i = 0; i < pasajeros.size(); i++) {
                    if (i == 0) {
                        pasajerosList.add(this.pasajero);
                    } else {
                        Pasajero p = new Pasajero();
                        p.setUsuario(new Usuario());
                        pasajerosList.add(p);
                    }
                }
            }
        } else if (pasajeros != null) {
            for (ReservaAsientoBean.Pasajero p1 : pasajeros) {
                Pasajero p = new Pasajero();
                p.setUsuario(new Usuario());
                pasajerosList.add(p);
            }
        }

        if (idItinerarios == null) return;

        this.asientosSeleccionados = (Map<Integer, List<InfoAsientoDTO>>) sessionMap.get("asientosSeleccionados");

        for (Integer id : idItinerarios) {
            var itinerario = itinerarioService.findById(id);
            if (itinerario != null) {
                total += itinerario.getPrecioBase();
                itinerarios.add(itinerario);
            }
        }

        if (asientosSeleccionados != null) {
            asientosSeleccionados.forEach((idVuelo, listaAsientos) -> {
                if (listaAsientos != null) {
                    listaAsientos.forEach(a -> total += a.getPrecio());
                }
            });
        }

        this.equipajePorPasajero = (List<EquipajePasajeroDTO>) sessionMap.get("equipajePorPasajero");

        if (sessionMap.containsKey("totalEquipaje")) {
            this.totalEquipaje = (Integer) sessionMap.get("totalEquipaje");
            this.total += this.totalEquipaje; // Acumular al costo final del vuelo
        }

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

        for (Pasajero pasajero : pasajerosList) {
            if (pasajero.getRut() == null || pasajero.getRut().isBlank() ||
                    pasajero.getUsuario() == null || pasajero.getUsuario().getNombre() == null || pasajero.getUsuario().getNombre().isBlank()) {
                addMessage(FacesMessage.SEVERITY_WARN, "Formulario Incompleto",
                        "Por favor, complete todos los campos obligatorios de cada pasajero.");
                return;
            }
        }

        // 2. VERIFICACIÓN DE DISPONIBILIDAD EN BD
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

        // 3. PERSISTIR EN SESIÓN Y PAGAR
        // 3. PERSISTIR EN SESIÓN Y PAGAR
        SolicitudReservaDTO dto = new SolicitudReservaDTO(
                this.usuario != null ? this.usuario.getRut() : null,
                this.pasajerosList,
                this.asientosSeleccionados,
                this.tarifasItinerarios,
                this.equipajePorPasajero,
                new BigDecimal(this.total),
                this.miSessionId
        );

        // 3. Dejar el DTO en la HttpSession nativa
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
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

        // Limpiar selección previa
        sessionMap.remove("asientosSeleccionados");

        Logger.logInfo("⚠️ Reserva cancelada automáticamente en checkout por expiración de TTL.");
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




    public void pagar() {
        try {
            // 1. Obtención de URL Base dinámica (http/https, dominio y puerto actual)
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();

            String scheme = request.getScheme();             // http o https
            String serverName = request.getServerName();     // localhost o midominio.cl
            int serverPort = request.getServerPort();       // 8080, 443, etc.
            String contextPath = request.getContextPath();   // /tu-app (si aplica)

            // Construye p.ej. http://localhost:8080/webpay/commit o https://skyway.cl/webpay/commit
            String portSegment = ((scheme.equals("http") && serverPort == 80) || (scheme.equals("https") && serverPort == 443))
                    ? "" : ":" + serverPort;
            String returnUrl = String.format("%s://%s%s%s%s", scheme, serverName, portSegment, contextPath, returnPath);

            // 2. Generación de BuyOrder y SessionId únicos (Transbank permite máx 26 caracteres para buyOrder)
            // Ejemplo: ORDEN-171542839210-942
            String buyOrder = "ORD-" + System.currentTimeMillis() % 1000000000L + "-" + UUID.randomUUID().toString().substring(0, 4);

            // Asignar el ID de sesión de JSF/Servlet o usar el ID dinámico creado previamente
            String currentSessionId = (this.miSessionId != null && !this.miSessionId.isBlank())
                    ? this.miSessionId
                    : request.getSession().getId();

            // 3. Crear transacción usando el servicio refactorizado
            WebpayPlusTransactionCreateResponse response = webPayService.createTransaction(
                    buyOrder,
                    currentSessionId,
                    this.total, // valor double o BigDecimal del total
                    returnUrl
            );

            Logger.logInfo("WebPay Transaction iniciada - Order: " + buyOrder + " | Token: " + response.getToken());

            // 4. Redirección al formulario/sitio de Webpay
            // Transbank retorna la URL de pago en response.getUrl() y el token en response.getToken()
            String redirectUrl = response.getUrl() + "?token_ws=" + response.getToken();
            externalContext.redirect(redirectUrl);

        } catch (Exception e) {
            Logger.logError("Error al iniciar pago en WebPay: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error de Pago", "No fue posible conectar con la pasarela de pago. Intente nuevamente.");
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

    public int getTotalEquipaje() {
        return totalEquipaje;
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
