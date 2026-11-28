package com.SkyWay.modules.aeropuerto.application.serviceImpl;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.modules.aeropuerto.presentation.dto.PaisDestinosDTO;
import com.SkyWay.util.Logger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class AeropuertoServiceImpl implements AeropuertoService {

    private static final int DEFAULT_CANTIDAD_PUERTAS = 4;
    private static final int MIN_PUERTAS = 1;
    private static final int MAX_PUERTAS = 20;
    private static final String DEFAULT_TERMINAL = "Terminal Principal";

    @Autowired
    private AeropuertoRepository aeropuertoRepository;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    @Cacheable("aeropuertos")
    public List<Aeropuerto> findAll() {
        return aeropuertoRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<Aeropuerto> findById(Integer id) {
        return aeropuertoRepository.findById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public Aeropuerto save(Aeropuerto aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public void delete(Integer id) {
        aeropuertoRepository.softDeleteById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public void reactivar(Integer id) {
        aeropuertoRepository.reactivarById(id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public Aeropuerto update(Aeropuerto aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AeropuertoMapaProjection> findAllConCoordenadas() {
        return aeropuertoRepository.findAllConCoordenadas();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AeropuertoMapaProjection> findAllConCoordenadasAdmin() {
        return aeropuertoRepository.findAllConCoordenadasAdmin();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AeropuertoMapaDTO> findAllParaMapa() {
        return aeropuertoRepository.findAllConCoordenadas().stream()
                .map(p -> new AeropuertoMapaDTO(
                        p.getCodigoIata(), p.getNombreAeropuerto(), p.getCiudad(),
                        p.getLatitud(), p.getLongitud()))
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public void registrarAeropuerto(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception {
        registrarAeropuerto(nombre, codigoIata, idCiudad, latitud, longitud, DEFAULT_CANTIDAD_PUERTAS, DEFAULT_TERMINAL);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public void registrarAeropuerto(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud, int cantidadPuertas, String terminal) throws Exception {
        validarParametrosEntrada(nombre, codigoIata, idCiudad, latitud, longitud);
        if (cantidadPuertas < MIN_PUERTAS || cantidadPuertas > MAX_PUERTAS) {
            throw new IllegalArgumentException("La cantidad de puertas de embarque debe estar entre " + MIN_PUERTAS + " y " + MAX_PUERTAS + ".");
        }

        String iataClean = codigoIata.trim().toUpperCase(Locale.ROOT);
        if (aeropuertoRepository.existeIataGlobal(iataClean)) {
            if (aeropuertoRepository.existeIataInactivo(iataClean)) {
                throw new IllegalArgumentException("El código IATA " + iataClean + " pertenece a un aeropuerto inactivo. Puedes reactivarlo desde la tabla en lugar de crearlo nuevamente.");
            }
            throw new IllegalArgumentException("Ya existe un aeropuerto registrado con el código IATA: " + iataClean);
        }

        String nombrePaisEsperado = aeropuertoRepository.obtenerNombrePaisPorCiudad(idCiudad);
        if (nombrePaisEsperado == null) {
            throw new IllegalArgumentException("La ciudad seleccionada no existe en el repositorio.");
        }

        validarUbicacionConPais(latitud, longitud, nombrePaisEsperado);

        sincronizarSecuencia("aeropuerto_seq", "aeropuerto", "id_aeropuerto");
        sincronizarSecuencia("puerta_seq", "puerta_embarque", "id_puerta");

        Integer idAeropuertoNuevo = aeropuertoRepository.registrarAeropuertoRetornandoId(
                nombre.trim(),
                iataClean,
                idCiudad,
                latitud,
                longitud
        );

        generarPuertasEmbarque(idAeropuertoNuevo, iataClean, cantidadPuertas, terminal);
    }

    @Override
    @Transactional
    @CacheEvict(value = "aeropuertos", allEntries = true)
    public void actualizarAeropuerto(Integer id, String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("El ID del aeropuerto es obligatorio para actualizar.");
        }
        validarParametrosEntrada(nombre, codigoIata, idCiudad, latitud, longitud);

        String iataClean = codigoIata.trim().toUpperCase(Locale.ROOT);
        if (aeropuertoRepository.existeIataGlobalExcluyendoId(iataClean, id)) {
            throw new IllegalArgumentException("Ya existe otro aeropuerto registrado con el código IATA: " + iataClean);
        }

        String nombrePaisEsperado = aeropuertoRepository.obtenerNombrePaisPorCiudad(idCiudad);
        if (nombrePaisEsperado == null) {
            throw new IllegalArgumentException("La ciudad seleccionada no existe en el repositorio.");
        }

        validarUbicacionConPais(latitud, longitud, nombrePaisEsperado);

        aeropuertoRepository.actualizarAeropuertoNativo(
                id,
                nombre.trim(),
                iataClean,
                idCiudad,
                latitud,
                longitud
        );

        if (aeropuertoRepository.contarPuertasActivasPorAeropuerto(id) == 0) {
            sincronizarSecuencia("puerta_seq", "puerta_embarque", "id_puerta");
            generarPuertasEmbarque(id, iataClean, DEFAULT_CANTIDAD_PUERTAS, DEFAULT_TERMINAL);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPuertasActivasPorAeropuerto(Integer idAeropuerto) {
        if (idAeropuerto == null) return 0L;
        return aeropuertoRepository.contarPuertasActivasPorAeropuerto(idAeropuerto);
    }

    private void generarPuertasEmbarque(Integer idAeropuerto, String iataClean, int cantidadPuertas, String terminalRaw) {
        if (idAeropuerto == null || cantidadPuertas <= 0) return;
        String terminal = (terminalRaw != null && !terminalRaw.isBlank()) ? terminalRaw.trim() : DEFAULT_TERMINAL;
        if (terminal.length() > 50) {
            terminal = terminal.substring(0, 50);
        }
        for (int i = 1; i <= cantidadPuertas; i++) {
            String codigoPuerta = String.format(Locale.ROOT, "%s-%02d", iataClean, i);
            aeropuertoRepository.insertarPuertaEmbarqueNativa(codigoPuerta, terminal, idAeropuerto);
        }
    }

    private void sincronizarSecuencia(String secuencia, String tabla, String columnaPk) {
        if (jdbcTemplate == null) return;
        try {
            jdbcTemplate.queryForObject(
                    "SELECT setval('" + secuencia + "', GREATEST(COALESCE((SELECT MAX(" + columnaPk + ") FROM " + tabla + "), 0), 1), (SELECT COUNT(*) > 0 FROM " + tabla + "))",
                    Long.class
            );
        } catch (Exception ignored) {
        }
    }

    private void validarParametrosEntrada(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del aeropuerto es requerido.");
        }
        String nombreLimpio = nombre.trim();
        if (nombreLimpio.length() < 3 || nombreLimpio.length() > 120) {
            throw new IllegalArgumentException("El nombre del aeropuerto debe tener entre 3 y 120 caracteres.");
        }
        if (codigoIata == null || !codigoIata.trim().matches("^[A-Za-z]{3}$")) {
            throw new IllegalArgumentException("El código IATA debe contener exactamente 3 letras.");
        }
        if (idCiudad == null) {
            throw new IllegalArgumentException("Debe seleccionar una ciudad válida.");
        }
        if (latitud == null || longitud == null) {
            throw new IllegalArgumentException("Debe marcar las coordenadas en el mapa.");
        }
        if (latitud < -90.0 || latitud > 90.0 || longitud < -180.0 || longitud > 180.0) {
            throw new IllegalArgumentException("Las coordenadas GPS están fuera de rango válido (latitud -90 a 90, longitud -180 a 180).");
        }
    }

    @SuppressWarnings("unchecked")
    private void validarUbicacionConPais(Double lat, Double lng, String nombrePaisEsperado) {
        try {
            URI reverseUri = URI.create(String.format(Locale.US,
                    "https://nominatim.openstreetmap.org/reverse?format=json&extratags=1&zoom=18&accept-language=es&lat=%.6f&lon=%.6f",
                    lat, lng));

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(3000);
            factory.setReadTimeout(4500);
            RestTemplate restTemplate = new RestTemplate(factory);

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "SkyWay-AirportManager/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(reverseUri, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body == null || body.containsKey("error") || !body.containsKey("address")) {
                throw new IllegalArgumentException(
                        "Ubicación no válida para un aeropuerto: las coordenadas caen en océano o en una zona sin infraestructura aeroportuaria registrada.");
            }

            Map<String, Object> address = (Map<String, Object>) body.get("address");
            String countryDetectado = (String) address.get("country");
            String countryCode = (String) address.get("country_code");

            if ((countryDetectado != null && !countryDetectado.isBlank()) || (countryCode != null && !countryCode.isBlank())) {
                if (!GeoNombreResolver.paisesCoinciden(countryDetectado, countryCode, nombrePaisEsperado)) {
                    throw new IllegalArgumentException(
                            String.format("Incoherencia geográfica: Las coordenadas marcadas pertenecen a '%s', pero seleccionó una ciudad de '%s'.",
                                    countryDetectado != null ? countryDetectado : countryCode.toUpperCase(Locale.ROOT),
                                    nombrePaisEsperado)
                    );
                }
            }

            if (!verificarInfraestructuraAeroportuaria(restTemplate, entity, lat, lng, body)) {
                throw new IllegalArgumentException(
                        "Ubicación no válida para un aeropuerto: no se detectó ningún aeropuerto ni aeródromo real en las coordenadas seleccionadas. Marque una infraestructura aeroportuaria válida (no calles, zonas residenciales, desiertos ni océanos).");
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.logError("Advertencia: No se pudo verificar la geocodificación externa: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private boolean verificarInfraestructuraAeroportuaria(RestTemplate restTemplate, HttpEntity<String> entity,
                                                          Double lat, Double lng, Map<String, Object> reverseBody) {
        if (esRespuestaAeropuertoDirecta(reverseBody)) {
            return true;
        }

        // Búsqueda acotada de aeródromos en OpenStreetMap alrededor del punto marcado
        double delta = 0.015;
        String viewbox = String.format(Locale.US, "%.5f,%.5f,%.5f,%.5f",
                lng - delta, lat + delta, lng + delta, lat - delta);
        URI searchUri = URI.create(
                "https://nominatim.openstreetmap.org/search?format=json&extratags=1&limit=5&bounded=1&viewbox="
                        + viewbox + "&q=%5Baeroway%3Daerodrome%5D");

        var searchResp = restTemplate.exchange(searchUri, HttpMethod.GET, entity, List.class);
        List<Map<String, Object>> results = searchResp.getBody();
        if (results == null || results.isEmpty()) {
            return false;
        }

        for (Map<String, Object> item : results) {
            if (item == null) continue;
            Map<String, Object> extra = (Map<String, Object>) item.get("extratags");
            if (extra != null && "helipad".equalsIgnoreCase(String.valueOf(extra.get("aeroway")))) {
                continue;
            }
            List<Object> bbox = (List<Object>) item.get("boundingbox");
            if (esPoligonoAerodromo(bbox)) {
                // Para aeropuertos con polígono OSM, exigir estar dentro de su perímetro real (~85 m de tolerancia)
                if (estaDentroDeBoundingBox(lat, lng, bbox, 0.0008)) {
                    return true;
                }
            } else {
                // Solo para aeródromos pequeños mapeados como punto único, aceptar hasta 350 m
                try {
                    double itemLat = Double.parseDouble(String.valueOf(item.get("lat")));
                    double itemLon = Double.parseDouble(String.valueOf(item.get("lon")));
                    if (calcularDistanciaKm(lat, lng, itemLat, itemLon) <= 0.35) {
                        return true;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return false;
    }

    private boolean esPoligonoAerodromo(List<Object> bbox) {
        if (bbox == null || bbox.size() < 4) return false;
        try {
            double latSpan = Math.abs(Double.parseDouble(String.valueOf(bbox.get(1))) - Double.parseDouble(String.valueOf(bbox.get(0))));
            double lonSpan = Math.abs(Double.parseDouble(String.valueOf(bbox.get(3))) - Double.parseDouble(String.valueOf(bbox.get(2))));
            return latSpan >= 0.003 || lonSpan >= 0.003;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private boolean esRespuestaAeropuertoDirecta(Map<String, Object> body) {
        if (body == null) return false;
        String cls = String.valueOf(body.getOrDefault("class", ""));
        String type = String.valueOf(body.getOrDefault("type", ""));
        if ("helipad".equalsIgnoreCase(type)) {
            return false;
        }
        if ("aeroway".equalsIgnoreCase(cls)) {
            return true;
        }
        Map<String, Object> address = (Map<String, Object>) body.get("address");
        if (address != null && address.get("aeroway") != null && !String.valueOf(address.get("aeroway")).isBlank()) {
            return true;
        }
        Map<String, Object> extra = (Map<String, Object>) body.get("extratags");
        if (extra != null) {
            if (extra.get("iata") != null || extra.get("icao") != null) {
                return true;
            }
            Object aeroTag = extra.get("aeroway");
            if (aeroTag != null && !"helipad".equalsIgnoreCase(String.valueOf(aeroTag))) {
                return true;
            }
        }
        return false;
    }

    private boolean estaDentroDeBoundingBox(double lat, double lng, List<Object> bbox, double margenGrados) {
        if (bbox == null || bbox.size() < 4) return false;
        try {
            double south = Double.parseDouble(String.valueOf(bbox.get(0))) - margenGrados;
            double north = Double.parseDouble(String.valueOf(bbox.get(1))) + margenGrados;
            double west = Double.parseDouble(String.valueOf(bbox.get(2))) - margenGrados;
            double east = Double.parseDouble(String.valueOf(bbox.get(3))) + margenGrados;
            return lat >= south && lat <= north && lng >= west && lng <= east;
        } catch (Exception e) {
            return false;
        }
    }

    private double calcularDistanciaKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Override
    @Transactional(readOnly = true)
    public long contarAeropuertosActivos() {
        return aeropuertoRepository.countByActivoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public long contarPaisesActivos() {
        return aeropuertoRepository.contarPaisesActivos();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaisDestinosDTO> obtenerDestinosAgrupadosPorPais() {
        List<Object[]> rows = aeropuertoRepository.findDirectorioAeropuertosPorPais();
        Map<String, List<PaisDestinosDTO.AeropuertoItemDTO>> agrupado = new LinkedHashMap<>();
        for (Object[] r : rows) {
            String pais = r[0] != null ? r[0].toString() : "";
            String ciudad = r[1] != null ? r[1].toString() : "";
            String iata = r[2] != null ? r[2].toString() : "";
            String nombre = r[3] != null ? r[3].toString() : "";
            agrupado.computeIfAbsent(pais, k -> new ArrayList<>())
                    .add(new PaisDestinosDTO.AeropuertoItemDTO(iata, nombre, ciudad));
        }
        List<PaisDestinosDTO> resultado = new ArrayList<>(agrupado.size());
        for (Map.Entry<String, List<PaisDestinosDTO.AeropuertoItemDTO>> e : agrupado.entrySet()) {
            resultado.add(new PaisDestinosDTO(e.getKey(), e.getValue()));
        }
        return resultado;
    }
}