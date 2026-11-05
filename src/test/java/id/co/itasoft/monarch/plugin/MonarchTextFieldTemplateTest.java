package id.co.itasoft.monarch.plugin;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MonarchTextFieldTemplateTest {
    private Template template;
    private Map<String, Object> model;
    private Map<String, String> properties;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_31);
        configuration.setClassForTemplateLoading(getClass(), "/templates");
        configuration.setDefaultEncoding("UTF-8");
        template = configuration.getTemplate("monarchTextField.ftl");
        properties = new HashMap<>();
        properties.put("id", "plant_name");
        properties.put("label", "Plant Name");
        properties.put("elementUniqueKey", "field_key");
        model = new HashMap<>();
        model.put("element", java.util.Collections.singletonMap("properties", properties));
        model.put("elementParamName", "plant_name");
        model.put("renderInstanceId", "render_1");
        model.put("request", java.util.Collections.singletonMap("contextPath", "/jw"));
        model.put("value", "Plant A");
        model.put("includeMetaData", false);
    }

    private String render() throws Exception {
        StringWriter writer = new StringWriter();
        template.process(model, writer);
        return writer.toString();
    }

    @Test
    public void builderHiddenUsesNativeInvisibleMarkerAndNoCustomColors() throws Exception {
        properties.put("hiddenField", "true");
        model.put("includeMetaData", true);
        model.put("elementMetaData", "data-cbuilder-classname=\"id.co.itasoft.monarch.plugin.MonarchTextField\"");
        String html = render();
        assertTrue(html.contains("data-cbuilder-element-invisible=\"\""));
        assertTrue(html.contains("data-cbuilder-classname=\"id.co.itasoft.monarch.plugin.MonarchTextField\""));
        assertTrue(html.contains(">plant_name</label>"));
        assertTrue(html.contains("readonly=\"readonly\""));
        assertFalse(html.contains("<style"));
        assertFalse(html.contains("<script"));
        assertFalse(html.contains("#777"));
    }

    @Test
    public void runtimeHiddenHasOnlyOneHiddenInputEvenWithTextOptions() throws Exception {
        properties.put("hiddenField", "true");
        properties.put("lettercase", "uppercase");
        properties.put("symbols", "true");
        properties.put("style", "euro");
        String html = render().trim();
        assertTrue(html.startsWith("<input "));
        assertTrue(html.contains("type=\"hidden\""));
        assertFalse(html.contains("<div"));
        assertFalse(html.contains("<script"));
        assertFalse(html.contains("data-cbuilder"));
    }

    @Test
    public void plainTextOmitsEmptySizeAndMaxlength() throws Exception {
        String html = render();
        assertTrue(html.contains("type=\"text\""));
        assertFalse(html.contains("size="));
        assertFalse(html.contains("maxlength="));
        assertFalse(html.contains("<script"));
    }

    @Test
    public void errorsShareOneInputClassAndHaveAccessibleAssociation() throws Exception {
        model.put("error", "Required");
        String html = render();
        String input = html.substring(html.indexOf("<input"), html.indexOf("/>", html.indexOf("<input")));
        assertEquals(1, input.split("class=", -1).length - 1);
        assertTrue(input.contains("form-error-cell"));
        assertTrue(input.contains("aria-invalid=\"true\""));
        assertTrue(input.contains("aria-describedby=\"plant_name_error\""));
    }

    @Test
    public void effectiveReadonlyLabelRendersWithoutEditableInput() throws Exception {
        model.put("readonly", true);
        model.put("valueLabel", "Fixed Label");
        properties.put("readonlyLabel", "true");
        properties.put("lettercase", "uppercase");
        String html = render();
        assertTrue(html.contains("<span>Fixed Label</span>"));
        assertTrue(html.contains("type=\"hidden\""));
        assertFalse(html.contains("type=\"text\""));
        assertFalse(html.contains("<script"));
    }

    @Test
    public void symbolsWorkIndependentlyOfLetterCase() throws Exception {
        properties.put("symbols", "true");
        assertTrue(render().contains("function normalize(event)"));
    }

    @Test
    public void builderNeverLoadsRuntimeScripts() throws Exception {
        model.put("includeMetaData", true);
        properties.put("style", "euro");
        properties.put("symbols", "true");
        properties.put("lettercase", "uppercase");
        assertFalse(render().contains("<script"));
    }

    @Test
    public void valuesAndAttributesAreHtmlEscaped() throws Exception {
        model.put("value", "\"/><script>alert(1)</script>");
        properties.put("placeholder", "\" autofocus=\"true");
        String html = render();
        assertFalse(html.contains("<script>"));
        assertTrue(html.contains("&lt;script&gt;"));
        assertTrue(html.contains("placeholder=\"&quot; autofocus=&quot;true\""));
    }

    @Test
    public void numericAffixesCannotBreakJavaScriptOrScriptTag() throws Exception {
        properties.put("style", "us");
        properties.put("prefix", "'\n</script><script>alert(1)</script>");
        String html = render();
        assertEquals(1, html.split("</script>", -1).length - 1);
        assertFalse(html.contains("prefix: ''\n"));
        assertTrue(html.contains("window.monarchTextFieldNumberFormatting"));
    }
}
