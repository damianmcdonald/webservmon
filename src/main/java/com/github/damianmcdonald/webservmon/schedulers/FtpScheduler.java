package com.github.damianmcdonald.webservmon.schedulers;

import com.github.damianmcdonald.webservmon.domain.DsFtpSettings;
import com.github.damianmcdonald.webservmon.domain.FtpMonitorStatus;
import com.github.damianmcdonald.webservmon.domain.FtpSettings;
import com.github.damianmcdonald.webservmon.domain.TmFtpSettings;
import com.github.damianmcdonald.webservmon.mailers.FtpMailer;
import com.github.damianmcdonald.webservmon.monitors.FtpMonitorService;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FtpScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpScheduler.class);

    private static final String KEY_CORRELATION_ID = "CORRELATION_ID";
    private static final String KEY_ERRORS_DOWNLOAD = "ERRORS_DOWNLOAD";
    private static final String KEY_ERRORS_UNZIP = "ERRORS_UNZIP";
    private static final String KEY_ERRORS_XML_VALIDATE = "ERRORS_XML_VALIDATE";
    private static final String KEY_SUCCESS = "SUCCESS";

    @Autowired
    private FtpMonitorService monitorService;

    @Autowired
    private TmFtpSettings tmFtpSettings;

    @Autowired
    private DsFtpSettings dsFtpSettings;

    @Autowired
    private FtpMailer mailer;

    @Scheduled(cron = "${ftp.schedule.before.threshold.interval.tm}")
    public void checkTmFtpStatusBeforeThreshold() {
        LOGGER.debug("Executing checkTmFtpStatusBeforeThreshold scheduled task.");
        checkFtpStatus(tmFtpSettings, true, false);
    }

    @Scheduled(cron = "${ftp.schedule.after.threshold.interval.tm}")
    public void checkTmFtpStatusAfterThreshold() {
        LOGGER.debug("Executing checkTmFtpStatusAfterThreshold scheduled task.");
        checkFtpStatus(tmFtpSettings, true, true);
    }

    @Scheduled(cron = "${ftp.schedule.before.threshold.interval.ds}")
    public void checkDsFtpStatusBeforeThreshold() {
        LOGGER.debug("Executing checkDsFtpStatusBeforeThreshold scheduled task.");
        checkFtpStatus(dsFtpSettings, false, false);
    }

    @Scheduled(cron = "${ftp.schedule.after.threshold.interval.ds}")
    public void checkDsFtpStatusAfterThreshold() {
        LOGGER.debug("Executing checkDsFtpStatusAfterThreshold scheduled task.");
        checkFtpStatus(dsFtpSettings, false, true);
    }

    private void checkFtpStatus(final FtpSettings ftpSettings, final boolean isTrademark, final boolean isFinalResult) {
        LOGGER.debug("Executing checkFtpStatusBeforeThreshold scheduled task.");

        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);

        String exceptionString = "";
        try {
            monitorService.checkServiceStatus(ftpSettings);
        } catch (Exception ex) {
            ex.printStackTrace();
            exceptionString= ExceptionUtils.getStackTrace(ex);
        }

        mailer.sendMail(createMonitorStatus(exceptionString), isTrademark, isFinalResult);

        if(ThreadContext.get(KEY_CORRELATION_ID).equalsIgnoreCase(correlationId)) {
            ThreadContext.clearStack();
        }
    }

    private FtpMonitorStatus createMonitorStatus(final String exceptionString) {
       return new FtpMonitorStatus(
               ThreadContext.get(KEY_CORRELATION_ID),
               ThreadContext.get(KEY_SUCCESS),
               ThreadContext.get(KEY_ERRORS_DOWNLOAD),
               ThreadContext.get(KEY_ERRORS_UNZIP),
               ThreadContext.get(KEY_ERRORS_XML_VALIDATE),
               exceptionString
       );
    }

}
