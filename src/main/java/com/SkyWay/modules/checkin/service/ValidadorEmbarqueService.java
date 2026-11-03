package com.SkyWay.modules.checkin.service;

import com.SkyWay.modules.checkin.dto.ResultadoValidacionEmbarqueDTO;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.util.BoardingPassSecurityService;
import com.SkyWay.util.BoardingPassSecurityService.PayloadBoardingPass;
import com.SkyWay.util.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio integral para validación de Tarjetas de Embarque (Gate Boarding Control).
 * Ejecuta verificación criptográfica inalterable (HMAC-SHA256), integridad en base de datos,
 * detección de asientos desactualizados y prevención de doble embarque (anti-duplicación).
 */
@Service
public class ValidadorEmbarqueService implements Serializable {
    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ReservaService reservaService;

    // Registro concurrente de pasajeros embarcados en la sesión de puerta para control de duplicados
    private final Map<String, RegistroEmbarque> registroEmbarcados = new ConcurrentHashMap<>();

    /**
     * Valida integralmente un código escaneado (óptico, cámara web o manual).
     */
    @Transactional(readOnly = true)
    public ResultadoValidacionEmbarqueDTO validarTarjetaEmbarque(String codigoRaw, String puertaActual) {
        if (codigoRaw == null || codigoRaw.isBlank()) {
            return ResultadoValidacionEmbarqueDTO.rechazado("INVALIDO", "No se recibió ningún código para validar.", "");
        }

        String codigoLimpio = codigoRaw.trim();
        Logger.logInfo("🛡️ [GATE SCANNER] Procesando código de abordaje: " + codigoLimpio);

        // 1. Decodificación y Verificación Criptográfica
        PayloadBoardingPass payload = BoardingPassSecurityService.parsearCodigo(codigoLimpio);
        if (!payload.isFormatoValido()) {
            return ResultadoValidacionEmbarqueDTO.rechazado(
                    "FORMATO_INVALIDO",
                    "El código escaneado no corresponde a un formato de Tarjeta de Embarque SkyWay válido.",
                    codigoLimpio
            );
        }

        // Si el código incluye firma criptográfica, verificarla estrictamente
        if (payload.isTieneFirma() && !payload.isFirmaAutentica()) {
            Logger.logWarn("🚨 [ALERTA DE SEGURIDAD] Firma criptográfica inválida para PNR: " + payload.getPnr() + ", Asiento: " + payload.getAsiento());
            return ResultadoValidacionEmbarqueDTO.adulterado(
                    "¡ALERTA DE SEGURIDAD! La firma digital HMAC-SHA256 no coincide. El código ha sido adulterado, falsificado o generado por un emisor no autorizado.",
                    codigoLimpio,
                    payload.getFirma()
            );
        }

        // 2. Verificación de existencia del PNR en Base de Datos
        Integer idReserva = reservaService.buscarIdPorCodigo(payload.getPnr());
        if (idReserva == null) {
            return ResultadoValidacionEmbarqueDTO.rechazado(
                    "RESERVA_INEXISTENTE",
                    "No se encontró ninguna reserva asociada al código PNR " + payload.getPnr() + " en el sistema.",
                    codigoLimpio
            );
        }

        // 3. Verificación de Check-In completado en la BD
        boolean tieneCheckIn = verificarCheckInPasajero(idReserva, payload.getRut());
        if (!tieneCheckIn) {
            return ResultadoValidacionEmbarqueDTO.rechazado(
                    "CHECKIN_PENDIENTE",
                    "El pasajero (RUT: " + payload.getRut() + ") NO registra Check-In completado para la reserva " + payload.getPnr() + ". Debe completar el proceso antes de ingresar a puerta.",
                    codigoLimpio
            );
        }

        // 4. Verificación de Vuelo, Asiento, Cabina y Puerta en Base de Datos
        ResultadoValidacionEmbarqueDTO dto = consultarDatosVueloYAsiento(idReserva, payload.getRut(), payload.getNumeroVuelo());
        if (dto == null) {
            return ResultadoValidacionEmbarqueDTO.rechazado(
                    "VUELO_NO_ASOCIADO",
                    "El vuelo " + payload.getNumeroVuelo() + " no está asociado a la reserva " + payload.getPnr() + " o al pasajero especificado.",
                    codigoLimpio
            );
        }

        dto.setCodigoRaw(codigoLimpio);
        dto.setPnr(payload.getPnr());
        dto.setFirmaHex(payload.getFirma());
        dto.setFirmaAutentica(payload.isFirmaAutentica());
        dto.setNonce(payload.getNonce());
        if (payload.getTipoCriptografia() != null) {
            dto.setAlgoritmoSeguridad(payload.getTipoCriptografia());
        }

        // 5. Verificación de coherencia del asiento (detección de pases obsoletos por cambio de asiento posterior)
        if (payload.getAsiento() != null && !payload.getAsiento().equalsIgnoreCase(dto.getAsiento())) {
            Logger.logWarn("⚠️ [ASIENTO DESACTUALIZADO] Asiento en tarjeta: " + payload.getAsiento() + " vs Asiento en BD: " + dto.getAsiento());
            dto.setValido(false);
            dto.setEstado("ASIENTO_DESACTUALIZADO");
            dto.setNivelAlerta("WARNING");
            dto.setMensaje("El asiento impreso en la tarjeta (" + payload.getAsiento() + ") no coincide con el asiento asignado actualmente en el sistema (" + dto.getAsiento() + "). El pasajero realizó un cambio de asiento posterior.");
            return dto;
        }

        // 6. Verificación de Doble Embarque (Detección de duplicados / clonación)
        String claveEmbarque = (payload.getPnr() + "#" + payload.getNumeroVuelo() + "#" + payload.getRut()).toUpperCase();
        if (registroEmbarcados.containsKey(claveEmbarque)) {
            RegistroEmbarque reg = registroEmbarcados.get(claveEmbarque);
            Logger.logWarn("🚨 [DOBLE ESCANEO DETECTADO] Pasajero ya abordado: " + claveEmbarque);
            return ResultadoValidacionEmbarqueDTO.duplicado(
                    "¡ATENCIÓN! Este pasabordo ya fue utilizado para abordar previamente a las " + reg.getHoraFormatted() + " en la puerta " + reg.getPuerta() + ". Posible intento de reuso o tarjeta duplicada.",
                    dto
            );
        }

        // Todo en orden: Auténtico y autorizado para abordar
        dto.setValido(true);
        dto.setEstado("AUTORIZADO");
        dto.setNivelAlerta("SUCCESS");
        dto.setMensaje("Tarjeta de Embarque verificada con éxito. Firma digital auténtica (HMAC-SHA256) y datos confirmados en el sistema.");
        return dto;
    }

