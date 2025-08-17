package com.SkyWay.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.SkyWay.modules.notificacion.domain.model.Notificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import com.SkyWay.model.Usuario;
import com.SkyWay.modules.notificacion.domain.repository.NotificacionRepository;
import com.SkyWay.modules.notificacion.domain.service.NotificacionService;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

@Named("userBean")
@RequestScoped
public class UserBean implements Serializable {

    private String name;
    private boolean loggedIn = false;
    private boolean isAuthenticated = false;
    private boolean isAdmin = false;  // Nueva propiedad para comprobar si el usuario es admin
    private String rol;
    private Usuario usuario;
    
    @Autowired
    private  NotificacionService notificacionService;
  
    private List<Notificacion> notifications = new ArrayList<>();
    private List<Notificacion> unreadNotifications = new ArrayList<>();

    // Constructor
    public UserBean(HttpSession session,NotificacionRepository notificacionRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            this.loggedIn = true;
            this.isAuthenticated = true;
            
            try {
            	this.usuario = (Usuario) session.getAttribute("usuario");
            	this.name = usuario.getNombre();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // Comprobamos si el usuario tiene el rol de administrador
            rol = authentication.getAuthorities().toArray()[0].toString();
            this.isAdmin = authentication.getAuthorities().stream()
                                         .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

           // notifications=notificacionRepository.findByRut(usuario.getRutUsuario());
            // Filtrar las notificaciones no leídas
            /*this.unreadNotifications = notifications.stream()
                    .filter(notif -> !notif.getLeida())
                    .collect(Collectors.toList());*/
        }
    }
    
    @PostConstruct
    

    // Getter y setter para 'name' y 'loggedIn'
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    // Obtener el número total de notificaciones
    public int getNotificationsCount() {
        return notifications.size();
    }

    // Obtener el número de notificaciones no leídas
    public int getUnreadNotificationsCount() {
        return unreadNotifications.size();
    }

    // Obtener todas las notificaciones
    public List<Notificacion> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notificacion> notifications) {
        this.notifications = notifications;
    }
    
    // Obtener notificaciones no leídas
    public List<Notificacion> getUnreadNotifications() {
        return unreadNotifications;
    }

    // Marcar una notificación como leída
    public void markAsRead(int notificationId) {

       
       var notificacion=  notifications.stream()
        .filter(n -> n.getIdNotificacion() == notificationId)
        .findFirst()
       .orElse(null);
        
       if (notificacion!=null) {
		
    	   notificacionService.marcarComoLeida(notificacion.getIdNotificacion());
    	   
    	   /*notifications=notificacionService.findByRut(usuario.getRutUsuario());
           // Filtrar las notificaciones no leídas
           this.unreadNotifications = notifications.stream()
                   .filter(notif -> !notif.getLeido())
                   .collect(Collectors.toList());*/
    	   
       }
       
       
        
   
       
       
    }

    // Método para cerrar sesión
    public String logout() {
        SecurityContextHolder.clearContext();  // Limpiar el contexto de seguridad
        this.loggedIn = false;
        this.isAuthenticated = false;
        return "index?faces-redirect=true";  // Redirigir a la página de inicio
    }
}
