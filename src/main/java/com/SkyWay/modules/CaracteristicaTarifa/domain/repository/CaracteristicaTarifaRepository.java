package com.SkyWay.modules.CaracteristicaTarifa.domain.repository;

import com.SkyWay.modules.CaracteristicaTarifa.domain.model.CaracteristicaTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaracteristicaTarifaRepository extends JpaRepository<CaracteristicaTarifa, Integer> {
}