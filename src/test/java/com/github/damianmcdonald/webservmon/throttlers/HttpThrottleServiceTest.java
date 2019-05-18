package com.github.damianmcdonald.webservmon.throttlers;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpThrottleServiceTest extends AbstractTestCase {

    private static final int LOOP_MAX = 25;
    private static final int LOOP_MIN = 0;
    private static final int MAX_THROTTLE_INSTANCES = 2;
    private static final int EMPTY_THROTTLE_INSTANCES = 0;
    private static final int SINGLE_THROTTLE_INSTANCE = 1;

    @Autowired
    private HttpThrottleService throttleService;

    @Test
    public void applyThrottleTest() {
        throttleService.applyThrottle();
        Assert.assertEquals(SINGLE_THROTTLE_INSTANCE, HttpThrottleService.THROTTLE_INSTANCES.size());
        throttleService.applyThrottle();
        Assert.assertEquals(MAX_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
        for(int i = LOOP_MIN; i<LOOP_MAX; i++) {
            throttleService.applyThrottle();
        }
        // validate that the number of THROTTLE_INSTANCES has been maintained at 2
        Assert.assertEquals(MAX_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
    }

    @Test
    public void decrementThrottleTest() {
        for(int i = LOOP_MIN; i<LOOP_MAX; i++) {
            throttleService.applyThrottle();
        }
        Assert.assertEquals(MAX_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
        throttleService.decrementThrottleInstance();
        Assert.assertEquals(SINGLE_THROTTLE_INSTANCE, HttpThrottleService.THROTTLE_INSTANCES.size());
        throttleService.decrementThrottleInstance();
        Assert.assertEquals(EMPTY_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
        for(int i = LOOP_MIN; i<LOOP_MAX; i++) {
            throttleService.decrementThrottleInstance();
        }
        Assert.assertEquals(EMPTY_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
    }

    @Test
    public void resetThrottleInstancesTest() {
        for(int i = LOOP_MIN; i<LOOP_MAX; i++) {
            throttleService.applyThrottle();
        }
        Assert.assertEquals(MAX_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
        throttleService.resetThrottleInstances();
        Assert.assertEquals(EMPTY_THROTTLE_INSTANCES, HttpThrottleService.THROTTLE_INSTANCES.size());
    }
}
