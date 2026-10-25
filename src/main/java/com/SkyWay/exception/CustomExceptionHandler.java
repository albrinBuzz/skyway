package com.SkyWay.exception;

import com.SkyWay.util.Logger;
import jakarta.faces.FacesException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import java.time.LocalDate;
import java.util.Iterator;

public class CustomExceptionHandler extends ExceptionHandlerWrapper {

    private final ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();

        while (iterator.hasNext()) {
            ExceptionQueuedEvent event = iterator.next();
            Exception exception = (Exception) event.getContext().getException();

            // Detectar si la excepción es un ViewExpiredException global
            if (exception instanceof ViewExpiredException) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                ExternalContext externalContext = facesContext.getExternalContext();

                try {
                    // Log opcional para control interno
   
                    Logger.logInfo("⚠️ [GlobalExceptionHandler] Vista expirada detectada. Redirigiendo al inicio...");

                    // Redirigir de manera limpia al index o página principal
                    externalContext.redirect(externalContext.getRequestContextPath() + "/home/index.xhtml");
                    facesContext.responseComplete();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    iterator.remove();
                }
            }
        }
        // Delegar el resto de excepciones al manejador por defecto
        getWrapped().handle();
    }
}