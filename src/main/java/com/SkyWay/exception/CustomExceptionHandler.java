package com.SkyWay.exception;

import com.SkyWay.util.Logger;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;

import java.io.IOException;
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
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable exception = context.getException();

            // Usamos un método auxiliar con nombre propio para evitar colisión con el framework
            Throwable rootCause = findRootCause(exception);

            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext == null) {
                continue;
            }

            ExternalContext externalContext = facesContext.getExternalContext();

            try {
                if (rootCause instanceof ViewExpiredException) {
                    Logger.logWarn("⚠️ [JSF] Vista expirada detectada. Redirigiendo a home...");
                    redirect(facesContext, externalContext, "/home/index.xhtml");
                } else {
                    Logger.logError("💥 [JSF EXCEPTION] Causa raíz: " + rootCause.getClass().getName() + " -> " + rootCause.getMessage());
                    redirect(facesContext, externalContext, "/home/error.xhtml");
                }
            } catch (Exception e) {
                Logger.logError("Error al procesar el manejador global de JSF: " + e.getMessage());
            } finally {
                iterator.remove(); // Evita bucles infinitos marcando la excepción como procesada
            }
        }

        getWrapped().handle();
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private void redirect(FacesContext facesContext, ExternalContext externalContext, String page) throws IOException {
        String path = externalContext.getRequestContextPath() + page;

        if (externalContext.isResponseCommitted()) {
            return;
        }

        // Si la llamada fue AJAX vía PrimeFaces/JSF
        if (facesContext.getPartialViewContext().isAjaxRequest()) {
            externalContext.getFlash().setKeepMessages(true);
            externalContext.redirect(path);
        } else {
            externalContext.redirect(path);
        }
        facesContext.responseComplete();
    }
}