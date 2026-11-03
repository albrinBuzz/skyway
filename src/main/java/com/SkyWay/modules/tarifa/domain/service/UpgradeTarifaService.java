package com.SkyWay.modules.tarifa.domain.service;

import com.SkyWay.modules.TarifaCaracteristica.domain.model.TarifaCaracteristica;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;
import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.reservaitinerario.domain.model.ReservaItinerario;
import com.SkyWay.modules.reservaitinerario.domain.repository.ReservaItinerarioRepository;
import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.presentation.dto.OpcionUpgradeDTO;
import com.SkyWay.modules.tarifa.presentation.dto.ResultadoUpgradeDTO;
import com.SkyWay.modules.itinerariovuelo.domain.model.ItinerarioVuelo;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import com.SkyWay.util.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de negocio integral para el Ascenso de Tarifa (Fare Upsell / Instant Upgrade).
 * Permite a los pasajeros calcular la diferencia de tarifa, pagar la diferencia
 * y acceder de inmediato a beneficios superiores (cambio de asiento, mayor plazo y flexibilidad).
 */
@Service
public class UpgradeTarifaService implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ReservaItinerarioRepository reservaItinerarioRepository;

    @Autowired
    private ItinerarioTarifaRepository itinerarioTarifaRepository;

    @PersistenceContext
    private EntityManager em;

    /**
     * Obtiene las opciones de ascenso tarifario disponibles para una reserva e itinerario específicos.
     * Solo retorna tarifas superiores a la actualmente contratada, calculando la diferencia económica exacta.
     */
    @Transactional(readOnly = true)
    public List<OpcionUpgradeDTO> obtenerOpcionesUpgrade(Integer idReserva, Integer idItinerario) {
        List<OpcionUpgradeDTO> opciones = new ArrayList<>();
        if (idReserva == null || idItinerario == null) {
            return opciones;
        }

        Optional<ReservaItinerario> optRi = reservaItinerarioRepository.findByReserva_IdReservaAndItinerario_IdItinerario(idReserva, idItinerario);
        if (optRi.isEmpty() || optRi.get().getItinerarioTarifa() == null) {
            Logger.logWarn("No se encontró ReservaItinerario para calcular upgrade: Reserva " + idReserva + ", Itinerario " + idItinerario);
            return opciones;
        }

        ReservaItinerario riActual = optRi.get();
        ItinerarioTarifa itActual = riActual.getItinerarioTarifa();
        BigDecimal precioActual = itActual.getPrecio() != null ? itActual.getPrecio() : BigDecimal.ZERO;
        Integer idTarifaActual = itActual.getTarifa() != null ? itActual.getTarifa().getIdTarifa() : 0;

        // Obtener todas las tarifas catalogadas para este itinerario
        List<ItinerarioTarifa> tarifasItinerario = itinerarioTarifaRepository.findByItinerarioIdItinerario(idItinerario);

        for (ItinerarioTarifa itCandidate : tarifasItinerario) {
            if (itCandidate.getTarifa() == null) continue;
            Integer idTarifaCandidate = itCandidate.getTarifa().getIdTarifa();

            // Solo ofrecer tarifas de nivel superior (o con precio superior)
            boolean esSuperior = (idTarifaCandidate > idTarifaActual) ||
                    (itCandidate.getPrecio() != null && itCandidate.getPrecio().compareTo(precioActual) > 0);

            if (esSuperior) {
                BigDecimal precioNuevo = itCandidate.getPrecio() != null ? itCandidate.getPrecio() : BigDecimal.ZERO;
                BigDecimal diferencia = precioNuevo.subtract(precioActual);

                if (diferencia.compareTo(BigDecimal.ZERO) < 0) {
                    diferencia = BigDecimal.ZERO;
                }

                OpcionUpgradeDTO opcion = new OpcionUpgradeDTO(
                        itCandidate.getIdItinerarioTarifa(),
                        idTarifaCandidate,
                        itCandidate.getTarifa().getNombre(),
                        precioNuevo,
                        precioActual,
                        diferencia
                );
                if (itActual.getTarifa() != null) {
                    opcion.setNombreTarifaActual(itActual.getTarifa().getNombre());
                } else {
                    opcion.setNombreTarifaActual("Básica");
                }

                // Mapear características y reglas de la tarifa superior
                Tarifa t = itCandidate.getTarifa();
                if (t.getTarifaCaracteristicas() != null) {
                    opcion.setCaracteristicas(t.getTarifaCaracteristicas());
                    for (TarifaCaracteristica tc : t.getTarifaCaracteristicas()) {
                        if (tc.getCaracteristica() == null || tc.getCaracteristica().getNombre() == null) continue;
                        String nombreCarac = tc.getCaracteristica().getNombre().toLowerCase();
                        Integer idC = tc.getCaracteristica().getIdCaracteristica();

                        if ((idC != null && idC == 2) || nombreCarac.contains("plazo") || nombreCarac.contains("horas")) {
                            if (tc.getValorInt() != null) opcion.setHorasMinimasCambioAsiento(tc.getValorInt());
                        } else if ((idC != null && idC == 1) || (nombreCarac.contains("cambio") && nombreCarac.contains("asiento"))) {
                            if (tc.getValorBool() != null) opcion.setPermiteCambioAsiento(tc.getValorBool());
                        } else if ((idC != null && idC == 3) || nombreCarac.contains("cancelaci")) {
                            if (tc.getValorBool() != null) opcion.setPermiteCancelacion(tc.getValorBool());
                        }
                    }
                }

                // Garantizar consistencia con reglas de negocio si no vinieron explícitas en BD
                if (opcion.getHorasMinimasCambioAsiento() == null) {
                    if (idTarifaCandidate == 2) {
                        opcion.setHorasMinimasCambioAsiento(48);
                    } else if (idTarifaCandidate == 3) {
                        opcion.setHorasMinimasCambioAsiento(24);
                    }
                }
                if (!opcion.isPermiteCambioAsiento() && idTarifaCandidate >= 2) {
                    opcion.setPermiteCambioAsiento(true);
                }

                opciones.add(opcion);
            }
        }

        Logger.logInfo("🎯 [UPGRADE TARIFA] " + opciones.size() + " opciones de ascenso encontradas para Reserva: " + idReserva);
        return opciones;
    }

    /**
     * Procesa de forma atómica y transaccional el ascenso de tarifa:
     * 1. Registra el nuevo pago por la diferencia en la tabla Pago.
     * 2. Actualiza ReservaItinerario al nuevo ItinerarioTarifa.
     * 3. Garantiza consistencia operacional en la base de datos V3.
     */
    @Transactional
    public boolean procesarUpgrade(Integer idReserva, Integer idItinerario, Integer idItinerarioTarifaNuevo,
                                   Integer idMetodoPago, String motivoOAgente) {
        ResultadoUpgradeDTO res = procesarYConfirmarUpgrade(idReserva, idItinerario, idItinerarioTarifaNuevo, idMetodoPago, motivoOAgente);
        return res != null;
    }

    /**
     * Procesa de forma atómica el ascenso de tarifa y construye el DTO completo de retroalimentación
     * para que el pasajero conozca con exactitud qué reserva, vuelo e itinerario fue actualizado,
     * la comparativa de tarifas y los nuevos beneficios adquiridos.
     */
    @Transactional
    public ResultadoUpgradeDTO procesarYConfirmarUpgrade(Integer idReserva, Integer idItinerario, Integer idItinerarioTarifaNuevo,
                                                         Integer idMetodoPago, String motivoOAgente) {
        if (idReserva == null || idItinerario == null || idItinerarioTarifaNuevo == null) {
            Logger.logError("Parámetros insuficientes para procesar upgrade de tarifa.");
            return null;
        }

        Optional<ReservaItinerario> optRi = reservaItinerarioRepository.findByReserva_IdReservaAndItinerario_IdItinerario(idReserva, idItinerario);
        if (optRi.isEmpty()) {
            Logger.logError("No se encontró ReservaItinerario para ejecutar upgrade.");
            return null;
        }

        ReservaItinerario ri = optRi.get();
        ItinerarioTarifa tarifaPrevia = ri.getItinerarioTarifa();
        Optional<ItinerarioTarifa> optTarifaNueva = itinerarioTarifaRepository.findById(idItinerarioTarifaNuevo);

        if (optTarifaNueva.isEmpty()) {
            Logger.logError("No existe el ItinerarioTarifa destino (ID: " + idItinerarioTarifaNuevo + ").");
            return null;
        }

        ItinerarioTarifa tarifaNueva = optTarifaNueva.get();

        // 1. Calcular la diferencia tarifaria real
        BigDecimal precioPrevio = (tarifaPrevia != null && tarifaPrevia.getPrecio() != null) ? tarifaPrevia.getPrecio() : BigDecimal.ZERO;
        BigDecimal precioNuevo = tarifaNueva.getPrecio() != null ? tarifaNueva.getPrecio() : BigDecimal.ZERO;
        BigDecimal diferenciaAPagar = precioNuevo.subtract(precioPrevio);

        if (diferenciaAPagar.compareTo(BigDecimal.ZERO) < 0) {
            diferenciaAPagar = BigDecimal.ZERO;
        }

        // 2. Registrar el pago de la diferencia si es mayor a cero
        if (diferenciaAPagar.compareTo(BigDecimal.ZERO) > 0) {
            Pago pago = new Pago();
            pago.setFecha(new Timestamp(System.currentTimeMillis()));
            pago.setMontoPagado(diferenciaAPagar);
            pago.setReserva(ri.getReserva());

            // Resolver método de pago (Default 1: Webpay, 2: PayPal)
            int metodoId = (idMetodoPago != null && idMetodoPago > 0) ? idMetodoPago : 1;
            MetodoPago mp = em.find(MetodoPago.class, metodoId);
            pago.setMetodoPago(mp);

            em.persist(pago);
            Logger.logInfo("💳 [PAGO UPGRADE] Registrado pago por diferencia de " + diferenciaAPagar + " CLP en tabla Pago (ID Reserva: " + idReserva + ")");
        }

        // 3. Actualizar la tarifa contratada en Reserva_Itinerario
        ri.setItinerarioTarifa(tarifaNueva);
        reservaItinerarioRepository.save(ri);

        // 4. Armar DTO de resultado y retroalimentación para el pasajero
        ResultadoUpgradeDTO res = new ResultadoUpgradeDTO();
        res.setIdReserva(idReserva);
        res.setIdItinerario(idItinerario);
        res.setMontoDiferencia(diferenciaAPagar);
        res.setMetodoPago(motivoOAgente != null ? motivoOAgente : "Pasarela de Pago");

        String nombreTarifaPrevia = (tarifaPrevia != null && tarifaPrevia.getTarifa() != null) ? tarifaPrevia.getTarifa().getNombre() : "Básica";
        String nombreTarifaNueva = (tarifaNueva.getTarifa() != null) ? tarifaNueva.getTarifa().getNombre() : "Superior";
        res.setTarifaAnterior(nombreTarifaPrevia);
        res.setTarifaNueva(nombreTarifaNueva);

        if (ri.getReserva() != null) {
            res.setCodigoPnr(ri.getReserva().getCodigoReserva() != null ? ri.getReserva().getCodigoReserva().toUpperCase() : "RES-" + idReserva);
            if (ri.getReserva().getPasajero() != null && ri.getReserva().getPasajero().getUsuario() != null) {
                String nom = ri.getReserva().getPasajero().getUsuario().getNombre();
                String ape = ri.getReserva().getPasajero().getUsuario().getApellido();
                res.setNombrePasajero(((nom != null ? nom : "") + " " + (ape != null ? ape : "")).trim());
            }
        }

        if (ri.getItinerario() != null) {
            String orig = (ri.getItinerario().getAeropuertoOrigen() != null && ri.getItinerario().getAeropuertoOrigen().getCodigoIata() != null)
                    ? ri.getItinerario().getAeropuertoOrigen().getCodigoIata() : "Origen";
            String dest = (ri.getItinerario().getAeropuertoDestino() != null && ri.getItinerario().getAeropuertoDestino().getCodigoIata() != null)
                    ? ri.getItinerario().getAeropuertoDestino().getCodigoIata() : "Destino";
            res.setOrigenRuta(orig);
            res.setDestinoRuta(dest);
            res.setRutaCompleta(orig + " ➔ " + dest);

            if (ri.getItinerario().getHoraSalida() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                res.setFechaHoraSalida(sdf.format(ri.getItinerario().getHoraSalida()));
            }

            if (ri.getItinerario().getItinerarioVuelos() != null && !ri.getItinerario().getItinerarioVuelos().isEmpty()) {
                for (ItinerarioVuelo iv : ri.getItinerario().getItinerarioVuelos()) {
                    if (iv.getVuelo() != null && iv.getVuelo().getNumeroVuelo() != null) {
                        res.setNumeroVuelo(iv.getVuelo().getNumeroVuelo());
                        break;
                    }
                }
            }
        }

        if (res.getNumeroVuelo() == null || res.getNumeroVuelo().isBlank()) {
            res.setNumeroVuelo(res.getRutaCompleta() != null ? res.getRutaCompleta().replace(" ➔ ", "-") : "Vuelo SkyWay");
        }

        // 5. Determinar beneficios específicos activados
        List<String> beneficios = new ArrayList<>();
        if ("Premium".equalsIgnoreCase(nombreTarifaNueva)) {
            res.setPermiteCambioAsiento(true);
            beneficios.add("Selección y cambio de asiento garantizado en cualquier momento");
            beneficios.add("Embarque prioritario preferente en puerta");
            beneficios.add("Equipaje de mano (10 kg) + Equipaje de bodega (23 kg) incluido");
            beneficios.add("Cancelación y reprogramación autorizada");
        } else {
            // Flexible
            res.setPermiteCambioAsiento(true);
            beneficios.add("Selección y cambio de asiento habilitado en mapa de cabina");
            beneficios.add("Equipaje de mano (10 kg) incluido en compartimento superior");
            beneficios.add("Modificaciones de vuelo según ventana de tarifa");
        }
        res.setBeneficiosNuevos(beneficios);

        Logger.logInfo("🚀 [UPGRADE CONFIRMADO] Reserva Nº " + idReserva + " ascendida exitosamente de '" +
                nombreTarifaPrevia + "' a '" + nombreTarifaNueva + "' (" + motivoOAgente + ")");

        return res;
    }
}
