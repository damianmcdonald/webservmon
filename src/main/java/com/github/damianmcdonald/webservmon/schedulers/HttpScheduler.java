package com.github.damianmcdonald.webservmon.schedulers;

import com.github.damianmcdonald.webservmon.throttlers.HttpThrottleService;
import com.github.damianmcdonald.webservmon.mailers.HttpMailer;
import com.github.damianmcdonald.webservmon.monitors.HttpMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service("httpScheduler")
public class HttpScheduler implements Scheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpScheduler.class);

    @Autowired
    private HttpMonitorService monitorService;

    @Autowired
    private HttpThrottleService throttleService;

    @Autowired
    private HttpMailer mailer;

    @Value("${http.service.urls}")
    private String[] urls;

    @Scheduled(cron = "${http.schedule.statuscheck.interval}")
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
            if (!throttleService.applyThrottle()) {
                LOGGER.debug("Sending ERROR mail.");
                mailer.sendMail(true, results);
            } else {
                LOGGER.debug("ERROR mail send threshold breached. ERROR mail will not be sent.");
            }
            return;
        }
        LOGGER.debug("Web service checks passed with no errors, not sending mail.");
        throttleService.decrementThrottleInstance();
    }

    @Scheduled(cron = "${http.schedule.alive.cron}")
    public void sendAliveMail() {
        LOGGER.debug("Executing sendAliveMail scheduled task.");
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        LOGGER.debug("Sending ALIVE mail.");
        mailer.sendMail(false, results);
    }

    @Scheduled(cron = "${http.throttle.threshold.period}")
    public void resetThrottleThresholdPeriod() {
        LOGGER.debug("Executing resetThrottleThresholdPeriod scheduled task.");
        throttleService.resetThrottleInstances();
    }
}
