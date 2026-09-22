package ir.hanzodev1375.ghostide.runer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import ir.hanzodev1375.ghostide.codeeditors.langs.lsp.AndroidClasspathResolver;
import ir.hanzodev1375.ghostide.codeeditors.langs.formatHelp.DebianBootstrap;
import ir.hanzodev1375.ghostide.codeeditors.setting.PreferencesUtils;
import ir.hanzodev1375.ghostide.terminal.activity.TerminalActivity;
import ir.hanzodev1375.ghostide.terminal.sheet.TerminalBottomSheetFragment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CodeRuner {

    private Context context;
    private final PreferencesUtils prefs;

    public CodeRuner(Context context) {
        this.context = context;
        this.prefs = new PreferencesUtils(context);
    }

    public void bindof(String path, boolean asBottomSheet) {
        String command = buildCommand(path);
        if (command == null) return;

        if (asBottomSheet) {
            runInBottomSheet(command);
        } else {
            runInActivity(command);
        }
    }

    /** Runs a raw shell command, without building one from a file. */
    public void runShell(String command, boolean asBottomSheet) {
        if (command == null || command.trim().isEmpty()) return;
        if (!prefs.isCodeRunnerEnabled() || !prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_SHELL)) return;
        if (asBottomSheet) {
            runInBottomSheet(command);
        } else {
            runInActivity(command);
        }
    }

    /** Whether a file is runnable (its extension maps to a registered language/toolchain). */
    public boolean isSupported(String path) {
        if (path == null) return false;
        return buildCommand(path) != null;
    }

    public void runInActivity(String command) {
        Intent i = new Intent(context, TerminalActivity.class);
        i.putExtra(TerminalActivity.EXTRA_COMMAND, command);
        if (!(context instanceof Activity)) {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(i);
    }

    public void runInBottomSheet(String command) {
        FragmentManager fm = resolveFragmentManager();
        if (fm == null) {
            runInActivity(command);
            return;
        }
        // اگه شیتِ قبلی هنوز منیجر وصل باشه (بسته/dismiss نشده)، اولش حذفش کن تا همیشه
        // یه شیتِ تازه با تب جدید باز بشه و هربار اجرای کد، تبِ قبلی دوباره لود نشه.
        Fragment previous = fm.findFragmentByTag(TerminalBottomSheetFragment.TAG);
        if (previous != null) {
            if (previous instanceof DialogFragment) {
                ((DialogFragment) previous).dismissAllowingStateLoss();
            } else {
                fm.beginTransaction().remove(previous).commitAllowingStateLoss();
            }
        }
        TerminalBottomSheetFragment.newInstance(command, null).show(fm, "terminal_sheet");
    }

    private FragmentManager resolveFragmentManager() {
        if (context instanceof FragmentActivity) {
            return ((FragmentActivity) context).getSupportFragmentManager();
        }
        return null;
    }

    private String buildCommand(String path) {
        if (path == null) return null;
        if (!prefs.isCodeRunnerEnabled()) return null;

        String custom = customCommand(path);
        if (custom != null) return custom;

        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".c")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_C)) return c(path);
            return null;
        }
        if (lower.endsWith(".cpp")
                || lower.endsWith(".h")
                || lower.endsWith(".hpp")
                || lower.endsWith(".cc")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_CPP)) return cpp(path);
            return null;
        }
        if (lower.endsWith(".py")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_PYTHON)) return python(path);
            return null;
        }
        if (lower.endsWith(".php")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_PHP)) return php(path);
            return null;
        }
        if (lower.endsWith(".go")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_GO)) return go(path);
            return null;
        }
        if (lower.endsWith(".js")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_NODE)) return node(path);
            return null;
        }
        if (lower.endsWith(".ts")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_TYPESCRIPT)) return typescript(path);
            return null;
        }
        if (lower.endsWith(".lua")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_LUA)) return lua(path);
            return null;
        }
        if (lower.endsWith(".java")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_JAVA)) return java(path);
            return null;
        }
        if (lower.endsWith(".scss") || lower.endsWith(".sass")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_SASS)) return sass(path);
            return null;
        }
        if (lower.endsWith(".kt")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_KOTLIN)) return kotlin(path);
            return null;
        }
        if (lower.endsWith(".kts")) {
            if (prefs.isRunnerEnabled(PreferencesUtils.KEY_RUNNER_KOTLIN)) return kotlinScript(path);
            return null;
        }
        return null;
    }

    // ==================== Custom runners ====================

    /**
     * Looks up the file against user-defined custom runners.
     *
     * <p>Format, one per line: {@code ext:command}. Multiple extensions separated by comma, e.g.
     * {@code rb:ruby "{file}"}. Supported placeholders: {@code {file}} (full path), {@code {dir}},
     * {@code {file_name}}, {@code {base}} (name without extension).
     */
    private String customCommand(String path) {
        String text = prefs.getCustomRunnersText();
        if (text == null || text.trim().isEmpty()) return null;
        String ext = extensionOf(path);
        if (ext.isEmpty()) return null;
        for (String line : text.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            int colon = trimmed.indexOf(':');
            if (colon <= 0) continue;
            String exts = trimmed.substring(0, colon).trim();
            String cmd = trimmed.substring(colon + 1).trim();
            if (cmd.isEmpty()) continue;
            for (String e : exts.split(",")) {
                if (e.trim().equalsIgnoreCase(ext)) return buildCustomCommand(cmd, path);
            }
        }
        return null;
    }

    private String buildCustomCommand(String cmd, String path) {
        File file = new File(path);
        String name = file.getName();
        String base = name;
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);
        String dir = file.getParent();
        if (dir == null) dir = ".";
        cmd =
                cmd.replace("{file}", path)
                        .replace("{dir}", dir)
                        .replace("{file_name}", name)
                        .replace("{base}", base);
        return "clear; cd \"" + dir + "\" && " + cmd;
    }

    private static String extensionOf(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0 || dot == path.length() - 1) return "";
        return path.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    // ==================== زبان‌ها ====================

    private String python(String path) {
        File file = new File(path);
        File dir = file.getParentFile();
        if (dir == null) dir = new File(".");

        String moduleName = file.getName();
        int dot = moduleName.lastIndexOf('.');
        if (dot != -1) moduleName = moduleName.substring(0, dot);

        List<String> packageParts = new ArrayList<>();
        packageParts.add(moduleName);

        File current = dir;
        File root = dir;
        while (current != null && new File(current, "__init__.py").exists()) {
            packageParts.add(0, current.getName());
            root = current.getParentFile();
            current = current.getParentFile();
        }

        String setup =
                "if ! command -v python3 >/dev/null 2>&1; then apt update && apt install python3 -y; fi; ";

        if (packageParts.size() > 1 && root != null) {
            StringBuilder dotted = new StringBuilder();
            for (int i = 0; i < packageParts.size(); i++) {
                if (i > 0) dotted.append('.');
                dotted.append(packageParts.get(i));
            }
            return "clear; "
                    + setup
                    + "cd \""
                    + root.getAbsolutePath()
                    + "\" && python3 -m "
                    + dotted;
        }

        return "clear; "
                + setup
                + "cd \""
                + dir.getAbsolutePath()
                + "\" && python3 \""
                + file.getName()
                + "\"";
    }

    private String php(String path) {
        File file = new File(path);
        File dir = file.getParentFile();
        if (dir == null) dir = new File(".");
        return "clear; "
                + "if ! command -v php >/dev/null 2>&1; then apt update && apt install php -y; fi; "
                + "cd \"" + dir.getAbsolutePath() + "\" && php \"" + file.getName() + "\"";
    }

    private String c(String path) {
        String exe = new File(path).getName();
        exe = exe.substring(0, exe.lastIndexOf('.'));

        return "clear; "
                + "if ! command -v gcc >/dev/null 2>&1; then apt update && apt install gcc -y; fi; "
                + "gcc \""
                + path
                + "\" -o \""
                + exe
                + "\" && ./"
                + exe;
    }

    private String cpp(String path) {
        String exe = new File(path).getName();
        exe = exe.substring(0, exe.lastIndexOf('.'));

        return "clear; "
                + "if ! command -v g++ >/dev/null 2>&1; then apt update && apt install g++ -y; fi; "
                + "g++ \""
                + path
                + "\" -o \""
                + exe
                + "\" && ./"
                + exe;
    }

    private String go(String path) {
        return "clear; "
                + "if ! command -v go >/dev/null 2>&1; then apt update && apt install golang-go -y; fi; "
                + "go run \""
                + path
                + "\"";
    }

    private String node(String path) {
        return "clear; "
                + "if ! command -v node >/dev/null 2>&1; then apt update && apt install nodejs npm -y; fi; "
                + "node \""
                + path
                + "\"";
    }

    private String typescript(String path) {
        return "clear; "
                + "if ! command -v ts-node >/dev/null 2>&1; then apt update && apt install nodejs npm -y && npm install -g typescript ts-node; fi; "
                + "ts-node \""
                + path
                + "\"";
    }

    private String lua(String path) {
        return "clear; "
                + "if ! command -v lua >/dev/null 2>&1; then apt update && apt install lua5.4 -y; fi; "
                + "lua \""
                + path
                + "\"";
    }

    private String java(String path) {
        File file = new File(path);
        File dir = file.getParentFile();
        if (dir == null) dir = new File(".");

        String androidHomeExport = androidHomeExport();

        File gradleWrapper = findGradleWrapper(dir);
        if (gradleWrapper != null) {
            File projectRoot = gradleWrapper.getParentFile();
            return "clear; "
                    + androidHomeExport
                    + "if [ ! -x \"" + gradleWrapper.getAbsolutePath() + "\" ]; then chmod +x \"" + gradleWrapper.getAbsolutePath() + "\"; fi; "
                    + "cd \"" + projectRoot.getAbsolutePath() + "\" && sh gradlew build";
        }

        File pomFile = findPomXml(dir);
        if (pomFile != null) {
            File projectRoot = pomFile.getParentFile();
            return "clear; "
                    + androidHomeExport
                    + "if ! command -v mvn >/dev/null 2>&1; then apt update && apt install maven -y; fi; "
                    + "cd \"" + projectRoot.getAbsolutePath() + "\" && mvn package";
        }

        File gradleFile = findGradleBuildFile(dir);
        if (gradleFile != null) {
            File projectRoot = gradleFile.getParentFile();
            return "clear; "
                    + androidHomeExport
                    + "if ! command -v gradle >/dev/null 2>&1; then apt update && apt install gradle -y; fi; "
                    + "cd \"" + projectRoot.getAbsolutePath() + "\" && gradle build";
        }

        // بدون build file: کامپایل با classpath واقعی (android.jar + کش گریدل + libs) و اجرا
        // (فقط اگه public static void main(String[]) داشته باشه؛ کلاس‌های اندرویدی مثل
        // Activity/Fragment رو نمی‌شه با java خالص اجرا کرد، فقط کامپایلشون می‌کنیم).
        String className = file.getName();
        int dot = className.lastIndexOf('.');
        if (dot != -1) className = className.substring(0, dot);

        String packageName = extractPackageName(file);
        String fqcn = (packageName != null && !packageName.isEmpty()) ? packageName + "." + className : className;

        File sourceRoot = resolvePackageRoot(dir, packageName);
        File outDir = new File(sourceRoot, ".ghostide_build");

        List<File> libJars = AndroidClasspathResolver.findLibraryJars(context, sourceRoot);
        String libClasspath = AndroidClasspathResolver.joinClasspath(libJars);
        String fullClasspath =
                outDir.getAbsolutePath() + (libClasspath.isEmpty() ? "" : ":" + libClasspath);

        String compileTargets;
        if (sourceRoot.equals(dir)) {
            compileTargets = "\"" + dir.getAbsolutePath() + "\"/*.java";
        } else {
            compileTargets = "\"" + path + "\"";
        }

        String compile =
                "mkdir -p \"" + outDir.getAbsolutePath() + "\" && javac -d \"" + outDir.getAbsolutePath()
                        + "\" -cp \"" + fullClasspath + "\" -sourcepath \"" + sourceRoot.getAbsolutePath()
                        + "\" " + compileTargets;

        String setup = "if ! command -v javac >/dev/null 2>&1; then apt update && apt install default-jdk -y; fi; ";

        if (hasMainMethod(file)) {
            return "clear; "
                    + setup
                    + compile
                    + " && java -cp \"" + fullClasspath + "\" " + fqcn;
        }

        return "clear; "
                + setup
                + compile
                + " && echo '✅ کامپایل شد. متد main پیدا نشد (کلاس اندرویدی/کتابخونه‌ای)، پس اجرا نمی‌شه.'";
    }

    /** export ANDROID_HOME/ANDROID_SDK_ROOT اگه SDK نصب‌شده پیدا بشه، وگرنه رشته‌ی خالی. */
    private String androidHomeExport() {
        File rootfs = DebianBootstrap.getRootfsDir(context);
        String sdkGuestPath = AndroidClasspathResolver.findAndroidSdkGuestPath(rootfs);
        if (sdkGuestPath == null) return "";
        return "export ANDROID_HOME=\"" + sdkGuestPath + "\" ANDROID_SDK_ROOT=\"" + sdkGuestPath + "\"; ";
    }

    private static final Pattern MAIN_METHOD_PATTERN =
            Pattern.compile("(?s).*\\bvoid\\s+main\\s*\\(\\s*String");

    private boolean hasMainMethod(File javaFile) {
        try {
            String content = new String(Files.readAllBytes(javaFile.toPath()));
            return MAIN_METHOD_PATTERN.matcher(content).matches();
        } catch (Exception e) {
            return false;
        }
    }

    private String kotlin(String path) {
        File file = new File(path);
        File dir = file.getParentFile();
        if (dir == null) dir = new File(".");

        String className = file.getName();
        int dot = className.lastIndexOf('.');
        if (dot != -1) className = className.substring(0, dot);

        String packageName = extractPackageName(file);
        File sourceRoot = resolvePackageRoot(dir, packageName);
        File outDir = new File(sourceRoot, ".ghostide_build");

        return "clear; "
                + "if ! command -v kotlinc >/dev/null 2>&1; then "
                + "  echo 'Kotlin compiler not found. Installing latest version...'; "
                + "  apt update && apt install wget unzip -y && "
                + "  wget https://github.com/JetBrains/kotlin/releases/download/v2.4.10/kotlin-compiler-2.4.10.zip && "
                + "  unzip kotlin-compiler-2.4.10.zip -d /opt/kotlin && "
                + "  ln -sf /opt/kotlin/kotlinc/bin/kotlinc /usr/bin/kotlinc && "
                + "  rm kotlin-compiler-2.4.10.zip; "
                + "fi; "
                + "mkdir -p \"" + outDir.getAbsolutePath() + "\" && "
                + "kotlinc \"" + path + "\" -include-runtime -d \"" + outDir.getAbsolutePath() + "/" + className + ".jar\" && "
                + "java -jar \"" + outDir.getAbsolutePath() + "/" + className + ".jar\"";
    }

    private String kotlinScript(String path) {
        return "clear; "
                + "if ! command -v kotlinc >/dev/null 2>&1; then "
                + "  echo 'Kotlin compiler not found. Installing latest version...'; "
                + "  apt update && apt install wget unzip -y && "
                + "  wget https://github.com/JetBrains/kotlin/releases/download/v2.4.10/kotlin-compiler-2.4.10.zip && "
                + "  unzip kotlin-compiler-2.4.10.zip -d /opt/kotlin && "
                + "  ln -sf /opt/kotlin/kotlinc/bin/kotlinc /usr/bin/kotlinc && "
                + "  rm kotlin-compiler-2.4.10.zip; "
                + "fi; "
                + "kotlinc -script \"" + path + "\"";
    }

    private File findGradleWrapper(File dir) {
        while (dir != null) {
            File wrapper = new File(dir, "gradlew");
            if (wrapper.exists()) return wrapper;
            dir = dir.getParentFile();
        }
        return null;
    }

    private File findPomXml(File dir) {
        while (dir != null) {
            File pom = new File(dir, "pom.xml");
            if (pom.exists()) return pom;
            dir = dir.getParentFile();
        }
        return null;
    }

    private File findGradleBuildFile(File dir) {
        while (dir != null) {
            File buildGradle = new File(dir, "build.gradle");
            File buildGradleKts = new File(dir, "build.gradle.kts");
            if (buildGradle.exists()) return buildGradle;
            if (buildGradleKts.exists()) return buildGradleKts;
            dir = dir.getParentFile();
        }
        return null;
    }

    private File resolvePackageRoot(File dir, String packageName) {
        if (packageName == null || packageName.isEmpty()) return dir;
        String[] segments = packageName.split("\\.");
        File current = dir;
        for (int i = segments.length - 1; i >= 0; i--) {
            if (current == null || !current.getName().equals(segments[i])) {
                return dir;
            }
            current = current.getParentFile();
        }
        return current != null ? current : dir;
    }

    private String extractPackageName(File javaFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(javaFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()
                        || trimmed.startsWith("//")
                        || trimmed.startsWith("/*")
                        || trimmed.startsWith("*")) {
                    continue;
                }
                if (trimmed.startsWith("package ")) {
                    String pkg = trimmed.substring("package ".length()).trim();
                    if (pkg.endsWith(";")) pkg = pkg.substring(0, pkg.length() - 1).trim();
                    return pkg;
                }
                break;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private String sass(String path) {
        String fileName = new File(path).getName();
        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
        String ext = path.endsWith(".scss") ? "scss" : "sass";
        String outputCss = baseName + ".css";
        String dir = new File(path).getParent();
        if (dir == null) dir = ".";

        return "clear; "
                + "echo '=== Compiling Sass to CSS ==='; "
                + "if ! command -v sass >/dev/null 2>&1; then "
                + "  echo 'Sass not found. Installing...'; "
                + "  apt update && apt install nodejs npm -y && npm install -g sass; "
                + "fi; "
                + "sass \"" + path + "\" \"" + dir + "/" + outputCss + "\" --style=expanded --no-source-map; "
                + "if [ $? -eq 0 ]; then "
                + "  echo ''; "
                + "  echo '✅ Compilation successful!'; "
                + "  echo '📄 Output: " + outputCss + "'; "
                + "  echo ''; "
                + "  echo '--- Generated CSS ---'; "
                + "  cat \"" + dir + "/" + outputCss + "\"; "
                + "else "
                + "  echo '❌ Compilation failed!'; "
                + "fi";
    }
}
