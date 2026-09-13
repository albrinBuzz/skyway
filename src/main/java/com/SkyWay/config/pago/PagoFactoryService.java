package com.SkyWay.config.pago;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PagoFactoryService {

    private final Map<MetodoPagoEnum, PasarelaPagoStrategy> estrategias;

    public PagoFactoryService(List<PasarelaPagoStrategy> estrategiaList) {
        this.estrategias = estrategiaList.stream()
                .collect(Collectors.toMap(PasarelaPagoStrategy::getMetodoPago, Function.identity()));
    }

    public PasarelaPagoStrategy obtenerEstrategia(MetodoPagoEnum metodo) {
        PasarelaPagoStrategy estrategia = estrategias.get(metodo);
        if (estrategia == null) {
            throw new IllegalArgumentException("Pasarela de pago no soportada: " + metodo);
        }
        return estrategia;
    }
}