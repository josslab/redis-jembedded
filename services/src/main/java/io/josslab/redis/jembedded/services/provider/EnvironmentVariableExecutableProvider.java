package io.josslab.redis.jembedded.services.provider;

import io.josslab.redis.jembedded.services.DefaultExecutableProperty;
import io.josslab.redis.jembedded.services.ExecutableProperty;

import java.io.File;

import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectArchitecture;
import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectOS;

public class EnvironmentVariableExecutableProvider extends AbstractExecutableProvider {

  public static final String DEFAULT = "REDIS_JEMBEDDED_EXECUTABLE";

  private final String envVariableName;
  private final ExecutableProperty executableProperty;

  public EnvironmentVariableExecutableProvider() {
    this(DEFAULT);
  }

  public EnvironmentVariableExecutableProvider(String envVariableName) {
    super();
    this.envVariableName = envVariableName;
    this.executableProperty = new DefaultExecutableProperty()
      .setOs(detectOS())
      .setArch(detectArchitecture())
      .setJarVersion("unknown")
      // TODO: run command redis-server --version to detect redis version
      .setBinaryVersion("unknown");
  }

  @Override
  protected File doGetExecutable() {
    return new File(System.getenv(envVariableName));
  }

  @Override
  public ExecutableProperty getProperty() {
    return executableProperty;
  }
}
