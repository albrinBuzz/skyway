package com.SkyWay.modules.usuario.domain.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Servicio de dominio para la recuperación y restablecimiento seguro de contraseñas
 * mediante tokens criptográficos de un solo uso persistidos en base de datos.
 */
public interface RecuperacionContrasenaService {

    /**
     * Resultado de una operación de recuperación de contraseña.
     *
     * @param exitoso indica si la operación finalizó sin errores de validación o regla de negocio
     * @param mensaje mensaje informativo o de error seguro para mostrar al usuario
     * @param segundosEsperaReenvio segundos restantes antes de poder solicitar un nuevo token
     */
    record ResultadoOperacion(boolean exitoso, String mensaje, long segundosEsperaReenvio) {
        public static ResultadoOperacion ok(String mensaje) {
            return new ResultadoOperacion(true, mensaje, 0L);
        }

        public static ResultadoOperacion ok(String mensaje, long segundosEsperaReenvio) {
            return new ResultadoOperacion(true, mensaje, Math.max(0L, segundosEsperaReenvio));
        }

        public static ResultadoOperacion error(String mensaje) {
            return new ResultadoOperacion(false, mensaje, 0L);
        }

        public static ResultadoOperacion espera(String mensaje, long segundosEsperaReenvio) {
            return new ResultadoOperacion(false, mensaje, Math.max(1L, segundosEsperaReenvio));
        }
    }

    /**
     * Genera un token criptográfico de 256 bits, guarda su hash SHA-256 en base de datos
     * y envía el enlace/token de recuperación al correo si existe un usuario asociado.
     *
     * @param correo correo electrónico de la cuenta
     * @param urlBaseApp URL base de la aplicación web para construir el enlace seguro
     * @param request petición HTTP actual para auditoría y trazabilidad IP (puede ser {@code null})
     * @return resultado neutro de la solicitud
     */
    ResultadoOperacion solicitarRecuperacion(String correo, String urlBaseApp, HttpServletRequest request);

    /**
     * Valida en base de datos si el token existe, no ha sido usado ni revocado y continúa vigente.
     *
     * @param token token recibido en el enlace o ingresado manualmente
     * @return resultado de vigencia
     */
    ResultadoOperacion validarToken(String token);

    /**
     * Valida el token en base de datos con bloqueo transaccional, actualiza la contraseña
     * y marca el token como consumido de un solo uso.
     *
     * @param token token criptográfico de recuperación
     * @param nuevaContrasena nueva contraseña en texto plano
     * @param confirmarContrasena confirmación de la nueva contraseña
     * @param request petición HTTP actual para auditoría y trazabilidad IP (puede ser {@code null})
     * @return resultado de la operación
     */
    ResultadoOperacion restablecerContrasena(
            String token,
            String nuevaContrasena,
            String confirmarContrasena,
            HttpServletRequest request
    );
}
