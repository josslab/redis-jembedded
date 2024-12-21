package io.josslab.redis.jembedded.services.provider;

import io.josslab.redis.jembedded.services.ExecutableDeleteHook;
import io.josslab.redis.jembedded.services.ExecutableProvider;

import java.io.File;

public abstract class AbstractExecutableProvider implements ExecutableProvider {

  private File executable;

  @Override
  public final File getExecutable() {
    if (executable == null) {
      executable = doGetExecutable();
      if (addDeleteHook()) {
        ExecutableDeleteHook.addDeleteFile(executable);
      }
    }
    return executable;
  }

  protected abstract File doGetExecutable();

  protected boolean addDeleteHook() {
    return false;
  }
}
