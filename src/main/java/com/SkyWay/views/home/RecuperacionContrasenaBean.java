package com.SkyWay.views.home;

import com.SkyWay.modules.usuario.domain.service.RecuperacionContrasenaService;
import com.SkyWay.modules.usuario.domain.service.RecuperacionContrasenaService.ResultadoOperacion;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * Backing bean JSF para el flujo público de recuperación de contraseña por token en base de datos.
 */
@Component
@Named("recuperacionContrasenaBean")
@ViewScoped
public class RecuperacionContrasenaBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String PASO_SOLICITAR = "SOLICITAR";
    public static final String PASO_VERIFICAR = "VERIFICAR";
    public static final String PASO_COMPLETADO = "COMPLETADO";

    private final transient RecuperacionContrasenaService recuperacionService;

    private String pasoActual = PASO_SOLICITAR;
    private String correo;
    private String token;
    private boolean tokenPrevalidado;
    private String nuevaContrasena;
    private String confirmarContrasena;
    private long segundosEsperaReenvio;

    public RecuperacionContrasenaBean(RecuperacionContrasenaService recuperacionService) {
        this.recuperacionService = recuperacionService;
    }

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) {
            return;
        }
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        if (params == null) {
            return;
        }

        String tokenParam = params.get("token");
        if (!StringUtils.hasText(tokenParam)) {
            return;
        }

        String tokenLimpio = tokenParam.trim();
        ResultadoOperacion validacion = recuperacionService.validarToken(tokenLimpio);
        if (validacion.exitoso()) {
            this.token = tokenLimpio;
            this.tokenPrevalidado = true;
            this.pasoActual = PASO_VERIFICAR;
        } else {
            this.pasoActual = PASO_SOLICITAR;
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "Enlace no válido", validacion.mensaje());
        }
    }

    /**
     * Solicita el envío de un enlace y token criptográfico de recuperación al correo ingresado.
     */
    public void solicitarCodigo() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResultadoOperacion resultado = recuperacionService.solicitarRecuperacion(
                correo,
                construirUrlBase(fc),
                obtenerRequest(fc)
        );
        this.segundosEsperaReenvio = resultado.segundosEsperaReenvio();

        if (resultado.exitoso()) {
            this.pasoActual = PASO_VERIFICAR;
            this.token = null;
            this.tokenPrevalidado = false;
            this.nuevaContrasena = null;
            this.confirmarContrasena = null;
            agregarMensaje(fc, FacesMessage.SEVERITY_INFO, "Solicitud enviada", resultado.mensaje());
        } else {
            agregarMensaje(fc, FacesMessage.SEVERITY_WARN, "Atención", resultado.mensaje());
        }
    }

    /**
     * Reenvía un nuevo token de recuperación manteniendo la vista actual.
     */
    public void reenviarCodigo() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResultadoOperacion resultado = recuperacionService.solicitarRecuperacion(
                correo,
                construirUrlBase(fc),
                obtenerRequest(fc)
        );
        this.segundosEsperaReenvio = resultado.segundosEsperaReenvio();

        FacesMessage.Severity severidad = resultado.exitoso()
                ? FacesMessage.SEVERITY_INFO
                : FacesMessage.SEVERITY_WARN;
        agregarMensaje(fc, severidad, resultado.exitoso() ? "Enlace reenviado" : "Espera requerida", resultado.mensaje());
    }

    /**
     * Valida en base de datos el token ingresado manualmente antes de habilitar los campos de nueva contraseña.
     */
    public void validarTokenIngresado() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String tokenLimpio = token != null ? token.trim() : "";
        ResultadoOperacion validacion = recuperacionService.validarToken(tokenLimpio);

        if (validacion.exitoso()) {
            this.token = tokenLimpio;
            this.tokenPrevalidado = true;
            this.nuevaContrasena = null;
            this.confirmarContrasena = null;
        } else {
            this.tokenPrevalidado = false;
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "Token no válido", validacion.mensaje());
        }
    }

    /**
     * Verifica el token contra la base de datos y actualiza la contraseña del usuario.
     */
    public void restablecerContrasena() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ResultadoOperacion resultado = recuperacionService.restablecerContrasena(
                token,
                nuevaContrasena,
                confirmarContrasena,
                obtenerRequest(fc)
        );

        if (resultado.exitoso()) {
            this.pasoActual = PASO_COMPLETADO;
            this.token = null;
            this.tokenPrevalidado = false;
            this.nuevaContrasena = null;
            this.confirmarContrasena = null;
        } else {
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "No se pudo restablecer", resultado.mensaje());
        }
    }

    /**
     * Regresa al primer paso para solicitar un nuevo enlace de recuperación.
     */
    public void volverASolicitar() {
        this.pasoActual = PASO_SOLICITAR;
        this.token = null;
        this.tokenPrevalidado = false;
        this.nuevaContrasena = null;
        this.confirmarContrasena = null;
    }

    public boolean isPasoSolicitar() {
        return PASO_SOLICITAR.equals(pasoActual);
    }

    public boolean isPasoVerificar() {
        return PASO_VERIFICAR.equals(pasoActual);
    }

    public boolean isPasoCompletado() {
        return PASO_COMPLETADO.equals(pasoActual);
    }

    public String getPasoActual() {
        return pasoActual;
    }

    public void setPasoActual(String pasoActual) {
        this.pasoActual = pasoActual;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isTokenPrevalidado() {
        return tokenPrevalidado;
    }

    public void setTokenPrevalidado(boolean tokenPrevalidado) {
        this.tokenPrevalidado = tokenPrevalidado;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }

    public long getSegundosEsperaReenvio() {
        return segundosEsperaReenvio;
    }

    public void setSegundosEsperaReenvio(long segundosEsperaReenvio) {
        this.segundosEsperaReenvio = segundosEsperaReenvio;
    }

    private void agregarMensaje(FacesContext fc, FacesMessage.Severity severidad, String resumen, String detalle) {
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severidad, resumen, detalle));
        }
    }

    private HttpServletRequest obtenerRequest(FacesContext fc) {
        if (fc == null || fc.getExternalContext() == null) {
            return null;
        }
        Object req = fc.getExternalContext().getRequest();
        return req instanceof HttpServletRequest httpReq ? httpReq : null;
    }

    private String construirUrlBase(FacesContext fc) {
        if (fc == null || fc.getExternalContext() == null) {
            return "";
        }
        ExternalContext ec = fc.getExternalContext();
        String scheme = ec.getRequestScheme();
        String host = ec.getRequestServerName();
        int port = ec.getRequestServerPort();
        String contextPath = ec.getRequestContextPath() != null ? ec.getRequestContextPath() : "";

        boolean puertoPorDefecto = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        String segmentoPuerto = puertoPorDefecto ? "" : ":" + port;
        return scheme + "://" + host + segmentoPuerto + contextPath;
    }
}
