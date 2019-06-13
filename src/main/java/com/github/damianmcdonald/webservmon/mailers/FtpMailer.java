package com.github.damianmcdonald.webservmon.mailers;

import com.github.damianmcdonald.webservmon.domain.FtpMonitorStatus;
import com.github.damianmcdonald.webservmon.templators.Templator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class FtpMailer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpMailer.class);

    private static final String RESULT_KEY = "results";

    private final static String TEMPLATE_FILE_TM = "ftp-tm-report-text.ftl";

    private final static String TEMPLATE_FILE_DS = "ftp-ds-report-text.ftl";

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Templator templator;

    @Value("${ftp.mail.from}")
    private String mailFrom;

    @Value("${ftp.mail.to}")
    private String[] mailTo;

    @Value("${ftp.mail.subject.success}")
    private String mailSuccessSubject;

    @Value("${ftp.mail.subject.warn}")
    private String mailWarnSubject;

    @Value("${ftp.mail.subject.error}")
    private String mailErrorSubject;

    public void sendMail(final FtpMonitorStatus status, final boolean isTrademark, final boolean isFinalResult) {
        LOGGER.debug(String.format(
                        ">>> Entering method with parameters: status=%s, isTrademark=%b, isFinalResult=%b"
                        , isTrademark
                        , isFinalResult
                        , status
                )
        );
        final String mailSubject = getSubject(status, isFinalResult);
        try {
            LOGGER.info(String.format(
                            "Sending email with subject: %s, isFinalResult: %b and ftp status: %s"
                            , mailSubject
                            , isFinalResult
                            , status
                    )
            );
            javaMailSender.send(constructMail(mailSubject, isTrademark, status));
            LOGGER.debug("<<< Exiting method.");
        } catch (Exception ex) {
            LOGGER.error(">>> An error has occurred: %s", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private MimeMessage constructMail(final String subject, final boolean isTrademark, final FtpMonitorStatus status) {
        LOGGER.debug(String.format(
                        ">>> Entering method with parameters: subject=%s, isTrademark=%b, status=%s"
                        , subject
                        , isTrademark
                        , status
                )
        );
        final String template = isTrademark ? TEMPLATE_FILE_TM : TEMPLATE_FILE_DS;
        final MimeMessage mail = javaMailSender.createMimeMessage();
        final HashMap model = new HashMap();
        model.put(RESULT_KEY, status);
        LOGGER.info(String.format(">>> Objects placed in model for merging: %s into email template: %s",
                        model.keySet().stream()
                                .map(key -> String.format("%s = %s", key, model.get(key)))
                                .collect(Collectors.joining(", ", "{", "}"))
                        , template
                )
        );
        try {
            final MimeMessageHelper helper = new MimeMessageHelper(mail, true);
            helper.setTo(mailTo);
            helper.setFrom(mailFrom);
            helper.setSubject(subject);
            helper.setText(templator.getMergedTemplate(model, template));
            LOGGER.debug("<<< Exiting method.");
            return mail;
        } catch (Exception ex) {
            LOGGER.error(">>> An error has occurred: %s", ex);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private String getSubject(final FtpMonitorStatus status, final boolean isFinalResult) {
        LOGGER.debug(String.format(
                        ">>> Entering method with parameters: status=%s, isFinalResult=%b"
                        , status
                        , isFinalResult
                )
        );
        if (StringUtils.isNotEmpty(status.getException())
                || !status.getErrorsDownload().isEmpty()
                || !status.getErrorsUnzip().isEmpty()
                || !status.getErrorsXmlValidation().isEmpty()
                ) {
            if (isFinalResult) {
                LOGGER.error(String.format("Email subject is: %s", mailErrorSubject));
                LOGGER.debug("<<< Exiting method.");
                return mailErrorSubject;
            }
            LOGGER.warn(String.format("Email subject is: %s", mailWarnSubject));
            LOGGER.debug("<<< Exiting method.");
            return mailWarnSubject;
        }
        if (!status.getSuccessMilestones().isEmpty()) {
            LOGGER.info(String.format("Email subject is: %s", mailSuccessSubject));
            LOGGER.debug("<<< Exiting method.");
            return mailSuccessSubject;
        }
        LOGGER.error(String.format("Email subject is: %s", mailErrorSubject));
        LOGGER.debug("<<< Exiting method.");
        return mailErrorSubject;
    }

}
