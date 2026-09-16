package ir.hanzodev1375.ghostide.helper;

import android.view.View;
import ir.hanzodev1375.components.RenameDialogFragment;
import ir.hanzodev1375.components.sheet.customitemsheet.ui.DialogCompat;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.hanzodev1375.ghostide.activity.FileManagerActivity;
import ir.hanzodev1375.ghostide.adapters.FileManagerAdapter;
import ir.hanzodev1375.ghostide.adapters.ZipBrowserAdapter;
import ir.hanzodev1375.ghostide.databinding.ActivityFilemanagerBinding;
import ir.hanzodev1375.ghostide.models.ZipEntryModel;
import ir.hanzodev1375.ghostide.models.ZipInfo;
import ir.hanzodev1375.ghostide.mvvm.viewmodel.FileViewModel;
import ir.hanzodev1375.ghostide.utils.ObjectUtil;
import ir.hanzodev1375.ghostide.utils.zip.ZipOperationManager;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.lingala.zip4j.ZipFile;

/**
 * مسیول مدیریت حالت zip (بررسی محتوای داخل فایل‌های zip).
 *
 * <p>شامل ورود/خروج به حالت zip، حرکت بین پوشه‌های داخل zip، کلیپ‌بورد zip
 * (کپی/برش/چسباندن/حذف ورودی‌ها) و منوی عملیات روی هر ورودی.
 */
public class ZipModeHelper {

  public interface SelectionChangedListener {
    void onSelectionChanged(int count);
  }

  private final FileManagerActivity activity;
  private final ActivityFilemanagerBinding bind;
  private final FileViewModel viewModel;
  private final FileManagerAdapter fileAdapter;

  private ZipBrowserAdapter zipAdapter;
  private SelectionChangedListener selectionChangedListener;

  private boolean isZipMode = false;
  private String currentZipFilePath = null;
  private List<ZipEntryModel> zipClipboard = new ArrayList<>();
  private String zipClipboardSource = null;
  private boolean zipClipboardCut = false;

  public ZipModeHelper(
      FileManagerActivity activity,
      ActivityFilemanagerBinding bind,
      FileViewModel viewModel,
      FileManagerAdapter fileAdapter) {
    this.activity = activity;
    this.bind = bind;
    this.viewModel = viewModel;
    this.fileAdapter = fileAdapter;
  }

  public void setSelectionChangedListener(SelectionChangedListener listener) {
    this.selectionChangedListener = listener;
  }

  /** راه‌اندازی آداپتر zip و شنونده‌های آن. */
  public void init() {
    zipAdapter = new ZipBrowserAdapter(activity);
    zipAdapter.setZipLoadListener(
        new ZipBrowserAdapter.ZipLoadListener() {
          @Override
          public void onLoadStarted() {
            bind.loadingprogass.setVisibility(View.VISIBLE);
          }

          @Override
          public void onLoadFinished(String internalPath, boolean hasParent) {
            bind.loadingprogass.setVisibility(View.GONE);
            if (currentZipFilePath != null) {
              bind.rvfiles.setLocationKey("zip:" + currentZipFilePath + "@" + internalPath);
              bind.rvfiles.restoreScrollPosition();
            }
          }

          @Override
          public void onLoadError(String message) {
            bind.loadingprogass.setVisibility(View.GONE);
            GhostToast.makeText(activity, "خطا: " + message, GhostToast.LENGTH_SHORT).show();
            exitZipMode();
          }
        });
    zipAdapter.setOnItemClickListener(
        (item, position) -> {
          if (item.isDirectory()) {
            if (currentZipFilePath != null) {
              bind.rvfiles.setLocationKey(
                  "zip:" + currentZipFilePath + "@" + zipAdapter.getCurrentInternalPath());
              bind.rvfiles.saveScrollPosition();
            }
            zipAdapter.loadZip(currentZipFilePath, item.getEntryPath());
          } else {
            extractAndOpenZipEntry(item);
          }
        });
    zipAdapter.setOnMoreClickListener(this::showItemMenu);
    zipAdapter.setSelectionStateListener(
        new ZipBrowserAdapter.SelectionStateListener() {
          @Override
          public void onSelectionChanged(int count) {
            if (selectionChangedListener != null) selectionChangedListener.onSelectionChanged(count);
          }

          @Override
          public void onSelectionModeStarted() {}

          @Override
          public void onSelectionModeEnded() {}
        });
  }

