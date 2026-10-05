package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.views.reserva.EquipajePasajeroDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.estadoreserva.domain.service.EstadoReservaService;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reservaasiento.domain.service.AsientoCacheService;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.service.ReservaItinerarioService;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Service
public class ReservaProcesadorService {

    @Autowired private PasajeroService pasajeroService;
    @Autowired private ReservaService reservaService;
    @Autowired private EstadoReservaService estadoReservaService;
    @Autowired private AsientoCacheService asientoCacheService;
    @Autowired private ItinerarioService itinerarioService;
    @Autowired private ItinerarioTarifaService itinerarioTarifaService;
    @Autowired private ReservaItinerarioService reservaItinerarioService;
    @Autowired private EquipajeService equipajeService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Integer ejecutarPersistenciaReserva(SolicitudReservaDTO dto) {
        Logger.logInfo("================================================================================");
        Logger.logInfo("📦 [RESERVA-PROCESSOR] INICIANDO PROCESO DE PERSISTENCIA DE RESERVA");
        Logger.logInfo("📋 Monto Total Reserva: $" + dto.montoTotal());
        Logger.logInfo("👥 Total Pasajeros recibidos en DTO: " + (dto.pasajeros() != null ? dto.pasajeros().size() : 0));
        Logger.logInfo("================================================================================");

        List<Pasajero> pasajerosList = dto.pasajeros();

        if (pasajerosList == null || pasajerosList.isEmpty()) {
            Logger.logError("❌ [RESERVA-PROCESSOR] La lista de pasajeros enviada está VACÍA o es NULL.");
            throw new IllegalArgumentException("No se pueden procesar reservas sin pasajeros.");
        }

        // -----------------------------------------------------------------------------------------
        // 1. REGISTRO Y AUDITORÍA DE PASAJEROS
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("🔍 --- PASO 1: VERIFICANDO Y GUARDANDO PASAJEROS ---");
        for (int i = 0; i < pasajerosList.size(); i++) {
            Pasajero p = pasajerosList.get(i);
            Usuario usr = p.getUsuario() != null ? p.getUsuario() : new Usuario();
            String nombreCompleto = usr.getNombre() + " " + usr.getApellido();

            Logger.logInfo(String.format("👤 [PASAJERO %d/%d] RUT: '%s' | Nombre: '%s' | Correo: '%s'",
                    i + 1, pasajerosList.size(), p.getRut(), nombreCompleto, usr.getCorreoElectronico()));

            if (dto.equipajePorPasajero() != null && i < dto.equipajePorPasajero().size()) {
                dto.equipajePorPasajero().get(i).setRutPasajero(p.getRut());
                Logger.logInfo(String.format("   🎒 Equipaje vinculado al RUT: '%s' (Índice %d)", p.getRut(), i));
            }

            usr.setRut(p.getRut());
            p.setUsuario(usr);

            boolean existe = pasajeroService.findById(p.getRut()).isPresent();
            if (!existe) {
                pasajeroService.save(p);
                Logger.logInfo(String.format("   ✅ [BD] Pasajero NUEVO guardado exitosamente en BD -> RUT: '%s'", p.getRut()));
            } else {
                Logger.logInfo(String.format("   ℹ️ [BD] Pasajero ya existía previamente en la base de datos -> RUT: '%s'", p.getRut()));
            }
        }

        // -----------------------------------------------------------------------------------------
        // 2. CREACIÓN DEL ENCABEZADO DE LA RESERVA (ASIGNACIÓN DE TITULAR)
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("📄 --- PASO 2: CREANDO ENCABEZADO DE RESERVA ---");
        Reserva reserva = new Reserva();
        Pasajero titular = pasajerosList.get(0);

        Logger.logInfo(String.format("👑 [TITULAR RESERVA] Asignando Pasajero Principal (Titular) -> RUT: '%s', Nombre: '%s %s'",
                titular.getRut(),
                titular.getUsuario() != null ? titular.getUsuario().getNombre() : "N/D",
                titular.getUsuario() != null ? titular.getUsuario().getApellido() : "N/D"));

        reserva.setPasajero(titular);

        var estado = estadoReservaService.findById(2)
                .orElseThrow(() -> new IllegalStateException("Estado de reserva 'PAGADA' (ID: 2) no encontrado"));

        reserva.setEstadoReservaBean(estado);
        reserva.setTotal(dto.montoTotal());
        reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

        Reserva reservaGuardada = reservaService.save(reserva);
        entityManager.flush();

        Integer idReservaGenerado = reservaGuardada.getIdReserva();
        Logger.logInfo(String.format("💾 [BD] Encabezado 'Reserva' creado Exitosamente. ID_RESERVA: %d | Titular RUT: '%s'",
                idReservaGenerado, titular.getRut()));

        // -----------------------------------------------------------------------------------------
        // 3. CONFIRMACIÓN Y ASOCIACIÓN DE ASIENTOS Y PASAJEROS POR VUELO
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("💺 --- PASO 3: ASIGNANDO ASIENTOS A PASAJEROS ---");
        if (dto.asientosSeleccionados() != null) {
            dto.asientosSeleccionados().forEach((idVuelo, asientos) -> {
                Logger.logInfo(String.format("✈️ Procesando Asientos para VUELO ID: %d (Total asientos a reservar: %d)", idVuelo, asientos.size()));
                List<Integer> idsConfirmados = new ArrayList<>();

                for (int idx = 0; idx < asientos.size(); idx++) {
                    InfoAsientoDTO asiento = asientos.get(idx);
                    Integer[] asientosIds = { asiento.getIdAsiento() };

                    // Búsqueda / Asignación del RUT de pasajero correspondiente
                    String rutPasajeroAsignado = buscarRutPorAsiento(pasajerosList, idx, asiento.getIdAsiento());

                    try {
                        Logger.logInfo(String.format("   👉 Asiento ID: %d ('%s') -> Se asignará al Pasajero RUT: '%s' | Reserva ID: %d",
                                asiento.getIdAsiento(), asiento.getNumeroAsiento(), rutPasajeroAsignado, idReservaGenerado));

                        reservaService.confirmarReserva(idVuelo, asientosIds, rutPasajeroAsignado, idReservaGenerado);
                        idsConfirmados.add(asiento.getIdAsiento());

                        Logger.logInfo(String.format("   ✅ [OK] Asiento %d asignado exitosamente a RUT '%s' en Vuelo %d",
                                asiento.getIdAsiento(), rutPasajeroAsignado, idVuelo));

                    } catch (Exception e) {
                        Logger.logError(String.format("   💥 [ERROR] Falló la asociación del Asiento %d al Pasajero RUT '%s' para Reserva %d: %s",
                                asiento.getIdAsiento(), rutPasajeroAsignado, idReservaGenerado, e.getMessage()));
                        throw new RuntimeException("Error FK Reserva-Asiento: " + e.getMessage(), e);
                    }
                }

                if (!idsConfirmados.isEmpty()) {
                    asientoCacheService.confirmarReservaDefinitiva(idVuelo, idsConfirmados);
                    Logger.logInfo("   🔥 [REDIS CACHE] Asientos liberados del Hold temporal y confirmados definitivamente para Vuelo ID: " + idVuelo);
                }
            });
        } else {
            Logger.logWarn("⚠️ No se encontraron asientos seleccionados en el DTO recibido.");
        }

        // -----------------------------------------------------------------------------------------
        // 4. CONFIRMACIÓN DE ITINERARIOS Y TARIFAS
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("🗺️ --- PASO 4: ASOCIANDO ITINERARIOS Y TARIFAS A LA RESERVA ---");
        if (dto.tarifasItinerarios() != null) {
            dto.tarifasItinerarios().forEach((idItinerario, idTarifa) -> {
                var itinerario = itinerarioService.findById(idItinerario);
                ReservaItinerario rersv = new ReservaItinerario();
                rersv.setReserva(reservaGuardada);
                rersv.setItinerario(itinerario);

                var tarifaItinerario = itinerarioTarifaService.getByTarifaAndItinerario(idItinerario, idTarifa);
                if (tarifaItinerario == null) {
                    Logger.logError(String.format("❌ No se encontró la combinación ItinerarioTarifa para Itinerario %d y Tarifa %d", idItinerario, idTarifa));
                    throw new IllegalStateException("No se encontró ItinerarioTarifa para Itinerario " + idItinerario + " y Tarifa " + idTarifa);
                }
                rersv.setItinerarioTarifa(tarifaItinerario);
                reservaItinerarioService.save(rersv);

                Logger.logInfo(String.format("   📌 Reserva ID %d asociada a Itinerario ID %d con Tarifa ID %d (Monto Tarifa: $%s)",
                        idReservaGenerado, idItinerario, idTarifa, tarifaItinerario.getPrecio()));
            });
        }

        // -----------------------------------------------------------------------------------------
        // 5. REGISTRO Y ASIGNACIÓN DE EQUIPAJE POR PASAJERO
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("🧳 --- PASO 5: REGISTRANDO EQUIPAJE POR PASAJERO ---");
        if (dto.equipajePorPasajero() != null) {
            dto.equipajePorPasajero().forEach(eqDTO -> {
                Logger.logInfo(String.format("   🧳 Procesando equipaje para Pasajero RUT: '%s' (Nombre: '%s')",
                        eqDTO.getRutPasajero(), eqDTO.getNombrePasajero()));

                if (eqDTO.getMaletas() != null && !eqDTO.getMaletas().isEmpty()) {
                    eqDTO.getMaletas().forEach(item -> {
                        Equipaje equipaje = new Equipaje();
                        equipaje.setPeso(item.getPeso());
                        equipaje.setDimensiones(item.getDimensiones());
                        equipaje.setTipo(item.getTipo());

                        equipajeService.guardar(equipaje, idReservaGenerado, eqDTO.getRutPasajero(), item.getIdTipo());
                        Logger.logInfo(String.format("      📦 Maleta registrada -> Tipo ID: %d | Descripción: '%s' | Peso: %.1fkg | RUT: '%s' | Reserva: %d",
                                item.getIdTipo(), item.getDescripcion(), item.getPeso(), eqDTO.getRutPasajero(), idReservaGenerado));
                    });
                } else {
                    Logger.logInfo("      ℹ️ Sin maletas adicionales registradas para este pasajero.");
                }
            });
        }

        Logger.logInfo("================================================================================");
        Logger.logInfo(String.format("🎉 [RESERVA-PROCESSOR] RESERVA ID: %d FINALIZADA Y PROCESADA CON ÉXITO", idReservaGenerado));
        Logger.logInfo("================================================================================");

        return idReservaGenerado;
    }

    /**
     * Mapea un asiento a su pasajero correspondiente por índice de lista o fallback.
     */
    private String buscarRutPorAsiento(List<Pasajero> pasajeros, int indiceAsiento, int idAsiento) {
        if (pasajeros == null || pasajeros.isEmpty()) {
            Logger.logWarn("⚠️ Lista de pasajeros vacía al mapear asiento ID: " + idAsiento);
            return "";
        }

        if (indiceAsiento < pasajeros.size() && pasajeros.get(indiceAsiento).getRut() != null) {
            String rutEncontrado = pasajeros.get(indiceAsiento).getRut();
            Logger.logInfo(String.format("   🎯 Mapeo directo por índice [%d]: Asiento ID %d -> Pasajero RUT '%s'",
                    indiceAsiento, idAsiento, rutEncontrado));
            return rutEncontrado;
        }

        // Fallback al primer pasajero (Titular) si el índice excede la lista
        String rutFallback = pasajeros.get(0).getRut();
        Logger.logWarn(String.format("   ⚠️ Índice de asiento [%d] fuera de rango. Usando Fallback (Titular) RUT: '%s' para Asiento ID %d",
                indiceAsiento, rutFallback, idAsiento));
        return rutFallback;
    }
}