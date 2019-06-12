package com.github.damianmcdonald.webservmon.rules;


import com.github.damianmcdonald.webservmon.AbstractTestCase;
import org.junit.rules.ExternalResource;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

public class FtpServerRule extends ExternalResource implements AbstractTestCase {

    private static final String FTP_TEST_FILE = "ftp/valid/testfile.zip";

    private final FakeFtpServer fakeFtpServer = new FakeFtpServer();

    private int port;

    public FtpServerRule(final int port) {
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        final FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry(FTP_TM_DIR));
        fileSystem.add(new DirectoryEntry(FTP_DS_DIR));

        final Path path = Paths.get(new File(this.getClass().getClassLoader().getResource(FTP_TEST_FILE).toURI()).getAbsolutePath());
        final byte[] bytes = Files.readAllBytes(path);

        final FileEntry fileEntryTm = new FileEntry(FTP_TM_VALID_ZIP);
        fileEntryTm.setContents(bytes);
        fileEntryTm.setLastModified(new Date());
        fileSystem.add(fileEntryTm);

        final FileEntry fileEntryDs = new FileEntry(FTP_DS_VALID_ZIP);
        fileEntryDs.setContents(bytes);
        fileEntryDs.setLastModified(new Date());
        fileSystem.add(fileEntryDs);

        fakeFtpServer.setFileSystem(fileSystem);

        // Create UserAccount with username, password, home-directory
        final UserAccount userAccount = new UserAccount(FTP_USERNAME, FTP_VALID_PASSWORD, "/");
        fakeFtpServer.addUserAccount(userAccount);

        fakeFtpServer.setServerControlPort(port);
        fakeFtpServer.start();
    }

    @Override
    protected void after() {
        super.after();
        fakeFtpServer.stop();
    }
}
