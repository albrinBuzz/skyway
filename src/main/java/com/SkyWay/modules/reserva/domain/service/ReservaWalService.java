package com.SkyWay.modules.reserva.domain.service;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.reserva.presentation.dto.SolicitudReservaDTO;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.util.Logger;
import com.SkyWay.views.reserva.EquipajePasajeroDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ReservaWalService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * DTO Snapshot seguro e inmutable para serialización en WAL.
     * Contiene únicamente tipos primitivos y desacoplados de Hibernate (cero proxies lazy).
     */
    public record WalPasajeroSnapshot(
            String rut,
            String nombre,
            String apellido,
            String correoElectronico,
            String telefono,
            String tipoDocumento,
            String numeroDocumento,
            String nacionalidad,
            Date fechaNacimiento
    ) implements Serializable {}

    public record WalPayloadSnapshot(
            String rutTitular,
            String emailContacto,
            List<WalPasajeroSnapshot> pasajeros,
            Map<Integer, List<InfoAsientoDTO>> asientosSeleccionados,
            Map<Integer, Integer> tarifasItinerarios,
            List<EquipajePasajeroDTO> equipajePorPasajero,
            BigDecimal montoTotal,
            String sessionId,
            String tokenPasarela,
            Integer idMetodoPago,
            Date fechaCaptura
    ) implements Serializable {}

    /**
     * Asegura la creación de la tabla especializada Wal_Transaccion_Pago en la base de datos.
     */
    @PostConstruct
    @Transactional
    public void initSchema() {
        try {
            entityManager.createNativeQuery(
                    "CREATE TABLE IF NOT EXISTS Wal_Transaccion_Pago (" +
                    "    id_wal BIGSERIAL PRIMARY KEY," +
                    "    token_pasarela VARCHAR(255) NOT NULL," +
                    "    metodo_pago VARCHAR(50) NOT NULL," +
                    "    id_metodo_pago INT," +
                    "    monto NUMERIC(12, 2) NOT NULL," +
                    "    moneda VARCHAR(10) DEFAULT 'CLP'," +
                    "    rut_titular VARCHAR(15)," +
                    "    email_contacto VARCHAR(150)," +
                    "    estado VARCHAR(50) NOT NULL DEFAULT 'CAPTURADO'," +
                    "    id_reserva INT REFERENCES Reserva(ID_RESERVA) ON DELETE SET NULL," +
                    "    payload_snapshot JSONB NOT NULL," +
                    "    motivo_fallo TEXT," +
                    "    direccion_ip VARCHAR(45)," +
                    "    user_agent VARCHAR(255)," +
                    "    fecha_captura TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()," +
                    "    fecha_actualizacion TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()" +
                    ");" +
                    "CREATE INDEX IF NOT EXISTS idx_wal_token ON Wal_Transaccion_Pago(token_pasarela);" +
                    "CREATE INDEX IF NOT EXISTS idx_wal_estado ON Wal_Transaccion_Pago(estado);" +
                    "CREATE INDEX IF NOT EXISTS idx_wal_rut ON Wal_Transaccion_Pago(rut_titular);" +
                    "CREATE INDEX IF NOT EXISTS idx_wal_fecha ON Wal_Transaccion_Pago(fecha_captura DESC);"
            ).executeUpdate();
            Logger.logInfo("✅ [WAL-SCHEMA] Tabla especializada 'Wal_Transaccion_Pago' verificada y lista.");
        } catch (Exception e) {
            Logger.logWarn("⚠️ [WAL-SCHEMA] Verificación de tabla Wal_Transaccion_Pago omitida: " + e.getMessage());
        }
    }

    /**
     * Registra un WAL (Write-Ahead Log) autónomo antes de ejecutar la persistencia atómica.
     * Se ejecuta con REQUIRES_NEW para garantizar que el cobro quede registrado sin importar el resultado posterior.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long registrarInicioPago(SolicitudReservaDTO dto, Integer idMetodoPago, String token, String ip, String userAgent) {
        if (dto == null) return null;

        try {
            String rutTitular = (dto.pasajeros() != null && !dto.pasajeros().isEmpty() && dto.pasajeros().get(0).getRut() != null)
                    ? dto.pasajeros().get(0).getRut().replace(".", "").trim().toUpperCase()
                    : (dto.rutUsuarioTitular() != null ? dto.rutUsuarioTitular() : "DESCONOCIDO");

            String emailContacto = (dto.pasajeros() != null && !dto.pasajeros().isEmpty()
                    && dto.pasajeros().get(0).getUsuario() != null
                    && dto.pasajeros().get(0).getUsuario().getCorreoElectronico() != null)
                    ? dto.pasajeros().get(0).getUsuario().getCorreoElectronico().trim().toLowerCase()
                    : null;

            String metodoStr = switch (idMetodoPago != null ? idMetodoPago : 1) {
                case 2 -> "PAYPAL";
                case 3 -> "MERCADOPAGO";
                default -> "WEBPAY";
            };

            WalPayloadSnapshot snapshot = construirSnapshot(dto, idMetodoPago, token, rutTitular, emailContacto);
            String payloadJson = objectMapper.writeValueAsString(snapshot);

            Object result = entityManager.createNativeQuery(
                    "INSERT INTO Wal_Transaccion_Pago (" +
                    "    token_pasarela, metodo_pago, id_metodo_pago, monto, moneda, rut_titular, email_contacto, " +
                    "    estado, payload_snapshot, direccion_ip, user_agent, fecha_captura, fecha_actualizacion" +
                    ") VALUES (" +
                    "    :token, :metodo, :idMetodo, :monto, 'CLP', :rut, :email, 'CAPTURADO', " +
                    "    CAST(:payload AS jsonb), :ip, :userAgent, NOW(), NOW()" +
                    ") RETURNING id_wal"
            )
            .setParameter("token", token != null ? token : "TOKEN-DESCONOCIDO")
            .setParameter("metodo", metodoStr)
            .setParameter("idMetodo", idMetodoPago)
            .setParameter("monto", dto.montoTotal())
            .setParameter("rut", rutTitular)
            .setParameter("email", emailContacto)
            .setParameter("payload", payloadJson)
            .setParameter("ip", ip != null ? ip : "127.0.0.1")
            .setParameter("userAgent", userAgent != null && userAgent.length() > 250 ? userAgent.substring(0, 250) : userAgent)
            .getSingleResult();

            Long idWal = (result instanceof Number) ? ((Number) result).longValue() : Long.valueOf(result.toString());
            Logger.logInfo(String.format("📝 [WAL-DEDICADO] Transacción de pago registrada con éxito en Wal_Transaccion_Pago ID #%d | Token: %s", idWal, token));
            return idWal;
        } catch (Exception e) {
            Logger.logError("❌ [WAL-DEDICADO] Error al registrar en Wal_Transaccion_Pago: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Marca el registro WAL como completado y enlazado a la reserva persistida.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarWalCompletado(Long idWal, Integer idReserva) {
        if (idWal == null) return;
        try {
            entityManager.createNativeQuery(
                    "UPDATE Wal_Transaccion_Pago " +
                    "SET estado = 'PERSISTIDO_EXITOSO', " +
                    "    id_reserva = :idReserva, " +
                    "    fecha_actualizacion = NOW() " +
                    "WHERE id_wal = :idWal"
            )
            .setParameter("idReserva", idReserva)
            .setParameter("idWal", idWal)
            .executeUpdate();
            Logger.logInfo(String.format("✅ [WAL-DEDICADO] WAL ID #%d enlazado exitosamente a Reserva #%d", idWal, idReserva));
        } catch (Exception e) {
            Logger.logWarn("⚠️ [WAL-DEDICADO] No se pudo actualizar estado exitoso en WAL ID #" + idWal + ": " + e.getMessage());
        }
    }

    /**
     * Marca el registro WAL como fallido por conflicto de concurrencia o error de escritura,
     * conservando todos los datos para reconstrucción o reembolso.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void marcarWalFallido(Long idWal, String motivo, Throwable error) {
        if (idWal == null) return;
        try {
            String causa = (motivo != null && !motivo.isBlank()) ? motivo : (error != null ? error.getMessage() : "Error desconocido");
            entityManager.createNativeQuery(
                    "UPDATE Wal_Transaccion_Pago " +
                    "SET estado = 'FALLO_CONCURRENCIA_REQUIERE_ACCION', " +
                    "    motivo_fallo = :causa, " +
                    "    fecha_actualizacion = NOW() " +
                    "WHERE id_wal = :idWal"
            )
            .setParameter("causa", causa.length() > 500 ? causa.substring(0, 500) : causa)
            .setParameter("idWal", idWal)
            .executeUpdate();
            Logger.logError(String.format("🚨 [WAL-DEDICADO] WAL ID #%d marcado como FALLIDO para acción/reconstrucción. Causa: %s", idWal, causa));
        } catch (Exception e) {
            Logger.logError("❌ [WAL-DEDICADO] Error al marcar fallo en WAL ID #" + idWal + ": " + e.getMessage());
        }
    }

    /**
     * Registra una solicitud formal de reembolso en la tabla dedicada de WAL.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarSolicitudReembolso(Long idWal, String motivo) {
        if (idWal == null) return;
        try {
            entityManager.createNativeQuery(
                    "UPDATE Wal_Transaccion_Pago " +
                    "SET estado = 'REEMBOLSO_SOLICITADO', " +
                    "    motivo_fallo = COALESCE(motivo_fallo, '') || ' | REEMBOLSO_CLIENTE: ' || :motivo, " +
                    "    fecha_actualizacion = NOW() " +
                    "WHERE id_wal = :idWal"
            )
            .setParameter("motivo", motivo != null ? motivo : "Solicitud voluntaria del cliente")
            .setParameter("idWal", idWal)
            .executeUpdate();
            Logger.logInfo(String.format("💸 [WAL-DEDICADO] Solicitud de reembolso registrada para WAL ID #%d", idWal));
        } catch (Exception e) {
            Logger.logError("❌ [WAL-DEDICADO] Error al registrar solicitud de reembolso en WAL ID #" + idWal + ": " + e.getMessage());
        }
    }

    /**
     * Obtiene el monto capturado de la transacción WAL.
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> obtenerMontoPorWalId(Long idWal) {
        if (idWal == null) return Optional.empty();
        try {
            Object result = entityManager.createNativeQuery(
                    "SELECT monto FROM Wal_Transaccion_Pago WHERE id_wal = :idWal"
            )
            .setParameter("idWal", idWal)
            .getSingleResult();
            if (result instanceof Number) {
                return Optional.of(BigDecimal.valueOf(((Number) result).doubleValue()));
            }
        } catch (Exception ignored) {}
        return Optional.empty();
    }

    /**
     * Recupera el DTO serializado desde el WAL para reconstruir la reserva íntegramente.
     */
    @Transactional(readOnly = true)
    public Optional<SolicitudReservaDTO> recuperarPayload(Long idWal) {
        if (idWal == null) return Optional.empty();
        try {
            Object jsonRaw = entityManager.createNativeQuery(
                    "SELECT payload_snapshot::text FROM Wal_Transaccion_Pago WHERE id_wal = :idWal"
            )
            .setParameter("idWal", idWal)
            .getSingleResult();

            if (jsonRaw != null) {
                WalPayloadSnapshot snapshot = objectMapper.readValue(jsonRaw.toString(), WalPayloadSnapshot.class);
                return Optional.of(reconstruirSolicitudDTO(snapshot));
            }
        } catch (Exception e) {
            Logger.logError("❌ [WAL-DEDICADO] Error al deserializar payload desde WAL ID #" + idWal + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    private WalPayloadSnapshot construirSnapshot(SolicitudReservaDTO dto, Integer idMetodoPago, String token, String rutTitular, String emailContacto) {
        List<WalPasajeroSnapshot> listaPasajeros = new ArrayList<>();
        if (dto.pasajeros() != null) {
            for (Pasajero p : dto.pasajeros()) {
                if (p == null) continue;
                Usuario u = p.getUsuario();
                listaPasajeros.add(new WalPasajeroSnapshot(
                        p.getRut(),
                        u != null ? u.getNombre() : null,
                        u != null ? u.getApellido() : null,
                        u != null ? u.getCorreoElectronico() : null,
                        u != null ? u.getTelefono() : null,
                        p.getTipoDocumento(),
                        p.getNumeroDocumento(),
                        p.getNacionalidad(),
                        p.getFechaNacimiento() != null ? p.getFechaNacimiento() : (u != null ? u.getFechaNacimiento() : null)
                ));
            }
        }

        return new WalPayloadSnapshot(
                rutTitular,
                emailContacto,
                listaPasajeros,
                dto.asientosSeleccionados(),
                dto.tarifasItinerarios(),
                dto.equipajePorPasajero(),
                dto.montoTotal(),
                dto.sessionId(),
                token,
                idMetodoPago,
                new Date()
        );
    }

    public SolicitudReservaDTO reconstruirSolicitudDTO(WalPayloadSnapshot snapshot) {
        List<Pasajero> pasajeros = new ArrayList<>();
        if (snapshot.pasajeros() != null) {
            for (WalPasajeroSnapshot s : snapshot.pasajeros()) {
                Pasajero p = new Pasajero();
                p.setRut(s.rut());
                p.setTipoDocumento(s.tipoDocumento());
                p.setNumeroDocumento(s.numeroDocumento());
                p.setNacionalidad(s.nacionalidad());
                p.setFechaNacimiento(s.fechaNacimiento());

                Usuario u = new Usuario();
                u.setRut(s.rut());
                u.setNombre(s.nombre());
                u.setApellido(s.apellido());
                u.setCorreoElectronico(s.correoElectronico());
                u.setTelefono(s.telefono());
                u.setDocumentoIdentidad(s.numeroDocumento());
                u.setFechaNacimiento(s.fechaNacimiento());
                p.setUsuario(u);

                pasajeros.add(p);
            }
        }

        return new SolicitudReservaDTO(
                snapshot.rutTitular(),
                pasajeros,
                snapshot.asientosSeleccionados(),
                snapshot.tarifasItinerarios(),
                snapshot.equipajePorPasajero(),
                snapshot.montoTotal(),
                snapshot.sessionId()
        );
    }
}
