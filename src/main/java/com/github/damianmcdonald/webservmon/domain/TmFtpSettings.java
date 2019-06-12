package com.github.damianmcdonald.webservmon.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("TmFtpSettings")
public class TmFtpSettings extends FtpSettings {

    @Value("${ftp.tm.directory}")
    private String ftpUploadDirectory;

    @Override
    public String getFtpUploadDirectory() {
        return ftpUploadDirectory;
    }
}
