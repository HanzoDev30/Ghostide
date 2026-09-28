package ir.ghostide.logcat;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import ir.hanzodev1375.components.views.GhostToast;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.theme.M3Theme;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MaterialLogCatView extends LinearLayout {

  private RecyclerView recyclerView;
  private EditText searchBox;
  private FloatingActionButton btnSave;
  private LogAdapter adapter;
  private static final int PERMISSION_REQUEST_CODE = 100;
  private final ExecutorService loader = Executors.newSingleThreadExecutor();
  private volatile boolean loading;

  private void postOnMain(Runnable action) {
    new Handler(Looper.getMainLooper()).post(action);
  }

  public MaterialLogCatView(Context context) {
    super(context);
    init(context);
  }

  public MaterialLogCatView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    inflate(context, R.layout.view_material_logcat, this);
    recyclerView = findViewById(R.id.recycler_logs);
    searchBox = findViewById(R.id.search_logs);
    btnSave = findViewById(R.id.fab_save_logs);
    M3Theme.fabView(btnSave);
    M3Theme.editText(searchBox);

    recyclerView.setLayoutManager(new LinearLayoutManager(context));
    recyclerView.setHasFixedSize(true);

    adapter = new LogAdapter(new java.util.ArrayList<>());
    recyclerView.setAdapter(adapter);
    recyclerView.addItemDecoration(new MarginItemDecoration());

    searchBox.addTextChangedListener(
        new TextWatcher() {
          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            adapter.getFilter().filter(s);
          }

          @Override
          public void afterTextChanged(Editable s) {}
        });

    btnSave.setOnClickListener(v -> saveLogsToFile());

    refreshLogs();
  }

  private void saveLogsToFile() {
    String allLogs = adapter.getAllFilteredMessages();
    if (allLogs.isEmpty()) {
      GhostToast.makeText(getContext(), "No logs to save", GhostToast.LENGTH_SHORT).show();
      return;
    }
    try {

      File downloadDir = new File("/storage/emulated/0/ghostide/");
      File logDir = new File(downloadDir, "applog");
      if (!logDir.exists()) {
        logDir.mkdirs();
      }

      String fileName = "logs_" + System.currentTimeMillis() + ".log";
      File logFile = new File(logDir, fileName);

      FileOutputStream fos = new FileOutputStream(logFile);
      OutputStreamWriter writer = new OutputStreamWriter(fos);
      writer.write(allLogs);
      writer.close();
      fos.close();

      GhostToast.makeText(
              getContext(),
              "Logs saved to " + logFile.getAbsolutePath(),
              GhostToast.LENGTH_LONG)
          .show();
    } catch (Exception e) {
      GhostToast.makeText(getContext(), "Save error: " + e.getMessage(), GhostToast.LENGTH_SHORT).show();
      e.printStackTrace();
    }
  }

  public void refreshLogs() {
    if (loading) {
      return;
    }
    loading = true;
    searchBox.setEnabled(false);
    loader.execute(
        () -> {
          List<LogEntry> newLogs = LogcatReader.getCurrentAppLogs();
          postOnMain(
              () -> {
                loading = false;
                if (searchBox != null) {
                  searchBox.setEnabled(true);
                }
                if (adapter != null) {
                  adapter.updateData(newLogs);
                }
              });
        });
  }

  public class MarginItemDecoration extends RecyclerView.ItemDecoration {
    private final int itemMargin;

    public MarginItemDecoration() {
      itemMargin = 2;
    }

    @Override
    public void getItemOffsets(
        @NonNull Rect outRect,
        @NonNull View view,
        @NonNull RecyclerView parent,
        @NonNull RecyclerView.State state) {
      int position = parent.getChildAdapterPosition(view);
      if (position != state.getItemCount() - 1) {
        outRect.bottom = itemMargin;
      }
    }
  }
}