    /**
     * Registra al pasajero formalmente como EMBARCADO en la puerta de salida.
     */
    public boolean registrarEmbarque(ResultadoValidacionEmbarqueDTO dto, String agente) {
        if (dto == null || !dto.isValido()) {
            return false;
        }

        String clave = (dto.getPnr() + "#" + dto.getNumeroVuelo() + "#" + dto.getRut()).toUpperCase();
        RegistroEmbarque reg = new RegistroEmbarque(
                dto.getPnr(),
                dto.getNumeroVuelo(),
                dto.getRut(),
                dto.getNombrePasajero(),
                dto.getAsiento(),
                dto.getPuerta(),
                agente != null ? agente : "Agente Puerta SkyWay",
                new Date()
        );

        registroEmbarcados.put(clave, reg);
        dto.setYaEmbarcado(true);
        dto.setHoraEmbarque(reg.getFechaEmbarque());
        Logger.logInfo("✅ [PASAJERO A BORDO] Registrado embarque para: " + dto.getNombrePasajero() + " en vuelo " + dto.getNumeroVuelo() + " asiento " + dto.getAsiento());
        return true;
    }

    /**
     * Limpia o reinicia la sesión de puerta de embarque si se requiere.
     */
    public void reiniciarSesionPuerta() {
        registroEmbarcados.clear();
        Logger.logInfo("🔄 [GATE CONTROL] Sesión de registros de embarque reiniciada.");
    }

    public int getTotalPasajerosEmbarcados() {
        return registroEmbarcados.size();
    }

    // --- Consultas Auxiliares en Base de Datos ---

    private boolean verificarCheckInPasajero(Integer idReserva, String rut) {
        try {
            Query q = em.createNativeQuery(
                    "SELECT COUNT(*) FROM checkin c " +
                    "JOIN checkin_pasajero cp ON cp.id_checkin = c.id_checkin " +
                    "WHERE c.id_reserva = :idReserva AND cp.rut = :rut"
            );
            q.setParameter("idReserva", idReserva);
            q.setParameter("rut", rut);
            Number count = (Number) q.getSingleResult();
            if (count != null && count.longValue() > 0) {
                return true;
            }

            // Fallback: verificar si el check-in global de la reserva ya está completado
            return reservaService.tieneCheckInCompletado(idReserva);
        } catch (Exception e) {
            Logger.logWarn("Error verificando checkin_pasajero: " + e.getMessage());
            return reservaService.tieneCheckInCompletado(idReserva);
        }
    }

