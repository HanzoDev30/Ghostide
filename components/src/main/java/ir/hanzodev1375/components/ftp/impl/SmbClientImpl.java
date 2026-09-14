package ir.hanzodev1375.components.ftp.impl;

import ir.hanzodev1375.components.ftp.interfaces.RemoteClient;
import ir.hanzodev1375.components.ftp.model.FtpEntry;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jcifs.CIFSContext;
import jcifs.config.PropertyConfiguration;
import jcifs.context.BaseContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

public class SmbClientImpl implements RemoteClient {

  private CIFSContext context;
  private String baseUrl;

  @Override
  public void connect(String host, int port, String user, String password) throws IOException {
    String[] parts = host.split("/", 2);
    if (parts.length < 2) {
      throw new IOException(
          "SMB connection requires host and share name, e.g., '192.168.1.100/Share'");
    }
    String hostname = parts[0];
    String share = parts[1];
    try {
      Properties props = new Properties();
      PropertyConfiguration config = new PropertyConfiguration(props);
      NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(null, user, password);
      context = new BaseContext(config);
      context = context.withCredentials(auth);
      baseUrl = "smb://" + hostname + "/" + share + "/";
    } catch (Exception e) {
      throw new IOException("SMB connection failed: " + e.getMessage(), e);
    }
  }

  @Override
  public List<FtpEntry> listFiles(String path) throws IOException {
    try {
      if (path.startsWith("/")) path = path.substring(1);
      String url = baseUrl + (path.isEmpty() ? "" : path + "/");
      SmbFile dir = new SmbFile(url, context);
      SmbFile[] files = dir.listFiles();
      List<FtpEntry> result = new ArrayList<>();
      for (SmbFile f : files) {
        String name = f.getName();
        if (name.equals(".") || name.equals("..")) continue;
        String fullPath = path.isEmpty() ? name : path + "/" + name;
        result.add(new FtpEntry(name, fullPath, f.isDirectory(), f.length(), f.lastModified()));
      }
      return result;
    } catch (Exception e) {
      throw new IOException("SMB list failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void download(String remotePath, String localPath) throws IOException {
    try {
      SmbFile remote = new SmbFile(baseUrl + remotePath, context);
      try (SmbFileInputStream in = new SmbFileInputStream(remote);
          FileOutputStream out = new FileOutputStream(localPath)) {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
          out.write(buffer, 0, len);
        }
      }
    } catch (Exception e) {
      throw new IOException("SMB download failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void upload(String localPath, String remotePath) throws IOException {
    try {
      SmbFile remote = new SmbFile(baseUrl + remotePath, context);
      try (FileInputStream in = new FileInputStream(localPath);
          SmbFileOutputStream out = new SmbFileOutputStream(remote)) {
        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) != -1) {
          out.write(buffer, 0, len);
        }
      }
    } catch (Exception e) {
      throw new IOException("SMB upload failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void delete(String remotePath) throws IOException {
    try {
      SmbFile file = new SmbFile(baseUrl + remotePath, context);
      file.delete();
    } catch (Exception e) {
      throw new IOException("SMB delete failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void rename(String oldPath, String newPath) throws IOException {
    try {
      SmbFile oldFile = new SmbFile(baseUrl + oldPath, context);
      SmbFile newFile = new SmbFile(baseUrl + newPath, context);
      oldFile.renameTo(newFile);
    } catch (Exception e) {
      throw new IOException("SMB rename failed: " + e.getMessage(), e);
    }
  }

  @Override
  public void disconnect() {
    context = null;
    baseUrl = null;
  }

  @Override
  public boolean isConnected() {
    return context != null;
  }
}
