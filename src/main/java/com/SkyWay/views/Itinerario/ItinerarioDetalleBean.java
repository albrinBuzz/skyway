package com.SkyWay.views.Itinerario;



import com.SkyWay.dto.VueloDTO;
import com.SkyWay.modules.itinerario.domain.model.Itinerario;
import com.SkyWay.modules.itinerario.domain.service.ItinerarioService;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDTO;
import com.SkyWay.modules.itinerario.presentation.dto.ItinerarioDetalleDTO;
import com.SkyWay.modules.tarifa.presentation.dto.TarifaDTO;
import com.SkyWay.modules.tarifaItinerario.domain.model.ItinerarioTarifa;
import com.SkyWay.modules.tarifaItinerario.domain.service.ItinerarioTarifaService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

@Named("itinerarioDetalleBean")
@ViewScoped
public class ItinerarioDetalleBean implements Serializable {

    private ItinerarioDTO selectedVuelo;

    private String selectedVueloParadas;
    private Integer itinerario;  // Identificador único para cada vuelo
    private List<ItinerarioDTO> vuelosIda;
    private List<ItinerarioDTO> vuelosRegreso;
    private List<ItinerarioDTO> vuelosSeleccionados;
    private HashMap<Integer,Integer>itinerariosTarifas;
    private String salida;
    private String llegada;
    private LocalDate fechaIda;
    private LocalDate fechaRegreso;
    private String tipoViaje; // OW o RT
    private String tipoVuelo;
    private List<ItinerarioDetalleDTO> paradasVuelo;
    private Integer total;
    @Autowired
    private ItinerarioService itinerarioService;
    private Integer cantAdultos;

    @Autowired
    private ItinerarioTarifaService itinerarioTarifaService;
    // Variable para los mensajes de la vista
    private FacesMessage facesMessage;

    @PostConstruct
    public void init() {
        try {
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            Map<String, String> params = externalContext.getRequestParameterMap();

            this.salida = params.get("salida");
            this.llegada = params.get("llegada");
            String fechaIdaStr = params.get("fechaIda");
            String fechaRegresoStr = params.get("fechaRegreso");
            this.tipoViaje = params.getOrDefault("trip", "OW").toUpperCase();
            String adultosStr = params.getOrDefault("adultos", "1");

            cantAdultos = Integer.parseInt(adultosStr);

            if (salida == null || llegada == null || fechaIdaStr == null || salida.isBlank() || llegada.isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Parámetros incompletos", "Debes completar origen, destino y fecha."));
                return;
            }

            if (salida.equalsIgnoreCase(llegada)) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Destino inválido", "El destino no puede ser igual al origen."));
                return;
            }

            this.fechaIda = LocalDate.parse(fechaIdaStr);
            if (fechaIda.isBefore(LocalDate.now())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fecha inválida", "La fecha de ida no puede estar en el pasado."));
                return;
            }

            if ("RT".equals(tipoViaje) && fechaRegresoStr != null) {
                this.fechaRegreso = LocalDate.parse(fechaRegresoStr);
                if (fechaRegreso.isBefore(fechaIda)) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Fechas inválidas", "La fecha de regreso no puede ser antes que la de ida."));
                    return;
                }
            }

            this.tipoVuelo = "RT".equals(tipoViaje) ? " Vuelos Ida" : "Solo Ida";

            vuelosIda = itinerarioService.buscarItinerarios(salida, llegada, fechaIda.toString());
            vuelosIda.sort(Comparator.comparing(ItinerarioDTO::getHoraLlegada24h));

            if ("RT".equals(tipoViaje) && fechaRegreso != null) {
                vuelosRegreso = itinerarioService.buscarItinerarios(llegada, salida, fechaRegreso.toString());
                vuelosRegreso.sort(Comparator.comparing(ItinerarioDTO::getHoraLlegada24h));
            }

            if ((vuelosIda == null || vuelosIda.isEmpty()) &&
                    ("RT".equals(tipoViaje) && (vuelosRegreso == null || vuelosRegreso.isEmpty()))) {
                //addMessage(FacesMessage.SEVERITY_WARN, "Warn Message", "Message Content");
                /*FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                        "No se encontraron vuelos para las fechas seleccionadas, cambie de destino o fecha"));*/

                FacesMessage mensaje = new FacesMessage(FacesMessage.SEVERITY_WARN,
                        "Sin resultados", "No se encontraron vuelos en las fechas seleccionadas.");
                FacesContext.getCurrentInstance().addMessage(null, mensaje);
            }

