package com.SkyWay.config.pago;

/** Resultado de iniciar una transaccion: URL de redireccion e id externo creado por la pasarela. */
public record InicioPago(String urlRedireccion, String idExterno) {}
