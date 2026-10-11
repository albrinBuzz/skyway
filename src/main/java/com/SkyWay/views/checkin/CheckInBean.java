package com.SkyWay.views.checkin;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.checkin.util.ValidadorCheckIn;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Named("checkInView")
@ViewScoped
public class CheckInBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired private ReservaService reservaService;
    @Autowired private AsientoService asientoService;

    private Integer idReserva;
    private Integer idItinerario;

    private String codigoReservaBuscado;
    private String apellidoORutBuscado;

    private int pasoActual = 0;
    private boolean aceptoDeclaracionSeguridad;
    private boolean checkInExitoso = false;

    // Estado de la ventana temporal ("HABILITADO", "NO_DISPONIBLE_AUN", "CERRADO")
    private String estadoVentanaCheckIn = "HABILITADO";
    private Date fechaSalidaVuelo;

    private List<InfoAsientoReservaDTO> listaAsientos = new ArrayList<>();
    private List<TicketInfo> listaTicketsBoardingPass = new ArrayList<>();
    private int intentosFallidos = 0;

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null && context.getExternalContext() != null) {
            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
            String paramReserva = params.get("idReserva");
            String paramItinerario = params.get("idItinerario");

            if (paramReserva != null && !paramReserva.isBlank()) {
                this.idReserva = Integer.parseInt(paramReserva);
            }
            if (paramItinerario != null && !paramItinerario.isBlank()) {
                this.idItinerario = Integer.parseInt(paramItinerario);
            }
        }

        if (idReserva != null && idItinerario != null) {
            this.pasoActual = 1;
            cargarDatosCheckIn();
        } else {
            this.pasoActual = 0;
        }
    }

    public void cargarDatosCheckIn() {
        try {
            if (idReserva != null && idItinerario != null) {
                // 1. Evaluar ventana temporal contra la hora del vuelo
                this.fechaSalidaVuelo = reservaService.obtenerFechaSalidaVuelo(idItinerario);
                this.estadoVentanaCheckIn = ValidadorCheckIn.validarEstadoVentanaCheckIn(this.fechaSalidaVuelo);

                if ("NO_DISPONIBLE_AUN".equals(estadoVentanaCheckIn)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN,
                                    "Check-In No Habilitado Aún",
                                    "El Check-In Online abre 48 horas antes del despegue."));
                } else if ("CERRADO".equals(estadoVentanaCheckIn)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                    "Check-In Web Cerrado",
                                    "El límite de tiempo para Check-In Web (60 min antes del despegue) ha expirado."));
                }

                this.listaAsientos = asientoService.getAsientosReservados(idItinerario, idReserva);
                this.listaTicketsBoardingPass = reservaService.getTicket("", idReserva, idItinerario);
            }
        } catch (Exception e) {
            Logger.logError("Error al cargar datos Check-In: " + e.getMessage());
        }
    }

    public boolean isCheckInHabilitado() {
        return "HABILITADO".equalsIgnoreCase(this.estadoVentanaCheckIn);
    }

    public void buscarReservaManual() {
        if (intentosFallidos >= 5) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Límite Superado", "Has superado el número máximo de intentos. Intenta nuevamente más tarde."));
            return;
        }

        if (codigoReservaBuscado == null || codigoReservaBuscado.isBlank() ||
                apellidoORutBuscado == null || apellidoORutBuscado.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Campos Incompletos", "Debes ingresar el código de reserva y tu apellido o RUT."));
            return;
        }

        try {
            this.idReserva = reservaService.buscarIdPorPnrYPasajero(
                    codigoReservaBuscado.trim(),
                    apellidoORutBuscado.trim()
            );

            if (this.idReserva == null) {
                intentosFallidos++;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Datos Incorrectos", "No se encontró ninguna reserva activa que coincida con los datos indicados."));
                return;
            }

            intentosFallidos = 0;
            this.idItinerario = reservaService.obtenerIdItinerarioPorReserva(this.idReserva);

            if (this.idItinerario == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Reserva", "La reserva no tiene un itinerario válido asignado."));
                return;
            }

            cargarDatosCheckIn();
            this.pasoActual = 1;

        } catch (Exception e) {
            Logger.logError("Error buscando reserva de check-in manual: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Búsqueda", "No fue posible encontrar la reserva. Verifica los datos ingresados."));
        }
    }

    public void siguientePaso() {
        // Bloqueo de seguridad en el backend
        if (!isCheckInHabilitado()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Check-In Bloqueado", "El Check-In web no se encuentra disponible actualmente para esta reserva."));
            return;
        }

        if (pasoActual == 1) {
            if (listaAsientos == null || listaAsientos.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin Asientos", "No se encontraron asientos asignados para procesar el Check-In."));
                return;
            }
            pasoActual = 2;
        } else if (pasoActual == 2) {
            if (!aceptoDeclaracionSeguridad) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Declaración Requerida", "Debes aceptar la declaración jurada de equipaje peligroso para continuar."));
                return;
            }
            procesarConfirmacionCheckIn();
        }
    }

    public void pasoAnterior() {
        if (pasoActual > 1 && !checkInExitoso) {
            pasoActual--;
        } else if (pasoActual == 1 && (codigoReservaBuscado != null && !codigoReservaBuscado.isBlank())) {
            pasoActual = 0;
        }
    }

    private void procesarConfirmacionCheckIn() {
        try {
            reservaService.procesarCheckInWeb(idReserva, idItinerario);
            cargarDatosCheckIn();

            String rutBusqueda = (apellidoORutBuscado != null && !apellidoORutBuscado.isBlank())
                    ? apellidoORutBuscado.trim() : "";
            this.listaTicketsBoardingPass = reservaService.getTicket(rutBusqueda, idReserva, idItinerario);

            this.checkInExitoso = true;
            this.pasoActual = 3;

            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Check-In Exitoso", "Tu check-in se ha realizado con éxito. Ya puedes imprimir tu Tarjeta de Embarque."));

        } catch (Exception e) {
            Logger.logError("Error procesando Check-In: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo completar el Check-In: " + e.getMessage()));
        }
    }

    // Getters y Setters
    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Integer getIdItinerario() { return idItinerario; }
    public void setIdItinerario(Integer idItinerario) { this.idItinerario = idItinerario; }

    public String getCodigoReservaBuscado() { return codigoReservaBuscado; }
    public void setCodigoReservaBuscado(String codigoReservaBuscado) { this.codigoReservaBuscado = codigoReservaBuscado; }

    public String getApellidoORutBuscado() { return apellidoORutBuscado; }
    public void setApellidoORutBuscado(String apellidoORutBuscado) { this.apellidoORutBuscado = apellidoORutBuscado; }

    public int getPasoActual() { return pasoActual; }
    public void setPasoActual(int pasoActual) { this.pasoActual = pasoActual; }

    public boolean isAceptoDeclaracionSeguridad() { return aceptoDeclaracionSeguridad; }
    public void setAceptoDeclaracionSeguridad(boolean aceptoDeclaracionSeguridad) { this.aceptoDeclaracionSeguridad = aceptoDeclaracionSeguridad; }

    public boolean isCheckInExitoso() { return checkInExitoso; }
    public void setCheckInExitoso(boolean checkInExitoso) { this.checkInExitoso = checkInExitoso; }

    public String getEstadoVentanaCheckIn() { return estadoVentanaCheckIn; }
    public Date getFechaSalidaVuelo() { return fechaSalidaVuelo; }

    public List<InfoAsientoReservaDTO> getListaAsientos() { return listaAsientos; }
    public List<TicketInfo> getListaTicketsBoardingPass() { return listaTicketsBoardingPass; }
}