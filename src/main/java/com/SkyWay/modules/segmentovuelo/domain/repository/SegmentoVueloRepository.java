package com.SkyWay.modules.segmentovuelo.domain.repository;




import com.SkyWay.modules.segmentovuelo.domain.model.SegmentoVuelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegmentoVueloRepository extends JpaRepository<SegmentoVuelo, Integer> {
    // Métodos CRUD ya están incluidos por JpaRepository

    @Query(value = "SELECT id_segmento, hora_llegada - hora_salida AS duracion FROM segmento_vuelo", nativeQuery = true)
    List<Object[]> obtenerDuraciones();

    @Query("SELECT s FROM SegmentoVuelo s WHERE s.vuelo.idVuelo = :idVuelo ORDER BY s.ordenSegmento")
    List<SegmentoVuelo> buscarPorVuelo(@Param("idVuelo") Integer idVuelo);


}
