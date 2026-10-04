package com.SkyWay.modules.reservaasiento.domain.service;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.EventoAsientoPush;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsientoCacheService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AsientoService asientoService;

    // Tiempo máximo de retención temporal antes de liberar el asiento (10 minutos)
    private static final long TIEMPO_EXPIRACION_MS = 10 * 60 * 1000L;

    //private static final long TIEMPO_EXPIRACION_MS = 25000;


    // Map<idVuelo, Map<idAsiento, InfoAsientoDTO>>
    private final Map<Integer, Map<Integer, InfoAsientoDTO>> mapaVuelos = new ConcurrentHashMap<>();

    // Map<idAsiento, TimestampSeleccionMs>
    private final Map<Integer, Long> tiempoSeleccionMap = new ConcurrentHashMap<>();

    public List<InfoAsientoDTO> getAsientosVuelo(Integer idVuelo) {
        if (idVuelo == null) return Collections.emptyList();

        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.computeIfAbsent(idVuelo, k -> {
            List<InfoAsientoDTO> dbAsientos = asientoService.getAsientosDisponibles(idVuelo);
            Map<Integer, InfoAsientoDTO> map = new ConcurrentHashMap<>();
            for (InfoAsientoDTO a : dbAsientos) {
                map.put(a.getIdAsiento(), a);
            }
            return map;
        });

        // ⚠️ CLONAR / DUPLICAR CADA DTO PARA QUE CADA PESTAÑA TENGA SUS PROPIAS INSTANCIAS EN MEMORIA
        List<InfoAsientoDTO> copias = new ArrayList<>();
        for (InfoAsientoDTO original : asientosMap.values()) {
            copias.add(clonarInfoAsiento(original));
        }
        return copias;
    }


    private InfoAsientoDTO clonarInfoAsiento(InfoAsientoDTO a) {
        InfoAsientoDTO copia = new InfoAsientoDTO();
        copia.setIdAsiento(a.getIdAsiento());
        copia.setNumeroAsiento(a.getNumeroAsiento());
        copia.setClase(a.getClase());
        copia.setPrecio(a.getPrecio());
        copia.setEstado(a.getEstado());
        copia.setReservadoPor(a.getReservadoPor());
        return copia;
    }

    public ResultadoSeleccion seleccionarOliberarAsiento(Integer idVuelo, InfoAsientoDTO asientoSolicitado, String sessionId) {
        if (asientoSolicitado == null || idVuelo == null || sessionId == null) {

            return ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
        }



        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null) {

            getAsientosVuelo(idVuelo);
            asientosMap = mapaVuelos.get(idVuelo);
            if (asientosMap == null) {

                return ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
            }
        }

        final ResultadoSeleccion[] resultado = { ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE };

        asientosMap.computeIfPresent(asientoSolicitado.getIdAsiento(), (id, asientoActual) -> {
            if ("OCUPADO".equalsIgnoreCase(asientoActual.getEstado())) {
                Logger.logWarn(String.format("[CacheService] Asiento %d rechazado: Estado OCUPADO en base de datos/cache.", id));
                resultado[0] = ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
                return asientoActual;
            }

            boolean estaLibre = "libre".equalsIgnoreCase(asientoActual.getEstado());
            boolean esMio = "SELECCIONADO".equalsIgnoreCase(asientoActual.getEstado())
                    && sessionId.equals(asientoActual.getReservadoPor());

            if (estaLibre) {
                asientoActual.setEstado("SELECCIONADO");
                asientoActual.setReservadoPor(sessionId);
                tiempoSeleccionMap.put(id, System.currentTimeMillis());
                resultado[0] = ResultadoSeleccion.EXITO_SELECCIONADO;
                Logger.logInfo(String.format("[CacheService] Asiento %d asignado con éxito a SessionId: %s", id, sessionId));

            } else if (esMio) {
                asientoActual.setEstado("libre");
                asientoActual.setReservadoPor(null);
                tiempoSeleccionMap.remove(id);
                resultado[0] = ResultadoSeleccion.EXITO_LIBERADO;
                Logger.logInfo(String.format("[CacheService] Asiento %d liberado por su dueño (SessionId: %s)", id, sessionId));

            } else {
                resultado[0] = ResultadoSeleccion.FALLO_BLOQUEADO_POR_OTRO;
                Logger.logWarn(String.format("[CacheService] Conflicto en Asiento %d: Retenido por SessionId %s, solicitado por %s",
                        id, asientoActual.getReservadoPor(), sessionId));
            }
            return asientoActual;
        });

        if (resultado[0] == ResultadoSeleccion.EXITO_SELECCIONADO || resultado[0] == ResultadoSeleccion.EXITO_LIBERADO) {
            InfoAsientoDTO actualizado = asientosMap.get(asientoSolicitado.getIdAsiento());
            if (actualizado != null) {
                EventoAsientoPush evento = new EventoAsientoPush(
                        actualizado.getIdAsiento(), idVuelo, actualizado.getEstado(), sessionId);



                messagingTemplate.convertAndSend("/topic/vuelo/" + idVuelo, evento);
            }
        }

        return resultado[0];
    }
    public boolean validarPertenenciaYSeleccion(Integer idVuelo, Integer idAsiento, String sessionId) {
        if (idVuelo == null || idAsiento == null || sessionId == null) return false;
        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null) return false;

        InfoAsientoDTO asiento = asientosMap.get(idAsiento);
        if (asiento == null) return false;

        return "SELECCIONADO".equalsIgnoreCase(asiento.getEstado())
                && sessionId.equals(asiento.getReservadoPor());
    }

    /**
     * Proceso en segundo plano que limpia asientos expirados cada 3 segundos (Optimizado para menor consumo de recursos).
     */
    @Scheduled(fixedRate = 3000)
    public void liberarAsientosExpirados() {
        if (tiempoSeleccionMap.isEmpty()) return;

        long ahora = System.currentTimeMillis();

        tiempoSeleccionMap.forEach((idAsiento, timestamp) -> {
            if (ahora - timestamp > TIEMPO_EXPIRACION_MS) {
                liberarAsientoEspecificoPorTimeout(idAsiento);
            }
        });
    }

    private void liberarAsientoEspecificoPorTimeout(Integer idAsiento) {
        for (Map.Entry<Integer, Map<Integer, InfoAsientoDTO>> entryVuelo : mapaVuelos.entrySet()) {
            Integer idVuelo = entryVuelo.getKey();
            Map<Integer, InfoAsientoDTO> asientosMap = entryVuelo.getValue();

            if (asientosMap.containsKey(idAsiento)) {
                asientosMap.computeIfPresent(idAsiento, (id, asiento) -> {
                    if ("SELECCIONADO".equalsIgnoreCase(asiento.getEstado())) {
                        String sessionOriginal = asiento.getReservadoPor();
                        asiento.setEstado("libre");
                        asiento.setReservadoPor(null);
                        tiempoSeleccionMap.remove(idAsiento);

                        Logger.logInfo(String.format("⏰ [TTL-EXPIRADO] Asiento ID %d liberado automáticamente.", idAsiento));

                        EventoAsientoPush evento = new EventoAsientoPush(idAsiento, idVuelo, "libre", sessionOriginal);
                        messagingTemplate.convertAndSend("/topic/vuelo/" + idVuelo, evento);
                    }
                    return asiento;
                });
                break;
            }
        }
    }

    /**
     * Retorna el tiempo restante del PRIMER asiento seleccionado por el usuario.
     * Mantiene un temporizador constante para toda la sesión de compra.
     */
    public long getTiempoRestanteMsParaSesion(String sessionId) {
        if (sessionId == null) return -1;

        long ahora = System.currentTimeMillis();
        long menorTiempoRestante = Long.MAX_VALUE;
        boolean tieneAsientos = false;

        for (Map<Integer, InfoAsientoDTO> asientosMap : mapaVuelos.values()) {
            for (Map.Entry<Integer, InfoAsientoDTO> entry : asientosMap.entrySet()) {
                InfoAsientoDTO asiento = entry.getValue();
                if ("SELECCIONADO".equalsIgnoreCase(asiento.getEstado()) && sessionId.equals(asiento.getReservadoPor())) {
                    Long timestamp = tiempoSeleccionMap.get(entry.getKey());
                    if (timestamp != null) {
                        long restante = TIEMPO_EXPIRACION_MS - (ahora - timestamp);
                        if (restante < menorTiempoRestante) {
                            menorTiempoRestante = restante;
                            tieneAsientos = true;
                        }
                    }
                }
            }
        }
        return tieneAsientos ? Math.max(0, menorTiempoRestante) : -1;
    }

    public void confirmarReservaDefinitiva(Integer idVuelo, List<Integer> idsAsientos) {
        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null || idsAsientos == null) return;

        for (Integer idAsiento : idsAsientos) {
            asientosMap.computeIfPresent(idAsiento, (id, asiento) -> {
                asiento.setEstado("OCUPADO");
                asiento.setReservadoPor(null);
                tiempoSeleccionMap.remove(id);

                EventoAsientoPush evento = new EventoAsientoPush(id, idVuelo, "OCUPADO", null);
                messagingTemplate.convertAndSend("/topic/vuelo/" + idVuelo, evento);
                return asiento;
            });
        }
    }

    public enum ResultadoSeleccion {
        EXITO_SELECCIONADO, EXITO_LIBERADO,
        FALLO_ASIENTO_NO_DISPONIBLE, FALLO_BLOQUEADO_POR_OTRO
    }
}