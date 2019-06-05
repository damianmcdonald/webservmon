package com.github.damianmcdonald.webservmon.mailers;

import com.github.damianmcdonald.webservmon.templators.Templator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Component("httpMailer")
public class HttpMailer implements Mailer<String, HttpStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMailer.class);

    private static final String RESULT_KEY = "results";

    private final static String TEMPLATE_FILE = "http-report-text.ftl";

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Templator templator;

    @Value("${http.mail.from}")
    private String mailFrom;

    @Value("${http.mail.to}")
    private String[] mailTo;

    @Value("${http.mail.subject.alive}")
    private String mailAliveSubject;

    @Value("${http.mail.subject.error}")
    private String mailErrorSubject;

    public void sendMail(final boolean hasErrors, final Map<String, HttpStatus> results) {
        final String mailSubject = hasErrors ? mailErrorSubject : mailAliveSubject;
        try {
            LOGGER.debug(String.format("Sending email with subject: %s", mailSubject));
            javaMailSender.send(constructMail(mailSubject, results));
        } catch (Exception ex) {
            ex.getMessage();
            LOGGER.error("An error has ocurred.", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private MimeMessage constructMail(final String subject, final Map<String, HttpStatus> results) {
        final MimeMessage mail = javaMailSender.createMimeMessage();
        final HashMap model = new HashMap();
        model.put(RESULT_KEY, results);
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(mailTo);
            helper.setFrom(mailFrom);
            helper.setSubject(subject);
            helper.setText(templator.getMergedTemplate(model, TEMPLATE_FILE));
            return mail;
        } catch (Exception ex) {
            ex.getMessage();
            LOGGER.error("An error has ocurred.", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

}
