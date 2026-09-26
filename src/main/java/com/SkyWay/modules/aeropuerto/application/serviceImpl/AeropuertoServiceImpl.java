package com.SkyWay.modules.aeropuerto.application.serviceImpl;

import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.repository.AeropuertoRepository;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaDTO;
import com.SkyWay.modules.aeropuerto.presentation.dto.AeropuertoMapaProjection;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AeropuertoServiceImpl implements AeropuertoService {

    @Autowired
    private AeropuertoRepository aeropuertoRepository;

    @Override
    @Transactional(readOnly = true)
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
    public Aeropuerto save(Aeropuerto aeropuerto) {
        return aeropuertoRepository.save(aeropuerto);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        aeropuertoRepository.deleteById(id);
    }

    @Override
    @Transactional
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
    public List<AeropuertoMapaDTO> findAllParaMapa() {
        return aeropuertoRepository.findAllConCoordenadas().stream()
                .map(p -> new AeropuertoMapaDTO(
                        p.getCodigoIata(), p.getNombreAeropuerto(), p.getCiudad(),
                        p.getLatitud(), p.getLongitud()))
                .toList();
    }

    @Override
    @Transactional
    public void registrarAeropuerto(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception {
        validarParametrosEntrada(nombre, codigoIata, idCiudad, latitud, longitud);

        String iataClean = codigoIata.trim().toUpperCase();
        if (aeropuertoRepository.existsByCodigoIata(iataClean)) {
            throw new IllegalArgumentException("Ya existe un aeropuerto registrado con el código IATA: " + iataClean);
        }

        String nombrePaisEsperado = aeropuertoRepository.obtenerNombrePaisPorCiudad(idCiudad);
        if (nombrePaisEsperado == null) {
            throw new IllegalArgumentException("La ciudad seleccionada no existe en el repositorio.");
        }

        validarUbicacionConPais(latitud, longitud, nombrePaisEsperado);

        aeropuertoRepository.registrarAeropuertoNativo(
                nombre.trim(),
                iataClean,
                idCiudad,
                latitud,
                longitud
        );
    }

    @Override
    @Transactional
    public void actualizarAeropuerto(Integer id, String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) throws Exception {
        if (id == null) {
            throw new IllegalArgumentException("El ID del aeropuerto es obligatorio para actualizar.");
        }
        validarParametrosEntrada(nombre, codigoIata, idCiudad, latitud, longitud);

        String iataClean = codigoIata.trim().toUpperCase();
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
    }

    private void validarParametrosEntrada(String nombre, String codigoIata, Integer idCiudad, Double latitud, Double longitud) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del aeropuerto es requerido.");
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
    }

    @SuppressWarnings("unchecked")
    private void validarUbicacionConPais(Double lat, Double lng, String nombrePaisEsperado) {
        try {
            String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&accept-language=es", lat, lng);
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "SkyWay-AirportManager/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            Map<String, Object> body = response.getBody();

            if (body != null && body.containsKey("address")) {
                Map<String, Object> address = (Map<String, Object>) body.get("address");
                String countryDetectado = (String) address.get("country");

                if (countryDetectado != null && !countryDetectado.toLowerCase().contains(nombrePaisEsperado.toLowerCase())) {
                    throw new IllegalArgumentException(
                            String.format("Incoherencia geográfica: Las coordenadas marcadas pertenecen a '%s', pero seleccionó una ciudad de '%s'.",
                                    countryDetectado, nombrePaisEsperado)
                    );
                }
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            Logger.logError("Advertencia: No se pudo verificar la geocodificación externa: " + ex.getMessage());
        }
    }
}