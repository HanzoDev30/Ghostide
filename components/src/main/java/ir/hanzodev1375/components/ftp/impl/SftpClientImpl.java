package ir.hanzodev1375.components.ftp.impl;

import android.util.Log;
import ir.hanzodev1375.components.ftp.interfaces.RemoteClient;
import ir.hanzodev1375.components.ftp.model.FtpEntry;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SftpClientImpl implements RemoteClient {

  private SSHClient ssh;
  private SFTPClient sftp;

  @Override
  public void connect(String host, int port, String user, String password) throws IOException {
    ssh = new SSHClient();
    ssh.addHostKeyVerifier(new PromiscuousVerifier());
    ssh.setConnectTimeout(10000);
    ssh.setTimeout(15000);
    ssh.connect(host, port);
    ssh.authPassword(user, password);
    sftp = ssh.newSFTPClient();
  }

  @Override
  public List<FtpEntry> listFiles(String path) throws IOException {
    List<RemoteResourceInfo> infos = sftp.ls(path);
    List<FtpEntry> result = new ArrayList<>();
    for (RemoteResourceInfo info : infos) {
      String name = info.getName();
      if (name.equals(".") || name.equals("..")) continue;
      String fullPath = path.endsWith("/") ? path + name : path + "/" + name;
      result.add(
          new FtpEntry(
              name,
              fullPath,
              info.isDirectory(),
              info.getAttributes().getSize(),
              info.getAttributes().getMtime() * 1000L));
    }
    return result;
  }

  @Override
  public void download(String remotePath, String localPath) throws IOException {
    sftp.get(remotePath, localPath);
  }

  @Override
  public void upload(String localPath, String remotePath) throws IOException {
    sftp.put(localPath, remotePath);
  }

  @Override
  public void delete(String remotePath) throws IOException {
    try {
      sftp.rm(remotePath);
    } catch (IOException e) {
      sftp.rmdir(remotePath);
    }
  }

  @Override
  public void rename(String oldPath, String newPath) throws IOException {
    sftp.rename(oldPath, newPath);
  }

  @Override
  public void disconnect() {
    try {
      if (sftp != null) sftp.close();
      if (ssh != null && ssh.isConnected()) ssh.disconnect();
    } catch (IOException ignored) {
       Log.e(getClass().getSimpleName(),ignored.getMessage());
    }
  }

  @Override
  public boolean isConnected() {
    return ssh != null && ssh.isConnected();
  }
}
