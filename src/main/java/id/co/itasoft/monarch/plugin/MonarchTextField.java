package id.co.itasoft.monarch.plugin;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.model.Element;
import org.joget.apps.form.model.FormBuilderPaletteElement;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.FormRow;
import org.joget.apps.form.model.FormRowSet;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;

public class MonarchTextField extends Element implements FormBuilderPaletteElement {
    // Validation and storage share values without retaining completed form requests indefinitely.
    protected final Map<FormData, String> submittedValueHolder = Collections.synchronizedMap(new WeakHashMap<FormData, String>());
    protected final Map<FormData, String> validationValueHolder = Collections.synchronizedMap(new WeakHashMap<FormData, String>());
    private static final String PLUGIN_NAME = "Shadow Text Field";

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public String getVersion() {
        return "8.0.1";
    }

    @Override
    public String getDescription() {
        return "Combined text and hidden field with native Joget builder previews.";
    }

    @Override
    public String renderTemplate(FormData formData, Map dataModel) {
        String value = resolveValue(formData);
        // Redisplay the original formatted input when another field fails validation.
        if (!isDefaultValueOnly() && submittedValueHolder.containsKey(formData)) {
            value = submittedValueHolder.get(formData);
        }
        boolean readonly = FormUtil.isReadonly(this, formData);
        dataModel.put("readonly", readonly);
        dataModel.put("renderInstanceId", UUID.randomUUID().toString());
        if (readonly && isEnabled("readonlyLabel")) {
            String valueLabel = value;
            if (value != null && !value.isEmpty() && hasNumberFormatting() && isEnabled("storeNumeric")) {
                valueLabel = StringUtil.numberFormat(value, getPropertyString("style"), getPropertyString("prefix"),
                        getPropertyString("postfix"), isEnabled("useThousandSeparator"), getPropertyString("numOfDecimal"));
            }
            dataModel.put("valueLabel", valueLabel);
        }
        dataModel.put("value", value);
        return FormUtil.generateElementHtml(this, formData, "monarchTextField.ftl", dataModel);
    }

    private String resolveValue(FormData formData) {
        String value = SecurityUtil.decrypt(FormUtil.getElementPropertyValue(this, formData));
        String priority = getPropertyString("useDefaultWhenEmpty");
        String defaultValue = getPropertyString("value");
        if (isDefaultValueOnly()) {
            return defaultValue;
        }
        if ("true".equalsIgnoreCase(priority)) {
            return value == null || value.isEmpty() ? defaultValue : value;
        }
        // A configured default takes precedence on load, but must not erase user input on redisplay.
        String parameterName = FormUtil.getElementParameterName(this);
        boolean submitted = formData != null && formData.getRequestParameterValues(parameterName) != null
                && !FormUtil.isReadonly(this, formData);
        if (!submitted && priority.isEmpty() && !defaultValue.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    @Override
    public FormData formatDataForValidation(FormData formData) {
        if (formData != null && hasNumberFormatting() && (!FormUtil.isReadonly(this, formData) || isDefaultValueOnly())) {
            String parameterName = FormUtil.getElementParameterName(this);
            if (parameterName != null && !parameterName.isEmpty()) {
                String submittedValue = resolveValue(formData);
                // Do not interpret an already normalized Euro decimal as a thousands separator.
                if (submittedValue != null && submittedValue.equals(validationValueHolder.get(formData))) {
                    return formData;
                }
                if (submittedValue != null) {
                    String validationValue = normalizeNumericValue(submittedValue);
                    formData.addRequestParameterValues(parameterName, new String[]{validationValue});
                    submittedValueHolder.put(formData, submittedValue);
                    validationValueHolder.put(formData, validationValue);
                }
            }
        }
        return formData;
    }

    private String normalizeNumericValue(String value) {
        String normalized = value.trim();
        String prefix = getPropertyString("prefix");
        String postfix = getPropertyString("postfix");
        if (!prefix.isEmpty() && normalized.startsWith(prefix)) {
            normalized = normalized.substring(prefix.length());
        }
        if (!postfix.isEmpty() && normalized.endsWith(postfix)) {
            normalized = normalized.substring(0, normalized.length() - postfix.length());
        }
        normalized = normalized.replace(" ", "").replace("\u00a0", "").replace("\u202f", "");
        // Hidden inputs and configured defaults may already contain canonical database numbers.
        if ((isEnabled("hiddenField") || isDefaultValueOnly()) && normalized.matches("[+-]?[0-9]+(?:\\.[0-9]+)?")) {
            return normalized;
        }
        if ("euro".equalsIgnoreCase(getPropertyString("style"))) {
            return normalized.replace(".", "").replace(',', '.');
        }
        return normalized.replace(",", "");
    }

    @Override
    public FormRowSet formatData(FormData formData) {
        String id = getPropertyString(FormUtil.PROPERTY_ID);
        if (id == null || id.isEmpty()) {
            return null;
        }
        String value = FormUtil.getElementPropertyValue(this, formData);
        if (isDefaultValueOnly()) {
            // Always normalize the configured default itself, even when a request supplies a value.
            value = getPropertyString("value");
            if (hasNumberFormatting() && isEnabled("storeNumeric")) {
                value = normalizeNumericValue(value);
            }
        } else if (value != null && hasNumberFormatting() && !FormUtil.isReadonly(this, formData)) {
            formatDataForValidation(formData);
            if (validationValueHolder.containsKey(formData)) {
                value = isEnabled("storeNumeric") ? validationValueHolder.get(formData) : submittedValueHolder.get(formData);
            }
        }
        if (value == null) {
            return null;
        }
        if (!FormUtil.isReadonly(this, formData) && isEnabled("encryption")) {
            value = SecurityUtil.encrypt(value);
        }
        FormRow result = new FormRow();
        result.setProperty(id, value);
        FormRowSet rowSet = new FormRowSet();
        rowSet.add(result);
        return rowSet;
    }

    private boolean hasNumberFormatting() {
        return !getPropertyString("style").isEmpty();
    }

    private boolean isEnabled(String property) {
        return "true".equalsIgnoreCase(getPropertyString(property));
    }

    private boolean isDefaultValueOnly() {
        return "valueOnly".equalsIgnoreCase(getPropertyString("useDefaultWhenEmpty"));
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public String getFormBuilderTemplate() {
        return "<label class='label'>" + ResourceBundleUtil.getMessage("org.joget.apps.form.lib.TextField.pluginLabel")
                + "</label><input type='text' />";
    }

    @Override
    public String getLabel() {
        return PLUGIN_NAME;
    }

    @Override
    public String getPropertyOptions() {
        String encryption = "";
        if (SecurityUtil.getDataEncryption() != null) {
            encryption = ",{name : 'encryption', label : '@@form.textfield.encryption@@', type : 'checkbox', value : 'false', ";
            encryption += "options : [{value : 'true', label : '' }]}";
        }
        return AppUtil.readPluginResource(getClass().getName(), "/properties/form/monarchTextField.json",
                new Object[]{encryption}, true, "messages/form/monarchTextField");
    }

    @Override
    public String getFormBuilderCategory() {
        return "<span><i class=\"fas fa-crown\"></i> Monarch </span>";
    }

    @Override
    public int getFormBuilderPosition() {
        return 100;
    }

    @Override
    public String getFormBuilderIcon() {
        return "<i class=\"fas fa-text-height\"></i>";
    }

    @Override
    public Boolean isReadonly(FormData formData) {
        return isDefaultValueOnly() || super.isReadonly(formData);
    }
}
