package com.SkyWay.modules.reembolso.domain.service;

import com.SkyWay.modules.reembolso.domain.model.CuentaReembolso;
import com.SkyWay.modules.reembolso.presentation.dto.CuentaBancariaSolicitudDTO;
import com.SkyWay.modules.reembolso.presentation.dto.PayoutResponseDTO;
import com.SkyWay.modules.reserva.presentation.dto.ReembolsoPasajeroDTO;

import java.util.List;

public interface ReembolsoService {

    List<CuentaReembolso> listarCuentasPorPasajero(String rutPasajero);

    List<ReembolsoPasajeroDTO> listarReembolsosPorPasajero(String rutPasajero);

    List<ReembolsoPasajeroDTO> listarTodosReembolsosAdmin();

    void registrarDatosBancariosReembolso(Integer idReembolso, String rutPasajero, CuentaBancariaSolicitudDTO datosCuenta);

    void confirmarDatosBancariosAdmin(Integer idReembolso, String rutAdmin, String observacion);

    void rechazarDatosBancariosAdmin(Integer idReembolso, String rutAdmin, String motivoRechazo);

    PayoutResponseDTO ejecutarReembolsoSimuladoAdmin(Integer idReembolso, String rutAdmin, String observacion, String escenarioSandbox);
}
