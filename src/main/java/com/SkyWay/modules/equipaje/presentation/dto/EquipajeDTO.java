package com.SkyWay.modules.equipaje.presentation.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record EquipajeDTO(
        Long idEquipaje,

        @NotNull(message = "El peso es obligatorio")
        @DecimalMin(value = "0.10", message = "El peso mínimo debe ser de 0.10 kg")
        BigDecimal peso,

        @NotBlank(message = "Las dimensiones son obligatorias (ej: 55x40x20 cm)")
        String dimensiones,

        @NotNull(message = "El ID de la reserva es obligatorio")
        Long idReserva,

        @NotBlank(message = "El RUT del pasajero es obligatorio")
        String rutPasajero,

        @NotNull(message = "El ID del tipo de equipaje es obligatorio")
        Long idTipoEquipaje,

        String nombreTipoEquipaje
) {}