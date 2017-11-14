package com.github.damianmcdonald.webservmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private Mailer mailer;

    @Value("${service.urls}")
    private String[] urls;

    @Scheduled(cron = "${schedule.statuscheck.interval}")
    public void checkServiceStatus() {
        LOGGER.debug("Executing checkServiceStatus scheduled task.");
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        final Set<HttpStatus> errors = results
                .values()
                .stream()
                .filter(httpStatus -> !httpStatus.is2xxSuccessful())
                .collect(Collectors.toSet());
        if (!errors.isEmpty()) {
            LOGGER.debug("One or more web services has errors, sending mail.");
            results.forEach((k, v) -> LOGGER.debug(String.format("URL %s : HttpStatus %s", k, v)));
            mailer.sendMail(true, results);
        }
        LOGGER.debug("Web service checks passed with no errors, not sending mail.");
    }

    @Scheduled(cron = "${schedule.alive.cron}")
    public void sendAliveMail() {
        LOGGER.debug("Executing sendAliveMail scheduled task.");
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        LOGGER.debug("Sending ALIVE mail.");
        mailer.sendMail(false, results);
    }
}
