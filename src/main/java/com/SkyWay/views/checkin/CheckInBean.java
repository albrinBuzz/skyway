package com.SkyWay.views.checkin;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.TarifaCaracteristica.infrastructure.validator.TarifaCaracteristicaValidator;
import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoReservaDTO;
import com.SkyWay.modules.checkin.util.ValidadorCheckIn;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reserva.presentation.dto.TicketInfo;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import com.SkyWay.modules.reserva.domain.service.ETicketPdfGeneratorService;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.servlet.http.HttpSession;
import com.SkyWay.util.BarcodeGeneratorUtil;
import java.io.ByteArrayInputStream;
import java.util.Optional;

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
    @Autowired private ETicketPdfGeneratorService eTicketPdfGeneratorService;
    @Autowired private TarifaCaracteristicaValidator tarifaValidator;

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
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();

            // Inyección segura de parámetros desde la sesión del servidor (previene manipulación de URLs)
            if (sessionMap.containsKey("CHECKIN_ID_RESERVA")) {
                Object r = sessionMap.remove("CHECKIN_ID_RESERVA");
                if (r instanceof Integer) {
                    this.idReserva = (Integer) r;
                } else if (r != null) {
                    try { this.idReserva = Integer.parseInt(r.toString()); } catch (NumberFormatException ignored) {}
                }
            }
            if (sessionMap.containsKey("CHECKIN_ID_ITINERARIO")) {
                Object it = sessionMap.remove("CHECKIN_ID_ITINERARIO");
                if (it instanceof Integer) {
                    this.idItinerario = (Integer) it;
                } else if (it != null) {
                    try { this.idItinerario = Integer.parseInt(it.toString()); } catch (NumberFormatException ignored) {}
                }
            }
        }

        if (idReserva != null && idItinerario != null) {
            cargarDatosCheckIn();
            if (this.checkInExitoso) {
                this.pasoActual = 3;
            } else {
                this.pasoActual = 1;
            }
        } else {
            this.pasoActual = 0;
        }
    }

    public void cargarDatosCheckIn() {
        try {
            if (idReserva != null && idItinerario != null) {
                FacesContext context = FacesContext.getCurrentInstance();

                // Validación de autorización: Si hay usuario en sesión, validar que sea titular o pasajero de la reserva
                if (context != null && context.getExternalContext() != null) {
                    HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
                    Usuario usuarioSesion = (session != null) ? (Usuario) session.getAttribute("usuario") : null;
                    if (usuarioSesion != null && usuarioSesion.getRut() != null) {
                        boolean esAutorizado = reservaService.esPasajeroDeReserva(usuarioSesion.getRut(), idReserva);
                        if (!esAutorizado) {
                            Logger.logWarn("Intento no autorizado de Check-In para reserva ID " + idReserva + " por usuario RUT: " + usuarioSesion.getRut());
                            this.idReserva = null;
                            this.idItinerario = null;
                            this.pasoActual = 0;
                            context.addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                            "Acceso No Autorizado",
                                            "No tienes permisos para acceder o gestionar el Check-In de esta reserva."));
                            return;
                        }
                    }
                }

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

                // 2. Detección de estado previo: si ya cuenta con Check-In completado, avanza directamente
                // Determinar RUT para la consulta de tickets y pasajeros
                String rutFiltro = "";
                if (apellidoORutBuscado != null && !apellidoORutBuscado.isBlank()) {
                    rutFiltro = apellidoORutBuscado.trim();
                } else {
                    HttpSession session = (context != null && context.getExternalContext() != null)
                            ? (HttpSession) context.getExternalContext().getSession(false) : null;
                    Usuario usuarioSesion = (session != null) ? (Usuario) session.getAttribute("usuario") : null;
                    if (usuarioSesion != null && usuarioSesion.getRut() != null) {
                        rutFiltro = usuarioSesion.getRut().trim();
                    }
                }

                if (rutFiltro.isBlank() && idReserva != null) {
                    Optional<Reserva> optRes = reservaService.findById(idReserva);
                    if (optRes.isPresent() && optRes.get().getPasajero() != null) {
                        rutFiltro = optRes.get().getPasajero().getRut();
                    }
                }

                this.listaAsientos = asientoService.getAsientosReservados(idItinerario, idReserva);
                this.listaTicketsBoardingPass = reservaService.getTicket(rutFiltro, idReserva, idItinerario);

                // 2. Detección de estado previo: si ya cuenta con Check-In completado, avanza directamente
                boolean yaCompletado = reservaService.tieneCheckInCompletado(idReserva);
                if (yaCompletado) {
                    this.checkInExitoso = true;
                    this.pasoActual = 3;
                    return;
                }
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
            if (this.checkInExitoso) {
                this.pasoActual = 3;
            } else {
                this.pasoActual = 1;
            }

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
            if ((listaTicketsBoardingPass == null || listaTicketsBoardingPass.isEmpty())
                    && (listaAsientos == null || listaAsientos.isEmpty())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Sin Asientos", "No se encontraron asientos asignados para procesar el Check-In."));
                return;
            }
            if (checkInExitoso) {
                // Si el Check-In ya fue completado previamente, salta directo a la tarjeta de embarque sin pedir declaración de nuevo
                pasoActual = 3;
                return;
            }
            pasoActual = 2;
        } else if (pasoActual == 2) {
            if (checkInExitoso) {
                pasoActual = 3;
                return;
            }
            if (!aceptoDeclaracionSeguridad) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Declaración Requerida", "Debes aceptar la declaración jurada de equipaje peligroso para continuar."));
                return;
            }
            procesarConfirmacionCheckIn();
        }
    }

    public void pasoAnterior() {
        if (pasoActual == 3 && checkInExitoso) {
            pasoActual = 1;
        } else if (pasoActual > 1 && !checkInExitoso) {
            pasoActual--;
        } else if (pasoActual == 1 && (codigoReservaBuscado != null && !codigoReservaBuscado.isBlank())) {
            pasoActual = 0;
        }
    }

    public void buscarOtraReserva() {
        this.idReserva = null;
        this.idItinerario = null;
        this.pasoActual = 0;
        this.checkInExitoso = false;
        this.codigoReservaBuscado = null;
        this.apellidoORutBuscado = null;
        this.listaAsientos.clear();
        this.listaTicketsBoardingPass.clear();
    }

    private void procesarConfirmacionCheckIn() {
        try {
            reservaService.procesarCheckInWeb(idReserva, idItinerario);
            cargarDatosCheckIn();

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

    public Date getFechaAperturaCheckIn() {
        if (fechaSalidaVuelo != null) {
            return new Date(fechaSalidaVuelo.getTime() - (48L * 3600 * 1000));
        }
        return null;
    }

    public Date getFechaCierreCheckIn() {
        if (fechaSalidaVuelo != null) {
            return new Date(fechaSalidaVuelo.getTime() - (1L * 3600 * 1000));
        }
        return null;
    }

    public List<InfoAsientoReservaDTO> getListaAsientos() { return listaAsientos; }
    public List<TicketInfo> getListaTicketsBoardingPass() { return listaTicketsBoardingPass; }

    public boolean isTieneTicketsDetallados() {
        return listaTicketsBoardingPass != null && !listaTicketsBoardingPass.isEmpty();
    }

    public TicketInfo getPrimerTicket() {
        if (listaTicketsBoardingPass != null && !listaTicketsBoardingPass.isEmpty()) {
            return listaTicketsBoardingPass.get(0);
        }
        return null;
    }

    public String getCodigoPnr() {
        TicketInfo primer = getPrimerTicket();
        if (primer != null && primer.getCodigoPnr() != null && !primer.getCodigoPnr().isBlank()) {
            return primer.getCodigoPnr();
        }
        return (codigoReservaBuscado != null && !codigoReservaBuscado.isBlank())
                ? codigoReservaBuscado.toUpperCase()
                : (idReserva != null ? "RES-" + idReserva : "---");
    }

    public String getNumeroVueloResumen() {
        TicketInfo primer = getPrimerTicket();
        return (primer != null && primer.getNumeroVuelo() != null) ? primer.getNumeroVuelo() : "---";
    }

    public String getRutaResumen() {
        TicketInfo primer = getPrimerTicket();
        return (primer != null && primer.getRutaTramo() != null) ? primer.getRutaTramo() : "---";
    }

    public String getPuertaTerminalResumen() {
        TicketInfo primer = getPrimerTicket();
        return (primer != null && primer.getPuertaTerminal() != null) ? primer.getPuertaTerminal() : "Por confirmar";
    }

    public Date getHoraLimiteEmbarqueResumen() {
        TicketInfo primer = getPrimerTicket();
        return (primer != null) ? primer.getHoraLimiteEmbarque() : null;
    }

    public boolean isPermiteCambioAsiento() {
        if (idReserva == null) return false;
        try {
            Optional<Reserva> optRes = reservaService.findById(idReserva);
            if (optRes.isEmpty()) return false;
            Reserva res = optRes.get();

            if (res.getEstadoReservaBean() != null && res.getEstadoReservaBean().getDescripcion() != null
                    && res.getEstadoReservaBean().getDescripcion().toLowerCase().contains("cancelad")) {
                return false;
            }

            if (res.getReservaItinerarios() != null && !res.getReservaItinerarios().isEmpty()) {
                for (ReservaItinerario ri : res.getReservaItinerarios()) {
                    if (idItinerario == null || (ri.getItinerario() != null && ri.getItinerario().getIdItinerario().equals(idItinerario))) {
                        if (ri.getItinerarioTarifa() != null && ri.getItinerarioTarifa().getTarifa() != null) {
                            List<TarifaCaracteristica> caracs = ri.getItinerarioTarifa().getTarifa().getTarifaCaracteristicas();
                            if (caracs == null || !tarifaValidator.permiteCambio(caracs)) {
                                return false;
                            }
                            if (this.fechaSalidaVuelo != null) {
                                long diffMillis = this.fechaSalidaVuelo.getTime() - System.currentTimeMillis();
                                int horasRestantes = (int) (diffMillis / (3600 * 1000));
                                if (!tarifaValidator.cumpleHorasMinimasCambio(caracs, horasRestantes)) {
                                    return false;
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logError("Error al verificar reglas de cambio de asiento para reserva #" + idReserva + ": " + e.getMessage());
        }
        return false;
    }

    public void irACambiarAsiento() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null && context.getExternalContext() != null && this.idReserva != null) {
                if (!isPermiteCambioAsiento()) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Cambio no permitido", "Tu tarifa contratada no autoriza modificaciones de asiento."));
                    return;
                }

                Integer vueloId = null;
                if (this.listaAsientos != null && !this.listaAsientos.isEmpty()) {
                    vueloId = this.listaAsientos.get(0).getIdVuelo();
                }

                Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
                sessionMap.put("CAMBIO_ASIENTO_ID_RESERVA", this.idReserva);
                if (vueloId != null) {
                    sessionMap.put("CAMBIO_ASIENTO_ID_VUELO", vueloId);
                }
                sessionMap.put("CAMBIO_ASIENTO_ID_ITINERARIO", this.idItinerario);
                sessionMap.put("CAMBIO_ASIENTO_ORIGEN", "CHECKIN");

                // Redirección segura a la vista pública sin parámetros en la URL
                context.getExternalContext().redirect(
                        context.getExternalContext().getRequestContextPath() + "/home/cambio-asiento.xhtml"
                );
            }
        } catch (Exception e) {
            Logger.logError("Error al redirigir a cambio de asiento: " + e.getMessage());
        }
    }

    /**
     * Genera el Data URL del código de barras 2D PDF417 (IATA BCBP) para renderizar en la vista web
     * de manera local y autónoma sin depender de APIs externas.
     */
    public String obtenerCodigoBarrasBase64(String codigo) {
        return BarcodeGeneratorUtil.generarPdf417DataUrl(codigo);
    }

    /**
     * Descarga formal de Tarjetas de Embarque en formato PDF IATA Oficial
     * utilizando el servicio de generación existente en el backend.
     */
    public StreamedContent descargarBoardingPassesPdf() {
        try {
            if (this.listaTicketsBoardingPass == null || this.listaTicketsBoardingPass.isEmpty()) {
                cargarDatosCheckIn();
            }

            byte[] pdfBytes = null;
            if (this.listaTicketsBoardingPass != null && !this.listaTicketsBoardingPass.isEmpty()) {
                pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(this.listaTicketsBoardingPass);
            }

            if ((pdfBytes == null || pdfBytes.length == 0) && this.idReserva != null) {
                Optional<Reserva> optRes = reservaService.findById(this.idReserva);
                if (optRes.isPresent()) {
                    pdfBytes = eTicketPdfGeneratorService.generarPdfBoardingPasses(optRes.get());
                }
            }

            if (pdfBytes != null && pdfBytes.length > 0) {
                final byte[] finalBytes = pdfBytes;
                String pnr = (codigoReservaBuscado != null && !codigoReservaBuscado.isBlank())
                        ? codigoReservaBuscado.trim().toUpperCase()
                        : ("RESERVA-" + this.idReserva);

                return DefaultStreamedContent.builder()
                        .name("Tarjetas_Embarque_SkyWay_" + pnr + ".pdf")
                        .contentType("application/pdf")
                        .stream(() -> new ByteArrayInputStream(finalBytes))
                        .build();
            }
        } catch (Exception e) {
            Logger.logError("Error al generar PDF de Boarding Pass: " + e.getMessage());
        }
        return null;
    }
}