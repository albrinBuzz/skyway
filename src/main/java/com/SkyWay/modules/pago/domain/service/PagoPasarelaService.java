package com.SkyWay.modules.pago.domain.service;

import com.SkyWay.modules.pago.domain.model.PagoPasarela;
import com.SkyWay.modules.pago.domain.repository.PagoPasarelaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;

/** Maquina de estados de los pagos de pasarela. Cada operacion corre en su propia transaccion. */
@Service
public class PagoPasarelaService {

    @Autowired
    private PagoPasarelaRepository repository;

    @Transactional(readOnly = true)
    public boolean yaConsumido(String proveedor, String idExterno) {
        return repository.existsByProveedorAndIdExterno(proveedor, idExterno);
    }

    /**
     * Registra la captura en estado CAPTURADO. Devuelve null si ese id externo ya fue consumido (replay).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Integer registrarCaptura(String proveedor, String idExterno, String ordenCompra,
                                    BigDecimal monto, String moneda) {
        PagoPasarela p = new PagoPasarela();
        p.setProveedor(proveedor);
        p.setIdExterno(idExterno);
        p.setOrdenCompra(ordenCompra);
        p.setMonto(monto);
        p.setMoneda(moneda);
        p.setEstado(PagoPasarela.CAPTURADO);
        Timestamp ahora = new Timestamp(System.currentTimeMillis());
        p.setFechaCreacion(ahora);
        p.setFechaActualizacion(ahora);
        try {
            return repository.saveAndFlush(p).getId();
        } catch (DataIntegrityViolationException e) {
            return null;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cambiarEstado(Integer id, String estado, Integer idReserva) {
        repository.findById(id).ifPresent(p -> {
            p.setEstado(estado);
            if (idReserva != null) {
                p.setIdReserva(idReserva);
            }
            p.setFechaActualizacion(new Timestamp(System.currentTimeMillis()));
            repository.save(p);
        });
    }
}
