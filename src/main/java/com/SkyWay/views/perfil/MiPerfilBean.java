package com.SkyWay.views.perfil;

import com.SkyWay.modules.rolusuario.domain.model.Role;
import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.UsuarioPerfilService;
import com.SkyWay.modules.usuario.domain.service.UsuarioPerfilService.ResultadoPerfil;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Backing bean JSF para la vista de perfil de usuario, edición de datos personales y cambio de contraseña.
 */
@Component
@Named("miPerfilBean")
@ViewScoped
public class MiPerfilBean implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String VISTA_RESUMEN = "RESUMEN";
    public static final String VISTA_EDITAR = "EDITAR";
    public static final String VISTA_SEGURIDAD = "SEGURIDAD";

    private final transient UsuarioPerfilService perfilService;
    private final transient UserDetailsService userDetailsService;

    private String vistaActual = VISTA_RESUMEN;
    private Usuario usuarioActual;

    // Campos de edición de perfil
    private String nombreEdit;
    private String apellidoEdit;
    private String correoEdit;
    private String telefonoEdit;
    private Date fechaNacimientoEdit;

    // Campos de cambio de contraseña
    private String contrasenaActual;
    private String nuevaContrasena;
    private String confirmarContrasena;

    public MiPerfilBean(UsuarioPerfilService perfilService, UserDetailsService userDetailsService) {
        this.perfilService = perfilService;
        this.userDetailsService = userDetailsService;
    }

    @PostConstruct
    public void init() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc == null || fc.getExternalContext() == null) {
            return;
        }
        recargarUsuarioDesdeSesionOContexto(fc);
        cargarCamposEdicion();
    }

    public void abrirResumen() {
        this.vistaActual = VISTA_RESUMEN;
        cargarCamposEdicion();
        limpiarCamposContrasena();
    }

    public void abrirEditar() {
        cargarCamposEdicion();
        this.vistaActual = VISTA_EDITAR;
    }

    public void abrirSeguridad() {
        limpiarCamposContrasena();
        this.vistaActual = VISTA_SEGURIDAD;
    }

    public void guardarPerfil() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (usuarioActual == null) {
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "Sesión expirada", "Inicia sesión nuevamente para actualizar tu perfil.");
            return;
        }

        String correoPrevio = usuarioActual.getCorreoElectronico();
        ResultadoPerfil resultado = perfilService.actualizarPerfil(
                usuarioActual.getRut(),
                nombreEdit,
                apellidoEdit,
                correoEdit,
                telefonoEdit,
                fechaNacimientoEdit,
                obtenerRequest(fc)
        );

        if (resultado.exitoso() && resultado.usuarioActualizado() != null) {
            this.usuarioActual = resultado.usuarioActualizado();
            sincronizarSesionYSeguridad(fc, this.usuarioActual, correoPrevio);
            cargarCamposEdicion();
            this.vistaActual = VISTA_RESUMEN;
            agregarMensaje(fc, FacesMessage.SEVERITY_INFO, "Perfil actualizado", resultado.mensaje());
        } else {
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "No se pudieron guardar los cambios", resultado.mensaje());
        }
    }

    public void guardarContrasena() {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (usuarioActual == null) {
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "Sesión expirada", "Inicia sesión nuevamente para cambiar tu contraseña.");
            return;
        }

        ResultadoPerfil resultado = perfilService.cambiarContrasena(
                usuarioActual.getRut(),
                contrasenaActual,
                nuevaContrasena,
                confirmarContrasena,
                obtenerRequest(fc)
        );

        limpiarCamposContrasena();
        if (resultado.exitoso() && resultado.usuarioActualizado() != null) {
            this.usuarioActual = resultado.usuarioActualizado();
            sincronizarUsuarioEnSesion(fc, this.usuarioActual);
            this.vistaActual = VISTA_RESUMEN;
            agregarMensaje(fc, FacesMessage.SEVERITY_INFO, "Contraseña actualizada", resultado.mensaje());
        } else {
            agregarMensaje(fc, FacesMessage.SEVERITY_ERROR, "No se pudo cambiar la contraseña", resultado.mensaje());
        }
    }

    public boolean isAutenticado() {
        return usuarioActual != null;
    }

    public boolean isVistaResumen() {
        return VISTA_RESUMEN.equals(vistaActual);
    }

    public boolean isVistaEditar() {
        return VISTA_EDITAR.equals(vistaActual);
    }

    public boolean isVistaSeguridad() {
        return VISTA_SEGURIDAD.equals(vistaActual);
    }

    public String getIniciales() {
        if (usuarioActual == null) {
            return "U";
        }
        String n = StringUtils.hasText(usuarioActual.getNombre()) ? usuarioActual.getNombre().trim().substring(0, 1) : "";
        String a = StringUtils.hasText(usuarioActual.getApellido()) ? usuarioActual.getApellido().trim().substring(0, 1) : "";
        String iniciales = (n + a).toUpperCase(Locale.ROOT);
        return StringUtils.hasText(iniciales) ? iniciales : "U";
    }

    public String getNombreCompleto() {
        if (usuarioActual == null) {
            return "";
        }
        String nombre = usuarioActual.getNombre() != null ? usuarioActual.getNombre().trim() : "";
        String apellido = usuarioActual.getApellido() != null ? usuarioActual.getApellido().trim() : "";
        return (nombre + " " + apellido).trim();
    }

    public Date getFechaMaximaNacimiento() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        return cal.getTime();
    }

    public boolean isTieneRol(String rolBuscado) {
        if (usuarioActual == null || usuarioActual.getRoles() == null || !StringUtils.hasText(rolBuscado)) {
            return false;
        }
        List<Role> roles = usuarioActual.getRoles();
        for (Role rol : roles) {
            if (rol != null && rol.getNombre() != null) {
                String nombre = rol.getNombre().trim();
                if (nombre.equalsIgnoreCase(rolBuscado) || nombre.equalsIgnoreCase("ROLE_" + rolBuscado)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void recargarUsuarioDesdeSesionOContexto(FacesContext fc) {
        ExternalContext ec = fc.getExternalContext();
        HttpSession session = ec.getSession(false) instanceof HttpSession httpSession ? httpSession : null;
        Usuario sesionUsuario = session != null && session.getAttribute("usuario") instanceof Usuario u ? u : null;

        Optional<Usuario> fresco = Optional.empty();
        if (sesionUsuario != null && StringUtils.hasText(sesionUsuario.getRut())) {
            fresco = perfilService.buscarPorRut(sesionUsuario.getRut());
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                fresco = perfilService.buscarPorCorreo(auth.getName());
            }
        }

        if (fresco.isPresent()) {
            this.usuarioActual = fresco.get();
            if (session != null) {
                session.setAttribute("usuario", this.usuarioActual);
            }
        } else {
            this.usuarioActual = sesionUsuario;
        }
    }

    private void sincronizarSesionYSeguridad(FacesContext fc, Usuario actualizado, String correoPrevio) {
        sincronizarUsuarioEnSesion(fc, actualizado);

        String correoNuevo = actualizado.getCorreoElectronico();
        if (!StringUtils.hasText(correoNuevo) || correoNuevo.equalsIgnoreCase(correoPrevio)) {
            return;
        }

        try {
            UserDetails nuevosDetalles = userDetailsService.loadUserByUsername(correoNuevo);
            UsernamePasswordAuthenticationToken nuevaAuth = new UsernamePasswordAuthenticationToken(
                    nuevosDetalles,
                    nuevosDetalles.getPassword(),
                    nuevosDetalles.getAuthorities()
            );
            SecurityContext contexto = SecurityContextHolder.getContext();
            contexto.setAuthentication(nuevaAuth);

            ExternalContext ec = fc != null ? fc.getExternalContext() : null;
            if (ec != null && ec.getSession(false) instanceof HttpSession session) {
                session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, contexto);
            }
        } catch (Exception ignored) {
            // Si el contexto de seguridad ya se refrescó o no aplica, la sesión de usuario ya quedó actualizada.
        }
    }

    private void sincronizarUsuarioEnSesion(FacesContext fc, Usuario actualizado) {
        if (fc == null || fc.getExternalContext() == null) {
            return;
        }
        Object sessionObj = fc.getExternalContext().getSession(false);
        if (sessionObj instanceof HttpSession session) {
            session.setAttribute("usuario", actualizado);
        }
    }

    private void cargarCamposEdicion() {
        if (usuarioActual == null) {
            return;
        }
        this.nombreEdit = usuarioActual.getNombre();
        this.apellidoEdit = usuarioActual.getApellido();
        this.correoEdit = usuarioActual.getCorreoElectronico();
        this.telefonoEdit = usuarioActual.getTelefono();
        this.fechaNacimientoEdit = usuarioActual.getFechaNacimiento();
    }

    private void limpiarCamposContrasena() {
        this.contrasenaActual = null;
        this.nuevaContrasena = null;
        this.confirmarContrasena = null;
    }

    private void agregarMensaje(FacesContext fc, FacesMessage.Severity severidad, String resumen, String detalle) {
        if (fc != null) {
            fc.addMessage(null, new FacesMessage(severidad, resumen, detalle));
        }
    }

    private HttpServletRequest obtenerRequest(FacesContext fc) {
        if (fc == null || fc.getExternalContext() == null) {
            return null;
        }
        Object req = fc.getExternalContext().getRequest();
        return req instanceof HttpServletRequest httpReq ? httpReq : null;
    }

    public String getVistaActual() {
        return vistaActual;
    }

    public void setVistaActual(String vistaActual) {
        this.vistaActual = vistaActual;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public String getNombreEdit() {
        return nombreEdit;
    }

    public void setNombreEdit(String nombreEdit) {
        this.nombreEdit = nombreEdit;
    }

    public String getApellidoEdit() {
        return apellidoEdit;
    }

    public void setApellidoEdit(String apellidoEdit) {
        this.apellidoEdit = apellidoEdit;
    }

    public String getCorreoEdit() {
        return correoEdit;
    }

    public void setCorreoEdit(String correoEdit) {
        this.correoEdit = correoEdit;
    }

    public String getTelefonoEdit() {
        return telefonoEdit;
    }

    public void setTelefonoEdit(String telefonoEdit) {
        this.telefonoEdit = telefonoEdit;
    }

    public Date getFechaNacimientoEdit() {
        return fechaNacimientoEdit;
    }

    public void setFechaNacimientoEdit(Date fechaNacimientoEdit) {
        this.fechaNacimientoEdit = fechaNacimientoEdit;
    }

    public String getContrasenaActual() {
        return contrasenaActual;
    }

    public void setContrasenaActual(String contrasenaActual) {
        this.contrasenaActual = contrasenaActual;
    }

    public String getNuevaContrasena() {
        return nuevaContrasena;
    }

    public void setNuevaContrasena(String nuevaContrasena) {
        this.nuevaContrasena = nuevaContrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }
}
