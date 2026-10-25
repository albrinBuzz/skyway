package com.SkyWay.controller;

import com.SkyWay.util.Logger;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ModelAndView handleError(HttpServletRequest request) {
        Object statusCodeObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object requestUriObj = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        Object exceptionObj = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        int statusCode = (statusCodeObj != null) ? Integer.parseInt(statusCodeObj.toString()) : 500;
        String requestUri = (requestUriObj != null) ? requestUriObj.toString() : "Desconocida";

        // 📊 AUDITORÍA Y OBSERVABILIDAD EN LOGS
        Logger.logError(String.format("🚨 [ERROR-CONTROLLER] Código HTTP: %s | Ruta afectada: %s", statusCode, requestUri));

        if (exceptionObj instanceof Throwable throwable) {
            Logger.logError("🔍 [STACKTRACE CAUSA RAIZ]: " + throwable.getMessage());
        }

        ModelAndView modelAndView = new ModelAndView();

        // Si es un 404 limpio, redirigir a la vista específica de página no encontrada
        if (statusCode == 404) {
            modelAndView.setViewName("redirect:/home/404.xhtml");
        } else {
            // Para cualquier otro fallo (500, etc.), mostrar la vista general de "Ups"
            modelAndView.setViewName("redirect:/home/error.xhtml");
        }

        return modelAndView;
    }
}