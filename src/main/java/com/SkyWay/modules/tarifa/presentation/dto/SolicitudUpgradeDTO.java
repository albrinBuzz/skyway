package com.SkyWay.modules.tarifa.presentation.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO que encapsula los datos de una solicitud de ascenso de tarifa (upgrade)
 * en tránsito antes y después de ser procesada por la pasarela de pago.
 */
public class SolicitudUpgradeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer idReserva;
    private Integer idItinerario;
    private Integer idItinerarioTarifaNuevo;
    private BigDecimal montoDiferencia;
    private String nombreTarifa;
    private String origen; // "perfil" o "checkin"
    private Integer idMetodoPago; // 1: Webpay, 2: PayPal, etc.

    public SolicitudUpgradeDTO() {}

    public SolicitudUpgradeDTO(Integer idReserva, Integer idItinerario, Integer idItinerarioTarifaNuevo,
                               BigDecimal montoDiferencia, String nombreTarifa, String origen, Integer idMetodoPago) {
        this.idReserva = idReserva;
        this.idItinerario = idItinerario;
        this.idItinerarioTarifaNuevo = idItinerarioTarifaNuevo;
        this.montoDiferencia = montoDiferencia;
        this.nombreTarifa = nombreTarifa;
        this.origen = origen;
        this.idMetodoPago = idMetodoPago;
    }

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    public Integer getIdItinerario() { return idItinerario; }
    public void setIdItinerario(Integer idItinerario) { this.idItinerario = idItinerario; }

    public Integer getIdItinerarioTarifaNuevo() { return idItinerarioTarifaNuevo; }
    public void setIdItinerarioTarifaNuevo(Integer idItinerarioTarifaNuevo) { this.idItinerarioTarifaNuevo = idItinerarioTarifaNuevo; }

    public BigDecimal getMontoDiferencia() { return montoDiferencia; }
    public void setMontoDiferencia(BigDecimal montoDiferencia) { this.montoDiferencia = montoDiferencia; }

    public String getNombreTarifa() { return nombreTarifa; }
    public void setNombreTarifa(String nombreTarifa) { this.nombreTarifa = nombreTarifa; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public Integer getIdMetodoPago() { return idMetodoPago; }
    public void setIdMetodoPago(Integer idMetodoPago) { this.idMetodoPago = idMetodoPago; }
}
