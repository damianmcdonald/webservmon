package com.github.damianmcdonald.webservmon.mailers;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.rules.SmtpServerRule;
import com.icegreen.greenmail.store.FolderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpMailerTest implements AbstractTestCase {

    @Value("${http.mail.subject.error}")
    private String mailErrorSubject;

    @Value("${http.mail.to}")
    private String[] mailTo;

    @Autowired
    private HttpMailer mailer;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(SMTP_PORT);

    @Before
    public void beforeTestRun() throws FolderException {
        smtpServerRule.purgeMessages();
    }

    @Test
    public void sendMailTestNoErrors() throws Exception {
        final HashMap<String, HttpStatus> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HttpStatus.OK);
        results.put(TEST_SERVICE_2_URL, HttpStatus.OK);
        mailer.sendMail(true, results);
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertFalse(checkPlainTextEmailHasErrors(email));

    }

    @Test
    public void sendMailTestWithErrors() throws Exception {
        final HashMap<String, HttpStatus> results = new HashMap<>();
        results.put(TEST_SERVICE_1_URL, HttpStatus.OK);
        results.put(TEST_SERVICE_2_URL, HttpStatus.SERVICE_UNAVAILABLE);
        mailer.sendMail(true, results);
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasErrors(email));
    }

}