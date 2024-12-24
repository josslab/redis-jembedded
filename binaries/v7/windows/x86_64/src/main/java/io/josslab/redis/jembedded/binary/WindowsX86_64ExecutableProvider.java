package io.josslab.redis.jembedded.binary;

import io.josslab.redis.jembedded.services.DefaultExecutableProperty;
import io.josslab.redis.jembedded.services.EnvironmentUtils;
import io.josslab.redis.jembedded.services.ExecutableProperty;
import io.josslab.redis.jembedded.services.Immutables;
import io.josslab.redis.jembedded.services.provider.AbstractExecutableProvider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.josslab.redis.jembedded.services.Arch.X86_64;
import static io.josslab.redis.jembedded.services.EnvironmentUtils.readPropertiesFromResource;
import static io.josslab.redis.jembedded.services.OS.WINDOWS;

public class WindowsX86_64ExecutableProvider extends AbstractExecutableProvider {

  static final ExecutableProperty EXECUTABLE_PROPERTY;
  private static final Set<String> BINARY_DLL_FILES;

  static {
    BINARY_DLL_FILES = Immutables.unmodifiableCollection(
      HashSet::new,
      "msys-2.0.dll",
      "msys-crypto-3.dll",
      "msys-ssl-3.dll"
    );

    Map<String, String> properties = readPropertiesFromResource(WindowsX86_64ExecutableProvider.class);
    EXECUTABLE_PROPERTY = new DefaultExecutableProperty()
      .setOs(WINDOWS)
      .setArch(X86_64)
      .setJarVersion(properties.get("binary.jar-version"))
      .setBinaryVersion(properties.get("binary.version"));
  }

  @Override
  protected File doGetExecutable() {
    File tempDir = EnvironmentUtils.newTempDirForBinary("redis-jembedded-windows-x86_64-" + EXECUTABLE_PROPERTY.jarVersion());
    try {
      writeBinaryDll(tempDir);
      return EnvironmentUtils.writeResourceToExecutableFile(WindowsX86_64ExecutableProvider.class, tempDir, "redis-server", "redis-server-" + EXECUTABLE_PROPERTY.binaryVersion() + ".exe");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public ExecutableProperty getProperty() {
    return EXECUTABLE_PROPERTY;
  }

  @Override
  protected boolean addDeleteHook() {
    return true;
  }

  private void writeBinaryDll(File tempDir) throws IOException {
    for (String binaryDllFile : BINARY_DLL_FILES) {
      EnvironmentUtils.writeResourceToExecutableFile(this.getClass(), tempDir, binaryDllFile, null);
    }
  }
}
