package com.SkyWay.modules.tarifaItinerario.domain.service;


import com.SkyWay.modules.tarifa.domain.model.Tarifa;
import com.SkyWay.modules.tarifa.presentation.dto.CaracteristicaDTO;
import com.SkyWay.modules.tarifa.presentation.dto.TarifaDTO;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.repository.ItinerarioTarifaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ItinerarioTarifaService {

    private final ItinerarioTarifaRepository repository;

    public ItinerarioTarifaService(ItinerarioTarifaRepository repository) {
        this.repository = repository;
    }

    public List<ItinerarioTarifa> findAll() {
        return repository.findAll();
    }

    public Optional<ItinerarioTarifa> findById(Integer id) {
        return repository.findById(id);
    }

    public ItinerarioTarifa save(ItinerarioTarifa entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public List<ItinerarioTarifa> findByItinerario(Integer idItinerario) {
        return repository.findByItinerarioIdItinerario(idItinerario);
    }
    public ItinerarioTarifa getByTarifaAndItinerario(Integer idItinerario, Integer idTarifa){
        return repository.getByTarifaAndItinerario(idItinerario,idTarifa);
    }

    public List<TarifaDTO> getTarifasItinerario(Integer idItinerario) {
        List<ItinerarioTarifa> tarifasItinerario = repository.findByItinerarioIdItinerario(idItinerario);

        List<TarifaDTO> resultado = new ArrayList<>();

        for (ItinerarioTarifa it : tarifasItinerario) {
            Tarifa tarifa = it.getTarifa(); // Asegúrate que esté cargada con fetch (o usa @EntityGraph en el repo)

            List<CaracteristicaDTO> caracteristicasDTO = tarifa.getTarifaCaracteristicas().stream()
                    .map(tc -> new CaracteristicaDTO(
                            tc.getCaracteristica().getNombre(),
                            tc.getValor(),
                            tc.getCaracteristica().getTipoDato(),
                            tc.getValorBool(),
                            tc.getValorInt()
                    ))
                    .collect(Collectors.toList());

            TarifaDTO dto = new TarifaDTO();

            dto.setIdTarifa(tarifa.getIdTarifa());
            dto.setNombre(tarifa.getNombre());
            dto.setPrecio(it.getPrecio());
            dto.setCaracteristicas(caracteristicasDTO);

            resultado.add(dto);
        }

        return resultado;
    }



}
