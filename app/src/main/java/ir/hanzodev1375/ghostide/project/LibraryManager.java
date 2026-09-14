package ir.hanzodev1375.ghostide.project;

import com.blankj.utilcode.util.FileIOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LibraryManager {

  public static class Library {
    public final String alias;
    public final String group;
    public final String artifact;
    public final String version;

    public Library(String alias, String group, String artifact, String version) {
      this.alias = sanitizeAlias(alias);
      this.group = group;
      this.artifact = artifact;
      this.version = version;
    }

    public String mavenCoord() {
      return group + ":" + artifact + ":" + version;
    }

    public String versionKey() {
      return alias.replace('.', '-');
    }

    public String libsAccessor() {
      return "libs." + alias.replace('-', '.');
    }

    private static String sanitizeAlias(String raw) {

      return raw.replaceAll("[^a-zA-Z0-9\\-]", "-").toLowerCase();
    }
  }

  public static void addLibraries(
      File projectRoot, File moduleDir, boolean useKts, List<Library> libraries)
      throws IOException {

    if (libraries == null || libraries.isEmpty()) return;

    File tomlFile = new File(projectRoot, "gradle/libs.versions.toml");
    boolean hasToml = tomlFile.exists();

    File buildFile = new File(moduleDir, useKts ? "build.gradle.kts" : "build.gradle");
    if (!buildFile.exists()) {

      buildFile = new File(moduleDir, useKts ? "build.gradle" : "build.gradle.kts");
      if (!buildFile.exists()) {
        throw new IOException("build.gradle not found in module: " + moduleDir.getAbsolutePath());
      }
    }
    boolean isKts = buildFile.getName().endsWith(".kts");

    if (hasToml) {
      addLibrariesToToml(tomlFile, libraries);
      addTomlDepsToGradle(buildFile, isKts, libraries);
    } else {
      addLiteralDepsToGradle(buildFile, isKts, libraries);
    }
  }


  static void addLibrariesToToml(File tomlFile, List<Library> libraries) throws IOException {
    String content = readFile(tomlFile);

    for (Library lib : libraries) {
      content = addVersionToToml(content, lib);
      content = addLibraryToToml(content, lib);
    }

    writeFile(tomlFile, content);
  }

  private static String addVersionToToml(String content, Library lib) {

    Pattern existCheck = Pattern.compile("(?m)^\\s*" + Pattern.quote(lib.versionKey()) + "\\s*=");
    if (existCheck.matcher(content).find()) return content;

    String newEntry = lib.versionKey() + " = \"" + lib.version + "\"";
    return insertIntoTomlSection(content, "[versions]", newEntry);
  }

  private static String addLibraryToToml(String content, Library lib) {

    Pattern existCheck = Pattern.compile("(?m)^\\s*" + Pattern.quote(lib.alias) + "\\s*=");
    if (existCheck.matcher(content).find()) return content;

    String newEntry =
        lib.alias
            + " = { group = \""
            + lib.group
            + "\", name = \""
            + lib.artifact
            + "\", version.ref = \""
            + lib.versionKey()
            + "\" }";
    return insertIntoTomlSection(content, "[libraries]", newEntry);
  }

  private static String insertIntoTomlSection(
      String content, String sectionHeader, String newLine) {

    Pattern sectionPat = Pattern.compile("(?m)^\\s*" + Pattern.quote(sectionHeader) + "\\s*$");
    Matcher secMatcher = sectionPat.matcher(content);

    if (!secMatcher.find()) {

      return content.stripTrailing() + "\n\n" + sectionHeader + "\n" + newLine + "\n";
    }

    int sectionStart = secMatcher.end();

    Pattern nextSectionPat = Pattern.compile("(?m)^\\s*\\[");
    Matcher nextMatcher = nextSectionPat.matcher(content);
    nextMatcher.region(sectionStart, content.length());

    int insertPos;
    if (nextMatcher.find()) {

      String sectionBody = content.substring(sectionStart, nextMatcher.start());
      insertPos = sectionStart + lastNonEmptyLineEnd(sectionBody);
    } else {
      insertPos = content.length();
    }

    return content.substring(0, insertPos) + "\n" + newLine + content.substring(insertPos);
  }

  private static int lastNonEmptyLineEnd(String block) {
    Pattern p = Pattern.compile("(?m)^[^\\n]+$");
    Matcher m = p.matcher(block);
    int last = block.length();
    while (m.find()) last = m.end();
    return last;
  }

  private static void addTomlDepsToGradle(File buildFile, boolean isKts, List<Library> libraries)
      throws IOException {

    String content = readFile(buildFile);
    List<String> toAdd = new ArrayList<>();

    for (Library lib : libraries) {
      String accessor = lib.libsAccessor();

      if (!content.contains(accessor)) {
        if (isKts) {
          toAdd.add("    implementation(" + accessor + ")");
        } else {
          toAdd.add("    implementation " + accessor);
        }
      }
    }

    if (!toAdd.isEmpty()) {
      content = insertIntoDependencies(content, toAdd);
      writeFile(buildFile, content);
    }
  }

  private static void addLiteralDepsToGradle(File buildFile, boolean isKts, List<Library> libraries)
      throws IOException {

    String content = readFile(buildFile);
    List<String> toAdd = new ArrayList<>();

    for (Library lib : libraries) {
      String coord = lib.mavenCoord();
      if (!content.contains(lib.group + ":" + lib.artifact)) {
        if (isKts) {
          toAdd.add("    implementation(\"" + coord + "\")");
        } else {
          toAdd.add("    implementation '" + coord + "'");
        }
      }
    }

    if (!toAdd.isEmpty()) {
      content = insertIntoDependencies(content, toAdd);
      writeFile(buildFile, content);
    }
  }

  private static String insertIntoDependencies(String content, List<String> lines) {
    String toInsert = String.join("\n", lines);

    Pattern startPat = Pattern.compile("(?m)^dependencies\\s*\\{");
    Matcher startMatcher = startPat.matcher(content);

    if (!startMatcher.find()) {

      return content.stripTrailing() + "\n\ndependencies {\n" + toInsert + "\n}\n";
    }

    int blockStart = startMatcher.end();
    int depth = 1;
    int i = blockStart;
    while (i < content.length() && depth > 0) {
      char c = content.charAt(i);
      if (c == '{') depth++;
      else if (c == '}') depth--;
      i++;
    }
    int closingBrace = i - 1;

    return content.substring(0, closingBrace) + toInsert + "\n" + content.substring(closingBrace);
  }

  private static String readFile(File f) {
    return FileIOUtils.readFile2String(f);
  }

  private static void writeFile(File f, String content) throws IOException {
    f.getParentFile().mkdirs();
    try (FileOutputStream fos = new FileOutputStream(f)) {
      fos.write(content.getBytes(StandardCharsets.UTF_8));
    }
  }
}
