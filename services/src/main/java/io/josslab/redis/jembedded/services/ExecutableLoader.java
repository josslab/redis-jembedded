package io.josslab.redis.jembedded.services;

import java.util.Iterator;
import java.util.ServiceLoader;

import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectArchitecture;
import static io.josslab.redis.jembedded.services.EnvironmentUtils.detectOS;

public class ExecutableLoader {

  private static ExecutableProvider provider;

  public static ExecutableProvider load() {
    if (provider != null) {
      return provider;
    }
    ServiceLoader<ExecutableProvider> serviceLoader = ServiceLoader.load(ExecutableProvider.class);
    Iterator<ExecutableProvider> iterator = serviceLoader.iterator();
    if (!iterator.hasNext()) {
      throw new IllegalStateException("No provider found");
    }
    ExecutableProvider executableProvider = iterator.next();
    OS os = detectOS();
    Arch arch = detectArchitecture();
    if (iterator.hasNext()) {
      throw new IllegalStateException(String.format(
        "More than one executable provider found, please provide only one executable match with your %s %s and try again.",
        os.getName(), arch.getName()
      ));
    }
    ExecutableProperty property = executableProvider.getProperty();
    if (property == null) {
      throw new NullPointerException("Cannot get executable property");
    }
    if (property.os() != os || property.arch() != arch) {
      throw new IllegalStateException(String.format(
        "Executable you provided (%s %s) doesn't match with current environment (%s %s)",
        property.os().getName(), property.arch().getName(), os.getName(), arch.getName()
      ));
    }
    provider = executableProvider;
    return provider;
  }
}
