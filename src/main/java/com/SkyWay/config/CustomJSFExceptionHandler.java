package com.SkyWay.config;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import java.util.Iterator;

public class CustomJSFExceptionHandler extends ExceptionHandlerWrapper {
    private final ExceptionHandler wrapped;

    public CustomJSFExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() { return wrapped; }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
        while (i.hasNext()) {
            ExceptionQueuedEvent event = i.next();
            Throwable t = event.getContext().getException();

            FacesContext fc = FacesContext.getCurrentInstance();
            try {
                // Previene el "response committed" redirigiendo de forma segura
                if (!fc.getResponseComplete()) {
                    fc.getExternalContext().redirect(fc.getExternalContext().getRequestContextPath() + "/home/index.xhtml");
                    fc.responseComplete();
                }
            } catch (Exception e) {
                // Silenciar o logear
            } finally {
                i.remove();
            }
        }
        getWrapped().handle();
    }
}