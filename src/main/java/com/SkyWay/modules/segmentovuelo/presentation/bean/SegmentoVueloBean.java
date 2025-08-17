package com.SkyWay.modules.segmentovuelo.presentation.bean;

import com.SkyWay.modules.segmentovuelo.domain.service.SegmentoVueloService;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;

@Named
@ViewScoped
public class SegmentoVueloBean implements Serializable {

    @Autowired
    private SegmentoVueloService segmentoVueloService;

}
