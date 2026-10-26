package com.SkyWay.modules.checkin.util;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class ValidadorCheckIn {

    private static final long HORAS_APERTURA_CHECKIN = 48; // Abre 48 horas antes
    private static final long MINUTOS_CIERRE_CHECKIN = 60; // Cierra 1 hora antes

    public static String validarEstadoVentanaCheckIn(Date fechaHoraSalidaVuelo) {
        if (fechaHoraSalidaVuelo == null) {
            // Fail-CLOSED: si no se puede determinar la fecha de salida no se habilita el check-in.
            return "CERRADO";
        }

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Santiago"));
        LocalDateTime salidaVuelo = fechaHoraSalidaVuelo.toInstant()
                .atZone(ZoneId.of("America/Santiago"))
                .toLocalDateTime();

        Duration duracionHastaSalida = Duration.between(ahora, salidaVuelo);
        long horasRestantes = duracionHastaSalida.toHours();
        long minutosRestantes = duracionHastaSalida.toMinutes();

        if (minutosRestantes < MINUTOS_CIERRE_CHECKIN) {
            return "CERRADO";
        }

        if (horasRestantes > HORAS_APERTURA_CHECKIN) {
            return "NO_DISPONIBLE_AUN";
        }

        return "HABILITADO";
    }
}