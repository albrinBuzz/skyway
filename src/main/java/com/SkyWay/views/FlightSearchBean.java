package com.SkyWay.views;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.SkyWay.modules.asiento.presentation.dto.InfoAsientoDTO;

import com.SkyWay.modules.asiento.domain.service.AsientoService;
import com.SkyWay.modules.avion.domain.service.AvionService;
import com.SkyWay.modules.ciudad.domain.model.Ciudad;
import com.SkyWay.modules.ciudad.domain.service.CiudadService;
import com.SkyWay.modules.pasajero.domain.service.PasajeroService;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.modules.vuelo.domain.service.VueloService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import com.SkyWay.dto.InfoVueloDTO;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.validation.constraints.Future;

@Named("flightSearchBean")
@RequestScoped
public class FlightSearchBean {

	
		@Autowired
	    private VueloService vueloService;

		@Autowired
		private AsientoService asientoService;
	
		@Autowired
		private AvionService avionService;
		@Autowired
		private CiudadService ciudadService;
		
		@Autowired
		private PasajeroService pasajeroService;
		
		@Autowired 
		private ReservaService reservaService;
		
		private	List<InfoVueloDTO> vuelos;
		private List<Ciudad>ciudads;
	
		  @ManagedProperty("#{facesContext}")
		    private FacesContext facesContext;

		private List<InfoAsientoDTO> asientoSeleccionados;

	 	@Future

		private List<LocalDate> range;


	 	 // Campos del formulario
	    private String departureCity;
	    private String arrivalCity;

	    private String classType;
	    private boolean isRoundTrip; // Solo Ida o Ida y Vuelta
	    private String airline; // Aerolínea
	    private String priceRange; // Rango de precio
	    private String flightDuration; // Duración del vuelo
	    private String stops; // Escalas

		@PostConstruct
		public void init(){
			range = new ArrayList<>();
			LocalDateTime fechaHora = LocalDateTime.now();
			range.add(fechaHora.toLocalDate());
			range.add(fechaHora.toLocalDate());

			ciudads=ciudadService.getAllCiudades();

		}


	    // Getters y setters para cada campo

	    public String getDepartureCity() {
	        return departureCity;
	    }

	    public void setDepartureCity(String departureCity) {
	        this.departureCity = departureCity;
	    }

	    public String getArrivalCity() {
	        return arrivalCity;
	    }

	    public void setArrivalCity(String arrivalCity) {
	        this.arrivalCity = arrivalCity;
	    }

		public LocalDate getDepartureDate() {
			if (range != null && range.size() > 0) {
				return range.get(0);
			}
			return null;  // or a default value, like LocalDate.now() or something else
		}


		public void setDepartureDate(LocalDate departureDate) {
	        this.range.set(0, departureDate);
	    }

	    public LocalDate getReturnDate() {
	        return range.get(1);
	    }

	    public void setReturnDate(LocalDate returnDate) {
	        this.range.set(1, returnDate);
	    }

	    public String getClassType() {
	        return classType;
	    }

	    public void setClassType(String classType) {
	        this.classType = classType;
	    }

	    public boolean isRoundTrip() {
	        return isRoundTrip;
	    }

	    public void setRoundTrip(boolean isRoundTrip) {
	        this.isRoundTrip = isRoundTrip;
	    }

	    public String getAirline() {
	        return airline;
	    }

	    public void setAirline(String airline) {
	        this.airline = airline;
	    }

	    public String getPriceRange() {
	        return priceRange;
	    }

	    public void setPriceRange(String priceRange) {
	        this.priceRange = priceRange;
	    }

	    public String getFlightDuration() {
	        return flightDuration;
	    }

	    public void setFlightDuration(String flightDuration) {
	        this.flightDuration = flightDuration;
	    }

	    public String getStops() {
	        return stops;
	    }

	    public void setStops(String stops) {
	        this.stops = stops;
	    }

		public List<Ciudad> getCiudads() {
			return ciudads;
		}

		public void setCiudads(List<Ciudad> ciudads) {
			this.ciudads = ciudads;
		}


	// Método para realizar la búsqueda de vuelos
	    public void buscarVuelos() {

			Logger.logInfo(departureCity+" "+arrivalCity+" "+ range.get(0) +" "+ range.get(1));

	    	 //vuelos= vueloService.buscarVuelo(departureCity, arrivalCity,String.valueOf( range.get(0)));
	    	 
	    	 System.out.println(vuelos);
	    	 if (vuelos.size()==0) {
	    		 System.out.println("no encontraron los vuelos");
	    	       FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("No se Encontraron Vuelos En Esas Fechas"));
	    	        PrimeFaces.current().ajax().update("form:messages");
			}

	    }



	    public void reservarVuelo(InfoVueloDTO vuelo) {
	 
	    	 // Guardar el vuelo seleccionado en el contexto de la sesión o como atributo en el bean
	        FacesContext facesContext = FacesContext.getCurrentInstance();
	        ExternalContext externalContext = facesContext.getExternalContext();
	        //externalContext.getSessionMap().put("vueloSeleccionado", vuelo);  // Guardamos el vuelo en la sesión
	        
	        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("vueloSeleccionado", vuelo);
	        
	
	        // Redirigimos a la página de booking
	        //return "home/booking.xhtml?faces-redirect=true";  // Redirección a la página de reserva
	    	
	        // Redirigir a la página destino
	        try {
	            //externalContext.redirect("booking.xhtml");
	            externalContext.redirect(externalContext.getRequestContextPath()
	                    + "/home/booking.xhtml");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public String editAction(String id) {
	  	  //id = "delete"
	    	return id;
	  	}


		public List<LocalDate> getRange() {
			return range;
		}

	public void setRange(List<LocalDate> range) {
		if (range == null) {
			range = new ArrayList<>();  // Initialize if it's null
		}
		this.range = range;
	}

		public List<InfoVueloDTO> getVuelos() {
			return vuelos;
		}

		public void setVuelos(List<InfoVueloDTO> vuelos) {
			this.vuelos = vuelos;
		}
	 	
	 	
	 	
}
