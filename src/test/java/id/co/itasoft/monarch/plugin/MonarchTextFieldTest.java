package id.co.itasoft.monarch.plugin;

import java.util.HashMap;
import java.util.Map;
import org.joget.apps.form.model.FormData;
import org.joget.apps.form.model.Form;
import org.joget.apps.form.service.FormUtil;
import org.joget.plugin.base.PluginManager;
import org.joget.plugin.property.service.PluginHashVariableHelper;
import org.joget.plugin.property.service.PropertyUtil;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import static org.junit.Assert.*;

public class MonarchTextFieldTest {
    private MonarchTextField field;
    private FormData data;
    private Map<String, Object> model;

    @Before
    public void setUp() {
        StaticApplicationContext context = new StaticApplicationContext();
        context.getBeanFactory().registerSingleton("pluginManager", new PluginManager() {
            @Override
            public String getPluginFreeMarkerTemplate(Map values, String name, String path, String translationPath) {
                return "rendered";
            }
        });
        context.getBeanFactory().registerSingleton("pluginHashVariableHelper", new PluginHashVariableHelper() {
            @Override
            public Map<String, Object> getHashVariableSupportedMap(Map<String, Object> properties) {
                return properties;
            }

            @Override
            public void setDisableHashVariable(Boolean disabled) {
            }
        });
        new PropertyUtil().setApplicationContext(context);
        new FormUtil().setApplicationContext(context);
        field = new MonarchTextField();
        field.setProperties(new HashMap<String, Object>());
        field.setProperty("id", "amount");
        Form form = new Form();
        form.setProperties(new HashMap<String, Object>());
        form.setProperty("id", "form");
        field.setParent(form);
        data = new FormData();
        model = new HashMap<>();
    }

    private void submit(String value) {
        data.addRequestParameterValues("form_SUBMITTED", new String[]{"true"});
        data.addRequestParameterValues("amount", new String[]{value});
    }

    private String stored() {
        return field.formatData(data).get(0).getProperty("amount");
    }

    private String renderedValue() {
        field.renderTemplate(data, model);
        return (String) model.get("value");
    }

    @Test
    public void hiddenAndTextBothStoreSubmittedValues() {
        submit("plant A");
        assertEquals("plant A", stored());
        field.setProperty("hiddenField", "true");
        assertEquals("plant A", stored());
    }

    @Test
    public void defaultFirstDoesNotOverwriteUserInputAfterValidationError() {
        field.setProperty("value", "initial");
        assertEquals("initial", renderedValue());
        submit("edited");
        assertEquals("edited", renderedValue());
        assertEquals("edited", stored());
    }

    @Test
    public void storedFirstFallsBackWhenValueIsEmpty() {
        field.setProperty("value", "fallback");
        field.setProperty("useDefaultWhenEmpty", "true");
        submit("");
        assertEquals("fallback", renderedValue());
        submit("saved");
        assertEquals("saved", renderedValue());
    }

    @Test
    public void alwaysDefaultIgnoresSubmittedValueAndIsReadonly() {
        field.setProperty("value", "fixed");
        field.setProperty("useDefaultWhenEmpty", "valueOnly");
        submit("tampered");
        assertEquals("fixed", stored());
        assertEquals("fixed", renderedValue());
        assertTrue(field.isReadonly(data));
        assertEquals(Boolean.TRUE, model.get("readonly"));
    }

    @Test
    public void numericAlwaysDefaultNormalizesDefaultRatherThanRequest() {
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        field.setProperty("value", "1.234,56");
        field.setProperty("useDefaultWhenEmpty", "valueOnly");
        submit("9.999,99");
        field.formatDataForValidation(data);
        assertEquals("1234.56", stored());
    }

    @Test
    public void euroNumericValidationIsIdempotentAndRedisplaysOriginal() {
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        submit("1.234,56");
        field.formatDataForValidation(data);
        assertEquals("1234.56", data.getRequestParameter("amount"));
        field.formatDataForValidation(data);
        assertEquals("1234.56", stored());
        assertEquals("1.234,56", renderedValue());
        assertEquals("1234.56", stored());
    }

    @Test
    public void usFormattingHandlesLiteralAffixesAndNonbreakingSpaces() {
        field.setProperty("style", "us");
        field.setProperty("storeNumeric", "true");
        field.setProperty("prefix", "US$");
        field.setProperty("postfix", "(net)");
        submit("US$\u00a01,234.56\u202f(net)");
        assertEquals("1234.56", stored());
    }

    @Test
    public void formattedStorageKeepsOriginalSubmittedText() {
        field.setProperty("style", "euro");
        submit("1.234,56");
        field.formatDataForValidation(data);
        assertEquals("1.234,56", stored());
    }

    @Test
    public void changedInputReplacesPreviouslyValidatedValue() {
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        submit("1.234,56");
        field.formatDataForValidation(data);
        submit("9.876,54");
        assertEquals("9876.54", stored());
    }

    @Test
    public void emptyNumericInputIsStoredAsEmpty() {
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        submit("");
        assertEquals("", stored());
    }

    @Test
    public void readonlyNumericValueDoesNotLoseItsDecimalSeparator() {
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        field.setProperty("readonly", "true");
        field.setProperty("value", "1234.56");
        field.formatDataForValidation(data);
        assertEquals("1234.56", stored());
    }

    @Test
    public void hiddenNumericValueCanRoundTripCanonicalDatabaseNumber() {
        field.setProperty("hiddenField", "true");
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        submit("1234.56");
        assertEquals("1234.56", stored());
    }

    @Test
    public void numericDefaultCanUseCanonicalDecimalNotation() {
        field.setProperty("style", "euro");
        field.setProperty("storeNumeric", "true");
        field.setProperty("useDefaultWhenEmpty", "valueOnly");
        field.setProperty("value", "1234.56");
        assertEquals("1234.56", stored());
    }

    @Test
    public void readonlyLabelReceivesDisplayValueAndEffectiveReadonly() {
        field.setProperty("readonlyLabel", "true");
        field.setProperty("useDefaultWhenEmpty", "valueOnly");
        field.setProperty("value", "fixed");
        renderedValue();
        assertEquals("fixed", model.get("valueLabel"));
        assertEquals(Boolean.TRUE, model.get("readonly"));
    }

    @Test
    public void missingIdDoesNotProduceAnEmptyColumn() {
        field.setProperty("id", "");
        assertNull(field.formatData(data));
    }
}
