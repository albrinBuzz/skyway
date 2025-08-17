package com.SkyWay.utils.converter;


import com.SkyWay.model.Aeropuerto;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter(value = "aeropuertoConverter",forClass = Aeropuerto.class)
@RequestScoped
public class AeropuertoConverter implements Converter<Aeropuerto> {


    private Aeropuerto aeropuerto;

    @Override
    public Aeropuerto getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.isEmpty()) {
            return null;
        }

        return aeropuerto;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Aeropuerto value) {
        if (value == null) {
            return "";
        }
        aeropuerto=value;
        //System.out.println("Aeropuerto seleccionado 2"+aeropuerto);
        return value.getIdAeropuerto().toString();  // Convertimos el objeto en un String (puede ser el código IATA).
    }


}