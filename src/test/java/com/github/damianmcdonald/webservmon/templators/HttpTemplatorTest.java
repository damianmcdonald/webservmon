package com.github.damianmcdonald.webservmon.templators;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.domain.HttpMonitorStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.UUID;
import org.springframework.http.HttpStatus;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpTemplatorTest implements AbstractTestCase {

    private final static String TEMPLATE_FILE_HTML = "http-report-html.ftl";

    @Autowired
    private Templator templator;

    @Test
    public void getMergedHtmlTemplateSuccessTest() {
        final HashMap model = new HashMap();
        final HashMap<String, HttpStatus> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HttpStatus.OK);
        results.put(TEST_SERVICE_2_URL, HttpStatus.OK);
        model.put(MODEL_RESULTS, new HttpMonitorStatus(UUID.randomUUID().toString(), results));
        final String template = templator.getMergedTemplate(model, TEMPLATE_FILE_HTML);
        Assert.assertNotNull(template);
        Assert.assertFalse(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void getMergedHtmlTemplateErrorTest() {
        final HashMap model = new HashMap();
        final HashMap<String, HttpStatus> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HttpStatus.OK);
        results.put(TEST_SERVICE_2_URL, HttpStatus.SERVICE_UNAVAILABLE);
        model.put(MODEL_RESULTS, new HttpMonitorStatus(UUID.randomUUID().toString(), results));
        final String template = templator.getMergedTemplate(model, TEMPLATE_FILE_HTML);
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void getMergedHtmlTemplateUnknownTest() {
        final HashMap model = new HashMap();
        final HashMap<String, HttpStatus> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HttpStatus.OK);
        results.put(TEST_SERVICE_2_URL, HttpStatus.I_AM_A_TEAPOT);
        model.put(MODEL_RESULTS, new HttpMonitorStatus(UUID.randomUUID().toString(), results));
        final String template = templator.getMergedTemplate(model, TEMPLATE_FILE_HTML);
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }
    
}
