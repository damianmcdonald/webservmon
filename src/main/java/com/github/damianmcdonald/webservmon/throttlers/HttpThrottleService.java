package com.github.damianmcdonald.webservmon.throttlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class HttpThrottleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpThrottleService.class);

    public static final BlockingQueue<Instant> THROTTLE_INSTANCES = new ArrayBlockingQueue<Instant>(100);

    @Value("${http.throttleable.instance.threshold}")
    private int throttleThreshold;

    public boolean applyThrottle() {
        LOGGER.debug(">>> Entering method");
        LOGGER.info(
                String.format(
                        ">>> Current throttle instances: %d, throttle threshold: %d",
                        THROTTLE_INSTANCES.size(),
                        throttleThreshold
                )
        );
        if (THROTTLE_INSTANCES.size() >= throttleThreshold) {
            LOGGER.info(">>> Throttle threshold has been breached. No need to increment.");
            return true;
        }
        try {
            LOGGER.debug("Throttle threshold has not been breached. Incrementing throttleable instances.");
            THROTTLE_INSTANCES.put(Instant.now());
        } catch (Exception ex) {
            LOGGER.error(">>> An error has occurred: %s", ex);
            throw new RuntimeException(ex.getMessage());
        }
        LOGGER.info(
                String.format(
                        ">>> Current throttle instances: %d, throttle threshold: %d",
                        THROTTLE_INSTANCES.size(),
                        throttleThreshold
                )
        );
        LOGGER.debug("<<< Exiting method");
        return false;
    }

    public void decrementThrottleInstance() {
        LOGGER.debug(">>> Entering method");
        if (THROTTLE_INSTANCES.isEmpty()) {
            LOGGER.info(
                String.format(
                        ">>> Current throttle instances: %d, Throttle instances is empty.",
                        THROTTLE_INSTANCES.size()
                )
        );
            LOGGER.debug("<<< Exiting method");
            return;
        }
        try {
            THROTTLE_INSTANCES.take();
        } catch (Exception ex) {
            LOGGER.error(">>> An error has occurred: %s", ex);
            throw new RuntimeException(ex.getMessage());
        }
        LOGGER.info(
                String.format(
                        ">>> Decremented throttleable instances. Remaining throttleable instances: %d",
                        THROTTLE_INSTANCES.size()
                )
        );
        LOGGER.debug("<<< Exiting method");
    }

    public void resetThrottleInstances() {
        LOGGER.debug(">>> Entering method");
        if (!THROTTLE_INSTANCES.isEmpty()) {
            LOGGER.info(
                String.format(
                        ">>> Current throttle instances: %d, Throttle instances is empty.",
                        THROTTLE_INSTANCES.size()
                )
        );
            LOGGER.info(">>> Clearing throttable instances.");
            THROTTLE_INSTANCES.clear();
            LOGGER.info(String.format(
                        ">>> Current throttle instances: %d, Throttle instances is empty.",
                        THROTTLE_INSTANCES.size()
                )
            );
        }
        LOGGER.debug("<<< Exiting method");
    }
}
