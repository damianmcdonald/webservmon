package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.mailers.HttpMailer;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class FtpMonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpMonitorService.class);

    private final FTPClient ftpClient = new FTPClient();

    @Autowired
    private HttpMailer mailer;

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

    public void checkServiceStatus() {

    }

    public Function<FtpSettings, FtpSettings> ftpLogon = ftp -> {

        try {
            // do logon
            ftpClient.connect(ftp.getHostname(), ftp.getPort());
            ftpClient.login(ftp.getUsername(), ftp.getPassword());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return ftp;
    };

    public Function<FtpSettings, String> ftpFileCheck = ftp -> {
        Optional<String> uploadFile = null;
        try {
            //list file
            uploadFile = Arrays.stream(ftpClient.listFiles(ftp.getEntityPath()))
                    .filter(f -> f.isFile())
                    .filter(f -> LocalDateTime.ofInstant(
                                    f.getTimestamp().toInstant(),
                                    ZoneId.systemDefault())
                                .isAfter(ftp.getOldestDate())
                    )
                    .filter(f -> f.getName().endsWith(".zip"))
                    .map(f -> f.getName())
                    .findFirst();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        if (!uploadFile.isPresent()) {
            throw new RuntimeException(
                    new FileNotFoundException(
                            String.format("Not valid zip file at path ftp://%s:%d%s with date newer than %s",
                                    ftp.getHostname(),
                                    ftp.getPort(),
                                    ftp.getEntityPath(),
                                    ftp.getOldestDate()
                            )
                    )
            );
        }
        return ftp.getEntityPath().concat("/").concat(uploadFile.get());
    };

    private Function<String, String> ftpFileDownload = ftpPath -> {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final String tmpDir = Paths.get(System.getProperty("java.io.tmpdir")).getFileName().toString();
        final String randomFile = tmpDir+File.pathSeparator+UUID.randomUUID().toString().concat(".zip");
        try {
            boolean success = ftpClient.retrieveFile(ftpPath, byteArrayOutputStream);
            ftpClient.disconnect();
            try(OutputStream outputStream = new FileOutputStream(randomFile)) {
                byteArrayOutputStream.writeTo(outputStream);
            }
            if (!success) {
                throw new IOException("Retrieve file failed: " + ftpPath);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return randomFile;
    };

    public static class FtpSettings {

        private final String hostname;
        private final int port;
        private final String username;
        private final String password;
        private final String entityPath;
        private final LocalDateTime oldestDate;

        public FtpSettings(final String hostname,
                             final int port,
                             final String username,
                             final String password,
                             final String entityPath,
                             final int ftpCompareDateMinusDays,
                             final int ftpCompareDateWithHours,
                             final int ftpCompareDateWithMinutes,
                             final int ftpCompareDateWithSeconds
                           ) {
            this.hostname = hostname;
            this.port = port;
            this.username = username;
            this.password = password;
            //this.entityPath = (entityType.equalsIgnoreCase("TM")) ? ftpRootDirectory.concat(ftpTmDirectory) : ftpRootDirectory.concat(ftpDsDirectory);
            this.entityPath = entityPath;
            this.oldestDate =  LocalDateTime
                    .ofInstant(Instant.now(), ZoneId.systemDefault())
                    .minusDays(ftpCompareDateMinusDays)
                    .withHour(ftpCompareDateWithHours)
                    .withMinute(ftpCompareDateWithMinutes)
                    .withSecond(ftpCompareDateWithSeconds);
        }

        public String getHostname() {
            return hostname;
        }

        public int getPort() {
            return port;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getEntityPath() {
            return entityPath;
        }

        public LocalDateTime getOldestDate() {
            return oldestDate;
        }
    }

}
