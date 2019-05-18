package com.github.damianmcdonald.webservmon.schedulers;

public interface Scheduler {

    void checkServiceStatus();

    void sendAliveMail();

    void resetThrottleThresholdPeriod();

}
