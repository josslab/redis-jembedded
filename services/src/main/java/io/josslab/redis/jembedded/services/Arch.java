package io.josslab.redis.jembedded.services;

public enum Arch {
  X86("x86"),
  X86_64("x86_64"),
  AARCH_64("aarch64");

  private final String name;

  Arch(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
