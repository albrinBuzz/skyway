package com.SkyWay.exception;


import com.SkyWay.util.Logger;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.application.Application;
import jakarta.faces.event.ExceptionQueuedEvent;

import java.io.IOException;
import java.util.Iterator;


public class FacesExceptionHandler extends ExceptionHandlerWrapper {

    private ExceptionHandler wrapped;

    public FacesExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    @Override
    public void handle()  {

        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext(); ) {
            ExceptionQueuedEvent event = i.next();
            Throwable exception = event.getContext().getException();

            if (exception instanceof ViewExpiredException) {
                Logger.logInfo("error de la expiracion de la vista");
                FacesContext facesContext = FacesContext.getCurrentInstance();
                // Aquí puedes redirigir a la página deseada o hacer lo que necesites
                try {
                    facesContext.getExternalContext().redirect("/login.xhtml"); // Redirige al login, por ejemplo
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                i.remove();
            }
        }

        getWrapped().handle();
    }
}
