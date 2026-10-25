package com.SkyWay.exception;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

public class CustomExceptionHandlerFactory extends ExceptionHandlerFactory {

    private final ExceptionHandlerFactory wrapped;

    public CustomExceptionHandlerFactory(ExceptionHandlerFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new CustomExceptionHandler(wrapped.getExceptionHandler());
    }
}