package com.SkyWay.views.reserva;

import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import com.SkyWay.modules.tipoequipaje.domain.service.TipoEquipajeService;
import com.SkyWay.util.Logger;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Named("equipajeBean")
@ViewScoped
public class EquipajeBean implements Serializable {

    // Límites máximos por pasajero
    private static final int MAX_CABINA = 1;
    private static final int MAX_BODEGA = 3; // Máximo total de maletas facturadas (15kg + 23kg)

    @Autowired
    private TipoEquipajeService tipoEquipajeService;

    private List<ReservaAsientoBean.Pasajero> pasajerosSesion;
    private List<TipoEquipaje>tipoEquipajes;
    private List<EquipajePasajeroDTO> equipajePorPasajero;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) return;


        ExternalContext ec = fc.getExternalContext();
        Map<String, Object> sessionMap = ec.getSessionMap();


        this.pasajerosSesion = (List<ReservaAsientoBean.Pasajero>) sessionMap.get("pasajeros");

        if (this.pasajerosSesion == null || this.pasajerosSesion.isEmpty()) {
            Logger.logWarn("[EquipajeBean] No se encontraron pasajeros en la sesión.");
            return;
        }

        if (sessionMap.containsKey("equipajePorPasajero")) {
            this.equipajePorPasajero = (List<EquipajePasajeroDTO>) sessionMap.get("equipajePorPasajero");
        } else {
            this.equipajePorPasajero = new ArrayList<>();
            for (int i = 0; i < pasajerosSesion.size(); i++) {
                ReservaAsientoBean.Pasajero p = pasajerosSesion.get(i);
                this.equipajePorPasajero.add(new EquipajePasajeroDTO(i, p.getNombre()));
            }
        }

        tipoEquipajes=tipoEquipajeService.listarTodos();
    }

    public void agregarEquipaje(int pasajeroIndex, String tipo, String descripcion, int precio, int peso, String dimensiones) {
        if (pasajeroIndex < 0 || pasajeroIndex >= equipajePorPasajero.size()) return;

        EquipajePasajeroDTO dto = equipajePorPasajero.get(pasajeroIndex);

        // Validación 1: Equipaje de cabina (Máximo 1)
        if ("CABINA".equals(tipo) && contarEquipajePorTipo(dto, "CABINA") >= MAX_CABINA) {
            addMessage(FacesMessage.SEVERITY_WARN, "Límite alcanzado",
                    "Solo se permite un máximo de " + MAX_CABINA + " equipaje de cabina por pasajero.");
            return;
        }

        // Validación 2: Equipaje de bodega (Máximo 3 en total entre 15kg y 23kg)
        if (tipo.startsWith("BODEGA") && contarEquipajeBodega(dto) >= MAX_BODEGA) {
            addMessage(FacesMessage.SEVERITY_WARN, "Límite alcanzado",
                    "Solo se permite un máximo de " + MAX_BODEGA + " maletas de bodega por pasajero.");
            return;
        }

        // Creación de ItemEquipaje asignando los nuevos atributos recibidos
        EquipajePasajeroDTO.ItemEquipaje item = new EquipajePasajeroDTO.ItemEquipaje(tipo, descripcion, precio);
        item.setPeso(BigDecimal.valueOf(peso));
        item.setDimensiones(dimensiones);

        // Mapeo dinámico para la FK de BD según el tipo
        if ("CABINA".equals(tipo)) {
            item.setIdTipo(1);
        } else if ("BODEGA_15KG".equals(tipo)) {
            item.setIdTipo(2);
        } else if ("BODEGA_23KG".equals(tipo)) {
            item.setIdTipo(3);
        }

        dto.getMaletas().add(item);

        addMessage(FacesMessage.SEVERITY_INFO, "Equipaje Añadido",
                descripcion + " agregado a " + dto.getNombrePasajero());
    }

    public void eliminarEquipaje(int pasajeroIndex, EquipajePasajeroDTO.ItemEquipaje item) {
        if (pasajeroIndex >= 0 && pasajeroIndex < equipajePorPasajero.size()) {
            equipajePorPasajero.get(pasajeroIndex).getMaletas().remove(item);
            addMessage(FacesMessage.SEVERITY_INFO, "Equipaje Eliminado", item.getDescripcion() + " removido.");
        }
    }

    // Métodos auxiliares para conteo de límites
    public long contarEquipajePorTipo(EquipajePasajeroDTO dto, String tipo) {
        return dto.getMaletas().stream()
                .filter(m -> m.getTipo().equals(tipo))
                .count();
    }

    public long contarEquipajeBodega(EquipajePasajeroDTO dto) {
        return dto.getMaletas().stream()
                .filter(m -> m.getTipo().startsWith("BODEGA"))
                .count();
    }

    public boolean puedeAgregarCabina(EquipajePasajeroDTO dto) {
        return contarEquipajePorTipo(dto, "CABINA") < MAX_CABINA;
    }

    public boolean puedeAgregarBodega(EquipajePasajeroDTO dto) {
        return contarEquipajeBodega(dto) < MAX_BODEGA;
    }

    public int getTotalEquipaje() {
        if (equipajePorPasajero == null) return 0;
        return equipajePorPasajero.stream()
                .flatMap(p -> p.getMaletas().stream())
                .mapToInt(EquipajePasajeroDTO.ItemEquipaje::getPrecio)
                .sum();
    }

    public void continuarACheckout() {
        try {
            ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
            ec.getSessionMap().put("equipajePorPasajero", equipajePorPasajero);
            ec.getSessionMap().put("totalEquipaje", getTotalEquipaje());

            Logger.logInfo("[EquipajeBean] Redirigiendo a /home/reserva.xhtml");
            ec.redirect("/home/reserva.xhtml");
        } catch (IOException e) {
            Logger.logError("Error al redirigir al checkout: " + e.getMessage());
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

    public List<EquipajePasajeroDTO> getEquipajePorPasajero() { return equipajePorPasajero; }
    public List<ReservaAsientoBean.Pasajero> getPasajerosSesion() { return pasajerosSesion; }

    public List<TipoEquipaje> getTipoEquipajes() {
        return tipoEquipajes;
    }
}