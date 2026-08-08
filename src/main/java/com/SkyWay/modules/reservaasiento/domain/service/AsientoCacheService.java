package com.SkyWay.modules.reservaasiento.domain.service;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.EventoAsientoPush;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsientoCacheService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AsientoService asientoService;

    // Map<idVuelo, Map<idAsiento, InfoAsientoDTO>>
    private final Map<Integer, Map<Integer, InfoAsientoDTO>> mapaVuelos = new ConcurrentHashMap<>();

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
                "[seleccionarOliberarAsiento] Inicio - Vuelo ID: %s, Asiento Solicitado: %s (ID: %s, Estado recibido: %s), SessionID: %s",
                idVuelo,
                asientoSolicitado != null ? asientoSolicitado.getNumeroAsiento() : "NULL",
                asientoSolicitado != null ? asientoSolicitado.getIdAsiento() : "NULL",
                asientoSolicitado != null ? asientoSolicitado.getEstado() : "NULL",
                sessionId
        ));

        if (asientoSolicitado == null || idVuelo == null) {
            Logger.logWarn("[seleccionarOliberarAsiento] Parámetros inválidos (idVuelo o asientoSolicitado son nulos).");
            return ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
        }

        Map<Integer, InfoAsientoDTO> asientosMap = mapaVuelos.get(idVuelo);
        if (asientosMap == null) {
            Logger.logInfo(String.format(
                    "[seleccionarOliberarAsiento] Mapa no encontrado para Vuelo ID: %d. Cargando asientos desde repositorio/servicio...",
                    idVuelo
            ));
            getAsientosVuelo(idVuelo);
            asientosMap = mapaVuelos.get(idVuelo);

            if (asientosMap == null) {
                Logger.logError(String.format(
                        "[seleccionarOliberarAsiento] No se pudieron cargar los asientos para el Vuelo ID: %d.",
                        idVuelo
                ));
                return ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
            }
        }

        final ResultadoSeleccion[] resultado = { ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE };

        asientosMap.computeIfPresent(asientoSolicitado.getIdAsiento(), (id, asientoActual) -> {
            Logger.logInfo(String.format(
                    "[seleccionarOliberarAsiento] Evaluando asiento en mapa -> ID: %d, Número: %s, Estado Actual: %s, ReservadoPor Actual: %s",
                    id, asientoActual.getNumeroAsiento(), asientoActual.getEstado(), asientoActual.getReservadoPor()
            ));

            if ("OCUPADO".equalsIgnoreCase(asientoActual.getEstado())) {
                Logger.logWarn(String.format(
                        "[seleccionarOliberarAsiento] Asiento ID: %d (%s) ya está OCUPADO definitivamente.",
                        id, asientoActual.getNumeroAsiento()
                ));
                resultado[0] = ResultadoSeleccion.FALLO_ASIENTO_NO_DISPONIBLE;
                return asientoActual;
            }

            boolean estaLibre = "libre".equalsIgnoreCase(asientoActual.getEstado());
            boolean esMio = "SELECCIONADO".equalsIgnoreCase(asientoActual.getEstado())
                    && sessionId != null && sessionId.equals(asientoActual.getReservadoPor());

            if (estaLibre) {
                asientoActual.setEstado("SELECCIONADO");
                asientoActual.setReservadoPor(sessionId);
                resultado[0] = ResultadoSeleccion.EXITO_SELECCIONADO;

                Logger.logInfo(String.format(
                        "[seleccionarOliberarAsiento] [ÉXITO SELECCIÓN] Asiento ID: %d (%s) cambiado a SELECCIONADO por SessionID: %s",
                        id, asientoActual.getNumeroAsiento(), sessionId
                ));
            } else if (esMio) {
                asientoActual.setEstado("libre");
                asientoActual.setReservadoPor(null);
                resultado[0] = ResultadoSeleccion.EXITO_LIBERADO;

                Logger.logInfo(String.format(
                        "[seleccionarOliberarAsiento] [ÉXITO LIBERACIÓN] Asiento ID: %d (%s) liberado a DISPONIBLE por su dueño (SessionID: %s)",
                        id, asientoActual.getNumeroAsiento(), sessionId
                ));
            } else {
                // Bloqueado por otro usuario en este preciso instante
                resultado[0] = ResultadoSeleccion.FALLO_BLOQUEADO_POR_OTRO;

                Logger.logWarn(String.format(
                        "[seleccionarOliberarAsiento] [BLOQUEADO] Asiento ID: %d (%s) intentado por SessionID: %s, pero pertenece a SessionID: %s (Estado: %s)",
                        id, asientoActual.getNumeroAsiento(), sessionId, asientoActual.getReservadoPor(), asientoActual.getEstado()
                ));
            }
            return asientoActual;
        });

        if (resultado[0] == ResultadoSeleccion.EXITO_SELECCIONADO || resultado[0] == ResultadoSeleccion.EXITO_LIBERADO) {
            InfoAsientoDTO actualizado = asientosMap.get(asientoSolicitado.getIdAsiento());

            if (actualizado != null) {
                EventoAsientoPush evento = new EventoAsientoPush(
                        actualizado.getIdAsiento(), idVuelo, actualizado.getEstado(), sessionId);

                String destination = "/topic/vuelo/" + idVuelo;
                Logger.logInfo(String.format(
                        "[seleccionarOliberarAsiento] Emitiendo evento WebSocket a '%s' -> Asiento ID: %d, Estado: %s, SessionID: %s",
                        destination, actualizado.getIdAsiento(), actualizado.getEstado(), sessionId
                ));

                messagingTemplate.convertAndSend(destination, evento);
            } else {
                Logger.logError(String.format(
                        "[seleccionarOliberarAsiento] No se pudo obtener el asiento actualizado (ID: %d) para emitir el evento WebSocket.",
                        asientoSolicitado.getIdAsiento()
                ));
            }
        } else {
            Logger.logInfo(String.format(
                    "[seleccionarOliberarAsiento] No se emite evento WebSocket debido a resultado: %s (Asiento ID: %d)",
                    resultado[0], asientoSolicitado.getIdAsiento()
            ));
        }

        return resultado[0];
    }


    public enum ResultadoSeleccion {
        EXITO_SELECCIONADO, EXITO_LIBERADO,
        FALLO_ASIENTO_NO_DISPONIBLE, FALLO_BLOQUEADO_POR_OTRO
    }
}