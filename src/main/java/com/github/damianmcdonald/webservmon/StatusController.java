package com.github.damianmcdonald.webservmon;

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

@RestController
public class StatusController {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);

    private static final String RESULT_KEY = "results";

    @Value("${service.urls}")
    private String[] urls;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private Templator templator;

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String status() {
        LOGGER.debug("Executing status controller.");
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        final HashMap model = new HashMap();
        model.put(RESULT_KEY, results);
        return templator.getMergedTemplate(model);
    }
}
