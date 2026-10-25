package com.SkyWay.exception;

import com.SkyWay.util.Logger;
import jakarta.faces.application.ViewExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ErrorHandlerController {

    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {
        Logger.logError("💥 [GLOBAL EXCEPTION] Excepción no controlada capturada: " + ex.getClass().getName() + " -> " + ex.getMessage());
        // Opcional: imprimir traza completa para depuración profunda en desarrollo
        // ex.printStackTrace();

        return "redirect:/home/error.xhtml";
    }

    @ExceptionHandler(ViewExpiredException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleViewExpiredException(ViewExpiredException ex) {
        Logger.logWarn("⚠️ [VIEW EXPIRED] La vista JSF ha caducado: " + ex.getMessage());
        return "redirect:/home/index.xhtml";
    }
}