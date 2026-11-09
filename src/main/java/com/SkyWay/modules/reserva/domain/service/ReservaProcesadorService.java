package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.modules.metodopago.domain.service.MetodoPagoService;
import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.estadoreserva.domain.service.EstadoReservaService;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pago.domain.service.PagoService; // o tu repositorio/servicio de Pago
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

import java.math.BigDecimal;
import java.sql.SQLException;
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
    @Autowired private PagoService pagoService; // Inyección para guardar el Pago
    @Autowired private EmailReservaService emailReservaService; // Inyección para enviar el correo con el PDF
    @Autowired private MetodoPagoService metodoPagoService;
    @Autowired private com.SkyWay.modules.usuario.domain.service.UsuarioService usuarioService;
    @Autowired private com.SkyWay.modules.usuario.domain.repository.UsuarioRepository usuarioRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Integer ejecutarPersistenciaReserva(SolicitudReservaDTO dto) {
        return ejecutarPersistenciaReserva(dto, 1); // Por defecto ID_METODO_PAGO = 1 (o según el DTO)
    }

    @Transactional
    public Integer ejecutarPersistenciaReserva(SolicitudReservaDTO dto, Integer idMetodoPago) {
        Logger.logInfo("================================================================================");
        Logger.logInfo("📦 [RESERVA-PROCESSOR] INICIANDO PROCESO DE PERSISTENCIA DE RESERVA");
        Logger.logInfo("📋 Monto Total Reserva: $" + dto.montoTotal());
        Logger.logInfo("💳 ID Método de Pago recibido: " + idMetodoPago);
        Logger.logInfo("👥 Total Pasajeros recibidos en DTO: " + (dto.pasajeros() != null ? dto.pasajeros().size() : 0));
        Logger.logInfo("================================================================================");

        List<Pasajero> pasajerosList = dto.pasajeros();

        if (pasajerosList == null || pasajerosList.isEmpty()) {
            Logger.logError("❌ [RESERVA-PROCESSOR] La lista de pasajeros enviada está VACÍA o es NULL.");
            throw new IllegalArgumentException("No se pueden procesar reservas sin pasajeros.");
        }

        // -----------------------------------------------------------------------------------------
        // 0. VERIFICACIÓN ATÓMICA ANTI-CHURNING (DUPLICIDAD EN ITINERARIOS)
        // -----------------------------------------------------------------------------------------
        if (dto.tarifasItinerarios() != null && !dto.tarifasItinerarios().isEmpty()) {
            for (Pasajero p : pasajerosList) {
                String rutP = (p.getRut() != null && !p.getRut().isBlank()) ? p.getRut().trim() : p.getNumeroDocumento();
                if (rutP == null || rutP.isBlank()) continue;

                for (Integer idItin : dto.tarifasItinerarios().keySet()) {
                    var pnrExistenteOpt = reservaService.buscarReservaActivaPorPasajeroEItinerario(rutP, idItin);
                    if (pnrExistenteOpt.isPresent()) {
                        String errorMsg = String.format("El pasajero con identificación '%s' ya cuenta con una reserva activa (PNR: %s) para el itinerario comercial #%d.",
                                rutP, pnrExistenteOpt.get(), idItin);
                        Logger.logError("❌ [ANTI-CHURNING] " + errorMsg);
                        throw new IllegalStateException(errorMsg);
                    }
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // 1. REGISTRO Y AUDITORÍA DE PASAJEROS (UPSERT RESILIENTE)
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("🔍 --- PASO 1: VERIFICANDO Y ACTUALIZANDO PASAJEROS ---");
        List<Pasajero> pasajerosPersistidos = new ArrayList<>();

        for (int i = 0; i < pasajerosList.size(); i++) {
            Pasajero p = pasajerosList.get(i);

            // Sanitización defensiva de respaldo
            String rutP = (p.getRut() != null && !p.getRut().isBlank()) ? p.getRut().replace(".", "").trim().toUpperCase() : null;
            if (rutP == null && p.getNumeroDocumento() != null) {
                rutP = p.getNumeroDocumento().replace(".", "").trim().toUpperCase();
            }
            if (rutP == null || rutP.isBlank()) {
                rutP = "PAS-" + System.currentTimeMillis() + "-" + (i + 1);
            }
            p.setRut(rutP);

            String tipoDoc = (p.getTipoDocumento() != null && !p.getTipoDocumento().isBlank()) ? p.getTipoDocumento().trim().toUpperCase() : "CEDULA";
            p.setTipoDocumento(tipoDoc);

            String numDoc = (p.getNumeroDocumento() != null && !p.getNumeroDocumento().isBlank()) ? p.getNumeroDocumento().replace(".", "").trim().toUpperCase() : rutP;
            p.setNumeroDocumento(numDoc);

            String nacionalidad = (p.getNacionalidad() != null && !p.getNacionalidad().isBlank()) ? p.getNacionalidad().trim() : "Chilena";
            p.setNacionalidad(nacionalidad);

            Usuario usrInput = p.getUsuario() != null ? p.getUsuario() : new Usuario();
            String nombre = (usrInput.getNombre() != null && !usrInput.getNombre().isBlank()) ? usrInput.getNombre().trim() : "Pasajero";
            String apellido = (usrInput.getApellido() != null && !usrInput.getApellido().isBlank()) ? usrInput.getApellido().trim() : String.valueOf(i + 1);
            String correo = (usrInput.getCorreoElectronico() != null && !usrInput.getCorreoElectronico().isBlank()) ? usrInput.getCorreoElectronico().trim().toLowerCase() : null;
            String telefono = (usrInput.getTelefono() != null && !usrInput.getTelefono().isBlank()) ? usrInput.getTelefono().trim() : "+56900000000";
            Date fechaNac = usrInput.getFechaNacimiento() != null ? usrInput.getFechaNacimiento() : (p.getFechaNacimiento() != null ? p.getFechaNacimiento() : new Date(System.currentTimeMillis() - 630720000000L));

            // 1.1 Persistencia / Upsert Seguro de Usuario
            Usuario usrPersistido;
            Optional<Usuario> usrExistenteOpt = usuarioService.findByRut(rutP);

            if (usrExistenteOpt.isPresent()) {
                usrPersistido = usrExistenteOpt.get();
                usrPersistido.setNombre(nombre);
                usrPersistido.setApellido(apellido);
                usrPersistido.setTelefono(telefono);
                usrPersistido.setFechaNacimiento(fechaNac);

                // Documento: solo actualizar si no colisiona con otro usuario
                if (!usuarioRepository.existsByDocumentoIdentidadIgnoreCaseAndRutNot(numDoc, rutP)) {
                    usrPersistido.setDocumentoIdentidad(numDoc);
                }

                // Correo: solo actualizar si se envió y no colisiona con otro usuario
                if (correo != null && !usuarioRepository.existsByCorreoElectronicoIgnoreCaseAndRutNot(correo, rutP)) {
                    usrPersistido.setCorreoElectronico(correo);
                }
                usrPersistido = usuarioRepository.save(usrPersistido);
            } else {
                usrPersistido = new Usuario();
                usrPersistido.setRut(rutP);
                usrPersistido.setNombre(nombre);
                usrPersistido.setApellido(apellido);
                usrPersistido.setTelefono(telefono);
                usrPersistido.setFechaNacimiento(fechaNac);

                // Evitar colisión de documento con otro usuario
                if (usuarioRepository.existsByDocumentoIdentidadIgnoreCaseAndRutNot(numDoc, rutP)) {
                    usrPersistido.setDocumentoIdentidad(rutP);
                } else {
                    usrPersistido.setDocumentoIdentidad(numDoc);
                }

                // Evitar colisión de correo con otro usuario
                if (correo != null) {
                    if (usuarioRepository.existsByCorreoElectronicoIgnoreCaseAndRutNot(correo, rutP)) {
                        usrPersistido.setCorreoElectronico(rutP.replace("-", "").toLowerCase() + "@passenger.skyway.cl");
                    } else {
                        usrPersistido.setCorreoElectronico(correo);
                    }
                } else {
                    usrPersistido.setCorreoElectronico(rutP.replace("-", "").toLowerCase() + "@passenger.skyway.cl");
                }

                usrPersistido.setFechaRegistro(new Timestamp(System.currentTimeMillis()));
                usrPersistido = usuarioRepository.save(usrPersistido);
            }

            // 1.2 Persistencia / Upsert Seguro de Pasajero
            Pasajero pasPersistido;
            Optional<Pasajero> pasExistenteOpt = pasajeroService.findById(rutP);

            if (pasExistenteOpt.isPresent()) {
                pasPersistido = pasExistenteOpt.get();
                pasPersistido.setTipoDocumento(tipoDoc);
                pasPersistido.setNumeroDocumento(numDoc);
                pasPersistido.setNacionalidad(nacionalidad);
                pasPersistido.setFechaNacimiento(fechaNac);
                pasPersistido.setUsuario(usrPersistido);
                pasPersistido = pasajeroService.save(pasPersistido);
            } else {
                p.setRut(rutP);
                p.setTipoDocumento(tipoDoc);
                p.setNumeroDocumento(numDoc);
                p.setNacionalidad(nacionalidad);
                p.setFechaNacimiento(fechaNac);
                p.setUsuario(usrPersistido);
                pasPersistido = pasajeroService.save(p);
            }

            pasajerosPersistidos.add(pasPersistido);

            if (dto.equipajePorPasajero() != null && i < dto.equipajePorPasajero().size()) {
                dto.equipajePorPasajero().get(i).setRutPasajero(rutP);
            }
        }

        entityManager.flush();

        // -----------------------------------------------------------------------------------------
        // 2. CREACIÓN DEL ENCABEZADO DE LA RESERVA
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("📄 --- PASO 2: CREANDO ENCABEZADO DE RESERVA ---");
        Reserva reserva = new Reserva();
        Pasajero titular = pasajerosPersistidos.get(0);

        reserva.setPasajero(titular);

        var estado = estadoReservaService.findById(2)
                .orElseThrow(() -> new IllegalStateException("Estado de reserva 'PAGADA' (ID: 2) no encontrado"));

        reserva.setEstadoReservaBean(estado);
        reserva.setTotal(dto.montoTotal());
        reserva.setFechaReserva(new Timestamp(System.currentTimeMillis()));

        Reserva reservaGuardada = reservaService.save(reserva);
        entityManager.flush();

        Integer idReservaGenerado = reservaGuardada.getIdReserva();

        // Registrar a todos los pasajeros de la reserva en la tabla asociativa pasajero_reserva
        for (Pasajero p : pasajerosPersistidos) {
            String rutP = p.getRut();
            if (rutP != null && !rutP.isBlank()) {
                entityManager.createNativeQuery(
                        "INSERT INTO pasajero_reserva (id_reserva, rut) VALUES (:idReserva, :rut) ON CONFLICT DO NOTHING"
                )
                .setParameter("idReserva", idReservaGenerado)
                .setParameter("rut", rutP)
                .executeUpdate();
            }
        }

        // -----------------------------------------------------------------------------------------
        // 3. CONFIRMACIÓN Y ASOCIACIÓN DE ASIENTOS
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("💺 --- PASO 3: ASIGNANDO ASIENTOS A PASAJEROS ---");
        if (dto.asientosSeleccionados() != null) {
            dto.asientosSeleccionados().forEach((idVuelo, asientos) -> {
                List<Integer> idsConfirmados = new ArrayList<>();

                for (int idx = 0; idx < asientos.size(); idx++) {
                    InfoAsientoDTO asiento = asientos.get(idx);
                    Integer[] asientosIds = { asiento.getIdAsiento() };
                    String rutPasajeroAsignado = buscarRutPorAsiento(pasajerosPersistidos, idx, asiento.getIdAsiento());

                    try {
                        reservaService.confirmarReserva(idVuelo, asientosIds, rutPasajeroAsignado, idReservaGenerado);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    idsConfirmados.add(asiento.getIdAsiento());
                }

                if (!idsConfirmados.isEmpty()) {
                    asientoCacheService.confirmarReservaDefinitiva(idVuelo, idsConfirmados);
                }
            });
        }

        // -----------------------------------------------------------------------------------------
        // 4. CONFIRMACIÓN DE ITINERARIOS Y TARIFAS
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("🗺️ --- PASO 4: ASOCIANDO ITINERARIOS Y TARIFAS ---");
        if (dto.tarifasItinerarios() != null) {
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
        }

        // -----------------------------------------------------------------------------------------
        // 5. REGISTRO DE EQUIPAJE
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("🧳 --- PASO 5: REGISTRANDO EQUIPAJE POR PASAJERO ---");
        if (dto.equipajePorPasajero() != null) {
            dto.equipajePorPasajero().forEach(eqDTO -> {
                if (eqDTO.getMaletas() != null && !eqDTO.getMaletas().isEmpty()) {
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

        // -----------------------------------------------------------------------------------------
        // 6. REGISTRO DEL PAGO Y ENVÍO DEL E-TICKET VÍA CORREO
        // -----------------------------------------------------------------------------------------
        Logger.logInfo("💳 --- PASO 6: REGISTRANDO PAGO Y ENVIANDO CORREO ---");
        // -----------------------------------------------------------------------------------------
// 6. REGISTRO DEL PAGO Y ENVÍO DEL E-TICKET VÍA CORREO
// -----------------------------------------------------------------------------------------
        Logger.logInfo("💳 --- PASO 6: REGISTRANDO PAGO Y ENVIANDO CORREO ---");
        try {
            Pago pago = new Pago();
            pago.setMontoPagado(dto.montoTotal());
            pago.setFecha(new Timestamp(System.currentTimeMillis()));
            pago.setReserva(reservaGuardada);

            int idMetodoFinal = (idMetodoPago != null) ? idMetodoPago : 1;

            // Obtener descripción para log claro
            MetodoPago mp = metodoPagoService.findById(idMetodoFinal).orElse(null);
            String nombreMetodo = (mp != null && mp.getDescripcion() != null) ? mp.getDescripcion() : "Desconocido (ID: " + idMetodoFinal + ")";

            Logger.logInfo(String.format("🔍 [AUDITORÍA PAGO] Asociando Pago -> Reserva ID: %d | Monto: $%s | Método Pago ID: %d (%s)",
                    idReservaGenerado, dto.montoTotal(), idMetodoFinal, nombreMetodo));

            if (mp == null) {
                mp = new MetodoPago();
                mp.setIdMetodoPago(idMetodoFinal);
            }
            pago.setMetodoPago(mp);

            Pago pagoGuardado = pagoService.save(pago);
            Logger.logInfo(String.format("✅ [BD] Registro de Pago #%d GUARDADO EXITOSAMENTE en BD | Método: '%s'",
                    pagoGuardado.getIdPago(), nombreMetodo));

            // Asocia la lista de pagos a la entidad Reserva para el PDF
            reservaGuardada.setPagos(List.of(pagoGuardado));

        } catch (Exception e) {
            Logger.logError("⚠️ Error crítico en el PASO 6 al registrar pago o enviar correo: " + e.getMessage());
            e.printStackTrace();
        }

        // -----------------------------------------------------------------------------------------
        // WAL / AUDITORÍA FINAL: REGISTRO DE PERSISTENCIA ATÓMICA EXITOSA
        // -----------------------------------------------------------------------------------------
        try {
            entityManager.createNativeQuery(
                    "INSERT INTO Log_Auditoria (rut_usuario, accion, nivel, tabla_afectada, id_registro_afectado, detalle, fecha_hora) " +
                    "VALUES (:rut, 'RESERVA_COMPLETADA_EXITOSA', 'INFO'::tipo_nivel_log, 'Reserva', :idReserva, :detalle, NOW())"
            )
            .setParameter("rut", titular.getRut())
            .setParameter("idReserva", idReservaGenerado)
            .setParameter("detalle", String.format("Reserva #%d persistida atómicamente con éxito tras confirmación de pago.", idReservaGenerado))
            .executeUpdate();
        } catch (Exception logEx) {
            Logger.logWarn("⚠️ [WAL] Entrada de éxito en Log_Auditoria omitida: " + logEx.getMessage());
        }

        SesionReservaUtil.limpiarObjetosSesionReserva();
        Logger.logInfo("================================================================================");
        Logger.logInfo(String.format("🎉 [RESERVA-PROCESSOR] RESERVA ID: %d FINALIZADA Y PROCESADA CON ÉXITO", idReservaGenerado));
        Logger.logInfo("================================================================================");

        return idReservaGenerado;
    }

    private String buscarRutPorAsiento(List<Pasajero> pasajeros, int indiceAsiento, int idAsiento) {
        if (pasajeros == null || pasajeros.isEmpty()) return "";
        if (indiceAsiento < pasajeros.size() && pasajeros.get(indiceAsiento).getRut() != null) {
            return pasajeros.get(indiceAsiento).getRut();
        }
        return pasajeros.get(0).getRut();
    }
}