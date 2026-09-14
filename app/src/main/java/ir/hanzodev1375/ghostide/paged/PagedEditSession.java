package ir.hanzodev1375.ghostide.paged;

import android.os.Handler;
import android.os.Looper;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.text.ContentIO;
import io.github.rosemoe.sora.widget.CodeEditor;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class PagedEditSession implements Closeable {

  public static final int DEFAULT_PAGE_SIZE = 10_000_000;
  public static final int MIN_PAGE_SIZE = 16;
  public static final Charset INTERNAL_STORAGE_CHARSET = StandardCharsets.UTF_16BE;
  private static final String PAGE_PREFIX = "page-";
  private static final String SWAP_TMP_PREFIX = "tmp-";
  private static final int NUMBER_PAD_LEN = 5;

  private final File tmpDir;
  private final ReentrantLock operationLock = new ReentrantLock();
  private final List<Page> pages = new ArrayList<>();
  private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private int tmpId = 0;

  public interface Callback {
    void onSuccess();

    void onError(IOException e);
  }

  /** Splits {@code source} into pages on disk. Must be called off the main thread. */
  public PagedEditSession(Reader source, File tmpDir) throws IOException {
    this(source, tmpDir, DEFAULT_PAGE_SIZE);
  }

  public PagedEditSession(Reader source, File tmpDir, int pageSize) throws IOException {
    this.tmpDir = tmpDir;
    if (pageSize < MIN_PAGE_SIZE) {
      throw new IllegalArgumentException("Page size must be at least " + MIN_PAGE_SIZE);
    }
    tmpDir.mkdirs();

    CharBuffer buffer = CharBuffer.wrap(new char[8192]);
    int count = source.read(buffer.array(), buffer.arrayOffset(), buffer.limit());
    if (count >= 0) {
      buffer.limit(count);
    }

    int currPageIndex = 0;
    int currWritten = 0;
    Writer currOutput =
        new OutputStreamWriter(
            new FileOutputStream(getPageFileForIndex(currPageIndex)), INTERNAL_STORAGE_CHARSET);
    int directWriteSize = pageSize - MIN_PAGE_SIZE;
    BreakIterator itr = BreakIterator.getCharacterInstance();

    while (true) {
      int charsToWrite = Math.max(0, Math.min(directWriteSize - currWritten, buffer.remaining()));
      boolean needInput = false;
      if (charsToWrite == 0 && (buffer.hasRemaining() || count == -1)) {
        if (buffer.remaining() < 2 * MIN_PAGE_SIZE && count != -1) {
          needInput = true;
        } else {
          int pageLength = currWritten;
          if (buffer.hasRemaining()) {
            int limit = Math.min(buffer.remaining(), MIN_PAGE_SIZE * 2);
            String text = buffer.subSequence(0, limit).toString();
            itr.setText(text);
            int nextBoundary = itr.following(Math.min(MIN_PAGE_SIZE - 1, text.length()));
            int sliceLength = nextBoundary == BreakIterator.DONE ? text.length() : nextBoundary;
            currOutput.write(text.substring(0, sliceLength));
            pageLength += sliceLength;
            buffer.position(buffer.position() + sliceLength);
          }

          currOutput.flush();
          currOutput.close();
          pages.add(new Page((long) pageLength));
          if (!buffer.hasRemaining() && count == -1) {
            break;
          }
          currOutput =
              new OutputStreamWriter(
                  new FileOutputStream(getPageFileForIndex(++currPageIndex)),
                  INTERNAL_STORAGE_CHARSET);
          currWritten = 0;
        }
      } else if (charsToWrite > 0) {
        currOutput.write(buffer.array(), buffer.arrayOffset() + buffer.position(), charsToWrite);
        buffer.position(buffer.position() + charsToWrite);
        currWritten += charsToWrite;
      }
      if ((needInput || !buffer.hasRemaining()) && count != -1) {
        buffer.compact();
        int remaining = buffer.limit() - buffer.position();
        if (remaining > 0) {
          count = source.read(buffer.array(), buffer.arrayOffset() + buffer.position(), remaining);
          buffer.limit(buffer.position() + Math.max(count, 0));
          buffer.position(0);
        }
      }
    }
  }

  File getPageFileForIndex(int index) {
    return new File(
        tmpDir, PAGE_PREFIX + String.format(Locale.US, "%0" + NUMBER_PAD_LEN + "d", index));
  }

  private File newTmpFile() {
    return new File(
        tmpDir,
        SWAP_TMP_PREFIX + "-" + String.format(Locale.US, "%0" + NUMBER_PAD_LEN + "d", tmpId++));
  }

  public int getPageCount() {
    return pages.size();
  }

  public void loadPageToEditor(int pageIndex, CodeEditor editor, Callback callback) {
    ioExecutor.execute(
        () -> {
          try {
            Content content;
            operationLock.lock();
            try (Reader reader =
                new InputStreamReader(
                    new FileInputStream(getPageFileForIndex(pageIndex)),
                    INTERNAL_STORAGE_CHARSET)) {
              content = ContentIO.createFrom(reader);
            } finally {
              operationLock.unlock();
            }
            mainHandler.post(
                () -> {
                  editor.setText(content);
                  if (callback != null) callback.onSuccess();
                });
          } catch (IOException e) {
            if (callback != null) mainHandler.post(() -> callback.onError(e));
          }
        });
  }

  /** Call on the main thread: snapshots the editor's current text before handing off to IO. */
  public void unloadPageFromEditor(int pageIndex, CodeEditor editor, Callback callback) {
    Page page = pages.get(pageIndex);
    Content text = editor.getText().copyTextShallow();
    ioExecutor.execute(
        () -> {
          try {
            operationLock.lock();
            try {
              File tmp = newTmpFile();
              try (FileOutputStream out = new FileOutputStream(tmp)) {
                ContentIO.writeTo(text, out, INTERNAL_STORAGE_CHARSET, true);
              }
              File pageFile = getPageFileForIndex(pageIndex);
              page.charsLength = text.length();
              pageFile.delete();
              tmp.renameTo(pageFile);
            } finally {
              operationLock.unlock();
              text.release();
            }
            if (callback != null) mainHandler.post(callback::onSuccess);
          } catch (IOException e) {
            if (callback != null) mainHandler.post(() -> callback.onError(e));
          }
        });
  }

  public void writeTo(File file, Callback callback) {
    writeTo(file, StandardCharsets.UTF_8, callback);
  }

  public void writeTo(File file, Charset charset, Callback callback) {
    ioExecutor.execute(
        () -> {
          try {
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), charset)) {
              writeToSync(writer);
            }
            if (callback != null) mainHandler.post(callback::onSuccess);
          } catch (IOException e) {
            if (callback != null) mainHandler.post(() -> callback.onError(e));
          }
        });
  }

  private void writeToSync(Writer output) throws IOException {
    operationLock.lock();
    try {
      for (int index = 0; index < pages.size(); index++) {
        try (Reader reader =
            new InputStreamReader(
                new FileInputStream(getPageFileForIndex(index)), INTERNAL_STORAGE_CHARSET)) {
          copy(reader, output);
        }
      }
    } finally {
      operationLock.unlock();
    }
  }

  private static void copy(Reader reader, Writer writer) throws IOException {
    char[] buf = new char[8192];
    int n;
    while ((n = reader.read(buf)) != -1) {
      writer.write(buf, 0, n);
    }
  }

  @Override
  public void close() {
    ioExecutor.shutdown();
    deleteRecursively(tmpDir);
  }

  private static void deleteRecursively(File file) {
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) deleteRecursively(child);
    }
    file.delete();
  }

  public static void restoreSessionFile(File tmpDir, File outputFile) throws IOException {
    restoreSessionFile(tmpDir, outputFile, StandardCharsets.UTF_8);
  }

  public static void restoreSessionFile(File tmpDir, File outputFile, Charset charset)
      throws IOException {
    File[] files = tmpDir.listFiles();
    List<File> pageFiles = new ArrayList<>();
    if (files != null) {
      for (File f : files) {
        if (f.getName().startsWith(PAGE_PREFIX)) pageFiles.add(f);
      }
    }
    pageFiles.sort(
        Comparator.comparingInt(
            f -> Integer.parseInt(f.getName().substring(PAGE_PREFIX.length()))));
    try (Writer output = new OutputStreamWriter(new FileOutputStream(outputFile), charset)) {
      for (File f : pageFiles) {
        try (Reader reader =
            new InputStreamReader(new FileInputStream(f), INTERNAL_STORAGE_CHARSET)) {
          copy(reader, output);
        }
      }
    }
  }

  public static class Page {
    public long charsLength;

    Page(long charsLength) {
      this.charsLength = charsLength;
    }
  }
}
