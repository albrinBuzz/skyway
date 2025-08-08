package com.SkyWay.views;


import com.SkyWay.model.Rol;
import com.SkyWay.model.Usuario;

import com.SkyWay.service.RolService;
import com.SkyWay.service.UsuarioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Date;
import java.util.Optional;

@Named
@RequestScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RolService rolService;


    private Usuario usuario;
    private String confirmarContrasena;

    public UsuarioBean() {
        usuario = new Usuario(); // Inicializar el objeto Usuario
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }

    @Transactional
    public String registrar() {
        // Validación de las contraseñas
        if (!usuario.getContrasena().equals(confirmarContrasena)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Las contraseñas no coinciden."));
            return null;
        }

        try {
            // Validar que el correo electrónico sea único
            if (isCorreoExistente(usuario.getCorreoElectronico())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo electrónico ya está registrado."));
                return null;
            }

            // Asignar un rol al usuario (puede ser un valor por defecto)
            Optional<Rol> rol = rolService.getRoleById(1); // Ejemplo: rol de usuario regular
            usuario.setRol(rol.get());

            // Guardar el usuario en la base de datos
            //entityManager.persist(usuario);
            usuarioService.save(usuario);

            // Mensaje de éxito con detalles
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "¡Registro exitoso!", "Tu cuenta ha sido creada correctamente."));

            // Limpiar los campos del formulario
            usuario = new Usuario();

            // Redirigir a la página de inicio de sesión
            return "login?faces-redirect=true";

        } catch (Exception e) {
            // Manejo de excepciones
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al registrar el usuario."));
            return null;
        }
    }

    private boolean isCorreoExistente(String correoElectronico) {
        try {
            long count = entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.correoElectronico = :correo", Long.class)
                    .setParameter("correo", correoElectronico)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isValidRut(String rut) {
        return rut != null && !rut.isEmpty();
    }

    public boolean isEdadValida(Date fechaNacimiento) {
        long edad = (new Date().getTime() - fechaNacimiento.getTime()) / (1000L * 60 * 60 * 24 * 365);
        return edad >= 18;
    }
}