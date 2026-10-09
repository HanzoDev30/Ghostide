package ir.hanzodev1375.ghostide.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import ir.hanzodev1375.components.image.ZoomableImageView;
import ir.hanzodev1375.components.views.GhostToast;
import ir.hanzodev1375.ghostide.R;
import ir.theme.M3Theme;

public class ImageViewerFragment extends Fragment {

  private static final String ARG_URI = "arg_uri";
  private static final String ARG_POSITION = "arg_position";

  public interface Host {
    int getRotation(int position);

    void onRotationChanged(int position, int degrees);

    void onZoomStateChanged(int position, boolean zoomed);
  }

  private ZoomableImageView zoomView;
  private int position;

  public static ImageViewerFragment newInstance(Uri uri, int position) {
    ImageViewerFragment f = new ImageViewerFragment();
    Bundle args = new Bundle();
    args.putString(ARG_URI, uri.toString());
    args.putInt(ARG_POSITION, position);
    f.setArguments(args);
    return f;
  }

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_image_viewer, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    zoomView = view.findViewById(R.id.ivMainImage);
    Bundle args = getArguments();
    if (args == null) return;
    position = args.getInt(ARG_POSITION, 0);
    String uriString = args.getString(ARG_URI);
    if (uriString == null) return;
    if (requireActivity() instanceof Host) {
      Host host = (Host) requireActivity();
      zoomView.applyRotation(host.getRotation(position));
      zoomView.setOnZoomStateListener(
          zoomed -> host.onZoomStateChanged(position, zoomed));
    }
    zoomView.setOnImageEventListener(
        new SubsamplingScaleImageView.DefaultOnImageEventListener() {
          @Override
          public void onImageLoadError(Exception e) {
            if (getContext() != null) {
              GhostToast.makeText(
                      getContext(), R.string.viewer_load_error, GhostToast.LENGTH_SHORT)
                  .show();
            }
          }
        });
    zoomView.setImage(ImageSource.uri(Uri.parse(uriString)));
    M3Theme.apply(view);
  }

  public void toggleZoom() {
    if (zoomView != null) zoomView.toggleZoom(true);
  }

  public void rotateClockwise() {
    if (zoomView == null) return;
    int degrees = zoomView.rotateClockwise();
    if (requireActivity() instanceof Host) {
      ((Host) requireActivity()).onRotationChanged(position, degrees);
    }
  }

  public void fitToScreen() {
    if (zoomView != null) zoomView.fitToScreen(true);
  }

  public void fillScreen() {
    if (zoomView != null) zoomView.fillScreen();
  }

  public void showActualSize() {
    if (zoomView != null) zoomView.showActualSize(true);
  }

  @Override
  public void onDestroyView() {
    if (zoomView != null) {
      zoomView.setOnZoomStateListener(null);
      zoomView = null;
    }
    super.onDestroyView();
  }
}
