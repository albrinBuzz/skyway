package com.SkyWay.modules.aerolinea.domain.service;




import com.SkyWay.modules.aerolinea.domain.model.Aerolinea;

import java.util.List;

public interface AerolineaService {
    List<Aerolinea> findAll();
    Aerolinea findById(Integer id);
    Aerolinea save(Aerolinea aerolinea);
    Aerolinea update(Aerolinea aerolinea);
    void deleteById(Integer id);
}
