package com.SkyWay.views.reserva;

import com.SkyWay.modules.reserva.domain.model.Reserva;
import com.SkyWay.modules.reserva.domain.service.ReservaService;
import com.SkyWay.util.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

@Named("pagoExitosoController")
@ViewScoped
public class PagoExitosoController implements Serializable {
    private static final long serialVersionUID = 1L;

    @Autowired
    private ReservaService reservaService;

    private Reserva reserva;
    private String pnr;

    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext == null || facesContext.getExternalContext() == null) return;

        Map<String, String> params = facesContext.getExternalContext().getRequestParameterMap();
        String idReservaParam = params.get("idReserva");

        if (idReservaParam != null && !idReservaParam.isBlank()) {
            try {
                Integer idReserva = Integer.parseInt(idReservaParam.trim());

                // Guardar/actualizar el PNR en la base de datos y obtener la reserva persistida
                this.reserva = reservaService.guardarOGenerarPnr(idReserva);

                Logger.logInfo(reserva.toString());
                if (this.reserva != null) {
                    this.pnr = this.reserva.getCodigoReserva();
                } else {
                    Logger.logWarn("No se encontró reserva para idReserva: " + idReserva);
                }
            } catch (NumberFormatException e) {
                Logger.logError("Parámetro idReserva inválido: " + idReservaParam);
            } catch (Exception e) {
                Logger.logError("Error al cargar comprobante de pago exitoso: " + e.getMessage());
            }
        }
    }

    public Reserva getReserva() { return reserva; }
    public String getPnr() { return pnr; }
}