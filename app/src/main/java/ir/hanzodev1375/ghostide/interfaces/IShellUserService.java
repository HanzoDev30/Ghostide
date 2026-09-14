package ir.hanzodev1375.ghostide.interfaces;

public interface IShellUserService {
  String exec(String[] command);

  void destroy();
}
