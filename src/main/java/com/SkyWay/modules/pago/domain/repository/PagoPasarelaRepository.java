package com.SkyWay.modules.pago.domain.repository;

import com.SkyWay.modules.pago.domain.model.PagoPasarela;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoPasarelaRepository extends JpaRepository<PagoPasarela, Integer> {
    boolean existsByProveedorAndIdExterno(String proveedor, String idExterno);
}
