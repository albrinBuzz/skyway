package com.SkyWay.exception;

import com.SkyWay.util.Logger;
import jakarta.el.ELException;
import jakarta.el.MethodNotFoundException;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import org.primefaces.PrimeFaces;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomExceptionHandler extends ExceptionHandlerWrapper implements Serializable {

    private static final long serialVersionUID = 1L;
    private final ExceptionHandler wrapped;

    public CustomExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public void handle() throws FacesException {
        Iterator<ExceptionQueuedEvent> iterator = getUnhandledExceptionQueuedEvents().iterator();

        while (iterator.hasNext()) {
            ExceptionQueuedEvent event = iterator.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable exception = context.getException();

            Throwable rootCause = findRootCause(exception);
            FacesContext facesContext = FacesContext.getCurrentInstance();

            if (facesContext == null) {
                continue;
            }

            ExternalContext externalContext = facesContext.getExternalContext();

            try {
                // =====================================================================
                // 1. SESIÓN / CADUCIDAD DE VISTA JSF (Redirección obligatoria)
                // =====================================================================
                if (rootCause instanceof ViewExpiredException) {
                    Logger.logWarn("⏳ [JSF] Sesión o vista expirada (ViewExpiredException). Redirigiendo al login...");
                    redirect(facesContext, externalContext, "/home/login.xhtml");
                }

                // =====================================================================
                // 2. SEGURIDAD Y PERMISOS (Spring Security)
                // =====================================================================
                else if (rootCause instanceof AccessDeniedException) {
                    Logger.logWarn("⛔ [SECURITY] Intento de acceso sin privilegios: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Acceso no permitido",
                            "No tienes permisos para realizar esta acción.");
                }

                // =====================================================================
                // 3. BASE DE DATOS Y REGISTROS (JPA, Hibernate, SQL)
                // =====================================================================
                else if (rootCause instanceof DataIntegrityViolationException
                        || rootCause.getClass().getName().contains("ConstraintViolationException")) {
                    Logger.logError("🗄️ [DATABASE] Violación de clave única o integridad: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Información existente",
                            "Esta información ya se encuentra registrada o está en uso.");
                }
                else if (rootCause instanceof EntityNotFoundException || rootCause instanceof NoSuchElementException) {
                    Logger.logError("🔍 [DATABASE] Registro no encontrado: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "No disponible",
                            "La información solicitada ya no se encuentra disponible.");
                }
                else if (rootCause instanceof EntityExistsException) {
                    Logger.logError("🗂️ [DATABASE] Intento de duplicación de entidad: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Ya registrado",
                            "Los datos ingresados ya se encuentran guardados en el sistema.");
                }
                else if (rootCause instanceof OptimisticLockingFailureException || rootCause instanceof OptimisticLockException) {
                    Logger.logWarn("🔄 [DATABASE] Edición concurrente detectada: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Cambios detectados",
                            "Alguien más actualizó esta información recientementemente. Por favor, recarga la página.");
                }
                else if (rootCause instanceof DataAccessException || rootCause instanceof PersistenceException) {
                    Logger.logError("💥 [DATABASE] Error general de datos: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Aviso",
                            "No se pudo guardar la información. Intenta de nuevo en unos momentos.");
                }

                // =====================================================================
                // 4. DATOS DE FORMULARIO (JSF / PrimeFaces)
                // =====================================================================
                else if (rootCause instanceof ValidatorException || rootCause instanceof ConverterException) {
                    Logger.logWarn("✏️ [VALIDATOR/CONVERTER] Error de formato en formulario: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Dato no válido",
                            "Revisa los datos ingresados y vuelve a intentarlo.");
                }

                // =====================================================================
                // 5. ERRORES DE INTERFAZ / MÉTODOS NO ENCONTRADOS
                // =====================================================================
                else if (rootCause instanceof MethodNotFoundException
                        || rootCause instanceof PropertyNotFoundException
                        || rootCause instanceof ELException) {
                    Logger.logError("⚠️ [EL / JSF] Excepción en Expression Language o método de Bean: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Inconveniente temporal",
                            "La opción seleccionada no está disponible por el momento.");
                }

                // =====================================================================
                // 6. RED Y MENSAJERÍA (Socket / Mail)
                // =====================================================================
                else if (rootCause instanceof SocketTimeoutException || rootCause instanceof ConnectException) {
                    Logger.logError("🌐 [NETWORK] Tiempo de espera agotado o fallo de conexión: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Sin respuesta",
                            "El servicio tardó demasiado en responder. Intenta más tarde.");
                }
                else if (rootCause instanceof MailException) {
                    Logger.logError("📧 [MAIL] Error al enviar correo: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Aviso",
                            "No se pudo enviar el mensaje por correo, pero tus datos fueron guardados.");
                }

                // =====================================================================
                // 7. ERRORES DE PROGRAMACIÓN HABITUALES (NullPointer, State)
                // =====================================================================
                else if (rootCause instanceof NullPointerException || rootCause instanceof IllegalArgumentException || rootCause instanceof IllegalStateException) {
                    Logger.logError("💥 [JAVA] Error interno o campos vacíos: " + rootCause.getMessage());
                    notificarErrorEnVista(facesContext, "Faltan datos",
                            "Asegúrate de completar todos los datos requeridos antes de continuar.");
                }

                // =====================================================================
                // 8. FALLBACK GENERAL (Cualquier otro error no mapeado)
                // =====================================================================
                else {
                    Logger.logError("💥 [UNHANDLED EXCEPTION] " + rootCause.getClass().getName() + " -> " + rootCause.getMessage());

                    if (facesContext.getPartialViewContext().isAjaxRequest()) {
                        notificarErrorEnVista(facesContext, "Algo salió mal",
                                "Ocurrió un inconveniente insospechado. Intenta de nuevo más tarde.");
                    } else {
                        redirect(facesContext, externalContext, "/templates/error/500.xhtml");
                    }
                }

            } catch (Exception e) {
                Logger.logError("Error interno en CustomExceptionHandler: " + e.getMessage());
            } finally {
                iterator.remove();
            }
        }

        getWrapped().handle();
    }

    private Throwable findRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private void notificarErrorEnVista(FacesContext facesContext, String titulo, String detalle) {
        if (externalContextCommitted(facesContext)) return;

        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, titulo, detalle));

        if (facesContext.getPartialViewContext().isAjaxRequest()) {
            try {
                PrimeFaces.current().ajax().update("msgs", "growl", ":msgs", ":growl");
            } catch (Exception ignored) {
                // Fallback silencioso
            }
        }
    }

    private boolean externalContextCommitted(FacesContext facesContext) {
        return facesContext.getExternalContext().isResponseCommitted();
    }

    private void redirect(FacesContext facesContext, ExternalContext externalContext, String page) throws IOException {
        String path = externalContext.getRequestContextPath() + page;

        if (externalContext.isResponseCommitted()) {
            return;
        }

        if (facesContext.getPartialViewContext().isAjaxRequest()) {
            externalContext.getFlash().setKeepMessages(true);
            externalContext.redirect(path);
        } else {
            externalContext.redirect(path);
        }
        facesContext.responseComplete();
    }
}