package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.rules.HttpServerRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpMonitorServiceTest implements AbstractTestCase {

    @Autowired
    private HttpMonitorService monitorService;

    @Value("${http.service.urls}")
    private String[] urls;

    @Rule
    public HttpServerRule httpServerRule = new HttpServerRule(1080);

    @Test
    public void checkServiceStatusTestAlive() {
        httpServerRule.createExpectationForAliveService();
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
        httpServerRule.createExpectationForDeadService();
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        final Set<HttpStatus> errors = results
                .values()
                .stream()
                .filter(httpStatus -> !httpStatus.is2xxSuccessful())
                .collect(Collectors.toSet());
        Assert.assertFalse(errors.isEmpty());
    }

}