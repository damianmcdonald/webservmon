package com.github.damianmcdonald.webservmon.monitors;

import com.github.damianmcdonald.webservmon.domain.FtpSettings;
import com.github.damianmcdonald.webservmon.domain.Result;
import com.github.damianmcdonald.webservmon.mailers.HttpMailer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FtpMonitorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FtpMonitorService.class);
    private static final String TEMP_DIR_SYS_PROP = "java.io.tmpdir";
    private static final String FTP_DIR_SEPARATOR = "/";
    private static final String KEY_CORRELATION_ID = "CORRELATION_ID";
    private static final String KEY_FTP_PATH = "FTP_PATH";
    private static final String KEY_ERRORS_DOWNLOAD = "ERRORS_DOWNLOAD";
    private static final String KEY_ERRORS_UNZIP = "ERRORS_UNZIP";
    private static final String KEY_ERRORS_XML_VALIDATE = "ERRORS_XML_VALIDATE";
    private static final String KEY_SUCCESS = "SUCCESS";
    private static final String KEY_SPLITTER = ";::::;";

    private final FTPClient ftpClient = new FTPClient();

    @Autowired
    private HttpMailer mailer;

    @Value("${ftp.upload.format}")
    private String ftpUploadFormat;

    public Result checkServiceStatus(FtpSettings ftpSettings) {
        return ftpLogon
                .andThen(ftpFilesCheck)
                .andThen(ftpFilesDownload)
                .andThen(unzipFiles)
                .andThen(validateZipContents)
                .apply(ftpSettings);
    }

    protected Function<FtpSettings, FtpSettings> ftpLogon = ftp -> {
        try {
            // do logon
            ftpClient.connect(ftp.getFtpHostname(), ftp.getFtpPort());
            if(!ftpClient.login(ftp.getFtpUsername(), ftp.getFtpPassword())) {
                final String error = String.format(
                        "FTP login failed using username: %s and password: %s",
                        ftp.getFtpUsername(),
                        ftp.getFtpPassword());
                LOGGER.error(error);
                throw new RuntimeException(error);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return ftp;
    };

    protected Function<FtpSettings, List<String>> ftpFilesCheck = ftp -> {
        ThreadContext.put(KEY_FTP_PATH, ftp.getFtpRootDirectory().concat(ftp.getFtpUploadDirectory()));
        List<String> ftpFiles = null;
        try {
            //list file
            ftpFiles = Arrays.stream(ftpClient.listFiles(ftp.getFtpRootDirectory().concat(ftp.getFtpUploadDirectory())))
                    .filter(f -> f.isFile())
                    .filter(f -> LocalDateTime.ofInstant(
                                    f.getTimestamp().toInstant(),
                                    ZoneId.systemDefault())
                                .isAfter(ftp.getOldestDate())
                    )
                    .filter(f -> f.getName().endsWith(ftpUploadFormat))
                    .map(f -> f.getName())
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        if (ftpFiles == null || ftpFiles.isEmpty()) {
            throw new RuntimeException(
                    new FileNotFoundException(
                            String.format("No valid zip files at path ftp://%s:%d%s with date newer than %s",
                                    ftp.getFtpHostname(),
                                    ftp.getFtpPort(),
                                    ftp.getFtpRootDirectory().concat(ftp.getFtpUploadDirectory()),
                                    ftp.getOldestDate()
                            )
                    )
            );
        }
        return ftpFiles;
    };

    protected Function<List<String>, String> ftpFilesDownload = ftpFiles -> {
        final String correlationId = ThreadContext.get(KEY_CORRELATION_ID);
        final String ftpPath = ThreadContext.get(KEY_FTP_PATH);
        final File downloadDir = new File(Paths.get(System.getProperty(TEMP_DIR_SYS_PROP)).toFile(), correlationId);
        downloadDir.mkdir();
            ftpFiles.stream().forEach(ftpFile -> {
                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    boolean success = ftpClient.retrieveFile(ftpPath.concat(FTP_DIR_SEPARATOR).concat(ftpFile), byteArrayOutputStream);
                    try (final OutputStream outputStream = new FileOutputStream(
                            downloadDir.getAbsolutePath()
                                    .concat(File.separator)
                                    .concat(ftpFile)
                    )) {
                        byteArrayOutputStream.writeTo(outputStream);
                    }
                    if (!success) {
                        final String errorString = String.format("Failed to download file %s", ftpFile);
                        appendValueToMdcKey(KEY_ERRORS_DOWNLOAD, errorString);
                        LOGGER.error(errorString);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    appendValueToMdcKey(KEY_ERRORS_DOWNLOAD, ExceptionUtils.getStackTrace(ex));
                    LOGGER.error(String.format("Failed to download file %s", ftpFile));
                }
            });
        try {
            ftpClient.disconnect();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

        if(ftpFiles.size() != new File(downloadDir.getAbsolutePath()).listFiles().length) {
            final String errorString = String.format("Expected number of downloads: %d does not match actual number of downloads: %d"
                    ,ftpFiles.size()
                    ,new File(downloadDir.getAbsolutePath()).listFiles().length);
            appendValueToMdcKey(KEY_ERRORS_DOWNLOAD, errorString);
        }
        return downloadDir.getAbsolutePath();
    };

    protected Function<String, List<String>> unzipFiles = dir -> {
        return FileUtils.listFiles(
                new File(dir),
                new String[] { ftpUploadFormat },
                false)
                .stream()
                .filter(f -> f.isFile())
                .map(f -> unzip(f.getAbsolutePath(), dir))
                .collect(Collectors.toList());
    };

    protected Function<List<String>, Result> validateZipContents = dirs -> {
       dirs.stream()
               .forEach(dir -> {
                   FileUtils.listFiles(
                           new File(dir),
                           new String[] { ftpUploadFormat },
                           false)
                           .stream()
                           .filter(f -> f.isDirectory())
                           .forEach(file -> {
                               validateXml(file);
                           });

               });
        if(ThreadContext.containsKey(KEY_ERRORS_XML_VALIDATE)) {
            return Result.WITH_ERRORS;
        }
        return Result.NO_ERRORS;
    };

    private String unzip(final String zipFilePath, final String zipFileDestination) {
        final File zipFile = new File(zipFilePath);
        final File destinationDir = new File(
                zipFileDestination
                .concat(File.separator)
                .concat(FilenameUtils.getBaseName(zipFile.getName()))
        );
        destinationDir.mkdir();
        final byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFilePath));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                final File newFile = newFile(destinationDir, zipEntry);
                final FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            appendValueToMdcKey(KEY_ERRORS_UNZIP, ExceptionUtils.getStackTrace(ex));
        } finally {
            try {
                if (zis != null) {
                    zis.closeEntry();
                    zis.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                appendValueToMdcKey(KEY_ERRORS_UNZIP, ExceptionUtils.getStackTrace(ex));
            }
        }
        return destinationDir.getAbsolutePath();
    }

    private File newFile(final File destinationDir, final ZipEntry zipEntry) throws IOException {
        final File destFile = new File(destinationDir, zipEntry.getName());
        final String destDirPath = destinationDir.getCanonicalPath();
        final String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }

    private boolean validateXml(final File xmlFile) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        boolean isValid = true;
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = null;
        try {
            factory.newDocumentBuilder();
            builder.setErrorHandler(new SimpleErrorHandler());
            // the "parse" method also validates XML, will throw an exception if misformatted
            builder.parse(new InputSource(xmlFile.getAbsolutePath()));
        } catch (Exception ex) {
            ex.printStackTrace();
            appendValueToMdcKey(KEY_ERRORS_XML_VALIDATE, ExceptionUtils.getStackTrace(ex));
            isValid = false;
        }
        return isValid;
    }

    private void appendValueToMdcKey(final String key, final String value) {
        if(ThreadContext.containsKey(key)) {
            ThreadContext.put(key, ThreadContext.get(key).concat(KEY_SPLITTER).concat(value));
        } else {
            ThreadContext.put(key, value);
        }
    }

    private class SimpleErrorHandler implements ErrorHandler {
        public void warning(SAXParseException ex) throws SAXException {
            ex.printStackTrace();
            appendValueToMdcKey(KEY_ERRORS_XML_VALIDATE, ExceptionUtils.getStackTrace(ex));
        }

        public void error(SAXParseException ex) throws SAXException {
            ex.printStackTrace();
            appendValueToMdcKey(KEY_ERRORS_XML_VALIDATE, ExceptionUtils.getStackTrace(ex));
        }

        public void fatalError(SAXParseException ex) throws SAXException {
            ex.printStackTrace();
            appendValueToMdcKey(KEY_ERRORS_XML_VALIDATE, ExceptionUtils.getStackTrace(ex));
        }
    }

}

