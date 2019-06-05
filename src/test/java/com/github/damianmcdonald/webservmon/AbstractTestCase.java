package com.github.damianmcdonald.webservmon;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import org.apache.commons.mail.util.MimeMessageParser;
import org.mockserver.client.MockServerClient;

import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public abstract class AbstractTestCase {

    protected static final String MOCK_URL = "http://localhost:1080";
    protected static final String HTTP_HOSTNAME = "localhost";
    protected static final int HTTP_PORT = 1080;
    protected static final String HTTP_METHOD_GET = "GET";
    protected static final String HTTP_PATH_1 = "/testservice1";
    protected static final String HTTP_PATH_2 = "/testservice2";
    protected static final int HTTP_STATUS_SUCCESS_INT = 200;
    protected static final int HTTP_STATUS_ERROR_INT = 503;
    protected static final int HTTP_STATUS_UNKNOWN_INT = 418;
    protected static final String TEST_SERVICE_1_URL = "http://localhost:1080/testservice1";
    protected static final String TEST_SERVICE_2_URL = "http://localhost:1080/testservice2";
    protected static final String HTTP_STATUS_SUCCESS = "200";
    protected static final String HTTP_STATUS_ERROR = "503";
    protected static final String HTTP_STATUS_UNKNOWN = "418";
    protected static final String MODEL_RESULTS = "results";
    protected static final String HTML_TABLE_CELLS_SUCCESS = "//td[@class='http-success']";
    protected static final String HTML_TABLE_CELLS_ERROR = "//td[@class='http-error']";
    protected static final String HTML_TABLE_CELLS_UNKNOWN = "//td[@class='http-unknown']";
    protected static final String FAILED_TEXT= "FAILED";


    protected boolean checkHtmlTemplateHasErrors(final String htmlTemplate) {
        try {
            final StringWebResponse response = new StringWebResponse(htmlTemplate, new URL(MOCK_URL));
            final HtmlPage page = HTMLParser.parseHtml(response, new WebClient().getCurrentWindow());
            final List<HtmlTableCell> httpErrors = page.getByXPath(HTML_TABLE_CELLS_ERROR);
            final List<HtmlTableCell> httpUnknowns = page.getByXPath(HTML_TABLE_CELLS_UNKNOWN);
            final List<HtmlTableCell> httpSuccess = page.getByXPath(HTML_TABLE_CELLS_SUCCESS);
            if (!httpSuccess.isEmpty() && (httpUnknowns.isEmpty() && httpErrors.isEmpty())) return false;
            if (!httpSuccess.isEmpty() && (!httpErrors.isEmpty() && httpUnknowns.isEmpty())) return true;
            if (!httpSuccess.isEmpty() && (!httpUnknowns.isEmpty() && httpErrors.isEmpty())) return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    protected boolean checkPlainTextEmailHasErrors(final MimeMessage mimeMessage) throws Exception {
        final String emailContent =  new MimeMessageParser(mimeMessage)
                .parse()
                .getPlainContent();
        return (emailContent.contains(FAILED_TEXT)) ? true : false;
    }

    protected void createExpectationForAliveService() {
        new MockServerClient(HTTP_HOSTNAME, HTTP_PORT)
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
        new MockServerClient(HTTP_HOSTNAME, HTTP_PORT)
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

    protected void createExpectationForDeadService() {
        new MockServerClient(HTTP_HOSTNAME, HTTP_PORT)
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
        new MockServerClient(HTTP_HOSTNAME, HTTP_PORT)
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

    protected void createExpectationForUnknownService() {
        new MockServerClient(HTTP_HOSTNAME, HTTP_PORT)
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
        new MockServerClient(HTTP_HOSTNAME, HTTP_PORT)
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
}
