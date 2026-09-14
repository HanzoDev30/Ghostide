package ir.hanzodev1375.ghostide.shizuku;

import ir.hanzodev1375.ghostide.interfaces.IShellUserService;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Runs inside a separate process with shell (uid 2000) or root (uid 0) identity,
 * granted by Shizuku/Sui. Not a normal app process: avoid Context APIs here.
 */
public class ShellUserService implements IShellUserService {

  // Required empty constructor.
  public ShellUserService() {} 

  @Override 
  public String exec(String[] command) {
    StringBuilder output = new StringBuilder();
    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        String line;
        while ((line = reader.readLine()) != null) {
          output.append(line).append('\n');
        }
      }
      process.waitFor();
    } catch (Exception e) {
      output.append("error: ").append(e.getMessage());
    }
    return output.toString();
  }

  @Override
  public void destroy() {
    System.exit(0);
  }
}