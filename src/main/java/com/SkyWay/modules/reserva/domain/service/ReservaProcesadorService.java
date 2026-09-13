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
        Logger.logInfo("📦 [RESERVA-PROCESSOR] Iniciando persistencia. Total pasajeros: " + dto.pasajeros().size());

        // 1. Guardar o actualizar Pasajeros
        List<Pasajero> pasajerosList = dto.pasajeros();
        for (int i = 0; i < pasajerosList.size(); i++) {
            Pasajero p = pasajerosList.get(i);

            if (dto.equipajePorPasajero() != null && i < dto.equipajePorPasajero().size()) {
                dto.equipajePorPasajero().get(i).setRutPasajero(p.getRut());
            }

            Usuario usr = p.getUsuario() != null ? p.getUsuario() : new Usuario();
            usr.setRut(p.getRut());
            p.setUsuario(usr);

            if (!pasajeroService.findById(p.getRut()).isPresent()) {
                pasajeroService.save(p);
                Logger.logInfo("👤 [RESERVA-PROCESSOR] Nuevo pasajero registrado RUT: " + p.getRut());
            }
        }

        // 2. Crear Encabezado de Reserva y Sincronizar (FLUSH) con PostgreSQL
        Reserva reserva = new Reserva();
        Pasajero titular = pasajerosList.get(0);
        reserva.setPasajero(titular);

        var estado = estadoReservaService.findById(2)
                .orElseThrow(() -> new IllegalStateException("Estado de reserva 'PAGADA' (ID: 2) no encontrado"));

        reserva.setEstadoReservaBean(estado);
        reserva.setTotal(dto.montoTotal());
        reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

        // Guardar y Forzar la inserción inmediata en BD
        Reserva reservaGuardada = reservaService.save(reserva);
        entityManager.flush();

        Integer idReservaGenerado = reservaGuardada.getIdReserva();
        Logger.logInfo("💾 [RESERVA-PROCESSOR] Encabezado 'reserva' guardado y commiteado con ID: " + idReservaGenerado);

        // 3. Confirmar Asientos asociándolos a la FK idReservaGenerado
        dto.asientosSeleccionados().forEach((idVuelo, asientos) -> {
            List<Integer> idsConfirmados = new ArrayList<>();

            for (InfoAsientoDTO asiento : asientos) {
                Integer[] asientosIds = { asiento.getIdAsiento() };
                String rutPasajero = buscarRutPorAsiento(pasajerosList, asiento.getIdAsiento());

                try {
                    Logger.logInfo(String.format("💺 [RESERVA-PROCESSOR] Asociando Asiento ID: %d (Vuelo: %d) -> Reserva ID: %d",
                            asiento.getIdAsiento(), idVuelo, idReservaGenerado));

                    reservaService.confirmarReserva(idVuelo, asientosIds, rutPasajero, idReservaGenerado);
                    idsConfirmados.add(asiento.getIdAsiento());

                } catch (Exception e) {
                    Logger.logError(String.format("💥 Error al confirmar asiento %d para Reserva %d: %s",
                            asiento.getIdAsiento(), idReservaGenerado, e.getMessage()));
                    throw new RuntimeException("Error FK Reserva-Asiento: " + e.getMessage(), e);
                }
            }

            if (!idsConfirmados.isEmpty()) {
                asientoCacheService.confirmarReservaDefinitiva(idVuelo, idsConfirmados);
                Logger.logInfo("🔥 [CACHE] Asientos confirmados en Redis para Vuelo ID: " + idVuelo);
            }
        });

        // 4. Confirmar Itinerarios y Tarifas
        dto.tarifasItinerarios().forEach((idItinerario, idTarifa) -> {
            var itinerario = itinerarioService.findById(idItinerario);
            ReservaItinerario rersv = new ReservaItinerario();
            rersv.setReserva(reservaGuardada);
            rersv.setItinerario(itinerario);

            var tarifaItinerario = itinerarioTarifaService.getByTarifaAndItinerario(idItinerario, idTarifa);
            if (tarifaItinerario == null) {
                throw new IllegalStateException("No se encontró ItinerarioTarifa para Itinerario " + idItinerario + " y Tarifa " + idTarifa);
            }
            rersv.setItinerarioTarifa(tarifaItinerario);
            reservaItinerarioService.save(rersv);
        });

        // 5. Confirmar Equipaje
        if (dto.equipajePorPasajero() != null) {
            dto.equipajePorPasajero().forEach(eqDTO -> {
                if (eqDTO.getMaletas() != null) {
                    eqDTO.getMaletas().forEach(item -> {
                        Equipaje equipaje = new Equipaje();
                        equipaje.setPeso(item.getPeso());
                        equipaje.setDimensiones(item.getDimensiones());
                        equipaje.setTipo(item.getTipo());
                        equipajeService.guardar(equipaje, idReservaGenerado, eqDTO.getRutPasajero(), item.getIdTipo());
                    });
                }
            });
        }

        Logger.logInfo("🎉 [RESERVA-PROCESSOR] Proceso finalizado con éxito. Reserva ID: " + idReservaGenerado);
        return idReservaGenerado;
    }

    private String buscarRutPorAsiento(List<Pasajero> pasajeros, int idAsiento) {
        return pasajeros.stream()
                .filter(p -> p.getRut() != null)
                .findFirst()
                .map(Pasajero::getRut)
                .orElse(pasajeros.isEmpty() ? "" : pasajeros.get(0).getRut());
    }
}