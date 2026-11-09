package com.SkyWay.modules.CaracteristicaTarifa.domain.service;

import com.SkyWay.modules.CaracteristicaTarifa.domain.model.CaracteristicaTarifa;
import com.SkyWay.modules.CaracteristicaTarifa.domain.repository.CaracteristicaTarifaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CaracteristicaTarifaService {

    @Autowired
    private CaracteristicaTarifaRepository caracteristicaTarifaRepository;

    @Transactional(readOnly = true)
    public List<CaracteristicaTarifa> findAll() {
        return caracteristicaTarifaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<CaracteristicaTarifa> findAllAdmin() {
        return caracteristicaTarifaRepository.findAllAdmin();
    }

    public CaracteristicaTarifa save(CaracteristicaTarifa caracteristicaTarifa){
        return caracteristicaTarifaRepository.save(caracteristicaTarifa);
    }

    @Transactional
    public void eliminar(Integer id) {
        caracteristicaTarifaRepository.softDeleteById(id);
    }

    @Transactional
    public void reactivar(Integer id) {
        caracteristicaTarifaRepository.reactivarById(id);
    }
}