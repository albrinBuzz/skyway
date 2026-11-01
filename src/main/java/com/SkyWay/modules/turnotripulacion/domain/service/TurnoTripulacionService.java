package com.SkyWay.modules.turnotripulacion.domain.service;

import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turnotripulacion.domain.model.TurnoTripulacion;
import java.sql.Timestamp;
import com.SkyWay.modules.turnotripulacion.domain.repository.TurnoTripulacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TurnoTripulacionService {

    @Autowired
    private TurnoTripulacionRepository turnoTripulacionRepository;

    @Autowired
    private com.SkyWay.modules.vuelo.domain.repository.VueloRepository vueloRepository;

    // Save or update a TurnoTripulacion
    public TurnoTripulacion save(TurnoTripulacion turnoTripulacion) {
        return turnoTripulacionRepository.save(turnoTripulacion);
    }

    // Find all TurnoTripulacion records
    public List<TurnoTripulacion> findAll() {
        return turnoTripulacionRepository.findAll();
    }

    // Find a TurnoTripulacion by id
    public Optional<TurnoTripulacion> findById(Integer idTurnoTripulacion) {
        return turnoTripulacionRepository.findById(idTurnoTripulacion);
    }

    // Find TurnoTripulacion by Tripulacion's rut
    public List<TurnoTripulacion> findByTripulacionRut(String rut) {
        return turnoTripulacionRepository.findByTripulacion1Rut(rut);
    }

    // Find TurnoTripulacion by Turno's id
    public List<TurnoTripulacion> findByTurnoId(Integer idTurno) {
        return turnoTripulacionRepository.findByTurno1IdTurno(idTurno);
    }

    // Delete a TurnoTripulacion by id
    public void delete(Integer idTurnoTripulacion) {
        turnoTripulacionRepository.deleteById(idTurnoTripulacion);
    }

    // Validar asignación de tripulación: horas legales, solapamiento y descanso mínimo
    public void validarAsignacion(Tripulacion tripulante, Turno turno) {
        if (tripulante == null) {
            throw new IllegalArgumentException("El tripulante no puede ser nulo.");
        }
        if (turno == null) {
            throw new IllegalArgumentException("El turno no puede ser nulo.");
        }

        // 1. Validar horas legales del turno
        if (turno.excedeHorasLegales()) {
            throw new IllegalStateException(String.format(
                    "El turno excede el límite operacional máximo permitido de 24 horas continuas (duración calculada: %.1f hrs).",
                    turno.getDuracionHoras()));
        }

        if (turno.getHoraInicio() == null || turno.getHoraFin() == null) {
            return;
        }

        String rut = tripulante.getRut();
        if (rut == null && tripulante.getUsuario() != null) {
            rut = tripulante.getUsuario().getRut();
        }
        if (rut == null || rut.trim().isEmpty()) {
            return;
        }

        // 2. Validar solapamiento de horarios y descanso obligatorio (8 hrs) en TurnoTripulacion
        List<TurnoTripulacion> previas = findByTripulacionRut(rut);
        if (previas != null) {
            for (TurnoTripulacion asignacion : previas) {
                if (asignacion.getEstado() != null && asignacion.getEstado().equalsIgnoreCase("RECHAZADO")) {
                    continue;
                }

                Turno tOtro = asignacion.getTurno1();
                if (tOtro == null || (turno.getIdTurno() != null && turno.getIdTurno().equals(tOtro.getIdTurno()))) {
                    continue;
                }

                if (tOtro.getHoraInicio() != null && tOtro.getHoraFin() != null) {
                    // Solapamiento: inicio < fin2 && fin > inicio2
                    if (turno.getHoraInicio().before(tOtro.getHoraFin()) && turno.getHoraFin().after(tOtro.getHoraInicio())) {
                        String nroVuelo = (tOtro.getVuelo() != null && tOtro.getVuelo().getNumeroVuelo() != null)
                                ? tOtro.getVuelo().getNumeroVuelo() : "Turno #" + tOtro.getIdTurno();
                        throw new IllegalStateException(String.format(
                                "Solapamiento de horario: El tripulante ya se encuentra asignado al servicio %s en el rango solicitado.",
                                nroVuelo));
                    }

                    // Descanso mínimo de 8 horas
                    if (!turno.getHoraInicio().before(tOtro.getHoraFin())) {
                        long diffMs = turno.getHoraInicio().getTime() - tOtro.getHoraFin().getTime();
                        double horasDescanso = diffMs / (1000.0 * 3600.0);
                        if (horasDescanso < 8.0) {
                            throw new IllegalStateException(String.format(
                                    "Descanso legal insuficiente: El tripulante finaliza un servicio previo con solo %.1f hrs de descanso (mínimo legal requerido: 8 hrs).",
                                    horasDescanso));
                        }
                    } else if (!tOtro.getHoraInicio().before(turno.getHoraFin())) {
                        long diffMs = tOtro.getHoraInicio().getTime() - turno.getHoraFin().getTime();
                        double horasDescanso = diffMs / (1000.0 * 3600.0);
                        if (horasDescanso < 8.0) {
                            throw new IllegalStateException(String.format(
                                    "Descanso legal insuficiente: El tripulante tiene programado un servicio posterior con solo %.1f hrs de descanso (mínimo legal requerido: 8 hrs).",
                                    horasDescanso));
                        }
                    }
                }
            }
        }

        // 3. Validar vuelos donde este tripulante figure directamente como Piloto Comandante asignado
        if (vueloRepository != null) {
            List<com.SkyWay.modules.vuelo.domain.model.Vuelo> vuelos = vueloRepository.findAll();
            if (vuelos != null) {
                for (com.SkyWay.modules.vuelo.domain.model.Vuelo vOtro : vuelos) {
                    if (vOtro == null || (turno.getVuelo() != null && turno.getVuelo().getIdVuelo() != null && turno.getVuelo().getIdVuelo().equals(vOtro.getIdVuelo()))) {
                        continue;
                    }
                    if (vOtro.getPiloto() != null) {
                        String rutPiloto = (vOtro.getPiloto().getUsuario() != null)
                                ? vOtro.getPiloto().getUsuario().getRut() : vOtro.getPiloto().getRut();
                        if (rut.equalsIgnoreCase(rutPiloto)) {
                            Timestamp sal = (vOtro.getFechaHoraSalida() != null) ? vOtro.getFechaHoraSalida()
                                    : ((vOtro.getSegmentoVuelos() != null && !vOtro.getSegmentoVuelos().isEmpty())
                                            ? vOtro.getSegmentoVuelos().get(0).getHoraSalida() : null);
                            Timestamp lleg = (vOtro.getFechaHoraLlegada() != null) ? vOtro.getFechaHoraLlegada()
                                    : ((vOtro.getSegmentoVuelos() != null && !vOtro.getSegmentoVuelos().isEmpty())
                                            ? vOtro.getSegmentoVuelos().get(vOtro.getSegmentoVuelos().size() - 1).getHoraLlegada() : null);
                            if (sal != null && lleg != null) {
                                if (turno.getHoraInicio().before(lleg) && turno.getHoraFin().after(sal)) {
                                    String nro = (vOtro.getNumeroVuelo() != null) ? vOtro.getNumeroVuelo() : "SK-" + vOtro.getIdVuelo();
                                    throw new IllegalStateException(String.format(
                                            "Solapamiento de horario: El piloto ya se encuentra asignado como Comandante al vuelo %s.", nro));
                                }
                                if (!turno.getHoraInicio().before(lleg)) {
                                    long diffMs = turno.getHoraInicio().getTime() - lleg.getTime();
                                    double horas = diffMs / (1000.0 * 3600.0);
                                    if (horas < 8.0) {
                                        throw new IllegalStateException(String.format(
                                                "Descanso legal insuficiente: El piloto finaliza como Comandante un vuelo previo con solo %.1f hrs de descanso (mínimo legal: 8 hrs).", horas));
                                    }
                                } else if (!sal.before(turno.getHoraFin())) {
                                    long diffMs = sal.getTime() - turno.getHoraFin().getTime();
                                    double horas = diffMs / (1000.0 * 3600.0);
                                    if (horas < 8.0) {
                                        throw new IllegalStateException(String.format(
                                                "Descanso legal insuficiente: El piloto está programado como Comandante de un vuelo posterior con solo %.1f hrs de descanso (mínimo legal: 8 hrs).", horas));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Verificar si un tripulante está disponible para un turno específico sin lanzar excepciones
    public boolean estaDisponible(Tripulacion tripulante, Turno turno) {
        if (tripulante == null || turno == null) {
            return false;
        }
        try {
            validarAsignacion(tripulante, turno);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Confirmar turno por parte del tripulante
    public TurnoTripulacion confirmarTurno(Integer idTurnoTripulacion) {
        Optional<TurnoTripulacion> opt = findById(idTurnoTripulacion);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la asignación de turno con ID: " + idTurnoTripulacion);
        }
        TurnoTripulacion tt = opt.get();
        tt.setEstado("CONFIRMADO");
        tt.setFechaRespuesta(new Timestamp(System.currentTimeMillis()));
        return save(tt);
    }

    // Rechazar turno con motivo justificado
    public TurnoTripulacion rechazarTurno(Integer idTurnoTripulacion, String motivo) {
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe ingresar un motivo para justificar el rechazo del turno.");
        }
        Optional<TurnoTripulacion> opt = findById(idTurnoTripulacion);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la asignación de turno con ID: " + idTurnoTripulacion);
        }
        TurnoTripulacion tt = opt.get();
        tt.setEstado("RECHAZADO");
        tt.setMotivoRechazo(motivo.trim());
        tt.setFechaRespuesta(new Timestamp(System.currentTimeMillis()));
        return save(tt);
    }
}

