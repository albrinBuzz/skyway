package com.SkyWay.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

@RestController
@RequestMapping("/api/v1/heavy-tasks")
@CrossOrigin(origins = "*")
public class HeavyTaskController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Simula una tarea pesada en la JVM (CPU y Memoria RAM).
     * Genera cálculo intensivo de números primos y aloja objetos en Heap.
     * GET /api/v1/heavy-tasks/cpu-ram?iterations=500000
     */
    @GetMapping("/cpu-ram")
    public ResponseEntity<Map<String, Object>> simularTareaPesadaCPU(
            @RequestParam(defaultValue = "300000") int iterations) {

        long startTime = System.currentTimeMillis();
        List<String> memoryCollector = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        // 1. Tarea pesada de CPU: Cálculo de números primos gigantes
        for (int i = 0; i < iterations; i++) {
            BigInteger prime = BigInteger.probablePrime(128, random);

            // 2. Tarea pesada de Memoria: Generación y retención de strings en Heap
            if (i % 5 == 0) {
                memoryCollector.add("Memory_Payload_Chunk_" + i + "_" + prime.toString());
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "tipoTarea", "CPU_Y_MEMORIA_JVM",
                "iteracionesProcesadas", iterations,
                "elementosEnMemoria", memoryCollector.size(),
                "tiempoEjecucionMs", duration
        ));
    }

    /**
     * Simula una tarea pesada en la Base de Datos PostgreSQL/Oracle.
     * Ejecuta joins masivos, ordenamientos de alto costo y funciones agregadas sobre la BD.
     * GET /api/v1/heavy-tasks/database?intensidad=ALTA
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> simularTareaPesadaBD(
            @RequestParam(defaultValue = "MEDIA") String intensidad) {

        long startTime = System.currentTimeMillis();

        // Consulta SQL pesada: Realiza Joins complejos sin límites iniciales y fuerza ordenamientos pesados
        String sql = """
            SELECT 
                iv.id_itinerario,
                COUNT(v.id_vuelo) AS total_vuelos,
                AVG(pa.precio) AS precio_promedio,
                STRING_AGG(v.numero_vuelo, ', ') AS lista_vuelos
            FROM itinerario_vuelo iv
            JOIN vuelo v ON iv.id_vuelo = v.id_vuelo
            JOIN precio_asiento pa ON v.id_vuelo = pa.id_vuelo
            JOIN segmento_vuelo sv ON v.id_vuelo = sv.id_vuelo
            GROUP BY iv.id_itinerario
            HAVING COUNT(v.id_vuelo) >= 1
            ORDER BY precio_promedio DESC, total_vuelos DESC
            """;

        if ("ALTA".equalsIgnoreCase(intensidad)) {
            // Fuerza a la BD a ejecutar la consulta múltiples veces en un solo llamado
            for (int i = 0; i < 5; i++) {
                jdbcTemplate.queryForList(sql);
            }
        } else {
            jdbcTemplate.queryForList(sql);
        }

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "tipoTarea", "CONSULTA_HEAVY_DATABASE",
                "nivelIntensidad", intensidad.toUpperCase(),
                "tiempoEjecucionBDMs", duration
        ));
    }

    /**
     * Simula un bloqueo (sleep/lock) en la base de datos para medir consumo de conexiones en HikariCP.
     * GET /api/v1/heavy-tasks/db-sleep?segundos=5
     */
    @GetMapping("/db-sleep")
    public ResponseEntity<Map<String, Object>> simularBloqueoConexionBD(
            @RequestParam(defaultValue = "5") int segundos) {

        long startTime = System.currentTimeMillis();

        // Ejecuta un SLEEP nativo en PostgreSQL (retine la conexión del Pool)
        jdbcTemplate.execute("SELECT pg_sleep(" + segundos + ")");

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "tipoTarea", "RETENCION_CONEXION_HIKARICP",
                "segundosRetenido", segundos,
                "tiempoTotalMs", duration
        ));
    }



    /**
     * 1. Carga de CPU en BD: Genera millones de filas al vuelo en memoria RAM del motor
     * y realiza cálculos matemáticos (criptografía/hashes) sobre ellas.
     * GET /api/v1/heavy-db/cpu?filas=2000000
     */
    /**
     * 1. Carga CPU BD Corregida: Casteo explicito a BIGINT para evitar 'entero fuera de rango'.
     */
    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> simularCargaCpuBD(
            @RequestParam(defaultValue = "1000000") int filas) {

        long startTime = System.currentTimeMillis();

        // Casteamos g.i::bigint para permitir operaciones con números gigantes sin desbordamiento
        String sql = """
            SELECT 
                count(*),
                max(md5(g.i::text || 'skyway_salt')),
                max(sha256((g.i::bigint * 99999)::text::bytea))
            FROM generate_series(1, ?) AS g(i)
            """;

        jdbcTemplate.queryForMap(sql, filas);

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "estrategia", "GENERACION_EN_MEMORIA_Y_HASHING_CPU",
                "filasProcesadasAlVuelo", filas,
                "tiempoEjecucionBDMs", duration
        ));
    }

    /**
     * 2. Carga I/O BD Corregida: Uso de Subquery (CTE) en lugar de tablas temporales para
     * evitar fallos en peticiones concurrentes del pool de conexiones.
     */
    @GetMapping("/io-ram")
    public ResponseEntity<Map<String, Object>> simularCargaIoRamBD(
            @RequestParam(defaultValue = "200000") int filas) {

        long startTime = System.currentTimeMillis();

        // Genera la carga e I/O directamente en la misma consulta usando un WITH
        String sql = """
            WITH temp_stress_data AS (
                SELECT i AS id, md5(i::text) AS hash_data, random() AS val
                FROM generate_series(1, ?) AS i
            )
            SELECT * FROM temp_stress_data ORDER BY hash_data DESC LIMIT 100
            """;

        jdbcTemplate.queryForList(sql, filas);

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "estrategia", "CTE_Y_ORDENAMIENTO_IO",
                "filasGeneradas", filas,
                "tiempoEjecucionBDMs", duration
        ));
    }

    /**
     * 3. Producto Cartesiano Masivo (Cross Join): Toma tus pocas tablas existentes (p. ej. 10 filas)
     * y las multiplica exponencialmente en memoria (10 x 10 x 10 x 10 = 10,000 operaciones).
     * GET /api/v1/heavy-db/cross-join
     */
    @GetMapping("/cross-join")
    public ResponseEntity<Map<String, Object>> simularCrossJoinConPocosDatos() {

        long startTime = System.currentTimeMillis();

        // Multiplica las filas de tus tablas existentes sin importar que tengan solo 5 o 10 registros
        String sql = """
            SELECT count(*) 
            FROM aeropuerto a1 
            CROSS JOIN aeropuerto a2 
            CROSS JOIN aeropuerto a3
            CROSS JOIN aeropuerto a4
            """;

        Long totalCombinaciones = jdbcTemplate.queryForObject(sql, Long.class);

        long duration = System.currentTimeMillis() - startTime;

        return ResponseEntity.ok(Map.of(
                "estrategia", "PRODUCTO_CARTESIANO_CROSS_JOIN",
                "totalCombinacionesCalculadas", totalCombinaciones != null ? totalCombinaciones : 0,
                "tiempoEjecucionBDMs", duration
        ));
    }
}