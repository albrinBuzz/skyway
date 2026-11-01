package com.SkyWay.modules.turno.domain.service;

import com.SkyWay.modules.turno.domain.model.Turno;
import com.SkyWay.modules.turno.domain.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.sql.Date;
import java.sql.Timestamp;
import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;
import com.SkyWay.modules.tipoturno.domain.service.TipoTurnoService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;

@Service
public class TurnoService {

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private TipoTurnoService tipoTurnoService;

    // Save or update a Turno
    public Turno save(Turno turno) {
        return turnoRepository.save(turno);
    }

    // Find all Turnos
    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    // Find a Turno by ID
    public Optional<Turno> findById(Integer idTurno) {
        return turnoRepository.findByIdTurno(idTurno);
    }

    // Find Turnos by Vuelo's ID
    public List<Turno> findByVueloId(Integer idVuelo) {
        return turnoRepository.findByVueloIdVuelo(idVuelo);
    }

    // Find Turnos by TipoTurno's ID
    /*public List<Turno> findByTipoTurnoId(Integer idTipoTurno) {
        return turnoRepository.findByTipoTurnoIdTipoTurno(idTipoTurno);
    }*/

    // Obtener turno existente o crear automáticamente según los horarios del vuelo
    public Turno obtenerOCrearTurnoParaVuelo(Vuelo vuelo) {
        if (vuelo == null || vuelo.getIdVuelo() == null) {
            return null;
        }

        List<Turno> turnos = findByVueloId(vuelo.getIdVuelo());
        if (turnos != null && !turnos.isEmpty()) {
            return turnos.get(0);
        }

        Turno nuevoTurno = new Turno();
        nuevoTurno.setVuelo(vuelo);

        Timestamp inicio = null;
        Timestamp fin = null;

        if (vuelo.getSegmentoVuelos() != null && !vuelo.getSegmentoVuelos().isEmpty()) {
            SegmentoVuelo primerSeg = vuelo.getSegmentoVuelos().get(0);
            SegmentoVuelo ultimoSeg = vuelo.getSegmentoVuelos().get(vuelo.getSegmentoVuelos().size() - 1);

            if (primerSeg.getHoraSalida() != null) {
                inicio = primerSeg.getHoraSalida();
            }
            if (ultimoSeg.getHoraLlegada() != null) {
                fin = ultimoSeg.getHoraLlegada();
            }
        }

        if (inicio == null && vuelo.getFechaHoraSalida() != null) {
            inicio = vuelo.getFechaHoraSalida();
        }
        if (fin == null && vuelo.getFechaHoraLlegada() != null) {
            fin = vuelo.getFechaHoraLlegada();
        }

        if (inicio == null) {
            inicio = new Timestamp(System.currentTimeMillis());
        }
        if (fin == null) {
            fin = new Timestamp(inicio.getTime() + (4 * 3600 * 1000L));
        }

        nuevoTurno.setHoraInicio(inicio);
        nuevoTurno.setHoraFin(fin);
        nuevoTurno.setFecha(new Date(inicio.getTime()));

        // Asignar tipo de turno (Diurno / Nocturno o Vuelo según catálogo)
        List<TipoTurno> tipos = tipoTurnoService.findAll();
        if (tipos != null && !tipos.isEmpty()) {
            int horaSalida = inicio.toLocalDateTime().getHour();
            boolean esDiurno = horaSalida >= 6 && horaSalida < 22;

            TipoTurno asignado = tipos.stream()
                    .filter(t -> esDiurno
                            ? t.getNombre().equalsIgnoreCase("Diurno") || t.getNombre().equalsIgnoreCase("Vuelo")
                            : t.getNombre().equalsIgnoreCase("Nocturno") || t.getNombre().equalsIgnoreCase("Vuelo"))
                    .findFirst()
                    .orElse(tipos.get(0));

            nuevoTurno.setTipoTurno(asignado);
        }

        return turnoRepository.save(nuevoTurno);
    }

    // Delete a Turno by ID
    public void delete(Integer idTurno) {
        turnoRepository.deleteById(idTurno);
    }
}
