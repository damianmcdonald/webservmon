package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.rules.FtpServerRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class FtpMonitorServiceTest implements AbstractTestCase {

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

    @Value("${ftp.tm.directory}")
    private String ftpTmDirectory;

    @Value("${ftp.ds.directory}")
    private String ftpDsDirectory;

    @Value("${ftp.upload.format}")
    private String ftpUploadFormat;

    @Value("${ftp.download.directory}")
    private String ftpDownloadDirectory;

    @Value("${ftp.comparedate.minus.days}")
    private int ftpCompareDateMinusDays;

    @Value("${ftp.comparedate.with.hours}")
    private int ftpCompareDateWithHours;

    @Value("${ftp.comparedate.with.minutes}")
    private int ftpCompareDateWithMinutes;

    @Value("${ftp.comparedate.with.seconds}")
    private int ftpCompareDateWithSeconds;

    @Autowired
    private FtpMonitorService monitorService;

    @Rule
    public FtpServerRule ftpServerRule = new FtpServerRule(FTP_PORT);

    @Test
      public void doFtpLogonTest() {
        final FtpMonitorService.FtpSettings ftpSettings =
                new FtpMonitorService.FtpSettings(
                        ftpHostname,
                        ftpPort,
                        ftpUsername,
                        ftpPassword,
                        ftpRootDirectory.concat(ftpTmDirectory),
                        ftpCompareDateMinusDays,
                        ftpCompareDateWithHours,
                        ftpCompareDateWithMinutes,
                        ftpCompareDateWithSeconds
                );
        monitorService.ftpLogon.apply(ftpSettings);
    }

    @Test
    public void doFtpFileCheck() {
        final FtpMonitorService.FtpSettings ftpSettings =
                new FtpMonitorService.FtpSettings(
                        ftpHostname,
                        ftpPort,
                        ftpUsername,
                        ftpPassword,
                        ftpRootDirectory.concat(ftpTmDirectory),
                        ftpCompareDateMinusDays,
                        ftpCompareDateWithHours,
                        ftpCompareDateWithMinutes,
                        ftpCompareDateWithSeconds
                );
        final String fileName = monitorService.ftpLogon.andThen(monitorService.ftpFileCheck).apply(ftpSettings);
        Assert.assertNotNull(fileName);
        Assert.assertTrue(fileName.equalsIgnoreCase(FTP_VALID_ZIP));
    }

}
