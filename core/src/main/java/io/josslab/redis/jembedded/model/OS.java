package io.josslab.redis.jembedded.model;

import io.josslab.redis.jembedded.error.UnknownOsName;

import java.util.function.Supplier;

public enum OS {
  WINDOWS(Architecture::detectWindowsArchitecture),
  UNIX(Architecture::detectUnixMacOSXArchitecture),
  MAC_OS_X(Architecture::detectUnixMacOSXArchitecture);

  private final Supplier<Architecture> archSupplier;

  OS(final Supplier<Architecture> archSupplier) {
    this.archSupplier = archSupplier;
  }

  public Architecture detectArchitecture() {
    return archSupplier.get();
  }

  public static OS detectOS() {
    final String osName = System.getProperty("os.name").toLowerCase();

    if (osName.contains("win"))
      return OS.WINDOWS;
    if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))
      return OS.UNIX;
    if ("Mac OS X".equalsIgnoreCase(osName))
      return OS.MAC_OS_X;

    throw new UnknownOsName(osName);
  }

}
