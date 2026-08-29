package com.SkyWay.modules.equipaje.domain.service;

import com.SkyWay.modules.equipaje.domain.model.Equipaje;
import com.SkyWay.modules.equipaje.domain.repository.EquipajeRepository;

import com.SkyWay.modules.equipaje.domain.service.EquipajeService;
import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.pasajero.domain.repository.PasajeroRepository; // Supeditado a tu modulo
import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.repository.ReservaRepository;   // Supeditado a tu modulo
import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import com.SkyWay.modules.tipoequipaje.domain.repository.TipoEquipajeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class EquipajeServiceImpl implements EquipajeService
{

    private static final BigDecimal MAX_PESO_PERMITIDO_POR_PASAJERO = new BigDecimal("50.00");

    private final EquipajeRepository equipajeRepository;
    private final ReservaRepository reservaRepository;
    private final PasajeroRepository pasajeroRepository;
    private final TipoEquipajeRepository tipoEquipajeRepository;

    public EquipajeServiceImpl(EquipajeRepository equipajeRepository,
                               ReservaRepository reservaRepository,
                               PasajeroRepository pasajeroRepository,
                               TipoEquipajeRepository tipoEquipajeRepository) {
        this.equipajeRepository = equipajeRepository;
        this.reservaRepository = reservaRepository;
        this.pasajeroRepository = pasajeroRepository;
        this.tipoEquipajeRepository = tipoEquipajeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipaje> listarTodos() {
        return equipajeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Equipaje buscarPorId(Integer id) {
        return equipajeRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("Equipaje no encontrado con ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipaje> buscarPorReserva(Integer idReserva) {
        return equipajeRepository.findByReservaIdReserva(Long.valueOf(idReserva));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Equipaje> buscarPorPasajero(String rut) {
        return equipajeRepository.findByPasajeroRut(rut);
    }

    @Override
    public Equipaje guardar(Equipaje equipaje, Integer idReserva, String rutPasajero, Integer idTipoEquipaje) {
        Reserva reserva = reservaRepository.findById(idReserva)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + idReserva));

        Pasajero pasajero = pasajeroRepository.findById(rutPasajero)
                .orElseThrow(() -> new RuntimeException("Pasajero no encontrado con RUT: " + rutPasajero));

        TipoEquipaje tipoEquipaje = tipoEquipajeRepository.findById(idTipoEquipaje)
                .orElseThrow(() -> new RuntimeException("Tipo de Equipaje no encontrado con ID: " + idTipoEquipaje));

        // Validación de reglas de negocio en la aerolínea
        BigDecimal pesoActual = equipajeRepository.sumPesoByReservaAndPasajero(Long.valueOf(idReserva), rutPasajero);
        BigDecimal pesoProyectado = pesoActual.add(equipaje.getPeso());

        if (pesoProyectado.compareTo(MAX_PESO_PERMITIDO_POR_PASAJERO) > 0) {
            throw new IllegalArgumentException(
                    String.format("El peso acumulado sobrepasa el máximo permitido de %s kg. Peso actual: %s kg, intentando registrar: %s kg",
                            MAX_PESO_PERMITIDO_POR_PASAJERO, pesoActual, equipaje.getPeso())
            );
        }

        equipaje.setReserva(reserva);
        equipaje.setPasajero(pasajero);
        equipaje.setTipoEquipaje(tipoEquipaje);
        equipaje.setTipo(tipoEquipaje.getNombre()); // Asignación del texto según la categoría

        return equipajeRepository.save(equipaje);
    }

    @Override
    public void eliminar(Integer id) {
        if (!equipajeRepository.existsById(Long.valueOf(id))) {
            throw new RuntimeException("No existe el equipaje a eliminar.");
        }
        equipajeRepository.deleteById(Long.valueOf(id));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularPesoTotalPasajeroEnReserva(Integer idReserva, String rut) {
        return equipajeRepository.sumPesoByReservaAndPasajero(Long.valueOf(idReserva), rut);
    }
}