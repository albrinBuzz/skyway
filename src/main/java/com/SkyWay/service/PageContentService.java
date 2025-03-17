package com.SkyWay.service;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.SkyWay.model.Contenido;
import com.SkyWay.repository.ContenidoRepository;

@Service
public class PageContentService {

    @Autowired
    private ContenidoRepository pageContentRepository;

    // Método para obtener el contenido por tipo
    public List<Contenido> getContentByTipo(String tipo) {
        return pageContentRepository.findByTipo(tipo); // Método del repositorio que obtiene contenido por tipo
    }
}
