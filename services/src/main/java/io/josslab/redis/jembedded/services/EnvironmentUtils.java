package io.josslab.redis.jembedded.services;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static io.josslab.redis.jembedded.services.Arch.*;
import static io.josslab.redis.jembedded.services.OS.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public final class EnvironmentUtils {

  private EnvironmentUtils() {
    // ignored
  }

  public static OS detectOS() {
    final String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains("win"))
      return WINDOWS;
    if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))
      return UNIX;
    if ("Mac OS X".equalsIgnoreCase(osName))
      return MAC_OS_X;

    throw new UnknownOSName(osName);
  }

  public static Arch detectArchitecture() {
    OS detectedOS = detectOS();
    Arch arch;
    if (Objects.requireNonNull(detectedOS) == WINDOWS) {
      arch = detectWindowsArchitecture();
    } else {
      arch = detectUnixMacOSXArchitecture();
    }
    return arch;
  }

  public static Arch detectWindowsArchitecture() {
    final String arch = System.getenv("PROCESSOR_ARCHITECTURE");
    final String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
    return isWindows64Bit(arch, wow64Arch) ? X86_64 : X86;
  }

  public static Arch detectUnixMacOSXArchitecture() {
    try (final Stream<String> lines = processToLines("uname -m")) {
      return lines
        .filter(line -> line.contains("64"))
        .map(line -> line.contains("aarch64") || line.contains("arm64") ? AARCH_64 : X86_64)
        .findFirst().orElse(X86);
    } catch (IOException e) {
      throw new OsArchitectureNotFound(e);
    }
  }

  private static boolean isWindows64Bit(final String arch, final String wow64Arch) {
    return arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64");
  }

  public static Stream<String> processToLines(final String command) throws IOException {
    final Process proc = Runtime.getRuntime().exec(command);
    return new BufferedReader(new InputStreamReader(proc.getInputStream())).lines();
  }

  public static File newTempDirForBinary(String folderName) {
    String tempDir = System.getProperty("java.io.tmpdir");
    final File tempDirectory = new File(tempDir + File.separator + folderName);
    if (!tempDirectory.exists()) {
      tempDirectory.mkdirs();
    }
    return tempDirectory;
  }

  public static synchronized File writeResourceToExecutableFile(Class<?> clazz, final File tempDirectory, final String resourcePath, String newName) throws IOException {
    final File executable = new File(tempDirectory, newName == null ? resourcePath : newName);
    if (executable.exists()) {
      return executable;
    }
    try (final InputStream in = clazz.getClassLoader().getResourceAsStream(resourcePath)) {
      if (in == null) {
        throw new FileNotFoundException("Could not find Redis executable at " + resourcePath);
      }
      Files.copy(in, executable.toPath(), REPLACE_EXISTING);
    }
    if (!executable.setExecutable(true)) {
      throw new IOException("Failed to set executable permission for binary " + resourcePath + " at temporary location " + executable);
    }
    return executable;
  }

  public static Map<String, String> readPropertiesFromResource(Class<?> clazz) {
    Map<String, String> properties = new HashMap<>();
    try (InputStream in = clazz.getClassLoader().getResourceAsStream("binary.properties")) {
      if (in == null) {
        throw new NullPointerException("Could not find binary properties from place of class " + clazz.getName());
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#")) {
          continue;
        }
        int equalSign = line.indexOf('=');
        properties.put(line.substring(0, equalSign).trim(), line.substring(equalSign + 1).trim());
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return properties;
  }
}
