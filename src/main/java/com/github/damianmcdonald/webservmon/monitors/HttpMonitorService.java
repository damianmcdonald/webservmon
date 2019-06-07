package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.mailers.HttpMailer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service("httpMonitorService")
public class HttpMonitorService implements MonitorService<String, HttpStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMonitorService.class);

    @Autowired
    private HttpMailer mailer;

    @Autowired
    private RestTemplate restTemplate;

    public Map<String, HttpStatus> checkServiceStatus(final String[] urls) {
        LOGGER.debug(String.format("Executing the service status checks for urls: %s", String.join(",", urls)));
        return Arrays.stream(urls)
                .parallel()
                .collect(HashMap<String, HttpStatus>::new,
                        (m, url) -> {
                            try {
                                m.put(url, doRetryableHttpRequest(url));
                            } catch (RuntimeException ex) {
                                if (ex instanceof HttpClientErrorException) {
                                    final HttpClientErrorException httpClientErrorException = (HttpClientErrorException) ex;
                                    httpClientErrorException.printStackTrace();
                                    m.put(url, httpClientErrorException.getStatusCode());
                                } else  if (ex instanceof HttpServerErrorException) {
                                    final HttpServerErrorException httpServerErrorException = (HttpServerErrorException) ex;
                                    httpServerErrorException.printStackTrace();
                                    m.put(url, httpServerErrorException.getStatusCode());
                                } else {
                                    ex.printStackTrace();
                                    m.put(url, HttpStatus.I_AM_A_TEAPOT); // I_AM_A_TEAPOT indicates unknown error
                                }
                            }
                            catch (Exception ex) {
                                ex.printStackTrace();
                                m.put(url, HttpStatus.I_AM_A_TEAPOT); // I_AM_A_TEAPOT indicates unknown error
                            }
                        },
                        HashMap<String, HttpStatus>::putAll);
    }

    @Retryable(maxAttempts=3, value=RuntimeException.class, backoff = @Backoff(delay = 50000, multiplier=2))
    private HttpStatus doRetryableHttpRequest(final String url) {
        HttpStatus httpStatus = null;
        try {
            httpStatus = new RestTemplate().getForEntity(url, String.class).getStatusCode();
            LOGGER.info(String.format("Tested URL %s returned status code: %s", url, httpStatus));
            switch (httpStatus) {
                case OK:
                    return HttpStatus.OK;
                default:
                    throw new HttpClientErrorException(httpStatus);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
