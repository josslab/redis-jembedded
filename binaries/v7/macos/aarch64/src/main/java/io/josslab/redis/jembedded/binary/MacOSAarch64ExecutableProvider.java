package io.josslab.redis.jembedded.binary;

import io.josslab.redis.jembedded.services.DefaultExecutableProperty;
import io.josslab.redis.jembedded.services.EnvironmentUtils;
import io.josslab.redis.jembedded.services.ExecutableProperty;
import io.josslab.redis.jembedded.services.provider.AbstractExecutableProvider;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import static io.josslab.redis.jembedded.services.Arch.AARCH_64;
import static io.josslab.redis.jembedded.services.EnvironmentUtils.readPropertiesFromResource;
import static io.josslab.redis.jembedded.services.OS.MAC_OS_X;

public class MacOSAarch64ExecutableProvider extends AbstractExecutableProvider {

  static final ExecutableProperty EXECUTABLE_PROPERTY;

  static {
    Map<String, String> properties = readPropertiesFromResource(MacOSAarch64ExecutableProvider.class);
    EXECUTABLE_PROPERTY = new DefaultExecutableProperty()
      .setOs(MAC_OS_X)
      .setArch(AARCH_64)
      .setJarVersion(properties.get("binary.jar-version"))
      .setBinaryVersion(properties.get("binary.version"));
  }

  @Override
  protected File doGetExecutable() {
    File tempDir = EnvironmentUtils.newTempDirForBinary("redis-jembedded-macos-aarch64-" + EXECUTABLE_PROPERTY.jarVersion());
    try {
      return EnvironmentUtils.writeResourceToExecutableFile(MacOSAarch64ExecutableProvider.class, tempDir, "redis-server", "redis-server-" + EXECUTABLE_PROPERTY.binaryVersion());
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
}
