package ir.hanzodev1375.ghostide.shizuku;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import ir.hanzodev1375.ghostide.interfaces.IShellUserService;
import rikka.shizuku.Shizuku;

public class ShizukuManager {

  public static final int REQUEST_CODE = 1001;
  private static final String SERVICE_TAG = "ghostide_shell_service";

  public interface ExecCallback {
    void onResult(String output);
    void onUnavailable();
  }

  private static IShellUserService service;
  private static boolean binding = false;

  
  public static boolean isAvailable() {
    try {
      return Shizuku.pingBinder();
    } catch (Throwable t) {
      return false;
    }
  }

  public static boolean hasPermission() {
    return isAvailable() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
  }

  /** Call from an Activity. Result arrives via Shizuku.OnRequestPermissionResultListener. */
  public static void requestPermission() {
    if (isAvailable() && !hasPermission()) {
      Shizuku.requestPermission(REQUEST_CODE);
    }
  }

  /** Register once, e.g. in GhostIdeAppLoader#onCreate(). Safe to call multiple times. */
  public static void registerListeners() {
    Shizuku.addBinderDeadListener(() -> service = null);
  }

  /** Runs {@code command} with shell/root identity and posts the output back. */
  public static void exec(String[] command, ExecCallback callback) {
    if (!hasPermission()) {
      callback.onUnavailable();
      return;
    }
    if (service != null) {
      runOn(service, command, callback);
      return;
    }
    if (binding) return;
    binding = true;

    Shizuku.UserServiceArgs args =
        new Shizuku.UserServiceArgs(
                new ComponentName(
                    "ir.hanzodev1375.ghostide", ShellUserService.class.getName()))
            .daemon(false)
            .processNameSuffix("shell")
            .version(1)
            .tag(SERVICE_TAG);

    Shizuku.bindUserService(
        args,
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder binder) {
            binding = false;
            if (binder == null || !binder.pingBinder()) {
              callback.onUnavailable();
              return;
            }
            runOn(service, command, callback);
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            binding = false;
            service = null;
          }
        });
  }

  private static void runOn(IShellUserService svc, String[] command, ExecCallback callback) {
    new Thread(
            () -> {
              String result;
              try {
                result = svc.exec(command);
              } catch (Exception e) {
                result = "error: " + e.getMessage();
              }
              final String r = result;
              new android.os.Handler(android.os.Looper.getMainLooper())
                  .post(() -> callback.onResult(r));
            })
        .start();
  }
}