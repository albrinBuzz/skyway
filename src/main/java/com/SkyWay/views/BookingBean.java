package com.SkyWay.views;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.FilterMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.SkyWay.dto.InfoAsientoDTO;
import com.SkyWay.dto.InfoVueloDTO;
import com.SkyWay.model.EstadoReserva;
import com.SkyWay.model.Pasajero;
import com.SkyWay.model.Reserva;
import com.SkyWay.model.ReservaAsiento;
import com.SkyWay.model.Usuario;
import com.SkyWay.service.AsientoService;
import com.SkyWay.service.AvionService;
import com.SkyWay.service.PasajeroService;
import com.SkyWay.service.ReservaService;
import com.SkyWay.service.VueloService;

import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;


@Named("bookingBean")
@ViewScoped
public class BookingBean implements Serializable{

	private final Logger LOGGER = LoggerFactory.getLogger(BookingBean.class);
	

    private InfoVueloDTO vueloSeleccionado;
    private boolean reservaConfirmada;
    private List<InfoAsientoDTO>asientos;
	
    private List<InfoAsientoDTO>asientosFiltrados;
    private ArrayList<InfoAsientoDTO>asientosSeleccionados;

    private List<FilterMeta> filterBy;

    private boolean globalFilterOnly;

    
	@Autowired
	private AsientoService asientoService;
	
	@Autowired
    private VueloService vueloService;

	@Autowired
	private AvionService avionService;
	
	@Autowired
	private PasajeroService pasajeroService;
	
	@Autowired 
	private ReservaService reservaService;
	
	@Autowired
	private HttpSession session;

	private int totalReserva=0;

	private boolean value2;
	
	
    public BookingBean() {

    }
    

    @PostConstruct
    public void init() {
    	
    	asientosSeleccionados=new ArrayList<InfoAsientoDTO>();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        
        // Obtener el vuelo seleccionado desde la sesión
        vueloSeleccionado = (InfoVueloDTO) externalContext.getSessionMap().get("vueloSeleccionado");
        
        if (vueloSeleccionado != null) {
            // Si se encontró el vuelo, obtener los asientos
            System.out.println("Vuelo seleccionado: " + vueloSeleccionado);
            asientos = asientoService.getAsientosDisponibles(vueloSeleccionado.getIdAvion(), vueloSeleccionado.getIdVuelo());
            
            // Verificar si los asientos se cargaron correctamente
            System.out.println("Número de asientos cargados: " + (asientos != null ? asientos.size() : 0));
        } else {
            // Si no se encuentra el vuelo, redirigir o manejar el error
            System.out.println("No se encontró el vuelo seleccionado en la sesión.");
        }
    }

    
    
    // Método para confirmar la reserva
    public String confirmarReserva() {
        // Lógica para procesar la reserva (por ejemplo, guardar en la base de datos)
        reservaConfirmada = true; // Se confirma la reserva}
   
        LOGGER.info("Vuelo {} asientos {} ",vueloSeleccionado,asientos);
        
		  
		  ArrayList<ReservaAsiento>asientos=new ArrayList<ReservaAsiento>();
		  Reserva reserva=new Reserva();
		  ReservaAsiento reservaAsiento;
		  
		  for (int i = 0; i < asientosSeleccionados.size(); i++) {
			  var	asiento= asientoService.findById(asientosSeleccionados.get(i).getIdAsiento());
			  reservaAsiento=new ReservaAsiento();
			  reservaAsiento.setAsiento(asiento.get());
			  reservaAsiento.setReserva(reserva);
			  asientos.add(reservaAsiento);
		  }
		 
		  
		  		/*if (asientosSeleccionados==null||asientosSeleccionados.length==0 ) {
			model.addAttribute("mensaje", "Por favor, selecciona al menos un asiento.");
			return "home/booking"; // Regresar a la vista de reserva
		}*/
		  
		  

		var pasajero= (Usuario) session.getAttribute("usuario");
		
		if (pasajero instanceof Pasajero) {
			LOGGER.info("Informacion del pasajero {} ", pasajero);
			reserva.setPasajero((Pasajero) pasajero);
			reserva.setVuelo(vueloService.findById(vueloSeleccionado.getIdVuelo()).get());
			
			//LOGGER.info("Informacion del pasajero {} ",informacionPasjero);
			
	
			
//			LOGGER.info("Informacion del vuelo {} Avion {}",vueloService.findById(idVuelo).get());
			
			//asientos.forEach(System.out::println);
			
			EstadoReserva esadoReserva=new EstadoReserva();
			esadoReserva.setIdEstadoReserva(1);
			reserva.setEstadoReservaBean(esadoReserva);
			reserva.setReservaAsientos(asientos);
			reserva.setFechaReserva(new Timestamp(new Date().getTime()));
			reservaService.save(reserva);
			 FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Message", "Reserva lista");

		     PrimeFaces.current().dialog().showMessageDynamic(message);
		}

        
        return "reservaConfirmada.xhtml?faces-redirect=true"; // Redirige a la página de confirmación
    }

