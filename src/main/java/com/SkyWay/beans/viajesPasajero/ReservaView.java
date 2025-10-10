package com.SkyWay.beans.viajesPasajero;

import java.io.IOException;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import org.springframework.beans.factory.annotation.Autowired;

import com.SkyWay.dto.BoletoDTO;

import com.SkyWay.modules.reserva.domain.service.ReservaService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("viajePsView")
@RequestScoped
public class ReservaView {

	Reserva reserva;
	
	private String productId;
	  
	BoletoDTO boleto;
	
	@Autowired
	private ReservaService reservaService;
	
	@PostConstruct
	public void init() {

		 String idReserva = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("productId");
		 
		 if (idReserva!=null) {
			 reserva=reservaService.findById(Integer.parseInt(idReserva)).get();
			 
			 System.out.println(reserva.toString());
	     		
		}
	
	}
	
	public void cancelarReserva(Integer idReserva) {
		
		
		
		reservaService.cancelarReserva(idReserva);
		
		
		  	FacesContext facesContext = FacesContext.getCurrentInstance();
	        ExternalContext externalContext = facesContext.getExternalContext();

	        try {

	            externalContext.redirect(externalContext.getRequestContextPath()
	                    + "/perfil/pasajero/template.xhtml");
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		
		
	}
	
	public void verDetalle(Reserva reserva) {
	
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
    
        
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("reserva", reserva);
        
        try {
            //externalContext.redirect("booking.xhtml");
            externalContext.redirect(externalContext.getRequestContextPath()
                    + "/perfil/pasajero/detalleReserva.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	public void cambiarAsiento(Integer idVuelo ,Integer idReserva) {
		
		System.out.println("reserva :"+idReserva+" Vuelo: "+idVuelo);
		
	}
	public Reserva getReserva() {
		return reserva;
	}
	
	public void setReserva(Reserva reserva) {
		this.reserva = reserva;
	}
	 public String getProductId() {
	        return productId;
	    }

	    public void setProductId(String productId) {
	        this.productId = productId;
	    }

}