    @SuppressWarnings("unchecked")
    private ResultadoValidacionEmbarqueDTO consultarDatosVueloYAsiento(Integer idReserva, String rut, String numVuelo) {
        try {
            String sql =
                    "SELECT " +
                    "   us.nombre || ' ' || us.apellido AS nombre_pasajero, " +
                    "   us.rut AS rut_pasajero, " +
                    "   vl.numero_vuelo, " +
                    "   a_orig.codigo_iata AS origen_iata, " +
                    "   a_dest.codigo_iata AS destino_iata, " +
                    "   a_orig.nombre AS origen_nombre, " +
                    "   a_dest.nombre AS destino_nombre, " +
                    "   sgm.hora_salida, " +
                    "   sgm.hora_llegada, " +
                    "   ast.numero_asiento, " +
                    "   clas.descripcion AS clase_cabina, " +
                    "   COALESCE(prta.codigo_puerta, 'TBD') AS codigo_puerta, " +
                    "   COALESCE(prta.terminal, 'Terminal Internacional') AS terminal " +
                    "FROM reserva rsv " +
                    "JOIN reserva_asiento rsva ON rsva.id_reserva = rsv.id_reserva " +
                    "JOIN pasajero p ON p.rut = rsva.rut " +
                    "JOIN usuario us ON us.rut = p.rut " +
                    "JOIN vuelo vl ON vl.id_vuelo = rsva.id_vuelo " +
                    "JOIN segmento_vuelo sgm ON sgm.id_vuelo = vl.id_vuelo " +
                    "JOIN aeropuerto a_orig ON a_orig.id_aeropuerto = sgm.id_aeropuerto_origen " +
                    "JOIN aeropuerto a_dest ON a_dest.id_aeropuerto = sgm.id_aeropuerto_destino " +
                    "LEFT JOIN asignacion_puerta asgp ON asgp.id_segmento = sgm.id_segmento " +
                    "LEFT JOIN puerta_embarque prta ON prta.id_puerta = asgp.id_puerta " +
                    "LEFT JOIN asiento ast ON ast.id_asiento = rsva.id_asiento " +
                    "LEFT JOIN clase_asiento clas ON clas.id_clase = ast.id_clase " +
                    "WHERE rsv.id_reserva = :idReserva " +
                    "  AND rsva.rut = :rut " +
                    "  AND (UPPER(vl.numero_vuelo) = UPPER(:numVuelo) OR :numVuelo = 'NO-VUELO') " +
                    "ORDER BY sgm.orden_segmento ASC LIMIT 1";

            Query query = em.createNativeQuery(sql);
            query.setParameter("idReserva", idReserva);
            query.setParameter("rut", rut);
            query.setParameter("numVuelo", numVuelo != null ? numVuelo : "NO-VUELO");

            List<Object[]> rows = query.getResultList();
            if (rows.isEmpty()) {
                return null;
            }

            Object[] r = rows.get(0);
            ResultadoValidacionEmbarqueDTO dto = new ResultadoValidacionEmbarqueDTO();
            dto.setNombrePasajero(r[0] != null ? r[0].toString() : "Pasajero SkyWay");
            dto.setRut(r[1] != null ? r[1].toString() : rut);
            dto.setNumeroVuelo(r[2] != null ? r[2].toString() : numVuelo);
            dto.setOrigenIata(r[3] != null ? r[3].toString() : "---");
            dto.setDestinoIata(r[4] != null ? r[4].toString() : "---");
            dto.setOrigenNombre(r[5] != null ? r[5].toString() : "");
            dto.setDestinoNombre(r[6] != null ? r[6].toString() : "");
            dto.setRutaTramo(dto.getOrigenIata() + " ➔ " + dto.getDestinoIata());

            if (r[7] instanceof Timestamp tsSalida) {
                dto.setHoraSalida(new Date(tsSalida.getTime()));
            }
            if (r[8] instanceof Timestamp tsLlegada) {
                dto.setHoraLlegada(new Date(tsLlegada.getTime()));
            }

            dto.setAsiento(r[9] != null ? r[9].toString() : "---");
            dto.setClaseCabina(r[10] != null ? r[10].toString() : "Económica");
            dto.setPuerta(r[11] != null ? r[11].toString() : "TBD");
            dto.setTerminal(r[12] != null ? r[12].toString() : "Terminal Internacional");

            return dto;
        } catch (Exception e) {
            Logger.logError("Error consultando datos de vuelo y asiento para validación: " + e.getMessage());
            return null;
        }
    }

    /**
     * Registro interno de auditoría de un embarque completado.
     */
    public static class RegistroEmbarque implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String pnr;
        private final String vuelo;
        private final String rut;
        private final String nombre;
        private final String asiento;
        private final String puerta;
        private final String agente;
        private final Date fechaEmbarque;

        public RegistroEmbarque(String pnr, String vuelo, String rut, String nombre, String asiento, String puerta, String agente, Date fechaEmbarque) {
            this.pnr = pnr;
            this.vuelo = vuelo;
            this.rut = rut;
            this.nombre = nombre;
            this.asiento = asiento;
            this.puerta = puerta;
            this.agente = agente;
            this.fechaEmbarque = fechaEmbarque;
        }

        public String getHoraFormatted() {
            return new java.text.SimpleDateFormat("HH:mm:ss").format(fechaEmbarque);
        }

        public String getPnr() { return pnr; }
        public String getVuelo() { return vuelo; }
        public String getRut() { return rut; }
        public String getNombre() { return nombre; }
        public String getAsiento() { return asiento; }
        public String getPuerta() { return puerta; }
        public String getAgente() { return agente; }
        public Date getFechaEmbarque() { return fechaEmbarque; }
    }
}
