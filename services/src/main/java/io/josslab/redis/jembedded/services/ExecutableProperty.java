package io.josslab.redis.jembedded.services;

public interface ExecutableProperty {

  OS os();

  Arch arch();

  String jarVersion();

  String binaryVersion();
}
