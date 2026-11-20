package com.SkyWay.modules.turno.domain.service;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculador compartido de intervalos entre servicios, descanso mínimo reglamentario (8h),
 * margen mínimo de turnaround (60 min) y carga acumulada en rotaciones continuas (≤12h estándar).
 * Basado exclusivamente en las marcas de tiempo reales de Vuelo y Turno y en las reglas
 * operacionales vigentes de {@code TurnoTripulacionService} y {@code VueloServiceImpl}.
 */
public final class FatigaDescansoHelper {

    public static final long MIN_TURNAROUND_MIN = 60L;       // VueloServiceImpl.BUFFER_TURNAROUND_MINUTOS
    public static final long MIN_DESCANSO_LEGAL_MIN = 480L;  // TurnoTripulacionService (8 horas)
    public static final long LIMITE_JORNADA_MIN = 720L;      // Jornada estándar ≤ 12 horas

    private FatigaDescansoHelper() {
    }

    public record BloqueServicio(
            Integer id,
            String codigo,
            Timestamp inicio,
            Timestamp fin
    ) {
    }

    public static final class IntervaloServicioInfo implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Long minutosDesdeAnterior;
        private final String tiempoDesdeAnteriorTexto;
        private final String codigoPrevio;
        private final String estadoCodigo;
        private final String severidad;
        private final String etiqueta;
        private final String explicacionCorta;
        private final long minutosTrabajoAcumulado;
        private final String trabajoAcumuladoTexto;
        private final long minutosAmplitudJornada;
        private final String amplitudJornadaTexto;
        private final boolean sobreEsfuerzo;

        public IntervaloServicioInfo(
                Long minutosDesdeAnterior, String tiempoDesdeAnteriorTexto, String codigoPrevio,
                String estadoCodigo, String severidad, String etiqueta, String explicacionCorta,
                long minutosTrabajoAcumulado, String trabajoAcumuladoTexto,
                long minutosAmplitudJornada, String amplitudJornadaTexto, boolean sobreEsfuerzo
        ) {
            this.minutosDesdeAnterior = minutosDesdeAnterior;
            this.tiempoDesdeAnteriorTexto = tiempoDesdeAnteriorTexto;
            this.codigoPrevio = codigoPrevio;
            this.estadoCodigo = estadoCodigo;
            this.severidad = severidad;
            this.etiqueta = etiqueta;
            this.explicacionCorta = explicacionCorta;
            this.minutosTrabajoAcumulado = minutosTrabajoAcumulado;
            this.trabajoAcumuladoTexto = trabajoAcumuladoTexto;
            this.minutosAmplitudJornada = minutosAmplitudJornada;
            this.amplitudJornadaTexto = amplitudJornadaTexto;
            this.sobreEsfuerzo = sobreEsfuerzo;
        }

