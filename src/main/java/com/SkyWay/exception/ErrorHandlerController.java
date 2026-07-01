package com.SkyWay.exception;

import ch.qos.logback.core.model.Model;
import com.SkyWay.util.Logger;
import jakarta.faces.application.ViewExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class ErrorHandlerController {



    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {


        Logger.logInfo("entro en la expecion genererica"+ex.getMessage());
        //ex.printStackTrace();

        return "redirect:/";
    }

    @ExceptionHandler(ViewExpiredException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleViewExpiredException(ViewExpiredException ex, Model model) {
        Logger.logInfo("La vista ha expirado: " + ex.getMessage());
        //model.addAttribute("errorMessage", "La sesión ha expirado. Por favor, inicie sesión nuevamente.");
        return "/"; // Página de error personalizada
    }
}
