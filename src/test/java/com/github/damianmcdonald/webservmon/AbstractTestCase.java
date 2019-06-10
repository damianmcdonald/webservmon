package com.github.damianmcdonald.webservmon;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import org.apache.commons.mail.util.MimeMessageParser;

import javax.mail.internet.MimeMessage;
import java.net.URL;
import java.util.List;

public interface AbstractTestCase {

    String MOCK_URL = "http://localhost:1080";
    String TEST_SERVICE_1_URL = "http://localhost:1080/testservice1";
    String TEST_SERVICE_2_URL = "http://localhost:1080/testservice2";
    String HTTP_STATUS_SUCCESS = "200";
    String HTTP_STATUS_ERROR = "503";
    String HTTP_STATUS_UNKNOWN = "418";
    String MODEL_RESULTS = "results";
    String HTML_TABLE_CELLS_SUCCESS = "//td[@class='http-success']";
    String HTML_TABLE_CELLS_ERROR = "//td[@class='http-error']";
    String HTML_TABLE_CELLS_UNKNOWN = "//td[@class='http-unknown']";
    String FAILED_TEXT= "FAILED";
    int SMTP_PORT = 2525;
    String FTP_HOSTNAME = "localhost";
    int FTP_PORT = 2100;
    String FTP_TM_DIR = "/GR/TM/XML";
    String FTP_DS_DIR = "/GR/DS/XML";
    String FTP_VALID_ZIP = FTP_TM_DIR.concat("/12345678.zip");
    String FTP_VALID_ZIP_NAME = FTP_TM_DIR.concat("/12345678.zip");
    String FTP_INVALID_ZIP = FTP_TM_DIR.concat("/87654321.zip");
    String FTP_USERNAME = "ftpuser";
    String FTP_VALID_PASSWORD = "12345";
    String FTP_INVALID_PASSWORD = "54321";

    default boolean checkHtmlTemplateHasErrors(final String htmlTemplate) {
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

    default boolean checkPlainTextEmailHasErrors(final MimeMessage mimeMessage) throws Exception {
        final String emailContent =  new MimeMessageParser(mimeMessage)
                .parse()
                .getPlainContent();
        return (emailContent.contains(FAILED_TEXT)) ? true : false;
    }

}