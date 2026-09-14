package ir.hanzodev1375.ghostide.project;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ProjectCreator {

  public enum ProjectType {
    HTML,
    NODEJS,
    JAVA,
    FLUTTER,
    PYTHON,
    PYTHON_C,
    PHP,
    C,
    CPP,
    RUBY,
    ANDROIDMODULE
  }

  public interface OnCreateResult {
    void onSuccess(String projectPath);

    void onError(String message);
  }

  private final Context context;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());

  public ProjectCreator(Context context) {
    this.context = context;
  }

  public void create(
      ProjectType type,
      String projectName,
      String packageName,
      String parentPath,
      OnCreateResult callback) {
    new Thread(
            () -> {
              try {
                String result = buildProject(type, projectName, packageName, parentPath);
                mainHandler.post(() -> callback.onSuccess(result));
              } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
              }
            })
        .start();
  }

  private String buildProject(
      ProjectType type, String projectName, String packageName, String parentPath)
      throws IOException {

    if (!isValidProjectName(projectName)) {
      throw new IOException("Invalid project name: \"" + projectName + "\"");
    }

    File projectDir = new File(parentPath, projectName);
    if (projectDir.exists()) {
      throw new IOException("A folder named \"" + projectName + "\" already exists here.");
    }
    if (!projectDir.mkdirs()) {
      throw new IOException("Failed to create project directory. Check storage permissions.");
    }

    switch (type) {
      case HTML:
        buildHtml(projectDir, projectName);
        break;
      case NODEJS:
        buildNodejs(projectDir, projectName);
        break;
      case JAVA:
        buildJava(projectDir, projectName, packageName);
        break;
      case FLUTTER:
        buildFlutter(projectDir, projectName, packageName);
        break;
      case PYTHON:
        buildPython(projectDir, projectName);
        break;
      case PYTHON_C:
        buildPythonC(projectDir, projectName);
        break;
      case PHP:
        buildPhp(projectDir, projectName);
        break;
      case C:
        buildC(projectDir, projectName);
        break;
      case CPP:
        buildCpp(projectDir, projectName);
        break;
      case RUBY:
        buildRuby(projectDir, projectName);
        break;

    }

    return projectDir.getAbsolutePath();
  }

  // ─── existing builders ────────────────────────────────────────────────

  private void buildHtml(File root, String name) throws IOException {
    File cssDir = new File(root, "css");
    File jsDir = new File(root, "js");
    cssDir.mkdirs();
    jsDir.mkdirs();
    writeTemplate("templates/html/index.html", new File(root, "index.html"), name, null);
    writeTemplate("templates/html/style.css", new File(cssDir, "style.css"), name, null);
    writeTemplate("templates/html/script.js", new File(jsDir, "script.js"), name, null);
  }

  private void buildNodejs(File root, String name) throws IOException {
    writeTemplate("templates/nodejs/package.json", new File(root, "package.json"), name, null);
    writeTemplate("templates/nodejs/index.js", new File(root, "index.js"), name, null);
    writeTemplate("templates/nodejs/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildJava(File root, String name, String packageName) throws IOException {
    String pkgPath = packageName.replace('.', '/');
    File srcDir = new File(root, "src/main/java/" + pkgPath);
    srcDir.mkdirs();
    writeTemplate("templates/java/Main.java", new File(srcDir, "Main.java"), name, packageName);
    writeTemplate("templates/java/gitignore", new File(root, ".gitignore"), name, packageName);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  // ─── new builders ─────────────────────────────────────────────────────

  private void buildFlutter(File root, String name, String packageName) throws IOException {
    String pkgPath =
        packageName != null && !packageName.isEmpty()
            ? packageName.replace('.', '/')
            : name.toLowerCase().replaceAll("[^a-z0-9]", "_");
    if (packageName == null || packageName.isEmpty()) {
      packageName = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    // root files
    writeTemplate(
        "templates/flutter/pubspec.yaml", new File(root, "pubspec.yaml"), name, packageName);
    writeTemplate(
        "templates/flutter/analysis_options.yaml",
        new File(root, "analysis_options.yaml"),
        name,
        packageName);
    writeTemplate("templates/flutter/gitignore", new File(root, ".gitignore"), name, packageName);
    writeRaw("# " + name + "\n", new File(root, "README.md"));

    // lib/
    File libDir = new File(root, "lib");
    libDir.mkdirs();
    writeTemplate(
        "templates/flutter/lib/main.dart", new File(libDir, "main.dart"), name, packageName);

    // test/
    File testDir = new File(root, "test");
    testDir.mkdirs();
    writeTemplate(
        "templates/flutter/test/widget_test.dart",
        new File(testDir, "widget_test.dart"),
        name,
        packageName);

    // android/
    File ktDir = new File(root, "android/app/src/main/kotlin/" + pkgPath);
    ktDir.mkdirs();
    new File(root, "android/app/src/main/res/drawable").mkdirs();
    new File(root, "android/app/src/main/res/values").mkdirs();
    new File(root, "android/gradle/wrapper").mkdirs();

    writeTemplate(
        "templates/flutter/android/build.gradle",
        new File(root, "android/build.gradle"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/android/settings.gradle",
        new File(root, "android/settings.gradle"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/android/gradle.properties",
        new File(root, "android/gradle.properties"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/android/local.properties",
        new File(root, "android/local.properties"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/android/gradle/wrapper/gradle-wrapper.properties",
        new File(root, "android/gradle/wrapper/gradle-wrapper.properties"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/android/app/build.gradle",
        new File(root, "android/app/build.gradle"),
        name,
        packageName);
    writeTemplateWithPkgPath(
        "templates/flutter/android/app/src/main/AndroidManifest.xml",
        new File(root, "android/app/src/main/AndroidManifest.xml"),
        name,
        packageName,
        pkgPath);
    writeTemplateWithPkgPath(
        "templates/flutter/android/app/src/main/kotlin/MainActivity.kt",
        new File(ktDir, "MainActivity.kt"),
        name,
        packageName,
        pkgPath);
    writeTemplate(
        "templates/flutter/android/app/src/main/res/drawable/launch_background.xml",
        new File(root, "android/app/src/main/res/drawable/launch_background.xml"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/android/app/src/main/res/values/styles.xml",
        new File(root, "android/app/src/main/res/values/styles.xml"),
        name,
        packageName);

    // ios/
    new File(root, "ios/Runner/Assets.xcassets/AppIcon.appiconset").mkdirs();
    new File(root, "ios/Runner.xcodeproj").mkdirs();
    new File(root, "ios/Runner.xcworkspace").mkdirs();

    writeTemplate(
        "templates/flutter/ios/Runner/Info.plist",
        new File(root, "ios/Runner/Info.plist"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/ios/Runner/AppDelegate.swift",
        new File(root, "ios/Runner/AppDelegate.swift"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/ios/Runner/Assets.xcassets/AppIcon.appiconset/Contents.json",
        new File(root, "ios/Runner/Assets.xcassets/AppIcon.appiconset/Contents.json"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/ios/Runner.xcodeproj/project.pbxproj",
        new File(root, "ios/Runner.xcodeproj/project.pbxproj"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/ios/Runner.xcworkspace/contents.xcworkspacedata",
        new File(root, "ios/Runner.xcworkspace/contents.xcworkspacedata"),
        name,
        packageName);
    writeTemplate(
        "templates/flutter/ios/Podfile", new File(root, "ios/Podfile"), name, packageName);

    // web/
    new File(root, "web/icons").mkdirs();
    writeTemplate(
        "templates/flutter/web/index.html", new File(root, "web/index.html"), name, packageName);
    writeTemplate(
        "templates/flutter/web/manifest.json",
        new File(root, "web/manifest.json"),
        name,
        packageName);

    // linux/
    new File(root, "linux").mkdirs();
    writeTemplate(
        "templates/flutter/linux/CMakeLists.txt",
        new File(root, "linux/CMakeLists.txt"),
        name,
        packageName);
  }

  private void buildPython(File root, String name) throws IOException {
    writeTemplate("templates/python/main.py", new File(root, "main.py"), name, null);
    writeTemplate(
        "templates/python/requirements.txt", new File(root, "requirements.txt"), name, null);
    writeTemplate("templates/python/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildPythonC(File root, String name) throws IOException {
    writeTemplate("templates/python_c/main.py", new File(root, "main.py"), name, null);
    writeTemplate("templates/python_c/lib.c", new File(root, "lib.c"), name, null);
    writeTemplate("templates/python_c/lib.h", new File(root, "lib.h"), name, null);
    writeTemplate("templates/python_c/Makefile", new File(root, "Makefile"), name, null);
    writeTemplate("templates/python_c/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildPhp(File root, String name) throws IOException {
    writeTemplate("templates/php/index.php", new File(root, "index.php"), name, null);
    writeTemplate("templates/php/composer.json", new File(root, "composer.json"), name, null);
    writeTemplate("templates/php/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildC(File root, String name) throws IOException {
    File srcDir = new File(root, "src");
    srcDir.mkdirs();
    writeTemplate("templates/c/main.c", new File(srcDir, "main.c"), name, null);
    writeTemplate("templates/c/Makefile", new File(root, "Makefile"), name, null);
    writeTemplate("templates/c/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildCpp(File root, String name) throws IOException {
    File srcDir = new File(root, "src");
    srcDir.mkdirs();
    writeTemplate("templates/cpp/main.cpp", new File(srcDir, "main.cpp"), name, null);
    writeTemplate("templates/cpp/CMakeLists.txt", new File(root, "CMakeLists.txt"), name, null);
    writeTemplate("templates/cpp/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  private void buildRuby(File root, String name) throws IOException {
    writeTemplate("templates/ruby/main.rb", new File(root, "main.rb"), name, null);
    writeTemplate("templates/ruby/Gemfile", new File(root, "Gemfile"), name, null);
    writeTemplate("templates/ruby/gitignore", new File(root, ".gitignore"), name, null);
    writeRaw("# " + name + "\n", new File(root, "README.md"));
  }

  // ─── helpers ──────────────────────────────────────────────────────────

  private boolean isValidProjectName(String name) {
    if (name == null || name.isEmpty()) return false;
    if (name.contains("/") || name.contains("\\") || name.contains("..")) return false;
    if (!Character.isLetterOrDigit(name.charAt(0))) return false;
    return name.matches("[\\w\\- .]+");
  }

  private void writeTemplate(String assetPath, File dest, String projectName, String packageName)
      throws IOException {
    writeTemplateWithPkgPath(assetPath, dest, projectName, packageName, null);
  }

  private void writeTemplateWithPkgPath(
      String assetPath, File dest, String projectName, String packageName, String pkgPath)
      throws IOException {
    InputStream is = context.getAssets().open(assetPath);
    byte[] bytes = readAllBytesCompat(is);
    is.close();

    String content = new String(bytes, StandardCharsets.UTF_8);
    content = content.replace("{{PROJECT_NAME}}", projectName);
    content =
        content.replace(
            "{{PROJECT_NAME_LOWER}}", projectName.toLowerCase().replaceAll("[^a-z0-9\\-]", "-"));
    if (packageName != null) {
      content = content.replace("{{PACKAGE_NAME}}", packageName);
    }
    if (pkgPath != null) {
      content = content.replace("{{PACKAGE_PATH}}", pkgPath);
    }

    writeRaw(content, dest);
  }

  private void writeRaw(String content, File dest) throws IOException {
    dest.getParentFile().mkdirs();
    try (FileOutputStream fos = new FileOutputStream(dest)) {
      fos.write(content.getBytes(StandardCharsets.UTF_8));
    }
  }
  
  /* for old api Android 8,9,10,11,12 ect*/
  private static byte[] readAllBytesCompat(InputStream is) throws IOException {
    var buffer = new ByteArrayOutputStream();
    byte[] chunk = new byte[4096];
    int bytesRead;
    while ((bytesRead = is.read(chunk)) != -1) {
        buffer.write(chunk, 0, bytesRead);
    }
    return buffer.toByteArray();
}
}
