package com.SkyWay.views.admin;

import com.SkyWay.modules.claseasiento.domain.model.ClaseAsiento;
import com.SkyWay.modules.claseasiento.domain.repository.ClaseAsientoRepository;
import com.SkyWay.modules.estadoreserva.domain.model.EstadoReserva;
import com.SkyWay.modules.estadoreserva.domain.repository.EstadoReservaRepository;
import com.SkyWay.modules.estadovuelo.domain.model.EstadoVuelo;
import com.SkyWay.modules.estadovuelo.domain.repository.EstadoVueloRepository;
import com.SkyWay.modules.metodopago.domain.model.MetodoPago;

import com.SkyWay.modules.metodopago.domain.repository.MetodoPagoRepository;
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
import org.springframework.jdbc.core.JdbcTemplate;

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
    @Autowired(required = false) private transient JdbcTemplate jdbcTemplate;

    private int activeTab = 0;

    // Listas Administrativas (Traen TODO: Activos e Inactivos)
    private List<TipoEquipaje> listaTipoEquipajeAdmin;
    private List<ClaseAsiento> listaClaseAsientoAdmin;
    private List<TipoTurno> listaTipoTurnoAdmin;

    // Listas Activas (Solo Activos para dropdowns y uso operacional)
    private List<TipoEquipaje> listaTipoEquipajeActivos;
    private List<ClaseAsiento> listaClaseAsientoActivos;
    private List<TipoTurno> listaTipoTurnoActivos;

    // Listas de Estados y Métodos de Pago
    private List<MetodoPago> listaMetodoPago;
    private List<EstadoReserva> listaEstadoReserva;
    private List<EstadoVuelo> listaEstadoVuelo;

    private Integer idEdicion;
    private String nombreForm;
    private String descripcionForm;
    private boolean modoEdicion = false;

    @PostConstruct
    public void init() {
        cargarListas();
    }

    public void cargarListas() {
        // Listas administrativas completas
        this.listaTipoEquipajeAdmin = tipoEquipajeRepository.findAllAdmin();
        this.listaClaseAsientoAdmin = claseAsientoRepository.findAllAdmin();
        this.listaTipoTurnoAdmin = tipoTurnoRepository.findAllAdmin();

        // Listas de sólo activos
        this.listaTipoEquipajeActivos = tipoEquipajeRepository.findByActivoTrue();
        this.listaClaseAsientoActivos = claseAsientoRepository.findByActivoTrueOrderByDescripcionAsc();
        this.listaTipoTurnoActivos = tipoTurnoRepository.findByActivoTrue();

        // Estados y transaccionales
        this.listaMetodoPago = metodoPagoRepository.findAll();
        this.listaEstadoReserva = estadoReservaRepository.findAll();
        this.listaEstadoVuelo = estadoVueloRepository.findAll();
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

    private void sincronizarSecuencia(String secuencia, String tabla, String columnaPk) {
        if (jdbcTemplate == null) return;
        try {
            jdbcTemplate.queryForObject(
                    "SELECT setval('" + secuencia + "', GREATEST(COALESCE((SELECT MAX(" + columnaPk + ") FROM " + tabla + "), 0), 1), (SELECT COUNT(*) > 0 FROM " + tabla + "))",
                    Long.class
            );
        } catch (Exception ignored) {
        }
    }

    public void guardar() {
        try {
            String nombreLimpio = this.nombreForm != null ? this.nombreForm.trim() : "";
            String descLimpia = this.descripcionForm != null ? this.descripcionForm.trim() : "";

            switch (this.activeTab) {
                case 0 -> {
                    if (idEdicion == null) sincronizarSecuencia("tipo_equipaje_seq", "tipo_equipaje", "id_tipo");
                    TipoEquipaje te = (idEdicion != null) ? tipoEquipajeRepository.findById(idEdicion).orElse(new TipoEquipaje()) : new TipoEquipaje();
                    te.setNombre(nombreLimpio);
                    if (te.getActivo() == null) te.setActivo(true);
                    tipoEquipajeRepository.save(te);
                }
                case 1 -> {
                    if (idEdicion == null) sincronizarSecuencia("metodo_pago_seq", "metodo_pago", "id_metodo_pago");
                    MetodoPago mp = (idEdicion != null) ? metodoPagoRepository.findById(idEdicion).orElse(new MetodoPago()) : new MetodoPago();
                    mp.setDescripcion(descLimpia);
                    metodoPagoRepository.save(mp);
                }
                case 2 -> {
                    if (idEdicion == null) sincronizarSecuencia("estado_reserva_seq", "estado_reserva", "id_estado_reserva");
                    EstadoReserva er = (idEdicion != null) ? estadoReservaRepository.findById(idEdicion).orElse(new EstadoReserva()) : new EstadoReserva();
                    er.setDescripcion(descLimpia);
                    estadoReservaRepository.save(er);
                }
                case 3 -> {
                    if (idEdicion == null) sincronizarSecuencia("clase_asiento_seq", "clase_asiento", "id_clase");
                    ClaseAsiento ca = (idEdicion != null) ? claseAsientoRepository.findById(idEdicion).orElse(new ClaseAsiento()) : new ClaseAsiento();
                    ca.setDescripcion(descLimpia);
                    if (ca.getActivo() == null) ca.setActivo(true);
                    claseAsientoRepository.save(ca);
                }
                case 4 -> {
                    if (idEdicion == null) sincronizarSecuencia("estado_vuelo_seq", "estado_vuelo", "id_estado_vuelo");
                    EstadoVuelo ev = (idEdicion != null) ? estadoVueloRepository.findById(idEdicion).orElse(new EstadoVuelo()) : new EstadoVuelo();
                    ev.setEstado(nombreLimpio);
                    ev.setDescripcion(descLimpia);
                    estadoVueloRepository.save(ev);
                }
                case 5 -> {
                    if (idEdicion == null) sincronizarSecuencia("tipo_turno_seq", "tipo_turno", "id_tipo");
                    TipoTurno tt = (idEdicion != null) ? tipoTurnoRepository.findById(idEdicion).orElse(new TipoTurno()) : new TipoTurno();
                    tt.setNombre(nombreLimpio);
                    if (tt.getActivo() == null) tt.setActivo(true);
                    tipoTurnoRepository.save(tt);
                }
            }

            cargarListas();
            addMessage("Éxito", "Registro procesado correctamente.");
            PrimeFaces.current().executeScript("PF('dlgParametrica').hide();");
            PrimeFaces.current().ajax().update("mainPanel", "msgs");

        } catch (Exception e) {
            addErrorMessage("Error al guardar", e.getMessage());
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc != null) {
                fc.validationFailed();
            }
            PrimeFaces.current().ajax().update("msgs");
        }
    }

    public void eliminarEquipaje(Integer id) {
        try {
            tipoEquipajeRepository.deleteById(id);
            cargarListas();
            addMessage("Éxito", "Tipo de equipaje desactivado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void reactivarEquipaje(Integer id) {
        try {
            tipoEquipajeRepository.reactivarById(id);
            cargarListas();
            addMessage("Éxito", "Tipo de equipaje reactivado.");
        } catch (Exception e) { addErrorMessage("Error", "No se pudo reactivar: " + e.getMessage()); }
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
            addMessage("Éxito", "Clase de asiento desactivada.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void reactivarClaseAsiento(Integer id) {
        try {
            claseAsientoRepository.reactivarById(id);
            cargarListas();
            addMessage("Éxito", "Clase de asiento reactivada.");
        } catch (Exception e) { addErrorMessage("Error", "No se pudo reactivar: " + e.getMessage()); }
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
            addMessage("Éxito", "Tipo de turno desactivado.");
        } catch (Exception e) { addErrorMessage("Error", "No se puede eliminar: registro en uso."); }
    }

    public void reactivarTipoTurno(Integer id) {
        try {
            tipoTurnoRepository.reactivarById(id);
            cargarListas();
            addMessage("Éxito", "Tipo de turno reactivado.");
        } catch (Exception e) { addErrorMessage("Error", "No se pudo reactivar: " + e.getMessage()); }
    }

    private void addMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

    private void addErrorMessage(String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, detail));
    }

    public int getActiveTab() { return activeTab; }
    public void setActiveTab(int activeTab) { this.activeTab = activeTab; }

    // Getters Administrativos
    public List<TipoEquipaje> getListaTipoEquipajeAdmin() { return listaTipoEquipajeAdmin; }
    public List<ClaseAsiento> getListaClaseAsientoAdmin() { return listaClaseAsientoAdmin; }
    public List<TipoTurno> getListaTipoTurnoAdmin() { return listaTipoTurnoAdmin; }

    // Getters Activos
    public List<TipoEquipaje> getListaTipoEquipajeActivos() { return listaTipoEquipajeActivos; }
    public List<ClaseAsiento> getListaClaseAsientoActivos() { return listaClaseAsientoActivos; }
    public List<TipoTurno> getListaTipoTurnoActivos() { return listaTipoTurnoActivos; }

    // Compatibilidad
    public List<TipoEquipaje> getListaTipoEquipaje() { return listaTipoEquipajeAdmin; }
    public List<ClaseAsiento> getListaClaseAsiento() { return listaClaseAsientoAdmin; }
    public List<TipoTurno> getListaTipoTurno() { return listaTipoTurnoAdmin; }
    public List<MetodoPago> getListaMetodoPago() { return listaMetodoPago; }
    public List<EstadoReserva> getListaEstadoReserva() { return listaEstadoReserva; }
    public List<EstadoVuelo> getListaEstadoVuelo() { return listaEstadoVuelo; }

    public Integer getIdEdicion() { return idEdicion; }
    public void setIdEdicion(Integer idEdicion) { this.idEdicion = idEdicion; }

    public String getNombreForm() { return nombreForm; }
    public void setNombreForm(String nombreForm) { this.nombreForm = nombreForm; }

    public String getDescripcionForm() { return descripcionForm; }
    public void setDescripcionForm(String descripcionForm) { this.descripcionForm = descripcionForm; }

    public boolean isModoEdicion() { return modoEdicion; }
}