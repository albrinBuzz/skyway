package com.SkyWay.beans;

import java.util.List;

import com.SkyWay.model.Contenido;
import com.SkyWay.service.PageContentService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ViewScoped
@Named("contenidoView")
public class ContenidoBean {


    @Inject
    private PageContentService pageContentService; // Servicio que trae los datos

    private List<Contenido> contenidos; // Lista que guardará los contenidos

  
    private String tipo; 

    // Este método es llamado automáticamente después de la inicialización del bean
    @PostConstruct
    public void init() {
    	FacesContext facesContext = FacesContext.getCurrentInstance();
    	tipo = facesContext.getExternalContext().getRequestParameterMap().get("tipo");

    	 
    	System.out.println("Tipo: "+tipo);
        loadContent(); // Cargar el contenido automáticamente basado en el parámetro tipo
    }

    // Este método carga el contenido de acuerdo al tipo
    public void loadContent() {
        if (tipo == null || tipo.isEmpty()) {
            tipo = "faq"; // Si no se proporciona un tipo, se puede cargar un valor por defecto
        }
        contenidos = pageContentService.getContentByTipo(tipo); // Obtener el contenido por el tipo
    }

    // Getter para los contenidos
    public List<Contenido> getContenidos() {
        return contenidos;
    }

    // Setter para los contenidos
    public void setContenidos(List<Contenido> contenidos) {
        this.contenidos = contenidos;
    }

    // Setter y Getter para el tipo de contenido
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
	
}
