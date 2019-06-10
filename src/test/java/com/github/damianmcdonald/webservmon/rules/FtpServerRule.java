package com.github.damianmcdonald.webservmon.rules;


import com.github.damianmcdonald.webservmon.AbstractTestCase;
import org.junit.rules.ExternalResource;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;

import java.util.ArrayList;
import java.util.List;

public class FtpServerRule extends ExternalResource implements AbstractTestCase {

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
        fileSystem.add(new FileEntry(FTP_VALID_ZIP));

    /*
    http://mockftpserver.sourceforge.net/fakeftpserver-filesystems.html

        DirectoryEntry dirEntry = (DirectoryEntry)fileSystem.getEntry("c:/data");

        FileEntry fileEntry = (FileEntry)fileSystem.getEntry("c:/data/file1.txt");

        FileEntry newFileEntry = (FileEntry)fileSystem.getEntry("c:/data/new.txt");
        InputStream inputStream = newFileEntry.createInputStream();
        // read the file contents using inputStream
        */


        fakeFtpServer.setFileSystem(fileSystem);

        // Create UserAccount with username, password, home-directory
        final UserAccount userAccount = new UserAccount(FTP_USERNAME, FTP_VALID_PASSWORD, "/");
        final List<UserAccount> userAccounts = new ArrayList();
        userAccounts.add(userAccount);
        fakeFtpServer.setUserAccounts(userAccounts);

        fakeFtpServer.setServerControlPort(port);
        fakeFtpServer.start();
    }

    @Override
    protected void after() {
        super.after();
        fakeFtpServer.stop();
    }
}
