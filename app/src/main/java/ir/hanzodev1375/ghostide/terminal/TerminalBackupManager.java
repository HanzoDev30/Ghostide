package ir.hanzodev1375.ghostide.terminal;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public final class TerminalBackupManager {

  private TerminalBackupManager() {}

  public static final String EXTENSION = ".tgbl";

  private static final String MANIFEST_ENTRY = "ghost-terminal-backup.properties";
  private static final String BACKUP_SUBDIR = "Download/GhostIDE/TerminalBackups";
  private static final int BUFFER_SIZE = 1024 * 1024;
  private static final long PROGRESS_STEP_BYTES = 8L * 1024 * 1024;
  private static final long REPORT_INTERVAL_MS = 150L;
  private static final int MODE_FILE = 0644;
  private static final int MODE_EXEC = 0755;
  private static final int EXEC_BIT = 0100;
  private static final int MANIFEST_MAX_BYTES = 64 * 1024;

  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
  private static final Handler MAIN = new Handler(Looper.getMainLooper());
  private static final AtomicBoolean CANCELLED = new AtomicBoolean(false);
  private static final AtomicBoolean RUNNING = new AtomicBoolean(false);

  private static final class CancelledException extends IOException {
    CancelledException() {
      super("cancelled");
    }
  }

  private interface Task {
    Result run() throws IOException;
  }

  private static final class Result {
    final File archive;
    final long sizeBytes;

    Result(File archive, long sizeBytes) {
      this.archive = archive;
      this.sizeBytes = sizeBytes;
    }
  }

  private static final class Source {
    final File dir;
    final String prefix;

    Source(File dir, String prefix) {
      this.dir = dir;
      this.prefix = prefix;
    }
  }

  private static final class Progress {
    private final ProgressListener listener;
    private long total;
    private boolean estimate;
    private long processed;
    private long lastReport;
    private long lastReportTime;

    Progress(ProgressListener listener, long total, boolean estimate) {
      this.listener = listener;
      this.total = total;
      this.estimate = estimate;
    }

    void setTotal(long total, boolean estimate) {
      this.total = total;
      this.estimate = estimate;
    }

    long total() {
      return total;
    }

    private void push(String path) {
      lastReport = processed;
      lastReportTime = System.currentTimeMillis();
      int percent = percent(processed, total);
      if (percent >= 0 && estimate) {
        percent = Math.min(percent, 99);
      }
      post(listener, processed, total, percent, path);
    }

    void start() {
      push(null);
    }

    void add(long bytes) {
      processed += bytes;
      if (processed - lastReport >= PROGRESS_STEP_BYTES) {
        push(null);
      }
    }

    void file(String name) {
      if (System.currentTimeMillis() - lastReportTime >= REPORT_INTERVAL_MS) {
        push(name);
      }
    }

    void finish() {
      push(null);
    }
  }

  public interface ProgressListener {
    void onProgress(long processedBytes, long totalBytes, int percent, String currentPath);

    void onSuccess(File archive, long sizeBytes);

    void onCancelled();

    void onError(String message, Throwable error);
  }

  public static File backupDir(Context context) {
    File external = Environment.getExternalStorageDirectory();
    if (external != null && external.isDirectory()) {
      File dir = new File(external, BACKUP_SUBDIR);
      if (dir.isDirectory() || dir.mkdirs()) {
        return dir;
      }
    }
    File fallback = new File(context.getExternalFilesDir(null), "TerminalBackups");
    if (fallback.isDirectory() || fallback.mkdirs()) {
      return fallback;
    }
    return new File(context.getFilesDir(), "terminal_backups");
  }

  public static List<File> listBackups(Context context) {
    File[] files =
        backupDir(context)
            .listFiles((dir, name) -> name.toLowerCase(Locale.US).endsWith(EXTENSION));
    List<File> list = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(files));
    list.sort(Comparator.comparingLong(File::lastModified).reversed());
    return list;
  }

  public static void cancel() {
    CANCELLED.set(true);
  }

  public static boolean isRunning() {
    return RUNNING.get();
  }

  public static void backup(Context context, ProgressListener listener) {
    final Context app = context.getApplicationContext();
    final File[] tempHolder = new File[1];
    run(
        listener,
        () -> {
          File dir = backupDir(app);
          if (!dir.isDirectory() && !dir.mkdirs()) {
            throw new IOException("cannot create backup dir: " + dir);
          }
          File archive = new File(dir, buildFileName());
          File temp = new File(dir, archive.getName() + ".part");
          tempHolder[0] = temp;
          deleteQuietly(temp);

          List<Source> sources = collectSources(app);
          if (sources.isEmpty()) {
            throw new IOException("no terminal data to back up");
          }
          post(listener, 0L, -1L, -1, null);
          long total = totalSize(sources);

          Progress progress = new Progress(listener, total, false);
          writeArchive(app, sources, temp, progress);
          checkCancelled();
          if (!temp.renameTo(archive)) {
            moveFile(temp, archive);
          }
          tempHolder[0] = null;
          return new Result(archive, archive.length());
        },
        tempHolder);
  }

  public static void restore(Context context, File archive, ProgressListener listener) {
    final Context app = context.getApplicationContext();
    if (archive == null || !archive.isFile()) {
      postError(listener, "backup file not found", null);
      return;
    }
    run(
        listener,
        () -> {
          File filesDir = app.getFilesDir();
          long archiveSize = archive.length();
          Progress progress = new Progress(listener, -1L, true);
          progress.start();

          long restored = 0L;
          try (InputStream fileIn =
                  new BufferedInputStream(new FileInputStream(archive), BUFFER_SIZE);
              GzipCompressorInputStream gzip = new GzipCompressorInputStream(fileIn);
              TarArchiveInputStream tar = new TarArchiveInputStream(gzip, BUFFER_SIZE)) {

            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
              checkCancelled();
              String name = entry.getName();
              if (MANIFEST_ENTRY.equals(name)) {
                long declared = readManifestTotal(tar);
                if (declared > 0) {
                  progress.setTotal(declared, false);
                }
                continue;
              }
              if (progress.total() <= 0) {
                progress.setTotal(Math.max(archiveSize * 3L, 1L), true);
              }

              File target = resolveSafely(filesDir, name);
              if (entry.isDirectory()) {
                mkdirs(target);
                continue;
              }
              if (!entry.isFile()) {
                continue;
              }
              File parent = target.getParentFile();
              if (parent != null) {
                mkdirs(parent);
              }
              restored +=
                  copyEntry(tar, target, entry.getSize(), progress, name);
              if ((entry.getMode() & EXEC_BIT) != 0) {
                target.setExecutable(true, false);
              }
              if (entry.getModTime() != null) {
                target.setLastModified(entry.getModTime().getTime());
              }
            }
          }
          progress.finish();
          return new Result(archive, restored);
        },
        null);
  }

  private static List<Source> collectSources(Context app) {
    List<Source> sources = new ArrayList<>();
    File filesDir = app.getFilesDir();
    File rootfs = DebianBootstrap.getRootfsDir(app);
    if (rootfs.isDirectory()) {
      sources.add(new Source(rootfs, "rootfs"));
    }
    File shell = new File(filesDir, "shell");
    if (shell.isDirectory()) {
      sources.add(new Source(shell, "shell"));
    }
    return sources;
  }

  private static String entryNameFor(File filesDir, File file) {
    String base = filesDir.getAbsolutePath();
    String path = file.getAbsolutePath();
    if (path.equals(base)) {
      return "";
    }
    if (path.startsWith(base + File.separator)) {
      return path.substring(base.length() + 1);
    }
    return file.getName();
  }

  private static String buildFileName() {
    String stamp = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
    return "ghost-terminal-" + stamp + EXTENSION;
  }

  private static long totalSize(List<Source> sources) throws IOException {
    long total = 0L;
    for (Source source : sources) {
      Deque<File> stack = new ArrayDeque<>();
      stack.push(source.dir);
      while (!stack.isEmpty()) {
        checkCancelled();
        File current = stack.pop();
        File[] children = current.listFiles();
        if (children == null) {
          continue;
        }
        for (File child : children) {
          if (child.isDirectory()) {
            stack.push(child);
          } else if (child.isFile()) {
            total += child.length();
          }
        }
      }
    }
    return total;
  }

  private static void writeArchive(
      Context app, List<Source> sources, File temp, Progress progress) throws IOException {
    File filesDir = app.getFilesDir();
    try (OutputStream fileOut =
            new BufferedOutputStream(new FileOutputStream(temp), BUFFER_SIZE);
        GzipCompressorOutputStream gzip = new GzipCompressorOutputStream(fileOut);
        TarArchiveOutputStream tar = new TarArchiveOutputStream(gzip, BUFFER_SIZE)) {

      tar.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      tar.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
      tar.setAddPaxHeadersForNonAsciiNames(true);

      byte[] manifest =
          buildManifest(progress.total(), sources).getBytes(StandardCharsets.UTF_8);
      TarArchiveEntry manifestEntry = new TarArchiveEntry(MANIFEST_ENTRY);
      manifestEntry.setSize(manifest.length);
      manifestEntry.setMode(MODE_FILE);
      manifestEntry.setModTime(System.currentTimeMillis());
      tar.putArchiveEntry(manifestEntry);
      tar.write(manifest);
      tar.closeArchiveEntry();

      for (Source source : sources) {
        addTree(tar, filesDir, source.dir, progress);
      }
      tar.finish();
    }
  }

  private static void addTree(
      TarArchiveOutputStream tar, File filesDir, File root, Progress progress) throws IOException {
    addDirectory(tar, entryNameFor(filesDir, root));

    Deque<File> stack = new ArrayDeque<>();
    stack.push(root);
    while (!stack.isEmpty()) {
      checkCancelled();
      File current = stack.pop();
      File[] children = current.listFiles();
      if (children == null) {
        continue;
      }
      Arrays.sort(children, Comparator.comparing(File::getName));
      for (File child : children) {
        String name = entryNameFor(filesDir, child);
        if (child.isDirectory()) {
          addDirectory(tar, name);
          stack.push(child);
        } else if (child.isFile()) {
          addFile(tar, child, name, progress);
        }
      }
    }
  }

  private static void addDirectory(TarArchiveOutputStream tar, String name) throws IOException {
    if (name == null || name.isEmpty()) {
      return;
    }
    TarArchiveEntry entry = new TarArchiveEntry(name.endsWith("/") ? name : name + "/");
    entry.setMode(MODE_EXEC);
    entry.setModTime(System.currentTimeMillis());
    tar.putArchiveEntry(entry);
    tar.closeArchiveEntry();
  }

  private static void addFile(
      TarArchiveOutputStream tar, File file, String name, Progress progress) throws IOException {
    long size = file.length();
    TarArchiveEntry entry = new TarArchiveEntry(name);
    entry.setSize(size);
    entry.setMode(file.canExecute() ? MODE_EXEC : MODE_FILE);
    entry.setModTime(file.lastModified());
    tar.putArchiveEntry(entry);

    long written = 0L;
    byte[] buffer = new byte[BUFFER_SIZE];
    try (InputStream in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
      while (written < size) {
        checkCancelled();
        int toRead = (int) Math.min(buffer.length, size - written);
        int read = in.read(buffer, 0, toRead);
        if (read < 0) {
          break;
        }
        tar.write(buffer, 0, read);
        written += read;
        progress.add(read);
      }
    }
    tar.closeArchiveEntry();
    progress.file(name);
  }

  private static long copyEntry(
      TarArchiveInputStream tar,
      File target,
      long entrySize,
      Progress progress,
      String name)
      throws IOException {
    long written = 0L;
    byte[] buffer = new byte[BUFFER_SIZE];
    try (OutputStream out =
        new BufferedOutputStream(new FileOutputStream(target, false), BUFFER_SIZE)) {
      while (written < entrySize) {
        checkCancelled();
        int toRead = (int) Math.min(buffer.length, entrySize - written);
        int read = tar.read(buffer, 0, toRead);
        if (read < 0) {
          break;
        }
        out.write(buffer, 0, read);
        written += read;
        progress.add(read);
      }
    }
    progress.file(name);
    return written;
  }

  private static long readManifestTotal(TarArchiveInputStream tar) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] chunk = new byte[4096];
    int read;
    while (buffer.size() < MANIFEST_MAX_BYTES && (read = tar.read(chunk)) > 0) {
      buffer.write(chunk, 0, read);
    }
    Properties props = new Properties();
    try (InputStream in = new ByteArrayInputStream(buffer.toByteArray())) {
      props.load(in);
    } catch (IOException ignored) {
      return -1L;
    }
    try {
      return Long.parseLong(props.getProperty("totalBytes", "-1"));
    } catch (NumberFormatException e) {
      return -1L;
    }
  }

  private static String buildManifest(long total, List<Source> sources) {
    Properties props = new Properties();
    props.setProperty("format", "tgbl");
    props.setProperty("version", "1");
    props.setProperty("created", String.valueOf(System.currentTimeMillis()));
    props.setProperty("totalBytes", String.valueOf(total));
    StringBuilder roots = new StringBuilder();
    for (Source source : sources) {
      if (roots.length() > 0) {
        roots.append(',');
      }
      roots.append(source.prefix);
    }
    props.setProperty("roots", roots.toString());
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      props.store(out, "GhostIDE terminal backup");
    } catch (IOException ignored) {
    }
    return out.toString(StandardCharsets.UTF_8);
  }

  private static File resolveSafely(File base, String entryName) throws IOException {
    if (entryName == null || entryName.isEmpty()) {
      return base;
    }
    File target = new File(base, entryName);
    String basePath = base.getCanonicalPath();
    String targetPath = target.getCanonicalPath();
    if (!targetPath.equals(basePath) && !targetPath.startsWith(basePath + File.separator)) {
      throw new IOException("refusing to extract outside app data: " + entryName);
    }
    return target;
  }

  private static void mkdirs(File dir) {
    if (dir != null && !dir.isDirectory()) {
      dir.mkdirs();
    }
  }

  private static void moveFile(File from, File to) throws IOException {
    try (InputStream in = new BufferedInputStream(new FileInputStream(from), BUFFER_SIZE);
        OutputStream out = new BufferedOutputStream(new FileOutputStream(to), BUFFER_SIZE)) {
      byte[] buffer = new byte[BUFFER_SIZE];
      int read;
      while ((read = in.read(buffer)) > 0) {
        out.write(buffer, 0, read);
      }
    }
    deleteQuietly(from);
  }

  private static void deleteQuietly(File file) {
    if (file != null && file.exists()) {
      file.delete();
    }
  }

  private static int percent(long processed, long total) {
    if (total <= 0) {
      return -1;
    }
    long value = (processed * 100L) / total;
    return (int) Math.max(0L, Math.min(100L, value));
  }

  private static void checkCancelled() throws CancelledException {
    if (CANCELLED.get()) {
      throw new CancelledException();
    }
  }

  private static void post(
      ProgressListener listener, long processed, long total, int percent, String path) {
    if (listener == null) {
      return;
    }
    MAIN.post(() -> listener.onProgress(processed, total, percent, path));
  }

  private static void postError(ProgressListener listener, String message, Throwable error) {
    if (listener == null) {
      return;
    }
    MAIN.post(() -> listener.onError(message, error));
  }

  private static void run(ProgressListener listener, Task task, File[] tempHolder) {
    CANCELLED.set(false);
    RUNNING.set(true);
    EXECUTOR.execute(
        () -> {
          try {
            Result result = task.run();
            if (listener != null) {
              MAIN.post(() -> listener.onSuccess(result.archive, result.sizeBytes));
            }
          } catch (CancelledException cancelled) {
            if (tempHolder != null && tempHolder[0] != null) {
              deleteQuietly(tempHolder[0]);
            }
            if (listener != null) {
              MAIN.post(listener::onCancelled);
            }
          } catch (Throwable t) {
            if (tempHolder != null && tempHolder[0] != null) {
              deleteQuietly(tempHolder[0]);
            }
            String message = t.getMessage();
            if (message == null || message.isEmpty()) {
              message = t.getClass().getSimpleName();
            }
            postError(listener, message, t);
          } finally {
            RUNNING.set(false);
          }
        });
  }
}
