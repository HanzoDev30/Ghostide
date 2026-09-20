package ir.hanzodev1375.ghostide.refactor.renameclass;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class ClassScanResult {

  private final File targetFile;
  private final boolean kotlin;
  private final String packageName;
  private final String oldClassName;
  private final List<ClassFileTarget> targets;
  private final List<File> ambiguousFiles;

  public ClassScanResult(
      File targetFile,
      boolean kotlin,
      String packageName,
      String oldClassName,
      List<ClassFileTarget> targets,
      List<File> ambiguousFiles) {
    this.targetFile = targetFile;
    this.kotlin = kotlin;
    this.packageName = packageName;
    this.oldClassName = oldClassName;
    this.targets = Collections.unmodifiableList(targets);
    this.ambiguousFiles = Collections.unmodifiableList(ambiguousFiles);
  }

  public File getTargetFile() {
    return targetFile;
  }

  public boolean isKotlin() {
    return kotlin;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getOldClassName() {
    return oldClassName;
  }

  public List<ClassFileTarget> getTargets() {
    return targets;
  }

  public List<File> getAmbiguousFiles() {
    return ambiguousFiles;
  }
}
