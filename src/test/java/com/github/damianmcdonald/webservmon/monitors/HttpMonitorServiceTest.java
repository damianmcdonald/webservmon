package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import org.junit.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpMonitorServiceTest extends AbstractTestCase {

    private static ClientAndServer mockServer;

    @Autowired
    @Qualifier("httpMonitorService")
    private MonitorService monitorService;

    @Value("${http.service.urls}")
    private String[] urls;

    @BeforeClass
    public static void startServer() {
        mockServer = startClientAndServer(HTTP_PORT);
    }

    @AfterClass
    public static void stopServer() {
        mockServer.stop();
    }

    @Test
    public void checkServiceStatusTestAlive() {
        createExpectationForAliveService();
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        final Set<HttpStatus> errors = results
                .values()
                .stream()
                .filter(httpStatus -> !httpStatus.is2xxSuccessful())
                .collect(Collectors.toSet());
        Assert.assertTrue(errors.isEmpty());
    }

    @Test
    public void checkServiceStatusTestDead() {
        createExpectationForDeadService();
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        final Set<HttpStatus> errors = results
                .values()
                .stream()
                .filter(httpStatus -> !httpStatus.is2xxSuccessful())
                .collect(Collectors.toSet());
        Assert.assertFalse(errors.isEmpty());
    }

}
