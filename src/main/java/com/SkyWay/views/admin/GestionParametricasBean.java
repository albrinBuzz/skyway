package com.SkyWay.views.admin;

import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;
import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import com.SkyWay.modules.estadoreserva.domain.repository.EstadoReservaRepository;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.estadovuelo.domain.repository.EstadoVueloRepository;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;

import com.SkyWay.modules.metodopago.domain.model.MetodoPagoRepository;
import com.SkyWay.modules.tipoequipaje.domain.model.TipoEquipaje;
import com.SkyWay.modules.tipoequipaje.domain.repository.TipoEquipajeRepository;
import com.SkyWay.modules.tipoturno.domain.model.TipoTurno;
import com.SkyWay.modules.tipoturno.domain.repository.TipoTurnoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.PrimeFaces;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@Named("gestionParametricasBean")
@ViewScoped
public class GestionParametricasBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired private TipoEquipajeRepository tipoEquipajeRepository;
    @Autowired private MetodoPagoRepository metodoPagoRepository;
    @Autowired private EstadoReservaRepository estadoReservaRepository;
    @Autowired private ClaseAsientoRepository claseAsientoRepository;
    @Autowired private EstadoVueloRepository estadoVueloRepository;
    @Autowired private TipoTurnoRepository tipoTurnoRepository;

    private int activeTab = 0;

    private List<TipoEquipaje> listaTipoEquipaje;
    private List<MetodoPago> listaMetodoPago;
    private List<EstadoReserva> listaEstadoReserva;
    private List<ClaseAsiento> listaClaseAsiento;
    private List<EstadoVuelo> listaEstadoVuelo;
    private List<TipoTurno> listaTipoTurno;

    private Integer idEdicion;
    private String nombreForm;
    private String descripcionForm;
    private boolean modoEdicion = false;

    @PostConstruct
    public void init() {
        cargarListas();
    }

    public void cargarListas() {
        this.listaTipoEquipaje = tipoEquipajeRepository.findAll();
        this.listaMetodoPago = metodoPagoRepository.findAll();
        this.listaEstadoReserva = estadoReservaRepository.findAll();
        this.listaClaseAsiento = claseAsientoRepository.findAll();
        this.listaEstadoVuelo = estadoVueloRepository.findAll();
        this.listaTipoTurno = tipoTurnoRepository.findAll();
    }

    public void prepararNuevoRegistro() {
        this.idEdicion = null;
        this.nombreForm = "";
        this.descripcionForm = "";
        this.modoEdicion = false;
        abrirDialogo();
    }

    public void prepararEdicionTipoEquipaje(TipoEquipaje te) {
        this.modoEdicion = true;
        this.idEdicion = te.getIdTipo();
        this.nombreForm = te.getNombre();
        abrirDialogo();
    }

    public void prepararEdicionMetodoPago(MetodoPago mp) {
        this.modoEdicion = true;
        this.idEdicion = mp.getIdMetodoPago();
        this.descripcionForm = mp.getDescripcion();
        abrirDialogo();
    }

    public void prepararEdicionEstadoReserva(EstadoReserva er) {
        this.modoEdicion = true;
        this.idEdicion = er.getIdEstadoReserva();
        this.descripcionForm = er.getDescripcion();
        abrirDialogo();
    }

    public void prepararEdicionClaseAsiento(ClaseAsiento ca) {
        this.modoEdicion = true;
        this.idEdicion = ca.getIdClase();
        this.descripcionForm = ca.getDescripcion();
        abrirDialogo();
    }

    public void prepararEdicionEstadoVuelo(EstadoVuelo ev) {
        this.modoEdicion = true;
        this.idEdicion = ev.getIdEstadoVuelo();
        this.nombreForm = ev.getEstado();
        this.descripcionForm = ev.getDescripcion();
        abrirDialogo();
    }

    public void prepararEdicionTipoTurno(TipoTurno tt) {
        this.modoEdicion = true;
        this.idEdicion = tt.getIdTipo();
        this.nombreForm = tt.getNombre();
        abrirDialogo();
    }

    private void abrirDialogo() {
        PrimeFaces.current().ajax().update("dlgParametrica");
        PrimeFaces.current().executeScript("PF('dlgParametrica').show();");
    }

    public void guardar() {
        try {
            switch (this.activeTab) {
                case 0 -> {
                    TipoEquipaje te = (idEdicion != null) ? tipoEquipajeRepository.findById(idEdicion).orElse(new TipoEquipaje()) : new TipoEquipaje();
                    te.setNombre(this.nombreForm);
                    tipoEquipajeRepository.save(te);
                }
                case 1 -> {
                    MetodoPago mp = (idEdicion != null) ? metodoPagoRepository.findById(idEdicion).orElse(new MetodoPago()) : new MetodoPago();
                    mp.setDescripcion(this.descripcionForm);
                    metodoPagoRepository.save(mp);
                }
                case 2 -> {
                    EstadoReserva er = (idEdicion != null) ? estadoReservaRepository.findById(idEdicion).orElse(new EstadoReserva()) : new EstadoReserva();
                    er.setDescripcion(this.descripcionForm);
                    estadoReservaRepository.save(er);
                }
                case 3 -> {
                    ClaseAsiento ca = (idEdicion != null) ? claseAsientoRepository.findById(idEdicion).orElse(new ClaseAsiento()) : new ClaseAsiento();
                    ca.setDescripcion(this.descripcionForm);
                    claseAsientoRepository.save(ca);
                }
                case 4 -> {
                    EstadoVuelo ev = (idEdicion != null) ? estadoVueloRepository.findById(idEdicion).orElse(new EstadoVuelo()) : new EstadoVuelo();
                    ev.setEstado(this.nombreForm);
                    ev.setDescripcion(this.descripcionForm);
                    estadoVueloRepository.save(ev);
                }
                case 5 -> {
                    TipoTurno tt = (idEdicion != null) ? tipoTurnoRepository.findById(idEdicion).orElse(new TipoTurno()) : new TipoTurno();
                    tt.setNombre(this.nombreForm);
                    tipoTurnoRepository.save(tt);
                }
            }

            cargarListas();
            addMessage("Éxito", "Registro procesado correctamente.");
            PrimeFaces.current().executeScript("PF('dlgParametrica').hide();");
            PrimeFaces.current().ajax().update("mainPanel", "msgs");

        } catch (Exception e) {
            addErrorMessage("Error al guardar", e.getMessage());
        }
    }

    public void eliminarEquipaje(Integer id) {
        try {
            tipoEquipajeRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Tipo de equipaje eliminado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void eliminarMetodoPago(Integer id) {
        try {
            metodoPagoRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Método de pago eliminado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void eliminarEstadoReserva(Integer id) {
        try {
            estadoReservaRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Estado de reserva eliminado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void eliminarClaseAsiento(Integer id) {
        try {
            claseAsientoRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Clase de asiento eliminada.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void eliminarEstadoVuelo(Integer id) {
        try {
            estadoVueloRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Estado de vuelo eliminado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void eliminarTipoTurno(Integer id) {
        try {
            tipoTurnoRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Tipo de turno eliminado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void addErrorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }

    public int getActiveTab() { return activeTab; }
    public void setActiveTab(int activeTab) { this.activeTab = activeTab; }

    public List<TipoEquipaje> getListaTipoEquipaje() { return listaTipoEquipaje; }
    public List<MetodoPago> getListaMetodoPago() { return listaMetodoPago; }
    public List<EstadoReserva> getListaEstadoReserva() { return listaEstadoReserva; }
    public List<ClaseAsiento> getListaClaseAsiento() { return listaClaseAsiento; }
    public List<EstadoVuelo> getListaEstadoVuelo() { return listaEstadoVuelo; }
    public List<TipoTurno> getListaTipoTurno() { return listaTipoTurno; }

    public Integer getIdEdicion() { return idEdicion; }
    public void setIdEdicion(Integer idEdicion) { this.idEdicion = idEdicion; }

    public String getNombreForm() { return nombreForm; }
    public void setNombreForm(String nombreForm) { this.nombreForm = nombreForm; }

    public String getDescripcionForm() { return descripcionForm; }
    public void setDescripcionForm(String descripcionForm) { this.descripcionForm = descripcionForm; }

    public boolean isModoEdicion() { return modoEdicion; }
}