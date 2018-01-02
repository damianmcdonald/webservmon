package com.github.damianmcdonald.webservmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@Component
public class ThrottleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ThrottleService.class);
    
    public static final BlockingQueue<Instant> THROTTLE_INSTANCES = new ArrayBlockingQueue<Instant>(100);
    
    @Value("${throttleable.instance.threshold}")
    private int throttleThreshold;

    public boolean applyThrottle(){
      LOGGER.debug(String.format("Throttle threshold == %d", throttleThreshold));
      if (THROTTLE_INSTANCES.size() >= throttleThreshold) return true;
      LOGGER.debug("Throttle threshold has not been breached. Incrementing throttleable instances.");
        try {
            THROTTLE_INSTANCES.put(Instant.now());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        LOGGER.debug(String.format("Current throttleable instances == %d", THROTTLE_INSTANCES.size()));
      return false;
    }
    
    public void decrementThrottleInstance() {
      if (THROTTLE_INSTANCES.isEmpty()) return;
        try {
            THROTTLE_INSTANCES.take();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex.getMessage());
        }
        LOGGER.debug(String.format("Decrementing throttleable instances. Remaining throttleable instances == %d", THROTTLE_INSTANCES.size()));
    }

    public void resetThrottleInstances() {
      LOGGER.debug("Clearing remaining throttleable instances.");
      if(!THROTTLE_INSTANCES.isEmpty()) THROTTLE_INSTANCES.clear();
      LOGGER.debug(String.format("THROTTLE_INSTANCES is empty == %s", THROTTLE_INSTANCES.isEmpty()));
    }
}
