package com.SkyWay.config;

import jakarta.el.ValueExpression;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;

import java.io.Serial;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * PhaseListener global que intercepta y sanitiza los mensajes de validación
 * antes del renderizado en JSF/PrimeFaces.
 *
 * Elimina IDs técnicos como 'formDialog:j_idt140' y mensajes crudos de JSF
 * ('Error de validación: se necesita un valor.'), transformándolos en avisos
 * amigables, personalizados con el nombre del campo y sin duplicación de texto.
 */
public class CleanValidationMessagePhaseListener implements PhaseListener, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern TECHNICAL_ID_PATTERN = Pattern.compile("(?i)(j_idt\\d+|:[a-zA-Z0-9_]+:)+");

    private static final Map<String, String> COMMON_FIELD_LABELS = Map.ofEntries(
            Map.entry("rut", "RUT"),
            Map.entry("nombre", "Nombre"),
            Map.entry("apellido", "Apellido"),
            Map.entry("documentoidentidad", "Documento de Identidad"),
            Map.entry("fechanacimiento", "Fecha de Nacimiento"),
            Map.entry("correoelectronico", "Correo Electrónico"),
            Map.entry("correo", "Correo Electrónico"),
            Map.entry("email", "Correo Electrónico"),
            Map.entry("telefono", "Teléfono"),
            Map.entry("contrasena", "Contraseña"),
            Map.entry("contrasenaauxiliar", "Contraseña"),
            Map.entry("password", "Contraseña"),
            Map.entry("nombreaeropuerto", "Nombre del Aeropuerto"),
            Map.entry("codigoiata", "Código IATA"),
            Map.entry("iata", "Código IATA"),
            Map.entry("idciudad", "Ciudad"),
            Map.entry("ciudad", "Ciudad"),
            Map.entry("idpais", "País"),
            Map.entry("pais", "País"),
            Map.entry("idfabricante", "Fabricante"),
            Map.entry("fabricante", "Fabricante"),
            Map.entry("idmodelo", "Modelo"),
            Map.entry("modelo", "Modelo"),
            Map.entry("matricula", "Matrícula"),
            Map.entry("fechamantenimiento", "Fecha de Mantenimiento"),
            Map.entry("fechasalida", "Fecha de Salida"),
            Map.entry("fechallegada", "Fecha de Llegada"),
            Map.entry("nombreform", "Nombre"),
            Map.entry("descripcionform", "Descripción"),
            Map.entry("preciobase", "Precio Base"),
            Map.entry("precio", "Precio")
    );

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RENDER_RESPONSE;
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        if (context == null || context.getViewRoot() == null) {
            return;
        }

        // Sanitizar mensajes asociados a componentes específicos
        Iterator<String> clientIds = context.getClientIdsWithMessages();
        while (clientIds.hasNext()) {
            String clientId = clientIds.next();
            Iterator<FacesMessage> messages = context.getMessages(clientId);
            while (messages.hasNext()) {
                FacesMessage msg = messages.next();
                cleanFacesMessage(context, clientId, msg);
            }
        }

        // Sanitizar mensajes globales sin clientId asignado
        Iterator<FacesMessage> globalMessages = context.getMessages(null);
        while (globalMessages.hasNext()) {
            FacesMessage msg = globalMessages.next();
            cleanFacesMessage(context, null, msg);
        }
    }

    @Override
    public void afterPhase(PhaseEvent event) {
        // No requiere acción posterior
    }

    private void cleanFacesMessage(FacesContext context, String clientId, FacesMessage msg) {
        if (msg == null) {
            return;
        }

        String summary = msg.getSummary() != null ? msg.getSummary() : "";
        String detail = msg.getDetail() != null ? msg.getDetail() : "";

        boolean isRequiredError = summary.contains("se necesita un valor")
                || summary.contains("Error de validación")
                || detail.contains("se necesita un valor")
                || detail.contains("Error de validación")
                || summary.contains("UIInput.REQUIRED")
                || detail.contains("UIInput.REQUIRED");

        boolean containsTechnical = TECHNICAL_ID_PATTERN.matcher(summary).find()
                || TECHNICAL_ID_PATTERN.matcher(detail).find()
                || summary.contains("j_idt")
                || detail.contains("j_idt");

        if (!isRequiredError && !containsTechnical) {
            return;
        }

        // Determinar nombre amigable del campo
        String fieldLabel = resolveFieldLabel(context, clientId);

        if (isRequiredError) {
            msg.setSummary("Campo Requerido");
            if (fieldLabel != null && !fieldLabel.isBlank()) {
                msg.setDetail("El campo '" + fieldLabel + "' es obligatorio.");
            } else {
                msg.setDetail("Por favor, complete este campo obligatorio para continuar.");
            }
        } else {
            // Limpiar identificadores técnicos de otros errores
            String cleanSummary = stripTechnicalIds(summary, fieldLabel);
            String cleanDetail = stripTechnicalIds(detail, fieldLabel);

            msg.setSummary(cleanSummary.isBlank() ? "Aviso de Validación" : cleanSummary);
            msg.setDetail(cleanDetail.isBlank() ? "Por favor, revise la información ingresada." : cleanDetail);
        }
    }

    private String resolveFieldLabel(FacesContext context, String clientId) {
        if (context == null || context.getViewRoot() == null || clientId == null || clientId.isBlank()) {
            return null;
        }

        UIComponent component = findComponentRecursive(context.getViewRoot(), clientId);
        if (component == null) {
            return cleanClientIdFallback(clientId);
        }

        // 1. Atributo 'label' explícito
        Object labelAttr = component.getAttributes().get("label");
        if (labelAttr != null && !labelAttr.toString().isBlank() && !labelAttr.toString().contains("j_idt")) {
            return labelAttr.toString().trim();
        }

        // 2. Propiedad desde ValueExpression (ej: #{gestionUsuariosAdminBean.usuarioSeleccionado.correoElectronico})
        ValueExpression ve = component.getValueExpression("value");
        if (ve != null && ve.getExpressionString() != null) {
            String expr = ve.getExpressionString().trim();
            String propName = extractPropertyName(expr);
            if (propName != null && !propName.isBlank()) {
                String normalized = propName.toLowerCase().replaceAll("[^a-z]", "");
                if (COMMON_FIELD_LABELS.containsKey(normalized)) {
                    return COMMON_FIELD_LABELS.get(normalized);
                }
                return humanizeCamelCase(propName);
            }
        }

        // 3. ID explícito del componente si no es autogenerado (ej: inputNombreAeropuerto -> Nombre del Aeropuerto)
        String compId = component.getId();
        if (compId != null && !compId.startsWith("j_idt") && !compId.isBlank()) {
            return humanizeComponentId(compId);
        }

        // 4. Placeholder informativo si existe
        Object placeholder = component.getAttributes().get("placeholder");
        if (placeholder != null && !placeholder.toString().isBlank()) {
            String ph = placeholder.toString().trim();
            if (ph.startsWith("Ej:") || ph.startsWith("Ej.:")) {
                ph = ph.replaceFirst("^Ej\\.?:?\\s*", "").trim();
            }
            if (!ph.contains("•") && ph.length() <= 30) {
                return ph;
            }
        }

        return cleanClientIdFallback(clientId);
    }

    private String extractPropertyName(String expr) {
        // Remover #{ y }
        String clean = expr.replaceAll("[#{}]", "").trim();
        int lastDot = clean.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < clean.length() - 1) {
            return clean.substring(lastDot + 1);
        }
        return clean;
    }

    private String humanizeCamelCase(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (i == 0) {
                result.append(Character.toUpperCase(c));
            } else if (Character.isUpperCase(c)) {
                result.append(" ").append(c);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String humanizeComponentId(String compId) {
        String clean = compId.replaceFirst("^(input|select|date|txt|chk|btn)", "");
        if (clean.isBlank()) {
            clean = compId;
        }
        String normalized = clean.toLowerCase().replaceAll("[^a-z]", "");
        if (COMMON_FIELD_LABELS.containsKey(normalized)) {
            return COMMON_FIELD_LABELS.get(normalized);
        }
        return humanizeCamelCase(clean);
    }

    private String cleanClientIdFallback(String clientId) {
        if (clientId == null) {
            return null;
        }
        int lastColon = clientId.lastIndexOf(':');
        String lastPart = (lastColon >= 0 && lastColon < clientId.length() - 1) ? clientId.substring(lastColon + 1) : clientId;
        if (lastPart.startsWith("j_idt")) {
            return null; // Omitir IDs autogenerados feos
        }
        return humanizeComponentId(lastPart);
    }

    private String stripTechnicalIds(String text, String fieldLabel) {
        if (text == null) {
            return "";
        }
        String replaced = text.replaceAll("[a-zA-Z0-9_]+:j_idt\\d+:?", "")
                .replaceAll("j_idt\\d+:?", "")
                .replaceAll("form[a-zA-Z0-9_]*:?", "")
                .replaceAll("Error de validación:\\s*", "")
                .replaceAll("se necesita un valor\\.?", "")
                .trim();

        if (replaced.isBlank() && fieldLabel != null) {
            return "El campo '" + fieldLabel + "' requiere su atención.";
        }
        return replaced;
    }

    private UIComponent findComponentRecursive(UIComponent parent, String clientId) {
        if (parent == null || clientId == null) {
            return null;
        }
        if (clientId.equals(parent.getClientId())) {
            return parent;
        }
        for (UIComponent child : parent.getChildren()) {
            UIComponent found = findComponentRecursive(child, clientId);
            if (found != null) {
                return found;
            }
        }
        for (UIComponent facet : parent.getFacets().values()) {
            UIComponent found = findComponentRecursive(facet, clientId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
