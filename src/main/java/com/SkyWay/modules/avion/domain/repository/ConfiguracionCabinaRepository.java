package com.SkyWay.modules.avion.domain.repository;

import com.SkyWay.modules.avion.domain.model.ConfiguracionCabina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfiguracionCabinaRepository extends JpaRepository<ConfiguracionCabina, Integer> {

    @Query("SELECT cc FROM ConfiguracionCabina cc " +
           "LEFT JOIN FETCH cc.claseAsiento " +
           "WHERE cc.modeloAvion.idModelo = :idModelo " +
           "ORDER BY cc.filaInicio ASC")
    List<ConfiguracionCabina> findByModeloAvionIdModelo(@Param("idModelo") Integer idModelo);
}
