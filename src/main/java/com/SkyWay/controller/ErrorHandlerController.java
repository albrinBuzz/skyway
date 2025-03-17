package com.SkyWay.controller;

import jakarta.faces.application.ViewExpiredException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorHandlerController {



    @ExceptionHandler(Exception.class)
    public String handleException(Exception ex) {


        System.out.println("entro en la expecion genererica"+ex.getMessage());
        //ex.printStackTrace();

        return "redirect:/";
    }
}
