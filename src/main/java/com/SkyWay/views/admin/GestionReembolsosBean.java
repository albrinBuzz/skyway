package com.SkyWay.views.admin;

import com.SkyWay.modules.reembolso.domain.service.ReembolsoService;
import com.SkyWay.modules.reembolso.presentation.dto.PayoutResponseDTO;
import com.SkyWay.modules.reserva.presentation.dto.ReembolsoPasajeroDTO;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("gestionReembolsosBean")
@ViewScoped
public class GestionReembolsosBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ReembolsoService reembolsoService;

    private List<ReembolsoPasajeroDTO> todosReembolsos = new ArrayList<>();
    private List<ReembolsoPasajeroDTO> reembolsosFiltrados = new ArrayList<>();
    private String filtroEstado = "TODOS";
    private String textoBusqueda = "";

    private ReembolsoPasajeroDTO reembolsoActual;
    private String observacionModal;
    private String escenarioSandbox = "APROBADO";
    private PayoutResponseDTO ultimaRespuestaBanco;

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) {
            return;
        }
        recargar();
    }

    public void recargar() {
        this.todosReembolsos = reembolsoService.listarTodosReembolsosAdmin();
        aplicarFiltros();
    }

    public void aplicarFiltros() {
        if (this.todosReembolsos == null) {
            this.reembolsosFiltrados = new ArrayList<>();
            return;
        }
        String estadoSel = this.filtroEstado != null ? this.filtroEstado.trim().toUpperCase() : "TODOS";
        String term = this.textoBusqueda != null ? this.textoBusqueda.trim().toLowerCase() : "";

        this.reembolsosFiltrados = this.todosReembolsos.stream()
                .filter(r -> "TODOS".equals(estadoSel) || estadoSel.equalsIgnoreCase(r.getEstadoReembolso()))
                .filter(r -> {
                    if (term.isEmpty()) return true;
                    String pnr = r.getCodigoPnr() != null ? r.getCodigoPnr().toLowerCase() : "";
                    String rut = r.getRutSolicitante() != null ? r.getRutSolicitante().toLowerCase() : "";
                    String nom = r.getNombreSolicitante() != null ? r.getNombreSolicitante().toLowerCase() : "";
                    String bco = r.getBanco() != null ? r.getBanco().toLowerCase() : "";
                    String op = r.getCodigoOperacion() != null ? r.getCodigoOperacion().toLowerCase() : "";
                    return pnr.contains(term) || rut.contains(term) || nom.contains(term) || bco.contains(term) || op.contains(term);
                })
                .collect(Collectors.toList());
    }

    public void abrirGestionReembolso(ReembolsoPasajeroDTO rem) {
        this.reembolsoActual = rem;
        this.observacionModal = (rem != null && rem.getObservacionAdmin() != null) ? rem.getObservacionAdmin() : "";
        this.escenarioSandbox = "APROBADO";
        this.ultimaRespuestaBanco = null;
    }

    public void confirmarDatosBancarios() {
        if (this.reembolsoActual == null || this.reembolsoActual.getIdReembolso() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No hay reembolso seleccionado.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }
        try {
            reembolsoService.confirmarDatosBancariosAdmin(
                    this.reembolsoActual.getIdReembolso(),
                    obtenerRutUsuarioSesion(),
                    this.observacionModal
            );
            recargar();
            sincronizarActualPorId(this.reembolsoActual.getIdReembolso());
            addMessage(FacesMessage.SEVERITY_INFO, "Datos Confirmados",
                    "La cuenta bancaria del reembolso #" + this.reembolsoActual.getIdReembolso()
                            + " quedó confirmada y lista para transferencia.");
        } catch (Exception e) {
            Logger.logError("Error confirmando datos bancarios: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "No permitido", e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void rechazarDatosBancarios() {
        if (this.reembolsoActual == null || this.reembolsoActual.getIdReembolso() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No hay reembolso seleccionado.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }
        if (this.observacionModal == null || this.observacionModal.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Motivo requerido",
                    "Indique la observación para que el pasajero sepa qué corregir en su cuenta bancaria.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }
        try {
            reembolsoService.rechazarDatosBancariosAdmin(
                    this.reembolsoActual.getIdReembolso(),
                    obtenerRutUsuarioSesion(),
                    this.observacionModal
            );
            recargar();
            sincronizarActualPorId(this.reembolsoActual.getIdReembolso());
            addMessage(FacesMessage.SEVERITY_WARN, "Datos Observados",
                    "Se notificó al pasajero para que corrija su cuenta bancaria.");
        } catch (Exception e) {
            Logger.logError("Error observando datos bancarios: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    public void ejecutarReembolsoSimulado() {
        if (this.reembolsoActual == null || this.reembolsoActual.getIdReembolso() == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No hay reembolso seleccionado.");
            FacesContext.getCurrentInstance().validationFailed();
            return;
        }
        try {
            PayoutResponseDTO resp = reembolsoService.ejecutarReembolsoSimuladoAdmin(
                    this.reembolsoActual.getIdReembolso(),
                    obtenerRutUsuarioSesion(),
                    this.observacionModal,
                    this.escenarioSandbox
            );
            this.ultimaRespuestaBanco = resp;
            recargar();
            sincronizarActualPorId(this.reembolsoActual.getIdReembolso());

            if (resp.isAprobado()) {
                addMessage(FacesMessage.SEVERITY_INFO, "Devolución realizada",
                        "Comprobante: " + resp.getCodigoOperacion() + ". " + resp.getMensajeBanco());
            } else {
                boolean esRechazoCuenta = "14".equals(resp.getCodigoRespuestaBanco());
                addMessage(esRechazoCuenta ? FacesMessage.SEVERITY_ERROR : FacesMessage.SEVERITY_WARN,
                        esRechazoCuenta ? "El banco rechazó la transferencia" : "Sin respuesta del banco",
                        resp.getMensajeBanco());
                FacesContext.getCurrentInstance().validationFailed();
            }
        } catch (Exception e) {
            Logger.logError("Error ejecutando reembolso simulado: " + e.getMessage());
            addMessage(FacesMessage.SEVERITY_ERROR, "No permitido", e.getMessage());
            FacesContext.getCurrentInstance().validationFailed();
        }
    }

    private void sincronizarActualPorId(Integer idReembolso) {
        if (idReembolso == null || this.todosReembolsos == null) return;
        this.todosReembolsos.stream()
                .filter(r -> idReembolso.equals(r.getIdReembolso()))
                .findFirst()
                .ifPresent(r -> this.reembolsoActual = r);
    }

    private String obtenerRutUsuarioSesion() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return null;
        Object sess = fc.getExternalContext().getSession(false);
        if (sess instanceof HttpSession session && session.getAttribute("usuario") instanceof Usuario usr) {
            return usr.getRut();
        }
        return null;
    }

    private void addMessage(FacesMessage.Severity sev, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(sev, summary, detail));
        }
    }

    public long getCountPorEstado(String estado) {
        if (todosReembolsos == null) return 0L;
        return todosReembolsos.stream()
                .filter(r -> estado.equalsIgnoreCase(r.getEstadoReembolso()))
                .count();
    }

    public BigDecimal getMontoTotalPorEstado(String estado) {
        if (todosReembolsos == null) return BigDecimal.ZERO;
        return todosReembolsos.stream()
                .filter(r -> estado.equalsIgnoreCase(r.getEstadoReembolso()))
                .map(r -> r.getMontoReembolso() != null ? r.getMontoReembolso() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getEtiquetaEstado(String estado) {
        if (estado == null) return "Solicitado";
        return switch (estado.trim().toUpperCase()) {
            case "DATOS_BANCARIOS_ENVIADOS" -> "Por Confirmar Datos";
            case "DATOS_CONFIRMADOS" -> "Confirmado (Pagar)";
            case "REEMBOLSADO" -> "Reembolsado";
            case "RECHAZADO" -> "Observado";
            default -> "Falta Cuenta Bancaria";
        };
    }

    public String getEstiloBadge(String estado) {
        if (estado == null) return "background: rgba(245, 158, 11, 0.2); border: 1px solid #f59e0b; color: #fde68a;";
        return switch (estado.trim().toUpperCase()) {
            case "REEMBOLSADO" -> "background: rgba(16, 185, 129, 0.2); border: 1px solid #10b981; color: #6ee7b7;";
            case "DATOS_CONFIRMADOS" -> "background: rgba(59, 130, 246, 0.22); border: 1px solid #3b82f6; color: #93c5fd;";
            case "DATOS_BANCARIOS_ENVIADOS" -> "background: rgba(14, 165, 233, 0.2); border: 1px solid #0ea5e9; color: #7dd3fc;";
            case "RECHAZADO" -> "background: rgba(239, 68, 68, 0.22); border: 1px solid #ef4444; color: #fca5a5;";
            default -> "background: rgba(245, 158, 11, 0.2); border: 1px solid #f59e0b; color: #fde68a;";
        };
    }

    public List<ReembolsoPasajeroDTO> getReembolsosFiltrados() { return reembolsosFiltrados; }
    public String getFiltroEstado() { return filtroEstado; }
    public void setFiltroEstado(String filtroEstado) { this.filtroEstado = filtroEstado; }
    public String getTextoBusqueda() { return textoBusqueda; }
    public void setTextoBusqueda(String textoBusqueda) { this.textoBusqueda = textoBusqueda; }
    public ReembolsoPasajeroDTO getReembolsoActual() { return reembolsoActual; }
    public String getObservacionModal() { return observacionModal; }
    public void setObservacionModal(String observacionModal) { this.observacionModal = observacionModal; }
    public String getEscenarioSandbox() { return escenarioSandbox; }
    public void setEscenarioSandbox(String escenarioSandbox) { this.escenarioSandbox = escenarioSandbox; }
    public PayoutResponseDTO getUltimaRespuestaBanco() { return ultimaRespuestaBanco; }
}
