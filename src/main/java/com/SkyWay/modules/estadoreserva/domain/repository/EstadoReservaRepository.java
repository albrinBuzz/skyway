package com.SkyWay.modules.estadoreserva.domain.repository;


import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoReservaRepository extends JpaRepository<EstadoReserva, Integer> {
    // Aquí puedes agregar más métodos personalizados si es necesario
}
