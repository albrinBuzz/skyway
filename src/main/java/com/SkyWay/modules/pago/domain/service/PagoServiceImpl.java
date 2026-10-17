package com.SkyWay.modules.pago.domain.service;

import com.SkyWay.modules.pago.domain.model.Pago;
import com.SkyWay.modules.pago.domain.repository.PagoRepository;
import com.SkyWay.util.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PagoServiceImpl implements PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Override
    @Transactional
    public Pago save(Pago pago) {
        Logger.logInfo("💾 Guardando registro de pago por monto $" + pago.getMontoPagado());
        return pagoRepository.save(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pago> findById(Integer idPago) {
        return pagoRepository.findById(idPago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> findByReservaId(Integer idReserva) {
        return pagoRepository.findByReserva_IdReserva(idReserva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pago> findAll() {
        return pagoRepository.findAll();
    }
}