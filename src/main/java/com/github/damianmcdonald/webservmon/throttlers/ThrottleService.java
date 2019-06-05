package com.github.damianmcdonald.webservmon.throttlers;

public interface ThrottleService {

    boolean applyThrottle();

    void decrementThrottleInstance();

    void resetThrottleInstances();

}
