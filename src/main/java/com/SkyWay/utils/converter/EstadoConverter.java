package com.SkyWay.utils.converter;

import com.SkyWay.model.Aeropuerto;
import com.SkyWay.model.EstadoVuelo;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import org.springframework.stereotype.Component;

@FacesConverter(value = "estadoConverter",forClass = EstadoVuelo.class)
@Component
public class EstadoConverter implements Converter<EstadoVuelo> {

    private EstadoVuelo estado;

    @Override
    public EstadoVuelo getAsObject(FacesContext context, UIComponent component, String value) {
        return estado;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, EstadoVuelo value) {
        estado=value;
        return value.getIdEstadoVuelo().toString();
    }
}