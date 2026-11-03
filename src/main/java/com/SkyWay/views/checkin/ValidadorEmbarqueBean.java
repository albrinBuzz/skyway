package com.SkyWay.views.checkin;

import com.SkyWay.modules.checkin.dto.ResultadoValidacionEmbarqueDTO;
import com.SkyWay.modules.checkin.service.ValidadorEmbarqueService;
import com.SkyWay.util.BoardingPassSecurityService;
import com.SkyWay.util.Logger;
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
import java.util.Map;

/**
 * Managed Bean para el Control de Embarque y Escáner Web con Cámara (PrimeFaces / JSF).
 * Permite escanear tarjetas de embarque en tiempo real con la cámara web del dispositivo,
 * pistola lectora óptica o ingreso manual, aplicando validaciones criptográficas de autenticidad.
 */
@Named("validadorEmbarqueBean")
@ViewScoped
public class ValidadorEmbarqueBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ValidadorEmbarqueService validadorService;

    private String codigoManual;
    private String puertaActual = "SCL-I01";
    private ResultadoValidacionEmbarqueDTO resultado;
    private List<ResultadoValidacionEmbarqueDTO> historialEscaneos;

    private int contadorEscaneos = 0;
    private int contadorAutorizados = 0;
    private int contadorRechazados = 0;

    @PostConstruct
    public void init() {
        this.historialEscaneos = new ArrayList<>();
        Logger.logInfo("🛡️ [VALIDADOR EMBARQUE] Módulo de escáner y verificación inicializado.");
    }

    /**
     * Invocado de forma reactiva por JavaScript mediante <p:remoteCommand>
     * cuando la cámara web o el lector detecta un código 2D.
     */
    public void procesarEscaneoDesdeCamara() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null) return;

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String codigoLeido = params.get("codigoEscaneado");

        if (codigoLeido != null && !codigoLeido.isBlank()) {
            ejecutarValidacion(codigoLeido.trim());
        }
    }

    /**
     * Invocado cuando el operador presiona Enter o hace clic en "Validar Manualmente".
     */
    public void validarManual() {
        if (this.codigoManual == null || this.codigoManual.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Código Requerido", "Ingresa o escanea un código para validar."));
            return;
        }

        ejecutarValidacion(this.codigoManual.trim());
        this.codigoManual = "";
    }

    /**
     * Núcleo del procesamiento de validación y registro en el historial.
     */
    private void ejecutarValidacion(String codigo) {
        this.contadorEscaneos++;
        this.resultado = validadorService.validarTarjetaEmbarque(codigo, this.puertaActual);

        // Actualizar métricas de puerta
        if (this.resultado.isAutorizado()) {
            this.contadorAutorizados++;
        } else {
            this.contadorRechazados++;
        }

        // Agregar al historial visible (máximo 10 elementos recientes)
        if (this.historialEscaneos == null) {
            this.historialEscaneos = new ArrayList<>();
        }
        this.historialEscaneos.add(0, this.resultado);
        if (this.historialEscaneos.size() > 10) {
            this.historialEscaneos.remove(this.historialEscaneos.size() - 1);
        }

        // Notificación Growl
        FacesContext context = FacesContext.getCurrentInstance();
        if (this.resultado.isAutorizado()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "¡EMBARQUE AUTORIZADO!", "Pasajero: " + this.resultado.getNombrePasajero() + " | Asiento: " + this.resultado.getAsiento()));
        } else if (this.resultado.isAdulterado()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "¡CÓDIGO ADULTERADO!", this.resultado.getMensaje()));
        } else if (this.resultado.isDuplicado()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "¡ALERTA DE DUPLICADO!", this.resultado.getMensaje()));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "ACCESO DENEGADO", this.resultado.getMensaje()));
        }

        // Ejecutar feedback sonoro y visual en JavaScript
        PrimeFaces pf = PrimeFaces.current();
        if (pf != null) {
            if (this.resultado.isAutorizado()) {
                pf.executeScript("if (typeof reproducirBeepExito === 'function') reproducirBeepExito();");
            } else {
                pf.executeScript("if (typeof reproducirBeepAlerta === 'function') reproducirBeepAlerta();");
            }
        }
    }

    /**
     * Marca al pasajero formalmente como embarcado en cabina.
     */
    public void confirmarAbordaje() {
        if (this.resultado == null || !this.resultado.isAutorizado()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No Autorizado", "Solo se puede confirmar el abordaje de boletos autorizados."));
            return;
        }

        boolean ok = validadorService.registrarEmbarque(this.resultado, "Agente Puerta " + this.puertaActual);
        if (ok) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Pasajero A Bordo", "Se registró el embarque de " + this.resultado.getNombrePasajero() + " a cabina."));
            PrimeFaces pf = PrimeFaces.current();
            if (pf != null) {
                pf.executeScript("if (typeof reproducirBeepExito === 'function') reproducirBeepExito();");
            }
        }
    }

    /**
     * Prueba rápida con 1 clic para validar comportamientos del sistema.
     */
    public void probarEjemplo(String tipo) {
        if ("valido".equalsIgnoreCase(tipo)) {
            // Genera un código 100% auténtico y firmado para el pasajero registrado Cristóbal Román
            String codigoAutentico = BoardingPassSecurityService.generarCodigoSeguro("MEUJ7E", "SCL-MVD1", "2A", "21406428-6");
            ejecutarValidacion(codigoAutentico);
        } else if ("adulterado".equalsIgnoreCase(tipo)) {
            // Intento de fraude: se genera un pase legítimo y se adultera el asiento de 2A a 1A sin poseer la clave privada ECDSA
            String codigoLegitimo = BoardingPassSecurityService.generarCodigoSeguro("MEUJ7E", "SCL-MVD1", "2A", "21406428-6");
            String codigoAlterado = codigoLegitimo.replace("|2A|", "|1A|");
            ejecutarValidacion(codigoAlterado);
        } else if ("inexistente".equalsIgnoreCase(tipo)) {
            String codigoInexistente = BoardingPassSecurityService.generarCodigoSeguro("FAK99X", "SCL-MVD1", "12B", "99999999-9");
            ejecutarValidacion(codigoInexistente);
        }
    }

    public void limpiar() {
        this.resultado = null;
        this.codigoManual = "";
    }

    // Getters y Setters
    public String getCodigoManual() { return codigoManual; }
    public void setCodigoManual(String codigoManual) { this.codigoManual = codigoManual; }

    public String getPuertaActual() { return puertaActual; }
    public void setPuertaActual(String puertaActual) { this.puertaActual = puertaActual; }

    public ResultadoValidacionEmbarqueDTO getResultado() { return resultado; }
    public void setResultado(ResultadoValidacionEmbarqueDTO resultado) { this.resultado = resultado; }

    public List<ResultadoValidacionEmbarqueDTO> getHistorialEscaneos() { return historialEscaneos; }

    public int getContadorEscaneos() { return contadorEscaneos; }
    public int getContadorAutorizados() { return contadorAutorizados; }
    public int getContadorRechazados() { return contadorRechazados; }
}