            vuelosSeleccionados = new ArrayList<>();
            itinerariosTarifas=new HashMap<>();
            total = 0;

        } catch (Exception e) {
            facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Hubo un problema al cargar los vuelos.");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            e.printStackTrace();
        }
    }

    public void showParadas(Integer idItinerario) {

        //this.selectedVueloParadas = "Detalles de paradas: " + vuelo.getCantParadas(); // Aquí puedes colocar más detalles.

        paradasVuelo = itinerarioService.obtenerDetalleItinerario(idItinerario);
        Logger.logInfo(selectedVueloParadas);
        //Logger.logInfo("itinerario. "+vuelo.getItinerario());

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

    public String selectVuelo(ItinerarioDTO vuelo,TarifaDTO tarifa) {

        Logger.logInfo("seleccionar vuelo" + vuelo.toString());
        this.selectedVuelo = vuelo;
        this.total += vuelo.getPrecio();
        this.vuelosSeleccionados.add(vuelo);
        this.itinerariosTarifas.put(vuelo.getItinerario(),tarifa.getIdTarifa());
        if (tipoViaje.equals("RT")){
            this.vuelosIda = vuelosRegreso;
            this.tipoVuelo = "Vuelos Regreso";
            if (vuelosSeleccionados.size()>=2){
                return redireccionar();
            }
        }else {
            return redireccionar();
        }

        return "";

        // Aquí podrías guardar los detalles o proceder con alguna otra acción
    }

    // Método para proceder con la compra (simulación)
    public void procederCompra() {
        // Lógica para proceder con la compra
        // Por ejemplo, redirigir al proceso de pago o mostrar un mensaje de éxito.
    }



    public String redireccionar(){
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Compra confirmada", "Gracias por tu compra."));

        StringBuilder url = new StringBuilder();
        url.append("seleccionAsientos.xhtml?faces-redirect=true&itinerarios=");

        this.itinerariosTarifas.forEach((integer, integer2) -> {

            url.append(integer);
            url.append(",");
        });

        url.delete(url.toString().length() - 1,url.toString().length());
        url.append("&adultos=").append(cantAdultos);


        url.append("&tarifas=");
        this.itinerariosTarifas.forEach((integer, integer2) -> {

            url.append(integer2);
            url.append(",");
        });

        url.delete(url.toString().length() - 1,url.toString().length());
        Logger.logInfo(url.toString());

        return url.toString();
    }

    public List<ItinerarioTarifa> getTarifasPorItinerario(Integer itinerario) {
        return itinerarioTarifaService.findByItinerario(itinerario);

    }
    public List<TarifaDTO> getTarifasItinerario(Integer idItinerario) {
        // Consulta SQL → JSON → Mapear con Jackson/Gson o manualmente
        // Cada tarifa tendrá una lista de características dinámica
        return itinerarioTarifaService.getTarifasItinerario(idItinerario);
    }


    public void seleccionarTarifa(VueloDTO vuelo, ItinerarioTarifa tarifaSeleccionada) {
        //vuelo.setTarifaSeleccionada(tarifaSeleccionada);
        // recalcular total, aplicar impuestos, etc.
    }

    public void onVueloSelect(SelectEvent<ItinerarioDTO> event) {
        this.selectedVuelo = event.getObject();
        // cualquier lógica extra aquí si necesitas
    }




    public Integer getItinerario() {
        return itinerario;
    }

    public void setItinerario(Integer itinerario) {
        this.itinerario = itinerario;
    }


    // Getter

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

    public LocalDate getFechaIda() {
        return fechaIda;
    }

    public LocalDate getFechaRegreso() {
        return fechaRegreso;
    }

    public void setFechaRegreso(LocalDate fechaRegreso) {
        this.fechaRegreso = fechaRegreso;
    }

    public void setFechaIda(LocalDate fechaIda) {
        this.fechaIda = fechaIda;
    }

    public List<ItinerarioDTO> getVuelosIda() {
        return vuelosIda;
    }

    public List<ItinerarioDTO> getVuelosRegreso() {
        return vuelosRegreso;
    }

    public List<ItinerarioDTO> getVuelosSeleccionados() {
        return vuelosSeleccionados;
    }

    public void setVuelosSeleccionados(List<ItinerarioDTO> vuelosSeleccionados) {
        this.vuelosSeleccionados = vuelosSeleccionados;
    }

    public String getTipoViaje() {
        return tipoViaje;
    }

    public void setTipoViaje(String tipoViaje) {
        this.tipoViaje = tipoViaje;
    }

    public String getTipoVuelo() {
        return tipoVuelo;
    }

    public void setTipoVuelo(String tipoVuelo) {
        this.tipoVuelo = tipoVuelo;
    }

    public Integer getTotal() {
        return total;
    }
}
