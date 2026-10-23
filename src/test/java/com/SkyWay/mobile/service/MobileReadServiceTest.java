package com.SkyWay.mobile.service;

import com.SkyWay.mobile.dto.MobileDtos.AeropuertoDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
}
