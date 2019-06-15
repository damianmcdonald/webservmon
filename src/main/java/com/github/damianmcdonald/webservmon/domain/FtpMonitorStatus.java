package com.github.damianmcdonald.webservmon.domain;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FtpMonitorStatus {

    private static final String KEY_SPLITTER = ";::::;";

    private final String correlationId;

    private final boolean trademark;

    private final boolean finalResult;

    private final List<String> successMilestones = new ArrayList();

    private final List<String> errorsDownload = new ArrayList();

    private final List<String> errorsUnzip = new ArrayList();

    private final List<String> errorsXmlValidation = new ArrayList();

    private final String exception;

    public FtpMonitorStatus(final String correlationId,
            final boolean isTrademark,
            final boolean isFinalResult,
            final String successMessages,
            final String errorsDownloadMessages,
            final String errorsUnzipMessages,
            final String errorsValidateXmlMessages,
            final String exception) {
        this.correlationId = correlationId;
        this.trademark = isTrademark;
        this.finalResult = isFinalResult;
        this.exception = exception;
        convertSuccessMessages(successMessages);
        convertErrorsDownloadMessages(errorsDownloadMessages);
        convertErrorsUnzipMessages(errorsUnzipMessages);
        convertErrorsValidateXmlMessages(errorsValidateXmlMessages);
    }

    private void convertSuccessMessages(final String successMessages) {
        if (!StringUtils.isEmpty(successMessages)) {
            successMilestones.addAll(Arrays.stream(successMessages.split(KEY_SPLITTER)).collect(Collectors.toList()));
        }
    }

    private void convertErrorsDownloadMessages(final String errorsDownloadMessages) {
        if (!StringUtils.isEmpty(errorsDownloadMessages)) {
            errorsDownload.addAll(Arrays.stream(errorsDownloadMessages.split(KEY_SPLITTER)).collect(Collectors.toList()));
        }
    }

    private void convertErrorsUnzipMessages(final String errorsUnzipMessages) {
        if (!StringUtils.isEmpty(errorsUnzipMessages)) {
            errorsUnzip.addAll(Arrays.stream(errorsUnzipMessages.split(KEY_SPLITTER)).collect(Collectors.toList()));
        }
    }

    private void convertErrorsValidateXmlMessages(final String validateXmlMessages) {
        if (!StringUtils.isEmpty(validateXmlMessages)) {
            errorsXmlValidation.addAll(Arrays.stream(validateXmlMessages.split(KEY_SPLITTER)).collect(Collectors.toList()));
        }
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public List<String> getSuccessMilestones() {
        return successMilestones;
    }

    public List<String> getErrorsDownload() {
        return errorsDownload;
    }

    public List<String> getErrorsUnzip() {
        return errorsUnzip;
    }

    public List<String> getErrorsXmlValidation() {
        return errorsXmlValidation;
    }

    public String getException() {
        return exception;
    }

    public boolean isTrademark() {
        return trademark;
    }

    public boolean isFinalResult() {
        return finalResult;
    }

    public String toString() {
        return new ToStringBuilder(this)
                .append("correlationId", correlationId)
                .append("isTrademark", trademark)
                .append("isFinalResult", finalResult)
                .append("successMessages", successMilestones)
                .append("errorsDownloadMessages", errorsDownload)
                .append("errorsUnzipMessages", errorsUnzip)
                .append("errorsValidateXmlMessages", errorsXmlValidation)
                .append("exception", exception)
                .toString();
    }

}
