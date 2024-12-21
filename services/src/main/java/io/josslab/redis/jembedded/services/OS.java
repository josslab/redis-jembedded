package io.josslab.redis.jembedded.services;

public enum OS {
  WINDOWS("Windows"),
  UNIX("Unix"),
  MAC_OS_X("macOS");

  private final String name;

  OS(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
