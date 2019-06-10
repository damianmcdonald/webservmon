package com.github.damianmcdonald.webservmon.scheduler;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.rules.HttpServerRule;
import com.github.damianmcdonald.webservmon.rules.SmtpServerRule;
import com.github.damianmcdonald.webservmon.schedulers.Scheduler;
import com.github.damianmcdonald.webservmon.throttlers.HttpThrottleService;
import com.github.damianmcdonald.webservmon.throttlers.ThrottleService;
import com.icegreen.greenmail.store.FolderException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockserver.integration.ClientAndServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.mail.internet.MimeMessage;
import java.time.Instant;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class HttpSchedulerTest implements AbstractTestCase {

    private static ClientAndServer mockServer;

    @Value("${http.mail.subject.alive}")
    private String mailAliveSubject;

    @Value("${http.mail.subject.error}")
    private String mailErrorSubject;

    @Value("${http.mail.to}")
    private String[] mailTo;

    @Autowired
    @Qualifier("httpScheduler")
    private Scheduler scheduler;

    @Autowired
    @Qualifier("httpThrottleService")
    private ThrottleService throttleService;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Rule
    public HttpServerRule httpServerRule = new HttpServerRule(1080);

    @Before
    public void beforeTestRun() throws FolderException {
        smtpServerRule.purgeMessages();
        throttleService.resetThrottleInstances();
    }

    @Test
    public void checkServiceStatusTest() throws Exception {
        httpServerRule.createExpectationForAliveService();
        scheduler.checkServiceStatus();
    }

    @Test
    public void sendAliveEmailTest() throws Exception {
        httpServerRule.createExpectationForAliveService();
        scheduler.sendAliveMail();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailAliveSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertFalse(checkPlainTextEmailHasErrors(email));
    }

    @Test
    public void sendErrorEmailTest() throws Exception {
        httpServerRule.createExpectationForDeadService();
        scheduler.checkServiceStatus();
        final MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        Assert.assertEquals(1, receivedMessages.length);
        final MimeMessage email = receivedMessages[0];
        Assert.assertEquals(mailErrorSubject, email.getSubject());
        Assert.assertEquals(mailTo[0], email.getAllRecipients()[0].toString());
        Assert.assertTrue(checkPlainTextEmailHasErrors(email));
    }

    @Test
    public void resetThrottleThresholdPeriodTest() throws InterruptedException {
        Assert.assertEquals(0, HttpThrottleService.THROTTLE_INSTANCES.size());
        HttpThrottleService.THROTTLE_INSTANCES.put(Instant.now());
        HttpThrottleService.THROTTLE_INSTANCES.put(Instant.now());
        HttpThrottleService.THROTTLE_INSTANCES.put(Instant.now());
        Assert.assertEquals(3, HttpThrottleService.THROTTLE_INSTANCES.size());
        scheduler.resetThrottleThresholdPeriod();
        Assert.assertEquals(0, HttpThrottleService.THROTTLE_INSTANCES.size());
    }
}