package com.SkyWay.views;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import com.SkyWay.modules.usuario.domain.service.UsuarioService;
import com.SkyWay.util.Logger;
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

@Named
@RequestScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UsuarioService usuarioService;

    private Usuario usuario;
    private String confirmarContrasena;
    private String tipoDocumento = "CEDULA";
    private String nacionalidad = "Chilena";

    public UsuarioBean() {
        usuario = new Usuario();
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

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    @Transactional
    public void registrar() {
        Logger.logInfo("Iniciando proceso de registro para RUT: " + usuario.getRut());

        if (!usuario.getContrasena().equals(confirmarContrasena)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de Validación", "Las contraseñas no coinciden."));
            return;
        }

        try {
            if (isCorreoExistente(usuario.getCorreoElectronico())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "El correo electrónico ya se encuentra registrado."));
                return;
            }

            // Ejecución del Upsert que maneja acompañantes previos
            usuarioService.crearPasajeroConDetalle(usuario, tipoDocumento, nacionalidad);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "¡Registro Exitoso!", "Tu cuenta ha sido registrada correctamente."));

            // Limpieza del formulario
            usuario = new Usuario();
            confirmarContrasena = "";

        } catch (Exception e) {
            Logger.logError("Error al registrar usuario: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Hubo un problema al procesar el registro."));
        }
    }

    private boolean isCorreoExistente(String correo) {
        try {
            long count = entityManager.createQuery("SELECT COUNT(u) FROM Usuario u WHERE u.correoElectronico = :correo AND u.contrasena IS NOT NULL", Long.class)
                    .setParameter("correo", correo)
                    .getSingleResult();
            return count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}