package com.SkyWay.utils.converter;


import com.SkyWay.modules.aeropuerto.application.serviceImpl.AeropuertoServiceImpl;
import com.SkyWay.modules.aeropuerto.domain.model.Aeropuerto;
import com.SkyWay.modules.aeropuerto.domain.service.AeropuertoService;
import com.SkyWay.util.Logger;
import com.SkyWay.views.Vuelo.VueloBean;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@FacesConverter(value = "aeropuertoConverter", managed = true)
@Component
public class AeropuertoConverter implements Converter<Aeropuerto> {


    private Aeropuerto aeropuerto;

    @Autowired
    private AeropuertoService aeropuertoService;


    @Override
    public Aeropuerto getAsObject(FacesContext context, UIComponent component, String value) {

        if (value == null || value.isEmpty()) {
            return null;
        }
        // Obtener el bean AeropuertoService del contexto Spring manualmente
        ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        aeropuertoService = ctx.getBean(AeropuertoService.class);

        var aeropuerto=aeropuertoService.findById(Integer.valueOf(value)).get();
        //Logger.logInfo(aeropuerto.toString());
        return  aeropuerto;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Aeropuerto value) {
        if (value == null) {
            return "";
        }
        aeropuerto=value;
        //Logger.logInfo(value.toString());
        //System.out.println("Aeropuerto seleccionado 2"+aeropuerto);
        return value.getIdAeropuerto().toString();  // Convertimos el objeto en un String (puede ser el código IATA).
    }


}