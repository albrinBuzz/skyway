package com.SkyWay.modules.tipoturno.domain.service;



import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;
import java.util.List;
import java.util.Optional;

public interface TipoTurnoService {
    List<TipoTurno> findAll();
    Optional<TipoTurno> findById(Integer id);
    TipoTurno save(TipoTurno tipoTurno);
    TipoTurno update(Integer id, TipoTurno tipoTurno);
    void deleteById(Integer id);
}