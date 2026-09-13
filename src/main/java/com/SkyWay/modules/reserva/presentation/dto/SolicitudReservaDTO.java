package com.SkyWay.modules.reserva.presentation.dto;



import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.views.reserva.EquipajePasajeroDTO;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record SolicitudReservaDTO(
        String rutUsuarioTitular,
        List<Pasajero> pasajeros,
        Map<Integer, List<InfoAsientoDTO>> asientosSeleccionados,
        Map<Integer, Integer> tarifasItinerarios,
        List<EquipajePasajeroDTO> equipajePorPasajero,
        BigDecimal montoTotal,
        String sessionId
) implements Serializable {}