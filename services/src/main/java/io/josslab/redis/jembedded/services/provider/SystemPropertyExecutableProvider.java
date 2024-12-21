package io.josslab.redis.jembedded.services.provider;

import io.josslab.redis.jembedded.services.DefaultExecutableProperty;
import io.josslab.redis.jembedded.services.ExecutableProperty;

import java.io.File;

import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectArchitecture;
import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectOS;

public class SystemPropertyExecutableProvider extends AbstractExecutableProvider {

  public static final String DEFAULT = "redis.jembedded.executable";

  private final String propertyName;
  private final ExecutableProperty executableProperty;

  public SystemPropertyExecutableProvider() {
    this(DEFAULT);
  }

  public SystemPropertyExecutableProvider(String propertyName) {
    this.propertyName = propertyName;
    this.executableProperty = new DefaultExecutableProperty()
      .setOs(detectOS())
      .setArch(detectArchitecture())
      .setJarVersion("unknown")
      // TODO: run command redis-server --version to detect redis version
      .setBinaryVersion("unknown");
  }

  @Override
  public File doGetExecutable() {
    return new File(System.getProperty(propertyName));
  }

  @Override
  public ExecutableProperty getProperty() {
    return executableProperty;
  }
}
