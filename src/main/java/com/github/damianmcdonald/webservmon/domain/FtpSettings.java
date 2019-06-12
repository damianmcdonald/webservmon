package com.github.damianmcdonald.webservmon.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public abstract class FtpSettings {

    @Value("${ftp.hostname}")
    private String ftpHostname;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.username}")
    private String ftpUsername;

    @Value("${ftp.password}")
    private String ftpPassword;

    @Value("${ftp.root.directory}")
    private String ftpRootDirectory;

    @Value("${ftp.upload.format}")
    private String ftpUploadFormat;

    @Value("${ftp.download.directory}")
    private String ftpDownloadDirectory;

    @Value("${ftp.comparedate.minus.days}")
    private long ftpCompareDateMinusDays;

    @Value("${ftp.comparedate.with.hours}")
    private int ftpCompareDateWithHours;

    @Value("${ftp.comparedate.with.minutes}")
    private int ftpCompareDateWithMinutes;

    @Value("${ftp.comparedate.with.seconds}")
    private int ftpCompareDateWithSeconds;

    private LocalDateTime oldestDate;

    public LocalDateTime getOldestDate() {
        if(this.oldestDate == null) {
            this.oldestDate = LocalDateTime
                    .ofInstant(Instant.now(), ZoneId.systemDefault())
                    .minusDays(getFtpCompareDateMinusDays())
                    .withHour(getFtpCompareDateWithHours())
                    .withMinute(getFtpCompareDateWithMinutes())
                    .withSecond(getFtpCompareDateWithSeconds());
        }
        return oldestDate;
    }

    public abstract String getFtpUploadDirectory();

    public String getFtpHostname() {
        return ftpHostname;
    }

    public int getFtpPort() {
        return ftpPort;
    }

    public String getFtpUsername() {
        return ftpUsername;
    }

    public String getFtpPassword() {
        return ftpPassword;
    }

    public String getFtpRootDirectory() {
        return ftpRootDirectory;
    }

    public String getFtpUploadFormat() {
        return ftpUploadFormat;
    }

    public String getFtpDownloadDirectory() {
        return ftpDownloadDirectory;
    }

    public long getFtpCompareDateMinusDays() {
        return ftpCompareDateMinusDays;
    }

    public int getFtpCompareDateWithHours() {
        return ftpCompareDateWithHours;
    }

    public int getFtpCompareDateWithMinutes() {
        return ftpCompareDateWithMinutes;
    }

    public int getFtpCompareDateWithSeconds() {
        return ftpCompareDateWithSeconds;
    }
}
