package com.github.damianmcdonald.webservmon.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("DsFtpSettings")
public class DsFtpSettings extends FtpSettings {

    @Value("${ftp.ds.directory}")
    private String ftpUploadDirectory;

    @Override
    public String getFtpUploadDirectory() {
        return ftpUploadDirectory;
    }
}