    public void onRowSelect(SelectEvent<InfoAsientoDTO> event) {
        InfoAsientoDTO asiento = event.getObject();
        if (!asiento.getEstado().equals("ocupado") && !asientosSeleccionados.contains(asiento)) {
            asientosSeleccionados.add(asiento);
            totalReserva += asiento.getPrecio();
        }
    }

    public void onRowUnselect(UnselectEvent<InfoAsientoDTO> event) {
        InfoAsientoDTO asiento = event.getObject();
        if (asientosSeleccionados.contains(asiento)) {
            asientosSeleccionados.remove(asiento);
            totalReserva -= asiento.getPrecio();
        }
    }


    public InfoVueloDTO getVueloSeleccionado() {
		return vueloSeleccionado;
	}
    
    public void setVueloSeleccionado(InfoVueloDTO vueloSeleccionado) {
		this.vueloSeleccionado = vueloSeleccionado;
	}

    public boolean isReservaConfirmada() {
        return reservaConfirmada;
    }

    public void setReservaConfirmada(boolean reservaConfirmada) {
        this.reservaConfirmada = reservaConfirmada;
    }
	
    public List<InfoAsientoDTO> getAsientos() {
		return asientos;
	}
    
    public void setAsientos(List<InfoAsientoDTO> asientos) {
		this.asientos = asientos;
	}
    
    public List<FilterMeta> getFilterBy() {
        return filterBy;
    }

    
    public boolean isGlobalFilterOnly() {
        return globalFilterOnly;
    }

    public void setGlobalFilterOnly(boolean globalFilterOnly) {
        this.globalFilterOnly = globalFilterOnly;
    }
    
    public String[] getCustomerStatus() {
        List<String> statuses = List.of("Económica", "Ejecutiva", "Primera Clase");
        return statuses.toArray(new String[statuses.size()]);
    }
    
    public List<InfoAsientoDTO> getAsientosFiltrados() {
		return asientosFiltrados;
	}
    
    public void setAsientosFiltrados(List<InfoAsientoDTO> asientosFiltrados) {
		this.asientosFiltrados = asientosFiltrados;
	}

    public List<InfoAsientoDTO> getAsientosSeleccionados() {
		return asientosSeleccionados;
	}
    public void setAsientoService(AsientoService asientoService) {
		this.asientoService = asientoService;
	}
    
    public void setAsientosSeleccionados(ArrayList<InfoAsientoDTO> asientosSeleccionados) {
		this.asientosSeleccionados = asientosSeleccionados;
	}
    
    public int getTotalReserva() {
		return totalReserva;
	}
    
    public void setTotalReserva(int totalReserva) {
		this.totalReserva = totalReserva;
	}
    
    public void reservarAsiento(InfoAsientoDTO asiento) {
        if (!asiento.getEstado().equals("ocupado")) {
            if (asientosSeleccionados.contains(asiento)) {
                // Si el asiento ya está seleccionado, lo deseleccionamos
                asientosSeleccionados.remove(asiento);
                totalReserva -= asiento.getPrecio(); // Restar el precio al total
                
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Asientos Eliminados"));
                PrimeFaces.current().ajax().update("form:messages", "form:dt-asientos");
                PrimeFaces.current().executeScript("PF('dtAsientos').clearFilters()");
                
            } else {
                // Si no está seleccionado, lo agregamos a la lista
                asientosSeleccionados.add(asiento);
                totalReserva += asiento.getPrecio(); // Sumar el precio al total

                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Asiento Agregado"));
                PrimeFaces.current().executeScript("PF('manageAsientoDialog').hide()");
                PrimeFaces.current().ajax().update("form:messages", "form:dt-asientos");
               
            }
                
             
            }

    }

}


