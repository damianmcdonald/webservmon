package com.github.damianmcdonald.webservmon.scheduler;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.domain.FtpSettings;
import com.github.damianmcdonald.webservmon.domain.Result;
import com.github.damianmcdonald.webservmon.monitors.FtpMonitorService;
import com.github.damianmcdonald.webservmon.rules.SmtpServerRule;
import com.github.damianmcdonald.webservmon.schedulers.FtpScheduler;
import com.icegreen.greenmail.store.FolderException;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class FtpSchedulerTest implements AbstractTestCase {

    private static final String KEY_ERRORS_DOWNLOAD = "ERRORS_DOWNLOAD";
    private static final String KEY_ERRORS_UNZIP = "ERRORS_UNZIP";
    private static final String KEY_ERRORS_XML_VALIDATE = "ERRORS_XML_VALIDATE";
    private static final String KEY_SUCCESS = "SUCCESS";
    private static final String KEY_SPLITTER = ";::::;";
    private static final String EXCEPTION_ERROR_TEXT = "Test exception message cause";

    @Value("${ftp.mail.subject.success}")
    private String mailSuccessSubject;

    @Value("${ftp.mail.subject.warn}")
    private String mailWarnSubject;

    @Value("${ftp.mail.subject.error}")
    private String mailErrorSubject;

    @Value("${ftp.mail.to}")
    private String[] mailTo;

    @Autowired
    private FtpScheduler scheduler;

    @MockBean
    private FtpMonitorService monitorService;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Before
    public void beforeTestRun() throws FolderException {
        smtpServerRule.purgeMessages();
        ThreadContext.clearStack();
    }

    @Test
     public void checkValidTmFtpStatusBeforeThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.NO_ERRORS);
        createFtpMonitorStatusWithSuccessMilestones();
        scheduler.checkTmFtpStatusBeforeThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailSuccessSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasSuccess(email));
    }

    @Test
    public void checkValidDsFtpStatusBeforeThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.NO_ERRORS);
        createFtpMonitorStatusWithSuccessMilestones();
        scheduler.checkDsFtpStatusBeforeThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailSuccessSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasSuccess(email));
    }

    @Test
    public void checkWithExceptionTmFtpStatusBeforeThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenThrow(new RuntimeException(EXCEPTION_ERROR_TEXT));
        scheduler.checkTmFtpStatusBeforeThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailWarnSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasException(email));
    }

    @Test
    public void checkWithExceptionDsFtpStatusBeforeThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenThrow(new RuntimeException(EXCEPTION_ERROR_TEXT));
        scheduler.checkDsFtpStatusBeforeThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailWarnSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasException(email));
    }

    @Test
    public void checkWithErrorsTmFtpStatusBeforeThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.WITH_ERRORS);
        createFtpMonitorStatusWithErrors();
        scheduler.checkTmFtpStatusBeforeThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailWarnSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasFtpErrors(email));
    }

    @Test
    public void checkWithErrorsDsFtpStatusBeforeThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.WITH_ERRORS);
        createFtpMonitorStatusWithErrors();
        scheduler.checkDsFtpStatusBeforeThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailWarnSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasFtpErrors(email));
    }

    @Test
    public void checkValidTmFtpStatusAfterThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.NO_ERRORS);
        createFtpMonitorStatusWithSuccessMilestones();
        scheduler.checkTmFtpStatusAfterThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailSuccessSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasSuccess(email));
    }

    @Test
    public void checkValidDsFtpStatusAfterThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.NO_ERRORS);
        createFtpMonitorStatusWithSuccessMilestones();
        scheduler.checkDsFtpStatusAfterThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailSuccessSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasSuccess(email));
    }

    @Test
    public void checkWithExceptionTmFtpStatusAfterThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenThrow(new RuntimeException(EXCEPTION_ERROR_TEXT));
        scheduler.checkTmFtpStatusAfterThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasException(email));
    }

    @Test
    public void checkWithExceptionDsFtpStatusAfterThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenThrow(new RuntimeException(EXCEPTION_ERROR_TEXT));
        scheduler.checkDsFtpStatusAfterThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasException(email));
    }

    @Test
    public void checkWithErrorsTmFtpStatusAfterThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.WITH_ERRORS);
        createFtpMonitorStatusWithErrors();
        scheduler.checkTmFtpStatusAfterThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasFtpErrors(email));
    }

    @Test
    public void checkWithErrorsDsFtpStatusAfterThresholdTest() throws Exception {
        Mockito.when(monitorService.checkServiceStatus(Mockito.isA(FtpSettings.class))).thenReturn(Result.WITH_ERRORS);
        createFtpMonitorStatusWithErrors();
        scheduler.checkDsFtpStatusAfterThreshold();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasFtpErrors(email));
    }

    private void createFtpMonitorStatusWithSuccessMilestones() {
        final StringBuilder successMilestones = new StringBuilder();
        successMilestones.append(String.format("Success milestone message 1%s", KEY_SPLITTER));
        successMilestones.append(String.format("Success milestone message 2%s", KEY_SPLITTER));
        successMilestones.append(String.format("Success milestone message 3"));
        ThreadContext.put(KEY_SUCCESS, successMilestones.toString());
        ThreadContext.remove(KEY_ERRORS_DOWNLOAD);
        ThreadContext.remove(KEY_ERRORS_UNZIP);
        ThreadContext.remove(KEY_ERRORS_XML_VALIDATE);
    }

    private void createFtpMonitorStatusWithErrors() {
        final StringBuilder errorDownload = new StringBuilder();
        errorDownload.append(String.format("Error download message 1%s", KEY_SPLITTER));
        errorDownload.append(String.format("Error download message 2%s", KEY_SPLITTER));
        errorDownload.append(String.format("Error download message 3"));
        ThreadContext.put(KEY_ERRORS_DOWNLOAD, errorDownload.toString());

        final StringBuilder errorUnzip = new StringBuilder();
        errorUnzip.append(String.format("Error unzip message 1%s", KEY_SPLITTER));
        errorUnzip.append(String.format("Error unzip message 2%s", KEY_SPLITTER));
        errorUnzip.append(String.format("Error unzip message 3"));
        ThreadContext.put(KEY_ERRORS_UNZIP, errorUnzip.toString());

        final StringBuilder errorValidateXml = new StringBuilder();
        errorValidateXml.append(String.format("Error validate xml message 1%s", KEY_SPLITTER));
        errorValidateXml.append(String.format("Error validate xml 2%s", KEY_SPLITTER));
        errorValidateXml.append(String.format("Error validate xml 3"));
        ThreadContext.put(KEY_ERRORS_XML_VALIDATE, errorValidateXml.toString());

        ThreadContext.remove(KEY_SUCCESS);
    }

}