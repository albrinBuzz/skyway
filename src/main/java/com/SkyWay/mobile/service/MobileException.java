package com.SkyWay.mobile.service;

import org.springframework.http.HttpStatus;

public class MobileException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;
    public MobileException(HttpStatus status, String codigo, String message) {
        super(message); this.status = status; this.codigo = codigo;
    }
    public HttpStatus getStatus() { return status; }
    public String getCodigo() { return codigo; }
    public static MobileException notFound() {
        return new MobileException(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Recurso no encontrado.");
    }
    public static MobileException conflict(String message) {
        return new MobileException(HttpStatus.CONFLICT, "OPERACION_NO_DISPONIBLE", message);
    }
}
