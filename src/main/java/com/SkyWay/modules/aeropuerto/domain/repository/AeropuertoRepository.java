package com.SkyWay.modules.aeropuerto.domain.repository;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AeropuertoRepository extends JpaRepository<Aeropuerto, Integer> {

    boolean existsByCodigoIata(String codigoIata);

    @Query(value = """
        SELECT
            a.id_aeropuerto     AS idAeropuerto,
            a.codigo_iata       AS codigoIata,
            a.nombre_aeropuerto AS nombreAeropuerto,
            c.nombre            AS ciudad,
            ST_Y(a.posicion::geometry) AS latitud,
            ST_X(a.posicion::geometry) AS longitud
        FROM aeropuerto a
        JOIN ciudad c ON c.id_ciudad = a.id_ciudad
        ORDER BY a.nombre_aeropuerto
        """, nativeQuery = true)
    List<AeropuertoMapaProjection> findAllConCoordenadas();

    // Inserción nativa PostGIS sin modificar el esquema existente
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO aeropuerto (id_aeropuerto, nombre_aeropuerto, codigo_iata, id_ciudad, posicion)
        VALUES (nextval('aeropuerto_seq'), :nombre, :codigoIata, :idCiudad, ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326))
        """, nativeQuery = true)
    void registrarAeropuertoNativo(
            @Param("nombre") String nombre,
            @Param("codigoIata") String codigoIata,
            @Param("idCiudad") Integer idCiudad,
            @Param("latitud") Double latitud,
            @Param("longitud") Double longitud
    );

    // Edición nativa PostGIS
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE aeropuerto
        SET nombre_aeropuerto = :nombre,
            codigo_iata = :codigoIata,
            id_ciudad = :idCiudad,
            posicion = ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326)
        WHERE id_aeropuerto = :id
        """, nativeQuery = true)
    void actualizarAeropuertoNativo(
            @Param("id") Integer id,
            @Param("nombre") String nombre,
            @Param("codigoIata") String codigoIata,
            @Param("idCiudad") Integer idCiudad,
            @Param("latitud") Double latitud,
            @Param("longitud") Double longitud
    );

    @Query(value = """
        SELECT p.nombre 
        FROM ciudad c 
        JOIN pais p ON p.id_pais = c.id_pais 
        WHERE c.id_ciudad = :idCiudad
        """, nativeQuery = true)
    String obtenerNombrePaisPorCiudad(@Param("idCiudad") Integer idCiudad);

    @Query(value = """
        SELECT c.nombre 
        FROM ciudad c 
        WHERE c.id_ciudad = :idCiudad
        """, nativeQuery = true)
    String obtenerNombreCiudadPorId(@Param("idCiudad") Integer idCiudad);
}