  public void enterZipMode(String zipFilePath) {
    isZipMode = true;
    currentZipFilePath = zipFilePath;
    String dirPath = viewModel.getCurrentPath().getValue();
    if (dirPath != null) {
      bind.rvfiles.setLocationKey("dir:" + dirPath);
      bind.rvfiles.saveScrollPosition();
    }
    bind.rvfiles.setLocationKey("zip:" + zipFilePath);
    bind.rvfiles.setAdapter(zipAdapter);
    zipAdapter.setupSelectionTracker(bind.rvfiles);
    zipAdapter.loadZip(zipFilePath, "");
    activity.setFabVisible(false);
    bind.gitActionButton.setVisibility(View.GONE);
  }

  public void exitZipMode() {
    if (currentZipFilePath != null) {
      bind.rvfiles.setLocationKey(
          "zip:" + currentZipFilePath + "@" + zipAdapter.getCurrentInternalPath());
      bind.rvfiles.saveScrollPosition();
    }
    isZipMode = false;
    currentZipFilePath = null;
    zipAdapter.clearSelection();
    resetZipClipboard();
    bind.rvfiles.setAdapter(fileAdapter);
    fileAdapter.setupSelectionTracker(bind.rvfiles);
    viewModel.loadFiles(viewModel.getCurrentPath().getValue());
    activity.setFabVisible(true);
    activity.updateGitActionVisibility(viewModel.getCurrentPath().getValue());
  }

  /** تلاش برای رفتن به پوشه‌ی والد داخل zip؛ اگر در ریشه بود false برمی‌گرداند. */
  public boolean navigateUp() {
    if (currentZipFilePath == null) return false;
    return zipAdapter.navigateUp();
  }

  public boolean isZipMode() {
    return isZipMode;
  }

  public ZipBrowserAdapter getZipAdapter() {
    return zipAdapter;
  }

  public String getCurrentZipFilePath() {
    return currentZipFilePath;
  }

  public List<ZipEntryModel> getZipClipboard() {
    return zipClipboard;
  }

  public boolean hasZipClipboard() {
    return !zipClipboard.isEmpty();
  }

  public void resetZipClipboard() {
    zipClipboard.clear();
    zipClipboardSource = null;
    zipClipboardCut = false;
  }

  public void copySelection() {
    List<ZipEntryModel> selected = zipAdapter.getSelectedItems();
    if (selected.isEmpty()) return;
    zipClipboard = new ArrayList<>(selected);
    zipClipboardSource = currentZipFilePath;
    zipClipboardCut = false;
    zipAdapter.clearSelection();
    activity.markPasteAvailable();
    activity.setSelectionCount("0");
  }

  public void cutSelection() {
    List<ZipEntryModel> selected = zipAdapter.getSelectedItems();
    if (selected.isEmpty()) return;
    zipClipboard = new ArrayList<>(selected);
    zipClipboardSource = currentZipFilePath;
    zipClipboardCut = true;
    zipAdapter.clearSelection();
    activity.markPasteAvailable();
    activity.setSelectionCount("0");
  }

  public void deleteSelection() {
    List<ZipEntryModel> selected = zipAdapter.getSelectedItems();
    if (selected.isEmpty()) return;
    List<String> entryPaths = new ArrayList<>();
    for (ZipEntryModel e : selected) entryPaths.add(e.getEntryPath());
    new DialogCompat(activity)
        .setTitle(activity.getString(R.string.removed))
        .setMessage(activity.getString(R.string.removedmassges, selected.size()))
        .setPositiveButton(
            activity.getString(R.string.ok),
            (d, w) ->
                new ZipOperationManager()
                    .deleteEntries(
                        currentZipFilePath,
                        entryPaths,
                        new ZipOperationManager.Callback() {
                          @Override
                          public void onSuccess(String msg) {
                            GhostToast.makeText(
                                    activity,
                                    activity.getString(R.string.zip_deleted_ok),
                                    GhostToast.LENGTH_SHORT)
                                .show();
                            reloadCurrentEntry();
                          }

                          @Override
                          public void onError(String err) {
                            GhostToast.makeText(
                                    activity,
                                    activity.getString(R.string.zip_error_prefix, err),
                                    GhostToast.LENGTH_SHORT)
                                .show();
                          }
                        }))
        .setNegativeButton(activity.getString(R.string.cancel), null)
        .show();
  }

