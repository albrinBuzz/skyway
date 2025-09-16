package com.SkyWay.exception;

import com.SkyWay.util.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import jakarta.faces.application.ViewExpiredException;

@Component
public class CustomViewExpiredExceptionResolver extends AbstractHandlerExceptionResolver {

    @Override
    protected ModelAndView doResolveException(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        if (ex instanceof ViewExpiredException) {
            Logger.logInfo("dentro del expecion de la expiracion de la sesion");
            // Aquí rediriges a la página de login
            return new ModelAndView("redirect:/login.xhtml");
        }
        return null; // Deja que otros manejadores de excepciones manejen el error
    }
}
