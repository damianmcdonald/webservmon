package com.github.damianmcdonald.webservmon.domain;

import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.http.HttpStatus;

public class HttpMonitorStatus {

    private final String correlationId;

    private final Map<String, HttpStatus> statusChecks;

    public HttpMonitorStatus(final String correlationId, final Map<String, HttpStatus> statusChecks) {
        this.correlationId = correlationId;
        this.statusChecks = statusChecks;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public Map<String, HttpStatus> getStatusChecks() {
        return statusChecks;
    }
    
        public String toString() {
        return new ToStringBuilder(this)
                .append("correlationId", correlationId)
                .append("statusChecks", statusChecks)
                .toString();
    }

}
