package com.SkyWay.views.admin;

import com.SkyWay.modules.pasajero.domain.model.Pasajero;
import com.SkyWay.modules.piloto.domain.model.Piloto;
import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.tripulacion.domain.model.Tripulacion;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.GestionUsuariosAdminService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
//import org.omnifaces.cdi.ViewScoped;

import jakarta.faces.view.ViewScoped;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component("gestionUsuariosAdminBean")
@ViewScoped
public class GestionUsuariosAdminBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Autowired
    private GestionUsuariosAdminService adminService;

    private List<Usuario> listaUsuarios;
    private List<Role> listaRolesGlobales;

    private Usuario usuarioSeleccionado;
    private List<Integer> idsRolesSeleccionados;
    private String contrasenaAuxiliar; // Para manejar cambios de clave opcionales

    // Sub-perfiles
    private boolean esPiloto;
    private Piloto pilotoData;

    private boolean esTripulacion;
    private Tripulacion tripulacionData;

    private boolean esPasajero;
    private Pasajero pasajeroData;

    private boolean esNuevo;

    @PostConstruct
    public void init() {
        cargarUsuarios();
        cargarRoles();
    }

    public void cargarUsuarios() {
        this.listaUsuarios = adminService.listarTodosConRoles();
    }

    public void cargarRoles() {
        this.listaRolesGlobales = adminService.listarTodosLosRoles();
    }

    public void prepararNuevoUsuario() {
        this.esNuevo = true;
        this.usuarioSeleccionado = new Usuario();
        this.idsRolesSeleccionados = new ArrayList<>();
        this.contrasenaAuxiliar = "";

        this.esPiloto = false;
        this.pilotoData = new Piloto();

        this.esTripulacion = false;
        this.tripulacionData = new Tripulacion();

        this.esPasajero = true; // Por defecto Pasajero
        this.pasajeroData = new Pasajero();
    }

    public void prepararEdicion(Usuario usr) {
        this.esNuevo = false;
        this.usuarioSeleccionado = adminService.buscarPorRut(usr.getRut()).orElse(usr);
        this.contrasenaAuxiliar = ""; // Dejar vacío si no se desea cambiar

        this.idsRolesSeleccionados = new ArrayList<>();
        if (this.usuarioSeleccionado.getRoles() != null) {
            for (Role r : this.usuarioSeleccionado.getRoles()) {
                this.idsRolesSeleccionados.add(r.getIdRol());
            }
        }

        // Cargar Piloto
        Optional<Piloto> pOpt = adminService.buscarPilotoPorRut(usr.getRut());
        this.esPiloto = pOpt.isPresent();
        this.pilotoData = pOpt.orElse(new Piloto());

        // Cargar Tripulación
        Optional<Tripulacion> tOpt = adminService.buscarTripulacionPorRut(usr.getRut());
        this.esTripulacion = tOpt.isPresent();
        this.tripulacionData = tOpt.orElse(new Tripulacion());

        // Cargar Pasajero
        Optional<Pasajero> pasOpt = adminService.buscarPasajeroPorRut(usr.getRut());
        this.esPasajero = pasOpt.isPresent();
        this.pasajeroData = pasOpt.orElse(new Pasajero());
    }

    public void guardarUsuario() {
        if (usuarioSeleccionado.getRut() == null || usuarioSeleccionado.getRut().isBlank()) {
            addMessage(FacesMessage.SEVERITY_WARN, "Validación", "El RUT del usuario es obligatorio.");
            return;
        }

        try {
            List<Role> rolesAAsignar = new ArrayList<>();
            if (idsRolesSeleccionados != null) {
                for (Role r : listaRolesGlobales) {
                    if (idsRolesSeleccionados.contains(r.getIdRol())) {
                        rolesAAsignar.add(r);
                    }
                }
            }

            adminService.guardarUsuarioCompleto(
                    usuarioSeleccionado, rolesAAsignar, contrasenaAuxiliar,
                    esPiloto, pilotoData,
                    esTripulacion, tripulacionData,
                    esPasajero, pasajeroData
            );

            addMessage(FacesMessage.SEVERITY_INFO, "Éxito",
                    esNuevo ? "Usuario creado exitosamente." : "Usuario actualizado correctamente.");

            cargarUsuarios();
            org.primefaces.PrimeFaces.current().executeScript("PF('dlgUsuario').hide();");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar el usuario: " + e.getMessage());
        }
    }

    public void eliminarUsuario(Usuario usr) {
        try {
            adminService.eliminarUsuarioCompleto(usr.getRut());
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Usuario eliminado del sistema.");
            cargarUsuarios();
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el usuario.");
        }
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // Getters y Setters
    public List<Usuario> getListaUsuarios() { return listaUsuarios; }
    public List<Role> getListaRolesGlobales() { return listaRolesGlobales; }
    public Usuario getUsuarioSeleccionado() { return usuarioSeleccionado; }
    public void setUsuarioSeleccionado(Usuario usuarioSeleccionado) { this.usuarioSeleccionado = usuarioSeleccionado; }
    public List<Integer> getIdsRolesSeleccionados() { return idsRolesSeleccionados; }
    public void setIdsRolesSeleccionados(List<Integer> idsRolesSeleccionados) { this.idsRolesSeleccionados = idsRolesSeleccionados; }
    public String getContrasenaAuxiliar() { return contrasenaAuxiliar; }
    public void setContrasenaAuxiliar(String contrasenaAuxiliar) { this.contrasenaAuxiliar = contrasenaAuxiliar; }
    public boolean isEsPiloto() { return esPiloto; }
    public void setEsPiloto(boolean esPiloto) { this.esPiloto = esPiloto; }
    public Piloto getPilotoData() { return pilotoData; }
    public void setPilotoData(Piloto pilotoData) { this.pilotoData = pilotoData; }
    public boolean isEsTripulacion() { return esTripulacion; }
    public void setEsTripulacion(boolean esTripulacion) { this.esTripulacion = esTripulacion; }
    public Tripulacion getTripulacionData() { return tripulacionData; }
    public void setTripulacionData(Tripulacion tripulacionData) { this.tripulacionData = tripulacionData; }
    public boolean isEsPasajero() { return esPasajero; }
    public void setEsPasajero(boolean esPasajero) { this.esPasajero = esPasajero; }
    public Pasajero getPasajeroData() { return pasajeroData; }
    public void setPasajeroData(Pasajero pasajeroData) { this.pasajeroData = pasajeroData; }
    public boolean isEsNuevo() { return esNuevo; }
}