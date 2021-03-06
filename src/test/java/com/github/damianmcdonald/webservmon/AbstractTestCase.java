package com.github.damianmcdonald.webservmon;

import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import org.apache.commons.mail.util.MimeMessageParser;

import javax.mail.internet.MimeMessage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AbstractTestCase {
    
    static final Logger LOGGER = LoggerFactory.getLogger(AbstractTestCase.class);

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
    String TEXT_FAILED = "FAILED";
    String TEXT_SUCCESS_MILESTONE = "Success milestones achieved";
    String TEXT_EXCEPTION = "Exception";
    String TEXT_FTP_ERRORS = "Error messages from FTP";
    int SMTP_PORT = 2525;
    String FTP_VALID_HOSTNAME = "localhost";
    String FTP_INVALID_HOSTNAME = "INVALID";
    int FTP_PORT = 2100;
    String FTP_TM_DIR = "/GR/TM/XML";
    String FTP_DS_DIR = "/GR/DS/XML";
    String FTP_TM_VALID_ZIP = FTP_TM_DIR.concat("/tmzip.zip");
    String FTP_DS_VALID_ZIP = FTP_DS_DIR.concat("/dszip.zip");
    String FTP_USERNAME = "ftpuser";
    String FTP_VALID_PASSWORD = "12345";
    String FTP_INVALID_PASSWORD = "54321";
    String FTP_ROOT_DIR = "/GR";
    String FTP_VALID_UPLOAD_DIR = "/DS/XML";
    String FTP_INVALID_UPLOAD_DIR = "/DS/XML/INVALID";
    String FTP_UPLOAD_FORMAT = "zip";
    String FTP_DOWNLOAD_DIR = "C:/temp/ftp";
    long FTP_COMPAREDATE_MINUS_DAYS = 1;
    int FTP_COMPAREDATE_WITH_HOURS = 23;
    int FTP_COMPAREDATE_WITH_MINUTES = 55;
    int FTP_COMPAREDATE_WITH_SECONDS = 00;
    String FILE_EXTENSION_TEXT = ".txt";
    String FILE_EXTENSION_HTML = ".html";

    default boolean checkHtmlTemplateHasErrors(final String htmlTemplate) {
        try {
            final StringWebResponse response = new StringWebResponse(htmlTemplate, new URL(MOCK_URL));
            writeHtmlToFile(response, FILE_EXTENSION_HTML);
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
        final String emailContent = new MimeMessageParser(mimeMessage)
                .parse()
                .getPlainContent();
        writeEmailToFile(mimeMessage, FILE_EXTENSION_TEXT);
        return (emailContent.toLowerCase().contains(TEXT_FAILED.toLowerCase())) ? true : false;
    }

    default boolean checkPlainTextEmailHasSuccess(final MimeMessage mimeMessage) throws Exception {
        final String emailContent = new MimeMessageParser(mimeMessage)
                .parse()
                .getPlainContent();
        writeEmailToFile(mimeMessage, FILE_EXTENSION_TEXT);
        return (emailContent.toLowerCase().contains(TEXT_SUCCESS_MILESTONE.toLowerCase())) ? true : false;
    }

    default boolean checkPlainTextEmailHasException(final MimeMessage mimeMessage) throws Exception {
        final String emailContent = new MimeMessageParser(mimeMessage)
                .parse()
                .getPlainContent();
        writeEmailToFile(mimeMessage, FILE_EXTENSION_TEXT);
        return (emailContent.toLowerCase().contains(TEXT_EXCEPTION.toLowerCase())) ? true : false;
    }

    default boolean checkPlainTextEmailHasFtpErrors(final MimeMessage mimeMessage) throws Exception {
        final String emailContent = new MimeMessageParser(mimeMessage)
                .parse()
                .getPlainContent();
        writeEmailToFile(mimeMessage, FILE_EXTENSION_TEXT);
        return (emailContent.toLowerCase().contains(TEXT_FTP_ERRORS.toLowerCase())) ? true : false;
    }

    default void writeEmailToFile(final MimeMessage mimeMessage, final String fileExtension) {
        BufferedWriter writer = null;
        try {
            final String emailContent = new MimeMessageParser(mimeMessage)
                    .parse()
                    .getPlainContent();
            final File tempFile = File.createTempFile(Integer.toString(new Random().nextInt()), fileExtension);
            writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(emailContent);
            LOGGER.info(String.format("***** Merged template can be viewed at %s *****", tempFile));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    default void writeHtmlToFile(final StringWebResponse response, final String fileExtension) {
        BufferedWriter writer = null;
        try {
            final File tempFile = File.createTempFile(Integer.toString(new Random().nextInt()), fileExtension);
            writer = new BufferedWriter(new FileWriter(tempFile));
            writer.write(response.getContentAsString());
            LOGGER.info(String.format("***** Merged template can be viewed at %s *****", tempFile));
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}