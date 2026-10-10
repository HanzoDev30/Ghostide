package ir.hanzodev1375.ghostide.terminal;

import android.content.Context;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * پرامپتِ شلِ دبیان (proot) رو طوری تنظیم می‌کنه که به‌جای {@code root}، یوزرنیمِ حسابِ گیت‌هابِ
 * کاربر نشون داده بشه.
 *
 * <p>مکانیزم: یه اسکریپت کوچیک توی {@code /etc/profile.d/} داخل rootfs نوشته میشه. چون بج با
 * {@code --login} اجرا میشه، {@code /etc/profile} دبیان همه‌ی {@code profile.d/*.sh} رو سورس
 * میکنه و {@code PS1} ست میشه. مقدارِ یوزرنیم از طریق env var به اسم {@link #GHOST_USER_ENV}
 * به سشن پاس داده میشه (نه داخل فایل)، پس با هر لاگین/تغییر حساب خودکار به‌روز میشه.
 */
public final class TerminalPrompt {

  private TerminalPrompt() {}

  /** اسم env varی که یوزرنیم گیت‌هاب رو به شلِ داخل proot می‌رسونه. */
  public static final String GHOST_USER_ENV = "GHOST_USER";

  private static final String PROFILE_D_DIR = "etc/profile.d";
  private static final String PROFILE_SCRIPT_NAME = "ghost-prompt.sh";

  /**
   * یوزرنیمِ حساب گیت‌هاب رو از تنظیمات می‌خونه؛ اگه خالی بود اسم نمایشی، وگرنه رشته‌ی خالی.
   */
  public static String resolveUsername(Context context) {
    PreferencesUtils prefs = new PreferencesUtils(context);
    String username = prefs.getGitHubUsername();
    if (username == null || username.trim().isEmpty()) {
      username = prefs.getGitHubName();
    }
    return username == null ? "" : username.trim();
  }

  /** اسکریپت پرامپت رو داخل rootfs می‌نویسه (idempotent). */
  public static void install(File rootfsDir) throws IOException {
    File profileDir = new File(rootfsDir, PROFILE_D_DIR);
    if (!profileDir.isDirectory() && !profileDir.mkdirs()) {
      throw new IOException("could not create " + profileDir.getAbsolutePath());
    }
    // داخل دابل‌کوت: \w و \$ حفظ میشن و ${GHOST_USER} موقع نمایش پرامپت باز میشه.
    // اگه کاربر لاگین نکرده باشه، ${GHOST_USER:-root} به root برمی‌گرده.
    String script =
        "export PS1=\"${" + GHOST_USER_ENV + ":-root}@localhost:\\w \\$ \"\n";
    try (FileOutputStream out = new FileOutputStream(new File(profileDir, PROFILE_SCRIPT_NAME))) {
      out.write(script.getBytes(StandardCharsets.UTF_8));
    }
  }
}
