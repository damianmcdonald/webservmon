package com.github.damianmcdonald.webservmon.mailers;

import com.github.damianmcdonald.webservmon.domain.HttpMonitorStatus;
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
import java.util.stream.Collectors;
import org.apache.logging.log4j.ThreadContext;

@Component
public class HttpMailer {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpMailer.class);

    private static final String RESULT_KEY = "results";

    private final static String TEMPLATE_FILE = "http-report-text.ftl";
    
    private static final String KEY_CORRELATION_ID = "CORRELATION_ID";

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
        LOGGER.debug(String.format(
                        ">>> Entering method with parameters: hasErrors=%b, results=%s"
                        , hasErrors
                        , results.keySet().stream()
                                .map(key -> String.format("%s = %s", key, results.get(key)))
                                .collect(Collectors.joining(", ", "{", "}"))
                )
        );
        final String mailSubject = hasErrors ? mailErrorSubject : mailAliveSubject;
        try {
            LOGGER.info(String.format(
                            "Sending email with subject: %s, results: %s"
                            , mailSubject
                            , results.keySet().stream()
                                    .map(key -> String.format("%s = %s", key, results.get(key)))
                                    .collect(Collectors.joining(", ", "{", "}"))
                    )
            );
            javaMailSender.send(constructMail(mailSubject, results));
            LOGGER.debug("<<< Exiting method.");
        } catch (Exception ex) {
            LOGGER.error(">>> An error has occurred.", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private MimeMessage constructMail(final String subject, final Map<String, HttpStatus> results) {
        LOGGER.debug(String.format(
                        ">>> Entering method with parameters: subject=%s, results=%s"
                        , subject
                        , results.keySet().stream()
                                .map(key -> String.format("%s = %s", key, results.get(key)))
                                .collect(Collectors.joining(", ", "{", "}"))
                )
        );
        final MimeMessage mail = javaMailSender.createMimeMessage();
        final HashMap model = new HashMap();
        model.put(RESULT_KEY, new HttpMonitorStatus(ThreadContext.get(KEY_CORRELATION_ID), results));
        LOGGER.info(String.format(">>> Objects placed in model for merging: %s into email template: %s",
                        results.keySet().stream()
                                .map(key -> String.format("%s = %s", key, results.get(key)))
                                .collect(Collectors.joining(", ", "{", "}"))
                        , TEMPLATE_FILE
                )
        );
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(mailTo);
            helper.setFrom(mailFrom);
            helper.setSubject(subject);
            helper.setText(templator.getMergedTemplate(model, TEMPLATE_FILE));
            LOGGER.debug("<<< Exiting method.");
            return mail;
        } catch (Exception ex) {
            LOGGER.error(">>> An error has occurred.", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

}
