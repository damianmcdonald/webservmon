package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.AbstractTestCase;
import com.github.damianmcdonald.webservmon.domain.FtpSettings;
import com.github.damianmcdonald.webservmon.domain.Result;
import com.github.damianmcdonald.webservmon.rules.FtpServerRule;
import com.icegreen.greenmail.store.FolderException;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.ThreadContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@EnableConfigurationProperties
public class FtpMonitorServiceTest implements AbstractTestCase {

    private static final String KEY_CORRELATION_ID = "CORRELATION_ID";
    private static final String KEY_ERRORS_DOWNLOAD = "ERRORS_DOWNLOAD";
    private static final String KEY_ERRORS_UNZIP = "ERRORS_UNZIP";
    private static final String KEY_ERRORS_XML_VALIDATE = "ERRORS_XML_VALIDATE";
    private static final String FILE_EXTENSION_XML = "xml";
    private static final String FTP_VALID_DIR = "ftp/valid";
    private static final String FTP_INVALID_ZIP_DIR = "ftp/invalid/zip";
    private static final String FTP_INVALID_XML_DIR = "ftp/invalid/xml/invalid-xml";

    @Autowired
    private FtpMonitorService monitorService;

    @Autowired
    @Qualifier("TmFtpSettings")
    private FtpSettings tmFtpSettıngs;

    @MockBean
    @Qualifier("DSFtpSettings")
    private FtpSettings mockFtpSettıngs;

    @Rule
    public FtpServerRule ftpServerRule = new FtpServerRule(FTP_PORT);

    @Before
    public void beforeTestRun() throws FolderException {
        ThreadContext.clearStack();
    }

    @Test
    public void doValidFtpLogonTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        monitorService.ftpLogon.apply(tmFtpSettıngs);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

