package com.github.damianmcdonald.webservmon.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("DsFtpSettings")
public class DsFtpSettings extends FtpSettings {

    @Value("${ftp.ds.directory}")
    private String ftpUploadDirectory;

    @Override
    public String getFtpUploadDirectory() {
        return ftpUploadDirectory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ftpHostname", getFtpHostname())
                .append("ftpPort", getFtpPort())
                .append("ftpUsername", getFtpUsername())
                .append("ftpPassword", getFtpPassword())
                .append("ftpRootDirectory", getFtpRootDirectory())
                .append("ftpUploadFormat", getFtpUploadFormat())
                .append("ftpDownloadDirectory", getFtpDownloadDirectory())
                .append("ftpCompareDateMinusDays", getFtpCompareDateMinusDays())
                .append("ftpCompareDateWithHours", getFtpCompareDateWithHours())
                .append("ftpCompareDateWithMinutes", getFtpCompareDateWithMinutes())
                .append("ftpCompareDateWithSeconds", getFtpCompareDateWithSeconds())
                .append("dateComparisonWindow", getDateComparisonWindow())
                .append("ftpUploadDirectory", ftpUploadDirectory)
                .toString();
    }
}
