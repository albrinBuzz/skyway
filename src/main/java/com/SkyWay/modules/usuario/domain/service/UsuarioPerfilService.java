package com.SkyWay.modules.usuario.domain.service;

import com.SkyWay.modules.usuario.domain.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.Optional;

/**
 * Servicio de dominio para la consulta, edición segura y cambio de contraseña del perfil de usuario autenticado.
 */
public interface UsuarioPerfilService {

    /**
     * Resultado de operaciones sobre el perfil del usuario.
     *
     * @param exitoso indica si la operación se completó correctamente
     * @param mensaje mensaje legible para mostrar en la interfaz
     * @param usuarioActualizado entidad {@link Usuario} refrescada tras persistir cambios (puede ser {@code null} si falla)
     */
    record ResultadoPerfil(boolean exitoso, String mensaje, Usuario usuarioActualizado) {
        public static ResultadoPerfil ok(String mensaje, Usuario usuarioActualizado) {
            return new ResultadoPerfil(true, mensaje, usuarioActualizado);
        }

        public static ResultadoPerfil error(String mensaje) {
            return new ResultadoPerfil(false, mensaje, null);
        }
    }

    /**
     * Obtiene el usuario actualizado desde base de datos por su RUT.
     *
     * @param rut RUT del usuario
     * @return usuario si existe
     */
    Optional<Usuario> buscarPorRut(String rut);

    /**
     * Obtiene el usuario actualizado desde base de datos por su correo electrónico.
     *
     * @param correo correo electrónico del usuario
     * @return usuario si existe
     */
    Optional<Usuario> buscarPorCorreo(String correo);

    /**
     * Valida y actualiza los datos editables del perfil de usuario (nombre, apellido, correo, teléfono y fecha de nacimiento).
     *
     * @param rut identificador inmutable del usuario autenticado
     * @param nombre nuevo nombre
     * @param apellido nuevo apellido
     * @param correoElectronico nuevo correo electrónico
     * @param telefono nuevo teléfono de contacto
     * @param fechaNacimiento nueva fecha de nacimiento
     * @param request petición HTTP para trazabilidad de auditoría
     * @return resultado con la entidad persistida si fue exitoso
     */
    ResultadoPerfil actualizarPerfil(
            String rut,
            String nombre,
            String apellido,
            String correoElectronico,
            String telefono,
            Date fechaNacimiento,
            HttpServletRequest request
    );

    /**
     * Verifica la contraseña actual, valida fortaleza y coincidencia de la nueva clave y la actualiza cifrada.
     *
     * @param rut identificador inmutable del usuario autenticado
     * @param contrasenaActual contraseña vigente ingresada para verificación
     * @param nuevaContrasena nueva contraseña propuesta
     * @param confirmarContrasena confirmación de la nueva contraseña
     * @param request petición HTTP para trazabilidad de auditoría
     * @return resultado con la entidad actualizada si fue exitoso
     */
    ResultadoPerfil cambiarContrasena(
            String rut,
            String contrasenaActual,
            String nuevaContrasena,
            String confirmarContrasena,
            HttpServletRequest request
    );
}
