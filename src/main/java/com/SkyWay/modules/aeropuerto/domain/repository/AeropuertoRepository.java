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

    @Query(value = "SELECT COUNT(*) > 0 FROM aeropuerto WHERE UPPER(codigo_iata) = UPPER(:iata)", nativeQuery = true)
    boolean existeIataGlobal(@Param("iata") String iata);

    @Query(value = "SELECT COUNT(*) > 0 FROM aeropuerto WHERE UPPER(codigo_iata) = UPPER(:iata) AND activo = false", nativeQuery = true)
    boolean existeIataInactivo(@Param("iata") String iata);

    @Query(value = "SELECT COUNT(*) > 0 FROM aeropuerto WHERE UPPER(codigo_iata) = UPPER(:iata) AND id_aeropuerto <> :id", nativeQuery = true)
    boolean existeIataGlobalExcluyendoId(@Param("iata") String iata, @Param("id") Integer id);

    java.util.List<Aeropuerto> findByActivoTrue();

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE aeropuerto SET activo = false, fecha_baja = CURRENT_TIMESTAMP WHERE id_aeropuerto = :id", nativeQuery = true)
    void softDeleteById(@Param("id") Integer id);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "UPDATE aeropuerto SET activo = true, fecha_baja = NULL WHERE id_aeropuerto = :id", nativeQuery = true)
    void reactivarById(@Param("id") Integer id);

    @Query(value = """
        SELECT
            a.id_aeropuerto     AS idAeropuerto,
            a.codigo_iata       AS codigoIata,
            a.nombre_aeropuerto AS nombreAeropuerto,
            c.nombre            AS ciudad,
            p.nombre            AS pais,
            COALESCE(cont.nombre, 'Sin continente') AS continente,
            ST_Y(a.posicion::geometry) AS latitud,
            ST_X(a.posicion::geometry) AS longitud,
            a.activo            AS activo
        FROM aeropuerto a
        JOIN ciudad c ON c.id_ciudad = a.id_ciudad
        JOIN pais p ON p.id_pais = c.id_pais
        LEFT JOIN continente cont ON cont.id_continente = p.id_continente
        WHERE a.activo = true AND c.activo = true
        ORDER BY a.nombre_aeropuerto
        """, nativeQuery = true)
    List<AeropuertoMapaProjection> findAllConCoordenadas();

    @Query(value = """
        SELECT
            a.id_aeropuerto     AS idAeropuerto,
            a.codigo_iata       AS codigoIata,
            a.nombre_aeropuerto AS nombreAeropuerto,
            c.nombre            AS ciudad,
            p.nombre            AS pais,
            COALESCE(cont.nombre, 'Sin continente') AS continente,
            ST_Y(a.posicion::geometry) AS latitud,
            ST_X(a.posicion::geometry) AS longitud,
            a.activo            AS activo
        FROM aeropuerto a
        JOIN ciudad c ON c.id_ciudad = a.id_ciudad
        JOIN pais p ON p.id_pais = c.id_pais
        LEFT JOIN continente cont ON cont.id_continente = p.id_continente
        ORDER BY a.activo DESC, a.nombre_aeropuerto
        """, nativeQuery = true)
    List<AeropuertoMapaProjection> findAllConCoordenadasAdmin();

    // Inserción nativa PostGIS retornando ID para crear puertas de embarque en la misma transacción
    @Query(value = """
        INSERT INTO aeropuerto (id_aeropuerto, nombre_aeropuerto, codigo_iata, id_ciudad, posicion, activo)
        VALUES (nextval('aeropuerto_seq'), :nombre, :codigoIata, :idCiudad, ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326), true)
        RETURNING id_aeropuerto
        """, nativeQuery = true)
    Integer registrarAeropuertoRetornandoId(
            @Param("nombre") String nombre,
            @Param("codigoIata") String codigoIata,
            @Param("idCiudad") Integer idCiudad,
            @Param("latitud") Double latitud,
            @Param("longitud") Double longitud
    );

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO puerta_embarque (id_puerta, codigo_puerta, terminal, id_aeropuerto, activo)
        VALUES (nextval('puerta_seq'), :codigoPuerta, :terminal, :idAeropuerto, true)
        """, nativeQuery = true)
    void insertarPuertaEmbarqueNativa(
            @Param("codigoPuerta") String codigoPuerta,
            @Param("terminal") String terminal,
            @Param("idAeropuerto") Integer idAeropuerto
    );

    @Query(value = "SELECT COUNT(*) FROM puerta_embarque WHERE id_aeropuerto = :idAeropuerto AND COALESCE(activo, true) = true", nativeQuery = true)
    long contarPuertasActivasPorAeropuerto(@Param("idAeropuerto") Integer idAeropuerto);

    // Inserción nativa PostGIS sin modificar el esquema existente
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO aeropuerto (id_aeropuerto, nombre_aeropuerto, codigo_iata, id_ciudad, posicion, activo)
        VALUES (nextval('aeropuerto_seq'), :nombre, :codigoIata, :idCiudad, ST_SetSRID(ST_MakePoint(:longitud, :latitud), 4326), true)
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

    long countByActivoTrue();

    @Query(value = """
        SELECT COUNT(DISTINCT p.id_pais)
        FROM aeropuerto a
        JOIN ciudad c ON c.id_ciudad = a.id_ciudad
        JOIN pais p ON p.id_pais = c.id_pais
        WHERE a.activo = true AND c.activo = true AND p.activo = true
        """, nativeQuery = true)
    long contarPaisesActivos();

    @Query(value = """
        SELECT p.nombre AS pais, c.nombre AS ciudad, a.codigo_iata, a.nombre_aeropuerto
        FROM aeropuerto a
        JOIN ciudad c ON c.id_ciudad = a.id_ciudad
        JOIN pais p ON p.id_pais = c.id_pais
        WHERE a.activo = true AND c.activo = true AND p.activo = true
        ORDER BY p.nombre ASC, c.nombre ASC, a.codigo_iata ASC
        """, nativeQuery = true)
    List<Object[]> findDirectorioAeropuertosPorPais();
}