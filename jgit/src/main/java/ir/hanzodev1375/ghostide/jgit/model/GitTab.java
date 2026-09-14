package ir.hanzodev1375.ghostide.jgit.model;

import androidx.fragment.app.Fragment;

public class GitTab {
  private final String title;
  private final Fragment fragment;

  public GitTab(String title, Fragment fragment) {
    this.title = title;
    this.fragment = fragment;
  }

  public String getTitle() {
    return title;
  }

  public Fragment getFragment() {
    return fragment;
  }
}
