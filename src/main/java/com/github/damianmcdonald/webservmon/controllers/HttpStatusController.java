package com.github.damianmcdonald.webservmon.controllers;

import com.github.damianmcdonald.webservmon.domain.HttpMonitorStatus;
import com.github.damianmcdonald.webservmon.monitors.HttpMonitorService;
import com.github.damianmcdonald.webservmon.templators.Templator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.ThreadContext;

@RestController
public class HttpStatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpStatusController.class);

    private static final String RESULT_KEY = "results";

    private final static String TEMPLATE_FILE = "http-report-html.ftl";
    
    private static final String KEY_CORRELATION_ID = "CORRELATION_ID";

    @Value("${http.service.urls}")
    private String[] urls;

    @Autowired
    private HttpMonitorService monitorService;

    @Autowired
    private Templator templator;

    @RequestMapping(value = "/http-status", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String status() {
        LOGGER.debug(">>> Entering method");
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        LOGGER.info(String.format(">>> Start of correlation session: %s", correlationId));
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        LOGGER.info("Results of web service status checks;",
                results.keySet().stream()
                .map(key -> String.format("%s = %s", key, results.get(key)))
                .collect(Collectors.joining(", ", "{", "}")));
        final HashMap model = new HashMap();
        model.put(RESULT_KEY, new HttpMonitorStatus(ThreadContext.get(KEY_CORRELATION_ID), results));
        if (ThreadContext.get(KEY_CORRELATION_ID).equalsIgnoreCase(correlationId)) {
            ThreadContext.clearStack();
        }
        LOGGER.info(String.format("<<< End of correlation session: %s", correlationId));
        LOGGER.debug("<<< Exiting method");
        return templator.getMergedTemplate(model, TEMPLATE_FILE);
    }
}
