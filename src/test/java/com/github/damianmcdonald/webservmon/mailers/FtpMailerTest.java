package com.github.damianmcdonald.webservmon.mailers;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.domain.FtpMonitorStatus;
import com.github.damianmcdonald.webservmon.rules.SmtpServerRule;
import com.icegreen.greenmail.store.FolderException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class FtpMailerTest implements AbstractTestCase {

    @Value("${ftp.mail.subject.success}")
    private String mailSuccessSubject;

    @Value("${ftp.mail.subject.warn}")
    private String mailWarnSubject;

    @Value("${ftp.mail.subject.error}")
    private String mailErrorSubject;

    @Value("${ftp.mail.to}")
    private String[] mailTo;

    @Autowired
    private FtpMailer mailer;

    @Mock
    private FtpMonitorStatus monitorStatus;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(SMTP_PORT);

    @Before
    public void beforeTestRun() throws FolderException {
        smtpServerRule.purgeMessages();
    }

    @Test
    public void sendMailValidTmBeforeThresholdTest() throws Exception {
        Mockito.when(monitorStatus.getCorrelationId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(monitorStatus.isTrademark()).thenReturn(true);
        Mockito.when(monitorStatus.isFinalResult()).thenReturn(false);
        Mockito.when(monitorStatus.getException()).thenReturn(new String());
        Mockito.when(monitorStatus.getErrorsDownload()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsUnzip()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsXmlValidation()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getSuccessMilestones()).thenReturn(getSuccessMilestones());
        mailer.sendMail(monitorStatus);
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailSuccessSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasSuccess(email));
    }

    @Test
    public void sendMailExceptionDsBeforeThresholdTest() throws Exception {
        Mockito.when(monitorStatus.getCorrelationId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(monitorStatus.isTrademark()).thenReturn(false);
        Mockito.when(monitorStatus.isFinalResult()).thenReturn(false);
        Mockito.when(monitorStatus.getException()).thenReturn(ExceptionUtils.getStackTrace(new RuntimeException("Test exception message")));
        Mockito.when(monitorStatus.getErrorsDownload()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsUnzip()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsXmlValidation()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getSuccessMilestones()).thenReturn(new ArrayList<String>());
        mailer.sendMail(monitorStatus);
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailWarnSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasException(email));
    }

    @Test
    public void sendMailValidTmAfterThresholdTest() throws Exception {
        Mockito.when(monitorStatus.getCorrelationId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(monitorStatus.isTrademark()).thenReturn(true);
        Mockito.when(monitorStatus.isFinalResult()).thenReturn(true);
        Mockito.when(monitorStatus.getException()).thenReturn(new String());
        Mockito.when(monitorStatus.getErrorsDownload()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsUnzip()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsXmlValidation()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getSuccessMilestones()).thenReturn(getSuccessMilestones());
        mailer.sendMail(monitorStatus);
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailSuccessSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasSuccess(email));
    }

    @Test
    public void sendMailTmAfterThresholdWithErrorsTest() throws Exception {
        Mockito.when(monitorStatus.getCorrelationId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(monitorStatus.isTrademark()).thenReturn(true);
        Mockito.when(monitorStatus.isFinalResult()).thenReturn(true);
        Mockito.when(monitorStatus.getException()).thenReturn(new String());
        Mockito.when(monitorStatus.getErrorsDownload()).thenReturn(getErrorDownloadMessages());
        Mockito.when(monitorStatus.getErrorsUnzip()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getErrorsXmlValidation()).thenReturn(new ArrayList<String>());
        Mockito.when(monitorStatus.getSuccessMilestones()).thenReturn(new ArrayList<String>());
        mailer.sendMail(monitorStatus);
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasFtpErrors(email));
    }

    private List<String> getSuccessMilestones(){
        final List<String> successMilestones = new ArrayList();
        successMilestones.add("Success milestone message 1");
        successMilestones.add("Success milestone message 2");
        successMilestones.add("Success milestone message 3");
        return successMilestones;
    }

    private List<String> getErrorDownloadMessages(){
        final List<String> errorDownload = new ArrayList();
        errorDownload.add("Error download message 1");
        errorDownload.add("Error download message 2");
        errorDownload.add("Error download message 3");
        return errorDownload;
    }
}