package com.github.damianmcdonald.webservmon.rules;

import org.junit.rules.ExternalResource;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;

import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class HttpServerRule extends ExternalResource {

    protected static final String HTTP_HOSTNAME = "localhost";
    protected static final String HTTP_METHOD_GET = "GET";
    protected static final String HTTP_PATH_1 = "/testservice1";
    protected static final String HTTP_PATH_2 = "/testservice2";
    protected static final int HTTP_STATUS_SUCCESS_INT = 200;
    protected static final int HTTP_STATUS_ERROR_INT = 503;
    protected static final int HTTP_STATUS_UNKNOWN_INT = 418;

    private int port;

    private static ClientAndServer mockServer;

    public HttpServerRule(final int port) {
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        mockServer = startClientAndServer(port);
    }

    public void createExpectationForAliveService() {
        new MockServerClient(HTTP_HOSTNAME, port)
                .when(
                        request()
                                .withMethod(HTTP_METHOD_GET)
                                .withPath(HTTP_PATH_1),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(HTTP_STATUS_SUCCESS_INT)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
        new MockServerClient(HTTP_HOSTNAME, port)
                .when(
                        request()
                                .withMethod(HTTP_METHOD_GET)
                                .withPath(HTTP_PATH_2),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(HTTP_STATUS_SUCCESS_INT)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    public void createExpectationForDeadService() {
        new MockServerClient(HTTP_HOSTNAME, port)
                .when(
                        request()
                                .withMethod(HTTP_METHOD_GET)
                                .withPath(HTTP_PATH_1),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(HTTP_STATUS_ERROR_INT)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
        new MockServerClient(HTTP_HOSTNAME, port)
                .when(
                        request()
                                .withMethod(HTTP_METHOD_GET)
                                .withPath(HTTP_PATH_2),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(HTTP_STATUS_SUCCESS_INT)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    public void createExpectationForUnknownService() {
        new MockServerClient(HTTP_HOSTNAME, port)
                .when(
                        request()
                                .withMethod(HTTP_METHOD_GET)
                                .withPath(HTTP_PATH_1),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(HTTP_STATUS_UNKNOWN_INT)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
        new MockServerClient(HTTP_HOSTNAME, port)
                .when(
                        request()
                                .withMethod(HTTP_METHOD_GET)
                                .withPath(HTTP_PATH_2),
                        exactly(1)
                )
                .respond(
                        response()
                                .withStatusCode(HTTP_STATUS_SUCCESS_INT)
                                .withDelay(TimeUnit.SECONDS, 1)
                );
    }

    @Override
    protected void after() {
        super.after();
        mockServer.stop();
    }
}
