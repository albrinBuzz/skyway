package com.SkyWay.modules.itinerario.presentation.bean;



import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Named("itinerarioDetalleBean")
@ViewScoped
public class ItinerarioDetalleBean implements Serializable {

    private ItinerarioDTO selectedVuelo;

    private String selectedVueloParadas;
    private Integer itinerario;  // Identificador único para cada vuelo
    private List<ItinerarioDTO> vuelos;
    private List<ItinerarioDTO>vuelosSeleccionados;
    private String salida;
    private String llegada;
    private LocalDate fecha;
    private List<ItinerarioDetalleDTO>paradasVuelo;
    private Integer total;
    @Autowired
    private ItinerarioService itinerarioService;


    // Variable para los mensajes de la vista
    private FacesMessage facesMessage;

    @PostConstruct
    public void init() throws ParseException {

        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            // Obtener los parámetros de la URL
            this.salida = externalContext.getRequestParameterMap().get("salida");
            this.llegada = externalContext.getRequestParameterMap().get("llegada");
            String fechaParam = externalContext.getRequestParameterMap().get("fecha");


            // Convertir el parámetro de fecha a LocalDate
            // Validar que todos los parámetros estén presentes
            if (salida == null || llegada == null || fechaParam == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Los parámetros 'salida', 'llegada' y 'fecha' son requeridos", ""));
                return;
            }

            // Convertir la fecha solo si está presente
            this.fecha = LocalDate.parse(fechaParam);
            vuelos = itinerarioService.buscarItinerarios(salida, llegada, fecha.toString());
            // Validación si no se encontraron vuelos
            if (vuelos == null || vuelos.isEmpty()) {
                facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia", "No se encontraron vuelos para los parámetros seleccionados.");
                FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            }
            this.total=0;
            vuelosSeleccionados=new ArrayList<>();

        } catch (Exception e) {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al cargar los vuelos.");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            e.printStackTrace();
        }
    }

    public void showParadas(ItinerarioDTO vuelo) {

        this.selectedVueloParadas = "Detalles de paradas: " + vuelo.getCantParadas(); // Aquí puedes colocar más detalles.
        paradasVuelo=itinerarioService.obtenerDetalleItinerario(vuelo.getItinerario());
        Logger.logInfo(selectedVueloParadas);

    }

    public void setSelectedVuelo(ItinerarioDTO selectedVuelo) {
        this.selectedVuelo = selectedVuelo;
    }

    public ItinerarioDTO getSelectedVuelo() {
        return selectedVuelo;
    }

    public String getSelectedVueloParadas() {
        return selectedVueloParadas;
    }
    public void selectVuelo(ItinerarioDTO vuelo) {
        Logger.logInfo("seleccionar vuelo"+vuelo.toString());
        this.selectedVuelo = vuelo;
        this.total+=  vuelo.getPrecio();
        this.vuelosSeleccionados.add(vuelo);
        // Aquí podrías guardar los detalles o proceder con alguna otra acción
    }

    // Método para proceder con la compra (simulación)
    public void procederCompra() {
        // Lógica para proceder con la compra
        // Por ejemplo, redirigir al proceso de pago o mostrar un mensaje de éxito.
    }
    public String confirmarCompra() {
        if (vuelosSeleccionados == null || vuelosSeleccionados.isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "No hay vuelos seleccionados", "Agrega al menos un vuelo antes de confirmar."));
            //return;
        }

        // Aquí puedes procesar la compra o redirigir al resumen/finalizar
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Compra confirmada", "Gracias por tu compra."));

        // (Opcional) Limpiar la selección después de confirmar
        //vuelosSeleccionados.clear();
        //total = 0;
        StringBuilder url=new StringBuilder();
        url.append("seleccionAsientos.xhtml?faces-redirect=true&itinerarios=");
        for (ItinerarioDTO vuelosSeleccionado : vuelosSeleccionados) {

            url.append(vuelosSeleccionado.getItinerario());
            url.append(",");
        }

        Logger.logInfo(url.substring(0,url.toString().length()-1));

        return url.substring(0,url.toString().length()-1);


        ///seleccionAsientos.xhtml?itinerarios=1001,1002
        //return "seleccionAsientos.xhtml?faces-redirect=true&itinerarios=1001,1002";
    }


    public Integer getItinerario() {
        return itinerario;
    }

    public void setItinerario(Integer itinerario) {
        this.itinerario = itinerario;
    }


    // Getter
    public List<ItinerarioDTO> getVuelos() {
        return vuelos;
    }

    // Setter
    public void setVuelos(List<ItinerarioDTO> vuelos) {
        this.vuelos = vuelos;
    }

    public List<ItinerarioDetalleDTO> getParadasVuelo() {
        return paradasVuelo;
    }

    public String getDetallesDeParadas() {
        return "Detalles de paradas: " + selectedVuelo.getCantParadas();
    }

    // Otros métodos para manejar más detalles del vuelo si es necesario, como obtener la duración, el precio, etc.

    public String getDuracion() {
        return selectedVuelo.getDuracion();
    }

    public Integer getPrecio() {
        return selectedVuelo.getPrecio();
    }

    public String getHoraSalida() {
        return selectedVuelo.getHoraSalida24h();
    }

    public String getHoraLlegada() {
        return selectedVuelo.getHoraLlegada24h();
    }

    public String getSalida() {
        return salida;
    }

    public void setSalida(String salida) {
        this.salida = salida;
    }

    public String getLlegada() {
        return llegada;
    }

    public void setLlegada(String llegada) {
        this.llegada = llegada;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public List<ItinerarioDTO> getVuelosSeleccionados() {
        return vuelosSeleccionados;
    }

    public void setVuelosSeleccionados(List<ItinerarioDTO> vuelosSeleccionados) {
        this.vuelosSeleccionados = vuelosSeleccionados;
    }

    public Integer getTotal() {
        return total;
    }
}
