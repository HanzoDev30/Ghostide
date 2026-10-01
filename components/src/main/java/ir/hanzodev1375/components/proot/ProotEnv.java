package ir.hanzodev1375.components.proot;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * سازنده‌ی متغیرهای محیطیِ مشترکِ proot؛ همه‌ی جاهایی که proot را اجرا می‌کنند باید از این کلاس
 * استفاده کنند تا تنظیمات (مخصوصاً تشخیص کرنل‌های قدیمی) در یک نقطه بماند.
 *
 * <p>چرا {@code PROOT_NO_SECCOMP} لازم است: proot برای رهگیری سریع‌تر syscallها از فیلتر
 * seccomp حالت ۲ استفاده می‌کند. بعضی کرنل‌های قدیمیِ اندروید بیت {@code PTRACE_O_TRACESECCOMP} را
 * بی‌سروصدا قبول می‌کنند ولی پیاده‌سازی نمی‌کنند؛ در آن حالت syscall فیلترشده به‌جای رویدادِ
 * ptrace مقدار {@code -ENOSYS} برمی‌گرداند و proot با پیام
 * {@code Function not implemented} و راهنمای «possible causes» از کار می‌افتد. با غیرفعال‌کردن
 * seccomp، رهگیری از مسیرِ همیشگیِ {@code PTRACE_SYSCALL} انجام می‌شود و این خطا رخ نمی‌دهد
 * (کمی کندتر، ولی سازگار با همه‌ی کرنل‌ها).
 *
 * <p>این متغیر فقط روی کرنل‌های قدیمی ست می‌شود تا سرعت روی دستگاه‌های جدید حفظ شود. آستانه از
 * همان چیزی گرفته شده که خودِ proot در {@code tracee/event.c} برای تشخیص پشتیبانی از
 * {@code PTRACE_EVENT_SECCOMP} استفاده می‌کند، ولی محافظه‌کارانه‌تر (۴.۹ به‌جای ۳.۵).
 */
public final class ProotEnv {

  private static final String LOG_TAG = "GHOST_PROOT_ENV";

  private static final String PROOT_LIBRARY_NAME = "libproot.so";
  private static final String LOADER_LIBRARY_NAME = "libloader.so";
  private static final String LOADER32_LIBRARY_NAME = "libloader32.so";
  private static final String TMP_DIR_NAME = "proot-tmp";
  private static final String OS_RELEASE_PATH = "/proc/sys/kernel/osrelease";
  private static final int MIN_SAFE_KERNEL_MINOR = 9;

  private static Boolean seccompSafe;

  private ProotEnv() {}

  /** مسیر پوشه‌ی native library های اپ (همان‌جایی که libproot.so و libloader.so هستند). */
  public static String nativeLibraryDir(Context context) {
    return context.getApplicationInfo().nativeLibraryDir;
  }

  public static File prootBinary(Context context) {
    return new File(nativeLibraryDir(context), PROOT_LIBRARY_NAME);
  }

  public static File loaderBinary(Context context) {
    return new File(nativeLibraryDir(context), LOADER_LIBRARY_NAME);
  }

  /** لودرِ ۳۲ بیتی؛ فقط روی دستگاه‌های ۶۴ بیتی وجود دارد و ممکن است {@code null} باشد. */
  public static File loader32Binary(Context context) {
    File loader = new File(nativeLibraryDir(context), LOADER32_LIBRARY_NAME);
    return loader.exists() ? loader : null;
  }

  /** مسیرِ پوشه‌ی موقتِ مشترکِ proot؛ همه‌ی اجراهای کوتاه‌مدت از این استفاده می‌کنند. */
  public static File sharedTmpDir(Context context) {
    return new File(context.getCacheDir(), TMP_DIR_NAME);
  }

  /**
   * همه‌ی متغیرهای لازمِ proot را داخلِ {@code env} می‌نویسد. فرض می‌کند {@code env} قبلاً
   * {@code clear()} شده تا هیچ متغیرِ میزبانِ اندروید به داخل rootfs نشت نکند.
   */
  public static void apply(Map<String, String> env, Context context) {
    apply(env, context, sharedTmpDir(context));
  }

  public static void apply(Map<String, String> env, Context context, File tmpDir) {
    env.put("PROOT_TMP_DIR", tmpDir.getAbsolutePath());
    env.put("PROOT_LOADER", loaderBinary(context).getAbsolutePath());
    env.put("LD_LIBRARY_PATH", nativeLibraryDir(context));

    File loader32 = loader32Binary(context);
    if (loader32 != null) {
      env.put("PROOT_LOADER_32", loader32.getAbsolutePath());
    }
    if (!isSeccompSafe()) {
      env.put("PROOT_NO_SECCOMP", "1");
    }
    Log.i(LOG_TAG, "kernel=" + readKernelRelease() + " env=" + env);
  }

  /** همان {@link #apply} ولی به شکل لیستِ {@code KEY=VALUE} برای {@code TerminalSession}. */
  public static List<String> entries(Context context, File tmpDir) {
    List<String> env = new ArrayList<>();
    env.add("PROOT_TMP_DIR=" + tmpDir.getAbsolutePath());
    env.add("PROOT_LOADER=" + loaderBinary(context).getAbsolutePath());
    env.add("LD_LIBRARY_PATH=" + nativeLibraryDir(context));

    File loader32 = loader32Binary(context);
    if (loader32 != null) {
      env.add("PROOT_LOADER_32=" + loader32.getAbsolutePath());
    }
    if (!isSeccompSafe()) {
      env.add("PROOT_NO_SECCOMP=1");
    }
    return env;
  }

  /**
   * آیا کرنل دستگاه برای مسیرِ seccompِ proot قابل‌اعتماد است؟ نتیجه یک‌بار محاسبه و کش می‌شود
   * چون نسخه‌ی کرنل در طول عمر برنامه عوض نمی‌شود. اگر نسخه خوانده نشود، محافظه‌کارانه seccomp را
   * غیرفعال می‌کنیم تا برنامه روی دستگاه ناشناخته به‌جای کرش، کند کار کند.
   */
  public static boolean isSeccompSafe() {
    if (seccompSafe == null) {
      int[] version = parseKernelVersion(readKernelRelease());
      seccompSafe =
          version != null && (version[0] > 4 || (version[0] == 4 && version[1] >= MIN_SAFE_KERNEL_MINOR));
      Log.i(LOG_TAG, "seccompSafe=" + seccompSafe);
    }
    return seccompSafe;
  }

  private static String readKernelRelease() {
    try (BufferedReader reader = new BufferedReader(new FileReader(OS_RELEASE_PATH))) {
      return reader.readLine();
    } catch (IOException e) {
      Log.w(LOG_TAG, "kernel release خوانده نشد: " + e.getMessage());
      return null;
    }
  }

  /** از رشته‌ای مثل {@code 3.18.0-perf+} فقط {@code major.minor} را برمی‌گرداند. */
  private static int[] parseKernelVersion(String release) {
    if (release == null) {
      return null;
    }
    String[] parts = release.split("[.\\-+ ]");
    if (parts.length < 2) {
      return null;
    }
    try {
      return new int[] {Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
