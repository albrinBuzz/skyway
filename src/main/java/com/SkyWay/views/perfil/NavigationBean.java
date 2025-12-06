package com.SkyWay.views.perfil;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.faces.context.FacesContext;

@Named
@RequestScoped
public class NavigationBean {

    public String getCurrentView() {
        String viewId = FacesContext.getCurrentInstance()
                .getViewRoot()
                .getViewId();
        return viewId;
    }

    public boolean isActive(String view) {
        return getCurrentView().contains(view);
    }
    public boolean active(String view) {
        return getCurrentView().contains(view);
    }
}