  public void pasteClipboard() {
    if (zipClipboard.isEmpty()) return;
    String destDir = viewModel.getCurrentPath().getValue();
    if (destDir == null) return;
    List<String> entryPaths = new ArrayList<>();
    for (ZipEntryModel e : zipClipboard) entryPaths.add(e.getEntryPath());
    String sourceZip = zipClipboardSource;
    boolean cut = zipClipboardCut;
    new ZipOperationManager()
        .extractMultiple(
            currentZipFilePath,
            entryPaths,
            destDir,
            new ZipOperationManager.ProgressCallback() {
              @Override
              public void onProgress(int percent, String fileName) {}

              @Override
              public void onSuccess(String msg) {
                GhostToast.makeText(
                        activity,
                        activity.getString(R.string.zip_extracted_ok),
                        GhostToast.LENGTH_SHORT)
                    .show();
                zipAdapter.clearSelection();
                resetZipClipboard();
                activity.clearPasteAvailable();
                if (cut && sourceZip != null && sourceZip.equals(currentZipFilePath)) {
                  new ZipOperationManager()
                      .deleteEntries(
                          sourceZip,
                          entryPaths,
                          new ZipOperationManager.Callback() {
                            @Override
                            public void onSuccess(String msg1) {
                              reloadCurrentEntry();
                            }

                            @Override
                            public void onError(String err) {
                              GhostToast.makeText(
                                      activity,
                                      activity.getString(R.string.zip_error_prefix, err),
                                      GhostToast.LENGTH_SHORT)
                                  .show();
                            }
                          });
                }
              }

              @Override
              public void onError(String err) {
                GhostToast.makeText(
                        activity,
                        activity.getString(R.string.zip_error_prefix, err),
                        GhostToast.LENGTH_SHORT)
                    .show();
              }
            });
  }

  public void selectAll() {
    zipAdapter.selectAll();
  }

  public void clearSelection() {
    zipAdapter.clearSelection();
  }

  public void reloadCurrentEntry() {
    zipAdapter.loadZip(currentZipFilePath, zipAdapter.getCurrentInternalPath());
  }

  private void showItemMenu(ZipEntryModel item, View anchor, int pos) {
    List<String> items =
        List.of(
            activity.getString(R.string.removed),
            activity.getString(R.string.rename),
            activity.getString(R.string.zip_extract_here),
            activity.getString(R.string.zip_extract_to),
            activity.getString(R.string.zip_info));
    ObjectUtil.showGlassMenu(
        activity,
        anchor,
        items,
        (index, title) -> {
          ZipOperationManager zipOp = new ZipOperationManager();
          String destDefault = new File(currentZipFilePath).getParent();
          switch (index) {
            case 0 -> confirmDeleteEntry(item, zipOp);
            case 1 -> renameEntryDialog(item, zipOp);
            case 2 -> extractSingleEntry(item, zipOp, destDefault);
            case 3 -> confirmExtractTo(item, zipOp, destDefault);
            case 4 -> showInfo(item, zipOp);
          }
        });
  }

  private void confirmDeleteEntry(ZipEntryModel item, ZipOperationManager zipOp) {
    new DialogCompat(activity)
        .setTitle(activity.getString(R.string.removed))
        .setMessage(activity.getString(R.string.removedmassges, item.getName()))
        .setPositiveButton(
            activity.getString(R.string.ok),
            (d, w) ->
                zipOp.deleteEntries(
                    currentZipFilePath,
                    item.getEntryPath(),
                    new ZipOperationManager.Callback() {
                      @Override
                      public void onSuccess(String msg) {
                        GhostToast.makeText(
                                activity,
                                activity.getString(R.string.zip_deleted_ok),
                                GhostToast.LENGTH_SHORT)
                            .show();
                        reloadCurrentEntry();
                      }

                      @Override
                      public void onError(String err) {
                        GhostToast.makeText(
                                activity,
                                activity.getString(R.string.zip_error_prefix, err),
                                GhostToast.LENGTH_SHORT)
                            .show();
                      }
                    }))
        .setNegativeButton(activity.getString(R.string.cancel), null)
        .show();
  }

  private void renameEntryDialog(ZipEntryModel item, ZipOperationManager zipOp) {
    RenameDialogFragment dialog =
        RenameDialogFragment.getInstance(
            item.getName(),
            (prefix, extension) -> {
              String newName =
                  (extension != null && !extension.isEmpty()) ? prefix + "." + extension : prefix;
              zipOp.renameEntry(
                  currentZipFilePath,
                  item.getEntryPath(),
                  newName,
                  new ZipOperationManager.Callback() {
                    @Override
                    public void onSuccess(String msg) {
                      GhostToast.makeText(
                              activity,
                              activity.getString(R.string.zip_renamed_ok),
                              GhostToast.LENGTH_SHORT)
                          .show();
                      reloadCurrentEntry();
                    }

                    @Override
                    public void onError(String err) {
                      GhostToast.makeText(
                              activity,
                              activity.getString(R.string.zip_error_prefix, err),
                              GhostToast.LENGTH_SHORT)
                          .show();
                    }
                  });
            });
    dialog.show(activity.getSupportFragmentManager(), RenameDialogFragment.TAG);
  }

