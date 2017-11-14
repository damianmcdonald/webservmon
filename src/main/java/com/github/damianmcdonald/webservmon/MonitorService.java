package com.github.damianmcdonald.webservmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class MonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Mailer.class);

    @Autowired
    private Mailer mailer;

    public Map<String, HttpStatus> checkServiceStatus(final String[] urls) {
        LOGGER.debug(String.format("Executing the service status checks for urls: %s", String.join(",", urls)));
        return Arrays.stream(urls)
                .collect(HashMap<String, HttpStatus>::new,
                        (m, url) -> m.put(url, new RestTemplate().getForEntity(url, String.class).getStatusCode()),
                        HashMap<String, HttpStatus>::putAll);
    }

}
