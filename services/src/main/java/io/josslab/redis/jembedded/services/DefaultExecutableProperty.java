package io.josslab.redis.jembedded.services;

public class DefaultExecutableProperty implements ExecutableProperty {

  private OS os;
  private Arch arch;
  private String jarVersion;
  private String binaryVersion;

  public DefaultExecutableProperty() {
  }

  @Override
  public OS os() {
    return os;
  }

  @Override
  public Arch arch() {
    return arch;
  }

  @Override
  public String jarVersion() {
    return jarVersion;
  }

  @Override
  public String binaryVersion() {
    return binaryVersion;
  }

  public DefaultExecutableProperty setOs(OS os) {
    this.os = os;
    return this;
  }

  public DefaultExecutableProperty setArch(Arch arch) {
    this.arch = arch;
    return this;
  }

  public DefaultExecutableProperty setJarVersion(String jarVersion) {
    this.jarVersion = jarVersion;
    return this;
  }

  public DefaultExecutableProperty setBinaryVersion(String binaryVersion) {
    this.binaryVersion = binaryVersion;
    return this;
  }
}
