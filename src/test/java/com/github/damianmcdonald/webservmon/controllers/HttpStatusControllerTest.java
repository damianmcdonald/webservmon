package com.github.damianmcdonald.webservmon.controllers;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.rules.HttpServerRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpStatusControllerTest implements AbstractTestCase {

    @Autowired
    private HttpStatusController statusController;

    @Rule
    public HttpServerRule httpServerRule = new HttpServerRule(1080);

    @Test
    public void statusAliveTest() {
        httpServerRule.createExpectationForAliveService();
        final String template = statusController.status();
        Assert.assertNotNull(template);
        Assert.assertFalse(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void statusDeadTest() {
        httpServerRule.createExpectationForDeadService();
        final String template = statusController.status();
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void statusUnknownTest() {
        httpServerRule.createExpectationForUnknownService();
        final String template = statusController.status();
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }

}