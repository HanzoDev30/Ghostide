package ir.hanzodev1375.components.ftp.impl;

import ir.hanzodev1375.components.ftp.interfaces.RemoteClient;
import ir.hanzodev1375.components.ftp.model.FtpEntry;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FtpClientImpl implements RemoteClient {

  private final FTPClient client = new FTPClient();

  @Override
  public void connect(String host, int port, String user, String password) throws IOException {
    client.connect(host, port);
    client.setConnectTimeout(10000);
    client.setSoTimeout(15000);
    boolean ok = client.login(user, password);
    if (!ok) throw new IOException("FTP login failed");
    client.enterLocalPassiveMode();
    client.setFileType(FTP.BINARY_FILE_TYPE);
  }

  @Override
  public List<FtpEntry> listFiles(String path) throws IOException {
    FTPFile[] files = client.listFiles(path);
    List<FtpEntry> result = new ArrayList<>();
    if (files == null) return result;
    for (FTPFile f : files) {
      String name = f.getName();
      if (name.equals(".") || name.equals("..")) continue;
      String fullPath = path.endsWith("/") ? path + name : path + "/" + name;
      result.add(
          new FtpEntry(
              name,
              fullPath,
              f.isDirectory(),
              f.getSize(),
              f.getTimestamp() != null ? f.getTimestamp().getTimeInMillis() : 0));
    }
    return result;
  }

  @Override
  public void download(String remotePath, String localPath) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(localPath)) {
      boolean ok = client.retrieveFile(remotePath, fos);
      if (!ok) throw new IOException("FTP download failed: " + remotePath);
    }
  }

  @Override
  public void upload(String localPath, String remotePath) throws IOException {
    try (FileInputStream fis = new FileInputStream(localPath)) {
      boolean ok = client.storeFile(remotePath, fis);
      if (!ok) throw new IOException("FTP upload failed: " + remotePath);
    }
  }

  @Override
  public void delete(String remotePath) throws IOException {
    boolean ok = client.deleteFile(remotePath);
    if (!ok) {
      ok = client.removeDirectory(remotePath);
    }
    if (!ok) throw new IOException("FTP delete failed: " + remotePath);
  }

  @Override
  public void rename(String oldPath, String newPath) throws IOException {
    boolean ok = client.rename(oldPath, newPath);
    if (!ok) throw new IOException("FTP rename failed");
  }

  @Override
  public void disconnect() {
    try {
      if (client.isConnected()) {
        client.logout();
        client.disconnect();
      }
    } catch (IOException ignored) {
    }
  }

  @Override
  public boolean isConnected() {
    return client.isConnected();
  }
}