    @Test(expected=RuntimeException.class)
    public void doInValidFtpHostnameTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        Mockito.when(mockFtpSettıngs.getFtpHostname()).thenReturn(FTP_INVALID_HOSTNAME);
        Mockito.when(mockFtpSettıngs.getFtpPort()).thenReturn(FTP_PORT);
        Mockito.when(mockFtpSettıngs.getFtpUsername()).thenReturn(FTP_USERNAME);
        Mockito.when(mockFtpSettıngs.getFtpPassword()).thenReturn(FTP_VALID_PASSWORD);
        monitorService.ftpLogon.apply(mockFtpSettıngs);
    }

    @Test(expected=RuntimeException.class)
    public void doInValidFtpPasswordTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        Mockito.when(mockFtpSettıngs.getFtpHostname()).thenReturn(FTP_VALID_HOSTNAME);
        Mockito.when(mockFtpSettıngs.getFtpPort()).thenReturn(FTP_PORT);
        Mockito.when(mockFtpSettıngs.getFtpUsername()).thenReturn(FTP_USERNAME);
        Mockito.when(mockFtpSettıngs.getFtpPassword()).thenReturn(FTP_INVALID_PASSWORD);
        monitorService.ftpLogon.apply(mockFtpSettıngs);
    }

    @Test
    public void doValidTmFtpFilesCheckTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        final List<String> ftpFiles = monitorService.ftpLogon.andThen(monitorService.ftpFilesCheck).apply(tmFtpSettıngs);
        Assert.assertFalse(ftpFiles.isEmpty());
        Assert.assertTrue(ftpFiles.size() == 1);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

    @Test
    public void doValidDsFtpFilesCheckTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        Mockito.when(mockFtpSettıngs.getFtpHostname()).thenReturn(FTP_VALID_HOSTNAME);
        Mockito.when(mockFtpSettıngs.getFtpPort()).thenReturn(FTP_PORT);
        Mockito.when(mockFtpSettıngs.getFtpUsername()).thenReturn(FTP_USERNAME);
        Mockito.when(mockFtpSettıngs.getFtpPassword()).thenReturn(FTP_VALID_PASSWORD);
        Mockito.when(mockFtpSettıngs.getFtpRootDirectory()).thenReturn(FTP_ROOT_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadDirectory()).thenReturn(FTP_VALID_UPLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadFormat()).thenReturn(FTP_UPLOAD_FORMAT);
        Mockito.when(mockFtpSettıngs.getFtpDownloadDirectory()).thenReturn(FTP_DOWNLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateMinusDays()).thenReturn(FTP_COMPAREDATE_MINUS_DAYS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithHours()).thenReturn(FTP_COMPAREDATE_WITH_HOURS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithMinutes()).thenReturn(FTP_COMPAREDATE_WITH_MINUTES);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithSeconds()).thenReturn(FTP_COMPAREDATE_WITH_SECONDS);
        final LocalDateTime dateTime = LocalDateTime
                .ofInstant(Instant.now(), ZoneId.systemDefault())
                .minusDays(FTP_COMPAREDATE_MINUS_DAYS)
                .withHour(FTP_COMPAREDATE_WITH_HOURS)
                .withMinute(FTP_COMPAREDATE_WITH_MINUTES)
                .withSecond(FTP_COMPAREDATE_WITH_SECONDS);
        Mockito.when(mockFtpSettıngs.getDateComparisonWindow()).thenReturn(dateTime);
        final List<String> ftpFiles = monitorService.ftpLogon.andThen(monitorService.ftpFilesCheck).apply(mockFtpSettıngs);
        Assert.assertFalse(ftpFiles.isEmpty());
        Assert.assertTrue(ftpFiles.size() == 1);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

    @Test(expected=RuntimeException.class)
    public void doInValidDsFtpFilesCheckTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        Mockito.when(mockFtpSettıngs.getFtpHostname()).thenReturn(FTP_VALID_HOSTNAME);
        Mockito.when(mockFtpSettıngs.getFtpPort()).thenReturn(FTP_PORT);
        Mockito.when(mockFtpSettıngs.getFtpUsername()).thenReturn(FTP_USERNAME);
        Mockito.when(mockFtpSettıngs.getFtpPassword()).thenReturn(FTP_VALID_PASSWORD);
        Mockito.when(mockFtpSettıngs.getFtpRootDirectory()).thenReturn(FTP_ROOT_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadDirectory()).thenReturn(FTP_INVALID_UPLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadFormat()).thenReturn(FTP_UPLOAD_FORMAT);
        Mockito.when(mockFtpSettıngs.getFtpDownloadDirectory()).thenReturn(FTP_DOWNLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateMinusDays()).thenReturn(FTP_COMPAREDATE_MINUS_DAYS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithHours()).thenReturn(FTP_COMPAREDATE_WITH_HOURS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithMinutes()).thenReturn(FTP_COMPAREDATE_WITH_MINUTES);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithSeconds()).thenReturn(FTP_COMPAREDATE_WITH_SECONDS);
        final LocalDateTime dateTime = LocalDateTime
                .ofInstant(Instant.now(), ZoneId.systemDefault())
                .minusDays(FTP_COMPAREDATE_MINUS_DAYS)
                .withHour(FTP_COMPAREDATE_WITH_HOURS)
                .withMinute(FTP_COMPAREDATE_WITH_MINUTES)
                .withSecond(FTP_COMPAREDATE_WITH_SECONDS);
        Mockito.when(mockFtpSettıngs.getDateComparisonWindow()).thenReturn(dateTime);
        final List<String> ftpFiles = monitorService.ftpLogon.andThen(monitorService.ftpFilesCheck).apply(mockFtpSettıngs);
        Assert.assertFalse(ftpFiles.isEmpty());
        Assert.assertTrue(ftpFiles.size() == 1);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

    @Test(expected=RuntimeException.class)
    public void doInValidDateDsFtpFilesCheckTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        Mockito.when(mockFtpSettıngs.getFtpHostname()).thenReturn(FTP_VALID_HOSTNAME);
        Mockito.when(mockFtpSettıngs.getFtpPort()).thenReturn(FTP_PORT);
        Mockito.when(mockFtpSettıngs.getFtpUsername()).thenReturn(FTP_USERNAME);
        Mockito.when(mockFtpSettıngs.getFtpPassword()).thenReturn(FTP_VALID_PASSWORD);
        Mockito.when(mockFtpSettıngs.getFtpRootDirectory()).thenReturn(FTP_ROOT_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadDirectory()).thenReturn(FTP_INVALID_UPLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadFormat()).thenReturn(FTP_UPLOAD_FORMAT);
        Mockito.when(mockFtpSettıngs.getFtpDownloadDirectory()).thenReturn(FTP_DOWNLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateMinusDays()).thenReturn(FTP_COMPAREDATE_MINUS_DAYS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithHours()).thenReturn(FTP_COMPAREDATE_WITH_HOURS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithMinutes()).thenReturn(FTP_COMPAREDATE_WITH_MINUTES);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithSeconds()).thenReturn(FTP_COMPAREDATE_WITH_SECONDS);
        final LocalDateTime dateTime = LocalDateTime
                .ofInstant(Instant.now(), ZoneId.systemDefault())
                .plusDays(5)
                .withHour(FTP_COMPAREDATE_WITH_HOURS)
                .withMinute(FTP_COMPAREDATE_WITH_MINUTES)
                .withSecond(FTP_COMPAREDATE_WITH_SECONDS);
        Mockito.when(mockFtpSettıngs.getDateComparisonWindow()).thenReturn(dateTime);
        final List<String> ftpFiles = monitorService.ftpLogon.andThen(monitorService.ftpFilesCheck).apply(mockFtpSettıngs);
        Assert.assertFalse(ftpFiles.isEmpty());
        Assert.assertTrue(ftpFiles.size() == 1);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

    @Test
    public void doTmFtpFilesDownloadTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        final String downloadDir = monitorService.ftpLogon
                .andThen(monitorService.ftpFilesCheck)
                .andThen(monitorService.ftpFilesDownload)
                .apply(tmFtpSettıngs);
        Assert.assertNotNull(downloadDir);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_DOWNLOAD));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_UNZIP));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_XML_VALIDATE));
    }

    @Test
    public void doValidDsFtpFilesDownloadTest() {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        Mockito.when(mockFtpSettıngs.getFtpHostname()).thenReturn(FTP_VALID_HOSTNAME);
        Mockito.when(mockFtpSettıngs.getFtpPort()).thenReturn(FTP_PORT);
        Mockito.when(mockFtpSettıngs.getFtpUsername()).thenReturn(FTP_USERNAME);
        Mockito.when(mockFtpSettıngs.getFtpPassword()).thenReturn(FTP_VALID_PASSWORD);
        Mockito.when(mockFtpSettıngs.getFtpRootDirectory()).thenReturn(FTP_ROOT_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadDirectory()).thenReturn(FTP_VALID_UPLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpUploadFormat()).thenReturn(FTP_UPLOAD_FORMAT);
        Mockito.when(mockFtpSettıngs.getFtpDownloadDirectory()).thenReturn(FTP_DOWNLOAD_DIR);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateMinusDays()).thenReturn(FTP_COMPAREDATE_MINUS_DAYS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithHours()).thenReturn(FTP_COMPAREDATE_WITH_HOURS);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithMinutes()).thenReturn(FTP_COMPAREDATE_WITH_MINUTES);
        Mockito.when(mockFtpSettıngs.getFtpCompareDateWithSeconds()).thenReturn(FTP_COMPAREDATE_WITH_SECONDS);
        final LocalDateTime dateTime = LocalDateTime
                .ofInstant(Instant.now(), ZoneId.systemDefault())
                .minusDays(FTP_COMPAREDATE_MINUS_DAYS)
                .withHour(FTP_COMPAREDATE_WITH_HOURS)
                .withMinute(FTP_COMPAREDATE_WITH_MINUTES)
                .withSecond(FTP_COMPAREDATE_WITH_SECONDS);
        Mockito.when(mockFtpSettıngs.getDateComparisonWindow()).thenReturn(dateTime);
        final String downloadDir = monitorService.ftpLogon
                .andThen(monitorService.ftpFilesCheck)
                .andThen(monitorService.ftpFilesDownload)
                .apply(mockFtpSettıngs);
        Assert.assertNotNull(downloadDir);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_DOWNLOAD));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_UNZIP));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_XML_VALIDATE));
    }

    @Test
    public void doValidUnzipFilesTest() throws URISyntaxException {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        final String zipFileDirectory = new File(this.getClass().getClassLoader().getResource(FTP_VALID_DIR).toURI()).getAbsolutePath();
        final List<String> unzipDirs = monitorService.unzipFiles.apply(zipFileDirectory);
        Assert.assertFalse(unzipDirs.isEmpty());
        Assert.assertTrue(unzipDirs.size() == 1);
        unzipDirs.stream()
                .forEach(dir -> {
                    final List<File> files = (List<File>) FileUtils.listFiles(
                            new File(dir),
                            new String[]{FILE_EXTENSION_XML},
                            false);
                    Assert.assertTrue(files.size() == 3);
                });
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_DOWNLOAD));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_UNZIP));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_XML_VALIDATE));
    }

    @Test
    public void doInValidUnzipFilesTest() throws URISyntaxException {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        final String zipFileDirectory = new File(this.getClass().getClassLoader().getResource(FTP_INVALID_ZIP_DIR).toURI()).getAbsolutePath();
        monitorService.unzipFiles.apply(zipFileDirectory);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_DOWNLOAD));
        Assert.assertTrue(ThreadContext.containsKey(KEY_ERRORS_UNZIP));
        Assert.assertFalse(ThreadContext.containsKey(KEY_ERRORS_XML_VALIDATE));
    }

    @Test
    public void doSuccessValidateZipContentsTest() throws URISyntaxException {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        final String zipFileDirectory = new File(this.getClass().getClassLoader().getResource(FTP_VALID_DIR).toURI()).getAbsolutePath();
        Result result = monitorService.unzipFiles
                .andThen(monitorService.validateZipContents)
                .apply(zipFileDirectory);
        Assert.assertTrue(result == Result.NO_ERRORS);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

    @Test
    public void doFailValidateZipContentsTest() throws URISyntaxException {
        final String correlationId = UUID.randomUUID().toString();
        ThreadContext.put(KEY_CORRELATION_ID, correlationId);
        final String zipFileDirectory = new File(this.getClass().getClassLoader().getResource(FTP_INVALID_XML_DIR).toURI()).getAbsolutePath();
        List<String> badDirs = new ArrayList();
        badDirs.add(zipFileDirectory);
        Result result = monitorService.validateZipContents.apply(badDirs);
        Assert.assertTrue(result == Result.NO_ERRORS);
        Assert.assertEquals(correlationId, ThreadContext.get(KEY_CORRELATION_ID));
    }

}
