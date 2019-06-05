package com.github.damianmcdonald.webservmon.templators;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpTemplatorTest extends AbstractTestCase {

    private final static String TEMPLATE_FILE = "http-report-html.ftl";

    @Autowired
    private Templator templator;

    @Test
    public void getMergedTemplateSuccessTest() {
        final HashMap model = new HashMap();
        final HashMap<String, String> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HTTP_STATUS_SUCCESS);
        results.put(TEST_SERVICE_2_URL, HTTP_STATUS_SUCCESS);
        model.put(MODEL_RESULTS, results);
        final String template = templator.getMergedTemplate(model, TEMPLATE_FILE);
        Assert.assertNotNull(template);
        Assert.assertFalse(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void getMergedTemplateErrorTest() {
        final HashMap model = new HashMap();
        final HashMap<String, String> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HTTP_STATUS_SUCCESS);
        results.put(TEST_SERVICE_2_URL, HTTP_STATUS_ERROR);
        model.put(MODEL_RESULTS, results);
        final String template = templator.getMergedTemplate(model, TEMPLATE_FILE);
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void getMergedTemplateUnknownTest() {
        final HashMap model = new HashMap();
        final HashMap<String, String> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HTTP_STATUS_SUCCESS);
        results.put(TEST_SERVICE_2_URL, HTTP_STATUS_UNKNOWN);
        model.put(MODEL_RESULTS, results);
        final String template = templator.getMergedTemplate(model, TEMPLATE_FILE);
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }
}
