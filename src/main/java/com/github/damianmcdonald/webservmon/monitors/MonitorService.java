package com.github.damianmcdonald.webservmon.monitors;

import org.springframework.stereotype.Component;

import java.util.Map;

public interface MonitorService<T1,T2> {

    public Map<T1, T2> checkServiceStatus(final String[] endpoints);
}
