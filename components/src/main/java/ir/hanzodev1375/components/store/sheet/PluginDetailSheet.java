package ir.hanzodev1375.components.store.sheet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import io.noties.markwon.Markwon;
import ir.hanzodev1375.components.R;
import ir.hanzodev1375.components.sheet.BaseBlurBottomSheet;
import ir.hanzodev1375.components.store.data.PluginRepository;
import ir.hanzodev1375.components.store.event.PluginProgressEvent;
import ir.hanzodev1375.components.store.event.PluginResultEvent;
import ir.hanzodev1375.components.store.model.PluginDoc;
import ir.hanzodev1375.components.store.model.PluginItem;
import ir.hanzodev1375.components.store.viewmodel.PluginStoreViewModel;
import ir.hanzodev1375.components.views.PluginInstallButton;
import ir.theme.M3Theme;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/** Glass detail sheet for a store plugin: icon, dev, size, markdown description, install button. */
public class PluginDetailSheet extends BaseBlurBottomSheet {

  private static final String ARG_NAME = "arg_name";
  private static final String ARG_ICON = "arg_icon";
  private static final String ARG_DOC = "arg_doc";
  private static final String ARG_GPL = "arg_gpl";
  private static final String ARG_SOURCE = "arg_source";
  private static final String ARG_INSTALLED = "arg_installed";

  private final PluginRepository repository = new PluginRepository();

  private PluginItem item;
  private boolean installed;
  private boolean started;

  private ShapeableImageView iconView;
  private TextView nameView;
  private TextView devView;
  private TextView sizeView;
  private TextView sourceView;
  private TextView noteView;
  private PluginInstallButton installButton;

  private ViewModelProvider.AndroidViewModelFactory factory;
  private PluginStoreViewModel viewModel;

  public static PluginDetailSheet newInstance(PluginItem item, boolean installed) {
    PluginDetailSheet sheet = new PluginDetailSheet();
    Bundle args = new Bundle();
    args.putString(ARG_NAME, item.name());
    args.putString(ARG_ICON, item.icon());
    args.putString(ARG_DOC, item.doc());
    args.putString(ARG_GPL, item.gplfile());
    args.putString(ARG_SOURCE, item.source());
    args.putBoolean(ARG_INSTALLED, installed);
    sheet.setArguments(args);
    return sheet;
  }

  @Override
  public void onStart() {
    super.onStart();
    if (!EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().register(this);
    }
  }

  @Override
  public void onStop() {
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
    super.onStop();
  }

  @Override
  protected void onContentReady(ViewGroup contentContainer) {
    Bundle args = getArguments();
    if (args == null || getContext() == null) return;
    Context context = requireContext();

    item =
        new PluginItem(
            args.getString(ARG_NAME, ""),
            args.getString(ARG_ICON, ""),
            args.getString(ARG_DOC, ""),
            args.getString(ARG_GPL, ""),
            args.getString(ARG_SOURCE, ""));
    installed = args.getBoolean(ARG_INSTALLED, false);

    View v =
        LayoutInflater.from(context).inflate(R.layout.sheet_plugin_detail, contentContainer, false);
    contentContainer.addView(v);

    iconView = v.findViewById(R.id.pluginDetailIcon);
    nameView = v.findViewById(R.id.pluginDetailName);
    devView = v.findViewById(R.id.pluginDetailDev);
    sizeView = v.findViewById(R.id.pluginDetailSize);
    sourceView = v.findViewById(R.id.pluginDetailSource);
    noteView = v.findViewById(R.id.pluginDetailNote);
    installButton = v.findViewById(R.id.pluginDetailInstall);

    factory =
        new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication());
    viewModel = new ViewModelProvider(requireActivity(), factory).get(PluginStoreViewModel.class);

    nameView.setText(item.name());
    devView.setText(context.getString(R.string.pluginstore_by_dev, "…"));
    sourceView.setText(
        item.hasSource()
            ? context.getString(R.string.pluginstore_source_available)
            : context.getString(R.string.pluginstore_source_missing));

    loadIcon(context);
    loadDoc(context);
    loadSize(context);

    installButton.applyTheme();
    applyState();

    installButton.setOnInstallClickListener(this::onInstallClick);

