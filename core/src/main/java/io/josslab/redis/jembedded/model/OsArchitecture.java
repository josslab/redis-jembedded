package io.josslab.redis.jembedded.model;

import static io.josslab.redis.jembedded.model.Architecture.*;
import static io.josslab.redis.jembedded.model.OS.*;

public class OsArchitecture {

  public static final OsArchitecture WINDOWS_x86 = new OsArchitecture(WINDOWS, x86);
  public static final OsArchitecture WINDOWS_x86_64 = new OsArchitecture(WINDOWS, x86_64);
  public static final OsArchitecture UNIX_x86 = new OsArchitecture(UNIX, x86);
  public static final OsArchitecture UNIX_x86_64 = new OsArchitecture(UNIX, x86_64);
  public static final OsArchitecture MAC_OS_X_x86_64 = new OsArchitecture(MAC_OS_X, x86_64);
  public static final OsArchitecture MAC_OS_X_ARM64 = new OsArchitecture(MAC_OS_X, aarch64);

  public final OS os;
  public final Architecture arch;

  public static OsArchitecture detectOSandArchitecture() {
    final OS os = OS.detectOS();
    final Architecture arch = os.detectArchitecture();
    return new OsArchitecture(os, arch);
  }

  public OsArchitecture(final OS os, final Architecture arch) {
    this.os = os;
    this.arch = arch;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final OsArchitecture that = (OsArchitecture) o;
    return arch == that.arch && os == that.os;
  }

  @Override
  public int hashCode() {
    int result = os.hashCode();
    result = 31 * result + arch.hashCode();
    return result;
  }

}