  private void extractSingleEntry(ZipEntryModel item, ZipOperationManager zipOp, String dest) {
    zipOp.extractSingle(
        currentZipFilePath,
        item.getEntryPath(),
        dest,
        new ZipOperationManager.Callback() {
          @Override
          public void onSuccess(String msg) {
            GhostToast.makeText(
                    activity,
                    activity.getString(R.string.zip_extracted_ok),
                    GhostToast.LENGTH_SHORT)
                .show();
          }

          @Override
          public void onError(String err) {
            GhostToast.makeText(
                    activity,
                    activity.getString(R.string.zip_error_prefix, err),
                    GhostToast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void confirmExtractTo(ZipEntryModel item, ZipOperationManager zipOp, String dest) {
    new DialogCompat(activity)
        .setTitle(activity.getString(R.string.zip_extract_to))
        .setMessage(activity.getString(R.string.zip_extract_dest, dest))
        .setPositiveButton(
            activity.getString(R.string.ok),
            (d, w) -> extractSingleEntry(item, zipOp, dest))
        .setNegativeButton(activity.getString(R.string.cancel), null)
        .show();
  }

  private void showInfo(ZipEntryModel item, ZipOperationManager zipOp) {
    zipOp.getZipInfo(
        currentZipFilePath,
        new ZipOperationManager.ZipInfoCallback() {
          @Override
          public void onInfo(ZipInfo info) {
            new DialogCompat(activity)
                .setTitle(activity.getString(R.string.zip_info))
                .setMessage(
                    activity.getString(R.string.zip_info_files, info.fileCount)
                        + "\n"
                        + activity.getString(R.string.zip_info_dirs, info.dirCount)
                        + "\n"
                        + activity.getString(
                            R.string.zip_info_original, formatSize(info.totalUncompressed))
                        + "\n"
                        + activity.getString(
                            R.string.zip_info_compressed, formatSize(info.totalCompressed))
                        + "\n"
                        + activity.getString(R.string.zip_info_ratio, info.compressionRatio)
                        + "\n"
                        + activity.getString(
                            R.string.zip_info_encrypted,
                            info.isEncrypted
                                ? activity.getString(R.string.zip_info_yes)
                                : activity.getString(R.string.zip_info_no)))
                .setPositiveButton(activity.getString(R.string.ok), null)
                .show();
          }

          @Override
          public void onError(String err) {
            GhostToast.makeText(
                    activity,
                    activity.getString(R.string.zip_error_prefix, err),
                    GhostToast.LENGTH_SHORT)
                .show();
          }
        });
  }

  private void extractAndOpenZipEntry(ZipEntryModel entry) {
    File cacheDir = new File(activity.getCacheDir(), "zip_extract");
    if (!cacheDir.exists()) cacheDir.mkdirs();
    File outFile = new File(cacheDir, entry.getName());
    new Thread(
            () -> {
              try (ZipFile zipFile = new ZipFile(entry.getParentZipPath())) {
                zipFile.extractFile(entry.getEntryPath(), cacheDir.getAbsolutePath(), entry.getName());
                activity.runOnUiThread(
                    () -> {
                      if (entry.isEncrypted()) {
                        GhostToast.makeText(activity, "File Has Encrypted", GhostToast.LENGTH_LONG)
                            .show();
                      } else {
                        activity.setupClick(outFile.getAbsolutePath(), entry.getName(), null);
                      }
                    });
              } catch (Exception e) {
                activity.runOnUiThread(
                    () ->
                        GhostToast.makeText(
                                activity, "Error to UnZip", GhostToast.LENGTH_SHORT)
                            .show());
              }
            })
        .start();
  }

  private String formatSize(long bytes) {
    if (bytes >= 1024 * 1024)
      return String.format(Locale.getDefault(), "%.2f MB", bytes / (1024.0 * 1024.0));
    else if (bytes >= 1024) return String.format(Locale.getDefault(), "%.1f KB", bytes / 1024.0);
    else return bytes + " B";
  }
}