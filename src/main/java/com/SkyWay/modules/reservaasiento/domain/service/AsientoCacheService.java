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
    //private static final long TIEMPO_EXPIRACION_MS = 10 * 60 * 200;

    //private static final long TIEMPO_EXPIRACION_MS = 1 * 60 * 1000L;
    private static final long TIEMPO_EXPIRACION_MS =  10000;

    // Map<idVuelo, Map<idAsiento, InfoAsientoDTO>>
    private final Map<Integer, Map<Integer, InfoAsientoDTO>> mapaVuelos = new ConcurrentHashMap<>();


    // Map<idAsiento, TimestampSeleccionMs>
    private final Map<Integer, Long> tiempoSeleccionMap = new ConcurrentHashMap<>();

    public List<InfoAsientoDTO> getAsientosVuelo(Integer idVuelo) {
        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.computeIfAbsent(idVuelo, k -> {
            List<InfoAsientoDTO> dbAsientos = asientoService.getAsientosDisponibles(idVuelo);
            Map<Integer, InfoAsientoDTO> map = new ConcurrentHashMap<>();
            for (InfoAsientoDTO a : dbAsientos) map.put(a.getIdAsiento(), a);
            return map;
        });
        return new ArrayList<>(asientosMap.values());
    }

    /**
     * Alterna la selección de un asiento, respetando el dueño del lock.
     * - Si está OCUPADO: nunca se puede tocar (venta cerrada).
     * - Si está libre (DISPONIBLE): se bloquea para este sessionId.
     * - Si está SELECCIONADO por el mismo sessionId: se libera (toggle off).
     * - Si está SELECCIONADO por OTRO sessionId (EN_PROCESO para mí): la operación falla.
     */
    public ResultadoSeleccion seleccionarOliberarAsiento(Integer idVuelo, InfoAsientoDTO asientoSolicitado, String sessionId) {
        Logger.logInfo(String.format(
                "[seleccionarOliberarAsiento] Inicio - Vuelo ID: %s, Asiento: %s, SessionID: %s",
                idVuelo, asientoSolicitado != null ? asientoSolicitado.getNumeroAsiento() : "NULL", sessionId
        ));

        if (asientoSolicitado == null || idVuelo == null) {
            return ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
        }

        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null) {
            getAsientosVuelo(idVuelo);
            asientosMap = mapaVuelos.get(idVuelo);
            if (asientosMap == null) return ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
        }

        final ResultadoSeleccion[] resultado = { ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE };

        asientosMap.computeIfPresent(asientoSolicitado.getIdAsiento(), (id, asientoActual) -> {
            if ("OCUPADO".equalsIgnoreCase(asientoActual.getEstado())) {
                resultado[0] = ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
                return asientoActual;
            }

            boolean estaLibre = "libre".equalsIgnoreCase(asientoActual.getEstado());
            boolean esMio = "SELECCIONADO".equalsIgnoreCase(asientoActual.getEstado())
                    && sessionId != null && sessionId.equals(asientoActual.getReservadoPor());

            if (estaLibre) {
                asientoActual.setEstado("SELECCIONADO");
                asientoActual.setReservadoPor(sessionId);
                tiempoSeleccionMap.put(id, System.currentTimeMillis()); // Registrar tiempo de selección
                resultado[0] = ResultadoSeleccion.EXITO_SELECCIONADO;

            } else if (esMio) {
                asientoActual.setEstado("libre");
                asientoActual.setReservadoPor(null);
                tiempoSeleccionMap.remove(id); // Limpiar registro de tiempo
                resultado[0] = ResultadoSeleccion.EXITO_LIBERADO;

            } else {
                resultado[0] = ResultadoSeleccion.FALLO_BLOQUEADO_POR_OTRO;
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


    /**
     * Verifica si el asiento aún pertenece a la sesión y no ha sido liberado por el TTL.
     */
    public boolean validarPertenenciaYSeleccion(Integer idVuelo, Integer idAsiento, String sessionId) {
        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null) return false;

        InfoAsientoDTO asiento = asientosMap.get(idAsiento);
        if (asiento == null) return false;

        return "SELECCIONADO".equalsIgnoreCase(asiento.getEstado())
                && sessionId != null
                && sessionId.equals(asiento.getReservadoPor());
    }


    /**
     * Proceso en segundo plano que revisa y libera asientos expirados cada 30 segundos.
     */
    @Scheduled(fixedRate = 1000)
    public void liberarAsientosExpirados() {
        long ahora = System.currentTimeMillis();
        int totalVuelosEnMemoria = mapaVuelos.size();

        // Evitar saturar la consola si no hay vuelos cargados en la caché
        if (totalVuelosEnMemoria == 0) {
            return;
        }

        /*Logger.logInfo(String.format(
                "🔍 [TTL-CLEANER] Inicio de escaneo | Vuelos activos en mapa: %d | Límite TTL: %d ms",
                totalVuelosEnMemoria, TIEMPO_EXPIRACION_MS
        ));*/

        final int[] totalSeleccionados = {0};
        final int[] totalExpirados = {0};

        mapaVuelos.forEach((idVuelo, asientosMap) -> {
            //Logger.logInfo(String.format("🛫 [TTL-CLEANER] Escaneando Vuelo ID: %d (%d asientos cargados)", idVuelo, asientosMap.size()));

            asientosMap.forEach((idAsiento, asiento) -> {
                if ("SELECCIONADO".equalsIgnoreCase(asiento.getEstado())) {
                    totalSeleccionados[0]++;
                    Long timestamp = tiempoSeleccionMap.get(idAsiento);

                    if (timestamp != null) {
                        long transcurridoMs = ahora - timestamp;
                        long restanteMs = TIEMPO_EXPIRACION_MS - transcurridoMs;

                        /*Logger.logInfo(String.format(
                                "   ├─ Asiento ID: %d (%s) | Reservado por: %s | Transcurrido: %d ms | Restante: %d ms",
                                idAsiento, asiento.getNumeroAsiento(), asiento.getReservadoPor(), transcurridoMs, Math.max(0, restanteMs)
                        ));*/

                        if (transcurridoMs > TIEMPO_EXPIRACION_MS) {
                            totalExpirados[0]++;
                            String sessionOriginal = asiento.getReservadoPor();

                            // 1. Liberar Estado en Memoria
                            asiento.setEstado("libre");
                            asiento.setReservadoPor(null);
                            tiempoSeleccionMap.remove(idAsiento);

                            /*Logger.logInfo(String.format(
                                    "⏰ [TTL-EXPIRADO] Asiento ID: %d (%s) del Vuelo %d LIBERADO automáticamente tras %d ms de inactividad (Session: %s)",
                                    idAsiento, asiento.getNumeroAsiento(), idVuelo, transcurridoMs, sessionOriginal
                            ));*/

                            // 2. Notificación Push a los clientes WebSocket
                            EventoAsientoPush evento = new EventoAsientoPush(idAsiento, idVuelo, "libre", sessionOriginal);
                            String canalWebSocket = "/topic/vuelo/" + idVuelo;

                            messagingTemplate.convertAndSend(canalWebSocket, evento);

                            //Logger.logInfo(String.format("📡 [WEBSOCKET-PUSH] Evento de liberación enviado a '%s' para el Asiento ID: %d", canalWebSocket, idAsiento));
                        }
                    } else {
                        /*Logger.logWarn(String.format(
                                "   ⚠️ [TTL-CLEANER] Asiento ID: %d (%s) en estado SELECCIONADO pero sin timestamp en 'tiempoSeleccionMap'. Limpiando bloqueo huérfano...",
                                idAsiento, asiento.getNumeroAsiento()
                        ));*/
                        asiento.setEstado("libre");
                        asiento.setReservadoPor(null);
                    }
                }
            });
        });

        if (totalSeleccionados[0] > 0) {
            Logger.logInfo(String.format(
                    "✅ [TTL-CLEANER] Fin de escaneo | Asientos retenidos evaluados: %d | Asientos liberados en este ciclo: %d",
                    totalSeleccionados[0], totalExpirados[0]
            ));
        }
    }


    /**
     * Confirma la venta/reserva definitiva en memoria tras guardar en la BD.
     * Cambia el estado a OCUPADO y anula el limpiador TTL.
     */
    public void confirmarReservaDefinitiva(Integer idVuelo, List<Integer> idsAsientos) {
        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null) return;

        for (Integer idAsiento : idsAsientos) {
            asientosMap.computeIfPresent(idAsiento, (id, asiento) -> {
                // 1. Marcar como vendido
                asiento.setEstado("OCUPADO");
                asiento.setReservadoPor(null);

                // 2. Cancelar el temporizador TTL para que el limpiador de fondo no lo toque
                tiempoSeleccionMap.remove(id);

                Logger.logInfo(String.format(
                        "🔒 [COMPRA CONFIRMADA] Asiento ID: %d (%s) del Vuelo %d marcado como OCUPADO en memoria.",
                        id, asiento.getNumeroAsiento(), idVuelo
                ));

                // 3. Emitir evento WebSocket para renderizar la silla en rojo/bloqueada para todos los clientes
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