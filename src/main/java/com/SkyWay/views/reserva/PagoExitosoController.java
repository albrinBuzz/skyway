package com.SkyWay.views.reserva;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.io.Serializable;

@Component
@RequestScope
public class PagoExitosoController implements Serializable {

    @Autowired
    private ReservaService reservaService;

    private Reserva reserva;
    private String pnr;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null || facesContext.getExternalContext() == null) return;

        String idReservaParam = facesContext.getExternalContext().getRequestParameterMap().get("idReserva");

        if (idReservaParam != null && !idReservaParam.isBlank()) {
            try {
                Integer idReserva = Integer.parseInt(idReservaParam);

                // Cargar reserva persistida desde la BD
                this.reserva = reservaService.findById(idReserva).orElse(null);

                if (this.reserva != null) {
                    // PNR/Código representativo de la reserva
                    this.pnr = "SKW-" + String.format("%06d", this.reserva.getIdReserva());
                }
            } catch (NumberFormatException e) {
                // Manejo de parámetro inválido
            }
        }
    }

    public Reserva getReserva() { return reserva; }
    public String getPnr() { return pnr; }
}