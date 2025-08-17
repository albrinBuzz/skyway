package com.SkyWay.modules.segmentovuelo.infrastructure.validator;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;
import org.postgresql.util.PGInterval;

@Converter(autoApply = false)
public class DurationConverter implements AttributeConverter<Duration, Object> {

    @Override
    public Object convertToDatabaseColumn(Duration duration) {
        if (duration == null) return null;
        long seconds = duration.getSeconds();
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        int secs = (int) (seconds % 60);
        return new PGInterval(0, 0, 0, hours, minutes, secs);
    }

    @Override
    public Duration convertToEntityAttribute(Object dbData) {
        if (dbData == null) return null;
        if (dbData instanceof PGInterval pgInterval) {
            int hours = pgInterval.getHours();
            int minutes = pgInterval.getMinutes();
            int seconds = (int) pgInterval.getSeconds();
            return Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        }
        return null;
    }
}
