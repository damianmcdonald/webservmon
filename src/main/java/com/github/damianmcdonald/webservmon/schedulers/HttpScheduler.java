package com.github.damianmcdonald.webservmon.schedulers;

import com.github.damianmcdonald.webservmon.mailers.HttpMailer;
import com.github.damianmcdonald.webservmon.monitors.HttpMonitorService;
import com.github.damianmcdonald.webservmon.throttlers.HttpThrottleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.ThreadContext;

@Service
public class HttpScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpScheduler.class);

    private static final String KEY_CORRELATION_ID = "CORRELATION_ID";

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
        LOGGER.debug(">>> Entering method");
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        LOGGER.info(String.format(">>> Start of correlation session: %s", correlationId));
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        LOGGER.info(String.format(
                ">>> Results from HTTP status checks: %s",
                results.keySet().stream()
                        .map(key -> String.format("%s = %s", key, results.get(key)))
                        .collect(Collectors.joining(", ", "{", "}"))
        )
        );
        final Set<HttpStatus> errors = results
                .values()
                .stream()
                .filter(httpStatus -> !httpStatus.is2xxSuccessful())
                .collect(Collectors.toSet());
        if (!errors.isEmpty()) {
            LOGGER.error(">>> One or more web services has errors, sending mail.");
            results.forEach((k, v) -> LOGGER.info(String.format(">>> URL %s : HttpStatus %s", k, v)));
            if (!throttleService.applyThrottle()) {
                LOGGER.error(">>> Sending ERROR mail.");
                mailer.sendMail(true, results);
            } else {
                LOGGER.warn(">>> ERROR mail send threshold breached. ERROR mail will not be sent.");
            }
            return;
        }
        LOGGER.info("Web service checks passed with no errors, not sending mail.");
        throttleService.decrementThrottleInstance();
        if (ThreadContext.get(KEY_CORRELATION_ID).equalsIgnoreCase(correlationId)) {
            ThreadContext.clearStack();
        }
        LOGGER.info(String.format("<<< End of correlation session: %s", correlationId));
        LOGGER.debug("<<< Exiting method");
    }

    @Scheduled(cron = "${http.schedule.alive.cron}")
    public void sendAliveMail() {
        LOGGER.debug(">>> Entering method");
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        LOGGER.info(String.format(">>> Start of correlation session: %s", correlationId));
        final Map<String, HttpStatus> results = monitorService.checkServiceStatus(urls);
        LOGGER.info(String.format(
                ">>> Results from HTTP status checks: %s",
                results.keySet().stream()
                        .map(key -> String.format("%s = %s", key, results.get(key)))
                        .collect(Collectors.joining(", ", "{", "}"))
        )
        );
        LOGGER.info(">>> Sending ALIVE mail.");
        mailer.sendMail(false, results);
        if (ThreadContext.get(KEY_CORRELATION_ID).equalsIgnoreCase(correlationId)) {
            ThreadContext.clearStack();
        }
        LOGGER.info(String.format("<<< End of correlation session: %s", correlationId));
        LOGGER.debug("<<< Exiting method");
    }

    @Scheduled(cron = "${http.throttle.threshold.period}")
    public void resetThrottleThresholdPeriod() {
        LOGGER.debug(">>> Entering method");
        LOGGER.info(
                String.format(
                        ">>> Throttle count before reset: %d",
                        HttpThrottleService.THROTTLE_INSTANCES.size()
                )
        );
        throttleService.resetThrottleInstances();
        LOGGER.info(
                String.format(
                        ">>> Throttle count before reset: %d",
                        HttpThrottleService.THROTTLE_INSTANCES.size()
                )
        );
        LOGGER.debug("<<< Exiting method");
    }
}
