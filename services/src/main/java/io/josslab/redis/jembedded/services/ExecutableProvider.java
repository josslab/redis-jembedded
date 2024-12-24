package io.josslab.redis.jembedded.services;

import java.io.File;

public interface ExecutableProvider {

  File getExecutable();

  ExecutableProperty getProperty();
}
