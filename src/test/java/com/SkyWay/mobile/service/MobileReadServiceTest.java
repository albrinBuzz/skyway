package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.AeropuertoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MobileReadServiceTest {

    @Test
    @DisplayName("Debe mapear correctamente un arreglo con coordenadas PostGIS ST_X/ST_Y a AeropuertoDto")
    void debeMapearAeropuertoDtoConCoordenadasPostGis() {
        // Simular salida de native query con ST_Y (latitud) y ST_X (longitud) devueltos por PostGIS (Double o BigDecimal)
        Object[] row = new Object[] {
            10,                                              // 0: idAeropuerto
            "Aeropuerto Internacional Arturo Merino Benítez", // 1: nombre
            "SCL",                                          // 2: iata
            "Santiago",                                     // 3: ciudad
            "Chile",                                        // 4: pais
            -33.3928,                                       // 5: latitud (ST_Y)
            -70.7858                                        // 6: longitud (ST_X)
        };

        // start = 0
        AeropuertoDto dto = new AeropuertoDto(
            MobileReadService.number(row[0]),
            MobileReadService.text(row[1]),
            MobileReadService.text(row[2]),
            MobileReadService.text(row[3]),
            MobileReadService.text(row[4]),
            MobileReadService.decimal(row[5]),
            MobileReadService.decimal(row[6])
        );

        assertEquals(10, dto.idAeropuerto());
        assertEquals("Aeropuerto Internacional Arturo Merino Benítez", dto.nombreAeropuerto());
        assertEquals("SCL", dto.codigoIata());
        assertEquals("Santiago", dto.ciudad());
        assertEquals("Chile", dto.pais());
        assertEquals(-33.3928, dto.latitud(), 0.0001);
        assertEquals(-70.7858, dto.longitud(), 0.0001);
    }

    @Test
    @DisplayName("Debe manejar correctamente tipos BigDecimal provenientes de funciones espaciales")
    void debeMapearDecimalDesdeBigDecimalYNumber() {
        BigDecimal bdLat = new BigDecimal("-23.4356");
        Double dLon = -46.4731;

        assertEquals(-23.4356, MobileReadService.decimal(bdLat), 0.0001);
        assertEquals(-46.4731, MobileReadService.decimal(dLon), 0.0001);
        assertNull(MobileReadService.decimal(null));
    }

    @Test
    @DisplayName("Debe convertir correctamente Timestamp y LocalDateTime")
    void debeMapearFechasCorrectamente() {
        LocalDateTime now = LocalDateTime.of(2026, 10, 23, 14, 30, 0);
        Timestamp ts = Timestamp.valueOf(now);

        assertEquals(now, MobileReadService.date(now));
        assertEquals(now, MobileReadService.date(ts));
        assertNull(MobileReadService.date(null));
    }

    @Test
    @DisplayName("Debe generar código 2D IATA BCBP con firma criptográfica válida y Data URL PDF417")
    void debeGenerarCodigoIataBcbpYPdf417DataUrl() {
        String pnr = "SKW123";
        String numVuelo = "SW-501";
        String asiento = "3F";
        String rut = "11111111-1";

        // 1. Generar código seguro IATA BCBP
        String bcbp = com.SkyWay.util.BoardingPassSecurityService.generarCodigoSeguro(pnr, numVuelo, asiento, rut);
        assertNotNull(bcbp);
        assertTrue(bcbp.startsWith("SKYWAY"), "El código debe tener el prefijo oficial SKYWAY");

        // 2. Validar autenticidad criptográfica
        com.SkyWay.util.BoardingPassSecurityService.PayloadBoardingPass payload =
                com.SkyWay.util.BoardingPassSecurityService.parsearCodigo(bcbp);
        assertTrue(payload.isFormatoValido(), "El formato debe ser válido");
        assertTrue(payload.isFirmaAutentica(), "La firma digital debe ser auténtica");
        assertEquals("SKW123", payload.getPnr());
        assertEquals("SW-501", payload.getNumeroVuelo());
        assertEquals("3F", payload.getAsiento());
        assertEquals("11111111-1", payload.getRut());

        // 3. Generar PDF417 Base64 Data URL
        String dataUrl = com.SkyWay.util.BarcodeGeneratorUtil.generarPdf417DataUrl(bcbp);
        assertNotNull(dataUrl);
        assertTrue(dataUrl.startsWith("data:image/png;base64,"), "Debe generar una imagen PNG en Data URL");
        assertTrue(dataUrl.length() > 50, "El Data URL debe contener datos de imagen base64");
    }

    @Test
    @DisplayName("pase debe bloquear el acceso con CHECKIN_PENDIENTE si no se ha realizado el check-in")
    void debeBloquearPaseSinCheckin() {
        MobileReadService service = new MobileReadService(null, null, null, null) {
            @Override
            public com.SkyWay.mobile.dto.MobileDtos.DetalleReserva detalle(String rut, int id) {
                return new com.SkyWay.mobile.dto.MobileDtos.DetalleReserva(
                        id, null, null, null, true, List.of(), List.of(), null
                );
            }
        };

        MobileException ex = assertThrows(MobileException.class, () -> service.pase("11111111-1", 10, 55));
        assertEquals("CHECKIN_PENDIENTE", ex.getCodigo());
        assertTrue(ex.getMessage().contains("después de realizar el check-in"));
    }

    @Test
    @DisplayName("descargarBoardingPassPdf debe bloquear la descarga con CHECKIN_PENDIENTE si no se ha realizado el check-in")
    void debeBloquearDescargaPdfSinCheckin() {
        MobileReadService service = new MobileReadService(null, null, null, null) {
            @Override
            public com.SkyWay.mobile.dto.MobileDtos.DetalleReserva detalle(String rut, int id) {
                return new com.SkyWay.mobile.dto.MobileDtos.DetalleReserva(
                        id, null, null, null, true, List.of(), List.of(), null
                );
            }
        };

        MobileException ex = assertThrows(MobileException.class, () -> service.descargarBoardingPassPdf("11111111-1", 10));
        assertEquals("CHECKIN_PENDIENTE", ex.getCodigo());
        assertTrue(ex.getMessage().contains("después de realizar el check-in"));
    }
}
