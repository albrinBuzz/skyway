package com.SkyWay.views.perfil.pasajero;


import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.reservaasiento.domain.service.ReservaAsientoService;
import com.SkyWay.modules.vuelo.domain.model.Vuelo;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
//import jakarta.faces.view.ViewScoped;
import org.omnifaces.cdi.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Named("cambioBean")
@ViewScoped
public class CambioAsientoBean {
    @Autowired
    private VueloService vueloService;

    @Autowired
    private AsientoService asientoService;

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ReservaAsientoService reservaAsientoService;

    private String idVuelo;
    private String idReserva;

    private List<InfoAsientoDTO> asientos;
    private List<InfoAsientoDTO> asientoSeleccionados;
    private List<InfoAsientoDTO>asientosFiltrados;
    private ArrayList<InfoAsientoDTO> asientosSeleccionados;
    private HashMap<Integer, InfoAsientoDTO> asientosCambio;
    //private InfoVueloDTO vuelo;
    private Vuelo vuelo;

    private InfoAsientoDTO asientoActual;


    // Almacena asientos particionados en grupos
    private List<List<InfoAsientoDTO>> asientosParticionados;
    @PostConstruct
    public void init() {
        asientosCambio = new HashMap<>();
        asientos = new ArrayList<>();
        asientoSeleccionados = new ArrayList<>();
        asientosParticionados = new ArrayList<>();

        // ✅ SEGURIDAD: Solo ejecutar si existe una petición web/JSF activa
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getExternalContext() != null) {
            Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
            idVuelo = params.get("idVuelo");
            idReserva = params.get("idReserva");

            if (idVuelo != null && !idVuelo.isEmpty() && idReserva != null && !idReserva.isEmpty()) {
                vuelo = vueloService.findById(Integer.parseInt(idVuelo)).orElse(null);

                if (vuelo != null) {
                    asientos = asientoService.getAsientosVuelo(Integer.parseInt(idReserva), Integer.parseInt(idVuelo));

                    if (asientos != null) {
                        asientoSeleccionados = asientos.stream()
                                .filter(asiento -> "seleccionado".equals(asiento.getEstado()))
                                .toList();

                        for (int i = 0; i < asientos.size(); i += 6) {
                            asientosParticionados.add(asientos.subList(i, Math.min(i + 6, asientos.size())));
                        }
                    }
                }
            }
        }
    }



    public void reservarAsiento(InfoAsientoDTO asiento) {

        System.out.println(asiento);
    }


    // Getters y setters

    public String[] getCustomerStatus() {
        List<String> statuses = List.of("Económica", "Ejecutiva", "Primera Clase");
        return statuses.toArray(new String[statuses.size()]);
    }

    public void abrirDistribucion() {
        PrimeFaces.current().executeScript("PF('modalAsientos').show();");
    }

    public void cambiarAsiento(InfoAsientoDTO asiento) {

        this.asientoActual=asiento;
        PrimeFaces.current().executeScript("PF('modalAsientos').show();");
    }

    public void setearAsiento(InfoAsientoDTO asiento) {

        asientosCambio.put(asientoActual.getIdAsiento(), asiento);

    }

    public void confirmar(String s) {

        System.out.println("Confirmado los cambis");

        for (Map.Entry<Integer, InfoAsientoDTO> entry : asientosCambio.entrySet()) {
            Integer key = entry.getKey();
            InfoAsientoDTO val = entry.getValue();

            //System.out.println(asientoOrg+"-"+val);
            var asientoOrg=	asientos.stream().filter(asiento -> asiento.getIdAsiento()==key).findFirst().get();


            System.out.println(asientoOrg+"  :-:  "+val);
            reservaAsientoService.cambiarAsiento(val.getIdAsiento(), Integer.parseInt (idReserva),asientoOrg.getIdAsiento());

            // System.out.println("Salida : " + reservaAsientoService.cambiarAsiento(val.getIdAsiento(), Integer.parseInt (idReserva),asientoOrg.getIdAsiento()));

        }
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Cambio exitoso", "Los asientos fueron cambiados exitosamente");
        FacesContext.getCurrentInstance().addMessage(null, message);

    }

    public String getCambio(Integer idAsiento) {
        // Obtener el valor del HashMap de forma segura usando Optional
        Optional<InfoAsientoDTO> asientoOptional = Optional.ofNullable(asientosCambio.get(idAsiento));

        // Si el valor está presente, obtenemos el número de asiento, si no, devolvemos una cadena vacía
        return asientoOptional.map(InfoAsientoDTO::getNumeroAsiento).orElse("");
    }

    public Vuelo getVuelo() {
        return vuelo;
    }

    public void setVuelo(Vuelo vuelo) {
        this.vuelo = vuelo;
    }

    public String getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(String idReserva) {
        this.idReserva = idReserva;
    }

    public String getIdVuelo() {
        return idVuelo;
    }

    public void setIdVuelo(String idVuelo) {
        this.idVuelo = idVuelo;
    }

    public List<InfoAsientoDTO> getAsientos() {
        return asientos;
    }

    public void setAsientos(List<InfoAsientoDTO> asientos) {
        this.asientos = asientos;
    }

    public List<InfoAsientoDTO> getAsientoSeleccionados() {
        return asientoSeleccionados;
    }

    public void setAsientoSeleccionados(List<InfoAsientoDTO> asientoSeleccionados) {
        this.asientoSeleccionados = asientoSeleccionados;
    }

    public List<List<InfoAsientoDTO>> getAsientosParticionados() {
        return asientosParticionados;
    }

    public void setAsientosParticionados(List<List<InfoAsientoDTO>> asientosParticionados) {
        this.asientosParticionados = asientosParticionados;
    }

    public List<InfoAsientoDTO> getAsientosFiltrados() {
        return asientosFiltrados;
    }
    public void setAsientosFiltrados(List<InfoAsientoDTO> asientosFiltrados) {
        this.asientosFiltrados = asientosFiltrados;
    }

    public ArrayList<InfoAsientoDTO> getAsientosSeleccionados() {
        return asientosSeleccionados;
    }

    public void setAsientosSeleccionados(ArrayList<InfoAsientoDTO> asientosSeleccionados) {
        this.asientosSeleccionados = asientosSeleccionados;
    }

    public InfoAsientoDTO getAsientoActual() {
        return asientoActual;
    }


    public void setAsientoActual(InfoAsientoDTO asientoActual) {
        this.asientoActual = asientoActual;
    }

}