    M3Theme.text(nameView, devView, sizeView, sourceView, noteView);
    M3Theme.card(v.findViewById(R.id.pluginDetailSizeCard), v.findViewById(R.id.pluginDetailSourceCard));
    M3Theme.applyShallow(v);
  }

  private void loadIcon(Context context) {
    if (item.icon() == null || item.icon().trim().isEmpty()) {
      iconView.setImageResource(R.drawable.ic_outline_extension);
      return;
    }
    Glide.with(context)
        .load(item.icon())
        .override(160, 160)
        .placeholder(R.drawable.ic_outline_extension)
        .error(R.drawable.ic_outline_extension)
        .dontAnimate()
        .into(iconView);
  }

  private void loadDoc(Context context) {
    Markwon markwon = Markwon.create(context);
    noteView.setText(R.string.pluginstore_no_description);
    repository.fetchDoc(
        item,
        new PluginRepository.Callback<PluginDoc>() {
          @Override
          public void onSuccess(PluginDoc doc) {
            if (noteView == null || doc == null) return;
            String note = doc.note();
            String dev = doc.devname();
            if (dev != null && !dev.trim().isEmpty() && devView != null) {
              devView.setText(context.getString(R.string.pluginstore_by_dev, dev));
            }
            if (note != null && !note.trim().isEmpty()) {
              markwon.setMarkdown(noteView, note);
            }
            if (doc.icon() != null && !doc.icon().trim().isEmpty() && iconView != null) {
              String url = doc.icon();
              if (!url.startsWith("http")) {
                int slash = item.doc().lastIndexOf('/');
                url = slash >= 0 ? item.doc().substring(0, slash + 1) + url : url;
              }
              Glide.with(context)
                  .load(url)
                  .override(160, 160)
                  .placeholder(R.drawable.ic_outline_extension)
                  .error(R.drawable.ic_outline_extension)
                  .dontAnimate()
                  .into(iconView);
            }
          }

          @Override
          public void onError(String message) {
            if (noteView == null) return;
            noteView.setText(R.string.pluginstore_no_description);
          }
        });
  }

  private void loadSize(Context context) {
    repository.fetchSize(
        item,
        new PluginRepository.Callback<Long>() {
          @Override
          public void onSuccess(Long bytes) {
            if (sizeView == null || bytes == null || bytes <= 0L) return;
            sizeView.setText(formatSize(bytes));
          }

          @Override
          public void onError(String message) {
            if (sizeView == null) return;
            sizeView.setText(R.string.pluginstore_size_unknown);
          }
        });
  }

  private void applyState() {
    if (installButton == null) return;
    if (!item.hasGpl()) {
      installButton.setEnabled(false);
      installButton.setText(R.string.pluginstore_no_gpl);
      return;
    }
    installButton.setEnabled(true);
    if (started) {
      installButton.setState(PluginInstallButton.State.INSTALLING);
    } else if (installed) {
      installButton.setState(PluginInstallButton.State.INSTALLED);
    } else {
      installButton.setState(PluginInstallButton.State.IDLE);
    }
  }

  private void onInstallClick() {
    if (viewModel == null || item == null) return;
    if (installed) {
      viewModel.requestSetup(item);
      return;
    }
    started = true;
    installButton.setState(PluginInstallButton.State.INSTALLING);
    installButton.setProgress(0f);
    viewModel.install(item, false);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onProgress(PluginProgressEvent event) {
    if (installButton == null || !started || item == null) return;
    if (event.plugin == null || !item.name().equals(event.plugin.name())) return;
    installButton.setProgress(event.progress);
  }

  @Subscribe(threadMode = ThreadMode.MAIN)
  public void onResult(PluginResultEvent event) {
    if (installButton == null || item == null) return;
    if (event.plugin == null || !item.name().equals(event.plugin.name())) return;
    if (event.success) {
      installed = true;
      started = false;
      installButton.setState(PluginInstallButton.State.INSTALLED);
    } else {
      started = false;
      installButton.setState(PluginInstallButton.State.IDLE);
      if (getContext() != null) {
        Toast.makeText(
                requireContext(),
                getString(R.string.pluginstore_install_error, event.message),
                Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  @Override
  public void onDestroyView() {
    installButton = null;
    super.onDestroyView();
  }

  private static String formatSize(long bytes) {
    if (bytes < 1024L) return bytes + " B";
    double kb = bytes / 1024.0;
    if (kb < 1024.0) return String.format(Locale.getDefault(), "%.1f KB", kb);
    double mb = kb / 1024.0;
    if (mb < 1024.0) return String.format(Locale.getDefault(), "%.1f MB", mb);
    return String.format(Locale.getDefault(), "%.2f GB", mb / 1024.0);
  }
}
