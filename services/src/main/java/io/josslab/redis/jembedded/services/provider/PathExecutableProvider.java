package io.josslab.redis.jembedded.services.provider;

import io.josslab.redis.jembedded.services.DefaultExecutableProperty;
import io.josslab.redis.jembedded.services.ExecutableProperty;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectArchitecture;
import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectOS;

public class PathExecutableProvider extends AbstractExecutableProvider {

  public static final String DEFAULT = "redis";

  private final String executableName;
  private final ExecutableProperty executableProperty;

  public PathExecutableProvider() {
    this(DEFAULT);
  }

  public PathExecutableProvider(String executableName) {
    this.executableName = executableName;
    this.executableProperty = new DefaultExecutableProperty()
      .setOs(detectOS())
      .setArch(detectArchitecture())
      .setJarVersion("unknown")
      // TODO: run command redis-server --version to detect redis version
      .setBinaryVersion("unknown");
  }

  @Override
  protected File doGetExecutable() {
    return findBinaryInPath(executableName);
  }

  @Override
  public ExecutableProperty getProperty() {
    return executableProperty;
  }

  private static File findBinaryInPath(final String name) {
    String pathVar = System.getenv("PATH");
    final Optional<Path> location = Stream.of(pathVar.split(Pattern.quote(File.pathSeparator)))
      .map(Paths::get)
      .map(path -> path.resolve(name))
      .filter(Files::isRegularFile)
      .findAny();
    if (!location.isPresent()) {
      throw new RuntimeException("Could not find binary '" + name + "' in PATH");
    }
    return location.get().toFile();
  }
}
