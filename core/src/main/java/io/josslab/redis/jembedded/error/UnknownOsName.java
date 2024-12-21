package io.josslab.redis.jembedded.error;

public class UnknownOsName extends RuntimeException {

  public UnknownOsName(final String name) {
    super("Unrecognized OS: " + name);
  }

}
