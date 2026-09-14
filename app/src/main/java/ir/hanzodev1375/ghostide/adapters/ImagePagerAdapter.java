package ir.hanzodev1375.ghostide.adapters;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ir.hanzodev1375.ghostide.fragments.ImageViewerFragment;
import java.util.List;

public class ImagePagerAdapter extends FragmentStateAdapter {

  private final List<Uri> uriList;

  public ImagePagerAdapter(@NonNull FragmentActivity fa, List<Uri> uriList) {
    super(fa);
    this.uriList = uriList;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return ImageViewerFragment.newInstance(uriList.get(position));
  }

  @Override
  public int getItemCount() {
    return uriList != null ? uriList.size() : 0;
  }
}
