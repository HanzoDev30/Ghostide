package ir.hanzodev1375.ghostide.jgit.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import ir.hanzodev1375.ghostide.jgit.model.GitTab;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {
  private final List<GitTab> tabs;

  public ViewPagerAdapter(@NonNull FragmentActivity activity, List<GitTab> tabs) {
    super(activity);
    this.tabs = tabs;
  }

  /**
   * با میزبانِ Activity فرگمنت‌های تب بعد از بسته شدن شیت در FragmentManager اکتیویتی می‌مانند و
   * observer هایشان زنده می‌ماند؛ با هر بار باز کردن شیت تعدادشان بیشتر می‌شد.
   */
  public ViewPagerAdapter(@NonNull Fragment host, List<GitTab> tabs) {
    super(host);
    this.tabs = tabs;
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    return tabs.get(position).getFragment();
  }

  @Override
  public int getItemCount() {
    return tabs.size();
  }

  public String getPageTitle(int position) {
    return tabs.get(position).getTitle();
  }
}
