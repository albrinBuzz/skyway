package com.SkyWay.modules.checkin.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DTO que encapsula el resultado integral de la validación criptográfica y operacional
 * de una Tarjeta de Embarque escaneada en puerta o mostrador.
 */
public class ResultadoValidacionEmbarqueDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean valido;
    private boolean firmaAutentica;
    private boolean yaEmbarcado;
    private String estado;        // AUTORIZADO, RECHAZADO, ADULTERADO, DUPLICADO, CHECKIN_PENDIENTE
    private String nivelAlerta;   // SUCCESS, DANGER, WARNING, INFO
    private String mensaje;
    private String codigoRaw;

    // Datos del Pasajero
    private String nombrePasajero;
    private String rut;

    // Datos del Vuelo y Ruta
    private String pnr;
    private String numeroVuelo;
    private String origenIata;
    private String destinoIata;
    private String origenNombre;
    private String destinoNombre;
    private String rutaTramo;

    // Datos de Cabina y Ubicación
    private String asiento;
    private String claseCabina;
    private String puerta;
    private String terminal;

    // Horarios
    private Date horaSalida;
    private Date horaLlegada;
    private Date horaEmbarque;
    private Date fechaEscaneo;

    // Metadatos de Seguridad
    private String firmaHex;
    private String nonce;
    private String algoritmoSeguridad = "ECDSA NIST P-256 (Criptografía Asimétrica Inalterable)";

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public ResultadoValidacionEmbarqueDTO() {
        this.fechaEscaneo = new Date();
    }

    // Factory Helpers
    public static ResultadoValidacionEmbarqueDTO rechazado(String estado, String mensaje, String codigoRaw) {
        ResultadoValidacionEmbarqueDTO dto = new ResultadoValidacionEmbarqueDTO();
        dto.setValido(false);
        dto.setEstado(estado);
        dto.setNivelAlerta("DANGER");
        dto.setMensaje(mensaje);
        dto.setCodigoRaw(codigoRaw);
        return dto;
    }

    public static ResultadoValidacionEmbarqueDTO adulterado(String mensaje, String codigoRaw, String firma) {
        ResultadoValidacionEmbarqueDTO dto = new ResultadoValidacionEmbarqueDTO();
        dto.setValido(false);
        dto.setFirmaAutentica(false);
        dto.setEstado("ADULTERADO");
        dto.setNivelAlerta("DANGER");
        dto.setMensaje(mensaje);
        dto.setCodigoRaw(codigoRaw);
        dto.setFirmaHex(firma);
        return dto;
    }

    public static ResultadoValidacionEmbarqueDTO duplicado(String mensaje, ResultadoValidacionEmbarqueDTO base) {
        base.setValido(false);
        base.setYaEmbarcado(true);
        base.setEstado("DUPLICADO");
        base.setNivelAlerta("WARNING");
        base.setMensaje(mensaje);
        return base;
    }

    // Métodos para vistas JSF
    public boolean isAutorizado() {
        return "AUTORIZADO".equalsIgnoreCase(this.estado);
    }

    public boolean isAdulterado() {
        return "ADULTERADO".equalsIgnoreCase(this.estado);
    }

    public boolean isDuplicado() {
        return "DUPLICADO".equalsIgnoreCase(this.estado);
    }

    public boolean isCheckInPendiente() {
        return "CHECKIN_PENDIENTE".equalsIgnoreCase(this.estado);
    }

    public String getHoraSalidaFormatted() {
        if (horaSalida == null) return "---";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(horaSalida);
    }

    public String getHoraEmbarqueFormatted() {
        if (horaEmbarque == null) return "---";
        return new SimpleDateFormat("HH:mm:ss").format(horaEmbarque);
    }

    public String getFechaEscaneoFormatted() {
        if (fechaEscaneo == null) return "---";
        return new SimpleDateFormat("HH:mm:ss").format(fechaEscaneo);
    }

    public String getPuertaTerminalCompleta() {
        return (puerta != null ? puerta : "TBD") + " / " + (terminal != null ? terminal : "Terminal Internacional");
    }

    // Getters y Setters
    public boolean isValido() { return valido; }
    public void setValido(boolean valido) { this.valido = valido; }

    public boolean isFirmaAutentica() { return firmaAutentica; }
    public void setFirmaAutentica(boolean firmaAutentica) { this.firmaAutentica = firmaAutentica; }

    public boolean isYaEmbarcado() { return yaEmbarcado; }
    public void setYaEmbarcado(boolean yaEmbarcado) { this.yaEmbarcado = yaEmbarcado; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(String nivelAlerta) { this.nivelAlerta = nivelAlerta; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public String getCodigoRaw() { return codigoRaw; }
    public void setCodigoRaw(String codigoRaw) { this.codigoRaw = codigoRaw; }

    public String getNombrePasajero() { return nombrePasajero; }
    public void setNombrePasajero(String nombrePasajero) { this.nombrePasajero = nombrePasajero; }

    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }

    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }

    public String getNumeroVuelo() { return numeroVuelo; }
    public void setNumeroVuelo(String numeroVuelo) { this.numeroVuelo = numeroVuelo; }

    public String getOrigenIata() { return origenIata; }
    public void setOrigenIata(String origenIata) { this.origenIata = origenIata; }

    public String getDestinoIata() { return destinoIata; }
    public void setDestinoIata(String destinoIata) { this.destinoIata = destinoIata; }

    public String getOrigenNombre() { return origenNombre; }
    public void setOrigenNombre(String origenNombre) { this.origenNombre = origenNombre; }

    public String getDestinoNombre() { return destinoNombre; }
    public void setDestinoNombre(String destinoNombre) { this.destinoNombre = destinoNombre; }

    public String getRutaTramo() { return rutaTramo; }
    public void setRutaTramo(String rutaTramo) { this.rutaTramo = rutaTramo; }

    public String getAsiento() { return asiento; }
    public void setAsiento(String asiento) { this.asiento = asiento; }

    public String getClaseCabina() { return claseCabina; }
    public void setClaseCabina(String claseCabina) { this.claseCabina = claseCabina; }

    public String getPuerta() { return puerta; }
    public void setPuerta(String puerta) { this.puerta = puerta; }

    public String getTerminal() { return terminal; }
    public void setTerminal(String terminal) { this.terminal = terminal; }

    public Date getHoraSalida() { return horaSalida; }
    public void setHoraSalida(Date horaSalida) { this.horaSalida = horaSalida; }

    public Date getHoraLlegada() { return horaLlegada; }
    public void setHoraLlegada(Date horaLlegada) { this.horaLlegada = horaLlegada; }

    public Date getHoraEmbarque() { return horaEmbarque; }
    public void setHoraEmbarque(Date horaEmbarque) { this.horaEmbarque = horaEmbarque; }

    public Date getFechaEscaneo() { return fechaEscaneo; }
    public void setFechaEscaneo(Date fechaEscaneo) { this.fechaEscaneo = fechaEscaneo; }

    public String getFirmaHex() { return firmaHex; }
    public void setFirmaHex(String firmaHex) { this.firmaHex = firmaHex; }

    public String getAlgoritmoSeguridad() { return algoritmoSeguridad; }
    public void setAlgoritmoSeguridad(String algoritmoSeguridad) { this.algoritmoSeguridad = algoritmoSeguridad; }
}
