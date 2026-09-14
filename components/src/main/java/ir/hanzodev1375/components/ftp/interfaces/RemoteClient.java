package ir.hanzodev1375.components.ftp.interfaces;

import java.io.IOException;
import java.util.List;
import ir.hanzodev1375.components.ftp.model.FtpEntry;

public interface RemoteClient {
  void connect(String host, int port, String user, String password) throws IOException;

  List<FtpEntry> listFiles(String path) throws IOException;

  void download(String remotePath, String localPath) throws IOException;

  void upload(String localPath, String remotePath) throws IOException;

  void delete(String remotePath) throws IOException;

  void rename(String oldPath, String newPath) throws IOException;

  void disconnect();

  boolean isConnected();
}