        public Long getMinutosDesdeAnterior() { return minutosDesdeAnterior; }
        public String getTiempoDesdeAnteriorTexto() { return tiempoDesdeAnteriorTexto; }
        public String getCodigoPrevio() { return codigoPrevio; }
        public String getEstadoCodigo() { return estadoCodigo; }
        public String getSeveridad() { return severidad; }
        public String getEtiqueta() { return etiqueta; }
        public String getExplicacionCorta() { return explicacionCorta; }
        public long getMinutosTrabajoAcumulado() { return minutosTrabajoAcumulado; }
        public String getTrabajoAcumuladoTexto() { return trabajoAcumuladoTexto; }
        public long getMinutosAmplitudJornada() { return minutosAmplitudJornada; }
        public String getAmplitudJornadaTexto() { return amplitudJornadaTexto; }
        public boolean isSobreEsfuerzo() { return sobreEsfuerzo; }
    }

    public static final class ResumenFatigaDTO implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String estadoCodigo;
        private final String severidad;
        private final String tituloEstado;
        private final String descansoMinimoTexto;
        private final String maxRotacionTexto;
        private final int enlacesMenores8h;
        private final int alertasCriticas;
        private final String detalleTexto;

        public ResumenFatigaDTO(
                String estadoCodigo, String severidad, String tituloEstado,
                String descansoMinimoTexto, String maxRotacionTexto,
                int enlacesMenores8h, int alertasCriticas, String detalleTexto
        ) {
            this.estadoCodigo = estadoCodigo;
            this.severidad = severidad;
            this.tituloEstado = tituloEstado;
            this.descansoMinimoTexto = descansoMinimoTexto;
            this.maxRotacionTexto = maxRotacionTexto;
            this.enlacesMenores8h = enlacesMenores8h;
            this.alertasCriticas = alertasCriticas;
            this.detalleTexto = detalleTexto;
        }

        public String getEstadoCodigo() { return estadoCodigo; }
        public String getSeveridad() { return severidad; }
        public String getTituloEstado() { return tituloEstado; }
        public String getDescansoMinimoTexto() { return descansoMinimoTexto; }
        public String getMaxRotacionTexto() { return maxRotacionTexto; }
        public int getEnlacesMenores8h() { return enlacesMenores8h; }
        public int getAlertasCriticas() { return alertasCriticas; }
        public String getDetalleTexto() { return detalleTexto; }
    }

    public static final class ResultadoAnalisisFatiga implements Serializable {
        private static final long serialVersionUID = 1L;
        private final Map<Integer, IntervaloServicioInfo> porId;
        private final ResumenFatigaDTO resumen;

        public ResultadoAnalisisFatiga(Map<Integer, IntervaloServicioInfo> porId, ResumenFatigaDTO resumen) {
            this.porId = porId;
            this.resumen = resumen;
        }

        public Map<Integer, IntervaloServicioInfo> getPorId() { return porId; }
        public ResumenFatigaDTO getResumen() { return resumen; }
    }

    public static IntervaloServicioInfo intervaloSinReferencia(long duracionMin) {
        String dur = formatearDuracion(duracionMin);
        boolean excede12h = duracionMin > LIMITE_JORNADA_MIN;
        return new IntervaloServicioInfo(
                null, "—", "",
                excede12h ? "JORNADA_EXTENDIDA" : "SIN_DATO",
                excede12h ? "warning" : "secondary",
                excede12h ? "Jornada > 12h" : "Sin ciclo previo",
                excede12h ? "Supera 12h continuas en un solo servicio" : "Sin servicio previo para comparar descanso",
                duracionMin, dur,
                duracionMin, dur, excede12h
        );
    }

    public static ResultadoAnalisisFatiga analizar(List<BloqueServicio> activos) {
        Map<Integer, IntervaloServicioInfo> porId = new HashMap<>();
        if (activos == null || activos.isEmpty()) {
            return new ResultadoAnalisisFatiga(porId, new ResumenFatigaDTO(
                    "CONFORME", "success", "Sin carga activa",
                    "N/D", "0h 00m", 0, 0, "Sin servicios activos registrados."
            ));
        }

        List<BloqueServicio> ordenados = activos.stream()
                .filter(b -> b.id() != null && b.inicio() != null && b.fin() != null)
                .sorted(Comparator.comparing(BloqueServicio::inicio).thenComparing(BloqueServicio::id))
                .toList();

        if (ordenados.isEmpty()) {
            return new ResultadoAnalisisFatiga(porId, new ResumenFatigaDTO(
                    "CONFORME", "success", "Sin horarios definidos",
                    "N/D", "0h 00m", 0, 0, "Sin marcas horarias completas."
            ));
        }

        long minDescansoMin = Long.MAX_VALUE;
        long maxTrabajoMin = 0L;
        long maxAmplitudMin = 0L;
        int enlacesMenores8h = 0;
        int alertasCriticas = 0;

        BloqueServicio prev = null;
        Timestamp inicioRotacion = null;
        long trabajoRotacionMin = 0L;

        for (BloqueServicio curr : ordenados) {
            long durMin = Math.max(0L, (curr.fin().getTime() - curr.inicio().getTime()) / 60_000L);

            if (prev == null) {
                inicioRotacion = curr.inicio();
                trabajoRotacionMin = durMin;
                long amplitudMin = Math.max(0L, (curr.fin().getTime() - inicioRotacion.getTime()) / 60_000L);
                maxTrabajoMin = Math.max(maxTrabajoMin, trabajoRotacionMin);
                maxAmplitudMin = Math.max(maxAmplitudMin, amplitudMin);

                boolean excede = durMin > LIMITE_JORNADA_MIN;
                if (excede) {
                    alertasCriticas++;
                }

                porId.put(curr.id(), new IntervaloServicioInfo(
                        null,
                        "Inicio de jornada",
                        "",
                        excede ? "JORNADA_EXTENDIDA" : "PRIMER_SERVICIO",
                        excede ? "warning" : "secondary",
                        excede ? "Jornada > 12h" : "Primer servicio",
                        excede ? "Supera las 12h máximas de jornada" : "Inicia conteo de jornada (límite 12h)",
                        trabajoRotacionMin, formatearDuracion(trabajoRotacionMin),
                        amplitudMin, formatearDuracion(amplitudMin),
                        excede
                ));
                prev = curr;
                continue;
            }

            long gapMin = (curr.inicio().getTime() - prev.fin().getTime()) / 60_000L;
            minDescansoMin = Math.min(minDescansoMin, gapMin);

            String codigo;
            String severidad;
            String etiqueta;
            String explicacion;
            String tiempoTxt;
            boolean sobreEsfuerzo = false;

            if (gapMin < 0) {
                // Solapamiento directo
                codigo = "SOLAPAMIENTO";
                severidad = "danger";
                etiqueta = "Horario solapado";
                explicacion = "Se superpone con el servicio anterior";
                tiempoTxt = "-" + formatearDuracion(Math.abs(gapMin));
                trabajoRotacionMin += durMin;
                sobreEsfuerzo = true;
                alertasCriticas++;
            } else if (gapMin < MIN_TURNAROUND_MIN) {
                codigo = "MARGEN_CRITICO";
                severidad = "danger";
                etiqueta = "Margen < 1h";
                explicacion = "Menos de 1h libre para operar el enlace";
                tiempoTxt = formatearDuracion(gapMin);
                trabajoRotacionMin += durMin;
                sobreEsfuerzo = true;
                alertasCriticas++;
            } else if (gapMin < MIN_DESCANSO_LEGAL_MIN) {
                codigo = "ROTACION_CONTINUA";
                severidad = "warning";
                etiqueta = "Sin descanso 8h";
                explicacion = "Misma jornada (< 8h libres): acumula horas";
                tiempoTxt = formatearDuracion(gapMin);
                trabajoRotacionMin += durMin;
                enlacesMenores8h++;
            } else {
                // Descanso >= 8h reinicia rotación continua
                codigo = "DESCANSO_OK";
                severidad = "success";
                etiqueta = "Descanso ≥ 8h";
                explicacion = "Descanso cumplido: reinicia jornada";
                tiempoTxt = formatearDuracion(gapMin);
                inicioRotacion = curr.inicio();
                trabajoRotacionMin = durMin;
            }

            long amplitudMin = Math.max(0L, (curr.fin().getTime() - inicioRotacion.getTime()) / 60_000L);
            if (trabajoRotacionMin > LIMITE_JORNADA_MIN || amplitudMin > LIMITE_JORNADA_MIN) {
                sobreEsfuerzo = true;
                if (!"SOLAPAMIENTO".equals(codigo) && !"MARGEN_CRITICO".equals(codigo)) {
                    severidad = amplitudMin > 1080L ? "danger" : "warning";
                    etiqueta = "Jornada > 12h";
                    explicacion = "Excede 12h continuas sin descanso ≥ 8h";
                    alertasCriticas++;
                }
            }

            maxTrabajoMin = Math.max(maxTrabajoMin, trabajoRotacionMin);
            maxAmplitudMin = Math.max(maxAmplitudMin, amplitudMin);

            porId.put(curr.id(), new IntervaloServicioInfo(
                    gapMin,
                    tiempoTxt,
                    prev.codigo(),
                    codigo,
                    severidad,
                    etiqueta,
                    explicacion,
                    trabajoRotacionMin, formatearDuracion(trabajoRotacionMin),
                    amplitudMin, formatearDuracion(amplitudMin),
                    sobreEsfuerzo
            ));
            prev = curr;
        }

        String descansoMinTxt = minDescansoMin == Long.MAX_VALUE
                ? "Ciclo único"
                : (minDescansoMin < 0 ? "-" + formatearDuracion(Math.abs(minDescansoMin)) : formatearDuracion(minDescansoMin));
        String maxRotTxt = formatearDuracion(maxTrabajoMin) + " servicio (" + formatearDuracion(maxAmplitudMin) + " jornada)";

        String estadoCod;
        String sev;
        String titulo;
        if (alertasCriticas > 0 || (minDescansoMin < MIN_TURNAROUND_MIN && minDescansoMin != Long.MAX_VALUE)) {
            estadoCod = "ALERTA_CRITICA";
            sev = "danger";
            titulo = "Riesgo de Sobre-esfuerzo";
        } else if (enlacesMenores8h > 0 || maxAmplitudMin > LIMITE_JORNADA_MIN) {
            estadoCod = "PRECAUCION";
            sev = "warning";
            titulo = "Misma jornada (< 8h desc.)";
        } else {
            estadoCod = "CONFORME";
            sev = "success";
            titulo = "Descanso Reglamentario OK";
        }

        String detalle;
        if (ordenados.size() < 2) {
            detalle = "Solo 1 servicio activo (" + formatearDuracion(maxTrabajoMin) + ")";
        } else if (alertasCriticas > 0) {
            detalle = alertasCriticas + " alerta(s) crítica(s) · Mín. entre servicios: " + descansoMinTxt;
        } else if (enlacesMenores8h > 0) {
            detalle = enlacesMenores8h + " enlace(s) sin descanso de 8h (mín. libre: " + descansoMinTxt + ") · Jornada máx.: " + formatearDuracion(maxAmplitudMin) + " / 12h";
        } else {
            detalle = "Mín. entre servicios: " + descansoMinTxt + " (≥ 8h legal)";
        }

        return new ResultadoAnalisisFatiga(porId, new ResumenFatigaDTO(
                estadoCod, sev, titulo, descansoMinTxt, maxRotTxt, enlacesMenores8h, alertasCriticas, detalle
        ));
    }

    public static String formatearDuracion(long minutos) {
        long mAbs = Math.max(0L, minutos);
        long h = mAbs / 60L;
        long m = mAbs % 60L;
        return h + "h " + (m < 10 ? "0" : "") + m + "m";
    }
}
