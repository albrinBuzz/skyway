package com.SkyWay.mobile.controller;

import com.SkyWay.mobile.dto.MobileDtos.ApiError;
import com.SkyWay.mobile.service.MobileException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.*;

@RestControllerAdvice(basePackages="com.SkyWay.mobile.controller")
@Order(0)
public class MobileExceptionHandler {
    private final Clock clock;
    public MobileExceptionHandler(Clock clock) { this.clock=clock; }
    @ExceptionHandler(MobileException.class)
    ResponseEntity<ApiError> business(MobileException ex) { return error(ex.getStatus(),ex.getCodigo(),ex.getMessage()); }
    @ExceptionHandler({MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> invalid(Exception ex) { return error(HttpStatus.BAD_REQUEST,"DATOS_INVALIDOS","Revisa los datos enviados."); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> conflict(Exception ex) { return error(HttpStatus.CONFLICT,"CONFLICTO","La disponibilidad cambió. Actualiza e inténtalo nuevamente."); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception ex) {
        org.slf4j.LoggerFactory.getLogger(getClass()).error("Error interno de API móvil: {}",ex.getClass().getSimpleName());
        return error(HttpStatus.INTERNAL_SERVER_ERROR,"ERROR_INTERNO","No se pudo completar la operación.");
    }
    private ResponseEntity<ApiError> error(HttpStatus status,String code,String message) {
        return ResponseEntity.status(status).body(new ApiError(code,message,LocalDateTime.now(clock)));
    }
}
