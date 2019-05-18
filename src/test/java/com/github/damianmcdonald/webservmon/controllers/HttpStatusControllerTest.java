package com.github.damianmcdonald.webservmon.controllers;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpStatusControllerTest extends AbstractTestCase {

    private static ClientAndServer mockServer;

    @Autowired
    private HttpStatusController statusController;

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(HTTP_PORT);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    public void statusAliveTest() {
        createExpectationForAliveService();
        final String template = statusController.status();
        Assert.assertNotNull(template);
        Assert.assertFalse(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void statusDeadTest() {
        createExpectationForDeadService();
        final String template = statusController.status();
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }

    @Test
    public void statusUnknownTest() {
        createExpectationForUnknownService();
        final String template = statusController.status();
        Assert.assertNotNull(template);
        Assert.assertTrue(checkHtmlTemplateHasErrors(template));
    }

}
