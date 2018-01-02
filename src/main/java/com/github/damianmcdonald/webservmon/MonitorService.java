package com.github.damianmcdonald.webservmon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class MonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorService.class);

    @Autowired
    private Mailer mailer;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, HttpStatus> checkServiceStatus(final String[] urls) {
        LOGGER.debug(String.format("Executing the service status checks for urls: %s", String.join(",", urls)));
        return Arrays.stream(urls)
                .parallel()
                .collect(HashMap<String, HttpStatus>::new,
                        (m, url) -> {
                            try {
                                m.put(url, new RestTemplate().getForEntity(url, String.class).getStatusCode());
                            } catch (HttpStatusCodeException ex) {
                                ex.printStackTrace();
                                m.put(url, ex.getStatusCode());
                            } catch (ResourceAccessException ex) {
                                ex.printStackTrace();
                                 if(ex.getCause() != null &&
                                         (ex.getCause() instanceof ConnectException
                                         || ex.getCause() instanceof UnknownHostException)
                                         ) {
                                     m.put(url, HttpStatus.REQUEST_TIMEOUT);
                                } else {
                                    m.put(url, HttpStatus.I_AM_A_TEAPOT); // I_AM_A_TEAPOT indicates unknown error
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                m.put(url, HttpStatus.I_AM_A_TEAPOT); // I_AM_A_TEAPOT indicates unknown error
                            }
                        },
                        HashMap<String, HttpStatus>::putAll);
    }

}
